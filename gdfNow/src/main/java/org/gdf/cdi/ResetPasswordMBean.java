/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi;

import org.gdf.ejb.AccessBeanLocal;
import org.gdf.model.Access;
import org.gdf.util.GDFConstants;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author root
 */
@Named(value = "resetPasswordMBean")
@ViewScoped
public class ResetPasswordMBean implements Serializable {
    
    static final Logger LOGGER=Logger.getLogger(ResetPasswordMBean.class.getName());
    
    @Inject
    AccessBeanLocal accessBeanLocal;
    
    Access access;
    
    @PostConstruct
    public void init(){
        access=new Access();
        LOGGER.info("Access initialised in ResetPasswordMBean");
    }
    
    public String changePassword(){
        String toReturn=null;//it will always be null- neverthless - kept for easy readibility of the code.
        //First make sure the email is valid
        Access accessDb=accessBeanLocal.getAccess(access.getEmail());
        if (accessDb==null){
           FacesContext.getCurrentInstance().addMessage("email",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,"No Email found.","No Email found.")); 
        }else{
            LOGGER.info("Access request is valid for email: " + access.getEmail());
            String password = access.getPassword();
            String passwordConfirm = access.getPasswordConfirm();
            if (password.trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage("pwd1",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "No Password", "No Password"));
            } else {
                //First, RegEx the password
                Pattern pCdIn = Pattern.compile(GDFConstants.PW_REGEX);
                Matcher mPCdIn = pCdIn.matcher(password);
                if (!mPCdIn.find()) {
                    FacesContext.getCurrentInstance().addMessage("pwd1", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Password", "Invalid Password."));
                } else {//compare the password now
                    if (!password.equals(passwordConfirm)) {
                        FacesContext.getCurrentInstance().addMessage("pwd2",
                                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Passwords do not match", "Passwords do not match!"));
                    }

                }
            }
            List<FacesMessage> msgs = FacesContext.getCurrentInstance().getMessageList();
            if (msgs != null && msgs.size() > 0) {
                toReturn = null;
            } else {
                accessDb.setPassword(password);
                access = accessBeanLocal.changePassword(accessDb, password);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Password changed", "Password changed."));
                toReturn = null;

            }
            
            
        }
        return toReturn;
    }

    public Access getAccess() {
        return access;
    }

    public void setAccess(Access access) {
        this.access = access;
    }
    
    
    
}
