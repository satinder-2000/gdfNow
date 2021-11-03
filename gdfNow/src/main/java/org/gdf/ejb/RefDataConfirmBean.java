/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.ejb;

import org.gdf.event.NgoCategoryEvent;
import org.gdf.model.NgoCategory;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;

/**
 *
 * @author root
 */
@Stateless
@Local(RefDataConfirmBeanLocal.class)
@Remote(RefDataConfirmBeanRemote.class)
public class RefDataConfirmBean implements RefDataConfirmBeanLocal,RefDataConfirmBeanRemote  {
    
    
    Event<NgoCategoryEvent> events;

    @Override
    public void confirmNgoCategory(NgoCategory ngoCategory) {
        events.fire(new NgoCategoryEvent(ngoCategory));
    }

    
}
