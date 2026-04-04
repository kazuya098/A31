package com.competition.competition.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class BatchImageMetadata {
    /** 前端标识符，用于日志追踪（如文件名或自定义 ID） */
    private String imageId;

    /** 拍摄日期，格式 yyyy-MM-dd，对应 individual_image.shot_time */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate shotTime;
}
