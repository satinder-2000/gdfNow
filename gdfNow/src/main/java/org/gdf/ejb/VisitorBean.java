/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.ejb;

import org.gdf.model.Business;
import org.gdf.model.Deed;
import org.gdf.model.DeedCategory;
import org.gdf.model.Deeder;
import org.gdf.model.Government;
import org.gdf.model.Ngo;
import org.gdf.model.Visitor;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

/**
 *
 * @author root
 */
@Stateless
public class VisitorBean implements VisitorBeanLocal {
    
    static final Logger LOGGER=Logger.getLogger(VisitorBean.class.getName());
    
    @PersistenceContext(name = "gdfPU")
    EntityManager em;

    @Override
    public void saveVisitor(Visitor visitor) {
        try{
            em.persist(visitor);
            em.flush();
            LOGGER.log(Level.INFO, "Visitor from {0} saved", visitor.getiPAddress());
        }catch(Exception ex){
            LOGGER.log(Level.SEVERE, ex.getMessage());
        }
        
        
    }

    @Override
    public boolean performIPCheckIfSaved(String ipAddress) {
        TypedQuery vTq=em.createQuery("select v from Visitor v where v.iPAddress=?1", Visitor.class);
        vTq.setParameter(1, ipAddress);
        if(vTq.getResultList()!=null &&  !vTq.getResultList().isEmpty()){
            return true;
        }else{
            return false;
        }
    }

    @Override
    public List<Deeder> getAllDeeders(int startIndex, int size) {
        Query query = em.createQuery("select d from Deeder d",Deeder.class);
        query.setFirstResult(startIndex);
        query.setMaxResults(size);
        List<Deeder> deederList = query.getResultList();
        return deederList;
    }

    @Override
    public List<Deed> getAllDeeds(int startIndex, int size) {
        Query query = em.createQuery("select d from Deed d",Deed.class);
        query.setFirstResult(startIndex);
        query.setMaxResults(size);
        List<Deed> deedList = query.getResultList();
        return deedList;
    }

    @Override
    public int getAllDeedersCount() {
        Query deederCount=em.createQuery("select count(d) from Deeder d");//TRY select count(d) from Deeder d
        long count=(long)deederCount.getSingleResult();
        int toReturn=(int)count;
        LOGGER.info("getAllDeedersCount = "+toReturn);
        return toReturn;
    }

    @Override
    public int getAllDeedsCount() {
        Query deedCount=em.createQuery("select count(d) from Deed d");
        long count=(long)deedCount.getSingleResult();
        int toReturn=(int)count;
        LOGGER.info("getAllDeedsCount = "+toReturn);
        return toReturn;
    }

    @Override
    public int getAllBusinessCount() {
        Query businessCount=em.createQuery("select count(b) from Business b");
        long count=(long)businessCount.getSingleResult();
        int toReturn=(int)count;
        LOGGER.info("getAllBusinessCount = "+toReturn);
        return toReturn;
    }

    @Override
    public List<Business> getAllBusiness(int startIndex, int size) {
        Query query = em.createQuery("select b from Business b",Business.class);
        query.setFirstResult(startIndex);
        query.setMaxResults(size);
        List<Business> businessList = query.getResultList();
        return businessList;
    }

    @Override
    public int getAllGovernmentCount() {
        Query govtCount=em.createQuery("select count(g) from Government g");
        long count=(long)govtCount.getSingleResult();
        int toReturn=(int)count;
        LOGGER.info("getAllGovernmentCount = "+toReturn);
        return toReturn;
    }

    @Override
    public List<Government> getAllGovernments(int startIndex, int size) {
        Query query = em.createQuery("select g from Government g",Government.class);
        query.setFirstResult(startIndex);
        query.setMaxResults(size);
        List<Government> governmentList = query.getResultList();
        return governmentList;
    }

    @Override
    public int getAllNGOCount() {
        Query ngoCount=em.createQuery("select count(n) from Ngo n");
        long count=(long)ngoCount.getSingleResult();
        int toReturn=(int)count;
        LOGGER.info("getAllNGOCount = "+toReturn);
        return toReturn;
    }

    @Override
    public List<Ngo> getAllNGOs(int startIndex, int size) {
        Query query = em.createQuery("select n from Ngo n",Ngo.class);
        query.setFirstResult(startIndex);
        query.setMaxResults(size);
        List<Ngo> ngoList = query.getResultList();
        return ngoList;
    }

    @Override
    public int getAllDeedCategoryCount() {
        Query dcCount=em.createQuery("select count(dc) from DeedCategory dc");
        long count=(long)dcCount.getSingleResult();
        int toReturn=(int)count;
        LOGGER.info("getAllDeedCategoryCount = "+toReturn);
        return toReturn;
    }

    @Override
    public List<DeedCategory> getAllDeedCategories(int startIndex, int size) {
        Query query = em.createQuery("select dc from DeedCategory dc",DeedCategory.class);
        query.setFirstResult(startIndex);
        query.setMaxResults(size);
        List<DeedCategory> dcList = query.getResultList();
        return dcList;
        
    }

    
}
