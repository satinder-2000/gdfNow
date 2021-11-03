/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.view.offer;

import org.gdf.ejb.GovernmentBeanLocal;
import org.gdf.model.Access;
import org.gdf.model.GovernmentOffer;
import org.gdf.util.GDFConstants;
import java.util.List;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 *
 * @author satindersingh
 */
@Named(value = "governmentOffersMBean")
@RequestScoped
public class GovernmentOffersMBean {
    
    static final Logger LOGGER=Logger.getLogger(GovernmentOffersMBean.class.getName());
    
    private List<GovernmentOffer> governmentOffers;
    
    @Inject
    GovernmentBeanLocal governmentBeanLocal;
    
    @PostConstruct
    void init(){
        loadOffers();
        LOGGER.info("GovernmentOffersMBean initialised.");
    }
    
    public void loadOffers(){
        HttpServletRequest request=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=request.getSession();
        Access access=(Access) session.getAttribute(GDFConstants.ACCESS);
        Integer governmentId= access.getEntityId();//(Integer)session.getAttribute(GDFConstants.LOGGED_IN_GDF_ID);
        governmentOffers=governmentBeanLocal.getAllGovernmentOffers(governmentId);
        LOGGER.log(Level.INFO, "Total Government Offers loaded in GovernmentOffersMBean {0}", governmentOffers.size());
        
    }

    public List<GovernmentOffer> getGovernmentOffers() {
        
        return governmentOffers;
    }

    public void setGovernmentOffers(List<GovernmentOffer> governmentOffers) {
        this.governmentOffers = governmentOffers;
    }
    
    
    
}
