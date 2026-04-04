package com.competition.competition.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlgorithmClientService {

    @Value("${algorithm.service.url:https://retrolental-georgette-municipally.ngrok-free.dev/predict}")
    private String algorithmUrl;

    @Value("${algorithm.service.timeout-seconds:30}")
    private int timeoutSeconds;

    public static final int MAX_RETRIES = 3;

    public AlgorithmResult recognize(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            return null;
        }

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            HttpURLConnection conn = null;
            try {
                byte[] fileBytes = imageFile.getBytes();
                
                URL url = new URL(algorithmUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                
                // 使用标准的 boundary 格式
                String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                // 添加 ngrok 所需的 header（如果是通过 ngrok 暴露的服务）
                conn.setRequestProperty("ngrok-skip-browser-warning", "true");
                conn.setConnectTimeout(timeoutSeconds * 1000);
                conn.setReadTimeout(timeoutSeconds * 1000);

                try (OutputStream out = conn.getOutputStream()) {
                    String lineFeed = "\r\n";
                    String filename = imageFile.getOriginalFilename();
                    String contentType = imageFile.getContentType() != null ? imageFile.getContentType() : "image/jpeg";

                    // 正确的 multipart 格式：--boundary\r\n
                    out.write(("--" + boundary).getBytes());
                    out.write(lineFeed.getBytes());
                    out.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"").getBytes());
                    out.write(lineFeed.getBytes());
                    out.write(("Content-Type: " + contentType).getBytes());
                    out.write(lineFeed.getBytes());
                    out.write(lineFeed.getBytes());

                    out.write(fileBytes);

                    // 结束标记：\r\n--boundary--\r\n
                    out.write(lineFeed.getBytes());
                    out.write(("--" + boundary + "--").getBytes());
                    out.write(lineFeed.getBytes());

                    out.flush();
                }

                int responseCode = conn.getResponseCode();
                log.info("Algorithm service response code: {}", responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            response.append(line);
                        }

                        String responseStr = response.toString();
                        log.info("Algorithm service response: {}", responseStr);

                        String id = null;
                        Double confidence = null;
                        String heatmapBase64 = null;

                        // 1. 解析 identity_id
                        int identityIdIndex = responseStr.indexOf("\"identity_id\"");
                        if (identityIdIndex == -1) {
                            identityIdIndex = responseStr.indexOf("\"identityId\"");
                        }
                        log.info("Found identity_id key at index: {}", identityIdIndex);

                        if (identityIdIndex != -1) {
                            int colonIndex = responseStr.indexOf(':', identityIdIndex);
                            if (colonIndex != -1) {
                                // 跳过冒号和空格
                                int valueStart = colonIndex + 1;
                                while (valueStart < responseStr.length() && 
                                       Character.isWhitespace(responseStr.charAt(valueStart))) {
                                    valueStart++;
                                }
                                
                                if (valueStart < responseStr.length()) {
                                    char firstChar = responseStr.charAt(valueStart);
                                    log.info("First char of identity_id value: '{}' (code={})", firstChar, (int)firstChar);
                                    
                                    if (firstChar == '"') {
                                        // 字符串类型："identity_id": "person_001"
                                        int startQuote = valueStart;
                                        int endQuote = responseStr.indexOf('"', startQuote + 1);
                                        if (endQuote != -1) {
                                            id = responseStr.substring(startQuote + 1, endQuote);
                                            log.info("Parsed identity_id (string): {}", id);
                                        }
                                    } else if (Character.isDigit(firstChar) || firstChar == '-') {
                                        // 数字类型："identity_id": 1
                                        int valueEnd = valueStart;
                                        while (valueEnd < responseStr.length() && 
                                               (Character.isDigit(responseStr.charAt(valueEnd)) || 
                                                responseStr.charAt(valueEnd) == '-' ||
                                                responseStr.charAt(valueEnd) == '.')) {
                                            valueEnd++;
                                        }
                                        id = responseStr.substring(valueStart, valueEnd).trim();
                                        log.info("Parsed identity_id (number): {}", id);
                                    }
                                }
                            }
                        }

                        // 2. 解析 confidence
                        int confidenceIndex = responseStr.indexOf("\"confidence\"");
                        log.info("Found confidence key at index: {}", confidenceIndex);
                        if (confidenceIndex != -1) {
                            int colonIndex = responseStr.indexOf(':', confidenceIndex);
                            if (colonIndex != -1) {
                                int valueStart = colonIndex + 1;
                                while (valueStart < responseStr.length() && 
                                       Character.isWhitespace(responseStr.charAt(valueStart))) {
                                    valueStart++;
                                }
                                
                                if (valueStart < responseStr.length()) {
                                    int valueEnd = valueStart;
                                    while (valueEnd < responseStr.length() && 
                                           (Character.isDigit(responseStr.charAt(valueEnd)) || 
                                            responseStr.charAt(valueEnd) == '-' ||
                                            responseStr.charAt(valueEnd) == '.')) {
                                        valueEnd++;
                                    }
                                    if (valueEnd > valueStart) {
                                        try {
                                            confidence = Double.parseDouble(responseStr.substring(valueStart, valueEnd).trim());
                                            log.info("Parsed confidence: {}", confidence);
                                        } catch (NumberFormatException e) {
                                            log.warn("Failed to parse confidence: {}", e.getMessage());
                                        }
                                    }
                                }
                            }
                        }

                        // 3. 解析 heatmap_base64
                        String[] heatmapKeys = {"heatmap_base64", "heatmapBase64", "heatmap_image_base64", "attention_heatmap_base64"};
                        for (String key : heatmapKeys) {
                            int heatmapIndex = responseStr.indexOf("\"" + key + "\"");
                            if (heatmapIndex != -1) {
                                log.info("Found heatmap key '{}' at index: {}", key, heatmapIndex);
                                int colonIndex = responseStr.indexOf(':', heatmapIndex);
                                if (colonIndex != -1) {
                                    int startQuote = responseStr.indexOf('"', colonIndex + 1);
                                    if (startQuote != -1) {
                                        int endQuote = responseStr.indexOf('"', startQuote + 1);
                                        if (endQuote != -1) {
                                            heatmapBase64 = responseStr.substring(startQuote + 1, endQuote);
                                            log.info("Parsed heatmap_base64 (length={}): {}...", heatmapBase64.length(), heatmapBase64.substring(0, Math.min(20, heatmapBase64.length())));
                                            break;
                                        }
                                    }
                                }
                            }
                        }

                        log.info("=== Final Parsed Result ===");
                        log.info("identity_id: {}", id);
                        log.info("confidence: {}", confidence);
                        log.info("heatmap_base64 present: {}", heatmapBase64 != null);

                        if (id != null) {
                            conn.disconnect();
                            return AlgorithmResult.builder()
                                    .identityId(id)
                                    .confidence(confidence)
                                    .heatmapBase64(heatmapBase64)
                                    .build();
                        }
                    }
                } else {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                        StringBuilder errorResponse = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            errorResponse.append(line);
                        }
                        log.warn("Algorithm service error response: {}", errorResponse.toString());
                    }
                }

            } catch (Exception e) {
                log.warn("algorithm call failed (attempt {}/{}): {}", attempt, MAX_RETRIES, e.getMessage());
                e.printStackTrace();

                if (attempt == MAX_RETRIES) {
                    return null;
                }
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

            if (attempt < MAX_RETRIES) {
                try {
                    Thread.sleep(1000L * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }

        return null;
    }

    @lombok.Data
    @lombok.Builder
    public static class AlgorithmResult {
        private String identityId;
        private Double confidence;
        private String heatmapBase64;
        
        /** 新增：算法返回的同一个体多张历史图片 */
        private List<RelatedImageInfo> relatedImages;
        
        /** 新增：算法返回的详细报告内容 */
        private String reportDetails;
        
        @lombok.Data
        @lombok.Builder
        @lombok.NoArgsConstructor
        @lombok.AllArgsConstructor
        public static class RelatedImageInfo {
            private Long imageId;
            private String imagePath;
            private String shotTime;
            private Long recordId;
        }
    }
}