/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shitstorm.controllers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricCaseActivityInstance;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.processapp.utilities.beans.interfaces.IPrescriptiveServiceCaller;
import org.processapp.utilities.exceptions.TaskNotValidForInstanceException;
import org.shitstorm.constants.Pages;
import org.shitstorm.helper.CaseExecutionHelper;

@Named(value = "caseController")
@SessionScoped
public class CaseController implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private IPrescriptiveServiceCaller prescriptiveService;

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

    // Alle Variablen in er Prozessinstanz
    private Map<String, Object> variables;
    private Map<String, Object> taskFormVariables;

    public CaseController() {
        this.activeCaseExecutions = new ArrayList<>();
        this.enabledCaseExecutions = new ArrayList<>();
        this.availableCaseExecutions = new ArrayList<>();
        this.completedList = new ArrayList<>();
        this.taskFormVariables = new HashMap<>();
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
        this.taskFormVariables.clear();
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
//        String s = this.extractor.sayHello();
//        List<CaseExecution> extractEnabledCaseExecutions = this.extractor.extractEnabledCaseExecutions(caseInstanceId);
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
        this.variables = this.engine.getCaseService().getVariables(caseInstanceId);
    }

// select a task
    public String selectExecution(CaseExecution execution) {
        this.selectedExecution = execution;
        String currentCaseInstanceURL = Pages.getCaseInstanceURL(this.caseInstance.getId());
        if (this.selectedExecution != null) {
            String activityName = this.selectedExecution.getActivityName();
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

    public void startExecution(CaseExecution execution) {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            // Vor dem Starten der , da die präskriptive Lösung die Informationen der aktuellen 
            // Periode noch mitgeloefert bekommt und diese sonst nicht als Evidenz gesetzt werden können
            String resultMessage = this.prescriptiveService.registerDecision(execution.getCaseInstanceId(), execution.getActivityId());
            // Erst danach dei Case Execution starten und die 
            this.engine.getCaseService().manuallyStartCaseExecution(execution.getId());
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Successful", resultMessage));
            this.updateElementsStatus();
        } catch (TaskNotValidForInstanceException ex) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Registration failed:", ex.getMessage()));
        }
    }

    public String completeElement(CaseExecution execution) {
        boolean isCasePlanModel = CaseExecutionHelper.isCasePlanModel(execution);

        try {
            HashMap<String, Object> savedVariables = this.saveVariables();
            this.engine.getCaseService().completeCaseExecution(execution.getId(), savedVariables);
            this.updateElementsStatus();
            this.deselectExecution();
            if (isCasePlanModel) {
                return Pages.PAGE_CASE_INSTANCES;
            }
            return Pages.getCaseInstanceURL(this.caseInstance.getId());
        } catch (Exception ex) {
            // Code wenn nicht completable
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", ex.getMessage()));
            return Pages.getCaseInstanceURL(this.caseInstance.getId());
        }
    }

    // save taskform- and Application Variables
    private HashMap<String, Object> saveVariables() {
        HashMap<String, Object> influencedVariablesByTask = new HashMap<>();

        // save taskFormValues
        // with workarround to use boolean variables caseFormInput 2 boolean
        for (Map.Entry<String, Object> entry : this.taskFormVariables.entrySet()) {
            if (entry.getValue() != null && ("true".equals(entry.getValue()) || "false".equals(entry.getValue()))) {
                influencedVariablesByTask.put(entry.getKey(), Boolean.valueOf((String) entry.getValue()));
            } else {
                influencedVariablesByTask.put(entry.getKey(), entry.getValue());
            }
        }
        this.engine.getCaseService().setVariables(this.selectedExecution.getId(), influencedVariablesByTask);
        this.taskFormVariables.clear();
        return influencedVariablesByTask;
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

    public Map<String, Object> getTaskFormVariables() {
        return taskFormVariables;
    }

    public void setTaskFormVariables(Map<String, Object> taskFormVariables) {
        this.taskFormVariables = taskFormVariables;
    }

    public String goToRecommenderForm() {
        this.selectedExecution = null;
        this.currentCenterFormName = "Prescriptive Recommender";
        return Pages.PAGE_RECOMMENDER_FORM;
    }

}
