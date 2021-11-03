/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.offer.view.deeder;

import org.gdf.ejb.BusinessBeanLocal;
import org.gdf.ejb.GovernmentBeanLocal;
import org.gdf.ejb.NgoBeanLocal;
import org.gdf.model.BusinessOffer;
import org.gdf.model.GovernmentOffer;
import org.gdf.model.NgoOffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author satindersingh
 */
@Named(value = "deederOfferDetailsMBean")
@RequestScoped
public class DeederOfferDetailsMBean {

    static final Logger LOGGER = Logger.getLogger(DeederOfferDetailsMBean.class.getName());

    @Inject
    BusinessBeanLocal businessBeanLocal;
    
    @Inject
    GovernmentBeanLocal governmentBeanLocal;
    
    @Inject
    NgoBeanLocal ngoBeanLocal;

    BusinessOffer businessOffer;

    GovernmentOffer governmentOffer;

    NgoOffer ngoOffer;

    @PostConstruct
    public void init() {
        LOGGER.info("OfferDetailsMBean initialised");
        loadOffer();
    }

    private String offeror;
    private int offerId;

    public String loadOffer() {

        String toReturn = null;
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        offeror = request.getParameter("offeror");
        String offerIdStr = request.getParameter("offerId");
        offerId = Integer.parseInt(offerIdStr);
        LOGGER.log(Level.INFO, "Offer ID is {0}", offerId);

        switch (offeror) {
            case "BUSINESS": {
                businessOffer = businessBeanLocal.getBusinessOfferTree(offerId);
                LOGGER.log(Level.INFO, "BusinessOffer loaded with ID:{0}, Deed: {1} and Deeder : {2}", new Object[]{businessOffer.getId(), businessOffer.getDeed().getId(), businessOffer.getDeed().getDeeder().getId()});
                LOGGER.info(businessOffer.getOfferedOn().toString());
                LOGGER.info(businessOffer.getDescription());
                toReturn = "/view/deeder/DeederBODetails?faces-redirect=true";
                break;
            }
            case "GOVERNMENT": {
                governmentOffer = governmentBeanLocal.getGovernmentOffer(offerId);
                LOGGER.log(Level.INFO, "GovernmentOffer loaded with ID:{0}, Deed: {1} and Deeder : {2}", new Object[]{governmentOffer.getId(), governmentOffer.getDeed().getId(), governmentOffer.getDeed().getDeeder().getId()});
                toReturn = "/view/GovernmentOfferDetails?faces-redirect=true";
                break;
            }
            case "NGO": {
                ngoOffer = ngoBeanLocal.getNgoOffer(offerId);
                LOGGER.log(Level.INFO, "NgoOffer loaded with ID:{0}, Deed: {1} and Deeder : {2}", new Object[]{ngoOffer.getId(), ngoOffer.getDeed().getId(), ngoOffer.getDeed().getDeeder().getId()});
                toReturn = "/view/NgoOfferDetails?faces-redirect=true";
                break;
            }
            default: {
                throw new RuntimeException("No valid Offeror found");
            }
        }
        return toReturn;
    }

    public BusinessOffer getBusinessOffer() {
        return businessOffer;
    }

    public void setBusinessOffer(BusinessOffer businessOffer) {
        this.businessOffer = businessOffer;
    }

    public GovernmentOffer getGovernmentOffer() {
        return governmentOffer;
    }

    public void setGovernmentOffer(GovernmentOffer governmentOffer) {
        this.governmentOffer = governmentOffer;
    }

    public NgoOffer getNgoOffer() {
        return ngoOffer;
    }

    public void setNgoOffer(NgoOffer ngoOffer) {
        this.ngoOffer = ngoOffer;
    }

    public String getOfferor() {
        return offeror;
    }

    public void setOfferor(String offeror) {
        this.offeror = offeror;
    }

    public int getOfferId() {
        return offerId;
    }

    public void setOfferId(int offerId) {
        this.offerId = offerId;
    }

}
