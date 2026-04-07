package com.sahil.skillsgapanalyzer.dto;


import lombok.AllArgsConstructor;

import java.util.List;

//What springboot sends to Flask
@AllArgsConstructor
public class MlPredictionRequest {
    public List<Integer> skills;
    public List<Integer> corrects;
}
