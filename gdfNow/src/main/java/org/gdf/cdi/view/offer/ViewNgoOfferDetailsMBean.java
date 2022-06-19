/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.view.offer;

import org.gdf.ejb.AccessBeanLocal;
import org.gdf.ejb.DeederBeanLocal;
import org.gdf.ejb.NgoBeanLocal;
import org.gdf.model.Access;
import org.gdf.model.Deeder;
import org.gdf.model.Ngo;
import org.gdf.model.NgoOffer;
import org.gdf.model.comment.NgoOfferComment;
import org.gdf.model.like.DeederLike;
import org.gdf.model.like.NgoOfferLike;
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
@Named(value = "viewNgoOfferDetailsMBean")
@ViewScoped
public class ViewNgoOfferDetailsMBean implements Serializable {

    static final Logger LOGGER = Logger.getLogger(ViewNgoOfferDetailsMBean.class.getName());

    @Inject//TEMPORARY CODE
    AccessBeanLocal accessBeanLocal;

    @Inject
    NgoBeanLocal ngoBeanLocal;

    @Inject
    DeederBeanLocal deederBeanLocal;

    private NgoOffer ngoOffer;

    private NgoOfferComment ngoOfferComment;

    private List<NgoOfferComment> ngoOfferComments;

    
    private List<NgoOfferLike> ngoOfferLikes;

    private String ngoOfferLikesStr;

    private String deederLikesStr;

    private String likesBy;

    private String deederLikesBy;

    @PostConstruct
    public void init() {
        LOGGER.info("ViewGovernmentOfferDetailsMBean initialised");
        ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
        ngoOfferComment = new NgoOfferComment();
        //ngoOfferComments=new ArrayList<>();
        loadOffer();
    }

    public String loadOffer() {

        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String offerIdStr = request.getParameter("offerId");
        int offerId = Integer.parseInt(offerIdStr);
        LOGGER.log(Level.INFO, "Offer ID is {0}", offerId);

        ngoOffer = ngoBeanLocal.getNgoOffer(offerId);
        Ngo ngo = ngoOffer.getNgo();
        String logoFile=ngo.getLogoFile();
        String picType=logoFile.substring(logoFile.indexOf('.')+1);
        ImageVO imageVO=new ImageVO(picType, ngo.getImage());
        HttpSession session=request.getSession(true);
        session.setAttribute(GDFConstants.TEMP_IMAGE, imageVO);
        //set Pofile Image of the Deeder now
        Deeder deeder = ngoOffer.getDeed().getDeeder();
        String profileFile=deeder.getProfileFile();
        picType=profileFile.substring(profileFile.indexOf('.')+1);
        ImageVO imageVO2=new ImageVO(picType, deeder.getImage());
        session.setAttribute(GDFConstants.TEMP_IMAGE_2, imageVO2);
        
        
        ngoOfferComments = ngoBeanLocal.getNgoOfferComments(offerId);
        //Ngo Offer Likes now
        ngoOfferLikes = new ArrayList<>();
        ngoOfferLikes = ngoOffer.getNgoOfferLikes();
        ngoOfferLikesStr = String.valueOf(ngoOfferLikes.size());//For display on the front end.
        //Prepare for the Pop Up of the name of the 'likesBy'
        likesBy = "";
        if (ngoOfferLikes.size() > 0) {//exception handler in case there is no Like Yet.
            for (NgoOfferLike nOL : ngoOfferLikes) {
                likesBy = likesBy.concat(nOL.getLikeByName()).concat(",");
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

        LOGGER.log(Level.INFO, "NgoOffer loaded with ID:{0}, Deed: {1} and Deeder : {2}", new Object[]{ngoOffer.getId(), ngoOffer.getDeed().getId(), ngoOffer.getDeed().getDeeder().getId()});
        return "/view/ViewNgoOfferDetails?faces-redirect=true";
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
        int textLength = ngoOfferComment.getText().length();
        if (textLength > 1000) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Comment exceeds 1,000 chars.", "Comment exceeds 1,000 chars."));
            return null;
        }
        ngoOfferComment.setNgoOffer(ngoOffer);
        ngoOfferComment.setDate(LocalDateTime.now());
        /*LOGGER.warning("TEMPORARY CODE - STARTS");
        List<Access> accessL = accessBeanLocal.getAllAccess(20);
        int size = accessL.size();
        Random rand = new Random();
        int randIndex = rand.nextInt(size);
        access = accessL.get(randIndex);
        LOGGER.warning("TEMPORARY CODE - ENDS");*/
        ngoOfferComment.setPostedBy(access.getEmail());
        ngoOfferComment.setAccessType(access.getEntityType());
        ngoOffer.getNgoOfferComments().add(ngoOfferComment);
        LOGGER.log(Level.INFO, "Comment added :{0}", ngoOfferComment.getText());
        ngoBeanLocal.addNgoOfferComment(ngoOffer);
        ngoOfferComment = new NgoOfferComment();
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
        NgoOfferLike noLike = new NgoOfferLike();
        noLike.setNgoOffer(ngoOffer);
        /*LOGGER.warning("TEMPORARY CODE - STARTS");
        List<Access> accessL = accessBeanLocal.getAllAccess(20);
        int size = accessL.size();
        Random rand = new Random();
        int randIndex = rand.nextInt(size);
        access = accessL.get(randIndex);
        LOGGER.warning("TEMPORARY CODE - ENDS");*/
        noLike.setAccessType(access.getEntityType());
        noLike.setAccessId(access.getEntityId());
        noLike.setLikeByName(access.getName());
        noLike.setTime(LocalDateTime.now());
        noLike = ngoBeanLocal.addOfferLike(noLike);
        ngoOfferLikes.add(noLike);
        LOGGER.log(Level.INFO, "NgoOfferLike persisted with ID: {0}", noLike.getId());
        //refresh the Likes
        ngoOfferLikes = ngoBeanLocal.getNgoOfferLikes(ngoOffer.getId());
        ngoOfferLikesStr = String.valueOf(ngoOfferLikes.size());
        likesBy = "";
        for (NgoOfferLike nOL : ngoOfferLikes) {
            likesBy = likesBy.concat(nOL.getLikeByName()).concat(",");
        }
        int till = likesBy.lastIndexOf(",");
        likesBy = likesBy.substring(0, till);
        return null;
    }

    public void addCommentLike(int commentId) {
        int index = 0;
        for (NgoOfferComment noc : ngoOfferComments) {
            if (noc.getId() == commentId) {
                noc.setLikes(noc.getLikes() + 1);
                noc = ngoBeanLocal.addCommentLike(noc);
                ngoOfferComments.set(index, noc);//Since the Likes have been updated in the database, they must be reflected in memory as well.
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
        dl.setDeeder(ngoOffer.getDeed().getDeeder());
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
        List<DeederLike> deederLikes = ngoOffer.getDeed().getDeeder().getDeederLikes();
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

    public NgoOffer getNgoOffer() {
        return ngoOffer;
    }

    public void setNgoOffer(NgoOffer ngoOffer) {
        this.ngoOffer = ngoOffer;
    }

    public NgoOfferComment getNgoOfferComment() {
        return ngoOfferComment;
    }

    public void setNgoOfferComment(NgoOfferComment ngoOfferComment) {
        this.ngoOfferComment = ngoOfferComment;
    }

    public List<NgoOfferComment> getNgoOfferComments() {
        return ngoOfferComments;
    }

    public void setNgoOfferComments(List<NgoOfferComment> ngoOfferComments) {
        this.ngoOfferComments = ngoOfferComments;
    }

    public List<NgoOfferLike> getNgoOfferLikes() {
        return ngoOfferLikes;
    }

    public void setNgoOfferLikes(List<NgoOfferLike> ngoOfferLikes) {
        this.ngoOfferLikes = ngoOfferLikes;
    }

    public String getNgoOfferLikesStr() {
        return ngoOfferLikesStr;
    }

    public void setNgoOfferLikesStr(String ngoOfferLikesStr) {
        this.ngoOfferLikesStr = ngoOfferLikesStr;
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
