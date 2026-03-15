package com.competition.competition.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.Map;

/**
 * 调用 Python 算法推理服务的 HTTP 客户端。
 * 约定：POST multipart/form-data 字段 "file" 为图片；响应 JSON 至少包含 identity_id（rank-1 个体 ID）。
 * 配置：algorithm.service.url（如 http://localhost:5000/recognize）、algorithm.service.timeout-seconds。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlgorithmClientService {

    private final WebClient webClient;//spring容器注入的bean

    @Value("${algorithm.service.url:http://localhost:5000/recognize}")
    private String algorithmUrl;

    @Value("${algorithm.service.timeout-seconds:30}")
    private int timeoutSeconds;

    private static final int MAX_RETRIES = 3;

    /**
     * 调用算法服务进行跨时面部识别，带重试（网络波动时自动重试，失败返回 null）。
     *
     * @param imageFile 上传的面部图片
     * @return 识别结果（rank-1 身份 ID）；失败时返回 null
     */
    // AlgorithmClientService.java 第 47-98 行
    public AlgorithmResult recognize(MultipartFile imageFile) {

        // ====== 第 1 步：验证文件 ======
        if (imageFile == null || imageFile.isEmpty()) {
            return null;
        }

        // ====== 第 2 步：准备重试机制 ======
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            // MAX_RETRIES = 3，最多重试 3 次

            try {
                // ====== 第 3 步：构建 multipart/form-data 请求体 ======
                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("file", new ByteArrayResource(imageFile.getBytes()) {
                    @Override
                    public String getFilename() {
                        return imageFile.getOriginalFilename();
                        // 返回："face.jpg"
                    }
                });

                // ====== 第 4 步：用 WebClient 发送 HTTP 请求到 Python 服务 ======
                Map<String, Object> data = webClient.post()
                        .uri(algorithmUrl)
                        // algorithmUrl = "http://localhost:5000/recognize"

                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        // 设置 Content-Type: multipart/form-data

                        .body(BodyInserters.fromMultipartData(body))
                        // 放入请求体（包含图片数据）

                        .retrieve()
                        // 发起请求！

                        .bodyToMono(Map.class)
                        // 期望响应是 JSON 格式，自动解析为 Map

                        .block(Duration.ofSeconds(timeoutSeconds));
                // 阻塞等待最多 30 秒

                // ====== 第 5 步：解析 Python 服务的响应 ======
                if (data != null) {
                    // 假设 Python 返回：{"identity_id": "person_001"}
                    String id = (String) data.get("identity_id");
                    // 先尝试下划线命名

                    if (id == null) {
                        id = (String) data.get("identityId");
                        // 再尝试驼峰命名（兼容不同命名规范）
                    }

                    if (id != null) {
                        // 成功获取身份 ID
                        return AlgorithmResult.builder()
                                .identityId(id)  // "person_001"
                                .build();
                    }
                }

                return null;  // 响应格式不对

            } catch (WebClientResponseException e) {
                // 捕获 HTTP 错误（如 4xx、5xx）
                log.warn("algorithm call failed (attempt {}/{}), status={}, body={}",
                        attempt, MAX_RETRIES, e.getStatusCode(), e.getResponseBodyAsString());

                if (attempt == MAX_RETRIES) {
                    return null;  // 最后一次重试失败，放弃
                }

            } catch (Exception e) {
                // 捕获其他异常（网络超时、DNS 解析失败等）
                log.warn("algorithm call failed (attempt {}/{}): {}",
                        attempt, MAX_RETRIES, e.getMessage());

                if (attempt == MAX_RETRIES) {
                    return null;  // 最后一次重试失败，放弃
                }
            }

            // ====== 第 6 步：重试前的等待（指数退避）======
            if (attempt < MAX_RETRIES) {
                try {
                    Thread.sleep(1000L * attempt);
                    // 第 1 次失败后等 1 秒
                    // 第 2 次失败后等 2 秒
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }

        return null;  // 所有重试都失败了
    }

    @lombok.Data
    @lombok.Builder
    public static class AlgorithmResult {
        /** rank-1 识别出的身份 ID */
        private String identityId;
    }
}
