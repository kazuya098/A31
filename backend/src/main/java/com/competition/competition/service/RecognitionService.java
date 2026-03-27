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

    /** 上传图片 → 落库 → 调算法 → 归并个体/个体图片 → 返回结果（含报告用字段） */
    public RecognitionResultDto submit(MultipartFile file, String type, Long operatorUserId) {
        String savedPath = saveFile(file);
        String recordType = "human".equalsIgnoreCase(type) ? "human" : "non_human";

        RecognitionRecord record = new RecognitionRecord();
        record.setUserId(operatorUserId);
        record.setImagePath(savedPath);
        record.setStatus("processing");
        record.setType(recordType);
        record.setOperationStatus("正常");
        recordMapper.insert(record);

        AlgorithmClientService.AlgorithmResult algo = algorithmClient.recognize(file);
        if (algo != null) {
            Individual individual = findOrCreateIndividual(algo.getIdentityId(), recordType, savedPath);
            recordMapper.updateResultAndIndividual(record.getId(), "done", algo.getIdentityId(), algo.getConfidence(), individual.getId());
            IndividualImage img = new IndividualImage();
            img.setIndividualId(individual.getId());
            img.setImagePath(savedPath);
            img.setShotTime(LocalDate.now());
            img.setRecognitionRecordId(record.getId());
            individualImageMapper.insert(img);
            if (individual.getCoverImagePath() == null || individual.getCoverImagePath().isBlank()) {
                updateIndividualCover(individual.getId(), savedPath);
            }
            return RecognitionResultDto.builder()
                    .taskId(String.valueOf(record.getId()))
                    .status("done")
                    .identityId(algo.getIdentityId())
                    .confidence(algo.getConfidence())
                    .message("识别成功")
                    .imagePath(savedPath)
                    .recognitionTime(LocalDateTime.now())
                    .individualId(individual.getId())
                    .build();
        } else {
            recordMapper.updateResultAndIndividual(record.getId(), "failed", null, null, null);
            return RecognitionResultDto.builder()
                    .taskId(String.valueOf(record.getId()))
                    .status("failed")
                    .message("识别失败")
                    .imagePath(savedPath)
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
                    .confidence(rec.getConfidence())
                    .message(rec.getStatus())
                    .imagePath(rec.getImagePath())
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
                    .confidence(r.getConfidence())
                    .imagePath(r.getImagePath())
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
                            .imagePath(img.getImagePath())
                            .shotTime(img.getShotTime())
                            .recognitionRecordId(img.getRecognitionRecordId())
                            .build())
                    .collect(Collectors.toList());
            return IndividualReportDto.builder()
                    .individualId(ind.getId())
                    .speciesType(ind.getSpeciesType())
                    .coverImagePath(ind.getCoverImagePath())
                    .images(items)
                    .build();
        });
    }
}
