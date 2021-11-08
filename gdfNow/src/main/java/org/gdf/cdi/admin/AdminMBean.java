/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.admin;

import org.gdf.ejb.AdminBeanLocal;
import org.gdf.ejb.ReferenceDataBeanLocal;
import org.gdf.model.Business;
import org.gdf.model.BusinessCategory;
import org.gdf.model.DeedCategory;
import org.gdf.model.Government;
import org.gdf.model.GovernmentOrg;
import org.gdf.model.Ngo;
import org.gdf.model.NgoCategory;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.time.LocalDateTime;
import java.util.logging.Level;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author root
 */
@Named(value = "adminMBean")
@ViewScoped
public class AdminMBean implements Serializable {
    
    static final Logger LOGGER=Logger.getLogger(AdminMBean.class.getName());
    
    @Inject
    ReferenceDataBeanLocal referenceDataBeanLocal;
    
    @Inject
    AdminBeanLocal adminBeanLocal;
    
    
    List<NgoCategory> ngoCategories;
    List<DeedCategory> deedCategories;
    List<BusinessCategory> businessCategories;
    List<GovernmentOrg> governmentOrgs;
    List<Business> businesses;
    List<Government> governments;
    List<Ngo> ngos;
    
    
    private String message;
    
    @PostConstruct
    public void init(){
        deedCategories=adminBeanLocal.getUnconfirmedDeedCategories();
        ngoCategories=adminBeanLocal.getUnfonfirmedNgoCategories();
        businessCategories=adminBeanLocal.getUnfonfirmedBusinessCategories();
        governmentOrgs=adminBeanLocal.getUnconfirmedGovernmentOrgs();
        businesses=adminBeanLocal.getUnconfirmedBusineses();
        governments=adminBeanLocal.getUnconfirmedGovernments();
        ngos=adminBeanLocal.getUnconfirmedNgos();
        
    }

    public List<NgoCategory> getNgoCategories() {
        
        return ngoCategories;
    }

    public void setNgoCategories(List<NgoCategory> ngoCategories) {
        this.ngoCategories = ngoCategories;
    }
    
    public void approveDeedCategory(){
        HttpServletRequest request= (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String categoryIdStr=request.getParameter("categoryId");
        int categoryId= Integer.parseInt(categoryIdStr);
        LOGGER.log(Level.INFO, "Category Id is {0}", categoryId);
        for (DeedCategory dC : deedCategories) {
            if(dC.getId()==categoryId){
                dC.setConfirmed(true);
                dC=adminBeanLocal.approveDeedCategory(dC);
                LOGGER.log(Level.INFO, "DeedCategory approved : {0}", dC.getType().concat(" ").concat(dC.getSubtype()).concat(" on ").concat(LocalDateTime.now().toString()));
            }
            
        }
        message= "Approved";
    }
    
    
    public void approveNgoCategory(){
        HttpServletRequest request= (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String categoryIdStr=request.getParameter("categoryId");
        int categoryId= Integer.parseInt(categoryIdStr);
        LOGGER.log(Level.INFO, "Category Id is {0}", categoryId);
        for (NgoCategory nC : ngoCategories) {
            if(nC.getId()==categoryId){
                nC.setConfirmed(true);
                nC=adminBeanLocal.approveNgoCategory(nC);
                LOGGER.log(Level.INFO, "NgoCategory approved : {0}", nC.getType().concat(" ").concat(nC.getSubtype()).concat(" on ").concat(LocalDateTime.now().toString()));
            }
            
        }
        message= "Approved";
    }
    
    public void approveBusinessCategory(){
        HttpServletRequest request= (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String categoryIdStr=request.getParameter("categoryId");
        int categoryId= Integer.parseInt(categoryIdStr);
        LOGGER.log(Level.INFO, "Category Id is {0}", categoryId);
        for (BusinessCategory bC : businessCategories) {
            if(bC.getId()==categoryId){
                bC.setConfirmed(true);
                bC=adminBeanLocal.approveBusinessCategory(bC);
                LOGGER.log(Level.INFO, "BusinessCategory approved : {0}", bC.getType().concat(" ").concat(bC.getSubtype()).concat(" on ").concat(LocalDateTime.now().toString()));
            }
        }
        
        message= "Approved";
    }
    
    public void approveGovernmentOrg(){
        HttpServletRequest request= (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String orgIdStr=request.getParameter("orgId");
        int orgId= Integer.parseInt(orgIdStr);
        LOGGER.log(Level.INFO, "GovernmentOrg Id is {0}", orgId);
        for (GovernmentOrg go : governmentOrgs) {
            if(go.getId()==orgId){
                go.setConfirmed(true);
                go=adminBeanLocal.approveGovernmentOrg(go);
                LOGGER.log(Level.INFO, "GovernmentOrg approved : {0}", go.getMinistry().concat(" ").concat(go.getDepartment()).concat(" on ").concat(LocalDateTime.now().toString()));
            }
        }
        message= "Approved";
        
    }
    
    
    public void approveBusiness(){
        HttpServletRequest request= (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String businessIdStr=request.getParameter("businessId");
        int businessId= Integer.parseInt(businessIdStr);
        LOGGER.log(Level.INFO, "Business Id is {0}", businessId);
        for (Business b : businesses) {
            if(b.getId()==businessId){
                b.setConfirmed(true);
                adminBeanLocal.approveBusiness(b);
                LOGGER.log(Level.INFO, "Business approved : {0}", b.getName().concat(" on ").concat(LocalDateTime.now().toString()));
            }
        }
        message= "Approved";
     }
    
    public void approveGovernment(){
        HttpServletRequest request= (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String governmentIdStr=request.getParameter("governmentId");
        int governmentId= Integer.parseInt(governmentIdStr);
        LOGGER.log(Level.INFO, "Government Id is {0}", governmentId);
        for (Government g : governments) {
            if(g.getId()==governmentId){
                g.setConfirmed(true);
                adminBeanLocal.approveGovernment(g);
                LOGGER.log(Level.INFO, "Government approved : {0}", g.getOfficeName().concat(" on ").concat(LocalDateTime.now().toString()));
            }
            
        }
        message= "Approved";
     }
    
    public void approveNgo(){
        HttpServletRequest request= (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String ngoIdStr=request.getParameter("ngoId");
        int ngoId= Integer.parseInt(ngoIdStr);
        LOGGER.log(Level.INFO, "Ngo Id is {0}", ngoId);
        for (Ngo n : ngos) {
            if(n.getId()==ngoId){
                n.setConfirmed(true);
                adminBeanLocal.approveNgo(n);
                LOGGER.log(Level.INFO, "Ngo approved : {0}", n.getName().concat(" on ").concat(LocalDateTime.now().toString()));
            }
            
        }
        message= "Approved";
     }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<DeedCategory> getDeedCategories() {
        return deedCategories;
    }

    public void setDeedCategories(List<DeedCategory> deedCategories) {
        this.deedCategories = deedCategories;
    }

    public List<BusinessCategory> getBusinessCategories() {
        return businessCategories;
    }

    public void setBusinessCategories(List<BusinessCategory> businessCategories) {
        this.businessCategories = businessCategories;
    }

    public List<GovernmentOrg> getGovernmentOrgs() {
        return governmentOrgs;
    }

    public void setGovernmentOrgs(List<GovernmentOrg> governmentOrgs) {
        this.governmentOrgs = governmentOrgs;
    }

    public List<Business> getBusinesses() {
        return businesses;
    }

    public void setBusinesses(List<Business> businesses) {
        this.businesses = businesses;
    }

    public List<Government> getGovernments() {
        return governments;
    }

    public void setGovernments(List<Government> governments) {
        this.governments = governments;
    }

    public List<Ngo> getNgos() {
        return ngos;
    }

    public void setNgos(List<Ngo> ngos) {
        this.ngos = ngos;
    }
    
    
    
    
    
}
