/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.model.like;


import org.gdf.model.Deed;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * @author satindersingh
 */
@Entity
@Table(name = "DEED_LIKE")
public class DeedLike extends Like {
    
    @ManyToOne
    @JoinColumn(name = "DEED_ID")
    private Deed deed;

    public Deed getDeed() {
        return deed;
    }

    public void setDeed(Deed deed) {
        this.deed = deed;
    }
    
    
    
    

    
    
}
