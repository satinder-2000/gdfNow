/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.info;

import org.gdf.ejb.FooterServiceBeanLocal;
import org.gdf.model.Feedback;
import org.gdf.util.GDFConstants;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author root
 */

@Named(value = "feedbackMBean")
@SessionScoped
public class FeedbackMBean implements Serializable {
    
    private static final Logger LOGGER=Logger.getLogger(FeedbackMBean.class.getName());
    
    @Inject
    FooterServiceBeanLocal footerServiceBeanLocal;
    
    Feedback feedback;
    
    @PostConstruct
    public void init(){
        feedback=new Feedback();
        LOGGER.info("Feedback initialised");
    }
    
    public String validateForm(){
        String toReturn=null;
        String name=feedback.getName();
        String email=feedback.getEmail();
        String feedbackText=feedback.getFeedBackText();
        //Start validating them now
        if (name.isEmpty()){
           FacesContext.getCurrentInstance().addMessage("name", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Name not provided.", "Name not provided.")); 
        }else if (name.length()<2 || name.length()>75){
           FacesContext.getCurrentInstance().addMessage("name", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Name: 2-75 chars expected.", "Name: 2-75 chars expected.")); 
        }
        //Validate Email now
        String emailRegEx = GDFConstants.EMAIL_REGEX;
        Pattern pEmail = Pattern.compile(emailRegEx);
        Matcher mP = pEmail.matcher(email);
        boolean matches = mP.find();
        if (!matches) {
            FacesContext.getCurrentInstance().addMessage("email", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Email", "The Email is not valid"));
        }
        //Validate the Text now
        if (feedbackText.isEmpty()){
            FacesContext.getCurrentInstance().addMessage("feedbackText", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Feedback Text not provided.", "Feedback Text not provided."));
        }else if (feedbackText.length()<10 || name.length()>1000){
           FacesContext.getCurrentInstance().addMessage("feedbackText", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Feedback Text: 10-1000 chars expected.", "Name: 10-1000 chars expected.")); 
        }
        
        //Finally send to the next Page
        List<FacesMessage> msgs= FacesContext.getCurrentInstance().getMessageList();
        if (msgs!=null && msgs.size()>0){
            toReturn =null;
        }else{
            toReturn="FeedbackSuggestionConfirm?faces-redirect=true";
        }
        return toReturn;
        
    }
    
    public String amendFeedback(){
        return "FeedbackSuggestion?faces-redirect=true";
    }
    
    public String submitFeedback(){
        feedback.setDatetime(LocalDateTime.now());
        feedback=footerServiceBeanLocal.saveFeedback(feedback);
        LOGGER.log(Level.INFO, "Feedback persisted with ID {0}. Will refresh the Entity now", feedback.getId());
        //Reset the Feedback now
        feedback=new Feedback();
        FacesContext.getCurrentInstance().addMessage("null", new FacesMessage(FacesMessage.SEVERITY_INFO, "Your Feedback has been submitted.", "Your Feedback has been submitted."));
        return null;
    }

    public Feedback getFeedback() {
        return feedback;
    }

    public void setFeedback(Feedback feedback) {
        this.feedback = feedback;
    }
    
    
    
}
