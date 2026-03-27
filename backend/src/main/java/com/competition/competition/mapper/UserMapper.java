package com.competition.competition.mapper;

import com.competition.competition.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户表 MyBatis Mapper。
 */
@Mapper
public interface UserMapper {

    User findByUsername(String username);

    User findById(Long id);

    int insert(User user);

    int updatePassword(@Param("id") Long id, @Param("passwordHash") String passwordHash);
}
