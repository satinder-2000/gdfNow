/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.ejb;

import org.gdf.model.Access;
import org.gdf.model.AccessType;
import static org.gdf.model.AccessType.DEEDER;
import org.gdf.model.Deeder;
import org.gdf.model.OnHold;
import org.gdf.util.PasswordUtil;
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
public class AccessBean implements AccessBeanLocal {
    
    static final Logger LOGGER=Logger.getLogger(AccessBean.class.getCanonicalName());
    
    @PersistenceContext(name = "gdfPU")
    EntityManager em;
    
    @Inject
    EmailerBean emailerBean;
    
    @Inject
    DeederBeanLocal deederBeanLocal;
    

    @Override
    public Access getAccess(String email, String password) {
        LOGGER.log(Level.INFO, "Access sought for:{0} with {1}", new Object[]{email, password});
        Access toReturn=null;
        TypedQuery aTq=em.createQuery("select a from Access a where a.email=?1", Access.class);
        aTq.setParameter(1, email);
        List<Access> aList= aTq.getResultList();
        if (aList.isEmpty()){
            toReturn=new Access();
            toReturn.setExceptionMsg("Email not found.");
        }else{//fethch the first result tp compare password.
            Access access=aList.get(0);
            //first check the attempts.
            int attempts=access.getAttempts();
            if (attempts==3){
                access.setExceptionMsg("Your Account has been locked.");
                return access;
            }
            boolean passwordVerified=PasswordUtil.verifyUserPassword(password, access.getPassword(), email);
            if (passwordVerified && attempts==0){
               toReturn=access; 
            }else if (passwordVerified && attempts>0){
                access.setAttempts(0);
                //em.getTransaction().begin();
                access= em.merge(access);
                em.flush();
                toReturn=access;
            }else if (!passwordVerified){
                int count=++attempts;
                access.setAttempts(count);
                em.merge(access);
                em.flush();
                LOGGER.log(Level.INFO, "Attemps in the DB now: {0}", access.getAttempts());
                ;
                if (count==3)access.setExceptionMsg("Account Locked. 3 Login attempts expired");
                else access.setExceptionMsg("Login failed. "+(3-count)+" attemps left");
                toReturn=access;
            }
            
        }
        
        return toReturn;
    }

    @Override
    public Access createAccess(String email, String password, String name) {
        Access access=new Access();
        access.setEmail(email);
        access.setPassword(PasswordUtil.generateSecurePassword(password, email));
        //Remove from OnHold table now
        TypedQuery<OnHold> tQ1=em.createQuery("select h from OnHold h where h.email=?1", OnHold.class);
        tQ1.setParameter(1, email);
        OnHold val= tQ1.getSingleResult();
        access.setAccessType(val.getAccessType());
        access.setEntityId(val.getEntityId());
        access.setCreatedOn(LocalDateTime.now());
        access.setUpdatedOn(LocalDateTime.now());
        access.setProfileFile(val.getProfileFile());
        access.setName(name);
        em.persist(access);
        em.remove(val);
        LOGGER.log(Level.INFO, "Access granted successfully to: {0} and onhold record removed", email);
        emailerBean.sendAccessConfirmEmail(email);
        //emailSender.sendAccessConfirmEmail(email);
        return access;
    }

    @Override
    public boolean isEmailOnHold(String email) {
        TypedQuery<OnHold> tqN=em.createQuery("select oh from OnHold oh where oh.email=?1", OnHold.class);
        tqN.setParameter(1, email);
        List<OnHold> oHL=tqN.getResultList();
        if(oHL!=null && !oHL.isEmpty()){
           return true; 
        }else{
            return false;//false return is a good news.
        }
    }

    @Override
    public Access changePassword(Access access, String password) {
        String securePW=PasswordUtil.generateSecurePassword(password, access.getEmail());
        access.setPassword(securePW);
        access= em.merge(access);
        em.flush();
        return access;
        
    }

    @Override
    public OnHold getOnHold(String email) {
        TypedQuery<OnHold> tqN=em.createQuery("select oh from OnHold oh where oh.email=?1", OnHold.class);
        tqN.setParameter(1, email);
        try{
           OnHold onHold=tqN.getSingleResult();
           return onHold;
        }catch(NoResultException ex1){
            LOGGER.log(Level.WARNING, "No OnHold record found for {0}", email);
            return null;
        }
        
        
    }

    @Override
    public Access createAccess(Access access) {
        String email=access.getEmail();
        String password=access.getPassword();
        access.setPassword(PasswordUtil.generateSecurePassword(password, email));
        //Remove from OnHold table now
        TypedQuery<OnHold> tQ1=em.createQuery("select h from OnHold h where h.email=?1", OnHold.class);
        tQ1.setParameter(1, email);
        OnHold val= tQ1.getSingleResult();
        AccessType acType=val.getAccessType();
        access.setCreatedOn(LocalDateTime.now());
        access.setUpdatedOn(LocalDateTime.now());
        access.setAccessType(val.getAccessType());
        access.setName(val.getName());
        access.setEntityId(val.getEntityId());
        access.setProfileFile(val.getProfileFile());
        /*if (val.getImage()!=null){
            access.setImage(val.getImage());//For UserDeeder it won't be set in OnHold.
        }*/
        
        switch(acType){
            case DEEDER : {
                Deeder deeder=deederBeanLocal.getDeeder(val.getEntityId());
                deeder.setConfirmed(true);
            }
        }
        em.persist(access);
        em.remove(val);
        em.flush();
        LOGGER.log(Level.INFO, "Access granted successfully to: {0} and onhold record removed", email);
        emailerBean.sendAccessConfirmEmail(email);
        return access;
    }

    @Override
    public List<Access> getAllAccess(int limit) {
        TypedQuery<Access> tQA=em.createQuery("select a from Access a", Access.class);
        tQA.setMaxResults(limit);
        return tQA.getResultList();
    }

    @Override
    public OnHold updateOnHold(OnHold onHold) {
        OnHold onHoldDb=em.merge(onHold);
        LOGGER.info("onHold updated with profile file of :"+onHoldDb.getProfileFile());
        return onHoldDb;
    }

    @Override
    public Access updateAccess(Access access) {
        access.setPassword(PasswordUtil.generateSecurePassword(access.getPassword(), access.getEmail()));
        access=em.merge(access);
        em.flush();
        return access;
    }

    @Override
    public Access getAccess(String email) {
        Access toReturn=null;
        TypedQuery aTq=em.createQuery("select a from Access a where a.email=?1", Access.class);
        aTq.setParameter(1, email);
        List<Access> aList= aTq.getResultList();
        if (!aList.isEmpty()){
            toReturn=aList.get(0);
        }
        return toReturn;
    }

    @Override
    public boolean dispatchPasswordReset(String email) {
        try{
            emailerBean.setPasswordResetEmail(email);
            return true;
        }catch(Exception ex){
            ex.printStackTrace();
            return false;
        } 
    }

    @Override
    public OnHold getOnHold(int entityId, String accessType) {
        TypedQuery<OnHold> tqN=em.createQuery("select oh from OnHold oh where oh.entityId=?1 and oh.accessType=?2", OnHold.class);
        tqN.setParameter(1, entityId);
        AccessType acType=AccessType.valueOf(accessType);
        tqN.setParameter(2, acType);
        try{
           OnHold onHold=tqN.getSingleResult();
           return onHold;
        }catch(NoResultException ex1){
            LOGGER.log(Level.WARNING, "No OnHold record found for {0}", entityId);
            return null;
        }
        
    }

   
}
