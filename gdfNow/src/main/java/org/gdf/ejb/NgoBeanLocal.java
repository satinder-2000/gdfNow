/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.ejb;

import org.gdf.model.Deed;
import org.gdf.model.Deeder;
import org.gdf.model.Ngo;
import org.gdf.model.NgoCategory;
import org.gdf.model.NgoOffer;
import org.gdf.model.comment.NgoOfferComment;
import org.gdf.model.like.DeederLike;
import org.gdf.model.like.NgoOfferLike;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author satindersingh
 */
@Local
public interface NgoBeanLocal {

    public Ngo findNgoById(Integer ngoId);
    
    public Ngo findNgoByEmail(String email);

    public void amendNgo(Ngo ngo);
    
    public Ngo createNgo(Ngo ngo);
    
    public NgoOffer createNgoOffer(NgoOffer nOffer, Ngo ngo, Deed deed);
    
    public NgoOffer getNgoOffer(int offerId);
    
    public List<NgoOffer> getAllNgoOffers(int ngoId);
    
    public List<NgoOffer> getNgoOffersOnDeed(int deedId);
    
    public List<NgoOffer> getPublicListingNgoOffers();
    
    List<NgoOfferComment> getNgoOfferComments(int offerId);
    
    public void addNgoOfferComment(NgoOffer ngoOffer);
    
    public List<NgoOfferLike> getNgoOfferLikes(int offerId);
    
    public NgoOfferLike addOfferLike(NgoOfferLike ngoOfferLike);

    public NgoOfferComment addCommentLike(NgoOfferComment noc);

    public DeederLike addDeederLike(DeederLike dl);

    public void createNgoCategory(NgoCategory ngoCategory);

    public List<NgoOffer> getNgoOffersForDeeder(int deederId);

    public List<Deeder> getNgoRewardedDeeders(int ngoId);

}
