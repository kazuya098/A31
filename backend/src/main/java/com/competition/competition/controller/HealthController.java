package com.competition.competition.controller;

import com.competition.competition.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 健康检查/框架连通性测试接口。用于确认后端已启动、可访问。
 * 【需填充】：无，可直接保留用于部署后探测。
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public Result<Map<String, String>> health() {
        return Result.ok(Map.of("status", "up", "service", "competition-backend"));
    }
}
