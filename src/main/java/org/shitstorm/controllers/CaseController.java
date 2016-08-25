/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shitstorm.controllers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricCaseActivityInstance;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.model.cmmn.impl.CmmnModelConstants;
import org.shitstorm.constants.Pages;
import org.shitstorm.helper.CaseExecutionHelper;

@Named(value = "caseController")
@SessionScoped
public class CaseController implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private static final String NO_FORM_MESSAGE = "No Form selected!";

    @Inject
    private ProcessEngine engine;
    private CaseInstance caseInstance;
    private CaseDefinition caseDefinition;
    private CaseExecution selectedExecution;
    private String currentCenterFormName;

    // List of active caseExecutions
    private List<CaseExecution> activeCaseExecutions;
    // List of enabled caseExecutions
    private List<CaseExecution> enabledCaseExecutions;
    // List of enabled caseExecutions
    private List<CaseExecution> availableCaseExecutions;
    private List<HistoricCaseActivityInstance> completedList;

    private Map<String, Object> variables;

    public CaseController() {
        this.activeCaseExecutions = new ArrayList<>();
        this.enabledCaseExecutions = new ArrayList<>();
        this.availableCaseExecutions = new ArrayList<>();
        this.completedList = new ArrayList<>();
    }

    // Wenn case_instance.xhtml geladen wird (siehe f:metadata
    // Case-Instanz aus dem URL-Parameter laden
    public void initCaseByParameters() {
        Map<String, String> requestParameterMap = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getRequestParameterMap();
        String caseInstanceId = requestParameterMap.get("caseInstanceId");

        if (caseInstanceId != null) {
            this.initByCaseInstanceId(caseInstanceId);
        }
    }

    // reset Objects
    private void clear() {
        this.selectedExecution = null;
        this.caseInstance = null;
        this.activeCaseExecutions.clear();
        this.enabledCaseExecutions.clear();
        this.availableCaseExecutions.clear();
        this.completedList.clear();
        this.currentCenterFormName = NO_FORM_MESSAGE;
    }

    public void initByCaseInstanceId(final String caseInstanceId) {
        this.clear();
        variables = this.engine.getCaseService().getVariables(caseInstanceId);
        this.caseInstance = engine.getCaseService().createCaseInstanceQuery()
                .caseInstanceId(caseInstanceId).singleResult();
        // get Case Definition from caseInstance by using the repository Service
        this.caseDefinition = engine.getRepositoryService()
                .getCaseDefinition(this.caseInstance.getCaseDefinitionId());
        this.updateElementsStatus();
    }

    private void updateElementsStatus() {
        String caseInstanceId = this.caseInstance.getId();
        //List<Task> list = this.engine.getTaskService().createTaskQuery().caseInstanceId(caseInstanceId).list();
        this.availableCaseExecutions = this.engine.getCaseService().createCaseExecutionQuery()
                .caseInstanceId(caseInstanceId).available().list();
        this.enabledCaseExecutions = this.engine.getCaseService().createCaseExecutionQuery()
                .caseInstanceId(caseInstanceId).enabled().list();
        this.activeCaseExecutions = this.engine.getCaseService().createCaseExecutionQuery()
                .caseInstanceId(caseInstanceId).active().list();
        // get all historic caseActivityInstances by caseInstanceId
        this.completedList = engine.getHistoryService().createHistoricCaseActivityInstanceQuery()
                .caseInstanceId(this.caseInstance.getId()).completed().list();

        // TaskQuery caseInstanceId1 = taskService.createTaskQuery().caseInstanceId(caseInstanceId);
        //int x = 0;
        // arrange order for exceutions
//        for (CaseExecution caseExcecution : caseExceutions) {
//            String executionId = caseExcecution.getId();
//            String activityId = ((CaseExecutionEntity) caseExcecution).getActivityId();
//            String activityName = caseExcecution.getActivityName();
//            String activityType = caseExcecution.getActivityType();
//            
    }

//    public List<CaseExecution> getCompletedMilestones() {
//        List<CaseExecution> executions = new ArrayList<>();
//        for (CaseExecution ex : this.completedCaseExecutions) {
//            if (ex.getActivityType().equals(CmmnModelConstants.CMMN_ELEMENT_MILESTONE)) {
//                executions.add(ex);
//            }
//        }
//        return executions;
//    }
// select a task
    public String selectExecution(CaseExecution execution) {
        this.selectedExecution = execution;
        String currentCaseInstanceURL = Pages.getCaseInstanceURL(this.caseInstance.getId());
        if (this.selectedExecution != null) {
            String activityName = this.selectedExecution.getActivityName();
            String activityType = execution.getActivityType();
            this.currentCenterFormName = activityName;
            activityName = activityName.trim().toLowerCase().replaceAll(" ", "_");
            
            if (CaseExecutionHelper.isCasePlanModel(execution)) {
                return "caseplanmodel_" + activityName;
            } else if (CaseExecutionHelper.isStage(execution)) {
                return ("stage_" + activityName);
            } else if (CaseExecutionHelper.isTask(execution)) {
                return ("task_" + activityName);
            } else {
                return ("other_" + activityName);
            }
        }
        return currentCaseInstanceURL;
    }
    
    private void deselectExecution() {
        this.selectedExecution = null;
        this.currentCenterFormName = NO_FORM_MESSAGE;
    }

    public void enableElement(CaseExecution execution) {
        this.engine.getCaseService().manuallyStartCaseExecution(execution.getId());
        this.updateElementsStatus();
    }

    public void completeElement(CaseExecution execution) {
        this.engine.getCaseService().completeCaseExecution(execution.getId());
        this.updateElementsStatus();
        this.deselectExecution();
    }

    public String getSelectedExecutionName() {
        if (this.selectedExecution == null) {
            return NO_FORM_MESSAGE;
        }
        return this.selectedExecution.getActivityName();
    }

    public CaseInstance getCaseInstance() {
        return caseInstance;
    }

    public void setCaseInstance(CaseInstance caseInstance) {
        this.caseInstance = caseInstance;
    }

    public CaseExecution getSelectedExecution() {
        return selectedExecution;
    }

    public void setSelectedExecution(CaseExecution selectedExecution) {
        this.selectedExecution = selectedExecution;
        this.currentCenterFormName = selectedExecution.getActivityName();
    }

    public List<CaseExecution> getActiveCaseExecutions() {
        return activeCaseExecutions;
    }

    public void setActiveCaseExecutions(List<CaseExecution> activeCaseExecutions) {
        this.activeCaseExecutions = activeCaseExecutions;
    }

    public List<CaseExecution> getEnabledCaseExecutions() {
        return enabledCaseExecutions;
    }

    public void setEnabledCaseExecutions(List<CaseExecution> enabledCaseExecutions) {
        this.enabledCaseExecutions = enabledCaseExecutions;
    }

    public List<CaseExecution> getAvailableCaseExecutions() {
        return availableCaseExecutions;
    }

    public void setAvailableCaseExecutions(List<CaseExecution> availableCaseExecutions) {
        this.availableCaseExecutions = availableCaseExecutions;
    }

    public List<HistoricCaseActivityInstance> getCompletedList() {
        return completedList;
    }

    public void setCompletedList(List<HistoricCaseActivityInstance> completedList) {
        this.completedList = completedList;
    }

    public CaseDefinition getCaseDefinition() {
        return caseDefinition;
    }

    public void setCaseDefinition(CaseDefinition caseDefinition) {
        this.caseDefinition = caseDefinition;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public String getCurrentCenterFormName() {
        return currentCenterFormName;
    }

    public void setCurrentCenterFormName(String currentCenterFormName) {
        this.currentCenterFormName = currentCenterFormName;
    }

    

}
