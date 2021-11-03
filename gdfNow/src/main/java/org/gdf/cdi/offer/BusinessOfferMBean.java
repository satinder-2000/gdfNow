/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.offer;

import org.gdf.ejb.ActivityRecorderBeanLocal;
import org.gdf.ejb.BusinessBeanLocal;
import org.gdf.ejb.DeedBeanLocal;
import org.gdf.model.Access;
import org.gdf.model.ActivityType;
import org.gdf.model.Business;
import java.io.Serializable;
import java.util.logging.Logger;
import javax.inject.Named;
import org.gdf.model.BusinessOffer;
import org.gdf.model.Deed;
import org.gdf.model.OfferType;
import org.gdf.util.CurrencyLookUp;
import org.gdf.util.GDFConstants;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.flow.FlowScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author satindersingh
 */
@Named(value = "businessOfferMBean")
@FlowScoped(value = "BusinessOffer")
public class BusinessOfferMBean implements Serializable {
    
    static final Logger LOGGER=Logger.getLogger(BusinessOfferMBean.class.getName());
    
    
    @Inject
    DeedBeanLocal deedBeanLocal;
    
    @Inject
    BusinessBeanLocal businessBeanLocal;
    
    @Inject
    ActivityRecorderBeanLocal activityRecorderBeanLocal;
    
    private int deedId;
    
    private String value;
    
    BusinessOffer businessOffer;
    
    Deed deed;
    
    Business business;
    
    List<String> offerTypes;
    
    private String symbol;
    
    @PostConstruct 
    void init(){
        offerTypes=new ArrayList<>();
        offerTypes.add("Please Select");
        offerTypes.add(OfferType.CASH.toString());
        offerTypes.add(OfferType.DISCOUNT.toString());
        offerTypes.add(OfferType.KIND.toString());
        offerTypes.add(OfferType.PRIZE.toString());
        offerTypes.add(OfferType.SPONSORSHIP.toString());
        
        businessOffer=new BusinessOffer();
        LOGGER.info("BusinessOfferMBean inistialised.");
        HttpServletRequest request= (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=request.getSession();
        Access access=(Access) session.getAttribute(GDFConstants.ACCESS);
        int businessId= access.getEntityId();//(Integer)session.getAttribute(GDFConstants.LOGGED_IN_GDF_ID);
        business= businessBeanLocal.findBusinessById(businessId);
        LOGGER.log(Level.INFO, "Business loaded {0}", business.getWebsite());
        
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
    
    
    public List<String> getOfferTypes(){
       return offerTypes; 
    }
    
    public String offerFilled(){
        //Validate all the fields
        String selectedOfferType=businessOffer.getOfferType();
        if (selectedOfferType.equals("Please Select")){//User did not make any selection
            FacesContext.getCurrentInstance().addMessage("offerType",new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid Offer Type","Invalid Offer Type"));
        }
        //Description is mandatory
        String description = businessOffer.getDescription().trim();
        if (description.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("desc", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Description required", "Description required"));
        }
        //Check amounts
        switch (selectedOfferType){
            case "CASH" : {
                String valStr=businessOffer.getValue().trim();
                if (valStr.isEmpty()){
                    FacesContext.getCurrentInstance().addMessage("amt", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Amount required", "Amount required"));
                }else{
                    String offerVal=businessOffer.getValue();
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
            case "DISCOUNT" :{
                String valStr=businessOffer.getValue().trim();
                if (valStr.isEmpty()){
                    FacesContext.getCurrentInstance().addMessage("amt", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Percentage required", "Percentage required"));
                }else{
                    if (valStr.contains("%")){
                        valStr=valStr.replaceAll("%", "");
                    }
                    double discount = Double.parseDouble(valStr);
                    if (discount <= 0 || discount > 100) {
                        FacesContext.getCurrentInstance().addMessage("amt", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid discount", "Invalid discount"));
                    }
                }
                break;
            }
            default :{
                businessOffer.setValue("0");
            }
        }
        
        List<FacesMessage> msgs= FacesContext.getCurrentInstance().getMessageList();
        if (msgs!=null && msgs.size()>0){
            return null;
        }else{
            return "BusinessOfferConfirm?faces-redirect=true";
        }
    }
    
    public String amendOffer(){
        return "BusinessOfferAmend?faces-redirect=true";
    }
    
    /**
     * 
     * @return to Flow return Page.
     */
    public String getReturnValue() {
        LOGGER.log(Level.INFO, "Deed found {0}", deed.getId());
        LOGGER.log(Level.INFO, "Business found {0}", business.getId());
        LOGGER.log(Level.INFO, "BusinessOffer found {0}", businessOffer.getDescription());
        businessOffer.setOfferedOn(LocalDateTime.now());
        businessOffer.setDeed(deed);
        businessOffer.setBusiness(business);
        business.getBusinessOffers().add(businessOffer);
        deed.getBusinessOffers().add(businessOffer);
        businessOffer= businessBeanLocal.createBusinessOffer(businessOffer, business, deed);
        LOGGER.log(Level.INFO, "Business Offer created on :{0}", businessOffer.getOfferedOn());
        String message=businessOffer.getOfferType().concat(" offer made by Business: ").concat(businessOffer.getBusiness().getName());
        activityRecorderBeanLocal.add(ActivityType.BUSINESS_OFFER,businessOffer.getId(), message,businessOffer.getBusiness().getName());
        return "/flowreturns/BusinessOffer-return?faces-redirect=true";
    }
    
    public void ajaxTypeListener(AjaxBehaviorEvent event){
        String offerType=businessOffer.getOfferType();
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

    public BusinessOffer getBusinessOffer() {
        return businessOffer;
    }

    public void setBusinessOffer(BusinessOffer businessOffer) {
        this.businessOffer = businessOffer;
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
