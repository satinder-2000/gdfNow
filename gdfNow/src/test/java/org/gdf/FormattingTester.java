/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.gdf;

/**
 *
 * @author root
 */
public class FormattingTester {
    
    public static void main(String[] args){
        String greetings = String.format(
                    "Hello %2$s, welcome to %1$s !",
                    "Baeldung",
                    "Folks");
        System.out.print(greetings);
        String str1=String.format("dearBusiness %1$s","business1@gdf.org");
        System.out.println(str1);
    }
    
}
