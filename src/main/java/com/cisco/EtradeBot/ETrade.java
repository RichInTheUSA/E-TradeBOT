/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cisco.EtradeBot;

import static com.cisco.EtradeBot.Credentials.getEtradeConsumerKey;
import static com.cisco.EtradeBot.Credentials.getEtradeConsumerSecret;
import com.etrade.etws.account.Account;
import com.etrade.etws.account.AccountBalanceResponse;
import com.etrade.etws.account.AccountListResponse;
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
        public String oauth_consumer_key = null; // Your consumer key
        public String oauth_consumer_secret = null; // Your consumer secret
        public String oauth_request_token = null; // Request token 
        public String oauth_request_token_secret = null; // Request token secret 
    
    public void initETrade() throws IOException, ETWSException  { 
 
        client = OAuthClientImpl.getInstance(); // Instantiate IOAUthClient
        
        oauth_consumer_key = getEtradeConsumerKey();
        oauth_consumer_secret = getEtradeConsumerSecret();
        System.out.println ("Consumer key: " + oauth_consumer_key);
        System.out.println ("Consumer secret: " + oauth_consumer_secret);
        
        
        request = new ClientRequest(); // Instantiate ClientRequest

        request.setEnv(Environment.SANDBOX); // Use sandbox environment

        request.setConsumerKey(oauth_consumer_key); //Set consumer key
        request.setConsumerSecret(oauth_consumer_secret); // Set consumer secret
        
        token= client.getRequestToken(request); // Get request-token object
        oauth_request_token  = token.getToken(); // Get token string
        oauth_request_token_secret = token.getSecret(); // Get token secret
        
        System.out.println ("oauth_request_token: " + oauth_request_token);
        System.out.println ("oauth_request_token_secret: " + oauth_request_token_secret);
        
        request.setToken(oauth_request_token);
        request.setTokenSecret(oauth_request_token_secret);

        System.out.println("Next Step");
        
        
  
        String authorizeURL = null;
        authorizeURL = client.getAuthorizeUrl(request);
        System.out.println("Authorization URL is: " + authorizeURL);
        System.out.println("Copy the URL into your browser. Get the verification code and type here");
        
            try {
                URI uri = new java.net.URI(authorizeURL);
                Desktop desktop = Desktop.getDesktop();
                desktop.browse(uri);
            } catch (URISyntaxException ex) {
                Logger.getLogger(ETrade.class.getName()).log(Level.SEVERE, null, ex);
            }
        

            System.out.println("calling get_verification_code");
            String oauth_verify_code = get_verification_code();
            //oauth_verify_code = Verification(client,request);

        request.setVerifierCode(oauth_verify_code);
        
        token = client.getAccessToken(request);
        String oauth_access_token = token.getToken();
        String oauth_access_token_secret = token.getSecret();
        
        System.out.println("oauth access token " + oauth_access_token);
        System.out.println("oauth_access_secret " + oauth_access_token_secret);

        request.setToken(oauth_access_token);
        request.setTokenSecret(oauth_access_token_secret);
		
        // Get Account List
        
        try { 
            AccountsClient account_client = new AccountsClient(request);
            AccountListResponse response = account_client.getAccountList();


            List<Account> alist = response.getResponse();
            Iterator<Account> al = alist.iterator();
            while (al.hasNext()) {
                Account a = al.next();

                System.out.println("===================");
                System.out.println("Account: " + a.getAccountId());
                AccountBalanceResponse balance = account_client.getAccountBalance(a.getAccountId());
                System.out.println("Cash Balance: " + balance.getAccountBalance());
                System.out.println("===================");
            }
	} catch (Exception e) {
        }
      
     
        
        }
	
	public static String get_verification_code() {

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
