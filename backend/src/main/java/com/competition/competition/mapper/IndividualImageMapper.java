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

    /** 删除属于某条识别记录的全部图片（删除记录时级联调用） */
    int deleteByRecordId(@Param("recordId") Long recordId);

    /** 清空全表（仅用于测试数据清理） */
    int deleteAll();
}
