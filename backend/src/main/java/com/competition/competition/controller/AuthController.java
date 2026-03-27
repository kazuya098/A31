package com.competition.competition.controller;

import com.competition.competition.common.Result;
import com.competition.competition.dto.LoginRequest;
import com.competition.competition.dto.LoginResponse;
import com.competition.competition.dto.UserProfileDto;
import com.competition.competition.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        Optional<LoginResponse> res = authService.login(req.getUsername(), req.getPassword());
        return res.map(Result::ok).orElseGet(() -> Result.fail(401, "用户名或密码错误"));
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String token = request.getHeader("X-Auth-Token");
        authService.logout(token);
        return Result.ok(null);
    }

    @GetMapping("/current")
    public Result<UserProfileDto> current(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        Optional<UserProfileDto> user = authService.currentUser(userId);
        return user.map(Result::ok).orElseGet(() -> Result.fail(401, "未登录"));
    }
}
