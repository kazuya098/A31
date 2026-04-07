package com.competition.competition.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class BatchImageMetadata {
    /** 前端标识符，用于日志追踪（如文件名或自定义 ID）。支持 camelCase 和 snake_case JSON key。 */
    @JsonProperty("image_id")
    private String imageId;

    /** 拍摄日期，格式 yyyy-MM-dd，对应 individual_image.shot_time */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("shot_time")
    private LocalDate shotTime;
}
