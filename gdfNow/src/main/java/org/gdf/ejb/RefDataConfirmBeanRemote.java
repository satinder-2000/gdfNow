/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.ejb;

import org.gdf.model.NgoCategory;
import javax.ejb.Remote;


/**
 *
 * @author root
 */
@Remote
public interface RefDataConfirmBeanRemote {
    public void confirmNgoCategory(NgoCategory ngoCategory);
    
}
