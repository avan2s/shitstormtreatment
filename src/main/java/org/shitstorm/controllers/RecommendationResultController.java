/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shitstorm.controllers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.LegendPlacement;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.MenuElement;
import org.primefaces.model.menu.MenuModel;
import org.shitstorm.model.RecommendationGoalResultRow;
import wsclient.generated.prescriptiverecommender.ExpectedValue;
import wsclient.generated.prescriptiverecommender.GoalRequest;
import wsclient.generated.prescriptiverecommender.KipGoal;
import wsclient.generated.prescriptiverecommender.NextActionRecommendation;
import wsclient.generated.prescriptiverecommender.NextBestAction;
import wsclient.generated.prescriptiverecommender.SequenceRecommendation;
import wsclient.generated.prescriptiverecommender.SimAct;
import wsclient.generated.prescriptiverecommender.SimGoal;
import wsclient.generated.prescriptiverecommender.SimPeriod;

/**
 *
 * @author Andy
 */
@Named(value = "recommendationResultController")
@RequestScoped
public class RecommendationResultController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private RecommenderController recommendationController;

    private List<SimPeriod> simulationValues;
    private BarChartModel barChartModel;
    private NextActionRecommendation nextRecommendedAction;
    private SequenceRecommendation sequenceRecommendation;
    private MenuModel menuModel;

    public RecommendationResultController() {
        this.barChartModel = new BarChartModel();
        this.simulationValues = new ArrayList<>();
        //this.simulationValues.get(0).getSimActValues().get(0).getSimGoalValues().get(0).getExpectedValue().
    }

    @PostConstruct
    private void initialize() {
        this.nextRecommendedAction = recommendationController.getActionRecommendation();
        this.sequenceRecommendation = recommendationController.getSequenceRecommendation();
        if (nextRecommendedAction != null) {
            this.simulationValues.add(nextRecommendedAction.getNextRecommendedAction().getSimPeriod());
        } else if (sequenceRecommendation != null) {
            List<NextBestAction> actions = sequenceRecommendation.getNextBestActions();
            for (NextBestAction action : actions) {
                this.simulationValues.add(action.getSimPeriod());
            }
            this.initializeMenuModel();
        }
        this.initializeBarModel();
        
        
    }
    
    private MenuModel initializeMenuModel(){
        this.menuModel = new DefaultMenuModel();
        if(this.sequenceRecommendation!=null){
            List<NextBestAction> nextBestActions = this.sequenceRecommendation.getNextBestActions();
            for (NextBestAction nextBestAction : nextBestActions) {
                String itemName = nextBestAction.getTaskNameForAction();
                DefaultMenuItem item = new DefaultMenuItem(itemName);
                this.menuModel.addElement(item);
            }
        }
        return this.menuModel;
    }

    private BarChartModel initializeBarModel() {
        this.barChartModel = new BarChartModel();
        this.barChartModel.setTitle("Bar Chart");
        this.barChartModel.setLegendPosition("n");
        this.barChartModel.setLegendPlacement(LegendPlacement.OUTSIDEGRID);

        Axis xAxis = this.barChartModel.getAxis(AxisType.X);
        xAxis.setLabel("Decision Period");

        Axis yAxis = this.barChartModel.getAxis(AxisType.Y);
        yAxis.setLabel("Benefit");
        TreeMap<String, ChartSeries> act2ChartSeries = new TreeMap<>();
        for (SimPeriod simPeriod : this.simulationValues) {
            for (SimAct simAct : simPeriod.getSimActValues()) {
                ChartSeries chartSeries = act2ChartSeries.get(simAct.getTaskNameForAction());
                if (chartSeries == null) {
                    chartSeries = new ChartSeries();
                    chartSeries.setLabel(simAct.getTaskNameForAction());
                    act2ChartSeries.put(simAct.getTaskNameForAction(), chartSeries);
                }
                chartSeries.set(String.valueOf(simPeriod.getPeriod()), simAct.getBenefit());
            }
        }
        Collection<ChartSeries> values = act2ChartSeries.values();
        for (ChartSeries chartSerie : values) {
            this.barChartModel.addSeries(chartSerie);
        }
        return this.barChartModel;
    }

    public List<RecommendationGoalResultRow> getGoalResultRows(SimAct simAct) {
        List<GoalRequest> goalRequests = this.recommendationController.getGoals();
        List<SimGoal> simGoalValues = simAct.getSimGoalValues();
        List<RecommendationGoalResultRow> rows = new ArrayList<>();
        for (GoalRequest goalRequest : goalRequests) {
            String goalFigure = goalRequest.getGoalFigure();
            ExpectedValue expectedValue = null;
            double percentageWeight = 0;
            for (SimGoal simGoal : simGoalValues) {
                KipGoal kipGoal = simGoal.getKipGoal();
                if (kipGoal.getGoalTarget().equals(goalFigure)) {
                    expectedValue = simGoal.getExpectedValue();
                    percentageWeight = simGoal.getKipGoal().getGoalWeight();
                    break;
                }
            }
            RecommendationGoalResultRow row = new RecommendationGoalResultRow(goalRequest, expectedValue, percentageWeight);
            rows.add(row);
        }
        return rows;
    }

    @PreDestroy
    private void destroy() {
       int x = 0;
    }

    public List<SimPeriod> getSimulationValues() {
        return simulationValues;
    }

    public void setSimulationValues(List<SimPeriod> simulationValues) {
        this.simulationValues = simulationValues;
    }

    public BarChartModel getBarChartModel() {
        return barChartModel;
    }

    public void setBarChartModel(BarChartModel barChartModel) {
        this.barChartModel = barChartModel;
    }

    public MenuModel getMenuModel() {
        return menuModel;
    }

    public void setMenuModel(MenuModel menuModel) {
        this.menuModel = menuModel;
    }

}
