package com.competition.competition.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/** 个体详细报告：展示同一个体的全部图片（按上传时间排序） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndividualReportDto {
    /** 个体主键 ID，对应 individual.id */
    private Long individualId;
    /** 物种类型标识，如 human / non_human */
    private String speciesType;
    /** 个体封面图片路径，用于个体列表和详细报告展示 */
    private String coverImagePath;
    /** 按上传时间顺序排好的图片列表（shotTime 可选，仅作展示用） */
    private List<ImageItemDto> images;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageItemDto {
        /** 图片记录主键 ID，对应 individual_image.id */
        private Long imageId;
        /** 图片存储路径（URL 或相对路径） */
        private String imagePath;
        /** 拍摄时间，可为空，用于时间轴展示 */
        private LocalDate shotTime;
        /** 关联的识别记录 ID，对应 recognition_record.id */
        private Long recognitionRecordId;
    }
}
