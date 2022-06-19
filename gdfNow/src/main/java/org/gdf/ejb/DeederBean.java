/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.ejb;

import org.gdf.model.Access;
import org.gdf.model.EntityType;
import org.gdf.model.ActivityType;
import org.gdf.model.Deeder;
import org.gdf.model.OnHold;
import org.gdf.model.like.DeederLike;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

/**
 *
 * @author satindersingh
 */
@Stateless
public class DeederBean implements DeederBeanLocal {
    
    static final Logger LOGGER=Logger.getLogger(DeederBean.class.getCanonicalName());
    
    @Inject
    private EmailerBean emailerBean;
    
    @Inject
    ActivityRecorderBeanLocal activityRecorderBeanLocal;
    
    @Inject
    AccessBeanLocal accessBeanLocal;
    
    @PersistenceContext(name = "gdfPU")
    EntityManager em;

    @Override
    public Deeder getDeeder(int deederId) {
        Deeder deeder=em.find(Deeder.class, deederId);
        deeder.getDeederLikes();
        return deeder;
    }

    @Override
    public Deeder amendDeeder(Deeder deeder) {
        deeder.setUpdatedOn(LocalDateTime.now());   
        Access access=accessBeanLocal.getAccess(deeder.getEmail());
        if (!access.getProfileFile().equals(deeder.getProfileFile())){//implying profile file has been changed. Replication change applied in MBean in the FE. updateProfileFile()
           access.setProfileFile(deeder.getProfileFile());
           access.setImage(deeder.getImage());
        }
        Deeder toReturn=em.merge(deeder);
        em.merge(access); 
        em.flush();
        return toReturn;
    }

    @Override
    public Deeder getDeeder(String email) {
        TypedQuery<Deeder> tq=em.createQuery("select d from Deeder d where d.email=?1", Deeder.class);
        tq.setParameter(1, email);
        return tq.getSingleResult();
    }

    @Override
    public boolean deederExists(String email) {
        boolean exists=false;
        TypedQuery<Deeder> tq=em.createQuery("select d from Deeder d where d.email=?1", Deeder.class);
        tq.setParameter(1, email);
        List<Deeder> deeders= tq.getResultList();
        if (deeders.size()>0) exists=true;
        return exists;
    }

    @Override
    public Deeder createDeeder(Deeder deeder) {
        em.persist(deeder);
        em.flush();
        LOGGER.log(Level.INFO, "Deeder perseristed with ID: {0} and Address: {1}", new Object[]{deeder.getId(), deeder.getDeederAddress().getId()});
        OnHold onHold=new OnHold();
        onHold.setEmail(deeder.getEmail());
        onHold.setAccessType(EntityType.DEEDER);
        onHold.setEntityId(deeder.getId());
        onHold.setProfileFile(deeder.getProfileFile());
        onHold.setName(deeder.getFirstname().concat(" ").concat(deeder.getLastname()));
        onHold.setCountryCode(deeder.getDeederAddress().getCountry().getCode());
        onHold.setImage(deeder.getImage());
        em.persist(onHold);
        em.flush();
        String message="Deeder Registered: ".concat(deeder.getFirstname()).concat(" ").concat(deeder.getLastname());
        try {
            emailerBean.sendDeederRegConfirmEmail(deeder);
            
        } catch (Exception ex) {
            Logger.getLogger(DeederBean.class.getName()).log(Level.SEVERE, null, ex);
        } 
        activityRecorderBeanLocal.add(ActivityType.DEEDER, deeder.getId(), message, deeder.getFirstname().concat(" ").concat(deeder.getLastname()));
        return deeder;
    }

    @Override
    public List<DeederLike> getDeederLikes(int deederId) {
        TypedQuery<DeederLike> dlTq=em.createQuery("select dl from DeederLike dl join Deeder d on dl.deeder.id=d.id where d.id=?1", DeederLike.class);
        dlTq.setParameter(0, deederId);
        return dlTq.getResultList();
    }

    @Override
    public DeederLike addDeederLike(DeederLike dl) {
        dl.getDeeder().getDeederLikes().add(dl);
        em.persist(dl);
        em.merge(dl.getDeeder());
        em.flush();
        LOGGER.log(Level.INFO, "DeederLike persisted with ID:{0}", dl.getId());
        return dl;
    }

   
}
