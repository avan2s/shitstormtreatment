/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shitstorm.controllers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import org.shitstorm.model.GoalInformationRow;

/**
 *
 * @author Andy
 */
@Named(value = "recommenderController")
@SessionScoped
public class RecommenderController implements Serializable {

    private List<GoalInformationRow> goals;
    private final int minPeriod = 0;
    private final int maxPeriod = 4;

    public RecommenderController() {
        this.goals = new ArrayList<>();
        GoalInformationRow goalK = new GoalInformationRow("Kosten", 25, 0, this.minPeriod, this.maxPeriod,"Euro");
        GoalInformationRow goalKZ = new GoalInformationRow("Kundenzufriedenheit", 25,0, this.minPeriod, this.maxPeriod, "KZE");
        GoalInformationRow goalSP = new GoalInformationRow("Stakeholder-Power", 25,0, this.minPeriod, this.maxPeriod,"SP");
        GoalInformationRow goalIG = new GoalInformationRow("Informationsgewinn", 25,0, this.minPeriod, this.maxPeriod, "IGE");
        GoalInformationRow goalZA = new GoalInformationRow("Zeitaufwand", 25, 0, this.minPeriod, this.maxPeriod, "min");
        this.goals.add(goalK);
        this.goals.add(goalKZ);
        this.goals.add(goalSP);
        this.goals.add(goalIG);
        this.goals.add(goalZA);
    }
    
    public void onChange(String value){
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "You have selected: " + value, null)); 
    }

    public List<GoalInformationRow> getGoals() {
        return goals;
    }

    public void setGoals(List<GoalInformationRow> goals) {
        this.goals = goals;
    }

    public int getMinPeriod() {
        return minPeriod;
    }

    public int getMaxPeriod() {
        return maxPeriod;
    }
}
