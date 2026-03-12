package com.competition.competition.controller;

import com.competition.competition.common.Result;
import com.competition.competition.dto.ChangePasswordRequest;
import com.competition.competition.dto.UserProfileDto;
import com.competition.competition.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 设置页：用户名、用户身份、账户操作、退出登录、恢复数据。
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public Result<UserProfileDto> profile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        return userService.getProfile(userId).map(Result::ok).orElseGet(() -> Result.fail(401, "未登录"));
    }

    @PutMapping("/password")
    public Result<Void> changePassword(HttpServletRequest request, @Valid @RequestBody ChangePasswordRequest req) {
        Long userId = (Long) request.getAttribute("currentUserId");
        if (userService.updatePassword(userId, req.getOldPassword(), req.getNewPassword())) {
            return Result.ok(null);
        }
        return Result.fail(400, "原密码错误或修改失败");
    }

    @PostMapping("/restore-data")
    public Result<Void> restoreData(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        userService.restoreData(userId);
        return Result.ok(null);
    }
}
