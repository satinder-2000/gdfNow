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
import org.gdf.model.User;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
public class UserDeederBean implements UserDeederBeanLocal {
    
    static final Logger LOGGER=Logger.getLogger(UserDeederBean.class.getCanonicalName());
    
    @PersistenceContext(name = "gdfPU")
    EntityManager em;
    
    @Inject
    DeederBeanLocal deederBeanLocal;
    
    @Inject
    AccessBeanLocal accessBeanLocal;
    
    @Inject
    EmailerBean eMailer;
    
    @Inject
    ActivityRecorderBeanLocal activityRecorderBeanLocal;

    @Override
    public Deeder createUserDeeder(Deeder deeder, User user) {
        deeder.getUsers().add(user);
        em.persist(deeder);
        em.flush();
        int deederId=deeder.getId();
        LOGGER.log(Level.INFO, "Deeder persisited with Deeder ID: {0}",deederId);
        user.getDeeders().add(deeder);
        user = em.merge(user);
        em.persist(deeder);
        em.flush();
        LOGGER.log(Level.INFO, "User Deeder persisited with Deeder ID: {0} User being: {1}", new Object[]{deeder.getId(), user.getId()});
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
        
        String message="Deeder Nominated: ".concat(deeder.getFirstname()).concat(" ").concat(deeder.getLastname().concat("by User: ").concat(user.getFirstname()).concat(" ").concat(user.getLastname()));
        activityRecorderBeanLocal.add(ActivityType.DEEDER, deeder.getId(), message,deeder.getFirstname().concat(" ").concat(deeder.getLastname()));
        
        eMailer.sendUserDeederRegConfirmEmail(user, deeder);
        eMailer.sendDeederNominationConfirmEmail(user, deeder);
        return deeder;
    }
    
     @Override
    public Deeder updateUserDeederReview(Deeder deeder) {
        deeder.setUpdatedOn(LocalDateTime.now());   
        Deeder toReturn=em.merge(deeder);
        em.flush();
        LOGGER.info("User Deeder updated.");
        return toReturn;
    }

    @Override
    public boolean UserDeederExists(String email) {
        boolean exists=false;
        TypedQuery<Deeder> tq=em.createQuery("select d from Deeder d where d.email=?1", Deeder.class);
        tq.setParameter(1, email);
        List<Deeder> deeders= tq.getResultList();
        if (deeders.size()>0) exists=true;
        return exists;
    }

    @Override
    public Deeder getUserDeeder(int udId) {
        Deeder deeder=em.find(Deeder.class, udId);
        //deeder.getDeederLikes();
        return deeder;
    }

    @Override
    public Deeder createUserDeeder(Deeder deeder) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Deeder updateUserDeeder(Deeder deeder) {
        deeder.setUpdatedOn(LocalDateTime.now());   
        Deeder toReturn=em.merge(deeder);
        em.flush();
        LOGGER.info("User Deeder updated.");
        return toReturn;
        
    }

    @Override
    public List<Deeder> getNominatedDeeders(int userId) {
        TypedQuery<User> tq=em.createQuery("select u from User u where u.id=?1", User.class);
        tq.setParameter(1, userId);
        User user=tq.getSingleResult();
        Set<Deeder> userDeeders=user.getDeeders();
        List<Deeder> userDeedersList=new ArrayList<Deeder>();
        for (Deeder userDeeder : userDeeders) {
            userDeedersList.add(userDeeder);
        }
        return userDeedersList;
    }
}
