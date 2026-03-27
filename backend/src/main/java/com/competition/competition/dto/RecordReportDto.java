package com.competition.competition.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 单条识别的详细报告（弹窗/下载用）：识别结果、上传图、识别时间、识别编号等 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordReportDto {
    private Long recordId;
    private String recognitionResult;
    private Double confidence;
    private String imagePath;
    private LocalDateTime recognitionTime;
    private String operatorName;
    private String type;
    private String operationStatus;
}
