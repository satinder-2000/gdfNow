/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi;

import org.gdf.model.Access;
import org.gdf.model.AccessType;
import org.gdf.util.GDFConstants;
import org.gdf.util.GDFUtilWeb;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author satindersingh
 */
//Uncommented the below annotation after it was found that the User after nominating a Deed ot the Deeder, could not get back to Home Page. BUT WHY WERE THESE COMMENTED OUT AT FIRST PLACE??
@Named(value = "homeMBean")
@SessionScoped
public class HomeMBean implements Serializable {
    
    static final Logger LOGGER=Logger.getLogger(HomeMBean.class.getName());
    Set<String> acceptedCodes;
    
    @PostConstruct
    public void init(){
        acceptedCodes=new HashSet<>();
        acceptedCodes.add(Locale.UK.getCountry());
        acceptedCodes.add(Locale.US.getCountry());
        Locale locale=new Locale("hi", "IN"); 
        acceptedCodes.add(locale.getCountry());
        LOGGER.info("HomeMBean initialised and "+acceptedCodes.size()+" Locales loaded");
        
    }
    
    
    public void redirectToHome(){
        String homePage=null;
        HttpServletRequest request= (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=request.getSession(true);
        //Object typeOb=session.getAttribute(GDFConstants.ACCESS_TYPE);
        if (session.getAttribute(GDFConstants.ACCESS)==null){
           homePage = request.getContextPath() + "home?faces-redirect=true"; 
        }else{
          try {
            Access access = (Access) session.getAttribute(GDFConstants.ACCESS);
            AccessType acType = access.getAccessType();
            String reqCtxPath=request.getContextPath();
            LOGGER.info("request.getContextPath() is "+reqCtxPath);
            switch (acType) {
                case USER: {
                    homePage = request.getContextPath()+"/home/UserHome.xhtml?faces-redirect=true";
                    break;
                }
                case DEEDER: {
                    homePage = request.getContextPath()+"/home/DeederHome.xhtml?faces-redirect=true";
                    break;
                }
                case BUSINESS: {
                    homePage = request.getContextPath()+"/home/BusinessHome.xhtml?faces-redirect=true";
                    break;
                }
                case GOVERNMENT: {
                    homePage = request.getContextPath()+"/home/GovernmentHome.xhtml?faces-redirect=true";
                    break;
                }
                case NGO: {
                    homePage = request.getContextPath()+"/home/NgoHome.xhtml?faces-redirect=true";
                    break;
                }
                default: {
                    homePage = request.getContextPath() + "home.xhtml?faces-redirect=true";
                    break;
                }
            }
        } catch (Exception ex) {
            //ex.printStackTrace();
            LOGGER.warning(ex.getMessage());
            homePage = request.getContextPath() + "error.xhtml?faces-redirect=true";
        }  
        }
        
        HttpServletResponse response= (HttpServletResponse)FacesContext.getCurrentInstance().getExternalContext().getResponse();
        try {
            LOGGER.log(Level.INFO, "Redirecting to:{0}", request.getContextPath());
            response.sendRedirect(homePage);
        } catch (IOException ex) {
            Logger.getLogger(HomeMBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void redirectToSiteHome(){
        
        ExternalContext ctx=FacesContext.getCurrentInstance().getExternalContext();
        
        HttpServletRequest request= (HttpServletRequest)ctx.getRequest();
        HttpServletResponse response= (HttpServletResponse)ctx.getResponse();
        try {
            LOGGER.log(Level.INFO, "Redirecting to:{0}", request.getContextPath());
            response.sendRedirect(request.getContextPath());
        } catch (IOException ex) {
            Logger.getLogger(HomeMBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public String getCountryCode(){
        String toReturn=null;
        ExternalContext ctx=FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest request= (HttpServletRequest)ctx.getRequest();
        Locale locale= request.getLocale();
        String countryCode=locale.getCountry();
        boolean codeExists=acceptedCodes.contains(countryCode);
        if (codeExists){
            toReturn =countryCode;
        }else{
            toReturn = "er";
        }
        return toReturn;
        
    }
    
    @Deprecated
    public String getClientCountryCode(){
        HttpServletRequest request=(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session=request.getSession(true);
        String ctryCd= (String) session.getAttribute(GDFUtilWeb.CLIENT_COUNTRY_CODE);
        LOGGER.info("User Country Code in HomeMBean is :"+ctryCd);
        return ctryCd;
    }
    
}
