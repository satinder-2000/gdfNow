/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.register;

import org.gdf.ejb.DeedBeanLocal;
import org.gdf.ejb.DeederBeanLocal;
import org.gdf.ejb.ReferenceDataBeanLocal;
import org.gdf.model.Access;
import org.gdf.model.Country;
import org.gdf.model.Deed;
import org.gdf.model.DeedAddress;
import org.gdf.model.DeedCategory;
import org.gdf.model.Deeder;
import org.gdf.util.GDFConstants;
import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
@Named(value ="deedMBean" )
@FlowScoped(value = "CreateDeed")
public class DeedMBean implements Serializable {
    
    final static Logger LOGGER=Logger.getLogger(DeedMBean.class.getName());
    
    //@Inject
    //Conversation conversation;
    
    @Inject
    private DeederBeanLocal deederBeanLocal;
    
    @Inject
    private ReferenceDataBeanLocal referenceDataBeanLocal;
    
    @Inject
    private DeedBeanLocal deedBeanLocal;
    
    private Deeder deeder;
    
    private Deed deed;
    
    private DeedAddress deedAddress;
    
    private List<String> deedTypes;
    
    private List<String> deedSubtypes;
    
    List<DeedCategory> deedCategories;
    
    private List<Country> countriesL;
    
    private List<String> countries;
    
    private String deedType;
    
    private String deedSubtype;
    
    private String country;
    
    @PostConstruct
    public void init(){
        //startConversation();
        deedTypes=new ArrayList<>();
        deedSubtypes=new ArrayList<>();
        deedCategories= referenceDataBeanLocal.getDeedCategories();
        LOGGER.log(Level.INFO, "{0} Deed Categories loaded in DeedMBean ",deedCategories.size());
        deedTypes=new ArrayList<>();
        deedTypes.add("Please select");
        deedTypes.addAll(referenceDataBeanLocal.getDeedCategoryTypes());
        deedSubtypes.add("Please select");//SubTypes to be popolated later via AJAX call.
        LOGGER.log(Level.INFO, "{0} Deed Types loaded in DeedMBean ",deedTypes.size());
        
        countriesL=referenceDataBeanLocal.getCountries();
        countries=new ArrayList<>();
        countries.add("Please select");
        countriesL.forEach((c) -> {
            countries.add(c.getName());
        });
        LOGGER.log(Level.INFO, "{0} Countries loaded in DeedMBean ",countries.size());
        deed = new Deed();
        deedAddress = new DeedAddress();
        Country ctry = new Country();
        deedAddress.setCountry(ctry);
        deed.setDeedAddress(deedAddress);
        DeedCategory dc=new DeedCategory();
        deed.setDeedCategory(dc);
        LOGGER.info("Deed, Deed Category and Address initialised");
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session = request.getSession();
        Access access=(Access) session.getAttribute(GDFConstants.ACCESS);
        String deederEmail = access.getEmail();//(String) session.getAttribute(GDFConstants.LOGGED_IN_GDF);
        deeder = deederBeanLocal.getDeeder(deederEmail);
        deed.setDeeder(deeder);
        LOGGER.info("Deeder attached to the Deed");
        
    }
    
    public void ajaxTypeListener(AjaxBehaviorEvent event){
        LOGGER.log(Level.INFO, "Deed Type is {0}", deedType);
        deedSubtypes=new ArrayList<>();
        deedSubtypes.add("Please select");
        deedSubtypes.addAll(referenceDataBeanLocal.getDeedCategorySubTypes(deedType));
        LOGGER.log(Level.INFO, deedType+" {0} Deed Types loaded in DeedMBean ",deedSubtypes);
    }
    
    
    public String detailsFilled() {
        String toReturn=null;
        //Set Deed date for persistence.
        String deedDtStr = deed.getDateStr();
        LOGGER.log(Level.INFO, "deedDtStr {0}", deedDtStr);
        if (deedDtStr.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("deeddate",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Deed Date is required", "Deed Date is required"));
        } else {
            try {//date should be in format dd/MM/yyyy
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate deedDt = LocalDate.parse(deedDtStr, formatter);
                LocalDate dateNow = LocalDate.now();
                //Deed Date must be in the Past
                long daysElapsed=ChronoUnit.DAYS.between(deedDt,dateNow);//this must be positive implying deed was conducted in the past.
                if (daysElapsed<0){//Error if future date is entered.
                    FacesContext.getCurrentInstance().addMessage("deeddate",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Deed Date must be in the past.", "Deed Date must be in the past."));
                }else{
                    deed.setDeedDate(deedDt);
                }
            } catch (DateTimeParseException ex1) {
                FacesContext.getCurrentInstance().addMessage("dob",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "DOB needed in format DD/MM/YYY", "DOB needed in format DD/MM/YYY."));
            }
        }
        //Validate Title
        String deedTitle=deed.getTitle().trim();
        if (deedTitle.isEmpty() || deedTitle.length()<2){
            FacesContext.getCurrentInstance().addMessage("title", new FacesMessage(FacesMessage.SEVERITY_ERROR, "No Title applied", "No Title applied"));
        }else{
            if(deedTitle.length()>50){
             FacesContext.getCurrentInstance().addMessage("title", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Title exceeds limit of 50 chars", "Title exceeds limit of 50 chars"));   
            }
        }
        
        //Validate Links
        String regEx = GDFConstants.URL_REGEX;
        Pattern p =Pattern.compile(regEx);
        String link1=deed.getLink1();
        LOGGER.log(Level.INFO, "link1 {0}", link1);
        if (link1 == null || link1.trim().length() == 0) {//Link 1 is mandatory
            FacesContext.getCurrentInstance().addMessage("link1", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Link 1 required", "Link 1 to the Deed is mandatory"));
        } else {
            Matcher mP = p.matcher(link1);
            if (!mP.find()) {
                FacesContext.getCurrentInstance().addMessage("link1", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Poor URL", "Invalid Link 1 URL"));
            } else {
                //try connecting to the link now.
                if (!link1.startsWith("http://") && !link1.startsWith("https://")) {
                    deed.setLink1("http://" + deed.getLink1());
                    link1 = deed.getLink1();
                }
                try {
                    processExternalLink(link1, p);

                } catch (IOException ex) {
                    FacesContext.getCurrentInstance().addMessage("link1", new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ex.getMessage()));
                }
            }
        }
        //process other links now
        String link2=deed.getLink2();
        if (!link2.trim().isEmpty()) {
            //try connecting to the link now.
            if (!link2.startsWith("http://") && !link2.startsWith("https://")) {
                deed.setLink2("http://" + deed.getLink2());
                link2 = deed.getLink2();
            }
            try {
                processExternalLink(link2, p);
            } catch (IOException ex) {
                FacesContext.getCurrentInstance().addMessage("link2", new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ex.getMessage()));
            }
        }
        String link3=deed.getLink3();
        if (!link3.trim().isEmpty()) {
            //try connecting to the link now.
            if (!link3.startsWith("http://") && !link3.startsWith("https://")) {
                deed.setLink3("http://" + deed.getLink3());
                link3 = deed.getLink3();
            }
            try {
                processExternalLink(link3, p);
            } catch (IOException ex) {
                FacesContext.getCurrentInstance().addMessage("link3", new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ex.getMessage()));
            }
        }
        
        //Attach Country and Deed Category before sending
        for (DeedCategory dc: deedCategories) {
            if (dc.getSubtype().equals(deedSubtype)){
                deed.setDeedCategory(dc);
            }
        }
        //Process Deed Category before sending
        boolean deedValid=false;
        if (deed.getDeedCategory().getTypeTemp()!=null || deed.getDeedCategory().getSubTypeTemp()!=null){//alleast on of the field has been enterted by the use
            deedValid=processNewDeedCategory();
        }else{
            String errorIn=null;
            if (deedType.equals("Please select")){
               FacesContext.getCurrentInstance().addMessage("deedtype", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Deed Type not selected", "Deed Type not selected."));
               errorIn="deedtype";
               deedValid = false;//thought not needed to set.
            }
            if (deedSubtype.equals("Please select")){
               FacesContext.getCurrentInstance().addMessage("deedSubtype", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Deed Sub Type not selected", "Deed Sub Type not selected."));
               errorIn="deedSubtype";
               deedValid = false;//thought not needed to set.
            }
            if (errorIn==null){//means selections have been made
                for (DeedCategory dc : deedCategories) {
                    if (dc.getSubtype().equals(deedSubtype)) {
                        deed.setDeedCategory(dc);
                    }
                }
                deedValid = true;
            }
            
        }
        //replicate the Deed details to match the Deed Category details.
        deed.setConfirmed(deedValid);
        
        // Validate Address 
        
        //Now do the form validation 
        String line1=deedAddress.getLine1().trim();
        if (line1.isEmpty() || line1.length()<2){
           FacesContext.getCurrentInstance().addMessage("line1",new FacesMessage(FacesMessage.SEVERITY_ERROR,"Line 1 required","Line 1 is mandatory")); 
        }else if (line1.length()>75){
            FacesContext.getCurrentInstance().addMessage("line1",new FacesMessage(FacesMessage.SEVERITY_ERROR,"Line 1 exceeds 75 chars","Line 1 exceeds 75 chars")); 
        }
        String city=deedAddress.getCity().trim();
        if (city.isEmpty() || city.length()<2){
           FacesContext.getCurrentInstance().addMessage("city", new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid City","Invalid City.")); 
        }else if (city.length()>45){
            FacesContext.getCurrentInstance().addMessage("city", new FacesMessage(FacesMessage.SEVERITY_ERROR,"City exceeds 45 chars","City exceeds 45 chars"));
        }
        String state=deedAddress.getState().trim();
        if (state.isEmpty() || state.length()<2){
           FacesContext.getCurrentInstance().addMessage("state",new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid State","Invalid State.")); 
        }else if (state.length()>45){
            FacesContext.getCurrentInstance().addMessage("state", new FacesMessage(FacesMessage.SEVERITY_ERROR,"State exceeds 45 chars","State exceeds 45 chars"));
        }
        
        //Before validating Country specific PostCodes, ensure Country itself is valid
        if (country.equalsIgnoreCase("Please Select")) {//That's the value that appears on top of the list
            FacesContext.getCurrentInstance().addMessage("country", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Country", "Invalid Country Name."));
        } else {
            //Validate PostCode now..
            switch (country) {
                case GDFConstants.IN_NAME: {
                    Pattern pCdIn = Pattern.compile(GDFConstants.IN_POSTCODE_REGEX);
                    Matcher mPCdIn = pCdIn.matcher(deedAddress.getPostcode());
                    if (!mPCdIn.find()) {
                        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Not a valid Post Code of " + GDFConstants.IN_NAME));
                    }
                    break;

                }
                case GDFConstants.GB_NAME: {
                    Pattern pCdGB = Pattern.compile(GDFConstants.GB_POSTCODE_REGEX);
                    Matcher mPCdGB = pCdGB.matcher(deedAddress.getPostcode());
                    if (!mPCdGB.find()) {
                        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Not a valid Post Code of " + GDFConstants.GB_NAME));
                    }
                    break;
                }
                case GDFConstants.US_NAME: {
                    Pattern pCdUS = Pattern.compile(GDFConstants.US_POSTCODE_REGEX);
                    Matcher mPCdUS = pCdUS.matcher(deedAddress.getPostcode());
                    if (!mPCdUS.find()) {
                        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Not a valid Zip Code of " + GDFConstants.US_NAME));
                    }
                }
                break;
            }
        }

        //Now set Country
        for (Country c : countriesL) {
            if (c.getName().equals(country)){
                deedAddress.setCountry(c);
                break;
            }
        }
        
        //Finally send to the next Page
        List<FacesMessage> msgs= FacesContext.getCurrentInstance().getMessageList();
        if (msgs!=null && msgs.size()>0){
            toReturn =null;
        }else{
            toReturn="ConfirmDeed?faces-redirect=true";
        }
        
        return toReturn;
    }
    
    
    
    public String amendDetails(){
        return "AmendDeed?faces-redirect=true";
    }
    
    public void submitDetails(){
        deed.setDeedAddress(deedAddress);
        deedAddress.setDeed(deed);
        deed= deedBeanLocal.createDeed(deed);
        LOGGER.log(Level.INFO, "Deed created in the database with ID:{0}",deed.getId());
        
    }
    
    public String getReturnValue(){
        submitDetails();
        return "/flowreturns/CreateDeed-return?faces-redirect=true";
    }
    
    
    private void processExternalLink(String link, Pattern p) throws IOException {
        Matcher mP = p.matcher(link);
        if (!mP.find()) {
            throw new IOException("Invalid URL format");
        } else {
            validateURLConnectivity(link);
        }
    }
    
    
    
    private void validateURLConnectivity(String strUrl) throws IOException{
        URL url = null;
        HttpURLConnection urlConn = null;
        try {
            url = new URL(strUrl);
            urlConn = (HttpURLConnection) url.openConnection();
            urlConn.connect();
        }catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error occured while commecting to {0} ErrorMesage:{1}", new Object[]{strUrl, e.getMessage()});
            throw new IOException("Failed to connect.");
        } finally {
            if (urlConn != null) {
                urlConn.disconnect();
            }
        }
    }
    
        
    public String getDeedType() {
        return deedType;
    }

    public void setDeedType(String deedType) {
        this.deedType = deedType;
    }

    public String getDeedSubtype() {
        return deedSubtype;
    }

    public void setDeedSubtype(String deedSubtype) {
        this.deedSubtype = deedSubtype;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
    

    public Deed getDeed() {
        return deed;
    }

    public void setDeed(Deed deed) {
        this.deed = deed;
    }

    public DeedAddress getDeedAddress() {
        return deedAddress;
    }

    public void setDeedAddress(DeedAddress deedAddress) {
        this.deedAddress = deedAddress;
    }

    public List<String> getDeedTypes() {
        return deedTypes;
    }

    public void setDeedTypes(List<String> deedTypes) {
        this.deedTypes = deedTypes;
    }

    public List<String> getDeedSubtypes() {
        return deedSubtypes;
    }

    public void setDeedSubtypes(List<String> deedSubtypes) {
        this.deedSubtypes = deedSubtypes;
    }

    public List<String> getCountries() {
        return countries;
    }

    public void setCountries(List<String> countries) {
        this.countries = countries;
    }

    /**
     * This method is responsible to assign the Deed Category to the Deed that the Deeder is creating.
     * This method takes into account if Deeder has specified new Deed Type and/or new Deed Sub Type
     * @return 
     */
    private boolean processNewDeedCategory() {
        boolean toReturn=true;
        DeedCategory dc=deed.getDeedCategory();
        dc.setType(getDeedType());
        dc.setSubtype(getDeedSubtype());
        String dcTypeTemp=dc.getTypeTemp();
        String dcSubTypeTemp=dc.getSubTypeTemp();
        String errorIn=null;
        //Case :Type is New and Sub Type is from List
        if (!dcTypeTemp.isEmpty() && !dc.getSubtype().equals("Please select")){
            if (dcTypeTemp.length() < 2) {//Min required length is 2 chars
                FacesContext.getCurrentInstance().addMessage("deedtype", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Deed Type", "Deed Type must have > 2 chars."));
                errorIn = "typeTemp";
            }else{
                dc.setType(dcTypeTemp.toUpperCase());
                dc.setConfirmed(false);
                toReturn=false;
            }
        }
        //Case :Type - New andSub Type New
        if (!dcTypeTemp.isEmpty() && !dcSubTypeTemp.isEmpty()){
            if (dcTypeTemp.length() < 2) {//Min required length is 2 chars
                FacesContext.getCurrentInstance().addMessage("deedtype", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Deed Type", "Deed Type must have > 2 chars."));
                errorIn = "typeTemp";
            }if (dcSubTypeTemp.length() < 2) {//Min required length is 2 chars
                FacesContext.getCurrentInstance().addMessage("deedSubtype", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Deed Sub Type", "Deed Sub Type must have > 2 chars."));
                errorIn = "typeSubTemp";
            }//couldn't do else-if
            if (errorIn==null){//Deed Category is New
                dc.setType(dcTypeTemp.toUpperCase());
                dc.setSubtype(dcSubTypeTemp.toUpperCase());
                dc.setConfirmed(false);
                toReturn=false;
            }
        }
        //Case :Type - From List and Sub Type New
        if (!dc.getType().equals("Please select") && !dcSubTypeTemp.isEmpty()){
            if (dcSubTypeTemp.length() < 2) {//Min required length is 2 chars
                FacesContext.getCurrentInstance().addMessage("deedSubtype", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Deed Sub Type", "Deed Sub Type must have > 2 chars."));
                errorIn = "typeTemp";
            }else{
                dc.setSubtype(dcSubTypeTemp.toUpperCase());
                dc.setConfirmed(false);
                toReturn=false;
            }
        }
        
        //We need to persist the new DC now
        referenceDataBeanLocal.addDeedCategory(dc);
        
        
        return toReturn;
        
    }


    
    
}
