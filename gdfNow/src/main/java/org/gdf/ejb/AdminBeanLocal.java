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
import javax.ejb.Local;

/**
 *
 * @author root
 */
@Local
public interface AdminBeanLocal {

    public List<NgoCategory> getUnfonfirmedNgoCategories();
    
    public NgoCategory approveNgoCategory(NgoCategory category);
    
    public List<BusinessCategory> getUnfonfirmedBusinessCategories();
    
    public BusinessCategory approveBusinessCategory(BusinessCategory category);
    
    public List<GovernmentOrg> getUnconfirmedGovernmentOrgs();
    
    public GovernmentOrg approveGovernmentOrg(GovernmentOrg org);
    
    public List<DeedCategory> getUnconfirmedDeedCategories();
    
    public DeedCategory approveDeedCategory(DeedCategory dc);

    public List<Business> getUnconfirmedBusineses();

    public List<Government> getUnconfirmedGovernments();

    public List<Ngo> getUnconfirmedNgos();
    
    public void approveBusiness(Business business);
    
    public void approveGovernment(Government government);
    
    public void approveNgo(Ngo ngo);
    
}
