package com.competition.competition.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

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
    /** 注意力热力图路径（URL），前端用于渲染叠加/可视化 */
    private String heatmapPath;
    /** 识别时间，用于报告 */
    private java.time.LocalDateTime recognitionTime;
    /** 关联个体 id（同一生物展示用），对应 individual.id */
    private Long individualId;
    /** 置信度*/
    private Double confidence;
    
    /** 拍摄时间，用于展示该图片的拍摄时间 */
    private LocalDate shotTime;
    
    /** 详细报告内容，包含特征点匹配率等详细信息 */
    private String reportDetails;
    
    /** 同一个体的多张历史图片列表（包含拍摄时间） */
    private List<IndividualImageInfo> relatedImages;
    
    /** 个体图片信息 DTO */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IndividualImageInfo {
        /** 图片 ID */
        private Long imageId;
        /** 图片路径 */
        private String imagePath;
        /** 拍摄时间 */
        private LocalDate shotTime;
        /** 识别记录 ID */
        private Long recordId;
    }
}
