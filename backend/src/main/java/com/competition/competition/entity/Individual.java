package com.competition.competition.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Individual {
    private Long id;
    private String speciesType;
    private String algorithmIdentityId;
    private String coverImagePath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
