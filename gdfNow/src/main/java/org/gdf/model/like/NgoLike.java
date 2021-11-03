/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.model.like;

import org.gdf.model.Ngo;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * @author root
 */
@Entity
@Table(name = "NGO_LIKE")
public class NgoLike extends Like {
    
    @ManyToOne
    @JoinColumn(name = "NGO_ID")
    private Ngo ngo;

    public Ngo getNgo() {
        return ngo;
    }

    public void setNgo(Ngo ngo) {
        this.ngo = ngo;
    }
    
    
    
}
