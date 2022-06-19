/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi;

import org.gdf.ejb.AccessBeanLocal;
import org.gdf.model.Access;
import org.gdf.model.EntityType;
import org.gdf.model.OnHold;
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
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author satindersingh
 */
@Named(value = "accessConfirmMBean")
@ViewScoped
public class AccessConfirmMBean implements Serializable {
    
     final static Logger LOGGER=Logger.getLogger(AccessConfirmMBean.class.getName());
    
    
    
    @Inject
    AccessBeanLocal accessBeanLocal;
    
    private Access access;

    public Access getAccess() {
        return access;
    }

    public void setAccess(Access access) {
        this.access = access;
    }
    
    @PostConstruct
    public void init(){
        LOGGER.info("View initialised for Access");
        loadAccess();
    }

    private void loadAccess() {
        access = new Access();
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String email = request.getParameter("id");
        LOGGER.log(Level.INFO, "Access Confirm request received from {0}", email);
        //This email should be on hold..
        OnHold onHold = accessBeanLocal.getOnHold(email);
        if (onHold==null) {//preventive code. In case of unlikely event, should the Access link gets compromised.
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Id value", "Invalid Id value"));
            return;
        }
        access.setEmail(email);
        access.setEmailOrg(email);
        access.setName(onHold.getName());
        access.setCountryCode(onHold.getCountryCode());
        access.setImage(onHold.getImage());
        LOGGER.log(Level.INFO, "Access Bean Initialised {0}", access.getEmail());
        
    }
    
    public String processForm(){
        String toReturn=null;
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
            Access accessDb=accessBeanLocal.createAccess(this.getAccess());
            EntityType at= accessDb.getEntityType();
            switch (at){
                case USER: {toReturn="UserRegisterFinal";break;}
                case DEEDER:{toReturn="DeederRegisterFinal";break;}
                case BUSINESS:{toReturn="BusinessRegisterFinal";break;}
                case GOVERNMENT:{toReturn="GovernmentRegisterFinal";break;}
                case NGO:{toReturn="NgoRegisterFinal";break;}
                default : toReturn="index";
            }
        }
        LOGGER.log(Level.INFO, "toReturn is :{0}", toReturn);
        return toReturn;
    }
    
}
