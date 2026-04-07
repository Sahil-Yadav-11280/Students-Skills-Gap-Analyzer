package com.sahil.aianalyzer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SkillData {
    @JsonProperty("skill_id")
    private int skillId;

    private String status;

    @JsonProperty("predicted_probability")
    private double predictedProbability;

    @JsonProperty("master_score_percentage")
    private String masterScorePercentage;

    private String confidence;
}
