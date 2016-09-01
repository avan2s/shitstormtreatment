/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shitstorm.controllers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.model.chart.PieChartModel;
import org.processapp.utilities.beans.interfaces.ICaseInstanceInformationLoader;
import org.processapp.utilities.beans.interfaces.IPrescriptiveServiceCaller;
import org.processapp.utilities.exceptions.CaseInstanceNotValidException;
import org.shitstorm.constants.Pages;
import wsclient.generated.prescriptiverecommender.GoalRequest;
import wsclient.generated.prescriptiverecommender.NextActionRecommendation;
import wsclient.generated.prescriptiverecommender.SequenceRecommendation;
import wsclient.generated.prescriptiverecommender.SequenceType;

/**
 *
 * @author Andy
 */
@Named(value = "recommenderController")
@SessionScoped
public class RecommenderController implements Serializable {

    @Inject
    private CaseController caseController;

    @EJB
    private IPrescriptiveServiceCaller service;

    @EJB
    ICaseInstanceInformationLoader informationLoader;

    private List<GoalRequest> goals;
    private final int minPeriod = 0;
    private final int maxPeriod = 4;
    private boolean nothingActionAllowed;
    private SequenceType sequenceType;
    private int numberOfDecisions;
    private boolean showActionForm;
    private SequenceRecommendation sequenceRecommendation;
    private NextActionRecommendation actionRecommendation;
    private PieChartModel pieChart;

    public RecommenderController() {
        this.nothingActionAllowed = true;
        this.showActionForm = true;
        this.goals = new ArrayList<>();
        this.numberOfDecisions = 1;

        GoalRequest goalK = new GoalRequest();
        goalK.setGoalFigure("Kosten");
        goalK.setGoalUnit("Euro");
        goalK.setGoalWeight(20);
        goalK.setGoalStartPeriod(this.minPeriod);
        goalK.setGoalEndPeriod(this.maxPeriod);

        GoalRequest goalKZ = new GoalRequest();
        goalKZ.setGoalFigure("Kundenzufriedenheit");
        goalKZ.setGoalUnit("KZE");
        goalKZ.setGoalWeight(20);
        goalKZ.setGoalStartPeriod(this.minPeriod);
        goalKZ.setGoalEndPeriod(this.maxPeriod);

        GoalRequest goalSP = new GoalRequest();
        goalSP.setGoalFigure("Stakeholder-Power");
        goalSP.setGoalUnit("SPE");
        goalSP.setGoalWeight(20);
        goalSP.setGoalStartPeriod(this.minPeriod);
        goalSP.setGoalEndPeriod(this.maxPeriod);

        GoalRequest goalIG = new GoalRequest();
        goalIG.setGoalFigure("Informationsgewinn");
        goalIG.setGoalUnit("IGE");
        goalIG.setGoalWeight(20);
        goalIG.setGoalStartPeriod(this.minPeriod);
        goalIG.setGoalEndPeriod(this.maxPeriod);

        GoalRequest goalZA = new GoalRequest();
        goalZA.setGoalFigure("Zeitaufwand");
        goalZA.setGoalUnit("min");
        goalZA.setGoalWeight(20);
        goalZA.setGoalStartPeriod(this.minPeriod);
        goalZA.setGoalEndPeriod(this.maxPeriod);
        this.goals.add(goalK);
        this.goals.add(goalKZ);
        this.goals.add(goalSP);
        this.goals.add(goalIG);
        this.goals.add(goalZA);
        this.pieChart = new PieChartModel();
        this.pieChart.setTitle("Relative Goal Weights");
        this.pieChart.setLegendPosition("w");
        this.pieChart.setShowDataLabels(true);
        this.updateGoalPie();
    }

    public void updateGoalPie() {
        for (GoalRequest goal : this.goals) {
            this.pieChart.set(goal.getGoalFigure(), goal.getGoalWeight());
        }
    }

    public String recommendAction() {
        String caseInstanceId = this.caseController.getCaseInstance().getId();
        this.sequenceRecommendation = null;
        this.actionRecommendation = this.service.recommendNextAction(caseInstanceId, this.goals, this.nothingActionAllowed);
        if (this.actionRecommendation != null) {
            this.caseController.setCurrentCenterFormName("Recommendation Result:");
            return Pages.PAGE_RECOMMENDATION_RESULT;
        }
        return "";
    }

    public String recommendSequence() {
        try {
            String caseInstanceId = this.caseController.getCaseInstance().getId();
            this.sequenceRecommendation = this.service.recommendSequence(caseInstanceId, sequenceType, this.numberOfDecisions, goals, nothingActionAllowed);
            this.actionRecommendation = null;
            return Pages.PAGE_RECOMMENDATION_RESULT;
        } catch (CaseInstanceNotValidException ex) {
            Logger.getLogger(RecommenderController.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
    }

    public void onChange(String value) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "You have selected: " + value, null));
    }

    public List<GoalRequest> getGoals() {
        return goals;
    }

    public void setGoals(List<GoalRequest> goals) {
        this.goals = goals;
    }

    public int getMinPeriod() {
        return minPeriod;
    }

    public int getMaxPeriod() {
        return maxPeriod;
    }

    public SequenceType getSequenceType() {
        return sequenceType;
    }

    public void setSequenceType(SequenceType sequenceType) {
        this.sequenceType = sequenceType;
    }

    public SequenceType[] getSequenceTypes() {
        return SequenceType.values();
    }

    public PieChartModel getPieChart() {
        return pieChart;
    }

    public boolean isNothingActionAllowed() {
        return nothingActionAllowed;
    }

    public void setNothingActionAllowed(boolean nothingActionAllowed) {
        this.nothingActionAllowed = nothingActionAllowed;
    }

    public int getNumberOfDecisions() {
        return numberOfDecisions;
    }

    public void setNumberOfDecisions(int numberOfDecisions) {
        this.numberOfDecisions = numberOfDecisions;
    }

    public boolean isShowActionForm() {
        return showActionForm;
    }

    public void setShowActionForm(boolean showActionForm) {
        this.showActionForm = showActionForm;
    }

    public SequenceRecommendation getSequenceRecommendation() {
        return sequenceRecommendation;
    }

    public void setSequenceRecommendation(SequenceRecommendation sequenceRecommendation) {
        this.sequenceRecommendation = sequenceRecommendation;
    }

    public NextActionRecommendation getActionRecommendation() {
        return actionRecommendation;
    }

    public void setActionRecommendation(NextActionRecommendation actionRecommendation) {
        this.actionRecommendation = actionRecommendation;
    }

}
