package com.competition.competition.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** 同一物种个体列表一行：展示 id、封面图、详细报告(individualId) */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndividualListDto {
    /** 个体主键 ID，对应 individual.id */
    private Long individualId;
    /** 个体封面图片路径，用于列表缩略图展示 */
    private String coverImagePath;
    /** 物种类型标识，如 human / non_human */
    private String speciesType;
    /** 该个体关联的影像总数 */
    private Integer imageCount;
    /** 该个体最近一次目击日期（最新 individual_image.shot_time） */
    private LocalDate latestShotTime;
}
