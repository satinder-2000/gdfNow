/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.ejb;

import org.gdf.model.Activity;
import org.gdf.model.ActivityType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

/**
 *
 * @author satindersingh
 *
 * This class is primarily used to display the scroll on the main page of the
 * site. At one time up to MaxActivitiesDb (as per init parameter) are loaded in
 * the memory from the database After that the activities are 'asynchronously'
 * persisted in the Data Base also added to the Cache (List<Activity>
 * activityStack) of the Class. Once the Cache reaches the limit of
 * MaxActivities (as per init parameter), the Cache is repopulated from the
 * Database as a refresh.
 */
@Stateless
public class ActivityRecorderBean implements ActivityRecorderBeanLocal {
    
    static final Logger LOGGER=Logger.getLogger(ActivityRecorderBean.class.getCanonicalName());
    
    @PersistenceContext(name = "gdfPU")
    EntityManager em;
    
    @Resource(name = "maxActivities")
    int maxActivities;
    List<Activity> activityStack;
    
    @Override
    public void add(ActivityType actType, int entityId, String message, String entityName) {
        Activity activity = new Activity();
        activity.setAccessId(entityId);
        activity.setActivityType(actType.toString());
        activity.setDate(LocalDateTime.now());
        activity.setMessage(message);
        activity.setEntityName(entityName);
        em.persist(activity);
        em.flush();
        LOGGER.log(Level.INFO, "Activity persisted ID:{0}", activity.getId());
    }


    @Override
    public List<Activity> getActivityStack() {
        TypedQuery<Activity> tQA = em.createQuery("select a from Activity a order by a.id desc", Activity.class);
        tQA.setMaxResults(maxActivities);
        List<Activity> rs = tQA.getResultList();
        return rs;
    }
}
