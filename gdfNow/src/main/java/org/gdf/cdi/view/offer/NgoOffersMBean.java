/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.view.offer;

import org.gdf.model.Access;
import org.gdf.model.NgoOffer;
import org.gdf.ejb.NgoBeanLocal;
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
@Named(value = "ngoOffersMBean")
@RequestScoped
public class NgoOffersMBean {
    
    static final Logger LOGGER=Logger.getLogger(NgoOffersMBean.class.getName());
    
    List<NgoOffer> ngoOffers;
    
    @Inject
    NgoBeanLocal ngoBeanLocal;
    
    @PostConstruct
    public void init(){
        HttpServletRequest request=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=request.getSession();
        Access access=(Access) session.getAttribute(GDFConstants.ACCESS);
        Integer ngoId= access.getEntityId();//(Integer)session.getAttribute(GDFConstants.LOGGED_IN_GDF_ID);
        ngoOffers= ngoBeanLocal.getAllNgoOffers(ngoId);
        LOGGER.log(Level.INFO, "Total NGO Offers loaded in NgoOffersMBean {0}", ngoOffers.size());
    }
    
    

    public List<NgoOffer> getNgoOffers() {
        return ngoOffers;
    }

    public void setNgoOffers(List<NgoOffer> ngoOffers) {
        this.ngoOffers = ngoOffers;
    }
    
    
    
    
    
    
}
