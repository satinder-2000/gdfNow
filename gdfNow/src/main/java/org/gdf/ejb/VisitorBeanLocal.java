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
import javax.ejb.Local;

/**
 *
 * @author root
 */
@Local
public interface VisitorBeanLocal {
    
    
    
    public void saveVisitor(Visitor visitor);

    public boolean performIPCheckIfSaved(String ipAddress);
    
    public int getAllDeedersCount();
    
    public int getAllDeedsCount();
    
    public List<Deeder> getAllDeeders(int startIndex, int size);
    
    public List<Deed> getAllDeeds(int startIndex, int size);
    
    public int getAllBusinessCount();
    
    public List<Business> getAllBusiness(int startIndex, int size);
    
    public int getAllGovernmentCount();
    
    public List<Government> getAllGovernments(int startIndex, int size);
    
    public int getAllNGOCount();
    
    public List<Ngo> getAllNGOs(int startIndex, int size);
    
    public int getAllDeedCategoryCount();
    
    public List<DeedCategory> getAllDeedCategories(int startIndex, int size);
    
    
    
    
}
