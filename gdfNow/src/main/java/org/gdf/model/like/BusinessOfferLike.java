/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.model.like;

import org.gdf.model.BusinessOffer;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * @author satindersingh
 */
@Entity
@Table(name = "BUSINESS_OFFER_LIKE")
public class BusinessOfferLike extends Like {
    
    @ManyToOne
    @JoinColumn(name = "BUSINESS_OFFER_ID")
    private BusinessOffer businessOffer;

    public BusinessOffer getBusinessOffer() {
        return businessOffer;
    }

    public void setBusinessOffer(BusinessOffer businessOffer) {
        this.businessOffer = businessOffer;
    }
    
    
    
}
