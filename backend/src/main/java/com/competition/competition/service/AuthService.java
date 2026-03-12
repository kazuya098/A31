package com.competition.competition.service;

import com.competition.competition.config.AuthTokenStore;
import com.competition.competition.dto.LoginResponse;
import com.competition.competition.dto.UserProfileDto;
import com.competition.competition.entity.User;
import com.competition.competition.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenStore tokenStore;

    public Optional<LoginResponse> login(String username, String password) {
        return Optional.ofNullable(userMapper.findByUsername(username))
                .filter(u -> passwordEncoder.matches(password, u.getPasswordHash()))
                .map(u -> {
                    String token = tokenStore.put(u.getId());
                    return LoginResponse.builder()
                            .token(token)
                            .user(toProfile(u))
                            .build();
                });
    }

    public void logout(String token) {
        tokenStore.remove(token);
    }

    public Optional<UserProfileDto> currentUser(Long userId) {
        if (userId == null) return Optional.empty();
        return Optional.ofNullable(userMapper.findById(userId)).map(this::toProfile);
    }

    private UserProfileDto toProfile(User u) {
        return UserProfileDto.builder()
                .id(u.getId())
                .username(u.getUsername())
                .role(u.getRole())
                .build();
    }
}
