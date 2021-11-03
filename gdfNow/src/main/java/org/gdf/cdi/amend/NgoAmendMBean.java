/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.amend;

import org.gdf.ejb.NgoBeanLocal;
import org.gdf.ejb.ReferenceDataBeanLocal;
import org.gdf.ejb.AccessBeanLocal;
import org.gdf.cdi.register.NgoMBean;
import org.gdf.model.Access;
import org.gdf.model.Country;
import org.gdf.model.Ngo;
import org.gdf.model.NgoAddress;
import org.gdf.model.NgoCategory;
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
 */
@Named(value = "ngoAmendMBean")
@ViewScoped
public class NgoAmendMBean implements Serializable {

    static final Logger LOGGER = Logger.getLogger(NgoMBean.class.getName());

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

    private Part logoFile;

    @PostConstruct
    void init() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session = request.getSession();
        Access access = (Access) session.getAttribute(GDFConstants.ACCESS);
        Integer ngoId = access.getEntityId();
        ngo = ngoBeanLocal.findNgoById(ngoId);
        ngoAddress = ngo.getNgoAddress();
        country = ngo.getNgoAddress().getCountry().getName();//Email and Country not allowed to change
        ngoType = ngo.getNgoCategory().getType();
        ngoSubtype = ngo.getNgoCategory().getSubtype();
        LOGGER.log(Level.INFO.INFO, "Ngo loaded :{0}", ngo.getEmail());

        ngoTypes = new ArrayList<>();
        ngoSubtypes = new ArrayList<>();
        List<String> ngoTypesL = referenceDataBeanLocal.getNgoCategoryTypes();
        ngoTypesL.remove(ngoType);//let's remove the already chosen one.
        LOGGER.log(Level.INFO, "Values in ngoTypesL{0}", ngoTypesL.size());
        ngoTypes.addAll(ngoTypesL);
        LOGGER.log(Level.INFO, "NgoAmendMBean initialised");
        
        /*List<String> ngoSubTypesL = referenceDataBeanLocal.getNgoCategorySubTypes(ngoType);
        ngoSubTypesL.remove(ngoSubtype);//let's remove the already chosen one.
        ngoSubtypes.addAll(ngoSubTypesL);
        LOGGER.log(Level.INFO, "Subtypes {0} loaded for Type {1}", new Object[]{ngoSubTypesL, ngoType});*/
        
    }

    public void ajaxTypeListener(AjaxBehaviorEvent event) {
        LOGGER.log(Level.INFO, "Ngo Type is {0}", ngoType);
        ngoTypes = new ArrayList<>();
        List<String> nTypes = referenceDataBeanLocal.getNgoCategoryTypes();//SubTypes to be loaded next
        nTypes.remove(ngoType);//let's remove the already chosen one.
        ngoTypes.addAll(nTypes);
        ngoSubtype = "Please Select";
        ngoSubtypes = new ArrayList<>();
        ngoSubtypes.addAll(referenceDataBeanLocal.getNgoCategorySubTypes(ngoType));
        LOGGER.log(Level.INFO, ngoType + " {0} Ngo Sub Types loaded in NgoMBean ", ngoSubtypes);

    }

    /**
     * The MBean being ViewScoped- This method is to be called via AJAX from the
     * front end - hence no return value
     */
    public void amendNgo() {
        validateDetailsAndAddress();
        List<FacesMessage> msgs = FacesContext.getCurrentInstance().getMessageList();
        if (!msgs.isEmpty()) {
            LOGGER.log(Level.WARNING, "Validation of  Data and Address found {0} issues", msgs.size());
        } else {
            submitDetails();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Changes applied.", "Changes applied."));
        }
    }

    private void validateDetailsAndAddress() {
        //Validate Website of the Ngo (RegEx did not work in JSF Page)
        String website = ngo.getWebsite().trim();
        if (!website.isEmpty()) {//Website for an NGO is not mandatory
            Pattern pWebsite = Pattern.compile(GDFConstants.URL_REGEX);
            Matcher mW = pWebsite.matcher(website);
            boolean matchesU = mW.find();
            if (!matchesU) {
                FacesContext.getCurrentInstance().addMessage("website", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Website", "The website address is not valid"));
            }
        }

        //Validate Description
        String desc = ngo.getDescription().trim();
        if (desc.isEmpty() || desc.length() < 2) {
            FacesContext.getCurrentInstance().addMessage("desc", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Description", "Invalid Description"));
        }

        //Phones now
        String phone1 = ngoAddress.getPhone1().trim();//This is mandatory
        Pattern pPhone = Pattern.compile(GDFConstants.PHONE_REGEX);

        Matcher ph1M = pPhone.matcher(phone1);
        boolean matchesPh1 = ph1M.find();
        if (!matchesPh1) {
            FacesContext.getCurrentInstance().addMessage("phone1", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Phone 1", "Phone 1 is invalid"));
        }

        String phone2 = ngoAddress.getPhone2().trim();//mandatory

        Matcher ph2M = pPhone.matcher(phone2);
        boolean matchesPh2 = ph2M.find();
        if (!matchesPh2) {
            FacesContext.getCurrentInstance().addMessage("phone2", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Phone 2", "Phone 2 is invalid"));
        }

        String phone3 = ngoAddress.getPhone3().trim();//not mandatory
        if (phone3 != null && !phone3.isEmpty()) {
            Matcher ph3M = pPhone.matcher(phone3);
            boolean matchesPh3 = ph3M.find();
            if (!matchesPh3) {
                FacesContext.getCurrentInstance().addMessage("phone3", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Phone 3", "Phone 3 is invalid"));
            }
        }

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

        //Fill in the blanks..
        String city = ngoAddress.getCity().trim();
        if (city.length() < 2 || city.length() > 250) {
            FacesContext.getCurrentInstance().addMessage("city", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid City", "City is invalid"));
        }

        String state = ngoAddress.getState().trim();
        if (state.length() < 2 || state.length() > 250) {
            FacesContext.getCurrentInstance().addMessage("state", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid State", "State is invalid"));
        }

        //Before checking the NGO Category / Sub Category selected by the User- make sure no new values have been suggested
        String typeTemp = ngo.getNgoCategory().getTypeTemp();
        String subtypeTemp = ngo.getNgoCategory().getSubtypeTemp();
        if (!typeTemp.isEmpty() & typeTemp.length() > 2) {//atleast a new "type" has been specified be the user. Now check the subtype as well.
            if (subtypeTemp.isEmpty() || subtypeTemp.length() < 2) {
                FacesContext.getCurrentInstance().addMessage("subTypeNew", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Sub Type (min 2 chars required)", "Invalid Sub Type (min 2 chars required)"));
            } else {
                NgoCategory ngoCategory = new NgoCategory();
                ngoCategory.setType(typeTemp.toUpperCase());
                ngoCategory.setSubtype(subtypeTemp.toUpperCase());
                ngoCategory.setConfirmed(false);
                ngoBeanLocal.createNgoCategory(ngoCategory);
                ngo.setNgoCategory(ngoCategory);
                ngo.setConfirmed(false);//Will be confirmed by the Administrator

            }

        } else {//User has picked from the specified categories
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

        //Finally check if Logo needs change..
        if (logoFile != null) {
            String fileName = logoFile.getSubmittedFileName();
            if (!ngo.getLogoFile().equals(fileName)) {//If the names of files are different, it means profile file has been changed
                updateLogoFile();
                ngo.setLogoFile(fileName);
            }

        }
    }

    private void submitDetails() {
        ngo.setUpdatedOn(LocalDateTime.now());
        ngo.setNgoAddress(ngoAddress);
        ngoAddress.setNgo(ngo);
        ngoBeanLocal.amendNgo(ngo);
        LOGGER.log(Level.INFO, "Ngo updated with ID: {0} and Address ID: {1}", new Object[]{ngo.getId(), ngo.getNgoAddress().getId()});

        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session = request.getSession();
        Access access = (Access) session.getAttribute(GDFConstants.ACCESS);
        if (!access.getProfileFile().equals(ngo.getLogoFile())) {
            access.setProfileFile(ngo.getLogoFile());
            access.setImage(ngo.getImage());
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
                ngo.setLogoFile(fullFileName);
                ngo.setImage(imageInByte);
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

    public List<NgoCategory> getNgoCategories() {
        return ngoCategories;
    }

    public void setNgoCategories(List<NgoCategory> ngoCategories) {
        this.ngoCategories = ngoCategories;
    }

    public List<Country> getCountriesL() {
        return countriesL;
    }

    public void setCountriesL(List<Country> countriesL) {
        this.countriesL = countriesL;
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

    public Part getLogoFile() {
        return logoFile;
    }

    public void setLogoFile(Part logoFile) {
        this.logoFile = logoFile;
    }

}
