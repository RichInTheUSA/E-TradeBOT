/*
 * Copyright 2019, Rich Verjinski
 *
 * The Credentials class is used to store any type of credentials needed 
 * to operate the BOT or to integrate into any backend systems.
 *
 * Actual Keys are stored as environment variables, which are passed to the
 * runtime system.  This way, we are not hardcoding any sensitive information.
 */
package com.RichInTheUSA.EtradeBot;

/**
 *
 * @author richverjinski
 */
public class Credentials {
    
    /**
     *  The Person ID of the bot is a string of characters that uniquely
     *  identifies the BOT.  Messages from the BOT are filtered out, so that
     *  the BOT does not respond to it's own messages.
     * 
     *  The Person ID string should be stored in the BOTPID environment variable
     */
    
    public static String getMyPersonId() {
        String myPersonId = System.getenv().get("BOTPID");
        if ( myPersonId == null) {
            myPersonId = "Unknown PersonID";
        }
        return myPersonId;
    }

    /**
     *  The Access Token is a secret key that's been assigned to the Cisco Webex Spaces bot.  Each bot will have one.
     *  If you want to create a new bot, then get a new key at https://developer.ciscospark.com/apps.html
     *  
     * For security reasons, the accessToken is passed as an Environment variable called AT, 
     * which stands for Orchestration Access Token.
     */
    
    public static String getMyAccessToken() {
        String at = System.getenv().get("BOTAT");
        if ( at == null) {
            at = "Unknown Access Token";
        }
        return at;
    }
    
    /**
     * 
     * This is the port number that the Apache Spark server will listen on.
     * The specific number is stored as an Environment Variable which 
     * is read at run time.
     */
    
    public static int getApacheSparkPort() {
        String ApachePort = System.getenv().get("BOTPORT");
        int p = Integer.parseInt(ApachePort);
        return p;
    }   
    
    /**
     * The webhook URL can be configured manually at the Webex.cisco.com website,
     * OR you can enter it here, and then issues the command "createwebhook" to
     * the server... and this software will register the webhook for you.
     */
    
    private static final String myWebhook = "http://0.tcp.ngrok.io:12609/webhook";
    
    public static String getMyWebhook() {
        return myWebhook;
    }
     
    /**
     *  In some cases, you may want to restrict the users of a BOT to a particular
     *  Enterprise. Messages received can be filtered when appropriate.
     */
    private static final String emailDomain = "@verizon.net";
    public static String getEmailDomain() {
        return emailDomain;
    }
    
    
    /**
     * Consumer Key and Secrets are used as part of the E*Trade
     * authentication mechanism.   Each is a string that is stored as an environment
     * variable to prevent sensitive data from being stored here.
     */
    public static String getEtradeConsumerKey() {
        String at = System.getenv().get("ETCK");
        if ( at == null) {
            at = "Unknown Consumer Key";
        }
        return at;
    }
    public static String getEtradeConsumerSecret() {
        String at = System.getenv().get("ETCS");
        if ( at == null) {
            at = "Unknown Consumer Secret";
        }
        return at;
    }       
}
