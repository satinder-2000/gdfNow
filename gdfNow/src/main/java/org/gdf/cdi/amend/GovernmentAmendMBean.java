/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.amend;

import org.gdf.ejb.AccessBeanLocal;
import org.gdf.ejb.ReferenceDataBeanLocal;
import org.gdf.ejb.GovernmentBeanLocal;
import org.gdf.model.Access;
import org.gdf.model.Government;
import org.gdf.model.GovernmentAddress;
import org.gdf.model.GovernmentOrg;
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
@Named(value = "governmentAmendMBean")
@ViewScoped
public class GovernmentAmendMBean implements Serializable{
    
    static final Logger LOGGER=Logger.getLogger(GovernmentAmendMBean.class.getName());
    
    @Inject
    ReferenceDataBeanLocal referenceDataBeanLocal;
    
    @Inject
    GovernmentBeanLocal governmentBeanLocal;
    
    @Inject
    AccessBeanLocal accessBeanLocal;
    
    Government government;
    
    GovernmentAddress governmentAddress;
    
    List<GovernmentOrg> governmentOrgs;
    
    List<String> ministries;
    
    String ministry;
    
    List<String> departments;
    
    String department;
    
    private Part logoFile;
    
    private String clientCountryCode;
    
    private String country;
    
    @PostConstruct
    public void init(){
        HttpServletRequest request= (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=request.getSession();
        Access access=(Access) session.getAttribute(GDFConstants.ACCESS);
        Integer governmentId= access.getEntityId();//(Integer)session.getAttribute(GDFConstants.LOGGED_IN_GDF_ID);
        government=governmentBeanLocal.findGovernmentById(governmentId);
        governmentAddress=government.getGovernmentAddress();
        //country=governmentAddress.getCountry().getName();//Email and Country not allowed to change
        clientCountryCode=governmentAddress.getCountry().getCode();
        country=governmentAddress.getCountry().getName();
        LOGGER.log(Level.INFO, "Government loaded :{0}", government.getEmail1());
        ministry=government.getGovernmentOrg().getMinistry();
        department=government.getGovernmentOrg().getDepartment();
        ministries=new ArrayList<>();
        departments=new ArrayList<>();
        LOGGER.log(Level.INFO, "GovernmentMBean initialised");
        List<String> mTries=referenceDataBeanLocal.getGovernmentMinistries(access.getCountryCode());//SubTypes to be loaded via AJAX calls
        mTries.remove(ministry);//let's remove the already chosen one.
        ministries.addAll(mTries);
        LOGGER.info("Government and GovernmentAddress initialized");
        
        
    }
    
    public void ajaxTypeListener(AjaxBehaviorEvent event){
        LOGGER.log(Level.INFO, "Ministry is {0}", ministry);
        departments = new ArrayList<>();
        departments.add("Please Select");
        departments.addAll(referenceDataBeanLocal.getGovernmentDepartments(clientCountryCode, ministry));
        LOGGER.log(Level.INFO, ministry + " {0} Departments loaded in GovernmentMBean ", departments.size());
    }
    
     /**
     * The MBean being ViewScoped- This method is to be called via AJAX from the
     * front end - hence no return value
     */
    public void amendGovernment() {
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
    
    
    private void validateDetailsAndAddress(){
        String name=government.getName().trim();
        if (name.isEmpty()){
            FacesContext.getCurrentInstance().addMessage("governmentName",new FacesMessage(FacesMessage.SEVERITY_ERROR,"Department Name required.","Department Name is required"));
        }else if (name.length()<4 || name.length()>250){
            FacesContext.getCurrentInstance().addMessage("governmentName",new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid Name","Invalid Department Name"));
        }
        //Validate Email now
        String email2=government.getEmail2().trim();//Email 2 is Mandatory
         if (email2.isEmpty()){
            FacesContext.getCurrentInstance().addMessage("email2",new FacesMessage(FacesMessage.SEVERITY_ERROR,"Email 2 required","Email 2 is mandatory"));
        }else{
            String emailRegEx = GDFConstants.EMAIL_REGEX;
            Pattern pEmail = Pattern.compile(emailRegEx);
            Matcher mP = pEmail.matcher(email2);
            boolean matches = mP.find();
            if (!matches) {
                FacesContext.getCurrentInstance().addMessage("email2", new FacesMessage(FacesMessage.SEVERITY_ERROR,"Email 2 invalid","Email 2 is not valid"));
            }
        }
        //Website now
        String website=government.getWebsite();
        if (website.trim().isEmpty()){
            FacesContext.getCurrentInstance().addMessage("website",new FacesMessage(FacesMessage.SEVERITY_ERROR,"Website required.","Website Address is required"));
        }else{
            Pattern pWebsite = Pattern.compile(GDFConstants.URL_REGEX);
            Matcher mW = pWebsite.matcher(website);
            boolean matchesU = mW.find();
            if (!matchesU) {
                FacesContext.getCurrentInstance().addMessage("website", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Website invalid", "Website address not valid"));
            }
        }
        
        
        
        String contact=government.getContactName().trim();
        if (contact.isEmpty()){
            FacesContext.getCurrentInstance().addMessage(null,new FacesMessage(FacesMessage.SEVERITY_ERROR,"Contact required.","Contact Name is required."));
        }
        
        String description=government.getDescription().trim();
        if (description.isEmpty()){
            FacesContext.getCurrentInstance().addMessage("desc",new FacesMessage(FacesMessage.SEVERITY_ERROR,"Description required","Description is required."));
        }else if (description.length()<10){
            FacesContext.getCurrentInstance().addMessage("desc",new FacesMessage(FacesMessage.SEVERITY_ERROR,"At least 10 chars","Description must be atleast 10 chars long."));
        }else if (description.length()>250){
            FacesContext.getCurrentInstance().addMessage("desc",new FacesMessage(FacesMessage.SEVERITY_ERROR,"At most 250 chars","Description must be less than 250 chars long."));
        }
        
        //Address Validation
        if (governmentAddress.getLine1().trim().isEmpty()) {//Line 1 is Mandatory
            FacesContext.getCurrentInstance().addMessage("line1",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Line 1 is mandatory", "Line 1 is mandatory!"));
        }
        if (governmentAddress.getLine2().trim().isEmpty()) {//Line 2is Mandatory
            FacesContext.getCurrentInstance().addMessage("line2",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Line 2 is mandatory", "Line 2 is mandatory!"));
        }
        
        
        //Phones now
        String phone1=governmentAddress.getPhone1().trim();//This is mandatory
        Pattern pPhone=Pattern.compile(GDFConstants.PHONE_REGEX); 
        if (phone1.isEmpty()){
            FacesContext.getCurrentInstance().addMessage("phone1",new FacesMessage(FacesMessage.SEVERITY_ERROR,"Phone 1 required","Phone 1 is required"));
        }else{
            Matcher ph1M=pPhone.matcher(phone1);
            boolean matchesPh1= ph1M.find();
            if (!matchesPh1){
               FacesContext.getCurrentInstance().addMessage("phone1",new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid Phone 1","Phone 1 is not invalid")); 
            }
        }
        String phone2=governmentAddress.getPhone2().trim();//not mandatory
        if (!phone2.isEmpty()){
            Matcher ph2M=pPhone.matcher(phone2);
            boolean matchesPh2= ph2M.find();
            if (!matchesPh2){
               FacesContext.getCurrentInstance().addMessage("phone2",new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid Phone 2","Phone 2 is invalid")); 
            }
        }
        String phone3=governmentAddress.getPhone3().trim();//not mandatory
        if (!phone3.isEmpty()){
            Matcher ph3M=pPhone.matcher(phone3);
            boolean matchesPh3= ph3M.find();
            if (!matchesPh3){
               FacesContext.getCurrentInstance().addMessage("phone3",new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid Phone 3","Phone 3 is invalid")); 
            }
        }
        switch (clientCountryCode) {
                case GDFConstants.IN_CODE: {
                    Pattern pCdIn = Pattern.compile(GDFConstants.IN_POSTCODE_REGEX);
                    Matcher mPCdIn = pCdIn.matcher(governmentAddress.getPostcode());
                    if (!mPCdIn.find()) {
                        FacesContext.getCurrentInstance().addMessage("postcode", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Post Code", "Invalid Post Code of " + GDFConstants.IN_NAME));
                    }
                    break;

                }
                case GDFConstants.GB_CODE: {
                    Pattern pCdGB = Pattern.compile(GDFConstants.GB_POSTCODE_REGEX);
                    Matcher mPCdGB = pCdGB.matcher(governmentAddress.getPostcode());
                    if (!mPCdGB.find()) {
                        FacesContext.getCurrentInstance().addMessage("postcode", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Post Code", "Invalid Post Code Post Code of " + GDFConstants.GB_NAME));
                    }
                    break;
                }
                case GDFConstants.US_CODE: {
                    Pattern pCdUS = Pattern.compile(GDFConstants.US_POSTCODE_REGEX);
                    Matcher mPCdUS = pCdUS.matcher(governmentAddress.getPostcode());
                    if (!mPCdUS.find()) {
                        FacesContext.getCurrentInstance().addMessage("postcode", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Zip Code", "Invalid Zip Code of " + GDFConstants.US_NAME));
                    }
                }
                break;
            }
       //Fill in the blanks..
        
        String city=governmentAddress.getCity().trim();
        if (city.length()<2 || city.length()>250 ){
            FacesContext.getCurrentInstance().addMessage("city",new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid City","City is invalid")); 
        }
        
        String state=governmentAddress.getState().trim();
        if (state.length()<2 || state.length()>250){
            FacesContext.getCurrentInstance().addMessage("state",new FacesMessage(FacesMessage.SEVERITY_ERROR,"Invalid State","State is invalid")); 
        }
        
        if (governmentOrgs==null){
            governmentOrgs=referenceDataBeanLocal.getGovernmentOrgs(clientCountryCode);//need with Country Code
        }
        //First set governmentOrg
        for (GovernmentOrg go : governmentOrgs) {
            if(go.getMinistry().equals(ministry) && go.getDepartment().equals(department)){
                government.setGovernmentOrg(go);
                break;
            }
            
        }
        
        //Finally check if Logo needs change..
        if (logoFile!=null){
            String fileName=logoFile.getSubmittedFileName();
            if (!government.getLogoFile().equals(fileName)){//If the names of files are different, it means profile file has been changed
                updateLogoFile();
                government.setLogoFile(fileName);
            }
            
        }
    }
    
    public void submitDetails(){
        government.setUpdatedOn(LocalDateTime.now());
        government.setGovernmentAddress(governmentAddress);
        governmentAddress.setGovernment(government);
        governmentBeanLocal.amendGovernment(government);
        LOGGER.log(Level.INFO, "Government updated with ID: {0} and Address ID: {1}", new Object[]{government.getId(), government.getGovernmentAddress().getId()});
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session = request.getSession();
        Access access = (Access) session.getAttribute(GDFConstants.ACCESS);
        if (!access.getProfileFile().equals(government.getLogoFile())) {
            access.setProfileFile(government.getLogoFile());
            access.setImage(government.getImage());
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
                government.setLogoFile(fullFileName);
                government.setImage(imageInByte);
                //We would need to display the Image in the ConfirmPage, which is next in the Flow.
                //There is no solution for that - only a workaround.
                //We put this image in the session for now and once the data has been persisted in the Database the image from the session will be removed.
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
    
    public String getReturnValue() {
        return "GovernmentAcknowledge";
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

   
    private void updateData() {
        government.setUpdatedOn(LocalDateTime.now());
        governmentBeanLocal.amendGovernment(government);
        
        HttpServletRequest request= (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=request.getSession();
        Access access=(Access) session.getAttribute(GDFConstants.ACCESS);
        if (!access.getProfileFile().equals(government.getLogoFile())){
            access.setProfileFile(government.getLogoFile());
            accessBeanLocal.updateAccess(access);
            session.invalidate();
            session=request.getSession();
            session.setAttribute(GDFConstants.ACCESS, access);
            LOGGER.log(Level.INFO, "Access resent in session with Logo Image of {0}", access.getProfileFile());
            
        }
    }
    
    

    public Part getLogoFile() {
        return logoFile;
    }

    public void setLogoFile(Part logoFile) {
        this.logoFile = logoFile;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    
    
    
    
    
}
