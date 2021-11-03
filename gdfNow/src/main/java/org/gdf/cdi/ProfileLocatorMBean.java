/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi;

import org.gdf.model.Access;
import org.gdf.model.AccessType;
import org.gdf.util.GDFConstants;
import java.io.Serializable;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author satindersingh
 */
@Named(value = "profileLocatorMBean")
@SessionScoped
public class ProfileLocatorMBean implements Serializable{
    
    static final Logger LOGGER=Logger.getLogger(ProfileLocatorMBean.class.getName());
    
    ExternalContext extContext;
    
    Properties profileMap;
    
    String location;
    
    private String documentServer;
    
    //private String accessType;
    
    private String name;
    
    
    
    @PostConstruct
    public void init(){
        profileMap=new Properties();
        extContext=FacesContext.getCurrentInstance().getExternalContext();
        documentServer=extContext.getInitParameter("DocumentServer");
        profileMap.put("default", documentServer + extContext.getInitParameter("GdfLogo"));
        LOGGER.info("ProfileLocatorMBean initialised");
        
    }
    
    void retrieveLocationAndName(){
        //LOGGER.info("In retrieveLocationAndName()");
        extContext=FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest request = (HttpServletRequest) extContext.getRequest();
        HttpSession session = request.getSession(true);
        //A Session won't be there at startup and also at times there won't be user specifc details  in it.
        if (session==null || session.getAttribute(GDFConstants.ACCESS) == null){
            location = (String) profileMap.getProperty("default");
            name="";
        } else {
            Access access = (Access) session.getAttribute(GDFConstants.ACCESS);
            String email = access.getEmail();
            location = (String) profileMap.getProperty(email);
            name=access.getName();
            if (location == null) {
                AccessType accessType = access.getAccessType();
                String profileFile = access.getProfileFile();
                switch (accessType) {
                    case USER: {
                        location = documentServer + extContext.getInitParameter("UserDocPath") + email + "/" + profileFile;
                        profileMap.put(email, location);
                        LOGGER.log(Level.INFO, "Location: {0}", location);
                        break;
                    }
                    case DEEDER: {
                        location = documentServer + extContext.getInitParameter("DeederDocPath") + email + "/" + profileFile;
                        profileMap.put(email, location);
                        LOGGER.log(Level.INFO, "Location: {0}", location);
                        break;

                    }
                    case BUSINESS: {
                        location = documentServer + extContext.getInitParameter("BusinessDocPath") + email + "/" + profileFile;
                        profileMap.put(email, location);
                        LOGGER.log(Level.INFO, "Location: {0}", location);
                        break;

                    }
                    case GOVERNMENT: {
                        location = documentServer + extContext.getInitParameter("GovernmentDocPath") + email + "/" + profileFile;
                        profileMap.put(email, location);
                        LOGGER.log(Level.INFO, "Location: {0}", location);
                        break;

                    }
                    case NGO: {
                        location = documentServer + extContext.getInitParameter("NgoDocPath") + email + "/" + profileFile;
                        profileMap.put(email, location);
                        LOGGER.log(Level.INFO, "Location: {0}", location);
                        break;

                    }
                }
            }

        }
        
    }

    public String getLocation() {
        retrieveLocationAndName();
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

   
    
    
    
    
    
}
