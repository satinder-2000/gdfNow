/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.view.offer;

import org.gdf.ejb.DeedBeanLocal;
import org.gdf.ejb.BusinessBeanLocal;
import org.gdf.ejb.GovernmentBeanLocal;
import org.gdf.ejb.NgoBeanLocal;
import org.gdf.model.BusinessOffer;
import org.gdf.model.Deed;
import org.gdf.model.GovernmentOffer;
import org.gdf.model.NgoOffer;
import org.gdf.util.GDFConstants;
import org.gdf.util.ImageVO;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author satindersingh
 */
@Named(value = "offersOnDeedMBean")
@SessionScoped
public class OffersOnDeedMBean implements Serializable {
    
    @Inject
    BusinessBeanLocal businessBeanLocal;
    
    @Inject
    GovernmentBeanLocal governmentBeanLocal;
    
    @Inject
    NgoBeanLocal ngoBeanLocal;
    
    @Inject
    DeedBeanLocal deedBeanLocal;
    
    static final Logger LOGGER=Logger.getLogger(OffersOnDeedMBean.class.getName());
    
    private List<BusinessOffer> businessOffers;
    
    private List<GovernmentOffer> governmentOffers;
    
    private List<NgoOffer> ngoOffers;
    
    private Deed deed;
    
     Map<String, ImageVO> imageMap;
    
    @PostConstruct
    public void init(){
        LOGGER.info("public class OffersOnDeedMBean initialised");
        
    }
    
    public String loadOffersForDeed(){
        imageMap=new HashMap<>();
        HttpServletRequest request= (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String deedIdStr= request.getParameter("deedId");
        int deedId=Integer.parseInt(deedIdStr);
        deed= deedBeanLocal.getDeedWithDeeder(deedId);
        businessOffers= businessBeanLocal.getBusinessOffersOnDeed(deedId);
        for (BusinessOffer bo : businessOffers) {
            byte[] busImg=bo.getBusiness().getImage();
            String fileName=bo.getBusiness().getLogoFile();
            String imgType=fileName.substring(fileName.indexOf('.')+1);
            ImageVO vo=new ImageVO(imgType,busImg);
            imageMap.put("BO"+bo.getBusiness().getId(), vo);
        }
        LOGGER.log(Level.INFO, "{0} BusinessOffers loaded for Deed Id:{1}", new Object[]{businessOffers.size(), deedId});
        governmentOffers=governmentBeanLocal.getGovernmentOffersOnDeed(deedId);
        for (GovernmentOffer go : governmentOffers) {
            byte[] govImg=go.getGovernment().getImage();
            String fileName=go.getGovernment().getLogoFile();
            String imgType=fileName.substring(fileName.indexOf('.')+1);
            ImageVO vo=new ImageVO(imgType,govImg);
            imageMap.put("GO"+go.getGovernment().getId(), vo);
        }
        
        LOGGER.log(Level.INFO, "{0} GovernmentOffers loaded for Deed Id:{1}", new Object[]{governmentOffers.size(), deedId});
        ngoOffers=ngoBeanLocal.getNgoOffersOnDeed(deedId);
        for (NgoOffer no : ngoOffers) {
            byte[] ngoImg=no.getNgo().getImage();
            String fileName=no.getNgo().getLogoFile();
            String imgType=fileName.substring(fileName.indexOf('.')+1);
            ImageVO vo=new ImageVO(imgType,ngoImg);
            imageMap.put("NO"+no.getNgo().getId(), vo);
            
        }
        LOGGER.log(Level.INFO, "{0} NgoOffers loaded for Deed Id:{1}", new Object[]{ngoOffers.size(), deedId});
        HttpSession session=request.getSession();
        session.setAttribute(GDFConstants.TEMP_IMAGE_MAP, imageMap);
        
        return "/view/offer/OffersOnDeed?faces-redirect=true";
    }
    
    public List<BusinessOffer> getBusinessOffers() {
        return businessOffers;
    }

    public void setBusinessOffers(List<BusinessOffer> businessOffers) {
        this.businessOffers = businessOffers;
    }

    public Deed getDeed() {
        return deed;
    }

    public void setDeed(Deed deed) {
        this.deed = deed;
    }

    public List<GovernmentOffer> getGovernmentOffers() {
        return governmentOffers;
    }

    public void setGovernmentOffers(List<GovernmentOffer> governmentOffers) {
        this.governmentOffers = governmentOffers;
    }

    public List<NgoOffer> getNgoOffers() {
        return ngoOffers;
    }

    public void setNgoOffers(List<NgoOffer> ngoOffers) {
        this.ngoOffers = ngoOffers;
    }
    
    
    
    
    
    
    
}
