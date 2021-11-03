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
import java.util.List;
import java.util.Map;
import javax.ejb.Local;
import javax.enterprise.event.Observes;

/**
 *
 * @author satindersingh
 */
@Local
public interface ReferenceDataBeanLocal {
    
    public List<Country> getCountries();
    
    public Country getCountry(String code);
    
    public List<DeedCategory> getDeedCategories();
    
    public List<String> getDeedCategoryTypes();
    
    public List<String> getDeedCategorySubTypes(String type);

    public List<BusinessCategory> getBusinessCategories();
    
    public List<String> getBusinessCategoryTypes();
    
    public List<String> getBusinessCategorySubTypes(String type);
    
    public List<NgoCategory> getNgoCategories();
    
    public List<String> getNgoCategoryTypes();
    
    public List<String> getNgoCategorySubTypes(String type);
    
    public List<GovernmentOrg> getGovernmentOrgs();
    
    public List<GovernmentOrg> getGovernmentOrgs(String countryCode);
    
    public List<String> getGovernmentMinistries(String countryCode);
    
    public List<String> getGovernmentDepartments(String countryCode, String ministry);
    
    public GovernmentOrg getGovernmentOrg(String countryCode, String ministry, String department);
    
    public String getEmailTemplate(EmailTemplateType type);
    
    public Map<EmailTemplateType,String> getEmailTemplatesMap();

    public void addDeedCategory(DeedCategory dc);

    public List<NgoCategory> getUnapprovedNgoCategories();

    
    
}
