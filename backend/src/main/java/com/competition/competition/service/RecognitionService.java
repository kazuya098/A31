package com.competition.competition.service;

import com.competition.competition.dto.*;
import com.competition.competition.entity.Individual;
import com.competition.competition.entity.IndividualImage;
import com.competition.competition.entity.RecognitionRecord;
import com.competition.competition.entity.User;
import com.competition.competition.mapper.IndividualImageMapper;
import com.competition.competition.mapper.IndividualMapper;
import com.competition.competition.mapper.RecognitionRecordMapper;
import com.competition.competition.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.Duration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecognitionService {

    private final RecognitionRecordMapper recordMapper;
    private final IndividualMapper individualMapper;
    private final IndividualImageMapper individualImageMapper;
    private final UserMapper userMapper;
    private final AlgorithmClientService algorithmClient;
    private final WebClient webClient;

    @Value("${upload.path:./uploads}")
    private String uploadPath;

    @Value("${algorithm.service.url:https://retrolental-georgette-municipally.ngrok-free.dev/predict}")
    private String algorithmUrl;

    @Value("${algorithm.service.timeout-seconds:30}")
    private int timeoutSeconds;

    private Path uploadDirAbsolute() {
        return Paths.get(uploadPath).toAbsolutePath().normalize();
    }

    /** 将本地文件路径转换为可访问的 URL */
    private String toAccessibleUrl(String localPath) {
        if (localPath == null || localPath.isEmpty()) {
            return null;
        }
        // 提取文件名
        String filename = Paths.get(localPath).getFileName().toString();
        // 返回相对 URL 路径，前端拼接后端域名即可访问
        return "/static/uploads/" + filename;
    }


    /** 上传图片 → 落库 → 调算法 → 归并个体/个体图片 → 返回结果（含报告用字段） */
    public RecognitionResultDto submit(MultipartFile file,
                                       String type,
                                       Long operatorId) throws IOException {
        
        // ====== 新增：先验证并获取文件大小 ======
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        // ====== 关键修复：立即将文件内容读到内存，避免临时文件被清理 ======
        byte[] fileBytes = file.getBytes();
        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();

        // ====== 1. 保存上传的图片到本地（带唯一文件名）======
        String randomId = UUID.randomUUID().toString().replace("-", "");
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String savedFileName = randomId + ext;
        Path savePath = Paths.get(uploadPath).resolve(savedFileName);
        Files.createDirectories(savePath.getParent());
        Files.write(savePath, fileBytes);  // ✅ 直接使用已读取的字节数组

        log.info("Saved upload file to: {}", savePath.toAbsolutePath());

        // ====== 1.5 创建识别记录 ======
        RecognitionRecord record = new RecognitionRecord();
        record.setUserId(operatorId); // 从登录态获取操作者 ID
        record.setImagePath(savePath.toString());
        record.setStatus("pending");
        record.setType(type);
        record.setOperationStatus("正常"); // 设置默认操作状态
        recordMapper.insert(record);

        // ====== 2. 调用算法服务进行识别（传递字节数组而不是 MultipartFile）======
        AlgorithmClientService.AlgorithmResult algoResult = recognizeByAlgorithm(fileBytes, originalFilename, contentType);

        if (algoResult != null) {
            // 识别成功

            // 4.0 处理注意力热力图（可选）
            String heatmapSavedPath = saveHeatmapBase64(algoResult.getHeatmapBase64(), record.getId());

            // 4.1 查找或创建个体（归并同一生物）
            Individual individual = findOrCreateIndividual(
                    algoResult.getIdentityId(),    // 算法返回的身份 ID，如 "person_001"
                    type,              // "human"
                    savePath.toString()                // 图片路径
            );

            // 4.2 更新识别记录的状态和结果
            recordMapper.updateResultAndIndividual(
                    record.getId(),          // 1
                    "done",                  // 状态：已完成
                    algoResult.getIdentityId(),    // "person_001"（数字或字符串）
                    algoResult.getConfidence(),    // ✅ 保存置信度
                    individual.getId(),      // 个体 ID
                    heatmapSavedPath        // 注意力热力图路径（本地路径，可空）
            );
            // SQL: UPDATE recognition_record SET status='done', identity_id='person_001', individual_id=? WHERE id=1

            // 4.3 创建个体图片记录（关联到个体）
            IndividualImage img = new IndividualImage();
            img.setIndividualId(individual.getId());
            img.setImagePath(savePath.toString());
            img.setShotTime(LocalDate.now());
            img.setRecognitionRecordId(record.getId());
            individualImageMapper.insert(img);
            // SQL: INSERT INTO individual_image (...) VALUES (...)

            // 4.4 如果个体没有封面图，设置封面图
            if (individual.getCoverImagePath() == null || individual.getCoverImagePath().isBlank()) {
                updateIndividualCover(individual.getId(), savePath.toString());
            }

            // 4.5 构建并返回 DTO
            return RecognitionResultDto.builder()
                    .taskId(String.valueOf(record.getId()))      // "1"
                    .status("done")                               // "done"
                    .identityId(algoResult.getIdentityId())             // "person_001"
                    .message("识别成功")
                    .imagePath(toAccessibleUrl(savePath.toString()))        // "/static/uploads/a1b2c3d4...jpg"
                    .heatmapPath(toAccessibleUrl(heatmapSavedPath)) // 注意力热力图 URL（可空）
                    .recognitionTime(LocalDateTime.now())
                    .individualId(individual.getId())
                    .confidence(algoResult.getConfidence())
                    .build();

        } else {
            // 识别失败

            recordMapper.updateResultAndIndividual(
                    record.getId(),
                    "failed",
                    null,
                    null,
                    null,
                    null
            );

            return RecognitionResultDto.builder()
                    .taskId(String.valueOf(record.getId()))
                    .status("failed")
                    .message("识别失败")
                    .imagePath(toAccessibleUrl(savePath.toString()))
                    .recognitionTime(LocalDateTime.now())
                    .build();
        }
    }


    private String saveFile(MultipartFile file) {
        Path target = null;
        try {
            Path dir = uploadDirAbsolute();
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            String ext = "";
            String originalName = file.getOriginalFilename();
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf('.'));
            }
            String filename = UUID.randomUUID() + ext;
            target = dir.resolve(filename);
            file.transferTo(target.toFile());
            return target.toString();
        } catch (Exception e) {
            log.warn("save file failed, uploadPath={}, resolvedDir={}, target={}",
                    uploadPath, uploadDirAbsolute(), target, e);
            throw new RuntimeException("保存文件失败", e);
        }
    }

    /**
     * 保存算法生成的注意力热力图（可选）。
     * Python 建议返回：JSON 字段 `heatmap_base64`，内容为图片 base64（可以是 data URI，也可以是纯 base64）。
     */
    private String saveHeatmapBase64(String heatmapBase64, Long recordId) {
        if (heatmapBase64 == null || heatmapBase64.isBlank()) {
            return null;
        }
        try {
            String raw = heatmapBase64.trim();
            String mime = null;

            // 兼容 data URI：data:image/png;base64,xxxx
            if (raw.startsWith("data:")) {
                int semi = raw.indexOf(';');
                int comma = raw.indexOf(',');
                if (semi > 5) mime = raw.substring(5, semi);
                if (comma >= 0) raw = raw.substring(comma + 1);
            }

            // 兼容可能存在的 "base64," 片段
            if (raw.contains("base64,")) {
                int idx = raw.indexOf("base64,") + "base64,".length();
                raw = raw.substring(idx);
            }

            String ext = ".png";
            if (mime != null) {
                String m = mime.toLowerCase();
                if (m.contains("jpeg") || m.contains("jpg")) ext = ".jpg";
                else if (m.contains("webp")) ext = ".webp";
                else if (m.contains("bmp")) ext = ".bmp";
            }

            byte[] bytes = Base64.getDecoder().decode(raw);

            // 注意：toAccessibleUrl() 只取文件名，因此 heatmap 也需要保存到 uploads 根目录
            Path dir = uploadDirAbsolute();
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            String filename = "heatmap_" + recordId + "_" + UUID.randomUUID() + ext;
            Path target = dir.resolve(filename);
            Files.write(target, bytes);
            return target.toString();
        } catch (Exception e) {
            // 注意力热力图不保证一定返回，失败时不影响识别主流程
            log.warn("save heatmap failed: {}", e.getMessage());
            return null;
        }
    }

    private Individual findOrCreateIndividual(String algorithmIdentityId, String speciesType, String firstImagePath) {
        Individual ind = individualMapper.findByAlgorithmIdentityIdAndSpeciesType(algorithmIdentityId, speciesType);
        if (ind != null) return ind;
        Individual newInd = new Individual();
        newInd.setSpeciesType(speciesType);
        newInd.setAlgorithmIdentityId(algorithmIdentityId);
        newInd.setCoverImagePath(firstImagePath);
        individualMapper.insert(newInd);
        return newInd;
    }

    private void updateIndividualCover(Long individualId, String coverPath) {
        individualMapper.updateCover(individualId, coverPath);
    }

    public Optional<RecognitionResultDto> getResult(String taskId) {
        RecognitionRecord r = null;
        try {
            r = recordMapper.findById(Long.parseLong(taskId));
        } catch (NumberFormatException ignored) { }
        return Optional.ofNullable(r).map(rec -> {
            return RecognitionResultDto.builder()
                    .taskId(String.valueOf(rec.getId()))
                    .status(rec.getStatus())
                    .identityId(rec.getIdentityId())
                    .message(rec.getStatus())
                    .imagePath(toAccessibleUrl(rec.getImagePath()))
                    .heatmapPath(toAccessibleUrl(rec.getHeatmapPath()))
                    .recognitionTime(rec.getCreatedAt())
                    .individualId(rec.getIndividualId())
                    .build();
        });
    }

    public List<RecordListDto> listRecords(String type, LocalDateTime startTime, LocalDateTime endTime, Long recordId) {
        List<RecognitionRecord> list = recordMapper.list(type, startTime, endTime, recordId);
        if (list == null) list = Collections.emptyList();
        return list.stream().map(r -> {
            String operatorName = r.getUserId() != null ? Optional.ofNullable(userMapper.findById(r.getUserId())).map(User::getUsername).orElse("") : "";
            return RecordListDto.builder()
                    .id(r.getId())
                    .recognitionTime(r.getCreatedAt())
                    .recognitionResult(r.getIndividualId() != null ? String.valueOf(r.getIndividualId()) : r.getIdentityId())
                    .operatorName(operatorName)
                    .operationStatus(r.getOperationStatus() != null ? r.getOperationStatus() : "正常")
                    .type(r.getType())
                    .build();
        }).collect(Collectors.toList());
    }

    public Optional<RecordReportDto> getRecordReport(Long recordId) {
        return Optional.ofNullable(recordMapper.findById(recordId)).map(r -> {
            // 1. 修复操作员名称获取
            String operatorName = r.getUserId() != null
                    ? Optional.ofNullable(userMapper.findById(r.getUserId())).map(User::getUsername).orElse("演示操作员")
                    : "演示操作员";

            // 2. 【修复 Bug】修正逻辑：如果是 human 则显示人类
            String identityLabel = "human".equalsIgnoreCase(r.getType()) ? "人类" : "非人类";

            // 3. 【新增逻辑】将硬核分析结论“翻译”成客户看得懂的人话
            String humanReadableConclusion = generateConclusion(r.getType(), r.getConfidence());

            return RecordReportDto.builder()
                    .recordId(r.getId())
                    .recognitionResult(identityLabel)
                    .imagePath(toAccessibleUrl(r.getImagePath()))
                    .heatmapPath(toAccessibleUrl(r.getHeatmapPath()))
                    .recognitionTime(r.getCreatedAt())
                    .operatorName(operatorName)
                    .type(r.getType())
                    .operationStatus(r.getOperationStatus())
                    .confidence(r.getConfidence())
                    .build();
        });
    }

    /**
     * 客户友好型结论生成器
     */
    private String generateConclusion(String type, Double confidence) {
        if (confidence == null) {
            return "系统正在分析中，请稍候。";
        }

        String typeCn = "human".equalsIgnoreCase(type) ? "人类" : "非人类目标";

        // 换算成百分比，方便判断
        double percentage = confidence * 100;

        if (percentage >= 90) {
            return String.format("分析完成。图像特征非常清晰，系统以极高的把握（%.2f%%）判定该目标为【%s】。", percentage, typeCn);
        } else if (percentage >= 60) {
            return String.format("分析完成。系统倾向于认为该目标为【%s】（置信度 %.2f%%），但受光线或角度影响，建议人工复核。", percentage, typeCn);
        } else {
            return String.format("分析完成。当前图像质量欠佳或特征不明显，系统无法做出准确判断（置信度仅 %.2f%%），强烈建议人工介入。", percentage);
        }
    }

    public List<IndividualListDto> listIndividuals(String speciesType) {
        String type = "human".equalsIgnoreCase(speciesType) ? "human" : "non_human";
        List<Individual> indList = individualMapper.listBySpeciesType(type);
        if (indList == null) indList = Collections.emptyList();
        return indList.stream()
                .map(i -> IndividualListDto.builder()
                        .individualId(i.getId())
                        .coverImagePath(i.getCoverImagePath())
                        .speciesType(i.getSpeciesType())
                        .build())
                .collect(Collectors.toList());
    }

    public Optional<IndividualReportDto> getIndividualReport(Long individualId) {
        return Optional.ofNullable(individualMapper.findById(individualId)).map(ind -> {
            List<IndividualImage> images = individualImageMapper.listByIndividualIdOrderByShotTime(individualId);
            if (images == null) images = Collections.emptyList();
            List<IndividualReportDto.ImageItemDto> items = images.stream()
                    .map(img -> IndividualReportDto.ImageItemDto.builder()
                            .imageId(img.getId())
                            .imagePath(toAccessibleUrl(img.getImagePath()))
                            .shotTime(img.getShotTime())
                            .recognitionRecordId(img.getRecognitionRecordId())
                            .build())
                    .collect(Collectors.toList());
            return IndividualReportDto.builder()
                    .individualId(ind.getId())
                    .speciesType(ind.getSpeciesType())
                    .coverImagePath(toAccessibleUrl(ind.getCoverImagePath()))
                    .images(items)
                    .build();
        });
    }

    /**
     * 新增方法：使用字节数组调用算法服务
     */
    private AlgorithmClientService.AlgorithmResult recognizeByAlgorithm(byte[] fileBytes, 
                                                                        String originalFilename, 
                                                                        String contentType) {
        
        for (int attempt = 1; attempt <= AlgorithmClientService.MAX_RETRIES; attempt++) {
            HttpURLConnection conn = null;
            try {
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

                    // 正确的 multipart 格式：--boundary\r\n
                    out.write(("--" + boundary).getBytes());
                    out.write(lineFeed.getBytes());
                    out.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + originalFilename + "\"").getBytes());
                    out.write(lineFeed.getBytes());
                    out.write(("Content-Type: " + (contentType != null ? contentType : "image/jpeg")).getBytes());
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

                        // 1. 解析 identity_id（兼容数字和字符串）
                        int identityIdIndex = responseStr.indexOf("\"identity_id\"");
                        if (identityIdIndex == -1) {
                            identityIdIndex = responseStr.indexOf("\"identityId\"");
                        }

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
                                    
                                    if (firstChar == '"') {
                                        // 字符串类型："identity_id": "person_001"
                                        int startQuote = valueStart;
                                        int endQuote = responseStr.indexOf('"', startQuote + 1);
                                        if (endQuote != -1) {
                                            id = responseStr.substring(startQuote + 1, endQuote);
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
                                    }
                                }
                            }
                        }

                        // 2. 解析 confidence
                        int confidenceIndex = responseStr.indexOf("\"confidence\"");
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
                                int colonIndex = responseStr.indexOf(':', heatmapIndex);
                                if (colonIndex != -1) {
                                    int startQuote = responseStr.indexOf('"', colonIndex + 1);
                                    if (startQuote != -1) {
                                        int endQuote = responseStr.indexOf('"', startQuote + 1);
                                        if (endQuote != -1) {
                                            heatmapBase64 = responseStr.substring(startQuote + 1, endQuote);
                                            break;
                                        }
                                    }
                                }
                            }
                        }

                        log.info("Parsed identity_id: {}, confidence: {}, heatmap_base64 present: {}", id, confidence, heatmapBase64 != null);

                        if (id != null) {
                            conn.disconnect();
                            return AlgorithmClientService.AlgorithmResult.builder()
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
                log.warn("algorithm call failed (attempt {}/{}): {}", attempt, AlgorithmClientService.MAX_RETRIES, e.getMessage());
                e.printStackTrace();

                if (attempt == AlgorithmClientService.MAX_RETRIES) {
                    return null;
                }
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

            if (attempt < AlgorithmClientService.MAX_RETRIES) {
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

    // ==================== 批量上传 ====================

    /**
     * 批量上传：逐张调算法、落库，单张失败不影响其余张。
     * shotTimes[i] / imageIds[i] 与 files[i] 按下标对应，由 Controller 传入。
     */
    public List<RecognitionResultDto> submitBatch(
            MultipartFile[] files,
            String type,
            Long operatorId,
            String[] imageIds,
            String[] shotTimes) {

        List<BatchImageMetadata> metaList = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            BatchImageMetadata m = new BatchImageMetadata();
            m.setImageId(imageIds != null && i < imageIds.length ? imageIds[i] : null);
            if (shotTimes != null && i < shotTimes.length && shotTimes[i] != null) {
                try { m.setShotTime(LocalDate.parse(shotTimes[i])); } catch (Exception ignored) {}
            }
            metaList.add(m);
        }

        List<RecognitionResultDto> results = new ArrayList<>();

        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            BatchImageMetadata meta = (i < metaList.size()) ? metaList.get(i) : new BatchImageMetadata();

            // 立即读字节，防止临时文件被清理（与 submit() 相同策略）
            byte[] fileBytes;
            String originalFilename;
            String contentType;
            try {
                fileBytes = file.getBytes();
                originalFilename = file.getOriginalFilename();
                contentType = file.getContentType();
            } catch (IOException e) {
                log.error("Batch item {} read failed: {}", meta.getImageId(), e.getMessage());
                results.add(RecognitionResultDto.builder()
                        .status("failed").message("文件读取失败").build());
                continue;
            }

            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String savedFileName = UUID.randomUUID().toString().replace("-", "") + ext;
            Path savePath = Paths.get(uploadPath).resolve(savedFileName);

            try {
                Files.createDirectories(savePath.getParent());
                Files.write(savePath, fileBytes);
            } catch (IOException e) {
                log.error("Batch item {} save failed: {}", meta.getImageId(), e.getMessage());
                results.add(RecognitionResultDto.builder()
                        .status("failed").message("文件保存失败").build());
                continue;
            }

            // 插 recognition_record，status='pending'
            RecognitionRecord record = new RecognitionRecord();
            record.setUserId(operatorId);
            record.setImagePath(savePath.toString());
            record.setStatus("pending");
            record.setType(type);
            record.setOperationStatus("正常");
            recordMapper.insert(record);

            try {
                // 用 WebClient 调算法（使用内存中的字节，不依赖临时文件）
                AlgorithmClientService.AlgorithmResult algoResult =
                        callAlgorithmWithWebClient(fileBytes, originalFilename, contentType);

                if (algoResult == null) {
                    throw new RuntimeException("算法服务无响应");
                }

                // 保存 heatmap（复用现有私有方法）
                String heatmapPath = saveHeatmapBase64(algoResult.getHeatmapBase64(), record.getId());

                //  查重 / 新建 individual（复用现有私有方法）
                Individual individual = findOrCreateIndividual(
                        algoResult.getIdentityId(), type, savePath.toString());

                //  更新 recognition_record 为 done
                recordMapper.updateResultAndIndividual(
                        record.getId(), "done", algoResult.getIdentityId(),
                        algoResult.getConfidence(), individual.getId(), heatmapPath);

                //  插 individual_image，shot_time 取前端传入值（而非 LocalDate.now()）
                IndividualImage img = new IndividualImage();
                img.setIndividualId(individual.getId());
                img.setImagePath(savePath.toString());
                img.setShotTime(meta.getShotTime() != null ? meta.getShotTime() : LocalDate.now());
                img.setRecognitionRecordId(record.getId());
                individualImageMapper.insert(img);

                // 更新封面（复用现有私有方法）
                if (individual.getCoverImagePath() == null || individual.getCoverImagePath().isBlank()) {
                    updateIndividualCover(individual.getId(), savePath.toString());
                }

                results.add(RecognitionResultDto.builder()
                        .taskId(String.valueOf(record.getId()))
                        .status("done")
                        .identityId(algoResult.getIdentityId())
                        .message("识别成功")
                        .imagePath(toAccessibleUrl(savePath.toString()))
                        .heatmapPath(toAccessibleUrl(heatmapPath))
                        .recognitionTime(LocalDateTime.now())
                        .individualId(individual.getId())
                        .confidence(algoResult.getConfidence())
                        .build());

            } catch (Exception e) {
                // 单张失败：标记 failed，继续处理下一张
                recordMapper.updateResultAndIndividual(
                        record.getId(), "failed", null, null, null, null);
                log.error("Batch item {} failed: {}", meta.getImageId(), e.getMessage());
                results.add(RecognitionResultDto.builder()
                        .taskId(String.valueOf(record.getId()))
                        .status("failed")
                        .message("识别失败: " + e.getMessage())
                        .imagePath(toAccessibleUrl(savePath.toString()))
                        .recognitionTime(LocalDateTime.now())
                        .build());
            }
        }
        return results;
    }

    /**
     * 用 WebClient 调算法，接收内存字节以避免临时文件过期问题。
     * 返回与 recognizeByAlgorithm() 相同的 AlgorithmResult 类型，保持一致性。
     */
    private AlgorithmClientService.AlgorithmResult callAlgorithmWithWebClient(
            byte[] fileBytes, String originalFilename, String contentType) {
        try {
            // 用 ByteArrayResource 包装已读取的字节，并覆盖 getFilename() 传递原始文件名
            ByteArrayResource resource = new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return originalFilename != null ? originalFilename : "image.jpg";
                }
            };

            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", resource)
                   .contentType(MediaType.parseMediaType(
                           contentType != null ? contentType : "image/jpeg"));

            AlgorithmRawResponse raw = webClient.post()
                    .uri(algorithmUrl)
                    .header("ngrok-skip-browser-warning", "true")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(AlgorithmRawResponse.class)
                    .block(Duration.ofSeconds(timeoutSeconds));

            if (raw == null || raw.identityId == null) {
                return null;
            }

            return AlgorithmClientService.AlgorithmResult.builder()
                    .identityId(String.valueOf(raw.identityId))
                    .confidence(raw.confidence)
                    .heatmapBase64(raw.heatmapBase64)
                    .build();
        } catch (Exception e) {
            log.warn("WebClient algorithm call failed: {}", e.getMessage());
            return null;
        }
    }

    /** 算法接口响应的 JSON 结构（字段名与 Python FastAPI 保持一致） */
    private static class AlgorithmRawResponse {
        @JsonProperty("identity_id")
        public Object identityId;      // 兼容数字或字符串

        @JsonProperty("confidence")
        public Double confidence;

        @JsonProperty("heatmap_base64")
        public String heatmapBase64;   // 可为 null
    }
}
