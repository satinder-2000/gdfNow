/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.model.like;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.gdf.model.Deeder;

/**
 *
 * @author satindersingh
 */
@Entity
@Table(name = "DEEDER_LIKE")
public class DeederLike extends Like {
    
    @ManyToOne
    @JoinColumn(name = "DEEDER_ID")
    private Deeder deeder;

    public Deeder getDeeder() {
        return deeder;
    }

    public void setDeeder(Deeder deeder) {
        this.deeder = deeder;
    }
    
    
    
}
