/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.offer;

import org.gdf.ejb.ActivityRecorderBeanLocal;
import org.gdf.ejb.DeedBeanLocal;
import org.gdf.ejb.NgoBeanLocal;
import org.gdf.model.Access;
import org.gdf.model.ActivityType;
import org.gdf.model.Deed;
import org.gdf.model.Ngo;
import org.gdf.model.NgoOffer;
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
@Named(value = "ngoOfferMBean")
@FlowScoped(value = "NgoOffer")
public class NgoOfferMBean implements Serializable {
    
    static final Logger LOGGER=Logger.getLogger(NgoOfferMBean.class.getName());
    
   
    @Inject
    DeedBeanLocal deedBeanLocal;
    
    @Inject
    NgoBeanLocal ngoBeanLocal;
    
    @Inject
    ActivityRecorderBeanLocal activityRecorderBeanLocal;
    
    private int deedId;
    
    private String value;
    
    NgoOffer ngoOffer;
    
    Deed deed;
    
    Ngo ngo;
    
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
        
        ngoOffer=new NgoOffer();
        LOGGER.info("NgoOfferMBean inistialised.");
        
        HttpServletRequest request= (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=request.getSession();
        Access access=(Access) session.getAttribute(GDFConstants.ACCESS);
        int ngoId= access.getEntityId(); //(Integer)session.getAttribute(GDFConstants.LOGGED_IN_GDF_ID);
        ngo= ngoBeanLocal.findNgoById(ngoId);
        LOGGER.log(Level.INFO, "Ngo loaded {0}", ngo.getWebsite());
        
        String deedIdStr=request.getParameter("deedId");
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
        String selectedOfferType=ngoOffer.getOfferType();
        if (selectedOfferType.equals("Please Select")){//User did not make any selection
            FacesContext.getCurrentInstance().addMessage("offerType",new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid Offer Type","Invalid Offer Type"));
        }
        //Description is mandatory
        String description = ngoOffer.getDescription().trim();
        if (description.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("desc", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Description required", "Description required"));
        }
        //Check amounts
        switch (selectedOfferType){
            case "CASH" : {
                String valStr=ngoOffer.getValue().trim();
                if (valStr.isEmpty()){
                    FacesContext.getCurrentInstance().addMessage("amt", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Amount required", "Amount required"));
                }else{
                    
                    if (valStr.contains(",")){
                       valStr=valStr.replaceAll(",", "");
                    }
                    double cash = Double.parseDouble(valStr);
                    if (cash <= 0) {
                        FacesContext.getCurrentInstance().addMessage("amt", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Postive amount required", "Postive amount required"));
                    }
                }
                break; 
            }
            case "DISCOUNT" :{
                String valStr=ngoOffer.getValue().trim();
                if (valStr.isEmpty()){
                    FacesContext.getCurrentInstance().addMessage("amt", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Percentage required", "Percentage required"));
                }else{
                    double discount = Double.parseDouble(ngoOffer.getValue());
                    if (discount <= 0 || discount > 100) {
                        FacesContext.getCurrentInstance().addMessage("amt", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid discount", "Invalid discount"));
                    }
                }
                break;
            }
            default :{
                ngoOffer.setValue("0");
            }
        }
        
        //Check Description
        String desc=ngoOffer.getDescription().trim();
        if (desc.isEmpty() || desc.length()<2){
            FacesContext.getCurrentInstance().addMessage("desc", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Description", "Invalid Description"));
        }else if(desc.length()>350){
            FacesContext.getCurrentInstance().addMessage("desc", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Description exceeds limit.", "Description exceeds limit."));
        }
        
        List<FacesMessage> msgs= FacesContext.getCurrentInstance().getMessageList();
        if (msgs!=null && msgs.size()>0){
            return null;
        }else{
            return "NgoOfferConfirm?faces-redirect=true";
        }
    }
    
    public String amendOffer(){
        return "NgoOfferAmend?faces-redirect=true";
    }
    
    public String getReturnValue(){
        LOGGER.log(Level.INFO, "Deed found {0}", deed.getId());
        LOGGER.log(Level.INFO, "Ngo found {0}", ngo.getId());
        LOGGER.log(Level.INFO, "NgoOffer found {0}", ngoOffer.getDescription());
        ngoOffer.setOfferedOn(LocalDateTime.now());
        ngoOffer.setDeed(deed);
        ngoOffer.setNgo(ngo);
        ngo.getNgoOffers().add(ngoOffer);
        deed.getNgoOffers().add(ngoOffer);
        ngoOffer= ngoBeanLocal.createNgoOffer(ngoOffer, ngo, deed);
        String message=ngoOffer.getOfferType().concat(" offer made by NGO: ").concat(ngoOffer.getNgo().getName());
        activityRecorderBeanLocal.add(ActivityType.NGO_OFFER,ngoOffer.getId(), message,ngoOffer.getNgo().getName());
        LOGGER.log(Level.INFO, "Ngo Offer created on :{0}", ngoOffer.getOfferedOn());
        return "/flowreturns/NgoOffer-return?faces-redirect=true";
    }
    
    public void ajaxTypeListener(AjaxBehaviorEvent event){
        String offerType=ngoOffer.getOfferType();
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

    public NgoOffer getNgoOffer() {
        return ngoOffer;
    }

    public void setNgoOffer(NgoOffer ngoOffer) {
        this.ngoOffer = ngoOffer;
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
