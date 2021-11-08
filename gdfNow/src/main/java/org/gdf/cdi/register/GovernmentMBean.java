/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.register;

import org.gdf.ejb.AccessBeanLocal;
import org.gdf.ejb.GovernmentBeanLocal;
import org.gdf.ejb.ReferenceDataBeanLocal;
import org.gdf.model.Access;
import org.gdf.model.Country;
import org.gdf.model.Government;
import org.gdf.model.GovernmentAddress;
import org.gdf.model.GovernmentOrg;
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
@Named(value = "governmentMBean")
@FlowScoped(value = "GovernmentRegister")
public class GovernmentMBean implements Serializable {

    static final Logger LOGGER = Logger.getLogger(GovernmentMBean.class.getName());

    @Inject
    ReferenceDataBeanLocal referenceDataBeanLocal;

    @Inject
    GovernmentBeanLocal governmentBeanLocal;

    @Inject
    AccessBeanLocal accessBeanLocal;

    private String clientCountryCode;

    Government government;

    GovernmentAddress governmentAddress;

    List<GovernmentOrg> governmentOrgs;

    List<String> ministries;

    String ministry;

    List<String> departments;

    String department;

    List<String> countries;

    String country;

    private boolean acceptedTC;

    private Part logoFile;

    //@PostConstruct
    public String init() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        clientCountryCode = request.getParameter("clientCountryCode");

        ministries = new ArrayList<>();
        departments = new ArrayList<>();
        ministries.add("Please Select");
        departments.add("Please Select");
        Country countryDb = referenceDataBeanLocal.getCountry(clientCountryCode);
        List<String> governmentMins = referenceDataBeanLocal.getGovernmentMinistries(clientCountryCode);
        ministries.addAll(governmentMins);
        List<String> depts = referenceDataBeanLocal.getGovernmentDepartments(clientCountryCode, governmentMins.get(0));
        departments.addAll(depts);

        LOGGER.log(Level.INFO, "Departments {0} loaded for Ministry {1}", new Object[]{depts, governmentMins.get(0)});

        countries = new ArrayList<>();
        countries.add(countryDb.getName());

        LOGGER.log(Level.INFO, "{0} Countries added to the List", countries.size());
        government = new Government();
        government.setGovernmentOrg(new GovernmentOrg());
        governmentAddress = new GovernmentAddress();

        governmentAddress.setCountry(countryDb);
        government.setGovernmentAddress(governmentAddress);
        governmentAddress.setGovernment(government);
        LOGGER.info("Government and GovernmentAddress initialized");

        return "GovernmentDetails?faces-redirect=true";

    }

    public String getClientCountryCode() {
        return clientCountryCode;
    }

    public String setClientCountryCode(String clientCountryCode) {
        this.clientCountryCode = clientCountryCode;
        init();
        return "GovernmentDetails?faces-redirect=true";
    }

    public void ajaxTypeListener(AjaxBehaviorEvent event) {
        LOGGER.log(Level.INFO, "Ministry is {0}", ministry);
        departments = new ArrayList<>();
        departments.add("Please Select");
        departments.addAll(referenceDataBeanLocal.getGovernmentDepartments(clientCountryCode, ministry));
        LOGGER.log(Level.INFO, ministry + " {0} Departments loaded in GovernmentMBean ", departments.size());
    }

    public String detailsCaptured() {
        //Validate the captured details
        validateDetails();
        //Finally send to the next Page
        List<FacesMessage> msgs = FacesContext.getCurrentInstance().getMessageList();
        if (msgs != null && msgs.size() > 0) {
            return null;
        } else {
            return "GovernmentConfirm?faces-redirect=true";
        }
    }

    private void validateDetails() {
        String name = government.getOfficeName().trim();
        if (name.isEmpty() || name.length() < 2) {
            FacesContext.getCurrentInstance().addMessage("governmentName", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Department Name required.", "Department Name is required"));
        } else if (name.length() > 150) {
            FacesContext.getCurrentInstance().addMessage("governmentName", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Name exceeds limit", "Name exceeds limit"));
        }
        //Validate Email now
        String email= government.getEmail().trim();//Email 1 is Mandatory
        if (email.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("email", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Email required", "Email is mandatory"));
        } else {
            boolean exists = governmentBeanLocal.governmentExists(email);
            //Also check if this email exists in Access table. Fixing Production issue on 05/04/2019 where same email can be used to register as a USER and as a BUSINESS.
            Access access = accessBeanLocal.getAccess(email);
            boolean accessExists = false;
            if (access != null) {
                accessExists = true;

            }
            if (exists || accessExists) {
                FacesContext.getCurrentInstance().addMessage("email",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Email registered.", "This email is already registered."));
            } else {
                String emailRegEx = GDFConstants.EMAIL_REGEX;
                Pattern pEmail = Pattern.compile(emailRegEx);
                Matcher mP = pEmail.matcher(email);
                boolean matches = mP.find();
                if (!matches) {
                    FacesContext.getCurrentInstance().addMessage("email", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Email invalid", "Email is not valid"));
                } else {
                    saveLogoImage();
                }
            }
        }
        
        //Website now
        String website = government.getWebsite();
        if (website.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("website", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Website required.", "Website Address is required"));
        } else {
            Pattern pWebsite = Pattern.compile(GDFConstants.URL_REGEX);
            Matcher mW = pWebsite.matcher(website);
            boolean matchesU = mW.find();
            if (!matchesU) {
                FacesContext.getCurrentInstance().addMessage("website", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Website invalid", "Website address not valid"));
            }
        }

        String contact = government.getContactName().trim();
        if (contact.isEmpty() || contact.length() < 2) {
            FacesContext.getCurrentInstance().addMessage("contact", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Contact required.", "Contact Name is required."));
        } else if (contact.length() > 100) {
            FacesContext.getCurrentInstance().addMessage("contact", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Contact exceeds 100 chars.", "Contact exceeds 100 chars."));
        }

        String description = government.getOfficeFunction().trim();
        if (description.isEmpty() || description.length() < 2) {
            FacesContext.getCurrentInstance().addMessage("desc", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Valid Description required", "Valid Description required"));
        } else if (description.length() > 250) {
            FacesContext.getCurrentInstance().addMessage("desc", new FacesMessage(FacesMessage.SEVERITY_ERROR, "At most 250 chars", "Description must be less than 250 chars long."));
        }

        //Address Validation
        String line1 = governmentAddress.getLine1().trim();
        if (line1.isEmpty() || line1.length() < 2) {//Line 1 is Mandatory
            FacesContext.getCurrentInstance().addMessage("line1", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Line 1", "Invalid Line 1"));
        } else if (line1.length() > 45) {
            FacesContext.getCurrentInstance().addMessage("line1", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Line 1 exceeds limit", "Line 1 exceeds limit"));
        }
        //Line 2
        String line2 = governmentAddress.getLine2().trim();
        if (line2.isEmpty() || line2.length() < 2) {//Line 2 is Mandatory
            FacesContext.getCurrentInstance().addMessage("line2", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Line 2", "Invalid Line 2"));
        } else if (line2.length() > 45) {
            FacesContext.getCurrentInstance().addMessage("line2", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Line 2 exceeds limit", "Line 2 exceeds limit"));
        }

        //Phones now
        String phone1 = governmentAddress.getPhone1().trim();//This is mandatory
        Pattern pPhone = Pattern.compile(GDFConstants.PHONE_REGEX);
        if (phone1.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("phone1", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Phone 1 required", "Phone 1 is required"));
        } else {
            Matcher ph1M = pPhone.matcher(phone1);
            boolean matchesPh1 = ph1M.find();
            if (!matchesPh1) {
                FacesContext.getCurrentInstance().addMessage("phone1", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Phone 1", "Invalid Phone 1"));
            }
        }
        String phone2 = governmentAddress.getPhone2().trim();//not mandatory
        if (phone2.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("phone2", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Phone 2 required", "Phone 2 is required"));
        } else {
            Matcher ph2M = pPhone.matcher(phone2);
            boolean matchesPh2 = ph2M.find();
            if (!matchesPh2) {
                FacesContext.getCurrentInstance().addMessage("phone2", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Phone 2", "Invalid Phone 2"));
            }
        }
        //Before validating Country specific PostCodes, ensure Country itself is valid
        if (country.equals("Please Select")) {//That's the value that appears on top of the list
            FacesContext.getCurrentInstance().addMessage("country", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Country", "Invalid Country Name."));
        } else {
            //Validate PostCode now..
            switch (country) {
                case GDFConstants.IN_NAME: {
                    Pattern pCdIn = Pattern.compile(GDFConstants.IN_POSTCODE_REGEX);
                    Matcher mPCdIn = pCdIn.matcher(governmentAddress.getPostcode());
                    if (!mPCdIn.find()) {
                        FacesContext.getCurrentInstance().addMessage("postcode", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Post Code", "Invalid Post Code of " + GDFConstants.IN_NAME));
                    }
                    break;

                }
                case GDFConstants.GB_NAME: {
                    Pattern pCdGB = Pattern.compile(GDFConstants.GB_POSTCODE_REGEX);
                    Matcher mPCdGB = pCdGB.matcher(governmentAddress.getPostcode());
                    if (!mPCdGB.find()) {
                        FacesContext.getCurrentInstance().addMessage("postcode", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Post Code", "Invalid Post Code Post Code of " + GDFConstants.GB_NAME));
                    }
                    break;
                }
                case GDFConstants.US_NAME: {
                    Pattern pCdUS = Pattern.compile(GDFConstants.US_POSTCODE_REGEX);
                    Matcher mPCdUS = pCdUS.matcher(governmentAddress.getPostcode());
                    if (!mPCdUS.find()) {
                        FacesContext.getCurrentInstance().addMessage("postcode", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Zip Code", "Invalid Zip Code of " + GDFConstants.US_NAME));
                    }
                }
                break;
            }
        }
        //Fill in the blanks..

        String city = governmentAddress.getCity().trim();
        if (city.isEmpty() || city.length() < 2) {
            FacesContext.getCurrentInstance().addMessage("city", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid City", "City is invalid"));
        } else if (city.length() > 45) {
            FacesContext.getCurrentInstance().addMessage("city", new FacesMessage(FacesMessage.SEVERITY_ERROR, "City exceeds char limit", "City exceeds char limit"));
        }

        String state = governmentAddress.getState().trim();
        if (state.isEmpty() || state.length() < 2) {
            FacesContext.getCurrentInstance().addMessage("state", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid State", "Invalid State"));
        } else if (state.length() > 45) {
            FacesContext.getCurrentInstance().addMessage("state", new FacesMessage(FacesMessage.SEVERITY_ERROR, "State exceeds char limit", "State exceeds char limit"));
        }

        //Before checking the Ministry / Department selected by the User- make sure no new values have been suggested
        String ministryTemp = government.getGovernmentOrg().getMinistryTemp();
        String departmentTemp = government.getGovernmentOrg().getDepartmentTemp();
        if ((!ministryTemp.isEmpty() & ministryTemp.length() > 2) & (!departmentTemp.isEmpty() & departmentTemp.length() > 2)) {//both ministry and department have been suugested by the user
            GovernmentOrg governmentOrg = new GovernmentOrg();
            governmentOrg.setMinistry(ministryTemp.toUpperCase());
            governmentOrg.setDepartment(departmentTemp.toUpperCase());
            governmentOrg.setConfirmed(false);
            governmentOrg.setCountryCode(governmentAddress.getCountry().getCode());
            governmentBeanLocal.createGovernmentOrg(governmentOrg);
            government.setGovernmentOrg(governmentOrg);
            government.setConfirmed(false);//Will be confirmed by the Administrator
        } else if ((!ministry.isEmpty() & ministry.length() > 2) & (!departmentTemp.isEmpty() & departmentTemp.length() > 2)) {//Only new Department has been suggested by the User for an existing Ministry
            GovernmentOrg governmentOrg = new GovernmentOrg();
            governmentOrg.setMinistry(ministry);
            governmentOrg.setDepartment(departmentTemp.toUpperCase());
            governmentOrg.setConfirmed(false);
            governmentOrg.setCountryCode(governmentAddress.getCountry().getCode());
            governmentBeanLocal.createGovernmentOrg(governmentOrg);
            government.setGovernmentOrg(governmentOrg);
            government.setConfirmed(false);//Will be confirmed by the Administrator
        } else {//User has picked from the specified categories
            GovernmentOrg govtOrg = referenceDataBeanLocal.getGovernmentOrg(government.getGovernmentAddress().getCountry().getCode(), ministry, department);
            government.setGovernmentOrg(govtOrg);
        }

        //T&C must be accepted
        if (!this.acceptedTC) {
            FacesContext.getCurrentInstance().addMessage("confChBx",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "T&C not accetped.", "T&C not accetped"));
        }

    }

    void saveLogoImage() {
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
                government.setLogoFile(fullFileName);
                government.setImage(jpgData);
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

    public String amendDetails() {
        return "GovernmentAmend?faces-redirect=true";
    }

    /**
     * This method is potentially called twice. First is user confirms the
     * entered data on the first instance and second, after the user has amended
     * the data. Hence the method calls the @private method validateDetails()
     * once again before submitting the data to the database.
     *
     *
     */
    public void submitDetails() {
        government.setCreatedOn(LocalDateTime.now());
        government.setUpdatedOn(LocalDateTime.now());
        government = governmentBeanLocal.createGovernment(government);
        LOGGER.log(Level.INFO, "Government. persisted with ID: {0} and Address ID: {1}", new Object[]{government.getId(), government.getGovernmentAddress().getId()});
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session = request.getSession();
        session.removeAttribute(GDFConstants.TEMP_IMAGE);

    }

    public String getReturnValue() {
        submitDetails();
        return "/flowreturns/GovernmentRegister-return?faces-redirect=true";
    }

    public Government getGovernment() {
        return government;
    }

    public void setGovernment(Government government) {
        this.government = government;
    }

    public GovernmentAddress getGovernmentAddress() {
        return governmentAddress;
    }

    public void setGovernmentAddress(GovernmentAddress governmentAddress) {
        this.governmentAddress = governmentAddress;
    }

    public List<String> getMinistries() {
        return ministries;
    }

    public void setMinistries(List<String> ministries) {
        this.ministries = ministries;
    }

    public String getMinistry() {
        return ministry;
    }

    public void setMinistry(String ministry) {
        this.ministry = ministry;
    }

    public List<String> getDepartments() {
        return departments;
    }

    public void setDepartments(List<String> departments) {
        this.departments = departments;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public List<String> getCountries() {
        return countries;
    }

    public void setCountries(List<String> countries) {
        this.countries = countries;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
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
