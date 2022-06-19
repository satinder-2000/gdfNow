/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.ejb;

import org.gdf.model.Access;
import org.gdf.model.AccessType;
import org.gdf.model.ActivityType;
import org.gdf.model.Business;
import org.gdf.model.BusinessCategory;
import org.gdf.model.BusinessOffer;
import org.gdf.model.Deed;
import org.gdf.model.Deeder;
import org.gdf.model.OnHold;
import org.gdf.model.comment.BusinessOfferComment;
import org.gdf.model.like.BusinessOfferLike;
import org.gdf.model.like.DeederLike;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

/**
 *
 * @author satindersingh
 */
@Stateless
public class BusinessBean implements BusinessBeanLocal {
    
    static final Logger LOGGER=Logger.getLogger(BusinessBean.class.getCanonicalName());
    
    @PersistenceContext(name = "gdfPU")
    EntityManager em;
    
    @Inject
    EmailerBean emailerBean;
    
    @Inject
    AccessBeanLocal accessBeanLocal;
    
    @Inject
    ActivityRecorderBeanLocal activityRecorderBeanLocal;

    @Override
    public Business findBusinessById(Integer businessId) {
       Business toReturn= em.find(Business.class, businessId);
       return toReturn;
    }

    @Override
    public void amendBusiness(Business business) {
        business.setUpdatedOn(LocalDateTime.now());   
        Access access=accessBeanLocal.getAccess(business.getEmail());
        if (!access.getProfileFile().equals(business.getLogoFile())){//implying profile file has been changed. Replication change applied in MBean in the FE. updateProfileFile()
           access.setProfileFile(business.getLogoFile());
           access.setImage(business.getImage());
        }
        em.merge(business);
        em.merge(access);
        emailerBean.sendBusinessAmendedEmail(business);
        LOGGER.info("Business Details updated.");
    }

    @Override
    public BusinessOffer createBusinessOffer(BusinessOffer businessOffer, Business business, Deed deed) {
        em.persist(businessOffer);
        em.merge(business);
        em.merge(deed);
        em.flush();
        LOGGER.log(Level.INFO, "New Business Offer created with id:{0}", businessOffer.getId());
        emailerBean.notifyBusinessOfOffer(businessOffer);
        emailerBean.notifyDeederOfBusinessOffer(businessOffer);
        return businessOffer;
    }

    @Override
    public BusinessOffer getBusinessOfferTree(int offerId) {
        BusinessOffer bo=em.find(BusinessOffer.class, offerId);
        bo.getBusinessOfferLikes();
        bo.getDeed().getDeeder();
        bo.getBusiness();
        return bo;
    }

    @Override
    public List<BusinessOffer> getAllBusinessOffers(int businessId) {
        TypedQuery<BusinessOffer> tQ=  em.createQuery("SELECT bo from BusinessOffer bo where bo.business.id=?1 order by bo.offeredOn desc", BusinessOffer.class);
        tQ.setParameter(1, businessId);
        List<BusinessOffer> results= tQ.getResultList();
        return results;
    }

    @Override
    public BusinessOffer getBusinessOffer(int offerId) {
        return em.find(BusinessOffer.class, offerId);
    }

    @Override
    public List<BusinessOffer> getBusinessOffersOnDeed(int deedId) {
        TypedQuery<BusinessOffer> tQ=  em.createQuery("SELECT bo from BusinessOffer bo join Deed d on bo.deed.id=d.id join Deeder dr on d.deeder.id=dr.id where d.id=?1 order by bo.offeredOn", BusinessOffer.class);
        tQ.setParameter(1, deedId);
        List<BusinessOffer> results= tQ.getResultList();
        return results;
    }
    
    @Override
    public List<BusinessOffer> getBusinessOffersForDeeder(int deederId) {
        TypedQuery<BusinessOffer> tQ=  em.createQuery("SELECT bo from BusinessOffer bo join Deed d on bo.deed.id=d.id join Deeder dr on d.deeder.id=dr.id where dr.id=?1 order by bo.offeredOn", BusinessOffer.class);
        tQ.setParameter(1, deederId);
        List<BusinessOffer> results= tQ.getResultList();
        return results;
    }

    @Override
    public List<BusinessOffer> getPublicListingBusinessOffers() {
        TypedQuery<BusinessOffer> tQ=  em.createQuery("SELECT o from BusinessOffer o order by o.offeredOn desc", BusinessOffer.class);
        List<BusinessOffer> results= tQ.getResultList();
        return results;
    }

    @Override
    public List<BusinessOfferComment> getBusinessOfferComments(int businessOfferId) {
        TypedQuery<BusinessOfferComment> tQ=em.createQuery("select boc from BusinessOfferComment boc where boc.businessOffer.id=?1 order by boc.date desc", BusinessOfferComment.class);
        tQ.setParameter(1, businessOfferId);
        List<BusinessOfferComment> results=tQ.getResultList();
        return results;
    }

    @Override
    public void addBusinessOfferComment(BusinessOffer businessOffer) {
        em.merge(businessOffer);
        em.flush();
        LOGGER.log(Level.INFO, "BusinessOffer :{0} now has {1} comments", new Object[]{businessOffer.getId(), businessOffer.getBusinessOfferComments().size()});
    }

    @Override
    public Business createBusiness(Business business) {
        em.persist(business);
        em.flush();
        OnHold onHold=new OnHold();
        onHold.setEmail(business.getEmail());
        onHold.setAccessType(AccessType.BUSINESS);
        onHold.setEntityId(business.getId());
        onHold.setProfileFile(business.getLogoFile());
        onHold.setName(business.getName());
        onHold.setCountryCode(business.getBusinessAddress().getCountry().getCode());
        onHold.setImage(business.getImage());
        em.persist(onHold);
        em.flush();
        LOGGER.log(Level.INFO, "Business persisted successfuly with ID:{0} and Address ID: {1}", new Object[]{business.getId(), business.getBusinessAddress().getId()});
        emailerBean.sendBusinessRegConfirmEmail(business);
        String message="Business Registered: ".concat(business.getName());
        activityRecorderBeanLocal.add(ActivityType.BUSINESS,business.getId(),message,business.getName());
        return business;
    }

    @Override
    public Business findBusinessByEmail(String email) {
        TypedQuery<Business> tqB=em.createQuery("select b from Business b where b.email=?1", Business.class);
        tqB.setParameter(1, email);
        List<Business> businessL= tqB.getResultList();
        if (businessL.isEmpty()){
            return null;
        }else{
            return businessL.get(0);
        }
    }

    @Override
    public BusinessOfferLike addOfferLike(BusinessOfferLike businessOfferLike) {
        businessOfferLike.getBusinessOffer().getBusinessOfferLikes().add(businessOfferLike);
        em.persist(businessOfferLike);
        em.merge(businessOfferLike.getBusinessOffer());
        em.flush();
        LOGGER.log(Level.INFO, "BusinessOfferLike persisted with ID: {0}", businessOfferLike.getId());
        return businessOfferLike;
    }

    @Override
    public List<BusinessOfferLike> getBusinessOfferLikes(int offerId) {
        TypedQuery<BusinessOfferLike> tQ=em.createQuery("select bol from BusinessOfferLike bol where bol.businessOffer.id=?1 order by bol.time", BusinessOfferLike.class);
        tQ.setParameter(1, offerId);
        List<BusinessOfferLike> results=tQ.getResultList();
        return results;
    }

    @Override
    public BusinessOfferComment addCommentLike(BusinessOfferComment boc) {
        //it should be a simple update.
        boc= em.merge(boc);
        em.flush();
        LOGGER.log(Level.INFO, "BusinessOfferComment Likes increments . Total could now {0}", boc.getLikes());
        return boc;
    }

    @Override
    public DeederLike addDeederLike(DeederLike dl) {
        em.persist(dl);
        em.flush();
        LOGGER.log(Level.INFO, "DeederLike persisted with ID: {0}", dl.getId());
        return dl;
    }

    @Override
    public void addBusinessCategory(BusinessCategory businessCategory) {
        em.persist(businessCategory);
        em.flush();
        LOGGER.log(Level.INFO, "new BusinessCategory persisted with ID: {0}", businessCategory.getId());
    }

    @Override
    public List<Deeder> getBusinessRewardedDeeders(int businessId) {
        TypedQuery<Deeder> tqD=em.createQuery("select dr from BusinessOffer bo join Deed d on bo.deed.id=d.id join Deeder dr on d.deeder.id=dr.id where bo.business.id=?1",Deeder.class);
        //SELECT DR.* FROM BUSINESS_OFFER BO JOIN DEED D ON BO.DEED_ID=D.ID JOIN DEEDER DR ON D.DEEDER_ID=DR.ID WHERE BO.BUSINESS_ID=1;
        tqD.setParameter(1, businessId);
        List<Deeder> rewardedDeeders=tqD.getResultList();
        LOGGER.log(Level.INFO, "Returning {0} Deeders that have been rewarded by the Business {1}", new Object[]{rewardedDeeders.size(), businessId});
        return rewardedDeeders;
    }

   
}
