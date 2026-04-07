package com.sahil.skillsgapanalyzer.dto;

import java.util.Map;

// What Flask sends back to SpringBoot
public class MLPredictionResponse {
    public String api_status;
    public Map<String , SkillPredictionData> skills_data;

    public static class SkillPredictionData {
        public Double predicted_probability;
        public Double mastery_score_percentage;
        public Double model_confidence;
    }
}
