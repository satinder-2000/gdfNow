/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.view;

import org.gdf.ejb.ActivityRecorderBeanLocal;
import org.gdf.ejb.BusinessBeanLocal;
import org.gdf.ejb.DeedBeanLocal;
import org.gdf.ejb.DeederBeanLocal;
import org.gdf.ejb.GovernmentBeanLocal;
import org.gdf.ejb.NgoBeanLocal;
import org.gdf.model.Activity;
import org.gdf.model.ActivityType;
import org.gdf.model.Business;
import org.gdf.model.BusinessOffer;
import org.gdf.model.Deed;
import org.gdf.model.Deeder;
import org.gdf.model.Government;
import org.gdf.model.GovernmentOffer;
import org.gdf.model.Ngo;
import org.gdf.model.NgoOffer;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author satindersingh
 */
@Named (value = "activityMBean")
@ApplicationScoped
public class ViewActivityMBean implements Serializable {
    
    static final Logger LOGGER=Logger.getLogger(ViewActivityMBean.class.getName());
    
    @Inject
    DeedBeanLocal deedBeanLocal;
    @Inject
    DeederBeanLocal deederBeanLocal;
    @Inject
    BusinessBeanLocal businessBeanLocal;
    @Inject
    GovernmentBeanLocal governmentBeanLocal;
    @Inject
    NgoBeanLocal ngoBeanLocal;
    
    Deeder deeder;
    Business business;
    Government government;
    Ngo ngo;
    BusinessOffer businessOffer;
    GovernmentOffer governmentOffer;
    NgoOffer ngoOffer;
    Deed deed;
    
    private String redirect;
    
    
  
    
    
    private List<Activity> activities;
    
    private Activity activity;
    
    @Inject
    ActivityRecorderBeanLocal activityRecorderBeanLocal;
    
    @PostConstruct
    public void init(){
        activities=activityRecorderBeanLocal.getActivityStack();;
    }

    public List<Activity> getActivities() {
        activities=activityRecorderBeanLocal.getActivityStack();
        return activities;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }

    public String redirect() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String activityType = request.getParameter("activityType");
        String entityIdStr = request.getParameter("entityId");
        int entityId = Integer.parseInt(entityIdStr);
        ActivityType actType = ActivityType.valueOf(activityType);
        switch (actType) {
            case DEED: {
                redirect = "/view/ViewDeederDeedDetails?faces-redirect=true&deedId="+entityId;
                break;
            }
            case DEED_NGO: {
                redirect = "/view/ViewNgoDeedDetails?faces-redirect=true&deedId="+entityId;
                break;
            }
            case DEEDER: {
                redirect = "/view/ViewDeederDetails?faces-redirect=true&deederId="+entityId;
                break;
            }
            case BUSINESS: {
                redirect = "/view/ViewBusinessDetails?faces-redirect=true&businessId="+entityId;
                break;
            }
            case GOVERNMENT: {
                redirect = "/view/ViewGovernmentDetails?faces-redirect=true&governmentId="+entityId;
                break;
            }
            case NGO: {
                redirect = "/view/ViewNgoDetails?faces-redirect=true&ngoId="+entityId;
                break;
            }
            case BUSINESS_OFFER: {
                redirect = "/view/ViewBusinessOfferDetails?faces-redirect=true&offerId="+entityId;
                break;
            }
            case GOVERNMENT_OFFER: {
                redirect = "/view/ViewGovernmentOfferDetails?faces-redirect=true&offerId="+entityId;
                break;
            }
            case NGO_OFFER: {
                redirect = "/view/ViewNgoOfferDetails?faces-redirect=true&offerId="+entityId;
                break;
            }
        }
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public Deeder getDeeder() {
        return deeder;
    }

    public void setDeeder(Deeder deeder) {
        this.deeder = deeder;
    }

    public Business getBusiness() {
        return business;
    }

    public void setBusiness(Business business) {
        this.business = business;
    }

    public Government getGovernment() {
        return government;
    }

    public void setGovernment(Government government) {
        this.government = government;
    }

    public Ngo getNgo() {
        return ngo;
    }

    public void setNgo(Ngo ngo) {
        this.ngo = ngo;
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

    public Deed getDeed() {
        return deed;
    }

    public void setDeed(Deed deed) {
        this.deed = deed;
    }
    
    
    
    
    
}
