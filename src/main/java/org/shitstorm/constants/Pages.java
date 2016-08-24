/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shitstorm.constants;

/**
 *
 * @author Andy
 */
public class Pages {

    public static final String INDEX = "index";
    public static final String CASE_INSTANCES = "case_instances";
    public static final String NEW_APPLICATIION = "new-application";

    public static String getCaseInstanceURL(String caseInstanceId) {
        return "case-form.jsf?caseInstanceId=" + caseInstanceId;
    }
}
