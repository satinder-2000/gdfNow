/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.view;

import org.gdf.ejb.GovernmentBeanLocal;
import org.gdf.model.Government;
import org.gdf.model.GovernmentOffer;
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
@Named(value = "viewGovernmentDetailsMBean")
@ViewScoped
public class ViewGovernmentDetailsMBean implements Serializable {
    
    static final Logger LOGGER=Logger.getLogger(ViewGovernmentDetailsMBean.class.getName());
    
    @Inject
    GovernmentBeanLocal governmentBeanLocal;
    
    private Government government;
    
    @PostConstruct
    public void init(){
         ExternalContext extContext=FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest request=(HttpServletRequest)extContext.getRequest();
        if (!request.getParameter("governmentId").isEmpty()){
            String governmentIdS=request.getParameter("governmentId");
            int governmentId=Integer.valueOf(governmentIdS);
            government=governmentBeanLocal.findGovernmentById(governmentId);
            HttpSession session=request.getSession();
            String fullFileName=government.getLogoFile();
            String imgType=fullFileName.substring(fullFileName.indexOf('.')+1);
            ImageVO imageVO=new ImageVO(imgType,government.getImage());
            session.setAttribute(GDFConstants.TEMP_IMAGE, imageVO);
            LOGGER.log(Level.INFO, "Government Loaded with ID:{0}", government.getId());
        }else throw new RuntimeException("No valid Parameter found");
    }

    public Government getGovernment() {
        return government;
    }

    public void setGovernment(Government government) {
        this.government = government;
    }

 }
