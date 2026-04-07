package com.sahil.aianalyzer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiPredictionResponse {
    @JsonProperty("api_status")
    private String apiStatus;

    @JsonProperty("skills_data")
    private List<SkillData> skillsData;
}
