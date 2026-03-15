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
    /** 识别记录主键 ID，对应 recognition_record.id */
    private Long id;

    /** 识别时间/创建时间，对应 recognition_record.created_at */
    private LocalDateTime recognitionTime;

    /** 识别结果展示字符串，通常由 identity_id 或个体展示 id 生成 */
    private String recognitionResult;  // identity_id 或个体展示 id

    /** 操作者名称，一般由 user.username 映射 */
    private String operatorName;

    /** 操作状态，如 待确认/已确认，对应 recognition_record.operation_status */
    private String operationStatus;

    /** 识别类型，如 human / non_human，对应 recognition_record.type */
    private String type;  // human / non_human
}
