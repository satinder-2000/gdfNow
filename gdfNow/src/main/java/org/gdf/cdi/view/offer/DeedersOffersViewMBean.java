/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.view.offer;

import org.gdf.ejb.BusinessBeanLocal;
import org.gdf.ejb.GovernmentBeanLocal;
import org.gdf.ejb.NgoBeanLocal;
import org.gdf.model.Access;
import org.gdf.model.Business;
import org.gdf.model.BusinessOffer;
import org.gdf.model.Government;
import org.gdf.model.GovernmentOffer;
import org.gdf.model.Ngo;
import org.gdf.model.NgoOffer;
import org.gdf.util.GDFConstants;
import org.gdf.util.ImageVO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author root
 */
@RequestScoped
@Named(value = "deedersOffersViewMBean")
public class DeedersOffersViewMBean {
    
    static final Logger LOGGER= Logger.getLogger(DeedersOffersViewMBean.class.getName());
    
    @Inject
    BusinessBeanLocal businessBeanLocal;
    
    @Inject
    GovernmentBeanLocal governmentBeanLocal;
    
    @Inject
    NgoBeanLocal ngoBeanLocal;
    
    List<BusinessOffer> businessOffers;
    List<GovernmentOffer> governmentOffers;
    List<NgoOffer> ngoOffers;
    
    Map<String, ImageVO> imageMap;
    
    @PostConstruct
    public void init(){
        imageMap=new HashMap<>();
        ExternalContext extCtx=FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest request=(HttpServletRequest) extCtx.getRequest();
        HttpSession session=request.getSession();//A Deeder would be logged in.
        Access access=(Access) session.getAttribute(GDFConstants.ACCESS);
        int deederId=access.getEntityId();
        businessOffers=businessBeanLocal.getBusinessOffersForDeeder(deederId);
        LOGGER.log(Level.INFO, "Business: Total of {0} found", businessOffers.size());
        assignBusinessLogoLocations(businessOffers);
        governmentOffers=governmentBeanLocal.getGovernmentOffersForDeeder(deederId);
        assignGovernmentLogoLocations(governmentOffers);
        LOGGER.log(Level.INFO, "Government: Total of {0} found", governmentOffers.size());
        ngoOffers=ngoBeanLocal.getNgoOffersForDeeder(deederId);
        assignNgoLogoLocations(ngoOffers);
        session.setAttribute(GDFConstants.TEMP_IMAGE_MAP, imageMap);
        LOGGER.log(Level.INFO, "NGO: Total of {0} found", ngoOffers.size());
        
    }
    
    private void assignBusinessLogoLocations(List<BusinessOffer> businessOffers) {
        for (BusinessOffer businessOffer : businessOffers) {
            Business b=businessOffer.getBusiness();
            //b.setLogoURL(documentServer + businessDocPath + b.getEmail() + "/" + b.getLogoFile());
            String logoFile=b.getLogoFile();
            String type=logoFile.substring(logoFile.indexOf('.')+1);
            ImageVO imgVO=new ImageVO(type,b.getImage());
            imageMap.put("BO"+b.getId(), imgVO);
        }
    }
    
    private void assignGovernmentLogoLocations(List<GovernmentOffer> governmentOffers) {
        for (GovernmentOffer governmentOffer : governmentOffers) {
            Government g=governmentOffer.getGovernment();
            String logoFile=g.getLogoFile();
            String type=logoFile.substring(logoFile.indexOf('.')+1);
            ImageVO imgVO=new ImageVO(type,g.getImage());
            imageMap.put("GO"+g.getId(), imgVO);
        }
    }
    
    private void assignNgoLogoLocations(List<NgoOffer> ngoOffers) {
        for (NgoOffer ngoOffer : ngoOffers) {
            Ngo n=ngoOffer.getNgo();
            String logoFile=n.getLogoFile();
            String type=logoFile.substring(logoFile.indexOf('.')+1);
            ImageVO imgVO=new ImageVO(type,n.getImage());
            imageMap.put("NO"+n.getId(), imgVO);
        }
    }

    public List<BusinessOffer> getBusinessOffers() {
        return businessOffers;
    }

    public void setBusinessOffers(List<BusinessOffer> businessOffers) {
        this.businessOffers = businessOffers;
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
