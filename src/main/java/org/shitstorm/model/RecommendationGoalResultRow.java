/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shitstorm.model;

import wsclient.generated.prescriptiverecommender.ExpectedValue;
import wsclient.generated.prescriptiverecommender.GoalRequest;

/**
 *
 * @author Andy
 */
public final class RecommendationGoalResultRow {

    private GoalRequest goalRequest;
    private ExpectedValue expectedValue;
    private double percentageWeight;
    private double weightedUniformValue;

    public RecommendationGoalResultRow() {
        this.expectedValue = new ExpectedValue();
    }

    public RecommendationGoalResultRow(GoalRequest goalRequest, ExpectedValue expectedValue, double percentageWeight) {
        this.goalRequest = goalRequest;
        this.percentageWeight = percentageWeight;
        this.setExpectedValue(expectedValue);
    }

    public GoalRequest getGoalRequest() {
        return goalRequest;
    }

    public void setGoalRequest(GoalRequest goalRequest) {
        this.goalRequest = goalRequest;
    }

    public ExpectedValue getExpectedValue() {
        return expectedValue;
    }

    public void setExpectedValue(ExpectedValue expectedValue) {
        this.expectedValue = expectedValue;
        this.weightedUniformValue = this.expectedValue.getUniformUtility() * this.percentageWeight;
    }

    public double getWeightedUniformValue() {
        return this.weightedUniformValue;
    }
    
    public double getPercentageWeight() {
        return percentageWeight;
    }

    public void setPercentageWeight(double percentageWeight) {
        this.percentageWeight = percentageWeight;
    }
    
}
