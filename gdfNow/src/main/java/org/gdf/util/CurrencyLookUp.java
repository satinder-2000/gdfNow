/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.util;

import java.util.Properties;

/**
 *
 * @author satindersingh
 */
public class CurrencyLookUp {
    
    public static Properties currency=new Properties();
    
    static{
        currency.put("IN", "INR");
        currency.put("GB", "GBP");
        currency.put("US", "USD");
    }
    
    public static String getSymbol(String code){
        return (String) currency.get(code);
    }
    
}
