/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shitstorm.shitstormapp;

import java.util.List;
import org.camunda.bpm.application.PostDeploy;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.impl.ServletProcessApplication;
import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.variable.Variables;
import org.shitstorm.constants.ApplicationConstant;

/**
 *
 * @author Andy
 */
@ProcessApplication("Shitstorm Treatment Application")
public class ShitstormTreatmentApplication extends ServletProcessApplication {

    @PostDeploy
    public void startCaseInstance(ProcessEngine processEngine) {
        CaseService caseService = processEngine.getCaseService();
        CaseInstance ci = caseService.createCaseInstanceByKey(ApplicationConstant.APPLICATION_ID,
                Variables.createVariables()
                .putValue(ApplicationConstant.VAR_KUNDENZUFRIEDENHEIT, Variables.stringValue("niedrig"))
                .putValue(ApplicationConstant.VAR_STAKEHOLDER_POWER, Variables.stringValue("hoch"))
                .putValue(ApplicationConstant.VAR_URSACHE, Variables.stringValue("unbekannt")));
        
        // Gebe alle Case Excecutions
        List<CaseExecution> caseExceutions = processEngine.getCaseService()
                .createCaseExecutionQuery()
                .caseInstanceId(ci.getId())
                .list();
    }
}
