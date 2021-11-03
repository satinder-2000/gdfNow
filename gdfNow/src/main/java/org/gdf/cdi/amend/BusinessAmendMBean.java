/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.amend;

import org.gdf.ejb.AccessBeanLocal;
import org.gdf.ejb.BusinessBeanLocal;
import org.gdf.ejb.ReferenceDataBeanLocal;
import org.gdf.model.Access;
import org.gdf.model.Business;
import org.gdf.model.BusinessAddress;
import org.gdf.model.BusinessCategory;
import org.gdf.util.GDFConstants;
import org.gdf.util.ImageResizeUtil;
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
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
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
 *
 * This is Amend Business Details. The change of Email address is not allowed
 * and Country Change is not allowed. The Customer has to re-register should the
 * change ever.
 *
 */
@Named(value = "businessAmendMBean")
@ViewScoped
public class BusinessAmendMBean implements Serializable {

    static final Logger LOGGER = Logger.getLogger(BusinessAmendMBean.class.getName());

    private Business business;

    BusinessAddress businessAddress;

    @Inject
    BusinessBeanLocal businessBeanLocal;

    @Inject
    ReferenceDataBeanLocal referenceDataBeanLocal;

    @Inject
    AccessBeanLocal accessBeanLocal;

    List<String> businessTypes;
    List<String> businessSubTypes;
    private String type;
    private String subtype;

    private Part logoFile;

   @PostConstruct
    void init() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session = request.getSession();
        Access access = (Access) session.getAttribute(GDFConstants.ACCESS);
        Integer businessId = access.getEntityId(); //(Integer)session.getAttribute(GDFConstants.LOGGED_IN_GDF_ID);
        business = businessBeanLocal.findBusinessById(businessId);
        businessAddress = business.getBusinessAddress();
        type = business.getBusinessCategory().getType();
        subtype = business.getBusinessCategory().getSubtype();
        LOGGER.log(Level.INFO, "Business loaded :{0}", business.getEmail());
        
        businessTypes = new ArrayList<>();
        businessSubTypes = new ArrayList<>();
        List<String> bcTypes = referenceDataBeanLocal.getBusinessCategoryTypes();//SubTypes to be loaded via AJAX calls
        bcTypes.remove(type);//let's remove the already chosen one.
        businessTypes.addAll(bcTypes);
        LOGGER.log(Level.INFO, "BusinessMBean initialised");
    }

    public void ajaxTypeListener(AjaxBehaviorEvent event) {
        LOGGER.log(Level.INFO, "Business Type is {0}", type);
        businessTypes = new ArrayList<>();
        List<String> bcTypes = referenceDataBeanLocal.getBusinessCategoryTypes();//SubTypes to be loaded via AJAX calls
        bcTypes.remove(type);//let's remove the already chosen one.
        businessTypes.addAll(bcTypes);
        subtype = "Please Select";
        businessSubTypes = new ArrayList<>();
        businessSubTypes.addAll(referenceDataBeanLocal.getBusinessCategorySubTypes(type));
        LOGGER.log(Level.INFO, type + " {0} Business Types loaded in BusinessMBean ", businessSubTypes);

    }

    /**
     * The MBean being ViewScoped- This method is to be called via AJAX from the
     * front end - hence no return value
     */
    public void amendBusiness() {
        validateDetails();
        validateAddress();
        List<FacesMessage> msgs = FacesContext.getCurrentInstance().getMessageList();
        if (!msgs.isEmpty()) {
            LOGGER.log(Level.WARNING, "Validation of  Data and Address found {0} issues", msgs.size());
        } else {
            submitDetails();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Changes applied.", "Changes applied."));
        }
    }

    private void validateDetails() {

        String name = business.getName().trim();
        if (name.isEmpty() || name.length() < 2) {
            FacesContext.getCurrentInstance().addMessage("busName", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Business Name", "Invalid Business Name."));
        }

        String desc = business.getDescription().trim();
        if (desc.isEmpty() || desc.length() < 2) {
            FacesContext.getCurrentInstance().addMessage("desc", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Description", "Invalid Description"));
        }

        //Validate Website of the business (RegEx did not work in JSF Page)
        String website = business.getWebsite().trim();
        if (!website.isEmpty()) {
            Pattern pWebsite = Pattern.compile(GDFConstants.URL_REGEX);
            Matcher mW = pWebsite.matcher(website);
            boolean matchesU = mW.find();
            if (!matchesU) {
                FacesContext.getCurrentInstance().addMessage("website", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Website", "The website businessAddress is not valid"));
            }
        }

        //Finally, let's assign the BusinessCategory
        boolean bTyp = false;
        if (type.equals("Please Select")) {
            FacesContext.getCurrentInstance().addMessage("bTyp", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Business Type", "Invalid Business Type"));
        } else {
            bTyp = true;
        }
        boolean bSTyp = false;
        if (subtype.equals("Please Select")) {
            FacesContext.getCurrentInstance().addMessage("bSTyp", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Business Type", "Invalid Business Type"));
        } else {
            bSTyp = true;
        }
        if (bTyp & bSTyp) {
            List<BusinessCategory> bcList = referenceDataBeanLocal.getBusinessCategories();
            for (BusinessCategory bCt : bcList) {
                if (bCt.getType().equals(type) && bCt.getSubtype().equals(subtype)) {
                    business.setBusinessCategory(bCt);
                    break;
                }
            }
        }

        //Lastly, check if Profile Image needed to be changed
        if (logoFile != null) {
            String fileName = logoFile.getSubmittedFileName();
            if (!business.getLogoFile().equals(fileName)) {//If the names of files are different, it means profile file has been changed
                updateLogoFile();
                business.setLogoFile(fileName);
            }

        }

    }

    private void validateAddress() {

        if (businessAddress.getLine1().trim().length() < 2) {
            FacesContext.getCurrentInstance().addMessage("line1",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Line 1", "Invalid Line 1"));
        }
        if (businessAddress.getLine2().trim().length() < 2) {
            FacesContext.getCurrentInstance().addMessage("line2",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Line 2", "Invalid Line 2"));
        }
        if (businessAddress.getPostcode().trim().length() < 3) {
            FacesContext.getCurrentInstance().addMessage("postcode",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Postcode", "Invalid Postcode."));
        }
        if (businessAddress.getCity().trim().length() < 2) {
            FacesContext.getCurrentInstance().addMessage("city",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid City", "Invalid City."));
        }
        if (businessAddress.getState().trim().length() < 2) {
            FacesContext.getCurrentInstance().addMessage("state",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid State", "Invalid State."));
        }

        //Phones now
        //Phones now
        String phone1 = businessAddress.getPhone1();//This is mandatory
        Pattern pPhone = Pattern.compile(GDFConstants.PHONE_REGEX);
        if (phone1 == null || phone1.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("Phone1", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Phone1 required", "Phone 1 is required"));
        } else {
            Matcher ph1M = pPhone.matcher(phone1);
            boolean matchesPh1 = ph1M.find();
            if (!matchesPh1) {
                FacesContext.getCurrentInstance().addMessage("Phone1", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Phone 1", "Phone 1 is invalid"));
            }
        }
        String phone2 = businessAddress.getPhone2();//not mandatory
        if (phone2 != null && !phone2.trim().isEmpty()) {
            Matcher ph2M = pPhone.matcher(phone2);
            boolean matchesPh2 = ph2M.find();
            if (!matchesPh2) {
                FacesContext.getCurrentInstance().addMessage("Phone2", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Phone 2", "Phone 2 is invalid"));
            }
        }
        String phone3 = businessAddress.getPhone3();//not mandatory
        if (phone3 != null && !phone3.trim().isEmpty()) {
            Matcher ph3M = pPhone.matcher(phone3);
            boolean matchesPh3 = ph3M.find();
            if (!matchesPh3) {
                FacesContext.getCurrentInstance().addMessage("Phone3", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Phone 3", "Phone 3 is invalid"));
            }
        }
        //Validate PostCode now..
        switch (businessAddress.getCountry().getCode()) {
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

        List<FacesMessage> msgs = FacesContext.getCurrentInstance().getMessageList();
        if (!msgs.isEmpty()) {
            LOGGER.log(Level.WARNING, "Validation of  Address found {0} issues", msgs.size());
        }
    }

    private void submitDetails() {
        business.setUpdatedOn(LocalDateTime.now());
        business.setBusinessAddress(businessAddress);
        businessAddress.setBusiness(business);
        businessBeanLocal.amendBusiness(business);
        LOGGER.log(Level.INFO, "Business updated with ID: {0} and Address ID: {1}", new Object[]{business.getId(), business.getBusinessAddress().getId()});
        
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session = request.getSession();
        Access access = (Access) session.getAttribute(GDFConstants.ACCESS);
        if (!access.getProfileFile().equals(business.getLogoFile())) {
            access.setProfileFile(business.getLogoFile());
            access.setImage(business.getImage());
            session.invalidate();
            session = request.getSession(true);
            session.setAttribute(GDFConstants.ACCESS, access);
            LOGGER.log(Level.INFO, "Access reset in session with Logo Image of {0}", access.getProfileFile());
        }
    }
    
    private void updateLogoFile() {

        InputStream input = null;
        try {
            input = logoFile.getInputStream();
            int fileSize = (int) logoFile.getSize();
            if (fileSize > (1000 * 1024)) {//i.e size > 1MB
                FacesContext.getCurrentInstance().addMessage("ProfilePic",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Profile Img exceeds 1MB", "Profile Img exceeds 1MB"));
            } else {
                String fullFileName = logoFile.getSubmittedFileName();
                BufferedImage logoBufferedImage = ImageResizeUtil.resizeImage(input, 150);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(logoBufferedImage, "jpg", baos);
                baos.flush();
                byte[] imageInByte = baos.toByteArray();
                baos.close();
                business.setLogoFile(fullFileName);
                business.setImage(imageInByte);
                //We would need to display the Image in the ConfirmPage, which is next in the Flow.
                //There is no solution for that - only a workaround.
                //We put this image in the session for now and once the Business data has been persisted in the Database the image from the session will be removed.
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

    public Part getLogoFile() {
        return logoFile;
    }

    public void setLogoFile(Part logoFile) {
        this.logoFile = logoFile;
    }

    

}
