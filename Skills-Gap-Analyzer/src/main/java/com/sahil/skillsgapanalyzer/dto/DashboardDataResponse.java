package com.sahil.skillsgapanalyzer.dto;

import com.sahil.skillsgapanalyzer.entity.Student;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DashboardDataResponse {
    private StudentProfile profile;
    private OverviewMetrics overview;
    private List<SkillPredictionDto> skillsBreakdown;
    private ActionableInsights insights;

    public static class StudentProfile{
        public String studentName;
        public Integer totalAttempts;
        public String avgAccuracy;
        public String recentAccuracy;
    }

    public static class OverviewMetrics{
        public String predictedScore;
        public String modelConfidence;
        public Integer skillsGap;
        public Integer strongSkills;
    }

    public static class ActionableInsights{
        public List<String> weakSkillsList;
        public List<String> strongSkillsList;
        public String learningPathRecommendation;
        public String performancePrediction;
    }

}
