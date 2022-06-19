/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi;



import org.gdf.model.Access;
import org.gdf.model.EntityType;
import org.gdf.ejb.AccessBeanLocal;
import org.gdf.util.GDFConstants;
import java.io.Serializable;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;


/**
 *
 * @author satindersingh
 */
@Named(value = "loginMBean")
@ViewScoped
public class LoginMBean implements Serializable {
    
    static final Logger LOGGER=Logger.getLogger(LoginMBean.class.getName());
    
    @Inject
    AccessBeanLocal accessBeanLocal;
    
    
    private String email;
    
    private String password;
    
    String documentServer;
    
    ExternalContext extCtx;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    @PostConstruct
    public void init(){
        extCtx=FacesContext.getCurrentInstance().getExternalContext();
        LOGGER.info("View created for Login");
    }
    
    public String logIn(){
        Access access=null;
        String type= null;
        String nextPage=null;
        try {
            if(email.isEmpty()){
                throw new RuntimeException("No Email provided");
            }
            access= accessBeanLocal.getAccess(email, password);
            if (access.getExceptionMsg()!=null){
                throw new RuntimeException(access.getExceptionMsg());
            }
            
            if (access!=null){
                LOGGER.info("Access granted to "+access.getEmail());
            }else{
                LOGGER.warning("Failed to grant Access");
            }
            
            String profileFile = access.getProfileFile();
            EntityType acType=access.getEntityType();
            
            switch (acType){
                case USER : {
                    //access.setProfileURL(documentServer + extCtx.getInitParameter("UserDocPath") + email + "/" + profileFile);
                    nextPage="home/UserHome?faces-redirect=true";
                    break;
                }
                case DEEDER : {
                    //access.setProfileURL(documentServer + extCtx.getInitParameter("DeederDocPath") + email + "/" + profileFile);
                    nextPage="home/DeederHome?faces-redirect=true";
                    break;
                }
                case BUSINESS : {
                    //access.setProfileURL(documentServer + extCtx.getInitParameter("BusinessDocPath") + email + "/" + profileFile);
                    nextPage="home/BusinessHome?faces-redirect=true";
                    break;
                }
                case GOVERNMENT : {
                    //access.setProfileURL(documentServer + extCtx.getInitParameter("GovernmentDocPath") + email + "/" + profileFile);
                    nextPage="home/GovernmentHome?faces-redirect=true";
                    break;
                }
                case NGO : {
                    //access.setProfileURL(documentServer + extCtx.getInitParameter("NgoDocPath") + email + "/" + profileFile);
                    nextPage="home/NgoHome?faces-redirect=true";
                    break;
                }

            }
            
            //HttpServletRequest request= (HttpServletRequest)extCtx.getRequest();
            FacesContext facesContext = FacesContext.getCurrentInstance();
            HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
            //HttpSession session=(HttpSession)extCtx.getSession(true);
            session.setAttribute(GDFConstants.ACCESS, access);
            
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,ex.getMessage(),ex.getMessage()));
            return null;
        }
        
        
        
        return nextPage;
    }
    
}
