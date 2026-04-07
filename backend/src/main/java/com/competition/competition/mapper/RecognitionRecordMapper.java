package com.competition.competition.mapper;

import com.competition.competition.entity.RecognitionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 识别记录表 MyBatis Mapper。
 */
@Mapper
public interface RecognitionRecordMapper {

    int insert(RecognitionRecord record);

    RecognitionRecord findById(Long id);

    int updateResultAndIndividual(@Param("id") Long id,
                                  @Param("status") String status,
                                  @Param("identityId") String identityId,
                                  @Param("confidence") Double confidence,
                                  @Param("individualId") Long individualId,
                                  @Param("heatmapPath") String heatmapPath);

    List<RecognitionRecord> list(@Param("type") String type,
                                @Param("startTime") LocalDateTime startTime,
                                @Param("endTime") LocalDateTime endTime,
                                @Param("recordId") Long recordId);

    /** 删除单条识别记录 */
    int deleteById(@Param("id") Long id);

    /** 清空全表（仅用于测试数据清理） */
    int deleteAll();

    /** 插入时指定 created_at（用于演示数据植入） */
    int insertWithCreatedAt(RecognitionRecord record);

    /** 统计指定 operation_status 的记录数 */
    int countByOperationStatus(@Param("operationStatus") String operationStatus);
}
