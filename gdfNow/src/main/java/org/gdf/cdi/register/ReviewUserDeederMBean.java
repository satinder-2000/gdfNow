/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.register;

import org.gdf.ejb.AccessBeanLocal;
import org.gdf.ejb.DeederBeanLocal;
import org.gdf.ejb.ReferenceDataBeanLocal;
import org.gdf.ejb.UserDeederBeanLocal;
import org.gdf.model.Access;
import org.gdf.model.AccessType;
import org.gdf.model.Deeder;
import org.gdf.model.Country;
import org.gdf.model.DeederAddress;
import org.gdf.model.User;
import org.gdf.util.ConvertPngToJpg;
import org.gdf.util.GDFConstants;
import org.gdf.util.ImageResizeUtil;
import org.gdf.util.ImageVO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
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
 * @author root
 */
@Named(value = "reviewUserDeederMBean")
@FlowScoped(value = "ReviewUserDeeder")
public class ReviewUserDeederMBean implements Serializable {
    
    static final Logger LOGGER=Logger.getLogger(ReviewUserDeederMBean.class.getName());
    
    @Inject
    UserDeederBeanLocal userDeederBeanLocal;
    
    @Inject
    ReferenceDataBeanLocal referenceDataBeanLocal;
    
    @Inject
    AccessBeanLocal accessBeanLocal;
    
    
    Deeder deeder;
    
    DeederAddress deederAddress;
    
    private User user;
    
    private Part profileFilePart;
    
    private Access access;
    
    private boolean acceptedTC;
    
    
    @PostConstruct
    public void init(){
        HttpServletRequest request= (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=request.getSession();
        String udIdStr = (String)session.getAttribute("udId");
        //The request should give us the UD's ID
        int udId=Integer.parseInt(udIdStr);
        deeder= userDeederBeanLocal.getUserDeeder(udId);
        deederAddress=deeder.getDeederAddress();
        //Iterator<User> usersIter= ud.getUsers().iterator();
        //user = usersIter.next();
        String profileFile=deeder.getProfileFile();
        String imgType=profileFile.substring(profileFile.indexOf('.')+1);
        ImageVO imageVO=new ImageVO(imgType, deeder.getImage());
        session.setAttribute(GDFConstants.TEMP_IMAGE, imageVO);//Don't forget to remove it from the session after the UD details have been updated in the DB.
        LOGGER.info("User Deeder Loaded "+deeder.getEmail());
    }
    
    public String validateDetails(){
        String toReturn = null;
        validatePersonalDetails();
        validateAddress();
        List<FacesMessage> msgs = FacesContext.getCurrentInstance().getMessageList();
        if (!msgs.isEmpty()) {
            LOGGER.log(Level.WARNING, "Validation of User Data and Address found {0} issues", msgs.size());
            toReturn = null;//stays null. no harm.
        } else {
            toReturn = "ReviewConfirm?faces-redirect=true";
        }

        return toReturn;
    }
    
    private String validatePersonalDetails() {
        String toReturn = null;

        //First Name
        String fname = deeder.getFirstname().trim();
        if (fname.isEmpty() || fname.length() < 2) {
            FacesContext.getCurrentInstance().addMessage("firstname", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid First Name", "Invalid First Name"));
        } else if (fname.length() > 45) {
            FacesContext.getCurrentInstance().addMessage("firstname", new FacesMessage(FacesMessage.SEVERITY_ERROR, "First Name exceeds 45 chars.", "First Name exceeds 45 chars."));
        }

        //Last Name
        String lname = deeder.getLastname().trim();
        if (lname.isEmpty() || lname.length() < 2) {
            FacesContext.getCurrentInstance().addMessage("lastname", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Last Name", "Invalid Last Name"));
        } else if (lname.length() > 45) {
            FacesContext.getCurrentInstance().addMessage("lastname", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Last Name exceeds 45 chars.", "Last Name exceeds 45 chars."));
        }

        //Email could be changing.
        String email = deeder.getEmail();
        LOGGER.log(Level.INFO, "Will check for: {0}", email);
        //Email Regex validation
            Pattern p1 = Pattern.compile(GDFConstants.EMAIL_REGEX);
            Matcher m = p1.matcher(email);
            if (!m.find()) {
                FacesContext.getCurrentInstance().addMessage("email", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Email", "Invalid Email."));
            } 
        
        /*boolean exists = userDeederBeanLocal.UserDeederExists(email);
        if (exists) {
            FacesContext.getCurrentInstance().addMessage("email",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Sorry, Email taken", "Sorry, Email taken."));
        } else {//Email Regex validation
            Pattern p = Pattern.compile(GDFConstants.EMAIL_REGEX);
            Matcher m = p.matcher(email);
            if (!m.find()) {
                FacesContext.getCurrentInstance().addMessage("email", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Email", "Invalid Email."));
            } 
        }*/
        //About Deeder
        String about = deeder.getAbout().trim();
        if (about.isEmpty() || about.length() < 2) {
            FacesContext.getCurrentInstance().addMessage("about",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "About is required", "About is required."));
        } else if (about.length() > 350) {
            FacesContext.getCurrentInstance().addMessage("about",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "About too long", "About too long"));
        }

        //DOB Check
        String dobStr = deeder.getDobStr();
        if (dobStr.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("dob",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Date of Birth required", "Date of Birth required."));
        } else {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate dob = LocalDate.parse(dobStr, formatter);
                LocalDate dateNow = LocalDate.now();
                Period p = Period.between(dob, dateNow);
                int years = p.getYears();
                if (years < 13) {
                    FacesContext.getCurrentInstance().addMessage("dob",
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Min permitted age is 13 years", "You must be alteast 13 year old to register"));
                } else {
                    deeder.setDob(dob);
                }
            } catch (DateTimeParseException ex1) {
                FacesContext.getCurrentInstance().addMessage("dob",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Format", "DOB needed in format dd/mm/yyyy."));
            }
        }

        //Check the Phone and Mobile
        String phone = deeder.getPhone();
        String mobile = deeder.getMobile();

        if (phone.trim().isEmpty() && mobile.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("phone",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Phone or Mobile required", "Phone or Mobile required."));
        } else {
            Pattern p = Pattern.compile(GDFConstants.PHONE_REGEX);
            if (phone.length() > 0) {
                Matcher mP = p.matcher(phone);
                if (!mP.find()) {
                    FacesContext.getCurrentInstance().addMessage("phone",
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Phone", "Invalid Phone number."));
                }
            }
            if (mobile.length() > 0) {
                Matcher mM = p.matcher(mobile);
                if (!mM.find()) {
                    FacesContext.getCurrentInstance().addMessage("mobile",
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Mobile", "Invalid mobile number."));
                }
            }
        }

        //Lastly, check if Profile Image needed to be changed
        if (profileFilePart==null){
            FacesContext.getCurrentInstance().addMessage("profilePic",
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "No profile image provided", "No profile image provided."));
        }else{
            
        }
        if (profileFilePart != null) {
            String fileName = profileFilePart.getSubmittedFileName();
            if (!deeder.getProfileFile().equals(fileName)) {//If the names of files are different, it means profile file has been changed
                updateProfileFile();//deeder's file name and image byte[] are set in the method call. 
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

    private String validateAddress() {
        String toReturn = null;
        /*List<Country> countries = referenceDataBeanLocal.getCountries();
        String countryCode = deederAddress.getCountry().getCode();
        if (countryCode.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("country",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Country is required", "Country is required."));
        } else {
            for (Country country : countries) {
                if (country.getCode().equalsIgnoreCase(countryCode)) {
                    deederAddress.setCountry(country);
                    LOGGER.log(Level.INFO, "Country set to {0}", country.getName());
                    break;
                }
            }

        }*/

        //Now do the form validation
        String line1 = deederAddress.getLine1().trim();
        if (line1.isEmpty() || line1.length() < 2) {
            FacesContext.getCurrentInstance().addMessage("line1", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Line 1", "Invalid Line 1"));
        } else if (line1.length() > 45) {
            FacesContext.getCurrentInstance().addMessage("line1", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Line 1 exceeds 45 chars", "Line 1 exceeds 45 chars"));
        }
        String line2 = deederAddress.getLine2().trim();
        if (line2.isEmpty() || line2.length() < 2) {
            FacesContext.getCurrentInstance().addMessage("line2", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Line 2", "Invalid Line 2"));
        } else if (line2.length() > 45) {
            FacesContext.getCurrentInstance().addMessage("line2", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Line 2 exceeds 45 chars", "Line 2 exceeds 45 chars"));
        }

        if (deederAddress.getCountry().getCode().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("country", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Country", "Invalid Country."));
        } else {
            String countryCode = deederAddress.getCountry().getCode();//Not cahnging the country but within the Country the Post Code might change. S we DO need Country knowledge to validate the post codes.
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

        //City
        String city = deederAddress.getCity().trim();
        if (city.isEmpty() || city.length() < 2) {
            FacesContext.getCurrentInstance().addMessage("city", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid City", "Invalid City."));
        } else if (city.length() > 45) {
            FacesContext.getCurrentInstance().addMessage("city", new FacesMessage(FacesMessage.SEVERITY_ERROR, "City exceeds 45 chars", "City exceeds 45 chars"));
        }

        //State
        String state = deederAddress.getState().trim();
        if (state.isEmpty() || state.length() < 2) {
            FacesContext.getCurrentInstance().addMessage("state", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid State", "Invalid State."));
        } else if (state.length() > 45) {
            FacesContext.getCurrentInstance().addMessage("state", new FacesMessage(FacesMessage.SEVERITY_ERROR, "State exceeds 45 chars", "State exceeds 45 chars"));
        }

        List<FacesMessage> msgs = FacesContext.getCurrentInstance().getMessageList();
        if (msgs != null && msgs.size() > 0) {
            toReturn = null;
        } else {
            toReturn = "";
        }
        return toReturn;

    }
    
    public String amendDetails(){
        return "ReviewUserDeeder?faces-redirect=true";
    }

    private void updateProfileFile() {
        InputStream input = null;
        try {
            input = profileFilePart.getInputStream();
            int fileSize = (int) profileFilePart.getSize();
            if (fileSize > (1000 * 1024)) {//i.e size > 1MB
                FacesContext.getCurrentInstance().addMessage("ProfilePic",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Profile Img exceeds 1MB", "Profile Img exceeds 1MB"));
            } else {//process with the processing of the image.
                //Step 1 Resize the image
                BufferedImage logoBufferedImage = ImageResizeUtil.resizeImage(input, 150);
                String fullFileName = profileFilePart.getSubmittedFileName();
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
                deeder.setProfileFile(fullFileName);
                deeder.setImage(jpgData);
                //We would need to display the Image in the ConfirmPage, which is next in the Flow.
                //There is no solution for that - only a workaround.
                //We put this image in the session for now and once the Deeder data has been persisted in the Database the image from the session will be removed.
                ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
                HttpServletRequest request = (HttpServletRequest) extContext.getRequest();
                HttpSession session = request.getSession();
                String imgType=fullFileName.substring(fullFileName.indexOf('.'));
                ImageVO imageVO=new ImageVO(imgType, jpgData);
                session.setAttribute(GDFConstants.TEMP_IMAGE, imageVO);
            }
        } catch (IOException ex) {
            LOGGER.severe(ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }
    }

    
    public String submitDetails() {

        deeder.setDeederAddress(deederAddress);
        deederAddress.setDeeder(deeder);
        deeder.setUpdatedOn(LocalDateTime.now());
        deederAddress.setUpdatedOn(LocalDateTime.now());
        deeder = userDeederBeanLocal.updateUserDeederReview(deeder);
        LOGGER.log(Level.INFO, "Deeder persisted with ID: {0} and Address ID: {1}", new Object[]{deeder.getId(), deeder.getDeederAddress().getId()});
        //Initialise Access now because we are going to move to CreateAccess
        access=new Access();
        access.setAccessType(AccessType.DEEDER);
        access.setAttempts(0);
        access.setCountryCode(deederAddress.getCountry().getCode());
        access.setCreatedOn(LocalDateTime.now());
        access.setEmail(deeder.getEmail());
        access.setEntityId(deeder.getId());
        access.setImage(deeder.getImage());
        access.setName(deeder.getFirstname()+" "+deeder.getLastname());
        access.setProfileFile(deeder.getProfileFile());
        access.setUpdatedOn(LocalDateTime.now());//TODO: This need to be updated just before persisting as well.
        
        //Deeder has been sussessfully persisted in the Database. Now the Image (byte[]) can be removed from the session as well. 
        HttpServletRequest request= (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=request.getSession();
        session.removeAttribute(GDFConstants.TEMP_IMAGE);
        LOGGER.info("Temporary Image of Deeder removed from the session.");
        return "CreateAccess?faces-redirect=true";
        
    }
    
    public String validateAccess(){
        String password=access.getPassword();
        String passwordConfirm=access.getPasswordConfirm();
        if (password.trim().isEmpty()){
            FacesContext.getCurrentInstance().addMessage("pwd1",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,"No Password","No Password"));
        }else{
            //First, RegEx the password
            Pattern pCdIn=Pattern.compile(GDFConstants.PW_REGEX);
            Matcher mPCdIn=pCdIn.matcher(password);
            if (!mPCdIn.find()){
                FacesContext.getCurrentInstance().addMessage("pwd1",new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid Password","Invalid Password."));  
            }else{//compare the password now
                if(!password.equals(passwordConfirm)){
                    FacesContext.getCurrentInstance().addMessage("pwd2",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,"Passwords do not match","Passwords do not match!"));
                }
                
            }
        }
        List<FacesMessage> msgs= FacesContext.getCurrentInstance().getMessageList();
        if (msgs!=null && msgs.size()>0){
            return null;
        }else{
            submitAccess();
            return "AcknowledgeAccess?faces-redirect=true";
        }
    }
    
    private void submitAccess(){
        Access accessDb=accessBeanLocal.createAccess(this.getAccess());
        LOGGER.log(Level.INFO, "Access to {0}provided.", accessDb.getAccessType().toString());
    }
    
    
    public String getReturnValue() {
        return "/register/ReviewUserDeeder-return?faces-redirect=true";
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

    public Part getProfileFilePart() {
        return profileFilePart;
    }

    public void setProfileFilePart(Part profileFilePart) {
        this.profileFilePart = profileFilePart;
    }

    public Access getAccess() {
        return access;
    }

    public void setAccess(Access access) {
        this.access = access;
    }

    public boolean isAcceptedTC() {
        return acceptedTC;
    }

    public void setAcceptedTC(boolean acceptedTC) {
        this.acceptedTC = acceptedTC;
    }

    
    
    
    
    
}
