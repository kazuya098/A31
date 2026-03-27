package com.competition.competition.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 识别记录实体，对应数据库表。
 * 【需填充】：字段与表结构一致；若用 JPA 可加 @Table、@Column；若用 JDBC 则手写映射。
 */
@Data
public class RecognitionRecord {

    private Long id;
    /** 用户 ID（若暂无用户系统可先为空或去掉） */
    private Long userId;
    /** 上传图片存储路径或 URL */
    private String imagePath;
    /** 任务状态：pending / processing / done / failed */
    private String status;
    /** 识别结果：个体 ID（算法返回） */
    private String identityId;
    private Double confidence;
    /** human=人类识别, non_human=非人类识别 */
    private String type;
    /** 操作状态：正常/异常 */
    private String operationStatus;
    /** 关联个体表 id（同一生物多张图共用一个） */
    private Long individualId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
