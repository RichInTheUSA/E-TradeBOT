/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cisco.EtradeBot;

import static com.cisco.EtradeBot.Credentials.getEtradeConsumerKey;
import static com.cisco.EtradeBot.Credentials.getEtradeConsumerSecret;
import static com.cisco.EtradeBot.ETradeBot.e;
import com.etrade.etws.account.Account;
import com.etrade.etws.account.AccountBalanceResponse;
import com.etrade.etws.account.AccountListResponse;
import com.etrade.etws.account.AccountPositionsResponse;
import com.etrade.etws.oauth.sdk.client.IOAuthClient;
import com.etrade.etws.oauth.sdk.client.OAuthClientImpl;
import com.etrade.etws.oauth.sdk.common.Token;
import com.etrade.etws.sdk.client.AccountsClient;
import com.etrade.etws.sdk.client.ClientRequest;
import com.etrade.etws.sdk.client.Environment;
import com.etrade.etws.sdk.common.ETWSException;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.httpclient.contrib.ssl.*;


/**
 *
 * @author richverjinski
 */
public class ETrade {

    public IOAuthClient client = null;
    public ClientRequest request = null;
    public Token token = null;
    public String oauth_consumer_key = null;        // Your consumer key
    public String oauth_consumer_secret = null;     // Your consumer secret
    public String oauth_request_token = null;       // Request token 
    public String oauth_request_token_secret = null; // Request token secret 
    public Boolean ETradeInitialized = false;
    public Boolean ETradeAuthenticated = false;
    
    public String authorizeETrade(String roomId, String message, String personId) throws IOException, ETWSException  { 
        StringBuilder obm = new StringBuilder();
        String outboundMessage = null;
        client = OAuthClientImpl.getInstance();         // Instantiate IOAUthClient
        
        oauth_consumer_key = Credentials.getEtradeConsumerKey();
        oauth_consumer_secret = Credentials.getEtradeConsumerSecret();
        //System.out.println ("Consumer key: " + oauth_consumer_key);
        //System.out.println ("Consumer secret: " + oauth_consumer_secret);
        
        request = new ClientRequest();                  // Instantiate ClientRequest
        request.setEnv(Environment.SANDBOX);            // Use sandbox environment
        
        request.setConsumerKey(oauth_consumer_key);       //Set consumer key
        request.setConsumerSecret(oauth_consumer_secret); // Set consumer secret
        
        token= client.getRequestToken(request);         // Get request-token object
        oauth_request_token  = token.getToken();        // Get token string
        oauth_request_token_secret = token.getSecret(); // Get token secret
        
        if ((oauth_request_token != null) && (oauth_request_token_secret != null)) {
            ETradeInitialized = true;
            ETradeAuthenticated = false;
        }
        
        //System.out.println ("Oauth_request_token: " + oauth_request_token);
        //System.out.println ("Oauth_request_token_secret: " + oauth_request_token_secret);
        
        request.setToken(oauth_request_token);          // Now that we have the token, use it in our requests.
        request.setTokenSecret(oauth_request_token_secret);

        String authorizeURL = null;                     // Get the URL to authorize access.
        authorizeURL = client.getAuthorizeUrl(request);
        
        System.out.println("URL is " + authorizeURL);
        
        //System.out.println("Authorization URL is: " + authorizeURL);
        // Launch a browser window, which will take the user to Etrade.com to get a verification code.
        /*
        try {
        URI uri = new java.net.URI(authorizeURL);
        Desktop desktop = Desktop.getDesktop();
        desktop.browse(uri);
        } catch (URISyntaxException ex) {
        Logger.getLogger(ETrade.class.getName()).log(Level.SEVERE, null, ex);
        }
         */
        obm.append("Click [here](").append(authorizeURL).append (") to securely authenticate with E*Trade to obtain a Verification Key  \n\n"); ;
        obm.append("Enter **key** followed by the verification key from E*Trade (space separated).  \n");
        obm.append("Dont worry... this is sandbox data!  No access to your real data");
        outboundMessage = obm.toString();
        
        return outboundMessage;
    }

    public String needToInit(String roomId, String message, String personId) {
        String outboundMessage = null;
        
        System.out.println("Im in needToInit");
        
        if (ETradeInitialized == false ) {
            try { 
                outboundMessage = e.authorizeETrade(roomId, message, personId);
                ETradeInitialized = true;
            } catch (ETWSException ex) {
                Logger.getLogger(ETradeBot.class.getName()).log(Level.SEVERE, null, ex);
                outboundMessage = "Problem Initializing with ETrade...  \n";
            } catch (IOException ie) {
                Logger.getLogger(ETradeBot.class.getName()).log(Level.SEVERE, null, ie);
                outboundMessage = "Problem Initializing with ETrade...  \n";
            }        
        }
        
        System.out.println("Im in needToInit " + outboundMessage);
        return outboundMessage;
    }
    
    public String needToAuth(String roomId, String message, String personId) {
        String outboundMessage = null;
        StringBuilder obm = new StringBuilder();
        
        System.out.println("Im in needToAuth");
        
        if (ETradeAuthenticated == false ) {
            obm.append("Enter **key** followed by the verification key from E*Trade (space separated).  \n");
            obm.append("Dont worry... this is sandbox data!  No access to your real data  \n\n");
            obm.append("To regenerate another link, type **Init ETrade**  \n");
            outboundMessage = obm.toString();
        }
        
        System.out.println("Im in needToAuth " + outboundMessage);
        return outboundMessage;
    }
    
    public String parseETradeKey(String roomId, String message, String personId) throws IOException, ETWSException { 
        String outboundMessage = null;
        
        if (ETradeInitialized == false ) {
            outboundMessage = needToInit(roomId, message, personId);
            return outboundMessage;
        }
        if (ETradeAuthenticated == true ) {
            outboundMessage = "You are already Authenticated. Type **help**  \n";
            return outboundMessage;
        }
        
        
        // Get the verify code from the user
        // String oauth_verify_code = get_verification_code();
        String word;
        String oauth_verify_code = null;
        
        StringTokenizer st = new StringTokenizer(message);
        while (st.hasMoreTokens()) {
            word = st.nextToken();
            if (word.equals("key")) {
                oauth_verify_code =st.nextToken();
            }
        }
        System.out.println("Verification code is: " + oauth_verify_code.toUpperCase());
        
        request.setVerifierCode(oauth_verify_code.toUpperCase());
        
        String oauth_access_token = null;
        String oauth_access_token_secret = null;
        
        try {
            token = client.getAccessToken(request);
            oauth_access_token = token.getToken();
            oauth_access_token_secret = token.getSecret();
        } catch (Exception e) {
            Logger.getLogger(ETradeBot.class.getName()).log(Level.SEVERE, null, e);
            System.out.println("Got snagged in the catch");
            ETradeInitialized = false;
        }
    
        System.out.println("oauth access token " + oauth_access_token);
        System.out.println("oauth_access_secret " + oauth_access_token_secret);
        
        if ((oauth_access_token != null) && (oauth_access_token_secret !=null)) {
            request.setToken(oauth_access_token);
            request.setTokenSecret(oauth_access_token_secret);
            outboundMessage = "Authenticated with ETrade. Session will end at midnight US Eastern Time, or after 2 hours of no activity.";
            ETradeAuthenticated = true;
        } else {
            outboundMessage = "Authentication FAILED with ETrade";
            ETradeInitialized = false;
        }
       
        return outboundMessage;
		
    }
    
    public String revokeAccessToken (String roomId, String message, String personId) throws IOException, ETWSException {
    
       String outboundMessage = null;
        try {
            client.revokeAccessToken(request);
            // oauth_access_token = token.getToken();
            // oauth_access_token_secret = token.getSecret();
            
            ETradeInitialized = false;
            ETradeAuthenticated = false;
            outboundMessage = "Any prior Etrade Authentication for this session has been revoked. You must re-authenticate to continue.";
            
        } catch (ETWSException e) {
            outboundMessage = "Hmmm, doesnt look like there was anything to revoke.";
            Logger.getLogger(ETradeBot.class.getName()).log(Level.SEVERE, null, e);
        } catch (IOException eio) {  
            Logger.getLogger(ETradeBot.class.getName()).log(Level.SEVERE, null, eio);
        }
       return outboundMessage;
    }
    
    public String showAccountList(String roomId, String message, String personId) throws IOException, ETWSException {
        StringBuilder obm = new StringBuilder();
        String outboundMessage = null;
        
        System.out.println("Im in showAccountList");
        
        if (ETradeInitialized == false ) {
            outboundMessage = needToInit(roomId, message, personId);
            return outboundMessage;
        }
        if (ETradeAuthenticated == false ) {
            outboundMessage = needToAuth(roomId, message, personId);
            return outboundMessage;
        }        
        
        try { 
                AccountsClient account_client = new AccountsClient(request);
                AccountListResponse response = account_client.getAccountList();

                List<Account> alist = response.getResponse();
                Iterator<Account> al = alist.iterator();
                while (al.hasNext()) {
                    Account a = al.next();

                    obm.append("===================  \n");
                    obm.append("Account Desc: ").append(a.getAccountDesc()).append("  \n");
                    obm.append("Account Id: ").append(a.getAccountId()).append("  \n");
                    obm.append("Account Margin Level: ").append(a.getMarginLevel()).append("  \n");
                    obm.append("Account Value: ").append(a.getNetAccountValue()).append("  \n");
                    obm.append("Account Registration: ").append(a.getRegistrationType()).append("  \n");
                    obm.append("===================  \n");
                }
        } catch (Exception e) {
        }
        outboundMessage = obm.toString();
        
        System.out.println("Im in ShowAccountList " + outboundMessage);
        return obm.toString();
    }
    

//No longer used
    public static String get_verification_code() {
        System.out.print("Enter verification code: ");
        try{
            BufferedReader br =
            new BufferedReader(new InputStreamReader(System.in));

            String input;

            input=br.readLine();
            return input;
        }catch(IOException io){
            io.printStackTrace();
            return "";
        }
   
    }
    
}
