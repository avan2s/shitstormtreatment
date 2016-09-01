/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shitstorm.controllers;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.LegendPlacement;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.MenuModel;
import org.processapp.utilities.beans.interfaces.ICaseInstanceInformationLoader;
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
@SessionScoped
public class RecommendationResultController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private RecommenderController recommendationController;

//    private List<SimPeriod> simulationValues;
    private List<NextBestAction> nextBestActions;
    private BarChartModel barChartModel;
    private BarChartModel simpleModel;
    private NextActionRecommendation nextRecommendedAction;
    private SequenceRecommendation sequenceRecommendation;
    private MenuModel menuModel;

    public RecommendationResultController() {
        this.barChartModel = new BarChartModel();
//        this.simulationValues = new ArrayList<>();
        this.nextBestActions = new ArrayList<>();
        this.simpleModel = new BarChartModel();
    }

    public void buildSequenceRecommendationResult() {
//        this.simulationValues.clear();
        this.nextBestActions.clear();
        this.sequenceRecommendation = this.recommendationController.getSequenceRecommendation();
        List<NextBestAction> actions = sequenceRecommendation.getNextBestActions();
        for (NextBestAction action : actions) {
            this.nextBestActions.add(action);
//            this.simulationValues.add(action.getSimPeriod());
        }
        this.buildBarModel();
        this.buildStepModel();
    }

    public void buildActionRecommendationResult() {
//        this.simulationValues.clear();
        this.nextBestActions.clear();
        this.nextRecommendedAction = recommendationController.getActionRecommendation();
//        this.simulationValues.add(nextRecommendedAction.getNextRecommendedAction().getSimPeriod());
        this.nextBestActions.add(nextRecommendedAction.getNextRecommendedAction());
        this.buildBarModel();
        this.buildStepModel();
    }

    private MenuModel buildStepModel() {
        this.menuModel = new DefaultMenuModel();
        for (NextBestAction nextBestAction : this.nextBestActions) {
            BigDecimal benefit = new BigDecimal(nextBestAction.getBenefit());
            benefit = benefit.setScale(2, RoundingMode.HALF_UP);

            // Step-Aktivitätsnamen der Empfehlung aufbauen: <Name>(Benefit)
            StringBuilder sb = new StringBuilder(nextBestAction.getTaskNameForAction());
            sb.append(" (").append(benefit).append(")");
            String itemName = sb.toString();

            // Aktion als Step hinzufügen
            DefaultMenuItem item = new DefaultMenuItem(itemName);
            this.menuModel.addElement(item);
        }
        return this.menuModel;
    }

    private BarChartModel buildBarModel() {
        this.barChartModel = new BarChartModel();
        this.barChartModel.setLegendPosition("n");
        this.barChartModel.setLegendPlacement(LegendPlacement.OUTSIDEGRID);
//
//        this.simpleModel = new BarChartModel();
//        ChartSeries boys = new ChartSeries();
//        boys.setLabel("Boys");
//        boys.set("2004", 120);
//        boys.set("2004", 0);
//        boys.set("2005", 100);
//        boys.set("2007", 150);
//        boys.set("2008", 25);
//
//        ChartSeries girls = new ChartSeries();
//        girls.setLabel("Girls");
//        girls.set("2004", 52);
//        girls.set("2005", 60);
//        girls.set("2006", 110);
//        girls.set("2007", 135);
//        girls.set("2008", 120);
//        this.simpleModel.addSeries(boys);
//        this.simpleModel.addSeries(girls);
//        List<ChartSeries> simpleChartSeries = this.simpleModel.getSeries();
        LinkedHashMap<String, ChartSeries> act2ChartSeries = this.constructDefaultChartSeriesForBarChart();

        // Für jede vorgeschlagene Aktion...
        for (NextBestAction nextBestAction : this.nextBestActions) {
            // Simulationswerte der Periode heraussuchen
            SimPeriod simPeriod = nextBestAction.getSimPeriod();

            // Für Simulationswerte jeder Aktion...
            for (SimAct simAct : simPeriod.getSimActValues()) {
                // Bar-Chart-Serien der Aktion erhalten
                ChartSeries chartSeries = act2ChartSeries.get(simAct.getTaskNameForAction());
                // Wenn es noch keine Bar-Chart-Serien gibt, neu anlegen +
                // (wird wegen default-Konstrukt immer eine geben)
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

        this.barChartModel.setTitle("Action's Benefit");
        Axis xAxis = this.barChartModel.getAxis(AxisType.X);
        xAxis.setLabel("Decision Period");

        Axis yAxis = this.barChartModel.getAxis(AxisType.Y);
        yAxis.setLabel("Benefit");
        return this.barChartModel;
    }

    private LinkedHashMap<String, ChartSeries> constructDefaultChartSeriesForBarChart() {
        LinkedHashMap<String, ChartSeries> act2ChartSeries = new LinkedHashMap<>();
        if (nextBestActions.size() >= 0) {
            List<SimAct> allSimActs = this.nextBestActions.get(0).getSimPeriod().getSimActValues();

            for (SimAct simAct : allSimActs) {
                // Lege eine neue ChartSerie an
                ChartSeries chartSeries = new ChartSeries(simAct.getTaskNameForAction());
                // Für jede vorgeschlagene Aktion (d.h. jede Periode, die von Interesse ist)
                // ChartSerie befüllen
                for (NextBestAction nextBestAction : this.nextBestActions) {
                    // Für jede Periode zu jeder Aktion einen default-Wert festlegen
                    chartSeries.set(String.valueOf(nextBestAction.getPeriod()), 0);
                    act2ChartSeries.put(simAct.getTaskNameForAction(), chartSeries);
                }
            }
        }
        return act2ChartSeries;
    }

    // Simulationswerte jeder Aktion und jeder Periode
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

    public NextActionRecommendation getNextRecommendedAction() {
        return nextRecommendedAction;
    }

    public void setNextRecommendedAction(NextActionRecommendation nextRecommendedAction) {
        this.nextRecommendedAction = nextRecommendedAction;
    }

    public SequenceRecommendation getSequenceRecommendation() {
        return sequenceRecommendation;
    }

    public void setSequenceRecommendation(SequenceRecommendation sequenceRecommendation) {
        this.sequenceRecommendation = sequenceRecommendation;
    }

    public List<NextBestAction> getNextBestActions() {
        return nextBestActions;
    }

    public void setNextBestActions(List<NextBestAction> nextBestActions) {
        this.nextBestActions = nextBestActions;
    }

    public BarChartModel getSimpleModel() {
        return simpleModel;
    }

    public void setSimpleModel(BarChartModel simpleModel) {
        this.simpleModel = simpleModel;
    }

}
