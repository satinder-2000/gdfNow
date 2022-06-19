/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.view.offer;


import org.gdf.ejb.BusinessBeanLocal;
import org.gdf.ejb.GovernmentBeanLocal;
import org.gdf.ejb.NgoBeanLocal;
import org.gdf.model.AccessType;
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
import java.util.Set;
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
 * @author satindersingh
 */
@RequestScoped
@Named(value = "offersViewMBean")
public class OffersViewMBean {
    
    static final Logger LOGGER= Logger.getLogger(OffersViewMBean.class.getName());
    
    @Inject
    BusinessBeanLocal businessBeanLocal;
    
    @Inject
    GovernmentBeanLocal governmentBeanLocal;
    
    @Inject
    NgoBeanLocal ngoBeanLocal;
    
    List<BusinessOffer> businessOffers;
    List<GovernmentOffer> governmentOffers;
    List<NgoOffer> ngoOffers;
    
    BusinessOffer businessOffer;
    
    GovernmentOffer governmentOffer;
    
    NgoOffer ngoOffer;
    
    Map<String, ImageVO> imageMap;
    
    //String documentServer;
    //String businessDocPath;
    //String governmentDocPath;
    //String ngoDocPath;
    
    ExternalContext extCtx;
    
    
    @PostConstruct
    public void init(){
        extCtx=FacesContext.getCurrentInstance().getExternalContext();
        //documentServer= extCtx.getInitParameter("DocumentServer");
        //businessDocPath=extCtx.getInitParameter("BusinessDocPath");
        //governmentDocPath=extCtx.getInitParameter("GovernmentDocPath");
        //ngoDocPath=extCtx.getInitParameter("NgoDocPath");
        imageMap=new HashMap<>();
        businessOffers=businessBeanLocal.getPublicListingBusinessOffers();
        LOGGER.log(Level.INFO, "Business: Total of {0} found", businessOffers.size());
        assignBusinessLogoLocations(businessOffers);
        governmentOffers=governmentBeanLocal.getPublicListingGovernmentOffers();
        assignGovernmentLogoLocations(governmentOffers);
        LOGGER.log(Level.INFO, "Government: Total of {0} found", governmentOffers.size());
        ngoOffers=ngoBeanLocal.getPublicListingNgoOffers();
        assignNgoLogoLocations(ngoOffers);
        HttpServletRequest request= (HttpServletRequest) extCtx.getRequest();
        HttpSession session=request.getSession(true);
        session.setAttribute(GDFConstants.TEMP_IMAGE_MAP, imageMap);
        LOGGER.log(Level.INFO, "NGO: Total of {0} found", ngoOffers.size());
        
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
    
    
    
    public String generateDetails(){
       int offerId=0;
       String offeror=null;
       Map<String, String> reqMap= FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
       Set<String> keyS= reqMap.keySet();
        for (String string : keyS) {
           System.out.println(string);
        }
        for (String key : keyS) {
            if (key.contains("offerId")){
               String offerIdStr=(String) reqMap.get(key);
               offerId=Integer.parseInt(offerIdStr);
               break;
            }
        }
        for (String key : keyS) {
            if (key.contains("offerror")){
               offeror=(String) reqMap.get(key);
               break;
            }
        }
        AccessType ofr= AccessType.valueOf(offeror);
        switch (ofr){
            case BUSINESS : {
                return businessOfferDetails(offerId);
                
            }
            case GOVERNMENT:{
                return governmentOfferDetails(offerId);
            }
            case NGO :{
                return ngoOfferDetails(offerId);
            }
        }
        
        return null;
    }

    private String businessOfferDetails(int offerId) {
        businessOffer= businessBeanLocal.getBusinessOffer(offerId);
        return "BusinessOffer";
    }

    private String governmentOfferDetails(int offerId) {
        governmentOffer=governmentBeanLocal.getGovernmentOffer(offerId);
        return "GovernmentOffer";
    }

    private String ngoOfferDetails(int offerId) {
        ngoOffer=ngoBeanLocal.getNgoOffer(offerId);
        return "NgoOffer";
    }

    public BusinessOffer getBusinessOffer() {
        return businessOffer;
    }

    public void setBusinessOffer(BusinessOffer businessOffer) {
        this.businessOffer = businessOffer;
    }

    public GovernmentOffer getGovernmentOffer() {
        return governmentOffer;
    }

    public void setGovernmentOffer(GovernmentOffer governmentOffer) {
        this.governmentOffer = governmentOffer;
    }

    public NgoOffer getNgoOffer() {
        return ngoOffer;
    }

    public void setNgoOffer(NgoOffer ngoOffer) {
        this.ngoOffer = ngoOffer;
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
}
