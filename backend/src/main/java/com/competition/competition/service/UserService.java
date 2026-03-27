package com.competition.competition.service;

import com.competition.competition.dto.UserProfileDto;
import com.competition.competition.entity.User;
import com.competition.competition.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public Optional<UserProfileDto> getProfile(Long userId) {
        if (userId == null) return Optional.empty();
        return Optional.ofNullable(userMapper.findById(userId)).map(u -> UserProfileDto.builder()
                .id(u.getId())
                .username(u.getUsername())
                .role(u.getRole())
                .build());
    }

    public boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        if (userId == null) return false;
        User u = userMapper.findById(userId);
        if (u == null || !passwordEncoder.matches(oldPassword, u.getPasswordHash())) return false;
        userMapper.updatePassword(userId, passwordEncoder.encode(newPassword));
        return true;
    }

    /** 恢复数据：占位，可按需实现从备份恢复等 */
    public boolean restoreData(Long userId) {
        return true;
    }
}
