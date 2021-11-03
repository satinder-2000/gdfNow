/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.model.like;

import org.gdf.model.NgoOffer;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * @author satindersingh
 */
@Entity
@Table(name = "NGO_OFFER_LIKE")
public class NgoOfferLike extends Like {
    
    @ManyToOne
    @JoinColumn(name = "NGO_OFFER_ID")
    private NgoOffer ngoOffer;

    public NgoOffer getNgoOffer() {
        return ngoOffer;
    }

    public void setNgoOffer(NgoOffer ngoOffer) {
        this.ngoOffer = ngoOffer;
    }
    
    
    
    
}
