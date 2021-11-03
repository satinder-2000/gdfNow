/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.deed;

import org.gdf.ejb.AccessBeanLocal;
import org.gdf.ejb.DeedBeanLocal;
import org.gdf.ejb.DeederBeanLocal;
import org.gdf.ejb.NgoBeanLocal;
import org.gdf.model.Access;
import org.gdf.model.AccessType;
import org.gdf.model.Deed;
import org.gdf.model.like.DeedLike;
import org.gdf.util.GDFConstants;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.gdf.model.comment.DeedComment;
import org.gdf.model.Deeder;
import org.gdf.model.Ngo;
import org.gdf.model.like.DeederLike;
import org.gdf.util.ImageVO;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.view.ViewScoped;

/**
 *
 * @author satindersingh
 */
@Named(value = "deedDetailsMBean")
@ViewScoped
public class DeedDetailsMBean implements Serializable {
    
    static final Logger LOGGER=Logger.getLogger(DeedDetailsMBean.class.getName());
    
    @Inject
    DeedBeanLocal deedBeanLocal;
    
    @Inject
    DeederBeanLocal deederBeanLocal;
    
    @Inject
    NgoBeanLocal ngoBeanLocal;
    
    private int deedId;
    
    private String deedIdStr;
    
    
    public int getDeedId() {
        return deedId;
    }

    public void setDeedId(int deedId) {
        this.deedId = deedId;
    }

    public String getDeedIdStr() {
        return deedIdStr;
    }

    public void setDeedIdStr(String deedIdStr) {
        this.deedIdStr = deedIdStr;
    }
    
    
    private Deed deed;
    
    private DeedComment deedComment;
    
    private List<DeedComment> deedComments;
    
    private List<DeedLike> deedLikes;
    
    //String documentServer;
    //String deederDocPath;
    
    private String likesBy;
    private String deedLikesStr;
    
    private String deederLikesStr;
    private String deederLikesBy;
    
    private String ngoLikesStr;
    private String ngoLikesBy;

    public Deed getDeed() {
        return deed;
    }

    public void setDeed(Deed deed) {
        this.deed = deed;
    }

    public DeedComment getDeedComment() {
        return deedComment;
    }

    public void setDeedComment(DeedComment deedComment) {
        this.deedComment = deedComment;
    }
    
    
    
    @PostConstruct
    public void init(){
        ExternalContext extContext=FacesContext.getCurrentInstance().getExternalContext();
        //documentServer=extContext.getInitParameter("DocumentServer");
        //deederDocPath=extContext.getInitParameter("DeederDocPath");
        deedComments=new ArrayList<>();
        deedLikes=new ArrayList<>();
        //Initialise a new Comment as well.
        deedComment=new DeedComment();
        LOGGER.info("DeedDetailsMBean initialised");
        loadDeedDetails();
    }   
    
    
    
    public String loadDeedDetails(){
        LOGGER.info("Inside loadDeedDetails");
        HttpServletRequest request=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        deedIdStr=request.getParameter("deedId");
        LOGGER.log(Level.INFO, "Loading Deed Details for ID :{0}", deedIdStr);
        deedId=Integer.parseInt(deedIdStr);
        deed=deedBeanLocal.getDeedDetails(deedId);
        //Comments and Likes on Deed
        deedComments=deed.getDeedComments();
        deedLikes=deed.getDeedLikes();
        if (deedLikes!=null && !deedLikes.isEmpty()){
            deedLikesStr=Integer.toString(deedLikes.size());
            //Prepare for the Pop Up of the name of the 'likesBy'
            likesBy="";
            for (DeedLike dl : deedLikes) {
                likesBy=likesBy.concat(dl.getLikeByName()).concat(",");
            }
            int till = likesBy.lastIndexOf(",");
            likesBy = likesBy.substring(0, till);
        }else{
            deedLikesStr="0";
            likesBy="";
        }
        String toReturn=null;
        if (deed.getDeeder()!=null){
            assignDeederToDeed();
            toReturn = "/view/ViewDeederDeedDetails.xhtml?faces-redirect=true";
        }else if (deed.getNgo()!=null){
            assignNgoToDeed();
            toReturn = "/view/ViewNgoDeedDetails.xhtml?faces-redirect=true";
        }
        return toReturn;
        
    }
    
    
    public String addComment(){
        //Validate the size of the comment first. Max allowed is 1,000 chars
        int textLength=deedComment.getText().length();
        if (textLength>1000){
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Comment exceeds 1,000 chars.", "Comment exceeds 1,000 chars."));
            return null;
        }
        deedComment.setDeed(deed);
        deedComment.setDate(LocalDateTime.now());
        HttpServletRequest request=(HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=request.getSession();
        Access access=(Access) session.getAttribute(GDFConstants.ACCESS);
        deedComment.setPostedBy(access.getEmail());
        AccessType accessType= access.getAccessType();
        deedComment.setAccessType(accessType);
        deedComment.setAccessId(access.getEntityId());
        deed.getDeedComments().add(deedComment);
        LOGGER.log(Level.INFO, "Comment added :{0}", deedComment.getText());
        deedBeanLocal.addComment(deed);
        deedComment=new DeedComment();
        FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Thanks for posting your comment. It will be published soon.", "Thanks for posting your comment. It will be published soon."));
        return null;
    }
    
    public void addLike(){
        
        DeedLike dLike=new DeedLike();
        dLike.setDeed(deed);
        HttpServletRequest request=(HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=request.getSession();
        Access access=null;
        Object accessOb=session.getAttribute(GDFConstants.ACCESS);
        if (accessOb!=null){//A logged In user is liking the Deed.
            access = (Access) accessOb;
            dLike.setAccessType(access.getAccessType());
            dLike.setAccessId(access.getEntityId());
            dLike.setLikeByName(access.getName());
            dLike.setTime(LocalDateTime.now());
            dLike = deedBeanLocal.addDeedLike(dLike);
            //deedLikes.add(dLike);
            LOGGER.log(Level.INFO, "DeedLike persisted with ID: {0}", dLike.getId());
            //refresh the Likes
            deedLikes = deedBeanLocal.getDeedLikes(deed.getId());
            deedLikesStr = String.valueOf(deedLikes.size());
            likesBy = "";
            for (DeedLike gOL : deedLikes) {
                likesBy = likesBy.concat(gOL.getLikeByName()).concat(",");
            }
            int till = likesBy.lastIndexOf(",");
            likesBy = likesBy.substring(0, till);
        }else{
            LOGGER.finest("Anonymous Like on Deed ignored");
        }
    }
    
    
    public void addCommentLike(int commentId){
        int index=0;
        for (DeedComment dc : deedComments) {
            if(dc.getId()==commentId){
                dc.setLikes(dc.getLikes()+1);
                dc=deedBeanLocal.addCommentLike(dc);
                deedComments.set(index, dc);//Since the Likes have been updated in the database, they must be reflected in memory as well.
                break;
            }
            index++;
            
        }
    }
    
    
    public void addDeederLike(){
        DeederLike dl=new DeederLike();
        dl.setDeeder(deed.getDeeder());
        HttpServletRequest request=(HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=request.getSession();
        Access access=null;
        Object accessOb=session.getAttribute(GDFConstants.ACCESS);
        if (accessOb!=null){//A logged In user is liking the Deed.
            access = (Access) accessOb;
            dl.setAccessType(access.getAccessType());
            dl.setAccessId(access.getEntityId());
            dl.setLikeByName(access.getName());
            dl.setTime(LocalDateTime.now());
            dl = deederBeanLocal.addDeederLike(dl);
            LOGGER.log(Level.INFO, "DeedLike persisted with ID: {0}", dl.getId());
            //refresh the Likes
            List<DeederLike> deederLikes=deed.getDeeder().getDeederLikes();
            deederLikesStr = Integer.toString(deederLikes.size());
            //Prepare for the Pop Up of the name of the 'likesBy'
            deederLikesBy = "";
            for (DeederLike dlk : deederLikes) {
                deederLikesBy = deederLikesBy.concat(dlk.getLikeByName()).concat(",");
            }
            int till = deederLikesBy.lastIndexOf(",");
            deederLikesBy = deederLikesBy.substring(0, till);
            LOGGER.log(Level.INFO, "Deeder updated with new DeederLike ID: {0}", dl.getId());
        }else{
            LOGGER.finest("Anonymous Like on Deeder ignored");
        }
    }

    public List<DeedComment> getDeedComments() {
        return deedComments;
    }

    public void setDeedComments(List<DeedComment> deedComments) {
        this.deedComments = deedComments;
    }

    public List<DeedLike> getDeedLikes() {
        return deedLikes;
    }

    public void setDeedLikes(List<DeedLike> deedLikes) {
        this.deedLikes = deedLikes;
    }

    public String getDeedLikesStr() {
        return deedLikesStr;
    }

    public void setDeedLikesStr(String deedLikesStr) {
        this.deedLikesStr = deedLikesStr;
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

    private void assignDeederToDeed() {
        Deeder deeder=deederBeanLocal.getDeeder(deed.getDeeder().getId());//reextracting from the Database to retrive Deeder Likes as well.
        deed.setDeeder(deeder);
        HttpServletRequest request=(HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String profileFile=deeder.getProfileFile();
        String picType=profileFile.substring(profileFile.indexOf('.')+1);
        ImageVO imageVO=new ImageVO(picType, deeder.getImage());
        HttpSession session=request.getSession(true);
        session.setAttribute(GDFConstants.TEMP_IMAGE, imageVO);
        
        //Deeder Likes
        List<DeederLike> deederLikes=deeder.getDeederLikes();
        if (deederLikes!=null && !deederLikes.isEmpty()){
            deederLikesStr=Integer.toString(deederLikes.size());
            //Prepare for the Pop Up of the name of the 'likesBy'
            deederLikesBy="";
            for (DeederLike dl : deederLikes) {
                deederLikesBy=deederLikesBy.concat(dl.getLikeByName()).concat(",");
            }
            int till = deederLikesBy.lastIndexOf(",");
            deederLikesBy = deederLikesBy.substring(0, till);
        }else{
            deederLikesStr="0";
            deederLikesBy="";
        }
    }

    private void assignNgoToDeed() {
        Ngo ngo=ngoBeanLocal.findNgoById(deed.getNgo().getId());
        deed.setNgo(ngo);
        HttpServletRequest request=(HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String profileFile=ngo.getLogoFile();
        String picType=profileFile.substring(profileFile.indexOf('.')+1);
        ImageVO imageVO=new ImageVO(picType, ngo.getImage());
        HttpSession session=request.getSession(true);
        session.setAttribute(GDFConstants.TEMP_IMAGE, imageVO);
        
        //TODO NGO Likes
        /*List<DeederLike> deederLikes=deeder.getDeederLikes();
        if (deederLikes!=null && !deederLikes.isEmpty()){
            deederLikesStr=Integer.toString(deederLikes.size());
            //Prepare for the Pop Up of the name of the 'likesBy'
            deederLikesBy="";
            for (DeederLike dl : deederLikes) {
                deederLikesBy=deederLikesBy.concat(dl.getLikeByName()).concat(",");
            }
            int till = deederLikesBy.lastIndexOf(",");
            deederLikesBy = deederLikesBy.substring(0, till);
        }else{
            deederLikesStr="0";
            deederLikesBy="";
        }*/
    }

    public String getNgoLikesStr() {
        return ngoLikesStr;
    }

    public void setNgoLikesStr(String ngoLikesStr) {
        this.ngoLikesStr = ngoLikesStr;
    }

    public String getNgoLikesBy() {
        return ngoLikesBy;
    }

    public void setNgoLikesBy(String ngoLikesBy) {
        this.ngoLikesBy = ngoLikesBy;
    }
    
    
   
    
    
    
    
}
