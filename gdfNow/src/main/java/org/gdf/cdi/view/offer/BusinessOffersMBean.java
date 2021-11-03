/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.view.offer;

import org.gdf.model.Access;
import org.gdf.model.BusinessOffer;
import org.gdf.ejb.BusinessBeanLocal;
import org.gdf.util.GDFConstants;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author satindersingh
 */
@Named(value = "businessOffersMBean")
@RequestScoped
public class BusinessOffersMBean {
    
    static final Logger LOGGER=Logger.getLogger(BusinessOffersMBean.class.getName());
    
    @Inject
    BusinessBeanLocal businessBeanLocal;
    
    private List<BusinessOffer> businessOffers;
    
    @PostConstruct
    public void init(){
        loadOffers();
        LOGGER.info("BusinessOffersMBean initialised.");
        
    }
    
    void loadOffers(){
        HttpServletRequest request=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=request.getSession();
        Access access=(Access) session.getAttribute(GDFConstants.ACCESS);
        Integer businessId= access.getEntityId();//(Integer)session.getAttribute(GDFConstants.LOGGED_IN_GDF_ID);
        businessOffers=businessBeanLocal.getAllBusinessOffers(businessId);
        LOGGER.log(Level.INFO, "Total Busines Offers loaded in BusinessOffersMBean {0}", businessOffers.size());
        
    }

    public List<BusinessOffer> getBusinessOffers() {
        return businessOffers;
    }

    public void setBusinessOffers(List<BusinessOffer> businessOffers) {
        this.businessOffers = businessOffers;
    }
    
    
    
    
    
    
    
}
