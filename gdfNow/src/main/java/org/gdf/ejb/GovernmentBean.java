/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.ejb;

import org.gdf.model.Access;
import org.gdf.model.AccessType;
import org.gdf.model.ActivityType;
import org.gdf.model.Deed;
import org.gdf.model.Deeder;
import org.gdf.model.Government;
import org.gdf.model.GovernmentOffer;
import org.gdf.model.GovernmentOrg;
import org.gdf.model.OnHold;
import org.gdf.model.comment.GovernmentOfferComment;
import org.gdf.model.like.DeederLike;
import org.gdf.model.like.GovernmentOfferLike;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

/**
 *
 * @author satindersingh
 */
@Stateless
public class GovernmentBean implements GovernmentBeanLocal {
    
    static final Logger LOGGER=Logger.getLogger(GovernmentBean.class.getCanonicalName()); 
    
    @PersistenceContext(name = "gdfPU")
    EntityManager em;
    
    @Inject
    EmailerBean emailerBean;
    
    @Inject
    ActivityRecorderBeanLocal activityRecorderBeanLocal;
    
    @Inject
    AccessBeanLocal accessBeanLocal;

    @Override
    public Government findGovernmentById(Integer governmentId) {
        Government toReturn=em.find(Government.class, governmentId);
        //Government Address being a Lazy Fetch. Let's Fetch it now.
        toReturn.getGovernmentAddress().getCountry();
        return toReturn; 
    }

    @Override
    public Government createGovernment(Government government) {
        em.persist(government);
        em.flush();
        OnHold onHold=new OnHold();
        onHold.setEmail(government.getEmail1());
        onHold.setAccessType(AccessType.GOVERNMENT);
        onHold.setEntityId(government.getId());
        onHold.setProfileFile(government.getLogoFile());
        onHold.setName(government.getName());
        onHold.setCountryCode(government.getGovernmentAddress().getCountry().getCode());
        onHold.setImage(government.getImage());
        em.persist(onHold);
        em.flush();
        LOGGER.log(Level.INFO, "Government  persisted successfuuly with ID:{0} and Address ID: {1}", new Object[]{government.getId(), government.getGovernmentAddress().getId()});
        emailerBean.sendGovernmentRegConfirmEmail(government);
        String message="Government Department Registered: ".concat(government.getName());
        activityRecorderBeanLocal.add(ActivityType.GOVERNMENT,government.getId(), message,government.getName());
        
        return government;
    }

    @Override
    public void amendGovernment(Government government) {
        government.setUpdatedOn(LocalDateTime.now());
        Access access=accessBeanLocal.getAccess(government.getEmail1());
        if (!access.getProfileFile().equals(government.getLogoFile())){//implying profile file has been changed. Replication change applied in MBean in the FE. updateProfileFile()
           access.setProfileFile(government.getLogoFile());
           access.setImage(government.getImage());
        }
        em.merge(government);
        em.merge(access);
        emailerBean.sendGovernmentAmendedEmail(government);
        LOGGER.info("Government Details updated.");
    }

    @Override
    public GovernmentOffer createGovernmentOffer(GovernmentOffer governmentOffer, Government government, Deed deed) {
        em.persist(governmentOffer);
        em.merge(government);
        em.merge(deed);
        em.flush();
        LOGGER.log(Level.INFO, "New Government Offer created with id:{0}", governmentOffer.getId());
        String message=governmentOffer.getOfferType().concat(" made by ").concat(governmentOffer.getGovernment().getName()).concat(" on Deed ").concat(governmentOffer.getDeed().getTitle());
        activityRecorderBeanLocal.add(ActivityType.GOVERNMENT_OFFER,governmentOffer.getId(),message,governmentOffer.getGovernment().getName());
        emailerBean.notifyDeederOfGovernmentOffer(governmentOffer);
        emailerBean.notifyGovernmentOfOffer(governmentOffer);
        
        return governmentOffer;
    }

    @Override
    public GovernmentOffer getGovernmentOffer(int offerId) {
        return em.find(GovernmentOffer.class, offerId);
    }

    @Override
    public List<GovernmentOffer> getAllGovernmentOffers(int governmentId) {
        TypedQuery<GovernmentOffer> tQ=  em.createQuery("SELECT go from GovernmentOffer go where go.government.id=?1 order by go.offeredOn desc", GovernmentOffer.class);
        tQ.setParameter(1, governmentId);
        List<GovernmentOffer> results= tQ.getResultList();
        return results;
    }

    @Override
    public List<GovernmentOffer> getGovernmentOffersOnDeed(int deedId) {
        TypedQuery<GovernmentOffer> tQ=  em.createQuery("SELECT go from GovernmentOffer go join Deed d on go.deed.id=d.id join Deeder dr on d.deeder.id=dr.id where d.id=?1 order by go.offeredOn", GovernmentOffer.class);
        tQ.setParameter(1, deedId);
        List<GovernmentOffer> results= tQ.getResultList();
        return results;
    }
    
     @Override
    public List<GovernmentOffer> getGovernmentOffersForDeeder(int deederId) {
        TypedQuery<GovernmentOffer> tQ=  em.createQuery("SELECT go from GovernmentOffer go join Deed d on go.deed.id=d.id join Deeder dr on d.deeder.id=dr.id where dr.id=?1 order by go.offeredOn", GovernmentOffer.class);
        tQ.setParameter(1, deederId);
        List<GovernmentOffer> results= tQ.getResultList();
        return results;
    }

    @Override
    public List<GovernmentOffer> getPublicListingGovernmentOffers() {
        TypedQuery<GovernmentOffer> tQ=  em.createQuery("SELECT o from GovernmentOffer o order by o.offeredOn desc", GovernmentOffer.class);
        List<GovernmentOffer> results= tQ.getResultList();
        return results;
    }

    @Override
    public List<GovernmentOfferComment> getGovernmentOfferComments(int offerId) {
        TypedQuery<GovernmentOfferComment> tQ=em.createQuery("select goc from GovernmentOfferComment goc where goc.governmentOffer.id=?1 order by goc.date desc", GovernmentOfferComment.class);
        tQ.setParameter(1, offerId);
        List<GovernmentOfferComment> results=tQ.getResultList();
        return results;
    }

    @Override
    public void addGovernmentOfferComment(GovernmentOffer governmentOffer) {
        em.merge(governmentOffer);
        em.flush();
        LOGGER.log(Level.INFO, "GovernmentOffer :{0} now has {1} comments", new Object[]{governmentOffer.getId(), governmentOffer.getGovernmentOfferComments().size()});
    }

    @Override
    public boolean governmentExists(String email1) {
        boolean exists=false;
        TypedQuery<Government> tq=em.createQuery("select g from Government g where g.email1=?1", Government.class);
        tq.setParameter(1, email1);
        try{
            if (tq.getSingleResult()!=null) exists=true;
        }catch(NoResultException ex){
            //just ignore we'll return false anyway
        }
        return exists;
    }

    @Override
    public List<GovernmentOfferLike> getGovernmentOfferLikes(int offerId) {
        TypedQuery<GovernmentOfferLike> tQ=em.createQuery("select gol from GovernmentOfferLike gol where gol.governmentOffer.id=?1 order by gol.time", GovernmentOfferLike.class);
        tQ.setParameter(1, offerId);
        List<GovernmentOfferLike> results=tQ.getResultList();
        return results;
    }

    @Override
    public GovernmentOfferLike addOfferLike(GovernmentOfferLike governmentOfferLike) {
        governmentOfferLike.getGovernmentOffer().getGovernmentOfferLikes().add(governmentOfferLike);
        em.persist(governmentOfferLike);
        em.merge(governmentOfferLike.getGovernmentOffer());
        em.flush();
        LOGGER.log(Level.INFO, "GovernmentOfferLike persisted with ID: {0}", governmentOfferLike.getId());
        return governmentOfferLike;
    }

    @Override
    public GovernmentOfferComment addCommentLike(GovernmentOfferComment goc) {
        goc= em.merge(goc);
        em.flush();
        LOGGER.log(Level.INFO, "GovernmentOfferComment Likes incremented . Total count now {0}", goc.getLikes());
        return goc;
    }

    @Override
    public DeederLike addDeederLike(DeederLike dl) {
        em.persist(dl);
        em.flush();
        LOGGER.log(Level.INFO, "DeederLike persisted with ID: {0}", dl.getId());
        return dl;
    }
    
    public void createGovernmentOrg(GovernmentOrg governmentOrg){
        em.persist(governmentOrg);
        em.flush();
        LOGGER.log(Level.INFO, "GovernmentOrg persisted with ID: {0}", governmentOrg.getId());
        
    }

    @Override
    public List<Deeder> getGovernmentRewardedDeeders(int governmentId) {
        TypedQuery<Deeder> tqD=em.createQuery("select dr from GovernmentOffer go join Deed d on go.deed.id=d.id join Deeder dr on d.deeder.id=dr.id where go.government.id=?1",Deeder.class);
        tqD.setParameter(1, governmentId);
        List<Deeder> rewardedDeeders=tqD.getResultList();
        LOGGER.log(Level.INFO, "Returning {0} Deeders that have been rewarded by the Government {1}", new Object[]{rewardedDeeders.size(), governmentId});
        return rewardedDeeders;
    }

   
    
    
}
