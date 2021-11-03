/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.amend;

import org.gdf.ejb.AccessBeanLocal;
import org.gdf.ejb.ReferenceDataBeanLocal;
import org.gdf.ejb.UserBeanLocal;
import org.gdf.model.Access;
import org.gdf.model.UserAddress;
import org.gdf.model.Country;
import org.gdf.model.User;
import org.gdf.util.GDFConstants;
import org.gdf.util.ImageResizeUtil;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
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
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
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
@Named(value = "userAmendMBean")
@ViewScoped
public class UserAmendMBean implements Serializable {
    
    static final Logger LOGGER=Logger.getLogger(UserAmendMBean.class.getName());
    
    private User user;
    
    @Inject
    UserBeanLocal userBeanLocal;
    
    @Inject
    ReferenceDataBeanLocal referenceDataBeanLocal;
    
    @Inject
    AccessBeanLocal accessBeanLocal;
    
    private Part profileFilePart;
    
    @PostConstruct
    void init(){
        HttpServletRequest request= (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=request.getSession();
        Access access=(Access) session.getAttribute(GDFConstants.ACCESS);
        Integer userId=access.getEntityId();//(Integer)session.getAttribute(GDFConstants.LOGGED_IN_GDF_ID);
        user=userBeanLocal.getUser(userId);
        String dobStr=user.getDob().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        user.setDobStr(dobStr);
        /*int dOM=user.getDob().getDayOfMonth();
        int mOY=user.getDob().getMonthValue();
        int yr=user.getDob().getYear();
        String dobStr=String.valueOf(String.valueOf("" + yr + "-") + Integer.toString(mOY) + "-") + Integer.toString(dOM);
        user.setDobStr(dobStr);*/
        LOGGER.log(Level.INFO, "User loaded :{0}", user.getEmail());
    }
    
    /**
     * The MBean being ViewScoped- This method is to be called via AJAX from the front end - hence no return value
     */
    public String amendUser(){
        validatePersonalDetails();
        validateAddress();
        List<FacesMessage> msgs = FacesContext.getCurrentInstance().getMessageList();
        if (!msgs.isEmpty()) {
            LOGGER.log(Level.WARNING, "Validation of User Data and Address found {0} issues", msgs.size());
            return null;
        }else{
            updateUserData();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Changes applied.", "Changes applied."));
            return "/amend/user/AmendAcknowledge?faces-redirect=true";
        }
        
    }
    
    private void validatePersonalDetails() {
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

        if (phone.trim().isEmpty() && mobile.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("phone",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Phone or Mobile required", "Phone or Mobile required."));
        } else {
            Pattern p = Pattern.compile(GDFConstants.PHONE_REGEX);
            Matcher mP = p.matcher(phone);
            if (!mP.find()) {
                FacesContext.getCurrentInstance().addMessage("phone",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Phone", "Invalid Phone number."));
            } else if (mobile.length() > 1) {
                Matcher mM = p.matcher(mobile);
                if (!mM.find()) {
                    FacesContext.getCurrentInstance().addMessage("mobile",
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Mobile", "Invalid mobile number."));
                }
            }
        }
        
        //Lastly, check if Profile Image needed to be changed
        
        if (profileFilePart!=null){
            String fileName=profileFilePart.getSubmittedFileName();
            if (!user.getProfileFile().equals(fileName)){//If the names of files are different, it means profile file has been changed
                updateProfileFile();
                user.setProfileFile(fileName);
            }
            
        }
        
                

        List<FacesMessage> msgs = FacesContext.getCurrentInstance().getMessageList();
        if (!msgs.isEmpty()) {
            LOGGER.log(Level.WARNING, "Validation of User Data found {0} issues", msgs.size());
        } 

    }

    private void validateAddress() {
        UserAddress address=user.getAddress();
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
        if (address.getLine1().trim().equals("")) {
            FacesContext.getCurrentInstance().addMessage("line1",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Line 1 is mandatory", "Line 1 is mandatory!"));
        }
        if (address.getPostcode().trim().length() < 3) {
            FacesContext.getCurrentInstance().addMessage("postcode",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Postcode", "Invalid Postcode."));
        }
        if (address.getCity().trim().length() < 2) {
            FacesContext.getCurrentInstance().addMessage("city",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid City", "Invalid City."));
        }
        if (address.getState().trim().length() < 2) {
            FacesContext.getCurrentInstance().addMessage("state",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid State", "Invalid State."));
        }

        if (address.getCountry().getCode().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("country",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Country", "Invalid Country."));
        }

        List<FacesMessage> msgs = FacesContext.getCurrentInstance().getMessageList();
        if (!msgs.isEmpty()) {
            LOGGER.log(Level.WARNING, "Validation of User Address found {0} issues", msgs.size());
        } 
    }
    
    public void saveProfileImage() {
        InputStream input = null;
        try {
            input = profileFilePart.getInputStream();
            int fileSize = (int) profileFilePart.getSize();
            if (fileSize > (1000 * 1024)) {//i.e size > 1MB
                FacesContext.getCurrentInstance().addMessage("ProfilePic",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Profile Img exceeds 1MB", "Profile Img exceeds 1MB"));
            } else {
                String fullFileName = profileFilePart.getSubmittedFileName();
                BufferedImage logoBufferedImage = ImageResizeUtil.resizeImage(input, 150);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(logoBufferedImage, "jpg", baos);
                baos.flush();
                byte[] imageInByte = baos.toByteArray();
                baos.close();
                user.setProfileFile(fullFileName);
                user.setImage(imageInByte);
            }

        } catch (IOException ex) {
            LOGGER.severe(ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }

    }
        
   
        
   

    private void updateUserData() {
        user.setUpdatedOn(LocalDateTime.now());
        user.getAddress().setUpdatedOn(LocalDateTime.now());
        user.getAddress().setUser(user);
        userBeanLocal.amendUser(user);
        LOGGER.log(Level.INFO, "Deeder updated with ID: {0} and Address ID: {1}", new Object[]{user.getId(), user.getAddress().getId()});
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    private void updateProfileFile() {
        InputStream input = null;
        try {
            input = profileFilePart.getInputStream();
            int fileSize = (int) profileFilePart.getSize();
            if (fileSize > (1000 * 1024)) {//i.e size > 1MB
                FacesContext.getCurrentInstance().addMessage("ProfilePic",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Profile Img exceeds 1MB", "Profile Img exceeds 1MB"));
            } else {
                String fullFileName = profileFilePart.getSubmittedFileName();
                BufferedImage logoBufferedImage = ImageResizeUtil.resizeImage(input, 150);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(logoBufferedImage, "jpg", baos);
                baos.flush();
                byte[] imageInByte = baos.toByteArray();
                baos.close();
                user.setProfileFile(fullFileName);
                user.setImage(imageInByte);
                //We would need to display the Image in the ConfirmPage, which is next in the Flow.
                //There is no solution for that - only a workaround.
                //We put this image in the session for now and once the Deeder data has been persisted in the Database the image from the session will be removed.
                ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
                HttpServletRequest request = (HttpServletRequest) extContext.getRequest();
                HttpSession session = request.getSession();
                Access access = (Access) session.getAttribute(GDFConstants.ACCESS);
                access.setProfileFile(fullFileName);
                access.setImage(imageInByte);
            }
        } catch (IOException ex) {
            LOGGER.severe(ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }
    }

    public Part getProfileFilePart() {
        return profileFilePart;
    }

    public void setProfileFilePart(Part profileFilePart) {
        this.profileFilePart = profileFilePart;
    }
    
    
    
    
}
