package com.sahil.skillsgapanalyzer.dto;


//What springboot sends to Flask
public class MlPredictionRequest {
    public Integer skill_attempts;
    public Double skill_accuracy;
    public Integer total_attempts;
    public Double overall_accuracy;
    public Double recent_accuracy;
    public Integer hintCount;
    public Double timeTaken;
}
