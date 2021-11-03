/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.ejb;

import org.gdf.model.Business;
import org.gdf.model.BusinessCategory;
import org.gdf.model.DeedCategory;
import org.gdf.model.Government;
import org.gdf.model.GovernmentOrg;
import org.gdf.model.Ngo;
import org.gdf.model.NgoCategory;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

/**
 *
 * @author root
 */
@Stateless
public class AdminBean implements AdminBeanLocal {
    
    static final Logger LOGGER=Logger.getLogger(AdminBean.class.getName());
    
    @PersistenceContext(name = "gdfPU")
    EntityManager em;

    @Override
    public List<NgoCategory> getUnfonfirmedNgoCategories() {
        TypedQuery tQ=em.createQuery("select nc from NgoCategory nc where nc.confirmed=false", NgoCategory.class);
        List<NgoCategory> result= tQ.getResultList();
        LOGGER.log(Level.INFO, "Number of unconfirmed NGO categories: {0}", result.size());
        return result;
    }

    @Override
    public NgoCategory approveNgoCategory(NgoCategory category) {
        if (!category.isConfirmed()){
            category.setConfirmed(true);
        }
        category= em.merge(category);
        em.flush();
        LOGGER.log(Level.INFO, "New NgoCategory approved {0}", category.getType().concat(" ").concat(category.getSubtype()));
        return category;
    }

    @Override
    public List<BusinessCategory> getUnfonfirmedBusinessCategories() {
        TypedQuery tQ=em.createQuery("select bc from BusinessCategory bc where bc.confirmed=false", BusinessCategory.class);
        List<BusinessCategory> result= tQ.getResultList();
        LOGGER.log(Level.INFO, "Number of unconfirmed BusinessCategories: {0}", result.size());
        return result;
    }

    @Override
    public BusinessCategory approveBusinessCategory(BusinessCategory category) {
        if (!category.isConfirmed()){
            category.setConfirmed(true);
        }
        category= em.merge(category);
        em.flush();
        LOGGER.log(Level.INFO, "New BusinessCategory approved {0}", category.getType().concat(" ").concat(category.getSubtype()));
        return category;
    }

    @Override
    public List<GovernmentOrg> getUnconfirmedGovernmentOrgs() {
        TypedQuery tQ=em.createQuery("select go from GovernmentOrg go where go.confirmed=false", GovernmentOrg.class);
        List<GovernmentOrg> result= tQ.getResultList();
        LOGGER.log(Level.INFO, "Number of unconfirmed GovernmentOrs: {0}", result.size());
        return result;
    }

    @Override
    public GovernmentOrg approveGovernmentOrg(GovernmentOrg org) {
        if (!org.isConfirmed()){
            org.setConfirmed(true);
        }
        org= em.merge(org);
        em.flush();
        LOGGER.log(Level.INFO, "New GovernmentOrg approved {0}", org.getMinistry().concat(" ").concat(org.getDepartment()));
        return org;
    }

    @Override
    public List<DeedCategory> getUnconfirmedDeedCategories() {
        TypedQuery tQ=em.createQuery("select dc from DeedCategory dc where dc.confirmed=false", DeedCategory.class);
        List<DeedCategory> result= tQ.getResultList();
        LOGGER.log(Level.INFO, "Number of unconfirmed DeedCategory: {0}", result.size());
        return result;
    }

    @Override
    public DeedCategory approveDeedCategory(DeedCategory dc) {
        if (!dc.isConfirmed()){
            dc.setConfirmed(true);
        }
        dc= em.merge(dc);
        em.flush();
        LOGGER.log(Level.INFO, "New DeedCategory approved {0}", dc.getType().concat(" ").concat(dc.getSubtype()));
        return dc;
    }

    @Override
    public List<Business> getUnconfirmedBusineses() {
        TypedQuery tQ=em.createQuery("select b from Business b where b.confirmed=false", Business.class);
        List<Business> result= tQ.getResultList();
        LOGGER.log(Level.INFO, "Number of unconfirmed Business: {0}", result.size());
        return result;
    }

    @Override
    public List<Government> getUnconfirmedGovernments() {
        TypedQuery tQ=em.createQuery("select g from Government g where g.confirmed=false", Government.class);
        List<Government> result= tQ.getResultList();
        LOGGER.log(Level.INFO, "Number of unconfirmed Government: {0}", result.size());
        return result;
    }

    @Override
    public List<Ngo> getUnconfirmedNgos() {
        TypedQuery tQ=em.createQuery("select n from Ngo n where n.confirmed=false", Ngo.class);
        List<Ngo> result= tQ.getResultList();
        LOGGER.log(Level.INFO, "Number of unconfirmed Ngo: {0}", result.size());
        return result;
    }

    @Override
    public void approveBusiness(Business business) {
        if (!business.isConfirmed()) business.setConfirmed(true);
        business=em.merge(business);
        em.flush();
        LOGGER.log(Level.INFO, "Business confirmed ID: {0}", business.getId());
    }

    @Override
    public void approveGovernment(Government government) {
        if (!government.isConfirmed()) government.setConfirmed(true);
        government=em.merge(government);
        em.flush();
        LOGGER.log(Level.INFO, "Government confirmed ID: {0}", government.getId());
    }

    @Override
    public void approveNgo(Ngo ngo) {
        if (!ngo.isConfirmed()) ngo.setConfirmed(true);
        ngo=em.merge(ngo);
        em.flush();
        LOGGER.log(Level.INFO, "Ngo confirmed ID: {0}", ngo.getId());
    }

    
}
