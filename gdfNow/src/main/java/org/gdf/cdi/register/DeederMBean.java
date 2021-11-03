/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.register;

import org.gdf.ejb.AccessBeanLocal;
import org.gdf.ejb.DeederBeanLocal;
import org.gdf.ejb.ReferenceDataBeanLocal;
import org.gdf.model.Access;
import org.gdf.model.Country;
import org.gdf.model.Deeder;
import org.gdf.model.DeederAddress;
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
@Named(value = "deederMBean")
@FlowScoped(value = "DeederRegister")
public class DeederMBean implements Serializable {

    final static Logger LOGGER = Logger.getLogger(DeederMBean.class.getName());

    @Inject
    private DeederBeanLocal deederBeanLocal;

    @Inject
    private ReferenceDataBeanLocal referenceDataBeanLocal;
    
    @Inject
    private AccessBeanLocal accessBeanLocal;

    private Deeder deeder;

    private DeederAddress deederAddress;
    private boolean acceptedTC;

    private Part profileFile;

    

    @PostConstruct
    public void init() {
        deeder = new Deeder();
        deederAddress = new DeederAddress();
        Country ctry = new Country();
        deederAddress.setCountry(ctry);
        deeder.setDeederAddress(deederAddress);
    }

    public String personalSave() {
        String toReturn = validatePersonalDetails();
        if (toReturn != null) {
            toReturn = "DeederAddress?faces-redirect=true";
        }
        return toReturn;
    }

    public String personalAmended() {
        String toReturn = validatePersonalAmended();
        if (toReturn != null) {
            toReturn = "DeederAddress?faces-redirect=true";
        }
        return toReturn;
    }

    public String amendPersonal() {
        return "AmendDeeder?faces-redirect=true";
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

        //Email
        String email = deeder.getEmail();
        LOGGER.log(Level.INFO, "Will check for: {0}", email);
        boolean exists = deederBeanLocal.deederExists(email);
        boolean accessExists=false;
        Access access= accessBeanLocal.getAccess(email);
        if (access!=null){//implies the email has been taken by some other Entity
           accessExists=true; 
        }
        if (exists || accessExists) {
            FacesContext.getCurrentInstance().addMessage("email",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Sorry, Email taken", "Sorry, Email taken."));
        } else {//Email Regex validation
            Pattern p = Pattern.compile(GDFConstants.EMAIL_REGEX);
            Matcher m = p.matcher(email);
            if (!m.find()) {
                FacesContext.getCurrentInstance().addMessage("email", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Email", "Invalid Email."));
            } else {//Email is valid. Let's take this opportunity to store the Profile Image on Document Server
                if (profileFile == null) {
                    FacesContext.getCurrentInstance().addMessage("profileFile",
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Profile Image is mandatory", "Profile Image is mandatory"));
                } else {
                    saveProfileImage();
                }

               
            }
        }

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
            try {//date should be in format dd/MM/yyyy
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
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
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "DOB needed in format DD/MM/YYY", "DOB needed in format DD/MM/YYY."));
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
                deeder.setProfileFile(fullFileName);
                deeder.setImage(jpgData);
                //We would need to display the Image in the ConfirmPage, which is next in the Flow.
                //There is no solution for that - only a workaround.
                //We put this image in the session for now and once the Deeder data has been persisted in the Database the image from the session will be removed.
                HttpServletRequest request= (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
                HttpSession session=request.getSession(true);
                String imgType=fullFileName.substring(fullFileName.indexOf('.')+1);
                ImageVO imageVO=new ImageVO(imgType,jpgData);
                session.setAttribute(GDFConstants.TEMP_IMAGE, imageVO);//This Image will be removed from Session once the data has been persisted.
            }
        } catch (IOException ex) {
            LOGGER.severe(ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }
    }

    public String deederAddressSave() {
        String toReturn = validateAddress();
        if (toReturn != null) {
            toReturn = "DeederConfirm?faces-redirect=true";
        }
        return toReturn;
    }

    private String validateAddress() {
        String toReturn = null;
        List<Country> countries = referenceDataBeanLocal.getCountries();
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

        }

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

    public String amendDeederAddress() {
        return "DeederAddress?faces-redirect=true";
    }

    public void submitDetails() {

        deeder.setDeederAddress(deederAddress);
        deederAddress.setDeeder(deeder);
        deeder.setCreatedOn(LocalDateTime.now());
        deeder.setUpdatedOn(LocalDateTime.now());
        deederAddress.setCreatedOn(LocalDateTime.now());
        deederAddress.setUpdatedOn(LocalDateTime.now());
        deeder = deederBeanLocal.createDeeder(deeder);
        LOGGER.log(Level.INFO, "Deeder persisted with ID: {0} and Address ID: {1}", new Object[]{deeder.getId(), deeder.getDeederAddress().getId()});
        //Deeder has been sussessfully persisted in the Database. Now the Image (byte[]) can be removed from the session as well. 
        HttpServletRequest request= (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=request.getSession();
        session.removeAttribute(GDFConstants.TEMP_IMAGE);
        LOGGER.info("Temporary Image of Deeder removed from the session.");
        
        
        

    }

    public String getReturnValue() {
        submitDetails();
        return "/flowreturns/DeederRegister-return?faces-redirect=true";
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

    private String validatePersonalAmended() {
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

        //Email
        String email = deeder.getEmail();
        LOGGER.log(Level.INFO, "Will check for: {0}", email);
        boolean exists = deederBeanLocal.deederExists(email);
        boolean accessExists=false;
        Access access= accessBeanLocal.getAccess(email);
        if (access!=null){//implies the email has been taken by some other Entity
           accessExists=true; 
        }
        if (exists || accessExists) {
            FacesContext.getCurrentInstance().addMessage("email",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Sorry, Email taken", "Sorry, Email taken."));
        } else {//Email Regex validation
            Pattern p = Pattern.compile(GDFConstants.EMAIL_REGEX);
            Matcher m = p.matcher(email);
            if (!m.find()) {
                FacesContext.getCurrentInstance().addMessage("email", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Email", "Invalid Email."));
            } else {//Email is valid. Let's take this opportunity to store the Profile Image if it has changed.
                if(profileFile==null && deeder.getImage()!=null){//In this case NO profile image has been uploaded or amended. Check an image exists in deeder.
                   LOGGER.info("Profile image not updated.");
                }
                else {//Profile file has been updated again.
                    String fileName = profileFile.getSubmittedFileName();
                    if (!fileName.equals(deeder.getProfileFile())) {//If the Image has been altered, then an only then change the Image. This can happen in the Amend mode.
                        saveProfileImage();
                    }
                } 
}
        }

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
            try {//date should be in format dd/MM/yyyy
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
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
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "DOB needed in format DD/MM/YYY", "DOB needed in format DD/MM/YYY."));
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
        
        List<FacesMessage> msgs = FacesContext.getCurrentInstance().getMessageList();
        if (msgs != null && msgs.size() > 0) {
            toReturn = null;
        } else {
            toReturn = "";
        }
        return toReturn;

        

    }

    

}
