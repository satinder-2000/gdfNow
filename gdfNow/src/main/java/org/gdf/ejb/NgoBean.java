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
import org.gdf.model.Ngo;
import org.gdf.model.NgoCategory;
import org.gdf.model.NgoOffer;
import org.gdf.model.OnHold;
import org.gdf.model.comment.NgoOfferComment;
import org.gdf.model.like.DeederLike;
import org.gdf.model.like.NgoOfferLike;
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
public class NgoBean implements NgoBeanLocal {
    
    static final Logger LOGGER=Logger.getLogger(NgoBean.class.getCanonicalName());
    
    @PersistenceContext(name = "gdfPU")
    EntityManager em;
    
    @Inject
    EmailerBean emailerBean;
    
    @Inject
    ActivityRecorderBeanLocal activityRecorderBeanLocal;
    
    @Inject
    AccessBeanLocal accessBeanLocal;

    @Override
    public Ngo findNgoById(Integer ngoId) {
         Ngo ngo=em.find(Ngo.class, ngoId);
        ngo.getNgoAddress().getCountry();
        return ngo;
    }

    @Override
    public void amendNgo(Ngo ngo) {
       
       Access access=accessBeanLocal.getAccess(ngo.getEmail());
        if (!access.getProfileFile().equals(ngo.getLogoFile())){//implying profile file has been changed. Replication change applied in MBean in the FE. updateProfileFile()
           access.setProfileFile(ngo.getLogoFile());
           access.setImage(ngo.getImage());
        }
       em.merge(ngo);
       em.merge(access);        
       emailerBean.sendNgoAmendedEmail(ngo);
       LOGGER.info("Ngo Details updated.");
       
    }

    @Override
    public NgoOffer createNgoOffer(NgoOffer nOffer, Ngo ngo, Deed deed) {
        em.persist(nOffer);
        //em.flush();
        //ngo.getNgoOffers().add(nOffer);
        em.merge(ngo);
        //deed.getNgoOffers().add(nOffer);
        em.merge(deed);
        em.flush();
        LOGGER.log(Level.INFO, "New Ngo Offer created with id:{0}", nOffer.getId());
        emailerBean.notifyNgoOfOffer(nOffer);
        emailerBean.notifyDeederOfNgoOffer(nOffer);
        return nOffer;
    }

    @Override
    public NgoOffer getNgoOffer(int offerId) {
        return em.find(NgoOffer.class, offerId);
    }

    @Override
    public List<NgoOffer> getAllNgoOffers(int ngoId) {
        TypedQuery<NgoOffer> tQ=  em.createQuery("SELECT no from NgoOffer no where no.ngo.id=?1 order by no.offeredOn desc", NgoOffer.class);
        tQ.setParameter(1, ngoId);
        List<NgoOffer> results= tQ.getResultList();
        return results;
    }

    @Override
    public List<NgoOffer> getNgoOffersOnDeed(int deedId) {
        TypedQuery<NgoOffer> tQ=  em.createQuery("SELECT no from NgoOffer no join Deed d on no.deed.id=d.id join Deeder dr on d.deeder.id=dr.id where d.id=?1 order by no.offeredOn", NgoOffer.class);
        tQ.setParameter(1, deedId);
        List<NgoOffer> results= tQ.getResultList();
        return results;
    }
    
     @Override
    public List<NgoOffer> getNgoOffersForDeeder(int deederId) {
        TypedQuery<NgoOffer> tQ=  em.createQuery("SELECT no from NgoOffer no join Deed d on no.deed.id=d.id join Deeder dr on d.deeder.id=dr.id where dr.id=?1 order by no.offeredOn", NgoOffer.class);
        tQ.setParameter(1, deederId);
        List<NgoOffer> results= tQ.getResultList();
        return results;
    }

    @Override
    public List<NgoOffer> getPublicListingNgoOffers() {
        TypedQuery<NgoOffer> tQ=  em.createQuery("SELECT o from NgoOffer o order by o.offeredOn desc", NgoOffer.class);
        List<NgoOffer> results= tQ.getResultList();
        return results;
    }

    @Override
    public List<NgoOfferComment> getNgoOfferComments(int offerId) {
        TypedQuery<NgoOfferComment> tQ=em.createQuery("select noc from NgoOfferComment noc where noc.ngoOffer.id=?1 order by noc.date desc", NgoOfferComment.class);
        tQ.setParameter(1, offerId);
        List<NgoOfferComment> results=tQ.getResultList();
        return results;
    }

    @Override
    public void addNgoOfferComment(NgoOffer ngoOffer) {
        em.merge(ngoOffer);
        em.flush();
        LOGGER.log(Level.INFO, "NgoOffer :{0} now has {1} comments", new Object[]{ngoOffer.getId(), ngoOffer.getNgoOfferComments().size()});
    }

    @Override
    public Ngo findNgoByEmail(String email) {
        TypedQuery<Ngo> tqN=em.createQuery("select n from Ngo n where n.email=?1", Ngo.class);
        tqN.setParameter(1, email);
        List<Ngo> ngoL= tqN.getResultList();
        if (ngoL.isEmpty()){
            return null;
        }else{
            return ngoL.get(0);
        }
    }

    @Override
    public Ngo createNgo(Ngo ngo) {
        ngo.setCreatedOn(LocalDateTime.now());
        ngo.setUpdatedOn(LocalDateTime.now());
        em.persist(ngo);
        em.flush();
        OnHold onHold=new OnHold();
        onHold.setEmail(ngo.getEmail());
        onHold.setAccessType(AccessType.NGO);
        onHold.setEntityId(ngo.getId());
        onHold.setProfileFile(ngo.getLogoFile());
        onHold.setName(ngo.getName());
        onHold.setCountryCode(ngo.getNgoAddress().getCountry().getCode());
        onHold.setImage(ngo.getImage());
        em.persist(onHold);
        em.flush();
        LOGGER.log(Level.INFO.INFO, "Ngo persisted successfuuly with ID:{0} and Address ID: {1}", new Object[]{ngo.getId(), ngo.getNgoAddress().getId()});
        emailerBean.sendNgoRegConfirmEmail(ngo);
        String message="NGO Registered: ".concat(ngo.getName());
        activityRecorderBeanLocal.add(ActivityType.NGO, ngo.getId(), message,ngo.getName());
        return ngo;
    }

    @Override
    public List<NgoOfferLike> getNgoOfferLikes(int offerId) {
        TypedQuery<NgoOfferLike> tQ=em.createQuery("select nol from NgoOfferLike nol where nol.ngoOffer.id=?1 order by nol.time", NgoOfferLike.class);
        tQ.setParameter(1, offerId);
        List<NgoOfferLike> results=tQ.getResultList();
        return results;
    }

    @Override
    public NgoOfferLike addOfferLike(NgoOfferLike ngoOfferLike) {
        ngoOfferLike.getNgoOffer().getNgoOfferLikes().add(ngoOfferLike);
        em.persist(ngoOfferLike);
        em.merge(ngoOfferLike.getNgoOffer());
        em.flush();
        LOGGER.log(Level.INFO, "NgoOfferLike persisted with ID: {0}", ngoOfferLike.getId());
        return ngoOfferLike;
    }

    @Override
    public NgoOfferComment addCommentLike(NgoOfferComment noc) {
        noc= em.merge(noc);
        em.flush();
        LOGGER.log(Level.INFO, "NgoOfferComment Likes incremented . Total count now {0}", noc.getLikes());
        return noc;
    }

    @Override
    public DeederLike addDeederLike(DeederLike dl) {
        em.persist(dl);
        em.flush();
        LOGGER.log(Level.INFO, "DeederLike persisted with ID: {0}", dl.getId());
        return dl;
    }

    @Override
    public void createNgoCategory(NgoCategory ngoCategory) {
        em.persist(ngoCategory);
        em.flush();
        LOGGER.log(Level.INFO, "new NgoCategory persisted with ID: {0}", ngoCategory.getId());
    }

    @Override
    public List<Deeder> getNgoRewardedDeeders(int ngoId) {
        TypedQuery<Deeder> tqD=em.createQuery("select dr from NgoOffer no join Deed d on no.deed.id=d.id join Deeder dr on d.deeder.id=dr.id where no.ngo.id=?1",Deeder.class);
        tqD.setParameter(1, ngoId);
        List<Deeder> rewardedDeeders=tqD.getResultList();
        LOGGER.log(Level.INFO, "Returning {0} Deeders that have been rewarded by the Ngos {1}", new Object[]{rewardedDeeders.size(), ngoId});
        return rewardedDeeders;
    }

   
}
