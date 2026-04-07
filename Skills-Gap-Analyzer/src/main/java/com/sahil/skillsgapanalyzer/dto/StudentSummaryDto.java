package com.sahil.skillsgapanalyzer.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentSummaryDto {
    private Double id;
    private String name;
    private String code;
    private Integer score;
}
