/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.event;

import org.gdf.model.NgoCategory;

/**
 *
 * @author root
 */
public class NgoCategoryEvent {
    
    private NgoCategory ngoCategory;
    
    public NgoCategoryEvent(NgoCategory ngoCategory){
        this.ngoCategory=ngoCategory;
    }

    public NgoCategory getNgoCategory() {
        return ngoCategory;
    }

    public void setNgoCategory(NgoCategory ngoCategory) {
        this.ngoCategory = ngoCategory;
    }
    
    
    
}
