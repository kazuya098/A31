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
    /** 识别记录主键 ID，对应 recognition_record.id */
    private Long recordId;
    // 前端“详细报告”需要调用的

    /** 识别结果展示字符串，由 individualId 或 identityId 转成可读内容 */
    private String recognitionResult;
    private String imagePath;
    /** 识别时间，对应 recognition_record.created_at */
    private LocalDateTime recognitionTime;
    /** 操作者名称，一般由 user.username 映射 */
    private String operatorName;
    /** 识别类型，如 human / non_human，对应 recognition_record.type */
    private String type;
    /** 业务操作状态，如 待确认/已确认，对应 recognition_record.operation_status */
    private String operationStatus;
}
