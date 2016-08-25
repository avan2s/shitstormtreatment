/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shitstorm.controllers;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.identity.User;
import org.shitstorm.constants.Pages;

/**
 *
 * @author Andy
 */
@Named(value = "userController")
@SessionScoped
public class UserController implements Serializable {

    private String userId = "admin";
    private User user;
    private String password = "administrator";

    @Inject
    private ProcessEngine processEngine;

    public UserController() {
    }

    public boolean isLoggedIn() {
        return userId != null;
    }
    
    public String login(){
        user = processEngine.getIdentityService().createUserQuery().userId(userId).singleResult();
        if(user == null){
            return null;
        }
        return Pages.PAGE_CASE_INSTANCES;
    }
    
    public String logout(){
        final Object session = FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        this.password = null;
        if(session instanceof HttpSession){
            ((HttpSession)session).invalidate();
        }
        userId = null;
        return Pages.PAGE_INDEX;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}

