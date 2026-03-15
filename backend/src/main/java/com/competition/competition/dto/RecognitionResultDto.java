package com.competition.competition.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 返回给前端的识别结果 DTO。，上传识别接口返回，以及弹窗报告的使用
 * 【需填充】：与前端约定字段名；与算法返回结构对齐后再映射到此 DTO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecognitionResultDto {

    /** 识别任务编号，可与算法服务约定用于追踪一次任务 */
    private String taskId;

    /** 识别状态，如 success / failed / running */
    private String status;

    /** 算法识别到的身份 ID（rank-1），通常映射到 individual.algorithmIdentityId */
    private String identityId;

    /** 补充信息，如错误原因或提示文案 */
    private String message;

    /** 上传图片路径，用于报告展示 */
    private String imagePath;
    /** 识别时间，用于报告 */
    private java.time.LocalDateTime recognitionTime;
    /** 关联个体 id（同一生物展示用），对应 individual.id */
    private Long individualId;
}
