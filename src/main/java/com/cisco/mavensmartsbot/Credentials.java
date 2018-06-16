/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cisco.mavensmartsbot;

/**
 *
 * @author richverjinski
 */
public class Credentials {
    
    /**
     *  This the person Id of the bot. When creating a new bot, I generally
     *  receive this via the API, and then capture it here.  It's used so that
     *  the bot does not respond to questions from itself (which can cause an infinite loop).
     */
    
    
    public static String getMyPersonId() {
        String myPersonId = System.getenv().get("SSPID");
        if ( myPersonId == null) {
            myPersonId = "Unknown PersonID";
        }
        return myPersonId;
    }

    /**
     *  This is a secret key that's been assigned to the bot.  Each bot will have one.
     *  If you want to create a new bot, then get a new key at https://developer.ciscospark.com/apps.html
     *  
     * For security reasons, the accessToken is passed as an Environment variable called OrchAT, 
     * which stands for Orchestration Access Token.
     */
    
    public static String getMyAccessToken() {
        String at = System.getenv().get("SSAT");
        if ( at == null) {
            at = "Unknown Access Token";
        }
        return at;
    }
 
    public static String getMySSToken() {
        String SST = System.getenv().get("SST");
        if ( SST == null) {
            SST = "Unknown SmartSheet Token";
        }
        return SST;
    }           
    
    public static int getApacheSparkPort() {
        String ApachePort = System.getenv().get("BOTPORT");
        int p = Integer.parseInt(ApachePort);
        return p;
    }   
    
    private static final String myWebhook = "http://fe51f577.ngrok.io/webhook";
    
    public static String getMyWebhook() {
        return myWebhook;
    }
     
    private static final String emailDomain = "@cisco.com";
    public static String getEmailDomain() {
        return emailDomain;
    }
    

}
