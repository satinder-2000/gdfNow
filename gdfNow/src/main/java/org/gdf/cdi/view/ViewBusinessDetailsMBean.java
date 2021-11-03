/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.view;

import org.gdf.ejb.BusinessBeanLocal;
import org.gdf.model.Business;
import org.gdf.util.GDFConstants;
import org.gdf.util.ImageVO;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author root
 */
@Named(value = "viewBusinessDetailsMBean")
@ViewScoped
public class ViewBusinessDetailsMBean implements Serializable {
    
    static final Logger LOGGER=Logger.getLogger(ViewBusinessDetailsMBean.class.getName());
    
    @Inject
    BusinessBeanLocal businessBeanLocal;
    
    Business business;
    
    @PostConstruct
    public void init(){
        ExternalContext extContext=FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest request=(HttpServletRequest)extContext.getRequest();
        if (!request.getParameter("businessId").isEmpty()){
            String businessIdS=request.getParameter("businessId");
            int businessId=Integer.valueOf(businessIdS);
            business=businessBeanLocal.findBusinessById(businessId);
            HttpSession session=request.getSession();
            String fullFileName=business.getLogoFile();
            String imgType=fullFileName.substring(fullFileName.indexOf('.')+1);
            ImageVO imageVO=new ImageVO(imgType,business.getImage());
            session.setAttribute(GDFConstants.TEMP_IMAGE, imageVO);
            LOGGER.log(Level.INFO, "Business Loaded with ID:{0}", business.getId());
        }else throw new RuntimeException("No valid Parameter found");
    }

    public Business getBusiness() {
        return business;
    }

    public void setBusiness(Business business) {
        this.business = business;
    }

}
