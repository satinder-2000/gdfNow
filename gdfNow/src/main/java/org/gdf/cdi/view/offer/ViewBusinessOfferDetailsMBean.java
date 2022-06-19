/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.view.offer;

import org.gdf.ejb.AccessBeanLocal;
import org.gdf.ejb.BusinessBeanLocal;
import org.gdf.ejb.DeederBeanLocal;
import org.gdf.model.Access;
import org.gdf.model.Business;
import org.gdf.model.BusinessOffer;
import org.gdf.model.Deeder;
import org.gdf.model.comment.BusinessOfferComment;
import org.gdf.model.like.BusinessOfferLike;
import org.gdf.model.like.DeederLike;
import org.gdf.util.GDFConstants;
import org.gdf.util.ImageVO;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author satindersingh Need to carve out separate Classes
 * (Business/Government/NGO) to show the
 *
 */
@Named(value = "viewBusinessOfferDetailsMBean")
@ViewScoped
public class ViewBusinessOfferDetailsMBean implements Serializable {

    static final Logger LOGGER = Logger.getLogger(ViewBusinessOfferDetailsMBean.class.getName());

    @Inject//TEMPORARY CODE
    AccessBeanLocal accessBeanLocal;

    @Inject
    BusinessBeanLocal businessBeanLocal;

    @Inject
    DeederBeanLocal deederBeanLocal;

    private BusinessOffer businessOffer;

    private List<BusinessOfferLike> businessOfferLikes;

    private BusinessOfferComment businessOfferComment;

    private List<BusinessOfferComment> businessOfferComments;

    String businessOfferLikesStr;

    private String likesBy;

    private String deederLikesStr;

    private String deederLikesBy;

    @PostConstruct
    public void init() {
        ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
        businessOfferComment = new BusinessOfferComment();
        //businessOfferComments=new ArrayList<>();
        LOGGER.info("ViewBusinessOfferDetailsMBean initialised");
        loadOffer();
    }

    public String loadOffer() {

        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String offerIdStr = request.getParameter("offerId");
        int offerId = Integer.parseInt(offerIdStr);
        LOGGER.log(Level.INFO, "Offer ID is {0}", offerId);

        businessOffer = businessBeanLocal.getBusinessOffer(offerId);
        Business business = businessOffer.getBusiness();
        String logoFile=business.getLogoFile();
        String picType=logoFile.substring(logoFile.indexOf('.')+1);
        ImageVO imageVO=new ImageVO(picType, business.getImage());
        HttpSession session=request.getSession(true);
        session.setAttribute(GDFConstants.TEMP_IMAGE, imageVO);

        //set Pofile Image of the Deeder now
        Deeder deeder = businessOffer.getDeed().getDeeder();
        String profileFile=deeder.getProfileFile();
        picType=profileFile.substring(profileFile.indexOf('.')+1);
        ImageVO imageVO2=new ImageVO(picType, deeder.getImage());
        session.setAttribute(GDFConstants.TEMP_IMAGE_2, imageVO2);


        //Comments on Business Offer
        businessOfferComments = businessOffer.getBusinessOfferComments();

        //Business Offer Likes now
        businessOfferLikes = new ArrayList<>();
        businessOfferLikes = businessBeanLocal.getBusinessOfferLikes(offerId);
        businessOfferLikesStr = String.valueOf(businessOfferLikes.size());//For display on the front end.
        //Prepare for the Pop Up of the name of the 'likesBy'
        likesBy = "";
        if (businessOfferLikes.size() > 0) {//exception handler in case there is no Like Yet.
            for (BusinessOfferLike bOL : businessOfferLikes) {
                likesBy = likesBy.concat(bOL.getLikeByName()).concat(",");
            }
            int till = likesBy.lastIndexOf(",");
            likesBy = likesBy.substring(0, till);
        }
        //Finally Deeder Likes
        deederLikesBy = "";//Prepare for the Pop Up of the names
        List<DeederLike> deederLikes = deeder.getDeederLikes();
        if (deederLikes != null && !deederLikes.isEmpty()) {
            deederLikesStr = Integer.toString(deederLikes.size());
            for (DeederLike dL : deederLikes) {
                deederLikesBy = deederLikesBy.concat(dL.getLikeByName()).concat(",");
            }
            int till = deederLikesBy.lastIndexOf(",");
            deederLikesBy = deederLikesBy.substring(0, till);
        }
        LOGGER.log(Level.INFO, "BusinessOffer loaded with ID:{0}, Deed: {1} and Deeder : {2}", new Object[]{businessOffer.getId(), businessOffer.getDeed().getId(), businessOffer.getDeed().getDeeder().getId()});
        LOGGER.info(businessOffer.getOfferedOn().toString());
        LOGGER.info(businessOffer.getDescription());
        return "/view/ViewBusinessOfferDetails?faces-redirect=true";
    }

    public String addComment() {
        
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session = request.getSession();
        Access access = (Access) session.getAttribute(GDFConstants.ACCESS);
        if (access==null){//Not Logged in - change made on 17/03/2019
           FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please Login to comment", "Please Login to comment"));
                return null; 
        }
        //Validate the size of the comment first. Max allowed is 1,000 chars
        int textLength = businessOfferComment.getText().length();
        if (textLength > 1000) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Comment exceeds 1,000 chars.", "Comment exceeds 1,000 chars."));
            return null;
        }
        businessOfferComment.setBusinessOffer(businessOffer);
        businessOfferComment.setDate(LocalDateTime.now());
        
        /*LOGGER.warning("TEMPORARY CODE - STARTS");
        List<Access> accessL = accessBeanLocal.getAllAccess(20);
        int size = accessL.size();
        Random rand = new Random();
        int randIndex = rand.nextInt(size);
        access = accessL.get(randIndex);
        LOGGER.warning("TEMPORARY CODE - ENDS");*/
        businessOfferComment.setPostedBy(access.getEmail());
        businessOfferComment.setAccessType(access.getEntityType());
        businessOffer.getBusinessOfferComments().add(businessOfferComment);
        LOGGER.log(Level.INFO, "Comment added :{0}", businessOfferComment.getText());
        businessBeanLocal.addBusinessOfferComment(businessOffer);
        businessOfferComment = new BusinessOfferComment();
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Thanks for posting your comment. It will be published soon.", "Thanks for posting your comment. It will be published soon."));
        return null;
    }

    public String addLike() {
        
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session = request.getSession();
        Access access = (Access) session.getAttribute(GDFConstants.ACCESS);
        if (access==null){//Not Logged in - change made on 17/03/2019
           FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please Login to Like", "Please Login to Like"));
                return null; 
        }

        BusinessOfferLike boLike = new BusinessOfferLike();
        boLike.setBusinessOffer(businessOffer);
        /*LOGGER.warning("TEMPORARY CODE - STARTS");
        List<Access> accessL = accessBeanLocal.getAllAccess(20);
        int size = accessL.size();
        Random rand = new Random();
        int randIndex = rand.nextInt(size);
        access = accessL.get(randIndex);
        LOGGER.warning("TEMPORARY CODE - ENDS");*/
        boLike.setAccessType(access.getEntityType());
        boLike.setAccessId(access.getEntityId());
        boLike.setLikeByName(access.getName());
        boLike.setTime(LocalDateTime.now());
        boLike = businessBeanLocal.addOfferLike(boLike);
        businessOfferLikes.add(boLike);
        LOGGER.log(Level.INFO, "BusinessOfferLike persisted with ID: {0}", boLike.getId());
        //refresh the Likes
        businessOfferLikes = businessBeanLocal.getBusinessOfferLikes(businessOffer.getId());
        businessOfferLikesStr = String.valueOf(businessOfferLikes.size());
        likesBy = "";
        for (BusinessOfferLike bOL : businessOfferLikes) {
            likesBy = likesBy.concat(bOL.getLikeByName()).concat(",");
        }
        int till = likesBy.lastIndexOf(",");
        likesBy = likesBy.substring(0, till);
        return "";
    }

    public void addCommentLike(int commentId) {
        int index = 0;
        for (BusinessOfferComment boc : businessOfferComments) {
            if (boc.getId() == commentId) {
                boc.setLikes(boc.getLikes() + 1);
                boc = businessBeanLocal.addCommentLike(boc);
                businessOfferComments.set(index, boc);//Since the Likes have been updated in the database, they must be reflected in memory as well.
                break;
            }
            index++;

        }
    }

    public String addDeederLike() {
        
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session = request.getSession();
        Access access = (Access) session.getAttribute(GDFConstants.ACCESS);
        if (access==null){//Not Logged in - change made on 17/03/2019
           FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please Login to Like", "Please Login to Like"));
                return null; 
        }

        DeederLike dl = new DeederLike();
        dl.setDeeder(businessOffer.getDeed().getDeeder());
        
        /*LOGGER.warning("TEMPORARY CODE - STARTS");
        List<Access> accessL = accessBeanLocal.getAllAccess(20);
        int size = accessL.size();
        Random rand = new Random();
        int randIndex = rand.nextInt(size);
        access = accessL.get(randIndex);
        LOGGER.warning("TEMPORARY CODE - ENDS");*/
        dl.setAccessId(access.getEntityId());
        dl.setAccessType(access.getEntityType());
        dl.setLikeByName(access.getName());
        dl.setTime(LocalDateTime.now());
        dl = deederBeanLocal.addDeederLike(dl);
        //deed.getDeeder().getDeederLikes().add(dl);
        List<DeederLike> deederLikes = businessOffer.getDeed().getDeeder().getDeederLikes();
        deederLikesStr = Integer.toString(deederLikes.size());
        //Prepare for the Pop Up of the name of the 'likesBy'
        deederLikesBy = "";
        for (DeederLike dlk : deederLikes) {
            deederLikesBy = deederLikesBy.concat(dlk.getLikeByName()).concat(",");
        }
        int till = deederLikesBy.lastIndexOf(",");
        deederLikesBy = deederLikesBy.substring(0, till);
        LOGGER.log(Level.INFO, "Deeder updated with new DeederLike ID: {0}", dl.getId());
        return null;
    }

    public BusinessOfferComment getBusinessOfferComment() {
        return businessOfferComment;
    }

    public void setBusinessOfferComment(BusinessOfferComment businessOfferComment) {
        this.businessOfferComment = businessOfferComment;
    }

    public List<BusinessOfferComment> getBusinessOfferComments() {
        return businessOfferComments;
    }

    public void setBusinessOfferComments(List<BusinessOfferComment> businessOfferComments) {
        this.businessOfferComments = businessOfferComments;
    }

    public BusinessOffer getBusinessOffer() {
        return businessOffer;
    }

    public void setBusinessOffer(BusinessOffer businessOffer) {
        this.businessOffer = businessOffer;
    }

    public String getBusinessOfferLikesStr() {
        return businessOfferLikesStr;
    }

    public List<BusinessOfferLike> getBusinessOfferLikes() {
        return businessOfferLikes;
    }

    public void setBusinessOfferLikes(List<BusinessOfferLike> businessOfferLikes) {
        this.businessOfferLikes = businessOfferLikes;
    }

    public String getLikesBy() {
        return likesBy;
    }

    public void setLikesBy(String likesBy) {
        this.likesBy = likesBy;
    }

    public String getDeederLikesStr() {
        return deederLikesStr;
    }

    public void setDeederLikesStr(String deederLikesStr) {
        this.deederLikesStr = deederLikesStr;
    }

    public String getDeederLikesBy() {
        return deederLikesBy;
    }

    public void setDeederLikesBy(String deederLikesBy) {
        this.deederLikesBy = deederLikesBy;
    }

}
