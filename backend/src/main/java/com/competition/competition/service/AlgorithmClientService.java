package com.competition.competition.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 调用队友 Python 算法服务的客户端（推理接口）。
 * 【需填充】：
 * - algorithm.service.url 在 application.properties 中配置，如 http://localhost:5000/recognize
 * - 与队友约定接口：通常是 POST 上传图片，返回 { "identity_id": "xxx", "confidence": 0.95 } 等。
 * - 根据对方实际请求格式调整（multipart 或 base64 JSON）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlgorithmClientService {

    private final RestTemplate restTemplate;

    @Value("${algorithm.service.url:http://localhost:5000/recognize}")
    private String algorithmUrl;

    /**
     * 调用算法服务进行跨时面部识别。
     *
     * @param imageFile 上传的面部图片
     * @return 识别结果，如 identityId、confidence；失败时返回 null 或抛异常
     */
    public AlgorithmResult recognize(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            return null;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(imageFile.getBytes()) {
                @Override
                public String getFilename() {
                    return imageFile.getOriginalFilename();
                }
            });
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(algorithmUrl, HttpMethod.POST, request, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> data = response.getBody();
                // 兼容 identity_id / identityId，confidence 由算法同学返回
                String id = (String) data.get("identity_id");
                if (id == null) id = (String) data.get("identityId");
                Object confObj = data.get("confidence");
                if (confObj == null) confObj = data.get("score");
                Double conf = confObj != null ? ((Number) confObj).doubleValue() : null;
                return AlgorithmResult.builder()
                        .identityId(id)
                        .confidence(conf)
                        .build();
            }
        } catch (Exception e) {
            log.warn("algorithm call failed: {}", e.getMessage());
        }
        return null;
    }

    @lombok.Data
    @lombok.Builder
    public static class AlgorithmResult {
        private String identityId;
        private Double confidence;
    }
}
