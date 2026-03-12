package com.competition.competition.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class IndividualImage {
    private Long id;
    private Long individualId;
    private String imagePath;
    private LocalDate shotTime;
    private Long recognitionRecordId;
    private LocalDateTime createdAt;
}
