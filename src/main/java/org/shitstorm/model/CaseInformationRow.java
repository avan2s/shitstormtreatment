/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shitstorm.model;

import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.runtime.CaseInstance;

/**
 *
 * @author Andy
 */
public class CaseInformationRow {

    private CaseInstance caseInstance;
    private CaseDefinition caseDefinition;
    private String customerSatisfaction;
    private String reason;
    private String stakeholderPower;

    public CaseInformationRow(CaseInstance caseInstance, CaseDefinition caseDefinition) {
        this.caseInstance = caseInstance;
        this.caseDefinition = caseDefinition;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStakeholderPower() {
        return stakeholderPower;
    }

    public void setStakeholderPower(String stakeholderPower) {
        this.stakeholderPower = stakeholderPower;
    }

    public CaseInstance getCaseInstance() {
        return caseInstance;
    }

    public void setCaseInstance(CaseInstance caseInstance) {
        this.caseInstance = caseInstance;
    }

    public CaseDefinition getCaseDefinition() {
        return caseDefinition;
    }

    public void setCaseDefinition(CaseDefinition caseDefinition) {
        this.caseDefinition = caseDefinition;
    }

    public String getCustomerSatisfaction() {
        return customerSatisfaction;
    }

    public void setCustomerSatisfaction(String customerSatisfaction) {
        this.customerSatisfaction = customerSatisfaction;
    }
}
