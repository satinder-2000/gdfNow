/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.ejb;

import org.gdf.model.Access;
import org.gdf.model.EntityType;
import org.gdf.model.Deed;
import org.gdf.model.Deeder;
import org.gdf.model.OnHold;
import org.gdf.model.User;
import org.gdf.util.PasswordUtil;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.LocalBean;
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
@LocalBean
public class UserBean implements UserBeanLocal {

    static final Logger LOGGER=Logger.getLogger(UserBean.class.getName());
    
    @PersistenceContext(name = "gdfPU")
    private EntityManager em;
    
     @Inject
    AccessBeanLocal accessBeanLocal;
    
    @Inject
    EmailerBean emailerBean;
    
    @Override
    public User createUser(User user) {
        //try {
            em.persist(user);
            em.flush();
            int userId=user.getId();
            OnHold onHold=new OnHold();
            onHold.setEmail(user.getEmail());
            onHold.setAccessType(EntityType.USER);
            onHold.setEntityId(userId);
            onHold.setProfileFile(user.getProfileFile());
            onHold.setName(user.getFirstname().concat(" ").concat(user.getLastname()));
            onHold.setCountryCode(user.getAddress().getCountry().getCode());
            onHold.setImage(user.getImage());
            em.persist(onHold);
            em.flush();
            emailerBean.sendUserRegConfirmEmail(user);
            //emailSender.sendEmail();
            
        /*} catch (MessagingException ex) {
            Logger.getLogger(UserBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(UserBean.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        return user;
    }
    
    @Override
    public boolean userExists(String email) {
        boolean exists=false;
        TypedQuery<User> tq=em.createQuery("select u from User u where u.email=?1", User.class);
        tq.setParameter(1, email);
        List<User> users= tq.getResultList();
        if (users.size()>0) exists=true;
        return exists;
    }

    
    @Override
    public User amendUser(User user) {
        user.setUpdatedOn(LocalDateTime.now());
        Access access=accessBeanLocal.getAccess(user.getEmail());
        if (!access.getProfileFile().equals(user.getProfileFile())){//implying profile file has been changed. Replication change applied in MBean in the FE. updateProfileFile()
           access.setProfileFile(user.getProfileFile());
           access.setImage(user.getImage());
        }
        User toReturn=em.merge(user);
        em.merge(access);
        return toReturn;
    }

    @Override
    public User getUser(Integer id) {
       return em.find(User.class, id);
    }

    

    @Override
    public void setUserPassword(String email, String password) {
        String encryptPW= PasswordUtil.generateSecurePassword(password, email);
        Access access=new Access();
        access.setEmail(email);
        access.setPassword(encryptPW);
        access.setEntityType(EntityType.USER);
        access.setAttempts(0);
        em.persist(access);
        LOGGER.info("Access profile created to User: "+email);
    }

    @Override
    public User getUser(String email) {
        TypedQuery<User> tq=em.createQuery("select u from User u where u.email=?1", User.class);
        tq.setParameter(1, email);
        return tq.getSingleResult();
    }

    @Override
    public List<Deeder> getUserDeeders(String email) {
        TypedQuery<Deeder> tq=em.createQuery("select d from Deeder d join UserDeeder ud on d.id=ud.deederId join User u on ud.userId=u.id where u.email=?1", Deeder.class);
        tq.setParameter(1, email);
        return tq.getResultList();
    }

    @Override
    public List<Deed> getUserDeederDeeds(int deederId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
