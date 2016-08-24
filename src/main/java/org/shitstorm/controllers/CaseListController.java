/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shitstorm.controllers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.primefaces.util.Constants;
import org.shitstorm.constants.ApplicationConstant;
import org.shitstorm.model.CaseInformationRow;

/**
 *
 * @author Andy
 */
@Named(value = "caseListController")
@RequestScoped
public class CaseListController implements Serializable {

    @Inject
    private ProcessEngine processEngine;

    private ArrayList<CaseInformationRow> rows;

    public CaseListController() {
    }

    public List<CaseInformationRow> getRunningInstances() {
        if (this.rows == null) {
            this.rows = new ArrayList<>();
            List<CaseInstance> caseInstances = processEngine.getCaseService()
                    .createCaseInstanceQuery()
                    .active()
                    .list();
            for (CaseInstance ci : caseInstances) {
                // get case definition 
                CaseDefinition caseDefinition = processEngine.getRepositoryService()
                        .getCaseDefinition(ci.getCaseDefinitionId());

                // create caseRow with all inforation about instance, definition, variables
                String caseInstanceId = ci.getId();
                String customerSatisfaction = (String) this.processEngine.getCaseService()
                        .getVariable(caseInstanceId, ApplicationConstant.VAR_KUNDENZUFRIEDENHEIT);
                String stakeholderPower = (String) this.processEngine.getCaseService()
                        .getVariable(caseInstanceId, ApplicationConstant.VAR_STAKEHOLDER_POWER);
                String reason = (String) this.processEngine.getCaseService()
                        .getVariable(caseInstanceId, ApplicationConstant.VAR_URSACHE);
                CaseInformationRow row = new CaseInformationRow(ci, caseDefinition);
                row.setCustomerSatisfaction(customerSatisfaction);
                row.setStakeholderPower(stakeholderPower);
                row.setReason(reason);
                rows.add(row);
            }
        }
        return this.rows;
    }
}
