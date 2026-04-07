package com.sahil.skillsgapanalyzer.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class DashboardResponse {
    private int totalAttempts;
    private double avgAccuracy;
    private int skillGapsCount;
    private double predictedScore;

    private List<SkillSummary> strongSkills;
    private List<SkillSummary> skillGaps;
    private List<NextAttemptPrediction> nextAttemptProbabilities;
    private List<LearningRecommendation> learningPath;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillSummary{
        private String skillName;
        private int attempts;
        private double masteryPercent;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NextAttemptPrediction{
        private String shortName;
        private double probability;
        private String statusText;
        private String trend;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LearningRecommendation{
        private String title;
        private String description;
        private String priority;
    }
}
