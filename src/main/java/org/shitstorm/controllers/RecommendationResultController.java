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
import wsclient.generated.prescriptiverecommender.NextBestAction;
import wsclient.generated.prescriptiverecommender.SequenceRecommendation;
import wsclient.generated.prescriptiverecommender.SimAct;
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

    public RecommendationResultController() {
        this.barChartModel = new BarChartModel();
        this.simulationValues = new ArrayList<>();
    }

    @PostConstruct
    private void initialize() {
        NextBestAction nextRecommendedAction = recommendationController.getActionRecommendation().getNextRecommendedAction();
        SequenceRecommendation sequenceRecommendation = recommendationController.getSequenceRecommendation();

        if (nextRecommendedAction != null) {
            this.simulationValues.add(nextRecommendedAction.getSimPeriod());
        } else if (sequenceRecommendation != null) {
            List<NextBestAction> actions = sequenceRecommendation.getNextBestActions();
            for (NextBestAction action : actions) {
                this.simulationValues.add(action.getSimPeriod());
            }
        }
        this.initializeBarModel();
    }

    @PreDestroy
    private void destroy() {
        this.barChartModel = null;
        this.recommendationController.setActionRecommendation(null);
        this.recommendationController.setSequenceRecommendation(null);
        this.simulationValues.clear();
    }

    private BarChartModel initializeBarModel() {
        this.barChartModel = new BarChartModel();
        this.barChartModel.setTitle("Bar Chart");
        this.barChartModel.setLegendPosition("s");
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
        for(ChartSeries chartSerie : values){
            this.barChartModel.addSeries(chartSerie);
        }
        return this.barChartModel;
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

}
