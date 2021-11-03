/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.deed;

import org.gdf.ejb.DeedBeanLocal;
import org.gdf.model.Access;
import org.gdf.model.Deed;
import org.gdf.util.GDFConstants;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author satindersingh
 */
@Named(value="deedsListingMBean")
@ViewScoped
public class DeedsListingMBean implements Serializable {
    
    static final Logger LOGGER=Logger.getLogger(DeedsListingMBean.class.getName());
    
    @Inject
    DeedBeanLocal deedBeanLocal;
    
    List<Deed> deeds;
    
    List<Deed> deederDeeds;
    
    @PostConstruct
    public void getDeedsListing(){
        deeds=deedBeanLocal.getDeedsSummary();
        LOGGER.log(Level.INFO, "Deeds Listed has extracted {0} deeds", deeds.size());
    }

    public List<Deed> getDeeds() {
        return deeds;
    }

    public void setDeeds(List<Deed> deeds) {
        this.deeds = deeds;
    }
    
    public List<Deed> getDeedsOfLoggedInDeeder(){
        HttpServletRequest request= (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=request.getSession();
        Access access=(Access) session.getAttribute(GDFConstants.ACCESS);
        String loggedInDeeder= access.getEmail();//(String) session.getAttribute(GDFConstants.LOGGED_IN_GDF);
        List<Deed> deedsL= deedBeanLocal.getDeedsOfDeeder(loggedInDeeder);
        if (deedsL==null){
            FacesContext.getCurrentInstance().addMessage(null,new FacesMessage(FacesMessage.SEVERITY_WARN,"No Deeds found.","No Deeds found."));
            return null;
        }else{
            return deedsL;
        }
    }

    public List<Deed> getDeederDeeds() {
        deederDeeds=getDeedsOfLoggedInDeeder();
        return deederDeeds;
    }

    public void setDeederDeeds(List<Deed> deederDeeds) {
        this.deederDeeds = deederDeeds;
    }
    
    public String deedDetails(int deedId){
        return "DeedDetails?deedId"+deedId;
    }
    
    
    
    
    
    
}
