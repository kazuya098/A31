package com.competition.competition.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 返回给前端的识别结果 DTO。
 * 【需填充】：与前端约定字段名；与算法返回结构对齐后再映射到此 DTO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecognitionResultDto {

    private String taskId;
    private String status;
    private String identityId;
    private Double confidence;
    private String message;
    /** 上传图片路径，用于报告展示 */
    private String imagePath;
    /** 识别时间，用于报告 */
    private java.time.LocalDateTime recognitionTime;
    /** 关联个体 id（同一生物展示用） */
    private Long individualId;
}
