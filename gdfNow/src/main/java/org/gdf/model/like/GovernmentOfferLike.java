/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.model.like;

import javax.persistence.Entity;
import javax.persistence.Table;
import org.gdf.model.GovernmentOffer;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 *
 * @author satindersingh
 */
@Entity
@Table(name = "GOVERNMENT_OFFER_LIKE")
public class GovernmentOfferLike extends Like {
    
    
    @ManyToOne
    @JoinColumn(name = "GOVERNMENT_OFFER_ID")
    private GovernmentOffer governmentOffer;

    public GovernmentOffer getGovernmentOffer() {
        return governmentOffer;
    }

    public void setGovernmentOffer(GovernmentOffer governmentOffer) {
        this.governmentOffer = governmentOffer;
    }
    
    
    
    
}
