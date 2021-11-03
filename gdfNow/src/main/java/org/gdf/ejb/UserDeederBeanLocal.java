/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.ejb;

import org.gdf.model.Deeder;
import org.gdf.model.User;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author satindersingh
 */
@Local
public interface UserDeederBeanLocal {
    
    public Deeder createUserDeeder(Deeder deeder, User user);
    
    public Deeder updateUserDeederReview(Deeder deeder);
    
    public Deeder updateUserDeeder(Deeder deeder);
    
    public boolean UserDeederExists(String email);
    
    public Deeder getUserDeeder(int udId);

    public Deeder createUserDeeder(Deeder deeder);
    
    public List<Deeder> getNominatedDeeders(int userId);
    
    
}
