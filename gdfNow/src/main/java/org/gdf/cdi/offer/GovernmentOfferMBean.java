/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.offer;

import org.gdf.ejb.ActivityRecorderBeanLocal;
import org.gdf.ejb.DeedBeanLocal;
import org.gdf.ejb.GovernmentBeanLocal;
import org.gdf.model.Access;
import org.gdf.model.ActivityType;
import org.gdf.model.Government;
import org.gdf.model.GovernmentOffer;
import org.gdf.model.Deed;
import org.gdf.model.OfferType;
import org.gdf.util.CurrencyLookUp;
import org.gdf.util.GDFConstants;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.flow.FlowScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author satindersingh
 */
@Named(value = "governmentOfferMBean")
@FlowScoped(value = "GovernmentOffer")
public class GovernmentOfferMBean implements Serializable {

    static final Logger LOGGER = Logger.getLogger(GovernmentOfferMBean.class.getName());

    @Inject
    DeedBeanLocal deedBeanLocal;

    @Inject
    GovernmentBeanLocal  governmentBeanLocal;
    
    @Inject
    ActivityRecorderBeanLocal activityRecorderBeanLocal;
    
    private int deedId;

    private String value;

    GovernmentOffer governmentOffer;

    Deed deed;

    Government government;

    List<String> offerTypes;
    
    private String symbol;

    @PostConstruct
    void init() {
        offerTypes = new ArrayList<>();
        offerTypes.add("Please Select");
        offerTypes.add(OfferType.CASH.toString());
        offerTypes.add(OfferType.DISCOUNT.toString());
        offerTypes.add(OfferType.KIND.toString());
        offerTypes.add(OfferType.PRIZE.toString());
        offerTypes.add(OfferType.SPONSORSHIP.toString());

        governmentOffer = new GovernmentOffer();
        LOGGER.info("GovernmentOfferMBean inistialised.");
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session = request.getSession();
        Access access=(Access) session.getAttribute(GDFConstants.ACCESS);
        int governmentId = access.getEntityId(); //(Integer) session.getAttribute(GDFConstants.LOGGED_IN_GDF_ID);
        government = governmentBeanLocal.findGovernmentById(governmentId);
        LOGGER.log(Level.INFO, "Government loaded {0}", government.getWebsite());
        
        String deedIdStr= request.getParameter("deedId");
        deedId=Integer.parseInt(deedIdStr);
        deed= deedBeanLocal.getDeedDetails(deedId);
        LOGGER.log(Level.INFO, "Deed loaded {0}", deed.getDescription());
        

    }

    public int getDeedId() {
        return deedId;
    }

    public void setDeedId(int deedId) {
        this.deedId = deedId;
    }

    private String offerType;

    public List<String> getOfferTypes() {
        return offerTypes;
    }

    

    public String offerFilled() {
        //Validate all the fields
        String selectedOfferType = governmentOffer.getOfferType();
        if (selectedOfferType.equals("Please Select")) {//User did not make any selection
            FacesContext.getCurrentInstance().addMessage("offerType", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Offer Type", "Invalid Offer Type"));
        }
        //Description is mandatory
        String description = governmentOffer.getDescription().trim();
        if (description.isEmpty() || description.length()<2) {
            FacesContext.getCurrentInstance().addMessage("desc", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Description required", "Description required"));
        }else if (description.length()>350){
            FacesContext.getCurrentInstance().addMessage("desc", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Description exceeds 350 chars", "Description exceeds 350 chars"));
        }
        //Check amounts
        switch (selectedOfferType) {
            case "CASH": {
                String valStr = governmentOffer.getValue().trim();
                if (valStr.isEmpty()) {
                    FacesContext.getCurrentInstance().addMessage("amt", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Amount required", "Amount required"));
                } else {
                    String offerVal=governmentOffer.getValue();
                    if (offerVal.contains(",")){
                       offerVal=offerVal.replaceAll(",", "");
                    }
                    double cash = Double.parseDouble(offerVal);
                    if (cash <= 0) {
                        FacesContext.getCurrentInstance().addMessage("amt", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Postive amount required", "Postive amount required"));
                    }
                }
                break;
            }
            case "DISCOUNT": {
                String valStr = governmentOffer.getValue().trim();
                if (valStr.isEmpty()) {
                    FacesContext.getCurrentInstance().addMessage("amt", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Percentage required", "Percentage required"));
                } else {
                    double discount = Double.parseDouble(governmentOffer.getValue());
                    if (discount <= 0 || discount > 100) {
                        FacesContext.getCurrentInstance().addMessage("amt", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid discount", "Invalid discount"));
                    }
                }
                break;
            }
            default :{
                governmentOffer.setValue("0");
            }
        }

        List<FacesMessage> msgs = FacesContext.getCurrentInstance().getMessageList();
        if (msgs != null && msgs.size() > 0) {
            return null;
        } else {
            return "GovernmentOfferConfirm?faces-redirect=true";
        }
    }

    public String amendOffer() {
        return "GovernmentOfferAmend?faces-redirect=true";
    }

    public String getReturnValue() {
        LOGGER.log(Level.INFO, "Deed found {0}", deed.getId());
        LOGGER.log(Level.INFO, "Government found {0}", government.getId());
        LOGGER.log(Level.INFO, "GovernmentOffer found {0}", governmentOffer.getDescription());
        governmentOffer.setOfferedOn(LocalDateTime.now());
        governmentOffer.setDeed(deed);
        governmentOffer.setGovernment(government);
        government.getGovernmentOffers().add(governmentOffer);
        deed.getGovernmentOffers().add(governmentOffer);
        governmentOffer = governmentBeanLocal.createGovernmentOffer(governmentOffer, government, deed);
        String message=governmentOffer.getOfferType().concat(" offer made by Government: ").concat(governmentOffer.getGovernment().getOfficeName());
        activityRecorderBeanLocal.add(ActivityType.GOVERNMENT_OFFER,governmentOffer.getId(), message,governmentOffer.getGovernment().getOfficeName());
        LOGGER.log(Level.INFO, "Government Offer created on :{0}", governmentOffer.getOfferedOn());
        return "/flowreturns/GovernmentOffer-return?faces-redirect=true";
    }
    
    public void ajaxTypeListener(AjaxBehaviorEvent event){
        String offerType=governmentOffer.getOfferType();
        LOGGER.log(Level.INFO, "Offer Type is {0}",offerType);
        OfferType ot=OfferType.valueOf(offerType);
        switch (ot){
            case CASH : {
                String countryCode=deed.getDeedAddress().getCountry().getCode();
                //The current should the currency of the Country of the Deed
                symbol=CurrencyLookUp.getSymbol(countryCode);
                break;
            }
            case DISCOUNT : {
                symbol="%";
                break;
            }
            default : {
                symbol="NA";
            }
        }
    }

    public GovernmentOffer getGovernmentOffer() {
        return governmentOffer;
    }

    public void setGovernmentOffer(GovernmentOffer governmentOffer) {
        this.governmentOffer = governmentOffer;
    }

    public String getOfferType() {
        return offerType;
    }

    public void setOfferType(String offerType) {
        this.offerType = offerType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Deed getDeed() {
        return deed;
    }

    public void setDeed(Deed deed) {
        this.deed = deed;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    
    

}
