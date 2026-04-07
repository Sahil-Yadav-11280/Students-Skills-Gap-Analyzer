package com.sahil.aianalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AiPredictionRequest {
    private List<Integer> skills;
    private List<Integer> corrects;
}