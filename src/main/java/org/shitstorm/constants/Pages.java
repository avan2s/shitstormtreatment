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

    public static final String PAGE_INDEX = "index.xhtml";
    public static final String PAGE_CASE_INSTANCES = "case_instances.xhtml";
    public static final String PAGE_RECOMMENDER_FORM = "recommender_form.xhtml";
    public static final String PAGE_NEW_APPLICATIION = "new-application.xhtml";
    public static final String PAGE_CASE_INSTANCE = "case_instance.xhtml";

    public static String getCaseInstanceURL(String caseInstanceId) {
        return PAGE_CASE_INSTANCE + "?caseInstanceId=" + caseInstanceId;
    }
}
