/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.gdf.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 *
 * @author root
 */
public class URLShortner {
    
    private static final String tinyUrl = "http://tinyurl.com/api-create.php?url=";
    
    public static String shorter(String url) throws IOException {
        String tinyUrlLookup = tinyUrl + url;
        BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(tinyUrlLookup).openStream()));
        String tinyUrl = reader.readLine();
        return tinyUrl;
    }
    
}
