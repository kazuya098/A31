package com.competition.competition.dto;

import lombok.Data;

/**
 * 识别请求 DTO（若前端用 JSON 传参可在此定义）。
 * 当前上传为 MultipartFile，本类可作为扩展（例如传入 URL 或 base64 时使用）。
 * 【需填充】：按前端/算法约定补充字段与校验注解（如 @NotNull）。
 */
@Data
public class RecognitionRequest {
    // 例如：String imageUrl; 或 String imageBase64;
}
