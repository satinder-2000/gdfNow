/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.ejb;

import org.gdf.model.Activity;
import org.gdf.model.ActivityType;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author satindersingh
 */
@Local
public interface ActivityRecorderBeanLocal  {
    
    public List<Activity> getActivityStack(); 
    
    public void add(ActivityType actType, int entityId, String message, String entityName);

}
