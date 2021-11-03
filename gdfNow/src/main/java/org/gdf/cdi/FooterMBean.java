/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

/**
 *
 * @author root
 */
@Named(value = "footerMBean")
@RequestScoped
public class FooterMBean {
    
    public String needAbout(){
        return "info/About?faces-redirect=true";
    }
    
    public String needContactAndHelp(){
        return "info/ContactAndHelp?faces-redirect=true";
    }
    
    public String needFeedbackSuggestion(){
        return "info/FeedbackSuggestion?faces-redirect=true";
    }
    
    public String needHowItWorks(){
        return "info/HowItWorks?faces-redirect=true";
    }
    
    public String needTermsAndConditions(){
        return "info/TermsAndConditions?faces-redirect=true";
    }
    
    public String needFAQ(){
        return "info/faq?faces-redirect=true";
    }
    
}
