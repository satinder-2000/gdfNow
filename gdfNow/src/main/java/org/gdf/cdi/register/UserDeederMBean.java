/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.register;

import org.gdf.ejb.AccessBeanLocal;
import org.gdf.ejb.DeedBeanLocal;
import org.gdf.ejb.DeederBeanLocal;
import org.gdf.ejb.UserBeanLocal;
import org.gdf.ejb.UserDeederBeanLocal;
import org.gdf.ejb.ReferenceDataBeanLocal;
import org.gdf.model.Access;
import org.gdf.model.Country;
import org.gdf.model.Deed;
import org.gdf.model.DeedAddress;
import org.gdf.model.DeedCategory;
import org.gdf.model.Deeder;
import org.gdf.model.DeederAddress;
import org.gdf.model.User;
import org.gdf.util.GDFConstants;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author satindersingh
 */
@Named(value ="userDeederMBean" )
@FlowScoped(value = "UserDeederRegister")
public class UserDeederMBean implements Serializable {
    
    
    final static Logger LOGGER=Logger.getLogger(UserDeederMBean.class.getName());
    
    @Inject
    private DeederBeanLocal deederBeanLocal;
    
    @Inject
    private UserBeanLocal userBeanLocal;
    
    @Inject
    private ReferenceDataBeanLocal referenceDataBeanLocal;
    
    
    @Inject
    private UserDeederBeanLocal userDeederBeanLocal;
    
    @Inject
    private DeedBeanLocal deedBeanLocal; 
    
    @Inject
    private AccessBeanLocal accessBeanLocal;
    
    private User user;
    
    private Deeder deeder;
    
    private DeederAddress deederAddress;
    
    private Deed deed;
    
    private DeedAddress deedAddress;
    
    private List<String> deedTypes;
    
    private List<String> deedSubtypes;
    
    private List<DeedCategory> deedCategories;
    
    private String deedType;
    
    private String deedSubtype;
    
    private List<Country> countriesL;
    
    private List<String> countries;
    
    private String country;

    @PostConstruct
    public void init(){
        deeder = new Deeder();
        deederAddress = new DeederAddress();
        Country ctry = new Country();
        deederAddress.setCountry(ctry);
        deeder.setDeederAddress(deederAddress);
        deederAddress.setDeeder(deeder);
        countriesL=referenceDataBeanLocal.getCountries();
        countries=new ArrayList<>();
        countries.add("Please select");
        countriesL.forEach((c) -> {
            countries.add(c.getName());
        });
        LOGGER.log(Level.INFO, "{0} Countries loaded in UserDeederMBean ",countries.size());
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session = request.getSession();
        Access access = (Access) session.getAttribute(GDFConstants.ACCESS);
        String userEmail = access.getEmail();
        user = userBeanLocal.getUser(userEmail);
        LOGGER.info("User Deeder initialised");
    }
    
    public String validateDetails(){
        String toReturn=null;
        
        //Validate Names
        String fname=deeder.getFirstname().trim();
        if (fname.isEmpty() || fname.length()<2){
            FacesContext.getCurrentInstance().addMessage("firstname",new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid First Name","Invalid First Name"));
        }else if (fname.length()>45){
            FacesContext.getCurrentInstance().addMessage("firstname",new FacesMessage(FacesMessage.SEVERITY_ERROR,"First Name too long (max 45)","First Name too long (max 45)"));
        }
        
        String lname=deeder.getLastname().trim();
        if (lname.isEmpty() || lname.length()<2){
            FacesContext.getCurrentInstance().addMessage("lastname",new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid Last Name","Invalid Last Name"));
        }else if (lname.length()>45){
            FacesContext.getCurrentInstance().addMessage("lastname",new FacesMessage(FacesMessage.SEVERITY_ERROR,"Last Name too long (max 45)","Last Name too long (max 45)"));
        }
        
        String email=deeder.getEmail();
        LOGGER.log(Level.INFO, "Will check for: {0}", email);
        if (!email.trim().isEmpty()){//since email is optional 
            boolean exists=deederBeanLocal.deederExists(email);
            Access access=accessBeanLocal.getAccess(email);
            boolean accessExists=false;
            if (access!=null){//the email has been taken by some other Entity
                accessExists=true;
            }
            
            if (exists || accessExists){
               FacesContext.getCurrentInstance().addMessage("email",new FacesMessage(FacesMessage.SEVERITY_ERROR,"Email Taken","This email is already taken."));
            }else{//check the Pattern
               Pattern p = Pattern.compile(GDFConstants.EMAIL_REGEX);
               Matcher emM=p.matcher(email);
               if (!emM.find()){
                 FacesContext.getCurrentInstance().addMessage("email",new FacesMessage(FacesMessage.SEVERITY_ERROR,"Email invalid","Email is invalid.")); 
               }
            }
            
        }
        //DOB Check
        String dobStr = deeder.getDobStr();
        if (dobStr.trim().isEmpty()){
            FacesContext.getCurrentInstance().addMessage("dob",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "DOB is required", "DOB is required"));
        }
        else{
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate dob = LocalDate.parse(dobStr, formatter);
                LocalDate dateNow = LocalDate.now();
                Period p = Period.between(dob, dateNow);
                int years = p.getYears();
                if (years < 13) {
                    FacesContext.getCurrentInstance().addMessage("dob",
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Min permitted age is 13 years", "You must be alteast 13 year old to register"));
                }else{
                    deeder.setDob(dob);
                }
            } catch (DateTimeParseException ex1) {
                FacesContext.getCurrentInstance().addMessage("dob",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Format", "DOB needed in format dd/mm/yyyy."));
            }
        }
        
        //Check the Phone and Mobile
        String phone=deeder.getPhone().trim();
        String mobile=deeder.getMobile().trim();
        if (phone.isEmpty() & mobile.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("phone", new FacesMessage(FacesMessage.SEVERITY_ERROR, "No Phone entered", "No Phone entered."));
            FacesContext.getCurrentInstance().addMessage("mobile", new FacesMessage(FacesMessage.SEVERITY_ERROR, "No Mobile entered", "No Mobile entered."));
        } else {
            Pattern p = Pattern.compile(GDFConstants.PHONE_REGEX);
            if (!phone.isEmpty()) {
                Matcher mP = p.matcher(phone);
                if (!mP.find()) {
                    FacesContext.getCurrentInstance().addMessage("phone", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Phone invalid", "Phone is invalid."));
                }
            }
            if (!mobile.isEmpty()) {
                Matcher mP = p.matcher(mobile);
                if (!mP.find()) {
                    FacesContext.getCurrentInstance().addMessage("mobile", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Mobile invalid", "Mobile is invalid."));
                }
            }
        }
        
        //About Deeder
        String about=deeder.getAbout().trim();
        if(about.isEmpty()){
            FacesContext.getCurrentInstance().addMessage("about",
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "About is required", "About is required."));
        }else if (about.length()<2){
            FacesContext.getCurrentInstance().addMessage("about",
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "About too Short", "About too Short"));
        }else if (about.length()>250){
            FacesContext.getCurrentInstance().addMessage("about",
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "About too long", "About too long"));
        } 
        
        //Check Country now
        String countryCode=deederAddress.getCountry().getCode();//Country is mandatory
        if (countryCode.trim().isEmpty()){
            FacesContext.getCurrentInstance().addMessage("country",new FacesMessage(FacesMessage.SEVERITY_ERROR,"Country invalid","Country is invalid."));
        }else{
            List<Country> countries = referenceDataBeanLocal.getCountries();
            for (Country country : countries) {
                if (country.getCode().equalsIgnoreCase(countryCode)) {
                    deederAddress.setCountry(country);
                    LOGGER.log(Level.INFO, "Country set to {0}", country.getName());
                    break;
                }
            }
        }
        
        //Before validating Country specific PostCodes, ensure Country itself is valid
        if (countryCode.equals("")) {//That's the value that appears on top of the list
            FacesContext.getCurrentInstance().addMessage("country", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Country", "Invalid Country Name."));
        } else {
            //Validate PostCode now..
            switch (countryCode) {
                case GDFConstants.IN_CODE: {
                    Pattern pCdIn = Pattern.compile(GDFConstants.IN_POSTCODE_REGEX);
                    Matcher mPCdIn = pCdIn.matcher(deederAddress.getPostcode());
                    if (!mPCdIn.find()) {
                        FacesContext.getCurrentInstance().addMessage("postcode", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid PostCode", "Not a valid Post Code of " + GDFConstants.IN_NAME));
                    }
                    break;

                }
                case GDFConstants.GB_CODE: {
                    Pattern pCdGB = Pattern.compile(GDFConstants.GB_POSTCODE_REGEX);
                    Matcher mPCdGB = pCdGB.matcher(deederAddress.getPostcode());
                    if (!mPCdGB.find()) {
                        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid PostCode", "Not a valid Post Code of " + GDFConstants.GB_NAME));
                    }
                    break;
                }
                case GDFConstants.US_CODE: {
                    Pattern pCdUS = Pattern.compile(GDFConstants.US_POSTCODE_REGEX);
                    Matcher mPCdUS = pCdUS.matcher(deederAddress.getPostcode());
                    if (!mPCdUS.find()) {
                        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Zip Code", "Not a valid Zip Code of " + GDFConstants.US_NAME));
                    }
                }
                break;
            }
        }
        
        /*try{
            PostCodeValidator.validatePostCode(deederAddress.getPostcode().trim(), deederAddress.getCountry().getCode());
        }catch(ValidationException ex){
            FacesContext.getCurrentInstance().addMessage("postcode",new FacesMessage(FacesMessage.SEVERITY_ERROR,ex.getMessage(),ex.getMessage()));
        }*/
        String city=deederAddress.getCity().trim();
        if (city.isEmpty() || city.length()<2){
           FacesContext.getCurrentInstance().addMessage("city",new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid City","Invalid City Name")); 
        }else if (city.length()>45){
           FacesContext.getCurrentInstance().addMessage("city",new FacesMessage(FacesMessage.SEVERITY_ERROR,"City exceeds max chars(45)","City exceeds max chars(45)"));  
        }
        String state=deederAddress.getState().trim();
        if (state.isEmpty() || state.length()<2){
           FacesContext.getCurrentInstance().addMessage("state",new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid State","Invalid State Name")); 
        }else if (state.length()>45){
           FacesContext.getCurrentInstance().addMessage("state",new FacesMessage(FacesMessage.SEVERITY_ERROR,"State exceeds max chars(45)","State exceeds max chars(45)")); 
        }
        List<FacesMessage> msgs= FacesContext.getCurrentInstance().getMessageList();
        if (msgs!=null && msgs.size()>0){
            toReturn =null;
        }else{
            try {//assignImageToDeeder();
                assignImageToDeeder();
                //Put the Image in session for diaplay on the Confirm page. Make Use of carrier Access object
                HttpServletRequest request=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
                HttpSession session = request.getSession();
                session.setAttribute(GDFConstants.ACCESS_UD, deeder.getImage());
            } catch (IOException ex) {
                Logger.getLogger(UserDeederMBean.class.getName()).log(Level.SEVERE, null, ex);
                throw new RuntimeException(ex);
            }
            toReturn="ConfirmDetails?faces-redirect=true";
        }
        return toReturn;
    }
    
    
    public String submitDetails(){
        
        
        deeder.setDeederAddress(deederAddress);
        deederAddress.setDeeder(deeder);
        deeder.setCreatedOn(LocalDateTime.now());
        deeder.setUpdatedOn(LocalDateTime.now());
        deeder.setConfirmed(false);
        deederAddress.setCreatedOn(LocalDateTime.now());
        deederAddress.setUpdatedOn(LocalDateTime.now());
        deeder = userDeederBeanLocal.createUserDeeder(deeder, user);
        LOGGER.log(Level.INFO, "User Deeder persisted successfully with ID: {0}", deeder.getId());
        return "OfferAddDeed?faces-redirect=true";
        
    }
    
    private void assignImageToDeeder() throws MalformedURLException, IOException {
        String gender=deeder.getGender();
        BufferedImage image = null;
        if (gender.equals("MALE")){
           image = ImageIO.read(FacesContext.getCurrentInstance().getExternalContext().getResource("/resources/images/male.png"));
           deeder.setProfileFile("male.png");
        }else if (gender.equals("FEMALE")){
            image = ImageIO.read(FacesContext.getCurrentInstance().getExternalContext().getResource("/resources/images/female.png"));
            deeder.setProfileFile("female.png");
        }else{//is OTHER
            image = ImageIO.read(FacesContext.getCurrentInstance().getExternalContext().getResource("/resources/images/other.png"));
            deeder.setProfileFile("other.png");
        }
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] imageInByte = baos.toByteArray();
	baos.close();
        deeder.setImage(imageInByte);
        
        
    }
    
    
    public String suggestDeed(){
        deedTypes=new ArrayList<>();
        deedSubtypes=new ArrayList<>();
        deedCategories= referenceDataBeanLocal.getDeedCategories();
        LOGGER.log(Level.INFO, "{0} Deed Categories loaded in UserDeederMBean ",deedCategories.size());
        deedTypes=new ArrayList<>();
        deedTypes.add("Please select");
        deedTypes.addAll(referenceDataBeanLocal.getDeedCategoryTypes());
        deedSubtypes.add("Please select");//SubTypes to be popolated later via AJAX call.
        LOGGER.log(Level.INFO, "{0} Deed Types loaded in UserDeederMBean ",deedTypes.size());
        deed=new Deed();
        deedAddress=new DeedAddress();
        deedAddress.setCountry(new Country());
        deed.setDeedAddress(deedAddress);
        deed.setDeeder(deeder);
        return "SuggestDeedDetails?faces-redirect=true";
        
    }
    
    public void ajaxTypeListener(AjaxBehaviorEvent event){
        LOGGER.log(Level.INFO, "Deed Type is {0}", deedType);
        deedSubtypes=new ArrayList<>();
        deedSubtypes.add("Please select");
        deedSubtypes.addAll(referenceDataBeanLocal.getDeedCategorySubTypes(deedType));
        LOGGER.log(Level.INFO, deedType+" {0} Deed Types loaded in DeedMBean ",deedSubtypes);
    }
    
    public String deedDetailsFilled() {
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
                LocalDate dDt = LocalDate.parse(deedDtStr, formatter);
                LocalDate dateNow = LocalDate.now();
                if(dDt.isAfter(dateNow)){
                    FacesContext.getCurrentInstance().addMessage("deeddate",
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Deed date cannot be in future", "Deed date cannot be in future"));
                }else {
                    deed.setDeedDate(dDt);
                }
                
            } catch (DateTimeParseException ex1) {
                FacesContext.getCurrentInstance().addMessage("deeddate",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Deed Date-Invalid Format", "Deed Date-Invalid Format"));
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
        country=deedAddress.getCountry().getName();
        if (country.equals("Please Select")) {//That's the value that appears on top of the list
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
            toReturn="ConfirmDeedDetails?faces-redirect=true";
        }
        
        return toReturn;
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
    
    
    public String amendDeedDetails(){
        return "SuggestDeedDetails?faces-redirect=true";
    }
    
    
    public String getReturnValue() {
        if (deed!=null){//In case User has opted NOT to specify the Deed of the Deeder.
          submitDeedDetails();  
        }
        return "/flowreturns/UserDeederRegister-return?faces-redirect=true";
    }
    
    public void submitDeedDetails(){
        deed.setDeedAddress(deedAddress);
        deed.setConfirmed(false);
        deedAddress.setDeed(deed);
        deed= deedBeanLocal.createDeed(deed);
        LOGGER.log(Level.INFO, "Deed created in the database with ID:{0}",deed.getId());
    }
    
    
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Deeder getDeeder() {
        return deeder;
    }

    public void setDeeder(Deeder deeder) {
        this.deeder = deeder;
    }

    public DeederAddress getDeederAddress() {
        return deederAddress;
    }

    public void setDeederAddress(DeederAddress deederAddress) {
        this.deederAddress = deederAddress;
    }

    public Deed getDeed() {
        return deed;
    }

    public void setDeed(Deed deed) {
        this.deed = deed;
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

    public List<DeedCategory> getDeedCategories() {
        return deedCategories;
    }

    public void setDeedCategories(List<DeedCategory> deedCategories) {
        this.deedCategories = deedCategories;
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

    public List<String> getCountries() {
        return countries;
    }

    public void setCountries(List<String> countries) {
        this.countries = countries;
    }

    public DeedAddress getDeedAddress() {
        return deedAddress;
    }

    public void setDeedAddress(DeedAddress deedAddress) {
        this.deedAddress = deedAddress;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    
    
    

    
    
    
    
    
    
}
