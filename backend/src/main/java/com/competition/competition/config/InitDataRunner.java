package com.competition.competition.config;

import com.competition.competition.entity.User;
import com.competition.competition.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 首次启动时若没有用户，则创建默认管理员 admin / admin123（仅演示用）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InitDataRunner implements ApplicationRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (userMapper.findByUsername("admin") == null) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setRole("admin");
            userMapper.insert(admin);
            log.info("已创建默认管理员: admin / admin123");
        }
    }
}
