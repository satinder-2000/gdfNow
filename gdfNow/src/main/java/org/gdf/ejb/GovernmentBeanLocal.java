/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.ejb;

import org.gdf.model.Deed;
import org.gdf.model.Deeder;
import org.gdf.model.Government;
import org.gdf.model.GovernmentOffer;
import org.gdf.model.GovernmentOrg;
import org.gdf.model.comment.GovernmentOfferComment;
import org.gdf.model.like.DeederLike;
import org.gdf.model.like.GovernmentOfferLike;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author satindersingh
 */
@Local
public interface GovernmentBeanLocal {

    public Government findGovernmentById(Integer governmentId);

    public Government createGovernment(Government government);

    public void amendGovernment(Government government);
    
    public GovernmentOffer createGovernmentOffer(GovernmentOffer governmentOffer, Government government, Deed deed);
    
    public GovernmentOffer getGovernmentOffer(int offerId);
    
    public List<GovernmentOffer> getAllGovernmentOffers(int governmentId);
    
    public List<GovernmentOffer> getGovernmentOffersOnDeed(int deedId);
    
    public List<GovernmentOffer> getPublicListingGovernmentOffers();
    
    List<GovernmentOfferComment> getGovernmentOfferComments(int offerId);
    
    public void addGovernmentOfferComment(GovernmentOffer governmentOffer);
    
    boolean governmentExists(String email1);
    
    public List<GovernmentOfferLike> getGovernmentOfferLikes(int offerId);
    
    public GovernmentOfferLike addOfferLike(GovernmentOfferLike governmentOfferLike);

    public GovernmentOfferComment addCommentLike(GovernmentOfferComment goc);

    public DeederLike addDeederLike(DeederLike dl);

    public void createGovernmentOrg(GovernmentOrg governmentOrg);

    public List<GovernmentOffer> getGovernmentOffersForDeeder(int deederId);
    
    public List<Deeder> getGovernmentRewardedDeeders(int governmentId);
    
    
    
    
}
