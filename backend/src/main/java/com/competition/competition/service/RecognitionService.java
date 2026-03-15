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
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecognitionService {

    private final RecognitionRecordMapper recordMapper;
    private final IndividualMapper individualMapper;
    private final IndividualImageMapper individualImageMapper;
    private final UserMapper userMapper;
    private final AlgorithmClientService algorithmClient;

    @Value("${upload.path:./uploads}")
    private String uploadPath;

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
    // RecognitionService.java 第 56-100 行
    public RecognitionResultDto submit(MultipartFile file, String type, Long operatorUserId) {

        // ====== 阶段 1：保存图片到本地 ======
        String savedPath = saveFile(file);
        // 调用 saveFile() 方法（第 102-121 行）
        // 1. 创建上传目录：./uploads/
        // 2. 生成唯一文件名：UUID + 原扩展名
        // 3. 保存文件：file.transferTo(target.toFile())
        // 4. 返回路径

        // 确定识别类型
        String recordType = "human".equalsIgnoreCase(type) ? "human" : "non_human";

        // ====== 阶段 2：创建识别记录（数据库落库）======
        RecognitionRecord record = new RecognitionRecord();
        record.setUserId(operatorUserId);           // 1 (admin 的 ID)
        record.setImagePath(savedPath);             // 图片保存路径
        record.setStatus("processing");             // 状态：处理中
        record.setType(recordType);                 // "human"
        record.setOperationStatus("正常");
        recordMapper.insert(record);                // ← 插入数据库


        // ====== 阶段 3：调用 Python 算法服务（关键！）======
        AlgorithmClientService.AlgorithmResult algo = algorithmClient.recognize(file);
        // 这里就是调用 AlgorithmClientService 的地方！


        // ====== 阶段 4：处理算法返回结果 ======
        if (algo != null) {
            // 识别成功

            // 4.1 查找或创建个体（归并同一生物）
            Individual individual = findOrCreateIndividual(
                    algo.getIdentityId(),    // 算法返回的身份 ID，如 "person_001"
                    recordType,              // "human"
                    savedPath                // 图片路径
            );

            // 4.2 更新识别记录的状态和结果
            recordMapper.updateResultAndIndividual(
                    record.getId(),          // 1
                    "done",                  // 状态：已完成
                    algo.getIdentityId(),    // "person_001"
                    null,                    // confidence（置信度，暂不保存）
                    individual.getId()       // 个体 ID
            );
            // SQL: UPDATE recognition_record SET status='done', identity_id='person_001', individual_id=? WHERE id=1

            // 4.3 创建个体图片记录（关联到个体）
            IndividualImage img = new IndividualImage();
            img.setIndividualId(individual.getId());
            img.setImagePath(savedPath);
            img.setShotTime(LocalDate.now());
            img.setRecognitionRecordId(record.getId());
            individualImageMapper.insert(img);
            // SQL: INSERT INTO individual_image (...) VALUES (...)

            // 4.4 如果个体没有封面图，设置封面图
            if (individual.getCoverImagePath() == null || individual.getCoverImagePath().isBlank()) {
                updateIndividualCover(individual.getId(), savedPath);
            }

            // 4.5 构建并返回 DTO
            return RecognitionResultDto.builder()
                    .taskId(String.valueOf(record.getId()))      // "1"
                    .status("done")                               // "done"
                    .identityId(algo.getIdentityId())             // "person_001"
                    .message("识别成功")
                    .imagePath(toAccessibleUrl(savedPath))        // "/static/uploads/a1b2c3d4...jpg"
                    .recognitionTime(LocalDateTime.now())
                    .individualId(individual.getId())
                    .build();

        } else {
            // 识别失败

            recordMapper.updateResultAndIndividual(
                    record.getId(),
                    "failed",
                    null,
                    null,
                    null
            );

            return RecognitionResultDto.builder()
                    .taskId(String.valueOf(record.getId()))
                    .status("failed")
                    .message("识别失败")
                    .imagePath(toAccessibleUrl(savedPath))
                    .recognitionTime(LocalDateTime.now())
                    .build();
        }
    }


    private String saveFile(MultipartFile file) {
        try {
            Path dir = Paths.get(uploadPath);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            String ext = "";
            String originalName = file.getOriginalFilename();
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf('.'));
            }
            String filename = UUID.randomUUID() + ext;
            Path target = dir.resolve(filename);
            file.transferTo(target.toFile());
            return target.toString();
        } catch (Exception e) {
            log.warn("save file failed: {}", e.getMessage());
            throw new RuntimeException("保存文件失败", e);
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
            String operatorName = r.getUserId() != null ? Optional.ofNullable(userMapper.findById(r.getUserId())).map(User::getUsername).orElse("") : "";
            return RecordReportDto.builder()
                    .recordId(r.getId())
                    .recognitionResult(r.getIndividualId() != null ? String.valueOf(r.getIndividualId()) : r.getIdentityId())
                    .imagePath(toAccessibleUrl(r.getImagePath()))
                    .recognitionTime(r.getCreatedAt())
                    .operatorName(operatorName)
                    .type(r.getType())
                    .operationStatus(r.getOperationStatus())
                    .build();
        });
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
}
