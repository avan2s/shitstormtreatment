/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shitstorm.controllers;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import javax.inject.Inject;
import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.shitstorm.constants.ApplicationConstants;
import org.shitstorm.constants.Pages;

@Named(value = "newApplicationController")
@SessionScoped
public class NewApplicationController implements Serializable {

    private static final long serialVersionUID = 1L;

    private String customerSatisfaction;
    private String stakeholderPower;

    @Inject
    private ProcessEngine processEngine;

    @Inject
    private CaseController caseController;

    @Inject
    private CaseListController caseListController;

    public NewApplicationController() {
        this.stakeholderPower = "hoch";
        this.customerSatisfaction = "niedrig";
    }

    // create caseInstance
    public String saveAction() {
        CaseService caseService = processEngine.getCaseService();
        VariableMap variables = Variables.createVariables();

        // set Variables
        variables.putValue(ApplicationConstants.VAR_KUNDENZUFRIEDENHEIT, Variables.stringValue(this.customerSatisfaction));
        variables.putValue(ApplicationConstants.VAR_STAKEHOLDER_POWER, Variables.stringValue(this.stakeholderPower));
        variables.putValue(ApplicationConstants.VAR_URSACHE, Variables.stringValue("unbekannt"));

        CaseInstance caseInstance = caseService.createCaseInstanceByKey(ApplicationConstants.APPLICATION_ID, variables);
        this.caseController.initByCaseInstanceId(caseInstance.getId());
        this.resetApplication();

        return Pages.PAGE_CASE_INSTANCES;
    }

    private void resetApplication() {
        this.stakeholderPower = null;
        this.customerSatisfaction = null;
    }

    public String getCustomerSatisfaction() {
        return customerSatisfaction;
    }

    public void setCustomerSatisfaction(String customerSatisfaction) {
        this.customerSatisfaction = customerSatisfaction;
    }

    public String getStakeholderPower() {
        return stakeholderPower;
    }

    public void setStakeholderPower(String stakeholderPower) {
        this.stakeholderPower = stakeholderPower;
    }

}
