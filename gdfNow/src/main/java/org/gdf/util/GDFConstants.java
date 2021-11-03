/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.util;

/**
 *
 * @author satindersingh
 */
public interface GDFConstants {
    
    public static final String ACCESS="Access";
    
    /*public static final String LOGGED_IN_GDF="LoggedInGDF";
    
    public static final String LOGGED_IN_GDF_ID="LoggedInGDFId";
    
    public static final String LOGGED_IN_PROFILE_FILE="profileFile";
    
    public static final String ACCESS_TYPE="AccessType";*/
    
    public static final int DESCRIPTION_MAX_CHARS=250;
    
    public static final String URL_REGEX="^(http:\\/\\/www\\.|https:\\/\\/www\\.|http:\\/\\/|https:\\/\\/)?[a-z0-9]+([\\-\\.]{1}[a-z0-9]+)*\\.[a-z]{2,5}(:[0-9]{1,5})?(\\/.*)?$";
    
    public static final String IN_POSTCODE_REGEX="^[1-9][0-9]{5}$";
    
    public static final String GB_POSTCODE_REGEX="([Gg][Ii][Rr] 0[Aa]{2})|((([A-Za-z][0-9]{1,2})|(([A-Za-z][A-Ha-hJ-Yj-y][0-9]{1,2})|(([A-Za-z][0-9][A-Za-z])|([A-Za-z][A-Ha-hJ-Yj-y][0-9]?[A-Za-z]))))\\s?[0-9][A-Za-z]{2})";
    
    public static final String US_POSTCODE_REGEX="^[0-9]{5}(?:-[0-9]{4})?$";
    
    public static final String EMAIL_REGEX=".+\\@.+\\..+";
    
    public static final String PHONE_REGEX="\\(*\\d{3}\\)*( |-)*\\d{3}( |-)*\\d{4}";
    
    public static final String IN_NAME="INDIA";
    
    public static final String GB_NAME="UNITED KINGDOM";
    
    public static final String US_NAME="UNITED STATES OF AMERICA";
    
    public static final String IN_CODE="IN";
    
    public static final String GB_CODE="GB";
    
    public static final String US_CODE="US";
    
    
    public static final String TEMP_IMAGE="tempImg";
    
    public static final String TEMP_IMAGE_2="tempImg2";
    
    public static final String PUB_VIEW_IMAGE_MAP_DDR="tempImgPVMapDDR";
    
    public static final String PUB_VIEW_IMAGE_MAP_BUS="tempImgPVMapBUS";
    
    public static final String PUB_VIEW_IMAGE_MAP_GOV="tempImgPVMapGOV";
    public static final String PUB_VIEW_IMAGE_MAP_NGO="tempImgPVMapNGO";
    
    
    
    public static String TEMP_IMAGE_MAP="tempImgMap";
    
    /**
     * Password expresion that requires one lower case letter, one upper case letter, one digit, 6-13 length, and no spaces. 
     * http://regexlib.com/Search.aspx?k=password&AspxAutoDetectCookieSupport=1
     */
    
    /**
     * Password must be between 8 and 14 digits long and include at least one numeric digit.
    *
    */
    public static final String PW_REGEX="^(?=.*\\d).{8,14}$";//"^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?!.*\\s).{4,8}$";
    public static String ACCESS_UD="AccessUD";
    
    
    
}
