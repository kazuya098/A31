package com.competition.competition.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
/**
 * 个体图片表
 * 同一生物的多张跨时间域图片，每张一行；image_id 不同，individual_id 相同
 */

@Data
public class IndividualImage {
    private Long id;
    //该表的主键，同一个生物一个id（网页展示的编号），同个个体多张跨时间域的图片共用一个id

    private Long individualId;
    //外键->individual.id，同一个生物的多张图片此处相同

    private String imagePath;
    //该个图片的存储路径

    /** 拍摄/时间域日期，对应表 shot_time，用于按时间排序 */
    private LocalDate shotTime;

    private Long recognitionRecordId;
    //看看是来源于拿一条记录

    private LocalDateTime createdAt;
}
