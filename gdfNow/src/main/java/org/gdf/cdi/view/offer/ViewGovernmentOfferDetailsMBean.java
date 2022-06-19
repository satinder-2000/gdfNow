/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.view.offer;

import org.gdf.ejb.AccessBeanLocal;
import org.gdf.ejb.DeederBeanLocal;
import org.gdf.ejb.GovernmentBeanLocal;
import org.gdf.model.Access;
import org.gdf.model.EntityType;
import org.gdf.model.Deeder;
import org.gdf.model.Government;
import org.gdf.model.GovernmentOffer;
import org.gdf.model.comment.GovernmentOfferComment;
import org.gdf.model.like.DeederLike;
import org.gdf.model.like.GovernmentOfferLike;
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
 * @author satindersingh
 */
@Named(value = "viewGovernmentOfferDetailsMBean")
@ViewScoped
public class ViewGovernmentOfferDetailsMBean implements Serializable {
    
    static final Logger LOGGER=Logger.getLogger(ViewGovernmentOfferDetailsMBean.class.getName());
    
    @Inject//TEMPORARY CODE
    AccessBeanLocal accessBeanLocal;
    
    @Inject
    GovernmentBeanLocal governmentBeanLocal;
    
    @Inject
    DeederBeanLocal deederBeanLocal;
    
    private GovernmentOffer governmentOffer;
    
    private GovernmentOfferComment governmentOfferComment;
    
    private List<GovernmentOfferComment> governmentOfferComments;
    
    private List<GovernmentOfferLike> governmentOfferLikes;
    
    String governmentOfferLikesStr;
    
    private String likesBy;
    
    private String deederLikesStr;
    
    private String deederLikesBy;
    
    
    @PostConstruct
    public void init(){
        LOGGER.info("ViewGovernmentOfferDetailsMBean initialised");
        ExternalContext extContext=FacesContext.getCurrentInstance().getExternalContext();
        governmentOfferComment = new GovernmentOfferComment();
        governmentOfferComments=new ArrayList<>();
        loadOffer();
    }
    
    public String loadOffer(){
        
        HttpServletRequest request= (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String offerIdStr=request.getParameter("offerId");
        int offerId= Integer.parseInt(offerIdStr);
        LOGGER.log(Level.INFO, "Offer ID is {0}", offerId);
        
        governmentOffer=governmentBeanLocal.getGovernmentOffer(offerId);
        Government government=  governmentOffer.getGovernment();
        String logoFile=government.getLogoFile();
        String picType=logoFile.substring(logoFile.indexOf('.')+1);
        ImageVO imageVO=new ImageVO(picType, government.getImage());
        HttpSession session=request.getSession(true);
        session.setAttribute(GDFConstants.TEMP_IMAGE, imageVO);
        //set Pofile Image of the Deeder now
        Deeder deeder=governmentOffer.getDeed().getDeeder();
        String profileFile=deeder.getProfileFile();
        picType=profileFile.substring(profileFile.indexOf('.')+1);
        ImageVO imageVO2=new ImageVO(picType, deeder.getImage());
        session.setAttribute(GDFConstants.TEMP_IMAGE_2, imageVO2);
        
        //Comments on Government Offer
        governmentOfferComments=governmentBeanLocal.getGovernmentOfferComments(offerId);
        //Business Offer Likes now
        governmentOfferLikes=new ArrayList<>();
        governmentOfferLikes=governmentOffer.getGovernmentOfferLikes();
        governmentOfferLikesStr=String.valueOf(governmentOfferLikes.size());//For display on the front end.
        //Prepare for the Pop Up of the name of the 'likesBy'
        likesBy="";
        if (governmentOfferLikes.size()>0){//exception handler in case there is no Like Yet.
            for (GovernmentOfferLike gOL : governmentOfferLikes) {
                likesBy = likesBy.concat(gOL.getLikeByName()).concat(",");
            }
            int till = likesBy.lastIndexOf(",");
            likesBy = likesBy.substring(0, till);
        }
        //Finally Deeder Likes
        deederLikesBy="";//Prepare for the Pop Up of the names
        List<DeederLike> deederLikes=deeder.getDeederLikes();
        if (deederLikes!=null && !deederLikes.isEmpty()){
           deederLikesStr=Integer.toString(deederLikes.size());
            for (DeederLike dL : deederLikes) {
              deederLikesBy=deederLikesBy.concat(dL.getLikeByName()).concat(",");
            }
            int till = deederLikesBy.lastIndexOf(",");
            deederLikesBy = deederLikesBy.substring(0, till);
        }
        
        LOGGER.log(Level.INFO, "GovernmentOffer loaded with ID:{0}, Deed: {1} and Deeder : {2}", new Object[]{governmentOffer.getId(), governmentOffer.getDeed().getId(), governmentOffer.getDeed().getDeeder().getId()});
        return "/view/ViewGovernmentOfferDetails?faces-redirect=true";
    }
    
    public String addComment(){
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session = request.getSession();
        Access access = (Access) session.getAttribute(GDFConstants.ACCESS);
        if (access==null){//Not Logged in - change made on 17/03/2019
           FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please Login to comment", "Please Login to comment"));
                return null; 
        }
        //Validate the size of the comment first. Max allowed is 1,000 chars
        int textLength=governmentOfferComment.getText().length();
        if (textLength>1000){
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Comment exceeds 1,000 chars.", "Comment exceeds 1,000 chars."));
            return null;
        }
        governmentOfferComment.setGovernmentOffer(governmentOffer);
        governmentOfferComment.setDate(LocalDateTime.now());
        /*LOGGER.warning("TEMPORARY CODE - STARTS");
            List<Access> accessL=accessBeanLocal.getAllAccess(20);
            int size=accessL.size();
            Random rand=new Random();
            int randIndex=rand.nextInt(size);
            access=accessL.get(randIndex);
            LOGGER.warning("TEMPORARY CODE - ENDS");*/
        governmentOfferComment.setPostedBy(access.getEmail());
        governmentOfferComment.setAccessType(access.getEntityType());
        governmentOffer.getGovernmentOfferComments().add(governmentOfferComment);
        LOGGER.log(Level.INFO, "Comment added :{0}", governmentOfferComment.getText());
        governmentBeanLocal.addGovernmentOfferComment(governmentOffer);
        governmentOfferComment=new GovernmentOfferComment();
        FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Thanks for posting your comment. It will be published soon.", "Thanks for posting your comment. It will be published soon."));
        return null;
    }
    
    public String addLike(){
        
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session = request.getSession();
        Access access = (Access) session.getAttribute(GDFConstants.ACCESS);
        if (access==null){//Not Logged in - change made on 17/03/2019
           FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please Login to Like", "Please Login to Like"));
                return null; 
        }
        
        GovernmentOfferLike goLike=new GovernmentOfferLike();
        goLike.setGovernmentOffer(governmentOffer);
        
            /*LOGGER.warning("TEMPORARY CODE - STARTS");
            List<Access> accessL=accessBeanLocal.getAllAccess(20);
            int size=accessL.size();
            Random rand=new Random();
            int randIndex=rand.nextInt(size);
            access=accessL.get(randIndex);
            LOGGER.warning("TEMPORARY CODE - ENDS");*/
        goLike.setAccessType(access.getEntityType());
        goLike.setAccessId(access.getEntityId());
        goLike.setLikeByName(access.getName());
        goLike.setTime(LocalDateTime.now());
        goLike=governmentBeanLocal.addOfferLike(goLike);
        governmentOfferLikes.add(goLike);
        LOGGER.log(Level.INFO, "GovernmentOfferLike persisted with ID: {0}", goLike.getId());
        //refresh the Likes
        governmentOfferLikes=governmentBeanLocal.getGovernmentOfferLikes(governmentOffer.getId());
        governmentOfferLikesStr=String.valueOf(governmentOfferLikes.size());
        likesBy="";
        for (GovernmentOfferLike gOL : governmentOfferLikes) {
            likesBy=likesBy.concat(gOL.getLikeByName()).concat(",");
        }
        int till=likesBy.lastIndexOf(",");
        likesBy=likesBy.substring(0,till);
        return null;
    }
    
    public void addCommentLike(int commentId){
        int index=0;
        for (GovernmentOfferComment goc : governmentOfferComments) {
            if(goc.getId()==commentId){
                goc.setLikes(goc.getLikes()+1);
                goc=governmentBeanLocal.addCommentLike(goc);
                governmentOfferComments.set(index, goc);//Since the Likes have been updated in the database, they must be reflected in memory as well.
                break;
            }
            index++;
            
        }
    }
    
    public String addDeederLike(){
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session = request.getSession();
        Access access = (Access) session.getAttribute(GDFConstants.ACCESS);
        if (access==null){//Not Logged in - change made on 17/03/2019
           FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please Login to Like", "Please Login to Like"));
                return null; 
        }
        DeederLike dl=new DeederLike();
        dl.setDeeder(governmentOffer.getDeed().getDeeder());
        /*LOGGER.warning("TEMPORARY CODE - STARTS");
            List<Access> accessL=accessBeanLocal.getAllAccess(20);
            int size=accessL.size();
            Random rand=new Random();
            int randIndex=rand.nextInt(size);
            access=accessL.get(randIndex);
            LOGGER.warning("TEMPORARY CODE - ENDS");*/
        dl.setAccessId(access.getEntityId());
        dl.setAccessType(access.getEntityType());
        dl.setLikeByName(access.getName());
        dl.setTime(LocalDateTime.now());
        dl=deederBeanLocal.addDeederLike(dl);
        //deed.getDeeder().getDeederLikes().add(dl);
        List<DeederLike> deederLikes=governmentOffer.getDeed().getDeeder().getDeederLikes();
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
    

    public GovernmentOffer getGovernmentOffer() {
        return governmentOffer;
    }

    public void setGovernmentOffer(GovernmentOffer governmentOffer) {
        this.governmentOffer = governmentOffer;
    }

    public GovernmentOfferComment getGovernmentOfferComment() {
        return governmentOfferComment;
    }

    public void setGovernmentOfferComment(GovernmentOfferComment governmentOfferComment) {
        this.governmentOfferComment = governmentOfferComment;
    }

    public List<GovernmentOfferComment> getGovernmentOfferComments() {
        return governmentOfferComments;
    }

    public void setGovernmentOfferComments(List<GovernmentOfferComment> governmentOfferComments) {
        this.governmentOfferComments = governmentOfferComments;
    }

    public List<GovernmentOfferLike> getGovernmentOfferLikes() {
        return governmentOfferLikes;
    }

    public void setGovernmentOfferLikes(List<GovernmentOfferLike> governmentOfferLikes) {
        this.governmentOfferLikes = governmentOfferLikes;
    }

    public String getGovernmentOfferLikesStr() {
        return governmentOfferLikesStr;
    }

    public void setGovernmentOfferLikesStr(String governmentOfferLikesStr) {
        this.governmentOfferLikesStr = governmentOfferLikesStr;
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
