package com.competition.competition.config;

import com.competition.competition.entity.Individual;
import com.competition.competition.entity.IndividualImage;
import com.competition.competition.entity.RecognitionRecord;
import com.competition.competition.entity.User;
import com.competition.competition.mapper.IndividualImageMapper;
import com.competition.competition.mapper.IndividualMapper;
import com.competition.competition.mapper.RecognitionRecordMapper;
import com.competition.competition.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 首次启动时：
 *  若没有用户，创建默认管理员 admin / admin123。
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InitDataRunner implements ApplicationRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final IndividualMapper individualMapper;
    private final IndividualImageMapper individualImageMapper;
    private final RecognitionRecordMapper recordMapper;

    @Value("${upload.path:./uploads}")
    private String uploadPath;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 1. 创建默认管理员
        if (userMapper.findByUsername("admin") == null) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setRole("admin");
            userMapper.insert(admin);
            log.info("已创建默认管理员: admin / admin123");
        }

        if (userMapper.findByUsername("管理员admin") == null) {
            User demo = new User();
            demo.setUsername("管理员admin");
            demo.setPasswordHash(passwordEncoder.encode("123456"));
            demo.setRole("admin");
            userMapper.insert(demo);
            log.info("已创建演示管理员: 管理员admin / 123456");
        }
    }
}




