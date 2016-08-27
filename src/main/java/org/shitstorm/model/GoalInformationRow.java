/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shitstorm.model;

import java.io.Serializable;

/**
 *
 * @author Andy
 */
public class GoalInformationRow implements Serializable {

    private String goalFigure;
    private double priority;
    private double goalValue;
    private int goalStartPeriod;
    private int goalEndPeriod;
    private String goalUnit;

    public GoalInformationRow() {
    }

    public GoalInformationRow(String goalFigure, double priority, double goalValue, int goalStartPeriod, int goalEndPeriod, String unit) {
        this.goalFigure = goalFigure;
        this.priority = priority;
        this.goalValue = goalValue;
        this.goalStartPeriod = goalStartPeriod;
        this.goalEndPeriod = goalEndPeriod;
        this.goalUnit = unit;
    }

    public String getGoalFigure() {
        return goalFigure;
    }

    public void setGoalFigure(String goalFigure) {
        this.goalFigure = goalFigure;
    }

    public double getPriority() {
        return priority;
    }

    public void setPriority(double priority) {
        this.priority = priority;
    }

    public double getGoalValue() {
        return goalValue;
    }

    public void setGoalValue(double goalValue) {
        this.goalValue = goalValue;
    }

    public int getGoalStartPeriod() {
        return goalStartPeriod;
    }

    public void setGoalStartPeriod(int goalStartPeriod) {
        this.goalStartPeriod = goalStartPeriod;
    }

    public int getGoalEndPeriod() {
        return goalEndPeriod;
    }

    public void setGoalEndPeriod(int goalEndPeriod) {
        this.goalEndPeriod = goalEndPeriod;
    }

    public String getGoalUnit() {
        return goalUnit;
    }

    public void setGoalUnit(String goalUnit) {
        this.goalUnit = goalUnit;
    }

}
