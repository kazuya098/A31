package com.competition.competition.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 当前用户/设置页面的展示
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    /** 用户主键 ID，对应 user.id */
    private Long id;
    /** 登录用户名，对应 user.username */
    private String username;
    /** 用户角色，如 ADMIN / USER，对应 user.role（管理员还是普通用户） */
    private String role; // 管理员还是用户
}
