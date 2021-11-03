/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.amend;

import org.gdf.ejb.AccessBeanLocal;
import org.gdf.ejb.DeederBeanLocal;
import org.gdf.ejb.ReferenceDataBeanLocal;
import org.gdf.model.Access;
import org.gdf.model.Deeder;
import org.gdf.model.DeederAddress;
import org.gdf.util.GDFConstants;
import org.gdf.util.ImageResizeUtil;
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
 * @author root
 * 
 */
@Named(value = "deederAmendMBean")
@ViewScoped
public class DeederAmendMBean implements Serializable {

    static final Logger LOGGER = Logger.getLogger(DeederAmendMBean.class.getName());

    Deeder deeder;
    DeederAddress deederAddress;

    @Inject
    DeederBeanLocal deederBeanLocal;

    @Inject
    ReferenceDataBeanLocal referenceDataBeanLocal;

    @Inject
    AccessBeanLocal accessBeanLocal;

    private Part profileFilePart;

    @PostConstruct
    void init() {
        ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest request = (HttpServletRequest) extContext.getRequest();
        HttpSession session = request.getSession();
        Access access = (Access) session.getAttribute(GDFConstants.ACCESS);
        Integer deederId = access.getEntityId();
        deeder = deederBeanLocal.getDeeder(deederId);
        deederAddress = deeder.getDeederAddress();
        /*int dOM = deeder.getDob().getDayOfMonth();
        int mOY = deeder.getDob().getMonthValue();
        int yr = deeder.getDob().getYear();
        String dobStr = String.valueOf(String.valueOf("" + yr + "-") + Integer.toString(mOY) + "-") + Integer.toString(dOM);
        deeder.setDobStr(dobStr);*/
        String dobStr=deeder.getDob().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        deeder.setDobStr(dobStr);
        LOGGER.log(Level.INFO, "Deeder loaded :{0}", deeder.getEmail());
    }

    public String amendDeeder() {
        String toReturn = null;
        validatePersonalDetails();
        validateAddress();
        List<FacesMessage> msgs = FacesContext.getCurrentInstance().getMessageList();
        if (!msgs.isEmpty()) {
            LOGGER.log(Level.WARNING, "Validation of User Data and Address found {0} issues", msgs.size());
            toReturn = null;//stays null. no harm.
        } else {
            updateDeederData();
            toReturn = "/amend/deeder/AmendAcknowledge?faces-redirect.xhtml";
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

        //Email not changing.
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

        //Lastly, check if Profile Image needed to be changed
        if (profileFilePart != null) {
            String fileName = profileFilePart.getSubmittedFileName();
            if (!deeder.getProfileFile().equals(fileName)) {//If the names of files are different, it means profile file has been changed
                updateProfileFile();
                deeder.setProfileFile(fileName);
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
        //Country Not changing. Removed Code to validate country on 20/03/
        String countryCode = deederAddress.getCountry().getCode();
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

    /*public void saveProfileImage() {
        String fileName = null;
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
                deeder.setProfileFile(fullFileName);
                deeder.setImage(imageInByte);
            }

        } catch (IOException ex) {
            LOGGER.severe(ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }

    }*/

    private void updateDeederData() {
        deeder.setDeederAddress(deederAddress);
        deederAddress.setDeeder(deeder);
        deeder.setUpdatedOn(LocalDateTime.now());
        deederAddress.setUpdatedOn(LocalDateTime.now());
        deeder = deederBeanLocal.amendDeeder(deeder);
        LOGGER.log(Level.INFO, "Deeder updated with ID: {0} and Address ID: {1}", new Object[]{deeder.getId(), deeder.getDeederAddress().getId()});
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
                deeder.setProfileFile(fullFileName);
                deeder.setImage(imageInByte);
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
