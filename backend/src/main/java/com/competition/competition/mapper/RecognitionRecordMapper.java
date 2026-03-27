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
}
