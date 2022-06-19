/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi;

import org.gdf.ejb.AccessBeanLocal;
import org.gdf.ejb.DeederBeanLocal;
import org.gdf.ejb.UserDeederBeanLocal;
import org.gdf.model.Access;
import org.gdf.model.EntityType;
import org.gdf.model.Deeder;
import org.gdf.model.OnHold;
import org.gdf.util.GDFConstants;
import org.gdf.util.ImageResizeUtil;
import org.gdf.util.ImageVO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
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
@Named(value = "uDAccessConfirmMBean")
@ViewScoped
public class UDAccessConfirmMBean implements Serializable {
    
     final static Logger LOGGER=Logger.getLogger(UDAccessConfirmMBean.class.getName());
    
    
    
    @Inject
    AccessBeanLocal accessBeanLocal;
    
    @Inject
    DeederBeanLocal deederBeanLocal;
    
    @Inject
    UserDeederBeanLocal userDeederBeanLocal;
    
    private OnHold onHold;
    
    private Access access;
    
    private Deeder deeder;
    
    private boolean acceptedTC;
    
    private Part profileFilePart;
    
    public Access getAccess() {
        return access;
    }

    public void setAccess(Access access) {
        this.access = access;
    }
    
    @PostConstruct
    public void init(){
        LOGGER.info("View initialised for Access");
        loadAccess();
    }

    private void loadAccess() {
        access = new Access();
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String idStr = request.getParameter("id");
        LOGGER.log(Level.INFO, "Access Confirm request received from {0}", idStr);
        int id=Integer.parseInt(idStr);
        //This email should be on hold..
        onHold  = accessBeanLocal.getOnHold(id,EntityType.DEEDER.toString());
        if (onHold==null) {//preventive code. In case of unlikely event, should the Access link gets compromised.
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Id value", "Invalid Id value"));
            return;
        }
        access.setEntityId(onHold.getEntityId());
        access.setEmail(onHold.getEmail());
        access.setEmailOrg(onHold.getEmail());
        access.setCountryCode(onHold.getCountryCode());
        access.setProfileFile(onHold.getProfileFile());
        access.setImage(onHold.getImage());
        deeder=deederBeanLocal.getDeeder(onHold.getEntityId());
        LOGGER.log(Level.INFO, "Access Bean Initialised {0}", access.getEmail());
        
    }
    
    public String submitDetails(){
        
        String toReturn=null;
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
        //T&C must be accepted
        if (!this.acceptedTC) {
            FacesContext.getCurrentInstance().addMessage("confChBx",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "T&C not accetped.", "T&C not accetped"));
        }
        
        List<FacesMessage> msgs= FacesContext.getCurrentInstance().getMessageList();
        if (msgs!=null && msgs.size()>0){
            toReturn =null;
        }else{//Prepare fro submission in the DB
            saveProfileImage();
            //Proceed to create Access.
            access.setProfileFile(deeder.getProfileFile());//Name set in the previous method call - saveProfileImage
            access.setImage(deeder.getImage());//Image byte[] has been set in deeder in the previous method call - saveProfileImage
            access.setEntityType(EntityType.DEEDER);
            
            Access accessDb=accessBeanLocal.createAccess(access);//changed on 06/11/2018 from method createUserDeederAccess. Under the cover, there is the same functionality.
            LOGGER.log(Level.INFO, "UserDeeder Access record created successfully on {0}", accessDb.getCreatedOn());
            deeder.setConfirmed(true);
            deeder= userDeederBeanLocal.updateUserDeeder(deeder);
            LOGGER.log(Level.INFO, "UserDeeder record updated successfully on {0}", deeder.getUpdatedOn());
            toReturn="DeederRegisterFinal";
        }
        //Deeder has been sussessfully persisted in the Database. Now the Image (byte[]) can be removed from the session as well. 
        HttpServletRequest request= (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=request.getSession();
        session.removeAttribute(GDFConstants.TEMP_IMAGE);
        LOGGER.log(Level.INFO, "toReturn is :{0}", toReturn);
        return toReturn;
    }
    
    public void saveProfileImage() {
        try {
            InputStream input = profileFilePart.getInputStream();
            int fileSize = (int) profileFilePart.getSize();
            if (fileSize > (1000 * 1024)) {
                FacesContext.getCurrentInstance().addMessage("profileFile",
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
                HttpServletRequest request= (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
                HttpSession session=request.getSession(true);
                String imageType=fullFileName.substring(fullFileName.indexOf('.')+1);
                ImageVO imageVO=new ImageVO(imageType,deeder.getImage());
                session.setAttribute(GDFConstants.TEMP_IMAGE, imageVO);//This Image will be removed from Session once the data has been persisted.
            }
        } catch (IOException ex) {
            LOGGER.severe(ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }
        
    }

    public boolean isAcceptedTC() {
        return acceptedTC;
    }

    public void setAcceptedTC(boolean acceptedTC) {
        this.acceptedTC = acceptedTC;
    }

    public Part getProfileFilePart() {
        return profileFilePart;
    }

    public void setProfileFilePart(Part profileFilePart) {
        this.profileFilePart = profileFilePart;
    }
    
    

    
    
}
