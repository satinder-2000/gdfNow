/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi;

import org.gdf.ejb.VisitorBeanLocal;
import org.gdf.model.Visitor;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author root
 */
@Named(value = "visitorHomeMBean")
@SessionScoped
public class VisitorHomeMBean implements Serializable {
    
    static final Logger LOGGER=Logger.getLogger(VisitorHomeMBean.class.getName());
    
    @Inject
    VisitorBeanLocal visitorBeanLocal;
    
    private boolean rememberMe;

    /**
     * Creates a new instance of VisitorHomeMBean
     */
    public VisitorHomeMBean() {
    }
    
    @PostConstruct
    public void init(){
        ExternalContext extCtx=FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest request=(HttpServletRequest) extCtx.getRequest();
        HttpServletResponse response=(HttpServletResponse) extCtx.getResponse();
        String ipAddress=request.getRemoteAddr();
        LOGGER.info("IpAggress in VisitorHomeMBean is "+ipAddress);
        boolean isSaved=visitorBeanLocal.performIPCheckIfSaved(ipAddress);
        try{
        if (isSaved){
            //redirect to index page
            LOGGER.info("ipAddress found in the database. Redirecting to index page..");
            //RequestDispatcher rd=request.getRequestDispatcher("index.xhtml");
            //rd.forward(request, response);
            extCtx.redirect("index.xhtml");
        }else{
            //redirect to home page
            LOGGER.info("ipAddress NOT found in the database. Redirecting to home page..");
            //RequestDispatcher rd=request.getRequestDispatcher("home.xhtml");
            //rd.forward(request, response);
            extCtx.redirect("home.xhtml");
        }
        }catch(Exception ex){
            LOGGER.severe(ex.getMessage());
        }
        
    }
    
    public String saveVisitor(){
        
        if (rememberMe){//If Visitor checks the Box, store the IP Address in the Database. The home page will not be generated and control will go to the index.xhtml.
            Visitor visitor = new Visitor();
            ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
            HttpServletRequest request = (HttpServletRequest) ctx.getRequest();
            String ipAddress = request.getRemoteAddr();
            visitor.setiPAddress(ipAddress);
            visitor.setTime(LocalDateTime.now());
            String contextPath=request.getContextPath();
            LOGGER.log(Level.INFO, "contextPath is {0}", contextPath);
            LOGGER.info("saveVisitor ipAddress is " + ipAddress);
            visitorBeanLocal.saveVisitor(visitor);
        }
        return "/index?faces-redirect=true";
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
    
    
    
    
    
    
    
}
