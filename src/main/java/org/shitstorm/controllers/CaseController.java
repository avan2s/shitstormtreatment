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
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.bpmn.parser.ActivityTypes;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.model.cmmn.impl.CmmnModelConstants;

@Named(value = "caseController")
@SessionScoped
public class CaseController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private ProcessEngine engine;
    private CaseInstance caseInstance;
    private Task selectedTask;
    // List of active caseExecutions
    private List<CaseExecution> activeCaseExecutions;
    // List of enabled caseExecutions
    private List<CaseExecution> enabledCaseExecutions;
    // List of enabled caseExecutions
    private List<CaseExecution> availableCaseExecutions;

    public CaseController() {
        this.activeCaseExecutions = new ArrayList<>();
        this.enabledCaseExecutions = new ArrayList<>();
        this.availableCaseExecutions = new ArrayList<>();
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
    private void reset() {
        this.selectedTask = null;
        this.caseInstance = null;
        this.activeCaseExecutions.clear();
        this.enabledCaseExecutions.clear();
        this.availableCaseExecutions.clear();
    }

    public void initByCaseInstanceId(final String caseInstanceId) {
        this.reset();
        if (engine.getCaseService()
                .createCaseExecutionQuery()
                .caseInstanceId(caseInstanceId)
                .count() == 0) {
            return;
        }
        this.engine.getCaseService().getVariables(caseInstanceId);

        // get Case Instance with CaseInstanceQuery
        this.caseInstance = engine.getCaseService()
                .createCaseInstanceQuery()
                .caseInstanceId(caseInstanceId)
                .singleResult();

        this.loadCaseExcecutions();
    }

    private void loadCaseExcecutions() {
        String caseInstanceId = this.caseInstance.getId();
        TaskService taskService = this.engine.getTaskService();
        
// get all case excecutions
        List<CaseExecution> caseExceutions = engine.getCaseService()
                .createCaseExecutionQuery()
                .caseInstanceId(caseInstanceId)
                .list();

        // arrange order for exceutions
        for (CaseExecution caseExcecution : caseExceutions) {
            String executionId = caseExcecution.getId();
            String activityId = ((CaseExecutionEntity) caseExcecution).getActivityId();
            String activityName = caseExcecution.getActivityName();
            String activityType = caseExcecution.getActivityType();
            if (caseExcecution.isActive()) {
                activeCaseExecutions.add(caseExcecution);
                List<Task> list = taskService.createTaskQuery().caseExecutionId(executionId).list();
                int x = 0;
            } else if (caseExcecution.isEnabled()) {
                this.enabledCaseExecutions.add(caseExcecution);
                List<Task> list = taskService.createTaskQuery().caseExecutionId(executionId).list();
                int x = 0;
            } else if(caseExcecution.isAvailable()){
                this.availableCaseExecutions.add(caseExcecution);
            }
            else if (((CaseExecutionEntity) caseExcecution).getCurrentState().toString().equals("completed")) {
                this.enabledCaseExecutions.add(caseExcecution);
            }
        }

//        // get all historic caseActivityInstances by caseInstanceId
//        this.historicCaseActivityInstances = engine.getHistoryService()
//                .createHistoricCaseActivityInstanceQuery()
//                .caseInstanceId(this.caseInstance.getId())
//                .completed()
//                .list();
//        for (HistoricCaseActivityInstance i : this.historicCaseActivityInstances) {
//            System.out.println(i.getCaseActivityName() + " Type:" + i.getCaseActivityType() + "Status:");
//        }
//        int x = 0;
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
    
    
    public boolean isProccessTask(CaseExecution execution) {
        return execution.getActivityType().equals(CmmnModelConstants.CMMN_ELEMENT_PROCESS_TASK);
    }
    
    public boolean isTask(CaseExecution execution){
        return isHumanTask(execution)||isProccessTask(execution);
    }

    public boolean isHumanTask(CaseExecution execution) {
        if (execution == null) {
            return false;
        } else {
            // because complete case is also a HumanTask in Camunda
            boolean isHumanTask = execution.getActivityType().contentEquals(CmmnModelConstants.CMMN_ELEMENT_HUMAN_TASK);
            return isHumanTask && !isCasePlanModel(execution);
        }
    }
    
    public boolean isCasePlanModel(CaseExecution execution) {
        return execution.getActivityType().contentEquals(CmmnModelConstants.CMMN_ELEMENT_CASE_PLAN_MODEL);
    }
    
    

    public CaseInstance getCaseInstance() {
        return caseInstance;
    }

    public void setCaseInstance(CaseInstance caseInstance) {
        this.caseInstance = caseInstance;
    }

    public Task getSelectedTask() {
        return selectedTask;
    }

    public void setSelectedTask(Task selectedTask) {
        this.selectedTask = selectedTask;
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

}
