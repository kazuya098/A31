package com.competition.competition.mapper;

import com.competition.competition.entity.IndividualImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 个体图片表 MyBatis Mapper。
 */
@Mapper
public interface IndividualImageMapper {

    int insert(IndividualImage image);

    List<IndividualImage> listByIndividualIdOrderByShotTime(@Param("individualId") Long individualId);
}
