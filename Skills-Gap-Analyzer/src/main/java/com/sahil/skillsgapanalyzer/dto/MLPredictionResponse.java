package com.sahil.skillsgapanalyzer.dto;

// What Flask sends back to SpringBoot
public class MLPredictionResponse {
    public String status;
    public Double predicted_probability;
    public Double mastery_score_percentage;
    public Double model_confidence;
}
