/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.register;

import org.gdf.ejb.AccessBeanLocal;
import org.gdf.ejb.BusinessBeanLocal;
import org.gdf.ejb.ReferenceDataBeanLocal;
import org.gdf.model.Access;
import org.gdf.model.Business;
import org.gdf.model.BusinessAddress;
import org.gdf.model.BusinessCategory;
import org.gdf.model.Country;
import org.gdf.util.ConvertPngToJpg;
import org.gdf.util.GDFConstants;
import org.gdf.util.ImageResizeUtil;
import org.gdf.util.ImageVO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
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
@Named(value ="businessMBean" )
@FlowScoped(value = "BusinessRegister")
public class BusinessMBean implements Serializable {
    
    final static Logger LOGGER=Logger.getLogger(BusinessMBean.class.getName());
    
    @Inject
    BusinessBeanLocal businessBeanLocal;
    
    @Inject
    ReferenceDataBeanLocal referenceDataBeanLocal;
    
    @Inject
    AccessBeanLocal accessBeanLocal;
    
    Business business;
    
    BusinessAddress businessAddress;
    
    List<String> businessTypes;
    List<String> businessSubTypes;
    
    private List<BusinessCategory> businessCategories;
    private String type;
    private String subtype;
    private String countryCode;
    private List<String> countries; 
    private boolean acceptedTC;
    
    private Part logoFile;
    
    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public List<String> getCountries() {
        return countries;
    }

    public void setCountries(List<String> countries) {
        this.countries = countries;
    }
    
    
    @PostConstruct
    public void init(){
        business = new Business();
        businessAddress = new BusinessAddress();
        businessAddress.setCountry(new Country());
        business.setBusinessAddress(businessAddress);
        businessAddress.setBusiness(business);
        BusinessCategory businessCategory=new BusinessCategory();
        business.setBusinessCategory(businessCategory);
        LOGGER.info("Business and Address initialised");
        businessTypes=new ArrayList<>();
        businessSubTypes=new ArrayList<>();
        businessTypes.add("Please Select");
        businessSubTypes.add("Please Select");//SubTypes to be popolated later via AJAX call.
        
        List<String> bcTypes=referenceDataBeanLocal.getBusinessCategoryTypes();
        businessTypes.addAll(bcTypes);
        List<Country> countryList=referenceDataBeanLocal.getCountries();
        countries=new ArrayList<>();
        countries.add("Please Select");
        for (Country c : countryList) {
            countries.add(c.getName());
        }
        LOGGER.log(Level.INFO, "{0} Countries added to the List", countries.size());
        LOGGER.log(Level.INFO, "BusinessMBean initialised");
    }
    
    
    public void ajaxTypeListener(AjaxBehaviorEvent event){
        LOGGER.log(Level.INFO, "Business Type is {0}", type);
        businessSubTypes=new ArrayList<>();
        businessSubTypes.add("Please Select");
        businessSubTypes.addAll(referenceDataBeanLocal.getBusinessCategorySubTypes(type));
        LOGGER.log(Level.INFO, type+" {0} Business Types loaded in BusinessMBean ",businessSubTypes);
        
    }
    
    private void validateDetails() {

        String name = business.getName().trim();
        if (name.isEmpty() || name.length() < 2 ) {
            FacesContext.getCurrentInstance().addMessage("busName", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Business Name", "Invalid Business Name."));
        }else if (name.length()>45){
            FacesContext.getCurrentInstance().addMessage("busName", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Name exceeds 45 chars", "Name exceeds 45 chars"));
        }

        String desc = business.getDescription().trim();
        if (desc.isEmpty() || desc.length() < 2 ) {
            FacesContext.getCurrentInstance().addMessage("desc", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Description", "Invalid Description"));
        }else if(name.length()>250){
            FacesContext.getCurrentInstance().addMessage("desc", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Description exceeds 250 chars", "Description exceeds 250 chars"));
        }

        //Validate Website of the business (RegEx did not work in JSF Page)
        String website = business.getWebsite().trim();
        if (!website.isEmpty()) {
            Pattern pWebsite = Pattern.compile(GDFConstants.URL_REGEX);
            Matcher mW = pWebsite.matcher(website);
            boolean matchesU = mW.find();
            if (!matchesU) {
                FacesContext.getCurrentInstance().addMessage("website", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Website", "The website address is not valid"));
            }
        }

        //Validate Email now
        String email = business.getEmail();
        String emailRegEx = GDFConstants.EMAIL_REGEX;
        Pattern pEmail = Pattern.compile(emailRegEx);
        Matcher mP = pEmail.matcher(email);
        boolean matches = mP.find();
        if (!matches) {
            FacesContext.getCurrentInstance().addMessage("email", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Email", "The Email is not valid"));
        }else{
            saveLogoImage();
        }

        //Check if email already exists in the database
        Business businessDb = businessBeanLocal.findBusinessByEmail(email);
        if (businessDb != null) {
            FacesContext.getCurrentInstance().addMessage("email", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Email Taken", "The Email is already registered"));
        }
        //Also check if this email exists in Access table. Fixing Production issue on 05/04/2019 where same email can be used to register as a USER and as a BUSINESS.
        Access access=accessBeanLocal.getAccess(email);
        if (access!=null){
            FacesContext.getCurrentInstance().addMessage("email", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Email Taken", "The Email is already registered"));
        }
        
        //let's assign the BusinessCategory
        List<BusinessCategory> bcList = referenceDataBeanLocal.getBusinessCategories();
        for (BusinessCategory bCt : bcList) {
            if (bCt.getType().equals(type) && bCt.getSubtype().equals(subtype)) {
                business.setBusinessCategory(bCt);
                break;
            }

        }
        //Finally, let's assign the BusinessCategory
        //Before checking the Business Category / Sub Category selected by the User- make sure no new values have been suggested
        String typeTemp=business.getBusinessCategory().getTypeTemp();
        String subtypeTemp=business.getBusinessCategory().getSubtypeTemp();
        if (typeTemp!=null && !typeTemp.isEmpty() && typeTemp.length() > 2) {//atleast a new "type" has been specified be the user. Now check the subtype as well.
            if (subtypeTemp!=null && (subtypeTemp.isEmpty() || subtypeTemp.length() < 2)) {
                FacesContext.getCurrentInstance().addMessage("subTypeNew", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Sub Type (min 2 chars required)", "Invalid Sub Type (min 2 chars required)"));
            } else {
                BusinessCategory businessCategory = new BusinessCategory();
                businessCategory.setType(typeTemp);
                businessCategory.setSubtype(subtypeTemp);
                businessCategory.setConfirmed(false);
                businessBeanLocal.addBusinessCategory(businessCategory);
                business.setBusinessCategory(businessCategory);
                business.setConfirmed(false);//Will be confirmed by the Administrator

            }

        } else {//User has picked from the specified categories
            //Make sure User has NOT selected position holder for type
            boolean bTyp = false;
            if (type.equals("Please Select")) {
                FacesContext.getCurrentInstance().addMessage("bTyp", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Business Type", "Invalid Business Type"));
            } else {
                bTyp = true;
            }//type check ends
            //Make sure User has NOT selected position holder for subtype
            boolean bSTyp = false;
            if (subtype.equals("Please Select")) {
                FacesContext.getCurrentInstance().addMessage("bSTyp", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Business Sub Type", "Invalid Business Sub Type"));
            } else {
                bSTyp = false;
            }//subtype check ends
            //We are OK to proceed
            if (bTyp & bSTyp) {
                if (businessCategories == null) {
                    businessCategories = referenceDataBeanLocal.getBusinessCategories();
                }
                //First set BusinessCategory
                for (BusinessCategory bc : businessCategories) {
                    if (bc.getType().equals(type) && bc.getSubtype().equals(subtype)) {
                        business.setBusinessCategory(bc);
                        break;

                    }
                }
            }
        } 
           
        
        //Address Lines
        String line1 = business.getBusinessAddress().getLine1().trim();
        if (line1.isEmpty() || line1.length() < 2 ) {
            FacesContext.getCurrentInstance().addMessage("line1", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Line 1", "Invalid Line 1."));
        }else if (line1.length()>45){
            FacesContext.getCurrentInstance().addMessage("line1", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Line1 exceeds 45 chars", "Line 1 exceeds 45 chars"));
        }
        
        String line2 = business.getBusinessAddress().getLine2().trim();
        if (line2.isEmpty() || line2.length() < 2 ) {
            FacesContext.getCurrentInstance().addMessage("line2", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Line 2", "Invalid Line 2"));
        }else if (line1.length()>45){
            FacesContext.getCurrentInstance().addMessage("line2", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Line 2 exceeds 45 chars", "Line 2 exceeds 45 chars"));
        }
        
        String line3 = business.getBusinessAddress().getLine3();
        if (!line3.isEmpty() && line3.length()>45){
            FacesContext.getCurrentInstance().addMessage("line3", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Line 3 exceeds 45 chars", "Line 3 exceeds 45 chars"));
        }
        
         
        //Before validating Country specific PostCodes, ensure Country itself is valid
        if (countryCode.equals("")) {//That's the value that appears on top of the list
            FacesContext.getCurrentInstance().addMessage("country", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Country", "Invalid Country Name."));
        } else {
            //Validate PostCode now..
            switch (countryCode) {
                case GDFConstants.IN_CODE: {
                    Pattern pCdIn = Pattern.compile(GDFConstants.IN_POSTCODE_REGEX);
                    Matcher mPCdIn = pCdIn.matcher(businessAddress.getPostcode());
                    if (!mPCdIn.find()) {
                        FacesContext.getCurrentInstance().addMessage("postcode", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid PostCode", "Not a valid Post Code of " + GDFConstants.IN_NAME));
                    }
                    break;

                }
                case GDFConstants.GB_CODE: {
                    Pattern pCdGB = Pattern.compile(GDFConstants.GB_POSTCODE_REGEX);
                    Matcher mPCdGB = pCdGB.matcher(businessAddress.getPostcode());
                    if (!mPCdGB.find()) {
                        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid PostCode", "Not a valid Post Code of " + GDFConstants.GB_NAME));
                    }
                    break;
                }
                case GDFConstants.US_CODE: {
                    Pattern pCdUS = Pattern.compile(GDFConstants.US_POSTCODE_REGEX);
                    Matcher mPCdUS = pCdUS.matcher(businessAddress.getPostcode());
                    if (!mPCdUS.find()) {
                        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Zip Code", "Not a valid Zip Code of " + GDFConstants.US_NAME));
                    }
                }
                break;
            }
        }
        
        //Validate City
        String city=businessAddress.getCity().trim();
        if (city.isEmpty() || city.length()<2){
           FacesContext.getCurrentInstance().addMessage("city", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid City", "Invalid City")); 
        }else if (city.length()>45){
           FacesContext.getCurrentInstance().addMessage("city", new FacesMessage(FacesMessage.SEVERITY_ERROR, "City exceeds 45 chars", "City exceeds 45 chars")); 
        }
        
        //Validate State
        String state=businessAddress.getState().trim();
        if (state.isEmpty() || state.length()<2){
           FacesContext.getCurrentInstance().addMessage("state", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid State", "Invalid State")); 
        }else if (city.length()>45){
           FacesContext.getCurrentInstance().addMessage("state", new FacesMessage(FacesMessage.SEVERITY_ERROR, "State exceeds 45 chars", "State exceeds 45 chars")); 
        }

        //And Country
        List<Country> countryList = referenceDataBeanLocal.getCountries();
        for (Country c : countryList) {
            if (c.getCode().equals(countryCode)) {
                business.getBusinessAddress().setCountry(c);
                break;
            }
        }
        
        //T&C must be accepted
        if (!this.acceptedTC) {
            FacesContext.getCurrentInstance().addMessage("confChBx",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "T&C not accetped.", "T&C not accetped"));
        }
        
    }
    
    
    public void saveLogoImage() {
        if (logoFile==null && business.getImage() == null){
            FacesContext.getCurrentInstance().addMessage("logoFile",
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Logo is required", "Logo is required"));
        }

        if (logoFile != null && business.getImage() == null) {//In amend mode and no new Logo file has been uploaded
            try {
                InputStream input = logoFile.getInputStream();
                int fileSize = (int) logoFile.getSize();
                if (fileSize > (1000 * 1024)) {
                    FacesContext.getCurrentInstance().addMessage("logoFile",
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Profile Img exceeds 1MB", "Profile Img exceeds 1MB"));

                } else {//process with the processing of the image.
                    //Step 1 Resize the image
                    BufferedImage logoBufferedImage = ImageResizeUtil.resizeImage(input, 150);
                    String fullFileName = logoFile.getSubmittedFileName();
                    String fileType = fullFileName.substring(fullFileName.indexOf('.'));
                    byte[] jpgData = null;
                    if (fileType.equals("png")) {//convert to jpg first. Jelastic' OpenJDK doen not handle png images well and throw exception.
                        byte[] pngData = new byte[input.available()];
                        jpgData = ConvertPngToJpg.convertToJpg(pngData);
                    } else {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(logoBufferedImage, "jpg", baos);
                        baos.flush();
                        jpgData = baos.toByteArray();
                        baos.close();
                    }
                    business.setLogoFile(fullFileName);
                    business.setImage(jpgData);
                    //We would need to display the Image in the ConfirmPage, which is next in the Flow.
                    //There is no solution for that - only a workaround.
                    //We put this image in the session for now and once the Deeder data has been persisted in the Database the image from the session will be removed.
                    HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
                    HttpSession session = request.getSession(true);
                    String imgType = fullFileName.substring(fullFileName.indexOf('.') + 1);
                    ImageVO imageVO = new ImageVO(imgType, jpgData);
                    session.setAttribute(GDFConstants.TEMP_IMAGE, imageVO);//This Image will be removed from Session once the data has been persisted.
                }
            } catch (IOException ex) {
                LOGGER.severe(ex.getMessage());
                throw new RuntimeException(ex.getMessage());
            }
        }

    }
    
    /**
     * Method to be called when the details of the Business and its Address are first submitted.
     * @return the URL to next (Confirmation) screen i.e ConfirmDetails page. Else null (same page) if there are validation errors.
     */
    public String detailsCaptured(){
        String toReturn=null;
        validateDetails();
        
        //Finally send to the next Page
        List<FacesMessage> msgs= FacesContext.getCurrentInstance().getMessageList();
        if (msgs!=null && msgs.size()>0){
            toReturn =null;
        }else{
            toReturn="BusinessConfirm?faces-redirect=true";
        }
        return toReturn;
    }
    
    /**
     * User wishes to amend the details provided.
     * @return URL to AmendDetails page. 
     */
    public String amendDetails(){
        return "BusinessAmend?faces-redirect=true";
    }
    
    
    /**
     * Details are finally confirmed by the Business and are submitted in the database.
     * @return the URL to the Acknowledgement page.  
     */
    public String confirmDetails(){
        validateDetails();
        List<FacesMessage> msgs= FacesContext.getCurrentInstance().getMessageList();
        if (msgs!=null && msgs.size()>0){
            return null;
        }else {
            return "BusinessAcknowledge";
        }
    }
    
    void submitDetails(){
        business.setCreatedOn(LocalDateTime.now());
        business.setUpdatedOn(LocalDateTime.now());
        business = businessBeanLocal.createBusiness(business);
        LOGGER.log(Level.INFO, "Business created with ID: of {0} and BusinessAddress ID:{1}", new Object[]{business.getId(), business.getBusinessAddress().getId()});
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session = request.getSession();
        session.removeAttribute(GDFConstants.TEMP_IMAGE);
    }
    
     public String getReturnValue() {
        submitDetails();;
        return "/flowreturns/BusinessRegister-return?faces-redirect=true";
    }
    
    public Business getBusiness() {
        return business;
    }

    public void setBusiness(Business business) {
        this.business = business;
    }

    public BusinessAddress getBusinessAddress() {
        return businessAddress;
    }

    public void setBusinessAddress(BusinessAddress businessAddress) {
        this.businessAddress = businessAddress;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }
    
    
    

    public List<String> getBusinessTypes() {
        return businessTypes;
    }

    public void setBusinessTypes(List<String> businessTypes) {
        this.businessTypes = businessTypes;
    }

    public List<String> getBusinessSubTypes() {
        return businessSubTypes;
    }

    public void setBusinessSubTypes(List<String> businessSubTypes) {
        this.businessSubTypes = businessSubTypes;
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
