/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.ejb;

import org.gdf.model.Business;
import org.gdf.model.BusinessCategory;
import org.gdf.model.BusinessOffer;
import org.gdf.model.Deed;
import org.gdf.model.Deeder;
import org.gdf.model.comment.BusinessOfferComment;
import org.gdf.model.like.BusinessOfferLike;
import org.gdf.model.like.DeederLike;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author satindersingh
 */
@Local
public interface BusinessBeanLocal {
    
    public Business createBusiness(Business business);

    public Business findBusinessById(Integer businessId);
    
    public Business findBusinessByEmail(String email);

    public void amendBusiness(Business business);

    public BusinessOffer createBusinessOffer(BusinessOffer businessOffer, Business business, Deed deed);
    
    public BusinessOffer getBusinessOfferTree(int offerId);
    
    public List<BusinessOffer> getAllBusinessOffers(int businessId);
    
    public BusinessOffer getBusinessOffer(int offerId);
    
    public List<BusinessOffer> getBusinessOffersOnDeed(int deedId);
    
    public List<BusinessOffer> getBusinessOffersForDeeder(int deederId);
    
    public List<BusinessOffer> getPublicListingBusinessOffers();
    
    List<BusinessOfferComment> getBusinessOfferComments(int offerId);
    
    public void addBusinessOfferComment(BusinessOffer businessOffer);
    
    public List<BusinessOfferLike> getBusinessOfferLikes(int offerId);
    
    public List<Deeder> getBusinessRewardedDeeders(int businessId);
    
    public BusinessOfferLike addOfferLike(BusinessOfferLike businessOfferLike);

    public BusinessOfferComment addCommentLike(BusinessOfferComment boc);

    public DeederLike addDeederLike(DeederLike dl);

    public void addBusinessCategory(BusinessCategory businessCategory);
    
    
    
    
    
}
