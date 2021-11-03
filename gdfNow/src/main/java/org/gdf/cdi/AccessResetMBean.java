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
import java.util.logging.Level;
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
@Named(value ="accessResetMBean" )
@ViewScoped
public class AccessResetMBean implements Serializable {
    
    final static Logger LOGGER=Logger.getLogger(AccessResetMBean.class.getName());
    
    
    @Inject
    AccessBeanLocal accessBeanLocal;
    
    private String email;
    
    private Access access;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    public Access getAccess() {
        return access;
    }

    public void setAccess(Access access) {
        this.access = access;
    }
    
    @PostConstruct
    public void init(){
        LOGGER.info("View initialised for Access Reset");
        access=new Access();
    }
    
    public String dispatchResetLink(){
        Access access=accessBeanLocal.getAccess(email);
        if (access==null){
            FacesContext.getCurrentInstance().addMessage("email",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,"Email not found!","Email not found!"));
            return null;
        }else{
            boolean status = accessBeanLocal.dispatchPasswordReset(email);
            if (status) {
                return "DispatchSuccess?faces-redirect=true";
            } else {
                return "error?faces-redirect=true";
            }
        }
        
    }
    
    public String processForm(){
        String toReturn=null;
        Access accessDB=accessBeanLocal.getAccess(access.getEmail());
        
        String password=access.getPassword();
        String passwordConfirm=access.getPasswordConfirm();
        if (password.trim().isEmpty()){
            FacesContext.getCurrentInstance().addMessage("pwd1",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,"No Password","No Password"));
        }else{
            //First, RegEx the password
            Pattern pCdIn=Pattern.compile(GDFConstants.PW_REGEX);
            Matcher mPCdIn=pCdIn.matcher(password);
            if (!mPCdIn.find()){
                FacesContext.getCurrentInstance().addMessage("pwd1",new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid Password","Invalid Password."));  
            }else{//compare the password now
                if(!password.equals(passwordConfirm)){
                    FacesContext.getCurrentInstance().addMessage("pwd2",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,"Passwords do not match","Passwords do not match!"));
                }
                
            }
        }
        List<FacesMessage> msgs= FacesContext.getCurrentInstance().getMessageList();
        if (msgs!=null && msgs.size()>0){
            toReturn =null;
        }else{
            accessDB.setPassword(access.getPassword());
            accessDB.setAttempts(0);
            access=accessBeanLocal.updateAccess(accessDB);
            LOGGER.info("Access reset was successful");
            toReturn ="AccessResetConfirm?faces-redirect=true";
            
        }
        LOGGER.log(Level.INFO, "toReturn is :{0}", toReturn);
        return toReturn;
    }
    
    
    
}
