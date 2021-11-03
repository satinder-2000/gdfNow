/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.ejb;

import org.gdf.model.Access;
import org.gdf.model.OnHold;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author satindersingh
 */
@Local
public interface AccessBeanLocal {
    
    public Access getAccess(String email, String password);
    
    public Access getAccess(String email);
    
    @Deprecated
    public Access createAccess(String email, String password, String name);

    public boolean isEmailOnHold(String email);

    public Access changePassword(Access access, String password);

    public OnHold getOnHold(String email);
    
    public OnHold getOnHold(int entityId, String accessType);
    
    public OnHold updateOnHold(OnHold onHold);

    public Access createAccess(Access access);

    public List<Access> getAllAccess(int i);
    
    public Access updateAccess(Access access);
    
    public boolean dispatchPasswordReset(String email);
    
    
    
    
    
}
