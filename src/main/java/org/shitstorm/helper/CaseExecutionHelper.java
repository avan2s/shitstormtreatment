/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shitstorm.helper;

import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.model.cmmn.impl.CmmnModelConstants;

/**
 *
 * @author Andy
 */
public class CaseExecutionHelper {

    public static boolean isProccessTask(CaseExecution execution) {
        return execution.getActivityType().equals(CmmnModelConstants.CMMN_ELEMENT_PROCESS_TASK);
    }

    public static boolean isTask(CaseExecution execution) {
        return isHumanTask(execution) || isProccessTask(execution);
    }
    
    public static boolean isCasePlanModel(CaseExecution execution) {
        return execution.getActivityType().contentEquals(CmmnModelConstants.CMMN_ELEMENT_CASE_PLAN_MODEL);
    }
    
    public static boolean isStage(CaseExecution execution) {
        return execution.getActivityType().contentEquals(CmmnModelConstants.CMMN_ELEMENT_STAGE);
    }

    public static boolean isHumanTask(CaseExecution execution) {
        if (execution == null) {
            return false;
        } else {
            return execution.getActivityType().contentEquals(CmmnModelConstants.CMMN_ELEMENT_HUMAN_TASK);
        }
    }
}
