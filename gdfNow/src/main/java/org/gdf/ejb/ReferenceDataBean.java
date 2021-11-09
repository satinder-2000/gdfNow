/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.ejb;

import org.gdf.event.NgoCategoryEvent;
import org.gdf.model.BusinessCategory;
import org.gdf.model.Country;
import org.gdf.model.DeedCategory;
import org.gdf.model.EmailTemplate;
import org.gdf.model.EmailTemplateType;
import org.gdf.model.GovernmentOrg;
import org.gdf.model.NgoCategory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Observes;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.gdf.model.EmailMessage;

/**
 *
 * @author satindersingh
 */
@Singleton
@Startup
public class ReferenceDataBean implements ReferenceDataBeanLocal {
    
    final static Logger LOGGER = Logger.getLogger(ReferenceDataBean.class.getName());
    
    @PersistenceContext(name = "gdfPU")
    EntityManager em;

    private List<Country> countries = null;
    private List<DeedCategory> deedCategories = null;
    private List<BusinessCategory> businessCategories;
    private List<NgoCategory> ngoCategories;
    private List<GovernmentOrg> governmentOrgs;

    Map<String, String> deedCategoryMap;
    Map<String, String> businessCategoriesMap;
    Map<String, String> ngoCategoriesMap;
    Map<String, String> governmentOrgsMap;
    Map<String, List<String>> governmentMinistriesCtryCdMap;
    //Map<EmailTemplateType,String> emailTemplateMap;
    

    private List<String> deedCategoryTypes;

    private List<String> businessCategoryTypes;

    private List<String> ngoCategoryTypes;

    

    @PostConstruct
    public void init() {
        try {
            LOGGER.log(Level.INFO, "Loading Reference Data");
            TypedQuery<Country> ctQ = em.createQuery("select c from Country c", Country.class);
            countries = ctQ.getResultList();

            //Deed Categories
            TypedQuery<DeedCategory> dcTq = em.createQuery("select dc from DeedCategory dc", DeedCategory.class);
            deedCategories = dcTq.getResultList();
            deedCategoryMap = new HashMap<>();
            for (DeedCategory deedCategory : deedCategories) {
                deedCategoryMap.put(deedCategory.getType(), deedCategory.getSubtype());
            }
            Set<String> dcTypes = deedCategoryMap.keySet();
            deedCategoryTypes = new ArrayList(dcTypes);

            //Business Categories
            TypedQuery<BusinessCategory> bcTq = em.createQuery("select bc from BusinessCategory bc  where bc.confirmed=true", BusinessCategory.class);
            businessCategories = bcTq.getResultList();
            businessCategoriesMap = new HashMap<>();
            for (BusinessCategory businessCategory : businessCategories) {
                businessCategoriesMap.put(businessCategory.getType(), businessCategory.getSubtype());
            }
            Set<String> busTypes = businessCategoriesMap.keySet();
            businessCategoryTypes = new ArrayList(busTypes);

            //NGO Categories            
            TypedQuery<NgoCategory> ncTq = em.createQuery("select nc from NgoCategory nc where nc.confirmed=true", NgoCategory.class);
            ngoCategories = ncTq.getResultList();
            ngoCategoriesMap = new HashMap<>();
            for (NgoCategory ngoCategory : ngoCategories) {
                ngoCategoriesMap.put(ngoCategory.getType(), ngoCategory.getSubtype());
            }
            Set<String> ngoTypes = ngoCategoriesMap.keySet();
            ngoCategoryTypes = new ArrayList(ngoTypes);

            //Government Orgs By Country Code.
            TypedQuery<Object[]> goTq = em.createQuery("select distinct go.countryCode, go.ministry from GovernmentOrg go", Object[].class);
            
            List<Object[]> resultList=goTq.getResultList();
            governmentMinistriesCtryCdMap=new HashMap<>(resultList.size());
            for (Object[] objects : resultList) {
                String ctryCode=(String)objects[0];
                if(governmentMinistriesCtryCdMap.containsKey(ctryCode)){
                   List<String> ministries=governmentMinistriesCtryCdMap.get(ctryCode);
                   ministries.add((String)objects[1]);
                }else{
                    List<String> ministries=new ArrayList();
                    ministries.add((String)objects[1]);
                    governmentMinistriesCtryCdMap.put(ctryCode, ministries);
                }
            }
            /*resultList.listIterator();
            governmentOrgs = goTq.getResultList();
            governmentOrgsMap = new HashMap<>();
            for (GovernmentOrg governmentOrg : governmentOrgs) {
                governmentOrgsMap.put(governmentOrg.getMinistry(), governmentOrg.getDepartment());
            }
            Set<String> governmentMinistriesSet = governmentOrgsMap.keySet();
            governmentMinistries = new ArrayList(governmentMinistriesSet);*/
            
            //Email Templates
            /*TypedQuery<EmailTemplate> eTQ=em.createQuery("select et from EmailTemplate et", EmailTemplate.class);
            List<EmailTemplate> result=eTQ.getResultList();
            emailTemplateMap=new HashMap<>();
            for (EmailTemplate eT : result) {
                emailTemplateMap.put(eT.getEmailTemplateType(), eT.getFile());
            }*/
            
            LOGGER.log(Level.INFO, "Loading Reference Data Completed");
        } catch (Exception ex) {
            LOGGER.severe(ex.getMessage());
        } 

    }

    @Override
    public List<Country> getCountries() {
        return countries;
    }
    
    

    @Override
    public List<DeedCategory> getDeedCategories() {
        return deedCategories;
    }

    @Override
    public List<String> getDeedCategoryTypes() {
        return deedCategoryTypes;
    }

    @Override
    public List<String> getDeedCategorySubTypes(String type) {
        TypedQuery<String> dcTq = em.createQuery("select distinct  dc.subtype from DeedCategory dc where dc.type=?1", String.class);
        dcTq.setParameter(1, type);
        return dcTq.getResultList();
    }

    @Override
    public List<BusinessCategory> getBusinessCategories() {
        return businessCategories;
    }

    @Override
    public List<String> getBusinessCategoryTypes() {
        return businessCategoryTypes;
    }

    @Override
    public List<String> getBusinessCategorySubTypes(String type) {
        TypedQuery<String> bcTq = em.createQuery("select distinct  bc.subtype from BusinessCategory bc where bc.type=?1", String.class);
        bcTq.setParameter(1, type);
        return bcTq.getResultList();
    }

    @Override
    public List<NgoCategory> getNgoCategories() {
         return ngoCategories;
    }

    @Override
    public List<String> getNgoCategoryTypes() {
         return ngoCategoryTypes;
    }

    @Override
    public List<String> getNgoCategorySubTypes(String type) {
        TypedQuery<String> ncSTq = em.createQuery("select distinct  nc.subtype from NgoCategory nc where nc.type=?1", String.class);
        ncSTq.setParameter(1, type);
        return ncSTq.getResultList();
    }

    @Override
    public List<GovernmentOrg> getGovernmentOrgs() {
        return governmentOrgs;
    }

    @Override
    public List<String> getGovernmentMinistries(String countryCode) {
        return governmentMinistriesCtryCdMap.get(countryCode);
    }

    @Override
    public List<String> getGovernmentDepartments(String countryCode, String ministry) {
        TypedQuery<String> deptTq = em.createQuery("select distinct go.department from GovernmentOrg go where go.countryCode=?1 and go.ministry=?2", String.class);
        deptTq.setParameter(1, countryCode);
        deptTq.setParameter(2, ministry);
        return deptTq.getResultList();
    }

    /*@Override
    public String getEmailTemplate(EmailTemplateType type) {
        return emailTemplateMap.get(type.toString());
    }*/

    /*@Override
    public Map<EmailTemplateType,String> getEmailTemplatesMap() {
        return emailTemplateMap;
    }*/

    private void reloadNgoCategories() {
        //NGO Categories            
        TypedQuery<NgoCategory> ncTq = em.createQuery("select nc from NgoCategory nc where nc.confirmed=true", NgoCategory.class);
        ngoCategories = ncTq.getResultList();
        ngoCategoriesMap = new HashMap<>();
        for (NgoCategory ngoCategory : ngoCategories) {
            ngoCategoriesMap.put(ngoCategory.getType(), ngoCategory.getSubtype());
        }
        Set<String> ngoTypes = ngoCategoriesMap.keySet();
        ngoCategoryTypes = new ArrayList(ngoTypes);
        
    }

    @Override
    public List<NgoCategory> getUnapprovedNgoCategories() {
        TypedQuery<NgoCategory> ncTq = em.createQuery("select nc from NgoCategory nc where nc.confirmed=false", NgoCategory.class);
        return ncTq.getResultList();
    }

    @Override
    public GovernmentOrg getGovernmentOrg(String countryCode, String ministry, String department) {
        TypedQuery goTq=em.createQuery("select go from GovernmentOrg go where go.countryCode=?1 and go.ministry=?2 and go.department=?3", GovernmentOrg.class);
        goTq.setParameter(1, countryCode);
        goTq.setParameter(2, ministry);
        goTq.setParameter(3, department);
        GovernmentOrg govtOrg=(GovernmentOrg)goTq.getSingleResult();
        return govtOrg;
    }

    @Override
    public Country getCountry(String code) {
        TypedQuery q= em.createQuery("select c from Country c where c.code=?1", Country.class);
        q.setParameter(1, code);
        Country c= (Country)q.getSingleResult();
        return c;
        
    }

    @Override
    public List<GovernmentOrg> getGovernmentOrgs(String countryCode) {
        TypedQuery goTq=em.createQuery("select go from GovernmentOrg go where go.countryCode=?1", GovernmentOrg.class);
        goTq.setParameter(1, countryCode);
        List<GovernmentOrg> govtOrgs=goTq.getResultList();
        return govtOrgs;
    }

    @Override
    public void addDeedCategory(DeedCategory dc) {
        em.persist(dc);
        em.flush();
        LOGGER.info("New DeedCategory created with ID:"+dc.getId());
        //Refresh Deed Categories now
        TypedQuery<DeedCategory> dcTq = em.createQuery("select dc from DeedCategory dc", DeedCategory.class);
        deedCategories = dcTq.getResultList();
        deedCategoryMap = new HashMap<>();
        for (DeedCategory deedCategory : deedCategories) {
            deedCategoryMap.put(deedCategory.getType(), deedCategory.getSubtype());
        }
        Set<String> dcTypes = deedCategoryMap.keySet();
        deedCategoryTypes = new ArrayList(dcTypes);

    }

    @Override
    public HashMap<String, List<EmailMessage>> getEmailMessages() {
        TypedQuery<EmailMessage> tQ = em.createQuery("select em from EmailMessage em", EmailMessage.class);
        List<EmailMessage> resultSet = tQ.getResultList();
        HashMap<String, List<EmailMessage>> map = new HashMap();
        for (EmailMessage em : resultSet) {
            System.out.println(em.toString());
            if (map.containsKey(em.getTemplate())) {
                map.get(em.getTemplate()).add(em);
            } else {
                map.put(em.getTemplate(), new ArrayList());
            }
        }
        return map;
    }
}
