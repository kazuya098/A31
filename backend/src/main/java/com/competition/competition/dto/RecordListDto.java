package com.competition.competition.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 识别记录列表一行：识别时间、识别结果、详细报告(recordId)、操作者、操作状态 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordListDto {
    private Long id;
    private LocalDateTime recognitionTime;
    private String recognitionResult;  // identity_id 或个体展示 id
    private String operatorName;
    private String operationStatus;
    private String type;  // human / non_human
}
