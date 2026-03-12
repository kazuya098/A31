package com.competition.competition.mapper;

import com.competition.competition.entity.Individual;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 个体表 MyBatis Mapper。
 */
@Mapper
public interface IndividualMapper {

    int insert(Individual individual);

    Individual findById(Long id);

    Individual findByAlgorithmIdentityIdAndSpeciesType(@Param("algorithmIdentityId") String algorithmIdentityId,
                                                        @Param("speciesType") String speciesType);

    List<Individual> listBySpeciesType(String speciesType);

    int updateCover(@Param("id") Long id, @Param("coverImagePath") String coverImagePath);
}
