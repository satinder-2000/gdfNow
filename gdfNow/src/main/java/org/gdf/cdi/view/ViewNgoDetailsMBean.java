/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.view;

import org.gdf.ejb.NgoBeanLocal;
import org.gdf.model.Ngo;
import org.gdf.model.NgoOffer;
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
@Named(value = "viewNgoDetailsMBean")
@ViewScoped
public class ViewNgoDetailsMBean implements Serializable {
    
    static final Logger LOGGER=Logger.getLogger(ViewNgoDetailsMBean.class.getName());
    
    
    @Inject
    NgoBeanLocal ngoBeanLocal;
    
    Ngo ngo; 
   
    
    
    @PostConstruct
    public void init(){
        ExternalContext extContext=FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest request=(HttpServletRequest)extContext.getRequest();
        if (!request.getParameter("ngoId").isEmpty()){
            String ngoIdS=request.getParameter("ngoId");
            int ngoId=Integer.valueOf(ngoIdS);
            ngo=ngoBeanLocal.findNgoById(ngoId);
            HttpSession session=request.getSession();
            String fullFileName=ngo.getLogoFile();
            String imgType=fullFileName.substring(fullFileName.indexOf('.')+1);
            ImageVO imageVO=new ImageVO(imgType,ngo.getImage());
            session.setAttribute(GDFConstants.TEMP_IMAGE, imageVO);
            LOGGER.log(Level.INFO, "Ngo Loaded with ID:{0}", ngo.getId());
            
        }else throw new RuntimeException("No valid Parameter found");
    }

    public Ngo getNgo() {
        return ngo;
    }

    public void setNgo(Ngo ngo) {
        this.ngo = ngo;
    }

    
    
    
    
    
}
