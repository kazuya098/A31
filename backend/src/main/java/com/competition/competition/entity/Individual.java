package com.competition.competition.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 识别记录里面的“同一物种小类”，显示id+第一张图（封面）+详细报告
 */

@Data
public class Individual {
    private Long id;
    //该表的主键，同一个生物一个id（网页展示的编号），同个个体多张跨时间域的图片共用一个id

    private String speciesType;
    //判断是人类还是非人类

    private String algorithmIdentityId;
    //算法返回的Identityid，用于同一个生物的归并（相同identity_id+同type->同一个个体）

    private String coverImagePath;
    //该个体的封面图

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
