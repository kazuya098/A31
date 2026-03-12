package com.competition.competition.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 同一物种个体列表一行：展示 id、封面图、详细报告(individualId) */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndividualListDto {
    private Long individualId;
    private String coverImagePath;
    private String speciesType;
}
