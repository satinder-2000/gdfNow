/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.register;


import org.gdf.ejb.AccessBeanLocal;
import org.gdf.ejb.ReferenceDataBeanLocal;
import org.gdf.ejb.UserBeanLocal;
import org.gdf.model.Access;
import org.gdf.model.UserAddress;
import org.gdf.model.Country;
import org.gdf.model.User;
import org.gdf.util.ConvertPngToJpg;
import org.gdf.util.GDFConstants;
import org.gdf.util.ImageResizeUtil;
import org.gdf.util.ImageVO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
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
@Named(value = "userRegisterMBean")
@FlowScoped("UserRegister")
public class UserRegisterMBean implements Serializable {

    final static Logger LOGGER=Logger.getLogger(UserRegisterMBean.class.getName());
    
    @Inject
    private UserBeanLocal userBeanLocal;

    @Inject
    private ReferenceDataBeanLocal referenceDataBeanLocal;
    
    @Inject
    private AccessBeanLocal accessBeanLocal;

    private User user;

    private UserAddress address;

    private boolean acceptedTC;
    
    private Part profileFile;
    
    @PostConstruct
    public void init() {
        user = new User();
        address = new UserAddress();
        Country ctry = new Country();
        address.setCountry(ctry);
        user.setAddress(address);
        LOGGER.info("User and Address initialised");
    }
    
    public String personalSave() {
        String toReturn = validatePersonalDetails();
        if (toReturn != null) {
            toReturn = "UserAddress?faces-redirect=true";
        }
        return toReturn;
    }

    public String personalAmended() {
        String toReturn = validatePersonalDetailsAmended();
        if (toReturn != null) {
            toReturn = "UserConfirm?faces-redirect=true";
        }
        return toReturn;
    }

    public String amendPersonal() {
        return "UserAmend?faces-redirect=true";
    }

    private String validatePersonalDetails() {
        String toReturn = null;
        //Validate Name first
        String fname=user.getFirstname().trim();
        if (fname.isEmpty() || fname.length()<2){
           FacesContext.getCurrentInstance().addMessage("firstname",new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid First Name", "Invalid First Name")); 
        }else if (fname.length()>45){
           FacesContext.getCurrentInstance().addMessage("firstname",new FacesMessage(FacesMessage.SEVERITY_ERROR, "First Name too long (max 45 chars)", "First Name too long (max 45 chars)"));  
        }
        
        
        String email = user.getEmail();
        LOGGER.log(Level.INFO, "Will check for: {0}", email);
        boolean exists = userBeanLocal.userExists(email);
        boolean accessExists=false;
        Access access= accessBeanLocal.getAccess(email);
        if (access!=null){//implies the email has been taken by some other Entity
           accessExists=true; 
        }
        if (exists || accessExists) {
            FacesContext.getCurrentInstance().addMessage("email",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Email registered.", "This email is already registered."));
        } else {//Email Regex validation
            Pattern p = Pattern.compile(GDFConstants.EMAIL_REGEX);
            Matcher m = p.matcher(email);
            if (!m.find()) {
                FacesContext.getCurrentInstance().addMessage("email", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Email invalid.", "Invalid Email."));
            } else {//Email is valid. Let's take this opportunity to store the Profile Image on Document Server
                String fileName = profileFile.getSubmittedFileName();
                if (fileName != null && !fileName.equals(user.getProfileFile())) {//If the Image has been altered, then an only then change the Image.This is likely to happen when the User attempts to Amend the personal details.
                    saveProfileImage();
                }
            }
        }

        //DOB Check
        String dobStr = user.getDobStr();
        if (dobStr.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("dob",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Date of Birth required", "Date of Birth required."));
        } else {
            try {//date should be in format dd/MM/yyyy
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate dob = LocalDate.parse(dobStr, formatter);
                LocalDate dateNow = LocalDate.now();
                Period p = Period.between(dob, dateNow);
                int years = p.getYears();
                if (years < 13) {
                    FacesContext.getCurrentInstance().addMessage("dob",
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Min permitted age is 13 years", "You must be alteast 13 year old to register"));
                }else{
                    user.setDob(dob);
                }
            } catch (DateTimeParseException ex1) {
                FacesContext.getCurrentInstance().addMessage("dob",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Format", "DOB needed in format dd/mm/yyyy."));
            }
        }

        //Check the Phone and Mobile
        String phone = user.getPhone();
        String mobile = user.getMobile();

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
        
        //T&C must be accepted
        if (!this.acceptedTC) {
            FacesContext.getCurrentInstance().addMessage("confChBx",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "T&C not accetped.", "T&C not accetped"));
        }
        
        List<FacesMessage> msgs = FacesContext.getCurrentInstance().getMessageList();
        if (msgs != null && msgs.size() > 0) {
            toReturn = null;
        } else {
            toReturn = "";
        }
        return toReturn;

    }

    public String addressSave() {
        String toReturn = validateAddress();
        if (toReturn != null) {
            toReturn = "UserConfirm?faces-redirect=true";
        }
        return toReturn;
    }

    public String amendAddress() {
        return "AmendAddress?faces-redirect=true";
    }

    public String addressAmended() {
        return addressSave();
    }

    private String validateAddress() {
        String toReturn = null;
        List<Country> countries = referenceDataBeanLocal.getCountries();
        String countryCode = address.getCountry().getCode();
        if (countryCode.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("country",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Country is required", "Country is required."));
        } else {
            for (Country country : countries) {
                if (country.getCode().equalsIgnoreCase(countryCode)) {
                    address.setCountry(country);
                    LOGGER.log(Level.INFO, "Country set to {0}", country.getName());
                    break;
                }
            }

        }

        //Now do the form validation
        String line1=address.getLine1().trim();
        if (line1.isEmpty() || line1.length()<2) {
            FacesContext.getCurrentInstance().addMessage("line1", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Line 1", "Invalid Line 1"));
        }else if (line1.length()>45){
            FacesContext.getCurrentInstance().addMessage("line1", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Line 1 exceeds limit (45 chars)", "Line 1 exceeds limit (45 chars)"));
        }
        String line2=address.getLine2().trim();
        if (line2.isEmpty() || line2.length()<2) {
            FacesContext.getCurrentInstance().addMessage("line2", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Line 2", "Invalid Line 2"));
        }else if (line2.length()>45){
            FacesContext.getCurrentInstance().addMessage("line2", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Line 2 exceeds limit (45 chars)", "Line 2 exceeds limit (45 chars)"));
        }
        
        
        if (address.getCountry().getCode().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("country",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Country", "Invalid Country."));
        }else{
            //Validate PostCode now..
            switch (countryCode) {
                case GDFConstants.IN_CODE: {
                    Pattern pCdIn = Pattern.compile(GDFConstants.IN_POSTCODE_REGEX);
                    Matcher mPCdIn = pCdIn.matcher(address.getPostcode());
                    if (!mPCdIn.find()) {
                        FacesContext.getCurrentInstance().addMessage("postcode", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid PostCode", "Not a valid Post Code of " + GDFConstants.IN_NAME));
                    }
                    break;

                }
                case GDFConstants.GB_CODE: {
                    Pattern pCdGB = Pattern.compile(GDFConstants.GB_POSTCODE_REGEX);
                    Matcher mPCdGB = pCdGB.matcher(address.getPostcode());
                    if (!mPCdGB.find()) {
                        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid PostCode", "Not a valid Post Code of " + GDFConstants.GB_NAME));
                    }
                    break;
                }
                case GDFConstants.US_CODE: {
                    Pattern pCdUS = Pattern.compile(GDFConstants.US_POSTCODE_REGEX);
                    Matcher mPCdUS = pCdUS.matcher(address.getPostcode());
                    if (!mPCdUS.find()) {
                        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Zip Code", "Not a valid Zip Code of " + GDFConstants.US_NAME));
                    }
                }
                break;
            }
        }
        
        
        String city=address.getCity().trim();
        if (city.isEmpty() || city.length() < 2) {
            FacesContext.getCurrentInstance().addMessage("city",new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid City", "Invalid City."));
        }else if (city.length()>45){
            FacesContext.getCurrentInstance().addMessage("city", new FacesMessage(FacesMessage.SEVERITY_ERROR, "City exceeds limit (45 chars)", "City exceeds limit (45 chars)"));
        }
        
        String state=address.getState().trim();
        if (state.isEmpty() || state.length() < 2) {
            FacesContext.getCurrentInstance().addMessage("state",new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid State", "Invalid State."));
        }else if (state.length()>45){
            FacesContext.getCurrentInstance().addMessage("state", new FacesMessage(FacesMessage.SEVERITY_ERROR, "State exceeds limit (45 chars)", "State exceeds limit (45 chars)"));
        }

        

        List<FacesMessage> msgs = FacesContext.getCurrentInstance().getMessageList();
        if (msgs != null && msgs.size() > 0) {
            toReturn = null;
        } else {
            toReturn = "";
        }
        return toReturn;
    }
    
    
    private void saveProfileImage() {
        try {
            InputStream input = profileFile.getInputStream();
            
            int fileSize = (int) profileFile.getSize();
            if (fileSize > (1000 * 1024)) {
                FacesContext.getCurrentInstance().addMessage("profileFile",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Profile Img exceeds 1MB", "Profile Img exceeds 1MB"));

            } else {//process with the processing of the image.
                //Step 1 Resize the image
                BufferedImage logoBufferedImage = ImageResizeUtil.resizeImage(input, 150);
                String fullFileName = profileFile.getSubmittedFileName();
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
                user.setProfileFile(fullFileName);
                user.setImage(jpgData);
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
        
    

    public void submitDetails() {
        user.setCreatedOn(LocalDateTime.now());
        user.setUpdatedOn(LocalDateTime.now());
        address.setCreatedOn(LocalDateTime.now());
        address.setUpdatedOn(LocalDateTime.now());
        user.setAddress(address);
        address.setUser(user);
        user = userBeanLocal.createUser(user);
        LOGGER.log(Level.INFO, "User persisted with ID: {0} and Address ID: {1}", new Object[]{user.getId(), user.getAddress().getId()});
        //User has been sussessfully persisted in the Database. Now the Image (byte[]) can be removed from the session as well. 
        HttpServletRequest request= (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=request.getSession();
        session.removeAttribute(GDFConstants.TEMP_IMAGE);
        LOGGER.info("Temporary Image of User removed from the session.");
    }
    
    public String getReturnValue() {
        submitDetails();
        return "/flowreturns/UserRegister-return?faces-redirect=true";
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UserAddress getAddress() {
        return address;
    }

    public void setAddress(UserAddress address) {
        this.address = address;
    }

    public boolean isAcceptedTC() {
        return acceptedTC;
    }

    public void setAcceptedTC(boolean acceptedTC) {
        this.acceptedTC = acceptedTC;
    }

    public Part getProfileFile() {
        return profileFile;
    }

    public void setProfileFile(Part profileFile) {
        this.profileFile = profileFile;
    }

    private String validatePersonalDetailsAmended() {
        String toReturn = null;
        //Validate Name first
        String fname=user.getFirstname().trim();
        if (fname.isEmpty() || fname.length()<2){
           FacesContext.getCurrentInstance().addMessage("firstname",new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid First Name", "Invalid First Name")); 
        }else if (fname.length()>45){
           FacesContext.getCurrentInstance().addMessage("firstname",new FacesMessage(FacesMessage.SEVERITY_ERROR, "First Name too long (max 45 chars)", "First Name too long (max 45 chars)"));  
        }
        
        
        String email = user.getEmail();
        LOGGER.log(Level.INFO, "Will check for: {0}", email);
        boolean exists = userBeanLocal.userExists(email);
        boolean accessExists=false;
        Access access= accessBeanLocal.getAccess(email);
        if (access!=null){//implies the email has been taken by some other Entity
           accessExists=true; 
        }
        if (exists || accessExists) {
            FacesContext.getCurrentInstance().addMessage("email",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Email registered.", "This email is already registered."));
        } else {//Email Regex validation
            Pattern p = Pattern.compile(GDFConstants.EMAIL_REGEX);
            Matcher m = p.matcher(email);
            if (!m.find()) {
                FacesContext.getCurrentInstance().addMessage("email", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Email invalid.", "Invalid Email."));
            } 
        }
        
        //Email is valid. Let's take this opportunity to store the Profile Image on Document Server
        if (profileFile != null) {//If a new file has been uploaded.
            String fileName = profileFile.getSubmittedFileName();
            if (fileName != null && !fileName.equals(user.getProfileFile())) {//If the Image has been altered, then an only then change the Image.This is likely to happen when the User attempts to Amend the personal details.
                saveProfileImage();
            }
        }
                
            

        //DOB Check
        String dobStr = user.getDobStr();
        if (dobStr.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("dob",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Date of Birth required", "Date of Birth required."));
        } else {
            try {//date should be in format dd/MM/yyyy
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate dob = LocalDate.parse(dobStr, formatter);
                LocalDate dateNow = LocalDate.now();
                Period p = Period.between(dob, dateNow);
                int years = p.getYears();
                if (years < 13) {
                    FacesContext.getCurrentInstance().addMessage("dob",
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Min permitted age is 13 years", "You must be alteast 13 year old to register"));
                }else{
                    user.setDob(dob);
                }
            } catch (DateTimeParseException ex1) {
                FacesContext.getCurrentInstance().addMessage("dob",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Format", "DOB needed in format dd/mm/yyyy."));
            }
        }

        //Check the Phone and Mobile
        String phone = user.getPhone();
        String mobile = user.getMobile();

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
        
        List<FacesMessage> msgs = FacesContext.getCurrentInstance().getMessageList();
        if (msgs != null && msgs.size() > 0) {
            toReturn = null;
        } else {
            toReturn = "";
        }
        return toReturn;
        
    }

    
    
}
