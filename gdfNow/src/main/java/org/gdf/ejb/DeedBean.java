/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.ejb;

import org.gdf.model.ActivityType;
import org.gdf.model.Deed;
import org.gdf.model.Deeder;
import org.gdf.model.comment.DeedComment;
import org.gdf.model.like.DeedLike;
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
public class DeedBean implements DeedBeanLocal {
    
    static final Logger LOGGER=Logger.getLogger(DeedBean.class.getCanonicalName());
    
    @PersistenceContext(name = "gdfPU")
    EntityManager em;
    
    @Inject
    ActivityRecorderBeanLocal activityRecorderBeanLocal;
    
    @Inject
    EmailerBean emailerBean;
    
        
    @Override
    public Deed getDeedWithDeeder(int deedId) {
        Deed deed= em.find(Deed.class, deedId);
        Deeder deeder= deed.getDeeder();
        LOGGER.log(Level.INFO, "Deed details extracted along with Deeder:{0}", deeder.getId());
        return deed;
    }

    @Override
    public List<DeedComment> getDeedComments(int deedId) {
        TypedQuery<DeedComment> tQDC=em.createQuery("select dc from DeedComment dc where dc.deed.id=?1 order by dc.date desc", DeedComment.class);
        tQDC.setParameter(1, deedId);
        return tQDC.getResultList();
    }

    @Override
    public void addComment(Deed deed) {
        em.merge(deed);
        em.flush();
        LOGGER.log(Level.INFO, "Deed :{0}now has {1}comments", new Object[]{deed.getId(), deed.getDeedComments().size()});
    }

    @Override
    public List<Deed> getDeedsSummary() {
        TypedQuery<Deed> dsTq=em.createQuery("select d from Deed d", Deed.class);
        List<Deed> deeds= dsTq.getResultList();
        LOGGER.log(Level.INFO, "Deeds extracted: {0}", deeds.size());
        return deeds;
    }

    @Override
    public List<Deed> getDeedsOfDeeder(String deederEmail) {
        TypedQuery<Deed> dsTq=em.createQuery("select d from Deed d join Deeder dr on d.deeder.id=dr.id where dr.email=?1", Deed.class);
        dsTq.setParameter(1, deederEmail);
        return dsTq.getResultList();
    }
    
    @Override
    public List<Deed> getDeedsOfDeeder(int deederId) {
        TypedQuery<Deed> dsTq=em.createQuery("select d from Deed d join Deeder dr on d.deeder.id=dr.id where dr.id=?1", Deed.class);
        dsTq.setParameter(1, deederId);
        return dsTq.getResultList();
    }

    @Override
    public Deed getDeedDetails(int deedId) {
        Deed deed=em.find(Deed.class, deedId);
        LOGGER.log(Level.INFO, "Deed extracted: {0}", deed.toString());
        return deed;
    }

    @Override
    public Deed createDeed(Deed deed) {
        deed.setConfirmed(false);
        em.persist(deed);
        em.flush();
        LOGGER.log(Level.INFO, "Deed created in the Database. The ID is: {0}", deed.getId());
        if (deed.getDeeder()!=null){//A Deeder's Deed
            String message = "Deed submitted ".concat(deed.getIntro()).concat(" that was performed on ").concat(deed.getDeedDate().toString()).concat(" by ").concat(deed.getDeeder().getFirstname()).concat(" ").concat(deed.getDeeder().getLastname());
            activityRecorderBeanLocal.add(ActivityType.DEED, deed.getId(), message, deed.getDeeder().getFirstname().concat(" ").concat(deed.getDeeder().getLastname()));
            emailerBean.sendDeedCreated(deed.getDeeder(), deed);
        }else if (deed.getNgo()!=null){//NGO's Deed
            String message = "NGO Deed submitted ".concat(deed.getIntro()).concat(" that was performed on ").concat(deed.getDeedDate().toString()).concat(" by ").concat(deed.getNgo().getName());
            activityRecorderBeanLocal.add(ActivityType.DEED_NGO, deed.getId(), message, deed.getNgo().getName());
            emailerBean.sendDeedCreated(deed.getNgo(), deed);
        }
        
        return deed;
    }

    @Override
    public DeedLike addDeedLike(DeedLike deedLike) {
        deedLike.getDeed().getDeedLikes().add(deedLike);
        em.persist(deedLike);
        em.merge(deedLike.getDeed());
        em.flush();
        LOGGER.log(Level.INFO, "DeedLike persisted with ID: {0}", deedLike.getId());
        return deedLike;
    }

    @Override
    public List<DeedLike> getDeedLikes(int deedId) {
        TypedQuery<DeedLike> tQdl=em.createQuery("select dl from DeedLike dl join Deed d on dl.deed.id=d.id and d.id=?1", DeedLike.class);
        tQdl.setParameter(1, deedId);
        return tQdl.getResultList();
    }

    @Override
    public DeedComment addCommentLike(DeedComment dc) {
        em.merge(dc);
        em.flush();
        LOGGER.log(Level.INFO, "DeedCommentLike persisted on DeedComment with ID: {0}", dc.getId());
        return dc;
    }

    @Override
    public Deed confirmDeed(Deed deed) {
        deed.setConfirmed(true);
        Deed toReturn =em.merge(deed);
        em.flush();
        return toReturn;
    }

    
}
