/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.register;

import org.gdf.ejb.AccessBeanLocal;
import org.gdf.ejb.NgoBeanLocal;
import org.gdf.ejb.ReferenceDataBeanLocal;
import org.gdf.model.Access;
import org.gdf.model.Country;
import org.gdf.model.Ngo;
import org.gdf.model.NgoAddress;
import org.gdf.model.NgoCategory;
import org.gdf.util.ConvertPngToJpg;
import org.gdf.util.GDFConstants;
import org.gdf.util.ImageResizeUtil;
import org.gdf.util.ImageVO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.flow.FlowScoped;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

/**
 *
 * @author satindersingh
 */
@Named(value ="ngoMBean" )
@FlowScoped(value = "NgoRegister")
public class NgoMBean implements Serializable {
    
    static final Logger LOGGER=Logger.getLogger(NgoMBean.class.getName());
    
    @Inject
    ReferenceDataBeanLocal referenceDataBeanLocal;
    
    @Inject
    NgoBeanLocal ngoBeanLocal;
    
    @Inject
    AccessBeanLocal accessBeanLocal;
    
    
    private Ngo ngo;
    
    private NgoAddress ngoAddress;
    
    private String ngoType;
    
    private String ngoSubtype;
    
    private List<NgoCategory> ngoCategories;
    
    private List<Country> countriesL;
    
    private List<String> ngoTypes;
    
    private List<String> ngoSubtypes;
    
    private List<String> countries;
    
    private String country;
    
    private boolean acceptedTC;
    
     private Part logoFile;
    
    @PostConstruct
    public void init() {
        ngoTypes=new ArrayList<>();
        ngoSubtypes=new ArrayList<>();
        ngoTypes.add("Please Select");
        ngoSubtypes.add("Please Select");
        
        List<String> ngoTypesL=referenceDataBeanLocal.getNgoCategoryTypes();
        LOGGER.log(Level.INFO, "Values in ngoTypesL{0}", ngoTypesL.size());
        ngoTypes.addAll(ngoTypesL);
        String type1=ngoTypesL.get(0);
        List<String> ngoSubTypesL=referenceDataBeanLocal.getNgoCategorySubTypes(type1);
        ngoSubtypes.addAll(ngoSubTypesL);
        LOGGER.log(Level.INFO, "Subtypes {0} loaded for Type {1}", new Object[]{ngoSubTypesL, type1});
        
        countriesL=referenceDataBeanLocal.getCountries();
        countries=new ArrayList<>();
        countries.add("Please Select");
        for (Country c : countriesL) {
            countries.add(c.getName());
        }
        LOGGER.log(Level.INFO, "{0} Countries added to the List", countries.size());
        
        ngo=new Ngo();
        ngo.setNgoCategory(new NgoCategory());
        ngoAddress=new NgoAddress();
        ngoAddress.setCountry(new Country());
        ngo.setNgoAddress(ngoAddress);
        ngoAddress.setNgo(ngo);
        LOGGER.info("Ngo and NgoAddress initialized");
        
    }

    
   public void ajaxTypeListener(AjaxBehaviorEvent event){
        LOGGER.log(Level.INFO, "Ngo Type is {0}", ngoType);
        ngoSubtypes=new ArrayList<>();
        ngoSubtypes.add("Please Select");
        ngoSubtypes.addAll(referenceDataBeanLocal.getNgoCategorySubTypes(ngoType));
        LOGGER.log(Level.INFO, ngoType+" {0} NgoSubtypes Types loaded in NgoMBean ",ngoSubtypes.size());
    }

    
   

   
    public String detailsCaptured() {
        String toReturn=null;
        validateDetails();
        //Finally send to the next Page
        List<FacesMessage> msgs= FacesContext.getCurrentInstance().getMessageList();
        if (msgs!=null && msgs.size()>0){
            toReturn =null;
        }else{
            toReturn="NgoConfirm";
        }
        return toReturn;
    }
    
    private void validateDetails(){
        
        //Name validation
        String name=ngo.getName().trim();
        if (name.isEmpty() || name.length()<2){
            FacesContext.getCurrentInstance().addMessage("name", new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid Name","Invalid Name"));
        }else if (name.length()>125){
            FacesContext.getCurrentInstance().addMessage("name", new FacesMessage(FacesMessage.SEVERITY_ERROR,"Name too long.","Name too long."));
        }
        
        //Validate Email now
        String email=ngo.getEmail().trim();
        boolean status=false;
        if (ngoBeanLocal.findNgoByEmail(email)!=null){
            status=true;
        }
        //Also check if this email exists in Access table. Fixing Production issue on 05/04/2019 where same email can be used to register as a USER and as a BUSINESS.
        Access access=accessBeanLocal.getAccess(email);
        if (access!=null){
            status=true;
            
        }
        if (status){
           FacesContext.getCurrentInstance().addMessage("email",new FacesMessage(FacesMessage.SEVERITY_ERROR,"Email Taken","Email Taken")); 
        }else{
            status=accessBeanLocal.isEmailOnHold(email);
            if (status) {
                FacesContext.getCurrentInstance().addMessage("email", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Email Taken", "Email Taken"));
            }else{//everything is clear.
                String emailRegEx = GDFConstants.EMAIL_REGEX;
                Pattern pEmail = Pattern.compile(emailRegEx);
                Matcher mP = pEmail.matcher(email);
                boolean matches = mP.find();
                if (!matches) {
                    FacesContext.getCurrentInstance().addMessage("email", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Email", "The Email is not valid"));
                }else{
                    saveLogoImage();
                }
            }
        }
        
        //Validate Website of the Ngo (RegEx did not work in JSF Page)
        String website=ngo.getWebsite().trim();
        if (!website.isEmpty()){//Website for an NGO is not mandatory
            Pattern pWebsite = Pattern.compile(GDFConstants.URL_REGEX);
            Matcher mW = pWebsite.matcher(website);
            boolean matchesU = mW.find();
            if (!matchesU) {
                FacesContext.getCurrentInstance().addMessage("website", new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid Website","The website address is not valid"));
            }
        }
        
        
        
        //Validate Description
        String desc=ngo.getDescription().trim();
        if (desc.isEmpty()){
            FacesContext.getCurrentInstance().addMessage("desc",new FacesMessage(FacesMessage.SEVERITY_ERROR,"No Description provided.","No Description provided."));
        }else if (desc.length()<2){
            FacesContext.getCurrentInstance().addMessage("desc",new FacesMessage(FacesMessage.SEVERITY_ERROR,"Description must be 2-250 chars long","Description must be 2-250 chars long"));
        }else if (desc.length()>250){
            FacesContext.getCurrentInstance().addMessage("desc",new FacesMessage(FacesMessage.SEVERITY_ERROR,"Description exceeds the limit of 250 chars","Description exceeds the limit of 250 chars"));
        }
        
        //Address Lines
        String line1=ngo.getNgoAddress().getLine1().trim();
        if (line1.isEmpty() || line1.length()<2){
            FacesContext.getCurrentInstance().addMessage("line1", new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid Address Line 1","Invalid Address Line 1"));
        }else if (line1.length()>45){
            FacesContext.getCurrentInstance().addMessage("line1", new FacesMessage(FacesMessage.SEVERITY_ERROR,"Line 1 exceeds char limit (45)","Line 1 exceeds char limit (45)"));
        }
        
        String line2=ngo.getNgoAddress().getLine2().trim();
        if (line2.isEmpty() || line2.length()<2){
            FacesContext.getCurrentInstance().addMessage("line2", new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid Address Line 2","Invalid Address Line 2"));
        }else if (line2.length()>45){
            FacesContext.getCurrentInstance().addMessage("line2", new FacesMessage(FacesMessage.SEVERITY_ERROR,"Line 2 exceeds char limit (45)","Line 2 exceeds char limit (45)"));
        }
    
        //Phones now
        String phone1=ngoAddress.getPhone().trim();//This is mandatory
        Pattern pPhone=Pattern.compile(GDFConstants.PHONE_REGEX); 
        Matcher ph1M = pPhone.matcher(phone1);
        boolean matchesPh1 = ph1M.find();
        if (!matchesPh1) {
            FacesContext.getCurrentInstance().addMessage("phone1", new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid Phone 1","Phone 1 is invalid"));
        }
        
        //Before validating Country specific PostCodes, ensure Country itself is valid
        if (country.equals("Please Select")) {//That's the value that appears on top of the list
            FacesContext.getCurrentInstance().addMessage("country", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Country", "Invalid Country Name."));
        } else {
            //Validate PostCode now..
            switch (country) {
                case GDFConstants.IN_NAME: {
                    Pattern pCdIn = Pattern.compile(GDFConstants.IN_POSTCODE_REGEX);
                    Matcher mPCdIn = pCdIn.matcher(ngoAddress.getPostcode());
                    if (!mPCdIn.find()) {
                        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Not a valid Post Code of " + GDFConstants.IN_NAME));
                    }
                    break;

                }
                case GDFConstants.GB_NAME: {
                    Pattern pCdGB = Pattern.compile(GDFConstants.GB_POSTCODE_REGEX);
                    Matcher mPCdGB = pCdGB.matcher(ngoAddress.getPostcode());
                    if (!mPCdGB.find()) {
                        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Not a valid Post Code of " + GDFConstants.GB_NAME));
                    }
                    break;
                }
                case GDFConstants.US_NAME: {
                    Pattern pCdUS = Pattern.compile(GDFConstants.US_POSTCODE_REGEX);
                    Matcher mPCdUS = pCdUS.matcher(ngoAddress.getPostcode());
                    if (!mPCdUS.find()) {
                        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Not a valid Zip Code of " + GDFConstants.US_NAME));
                    }
                }
                break;
            }
        }
        
        //Fill in the blanks..
        
        String city=ngoAddress.getCity().trim();
        if (city.isEmpty() || city.length()<2){
            FacesContext.getCurrentInstance().addMessage("city",new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid City","City is invalid")); 
        }else if (city.length()>45){
            FacesContext.getCurrentInstance().addMessage("city",new FacesMessage(FacesMessage.SEVERITY_ERROR,"City exceeds max char(45)","City exceeds max char(45)"));
        }
        
        String state=ngoAddress.getState().trim();
        if (state.length()<2 || state.length()>250){
            FacesContext.getCurrentInstance().addMessage("state",new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid State","State is invalid")); 
        }
        if (state.isEmpty() || state.length()<2){
            FacesContext.getCurrentInstance().addMessage("state",new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid State","State is invalid")); 
        }else if (state.length()>45){
            FacesContext.getCurrentInstance().addMessage("state",new FacesMessage(FacesMessage.SEVERITY_ERROR,"State exceeds max char(45)","State exceeds max char(45)"));
        }
        
        //Before checking the NGO Category / Sub Category selected by the User- make sure no new values have been suggested
        String typeTemp=ngo.getNgoCategory().getTypeTemp();
        String subtypeTemp=ngo.getNgoCategory().getSubtypeTemp();
        if(typeTemp!=null && !typeTemp.isEmpty() & typeTemp.length()>2){//atleast a new "type" has been specified be the user. Now check the subtype as well.
            //if (subtypeTemp!=null && (subtypeTemp.isEmpty() || subtypeTemp.length()<2) ){
            if (!subtypeTemp.isEmpty() && subtypeTemp.length()<2 ){
                FacesContext.getCurrentInstance().addMessage("subTypeNew",new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid Sub Type (min 2 chars required)","Invalid Sub Type (min 2 chars required)")); 
            }else{
                NgoCategory ngoCategory=new NgoCategory();
                ngoCategory.setType(typeTemp.toUpperCase());
                ngoCategory.setSubtype(subtypeTemp.toUpperCase());
                ngoCategory.setConfirmed(false);
                ngoBeanLocal.createNgoCategory(ngoCategory);
                ngo.setNgoCategory(ngoCategory);
                ngo.setConfirmed(false);//Will be confirmed by the Administrator
                
            }
            
        }else{//User has picked from the specified categories
            if (ngoCategories == null) {
                ngoCategories = referenceDataBeanLocal.getNgoCategories();
            }
            //First set NgoCategory
            for (NgoCategory nc : ngoCategories) {
                if (nc.getType().equals(ngoType) && nc.getSubtype().equals(ngoSubtype)) {
                    ngo.setNgoCategory(nc);
                    break;
                }
            }
            
        }
        //Now set Country
        for (Country c : countriesL) {
            if (c.getCode().equals(country)){
                ngoAddress.setCountry(c);
                break;
            }
            
        }
        
        //T&C must be accepted
        if (!this.acceptedTC) {
            FacesContext.getCurrentInstance().addMessage("confChBx",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "T&C not accetped.", "T&C not accetped"));
        }
        
    }
        
    
    
    private void saveLogoImage() {
        try {
            InputStream input = logoFile.getInputStream();
            int fileSize = (int) logoFile.getSize();
            if (fileSize > (1000 * 1024)) {
                FacesContext.getCurrentInstance().addMessage("profileFile",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Profile Img exceeds 1MB", "Profile Img exceeds 1MB"));

            } else {//process with the processing of the image.
                //Step 1 Resize the image
                BufferedImage logoBufferedImage = ImageResizeUtil.resizeImage(input, 150);
                String fullFileName = logoFile.getSubmittedFileName();
                String fileType=fullFileName.substring(fullFileName.indexOf('.'));
                byte[] jpgData=null;
                if (fileType.equals("png")){//convert to jpg first. Jelastic' OpenJDK doen not handle png images well and throw exception.
                    byte[] pngData=new byte[input.available()];
                    jpgData=ConvertPngToJpg.convertToJpg(pngData);
                }else{
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(logoBufferedImage, "jpg", baos);
                    baos.flush();
                    jpgData = baos.toByteArray();
                    baos.close();
                }
                ngo.setLogoFile(fullFileName);
                ngo.setImage(jpgData);
                //We would need to display the Image in the ConfirmPage, which is next in the Flow.
                //There is no solution for that - only a workaround.
                //We put this image in the session for now and once the Deeder data has been persisted in the Database the image from the session will be removed.
                HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
                HttpSession session = request.getSession(true);
                String imgType=fullFileName.substring(fullFileName.indexOf('.')+1);
                ImageVO imageVO=new ImageVO(imgType,jpgData);
                session.setAttribute(GDFConstants.TEMP_IMAGE, imageVO);//This Image will be removed from Session once the data has been persisted.
            }
        } catch (IOException ex) {
            LOGGER.severe(ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }
    }

    public String amendDetails() {
        String toReturn=null;
        validateDetails();
        //Finally send to the next Page
        List<FacesMessage> msgs= FacesContext.getCurrentInstance().getMessageList();
        if (msgs!=null && msgs.size()>0){
            toReturn=null;//stays null
        }else{
            toReturn="NgoAmend?faces-redirect=true";
        }
        return toReturn;
    }

    /**
     * //TODO
     */
    public void submitDetails() {
        ngo.setCreatedOn(LocalDateTime.now());
        ngo.setUpdatedOn(LocalDateTime.now());
        ngo = ngoBeanLocal.createNgo(ngo);
        LOGGER.log(Level.INFO, "NGO persisted with ID: {0} and Address ID: {1}", new Object[]{ngo.getId(), ngo.getNgoAddress().getId()});
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session = request.getSession();
        session.removeAttribute(GDFConstants.TEMP_IMAGE);
    }
    
    public String getReturnValue() {
        submitDetails();
        return "/flowreturns/NgoRegister-return?faces-redirect=true";
    }

    
    public Ngo getNgo() {
        return ngo;
    }

    public void setNgo(Ngo ngo) {
        this.ngo = ngo;
    }

    public NgoAddress getNgoAddress() {
        return ngoAddress;
    }

    public void setNgoAddress(NgoAddress ngoAddress) {
        this.ngoAddress = ngoAddress;
    }

    public String getNgoType() {
        return ngoType;
    }

    public void setNgoType(String ngoType) {
        this.ngoType = ngoType;
    }

    public String getNgoSubtype() {
        return ngoSubtype;
    }

    public void setNgoSubtype(String ngoSubtype) {
        this.ngoSubtype = ngoSubtype;
    }

    public List<String> getNgoTypes() {
        return ngoTypes;
    }

    public void setNgoTypes(List<String> ngoTypes) {
        this.ngoTypes = ngoTypes;
    }

    public List<String> getNgoSubtypes() {
        return ngoSubtypes;
    }

    public void setNgoSubtypes(List<String> ngoSubtypes) {
        this.ngoSubtypes = ngoSubtypes;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public List<String> getCountries() {
        return countries;
    }

    public void setCountries(List<String> countries) {
        this.countries = countries;
    }

    public boolean isAcceptedTC() {
        return acceptedTC;
    }

    public void setAcceptedTC(boolean acceptedTC) {
        this.acceptedTC = acceptedTC;
    }

    public Part getLogoFile() {
        return logoFile;
    }

    public void setLogoFile(Part logoFile) {
        this.logoFile = logoFile;
    }

   
    
}
