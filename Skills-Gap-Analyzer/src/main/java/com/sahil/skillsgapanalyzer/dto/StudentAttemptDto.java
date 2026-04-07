package com.sahil.skillsgapanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentAttemptDto {
    private Long id;
    private String skill;
    private Integer correct;
    private Long actionNum;
    private Integer hintCount;
    private Double timeTaken;
}
