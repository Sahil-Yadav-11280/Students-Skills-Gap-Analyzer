package com.sahil.skillsgapanalyzer.service;

import com.sahil.skillsgapanalyzer.dto.*;
import com.sahil.skillsgapanalyzer.integration.MlPredictionClient; // Adjust if package differs
import com.sahil.skillsgapanalyzer.entity.Student;
import com.sahil.skillsgapanalyzer.entity.StudentAttempt;
import com.sahil.skillsgapanalyzer.integration.MlPredictionClient;
import com.sahil.skillsgapanalyzer.repository.StudentAttemptRepository;
import com.sahil.skillsgapanalyzer.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final StudentRepository studentRepository;
    private final StudentAttemptRepository studentAttemptRepository;
    private final MlPredictionClient mlPredictionClient;

    // Constructor Injection
    public DashboardService(StudentRepository studentRepository,
                            StudentAttemptRepository studentAttemptRepository,
                            MlPredictionClient mlPredictionClient) {
        this.studentRepository = studentRepository;
        this.studentAttemptRepository = studentAttemptRepository;
        this.mlPredictionClient = mlPredictionClient;
    }

    public DashboardDataResponse getStudentDashboard(Long studentId) {
        // 1. Fetching student and their attempt history
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student with id: " + studentId + " not found!"));

        List<StudentAttempt> history = studentAttemptRepository.findByStudent_IdOrderByActionNumAsc(studentId);

        if (history.isEmpty()) {
            throw new RuntimeException("No attempt history found for this student");
        }

        // 2. Calculate Overall Metrics
        int totalAttempts = history.size();
        long totalCorrect = history.stream().filter(a -> a.getCorrect() == 1).count();
        double overallAccuracy = (double) totalCorrect / totalAttempts;

        // Calculate recent accuracy (last 3 attempts)
        int recentCount = Math.min(3, totalAttempts);
        List<StudentAttempt> recentAttempts = history.subList(totalAttempts - recentCount, totalAttempts);
        long recentCorrect = recentAttempts.stream().filter(a -> a.getCorrect() == 1).count();
        double recentAccuracy = (double) recentCorrect / recentCount;


//        ============================================
//                          AI CALL
//        ============================================

        List<Integer> aiSkillsSequence = new ArrayList<>();
        List<Integer> aiCorrectSequence = new ArrayList<>();

        for(StudentAttempt attempt: history){
            aiSkillsSequence.add(getSkillIdFromName(attempt.getSkill()));
            aiCorrectSequence.add(attempt.getCorrect());
        }

        MlPredictionRequest mlRequest = new MlPredictionRequest(aiSkillsSequence , aiCorrectSequence);
        MLPredictionResponse mlResponse = mlPredictionClient.getPrediction(mlRequest);

        if (mlResponse == null || mlResponse.skills_data == null) {
            throw new RuntimeException("ML service failed");
        }

//        ==============================================
//                         DASHBOARD
//        ==============================================

        Map<String, List<StudentAttempt>> attemptsBySkill = history.stream()
                .collect(Collectors.groupingBy(StudentAttempt::getSkill));

        List<SkillPredictionDto> skillPredictions = new ArrayList<>();
        List<String> weakSkills = new ArrayList<>();
        List<String> strongSkills = new ArrayList<>();
        double totalMasteryScore = 0.0;
        double totalConfidence = 0.0;

        // Loop through the specific skills the student has tried to build their UI cards
        for (Map.Entry<String, List<StudentAttempt>> entry : attemptsBySkill.entrySet()) {
            String skillName = entry.getKey();
            List<StudentAttempt> skillHistory = entry.getValue();

            int skillAttempts = skillHistory.size();
            double avgTime = skillHistory.stream().mapToDouble(StudentAttempt::getTimeTaken).average().orElse(0.0);
            int totalHints = skillHistory.stream().mapToInt(StudentAttempt::getHintCount).sum();

            // Look up the AI's prediction for this specific skill from our 1 API response
            int aiSkillId = getSkillIdFromName(skillName);
            MLPredictionResponse.SkillPredictionData aiData = mlResponse.skills_data.get(String.valueOf(aiSkillId));

            if (aiData == null) continue; // Safety check

            // Map the ML data to your original UI DTO
            SkillPredictionDto dto = new SkillPredictionDto();
            dto.setSkill(skillName);
            dto.setMasteryScore(aiData.mastery_score_percentage);
            dto.setConfidence(aiData.model_confidence);
            dto.setAvgTime((double) (Math.round(avgTime * 10.0) / 10));
            dto.setTotalHints(totalHints);

            // Determine status based on probability
            double probab = aiData.predicted_probability;
            if (probab < 0.60) {
                dto.setStatus("Critical Weakness");
                weakSkills.add(skillName);
            } else if (probab < 0.75) {
                dto.setStatus("Needs Practice");
            } else if (probab < 0.85) {
                dto.setStatus("Proficient");
            } else {
                dto.setStatus("Mastered");
                strongSkills.add(skillName);
            }

            // Determine Trend (Using math since we aren't passing it to AI anymore)
            if (recentAccuracy > overallAccuracy + 0.1) dto.setTrend("Improving 📈");
            else if (recentAccuracy < overallAccuracy - 0.1) dto.setTrend("Declining📉");
            else dto.setTrend("Stable");

            skillPredictions.add(dto);
            totalMasteryScore += aiData.mastery_score_percentage;
            totalConfidence += aiData.model_confidence;
        }

        // Sort skills: lowest mastery first
        skillPredictions.sort(Comparator.comparingDouble(SkillPredictionDto::getMasteryScore));

        // Assemble Final Response
        DashboardDataResponse finalResponse = new DashboardDataResponse();

        DashboardDataResponse.StudentProfile profile = new DashboardDataResponse.StudentProfile();
        profile.studentName = student.getName() != null ? student.getName() : "Unknown Student";
        profile.totalAttempts = totalAttempts;
        profile.avgAccuracy = String.format("%.1f%%", overallAccuracy * 100);
        profile.recentAccuracy = String.format("%.1f%%", recentAccuracy * 100);
        finalResponse.setProfile(profile);

        DashboardDataResponse.OverviewMetrics overview = new DashboardDataResponse.OverviewMetrics();
        overview.skillsGap = weakSkills.size();
        overview.strongSkills = strongSkills.size();
        overview.predictedScore = String.format("%.1f%%", totalMasteryScore / attemptsBySkill.size());
        overview.modelConfidence = String.format("%.1f%%", totalConfidence / attemptsBySkill.size());
        finalResponse.setOverview(overview);

        DashboardDataResponse.ActionableInsights insights = new DashboardDataResponse.ActionableInsights();
        insights.weakSkillsList = weakSkills;
        insights.strongSkillsList = strongSkills;
        insights.learningPathRecommendation = !weakSkills.isEmpty() ?
                "Focus immediately on: " + weakSkills.get(0) + "." : "Great job! Proceed to new topics.";
        insights.performancePrediction = recentAccuracy >= overallAccuracy ?
                "Student is performing Steadily or improving" : "Student has struggled recently.";

        finalResponse.setInsights(insights);
        finalResponse.setSkillsBreakdown(skillPredictions);

        return finalResponse;
    }

    private int getSkillIdFromName(String skillName) {
        if (skillName == null) return 21; // Fallback to "noskill" if null

        switch (skillName.toLowerCase().trim()) {
            case "properties-of-geometric-figures": return 0;
            case "sum-of-interior-angles-more-than-3-sides": return 1;
            case "point-plotting": return 2;
            case "transformations-rotations": return 3;
            case "reading-graph": return 4;
            case "area": return 5;
            case "perimeter": return 6;
            case "square-root": return 7;
            case "isosceles-triangle": return 8;
            case "application: isosceles triangle": return 9;
            case "multiplying-decimals": return 10;
            case "proportion": return 11;
            case "pythagorean-theorem": return 12;
            case "interpreting-linear-equations": return 13;
            case "pattern-finding": return 14;
            case "application: compare points": return 15;
            case "application: multi-column subtraction": return 16;
            case "application: simple multiplication": return 17;
            case "application: compare expressions": return 18;
            case "application: order of operations": return 19;
            case "application: multi-column addition": return 20;
            case "noskill": return 21;
            case "application: read points": return 22;
            case "application: find slope in graph": return 23;
            case "p-patterns-relations-algebra": return 24;
            case "percent-of": return 25;
            case "venn-diagram": return 26;
            case "equivalent-fractions-decimals-percents": return 27;
            case "of-means-multiply": return 28;
            case "fraction-multiplication": return 29;
            case "supplementary-angles": return 30;
            case "transversals": return 31;
            case "triangle-inequality": return 32;
            case "multiplication": return 33;
            case "equation-solving": return 34;
            case "discount": return 35;
            case "sum-of-interior-angles-triangle": return 36;
            case "inducing-functions": return 37;
            case "subtraction": return 38;
            case "addition": return 39;
            case "division": return 40;
            case "divide-decimals": return 41;
            case "making-sense-of-expressions-and-equations": return 42;
            case "ordering-numbers": return 43;
            case "fraction-division": return 44;
            case "evaluating-functions": return 45;
            case "substitution": return 46;
            case "algebraic-manipulation": return 47;
            case "number-line": return 48;
            case "exponents": return 49;
            case "comparing-fractions": return 50;
            case "scientific-notation": return 51;
            case "order-of-operations": return 52;
            case "reciprocal": return 53;
            case "finding-percents": return 54;
            case "subtracting-decimals": return 55;
            case "integers": return 56;
            case "n-number-sense-operations": return 57;
            case "probability": return 58;
            case "combinatorics": return 59;
            case "symbolization-articulation": return 60;
            case "mean": return 61;
            case "meaning-of-pi": return 62;
            case "interpreting-numberline": return 63;
            case "graph-shape": return 64;
            case "linear-area-volume-conversion": return 65;
            case "inequality-solving": return 66;
            case "fractions": return 67;
            case "percents": return 68;
            case "unit-conversion": return 69;
            case "equation-concept": return 70;
            case "rate": return 71;
            case "median": return 72;
            case "mode": return 73;
            case "statistics": return 74;
            case "circle-graph": return 75;
            case "congruence": return 76;
            case "least-common-multiple": return 77;
            case "fraction-decimals-percents": return 78;
            case "multiplying-positive-negative-numbers": return 79;
            case "inequalities": return 80;
            case "graph interpretation": return 81;
            case "algebra symbolization": return 82;
            case "surface-area-and-volume": return 83;
            case "simple-calculation": return 84;
            case "stem-and-leaf-plot": return 85;
            case "prime-number": return 86;
            case "rounding": return 87;
            case "circumference": return 88;
            case "reduce-fraction": return 89;
            case "application: finding percentage of a number": return 90;
            case "area-of-circle": return 91;
            case "m-measurement": return 92;
            case "rate-with-distance-and-time": return 93;
            case "area-concept": return 94;
            case "divisibility": return 95;
            case "properties-of-solids": return 96;
            case "adding-decimals": return 97;
            case "measurement": return 98;
            case "g-geometry": return 99;
            case "similar-triangles": return 100;

            // Fallback just in case the database has a typo
            default: return 21; // Maps to "noskill" (index 21) safely
        }
    }

    // (You can keep your getAllStudentsSummary() and addStudent() methods down here exactly as they were!)
    //    Method to get all students from sidebar:
    public List<StudentSummaryDto> getAllStudentsSummary(){
        List<Student> allStudents = studentRepository.findAll();
        List<StudentSummaryDto> summaries = new ArrayList<>();

        for(Student student:allStudents){
            String code = "s-" + (1000 + student.getId());
            List<StudentAttempt> history = studentAttemptRepository.findByStudent_IdOrderByActionNumAsc((long) student.getId());
            int score =0;
            if(!history.isEmpty()){
                long correct = history.stream().filter(a -> a.getCorrect() == 1).count();
                score = (int) Math.round(((double) correct / history.size()) * 100);
            }

            String name = student.getName() !=null ? student.getName() : "Student " + student.getId();
            summaries.add(new StudentSummaryDto(student.getId() , name , code , score));
        }
        return summaries;
    }

    public StudentSummaryDto addStudent(String name){
        Student newStudent = new Student();
        newStudent.setName(name);

        Student savedStudent = studentRepository.save(newStudent);
        String code = "S-" + (1000 + savedStudent.getId());

        return new StudentSummaryDto(savedStudent.getId() , savedStudent.getName() , code , 0);
    }


    // ==========================================
    // FEATURE: Edit Student History
    // ==========================================

    // 1. Fetch the raw history for the Edit Table
    public List<StudentAttemptDto> getStudentRawHistory(Long studentId) {
        List<StudentAttempt> history = studentAttemptRepository.findByStudent_IdOrderByActionNumAsc(studentId);

        // Convert Entities to DTOs
        return history.stream().map(attempt -> new StudentAttemptDto(
                attempt.getId(),
                attempt.getSkill(),
                attempt.getCorrect(),
                attempt.getActionNum(),
                attempt.getHintCount(),
                attempt.getTimeTaken()
        )).collect(Collectors.toList());
    }

    // 2. Update a specific attempt
    public StudentAttemptDto updateStudentAttempt(Long attemptId, StudentAttemptDto updatedData) {
        // Find the existing attempt in the database
        StudentAttempt existingAttempt = studentAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt with ID " + attemptId + " not found!"));

        // Update the fields (Teacher might change it from Wrong (0) to Right (1))
        existingAttempt.setCorrect(updatedData.getCorrect());
        existingAttempt.setSkill(updatedData.getSkill());
        existingAttempt.setHintCount(updatedData.getHintCount());
        existingAttempt.setTimeTaken(updatedData.getTimeTaken());
        // Note: We usually don't change the actionNum so it stays in chronological order

        // Save back to database
        StudentAttempt saved = studentAttemptRepository.save(existingAttempt);

        // Return the updated DTO to the frontend
        return new StudentAttemptDto(
                saved.getId(),
                saved.getSkill(),
                saved.getCorrect(),
                saved.getActionNum(),
                saved.getHintCount(),
                saved.getTimeTaken()
        );
    }
}