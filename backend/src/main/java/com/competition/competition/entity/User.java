package com.competition.competition.entity;

import lombok.Data;

import java.time.LocalDateTime;
/**
 这个是用于登陆状态界面，设置里面的用户名和登录登出操作的
 */

@Data
public class User {
    private Long id;//用户登录帐号(主键）
    private String username;//用户名称
    private String passwordHash;//密码
    private String role;//用户的权限（管理员/普通用户）
    private LocalDateTime createdAt;//用户创建时间
    private LocalDateTime updatedAt;//用户更新用户设置时间
}

