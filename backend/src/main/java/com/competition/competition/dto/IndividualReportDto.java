package com.competition.competition.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/** 个体详细报告：按时间排序的全部图片及信息 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndividualReportDto {
    private Long individualId;
    private String speciesType;
    private String coverImagePath;
    /** 按生长时间顺序排好的图片列表 */
    private List<ImageItemDto> images;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageItemDto {
        private Long imageId;
        private String imagePath;
        private LocalDate shotTime;
        private Long recognitionRecordId;
    }
}
