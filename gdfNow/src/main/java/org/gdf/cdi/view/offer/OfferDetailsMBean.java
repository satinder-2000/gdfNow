/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.view.offer;

import org.gdf.ejb.BusinessBeanLocal;
import org.gdf.ejb.GovernmentBeanLocal;
import org.gdf.ejb.NgoBeanLocal;
import org.gdf.model.Business;
import org.gdf.model.BusinessOffer;
import org.gdf.model.Deeder;
import org.gdf.model.Government;
import org.gdf.model.GovernmentOffer;
import org.gdf.model.Ngo;
import org.gdf.model.NgoOffer;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author satindersingh
 */
@ViewScoped
@Named(value = "offerDetailsMBean")
public class OfferDetailsMBean implements Serializable {
    
    static final Logger LOGGER=Logger.getLogger(OfferDetailsMBean.class.getName());
    
    @Inject
    BusinessBeanLocal businessBeanLocal;
    
    @Inject
    GovernmentBeanLocal governmentBeanLocal;
    
    @Inject
    NgoBeanLocal ngoBeanLocal;
    
    
    BusinessOffer businessOffer;
    
    GovernmentOffer governmentOffer;
    
    NgoOffer ngoOffer;
    
    String documentServer;
    String businessDocPath;
    String governmentDocPath;
    String ngoDocPath;
    String deederDocPath;
    
    
    
    @PostConstruct
    public void init(){
        LOGGER.info("OfferDetailsMBean initialised");
        ExternalContext extContext=FacesContext.getCurrentInstance().getExternalContext();
        documentServer=extContext.getInitParameter("DocumentServer");
        businessDocPath= extContext.getInitParameter("BusinessDocPath");
        governmentDocPath=extContext.getInitParameter("GovernmentDocPath");
        ngoDocPath=extContext.getInitParameter("NgoDocPath");
        deederDocPath=extContext.getInitParameter("DeederDocPath");
        loadOffer();//Called this method on 08/10 14:15 to accomodate the call from index.xhtml to view the Offer Details. 
                    //This wasn't called prior to changing into the BootStrap tamplate. 
    }
    
    
    private String offeror;
    private int offerId;
    
    
    public String loadOffer(){
        
        String toReturn=null;
        HttpServletRequest request= (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        offeror=request.getParameter("offeror");
        String offerIdStr=request.getParameter("offerId");
        offerId= Integer.parseInt(offerIdStr);
        LOGGER.log(Level.INFO, "Offer ID is {0}", offerId);
        
        
        switch (offeror){
            case "BUSINESS":{
                businessOffer=businessBeanLocal.getBusinessOffer(offerId);
                Business business=businessOffer.getBusiness();
                String businessPath=documentServer.concat(businessDocPath).concat(business.getEmail());
                String logoFilePath=businessPath+"/"+business.getLogoFile();
                business.setLogoURL(logoFilePath);
                //set Pofile Image of the Deeder now
                Deeder deeder=businessOffer.getDeed().getDeeder();
                String deederPath=documentServer.concat(deederDocPath).concat(deeder.getEmail());
                String profileFilePath=deederPath+"/"+deeder.getProfileFile();
                deeder.setProfileURL(profileFilePath);
                
                LOGGER.log(Level.INFO, "BusinessOffer loaded with ID:{0}, Deed: {1} and Deeder : {2}", new Object[]{businessOffer.getId(), businessOffer.getDeed().getId(), businessOffer.getDeed().getDeeder().getId()});
                LOGGER.info(businessOffer.getOfferedOn().toString());
                LOGGER.info(businessOffer.getDescription());
                toReturn="/view/BusinessOfferDetails?faces-redirect=true";
                break;
            }
            case "GOVERNMENT":{
                governmentOffer=governmentBeanLocal.getGovernmentOffer(offerId);
                Government government=  governmentOffer.getGovernment();
                String governmentPath=documentServer.concat(governmentDocPath).concat(government.getEmail1());
                String logoFilePath=governmentPath+"/"+government.getLogoFile();
                government.setLogoURL(logoFilePath);
                //set Pofile Image of the Deeder now
                Deeder deeder=governmentOffer.getDeed().getDeeder();
                String deederPath=documentServer.concat(deederDocPath).concat(deeder.getEmail());
                String profileFilePath=deederPath+"/"+deeder.getProfileFile();
                deeder.setProfileURL(profileFilePath);
                LOGGER.log(Level.INFO, "GovernmentOffer loaded with ID:{0}, Deed: {1} and Deeder : {2}", new Object[]{governmentOffer.getId(), governmentOffer.getDeed().getId(), governmentOffer.getDeed().getDeeder().getId()});
                toReturn="/view/GovernmentOfferDetails?faces-redirect=true";
                break;
            }
            case "NGO":{
                ngoOffer=ngoBeanLocal.getNgoOffer(offerId);
                Ngo ngo=ngoOffer.getNgo();
                String ngoPath=documentServer.concat(ngoDocPath).concat(ngo.getEmail());
                String logoFilePath=ngoPath+"/"+ngo.getLogoFile();
                ngo.setLogoURL(logoFilePath);
                //set Pofile Image of the Deeder now
                Deeder deeder=ngoOffer.getDeed().getDeeder();
                String deederPath=documentServer.concat(deederDocPath).concat(deeder.getEmail());
                String profileFilePath=deederPath+"/"+deeder.getProfileFile();
                deeder.setProfileURL(profileFilePath);
                LOGGER.log(Level.INFO, "NgoOffer loaded with ID:{0}, Deed: {1} and Deeder : {2}", new Object[]{ngoOffer.getId(), ngoOffer.getDeed().getId(), ngoOffer.getDeed().getDeeder().getId()});
                toReturn="/view/NgoOfferDetails?faces-redirect=true";
                break;
            }
            default :{
                throw new RuntimeException("No valid Offeror found");
            }
        }
        return toReturn;
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
    
    
    
    
    
    
}
