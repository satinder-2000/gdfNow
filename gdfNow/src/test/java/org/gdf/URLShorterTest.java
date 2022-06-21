/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.gdf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author root
 */
public class URLShorterTest {
    private static final String tinyUrl = "http://tinyurl.com/api-create.php?url=";
    private static final String url = "https://apnews.com/article/india-plants-health-coronavirus-pandemic-climate-change-4dd09a1c6665d7810eda02297332bd92#:~:text=The%20government%20designated%20more%20than,235%20million%20acres)%20by%202030";
    
    public static void main(String... args){
        String tinyUrlLookup = tinyUrl + url;
	BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(new URL(tinyUrlLookup).openStream()));
            String tinyUrl = reader.readLine();
            System.out.println("tinyUrl: "+tinyUrl);
        } catch (MalformedURLException ex) {
            Logger.getLogger(URLShorterTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(URLShorterTest.class.getName()).log(Level.SEVERE, null, ex);
        }
	
	  
    }
    
}
