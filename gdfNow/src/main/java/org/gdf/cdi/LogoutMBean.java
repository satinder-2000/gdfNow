/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi;

import org.gdf.model.Access;
import org.gdf.util.GDFConstants;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author satindersingh
 */
@Named(value = "logoutMBean")
@RequestScoped
public class LogoutMBean implements Serializable {
    
    static final Logger LOGGER=Logger.getLogger(LogoutMBean.class.getName());
    
    
    public String logOut(){
        LOGGER.info("Logout Called.");
        HttpServletRequest request= (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=request.getSession();
        //Object ob=session.getAttribute(GDFConstants.LOGGED_IN_GDF);
        Access access=(Access)session.getAttribute(GDFConstants.ACCESS);
        if (access!=null){
            String email=access.getEmail();
            session.removeAttribute(GDFConstants.ACCESS);
            LOGGER.log(Level.INFO, "LogoutMBean : GDF User {0} logged out successfully.", email);
        }
        
        return "/LogoutConfirm?faces-redirect=true";
    }
    
}
