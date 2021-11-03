/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.ejb;

import org.gdf.model.Deed;
import org.gdf.model.Deeder;
import org.gdf.model.User;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author satindersingh
 */
@Local
public interface UserBeanLocal {

    public User createUser(User user);

    public boolean userExists(String email);

    public User amendUser(User user);

    public User getUser(Integer id);

    public void setUserPassword(String email, String password);

    public User getUser(String email);

    public List<Deeder> getUserDeeders(String email);

    public List<Deed> getUserDeederDeeds(int deederId);

}
