/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cisco.EtradeBot;

import com.etrade.etws.account.Account;
import com.etrade.etws.account.AccountListResponse;
import com.etrade.etws.oauth.sdk.client.IOAuthClient;
import com.etrade.etws.oauth.sdk.client.OAuthClientImpl;
import com.etrade.etws.oauth.sdk.common.Token;
import com.etrade.etws.sdk.client.ClientRequest;

/**
 *
 * @author richverjinski
 */
public class ETrade {

    public IOAuthClient client;
    public ClientRequest request = null;
    public Token token = null;
    public String oauth_consumer_key = null; // Your consumer key
    public String oauth_consumer_secret = null; // Your consumer secret
    public String oauth_request_token = null; // Request token 
    public String oauth_request_token_secret = null; // Request token secret   
 
    public static void initializeEtradeAPI(){
 
        client = OAuthClientImpl.getInstance(); // Instantiate IOAUthClient
        request = new ClientRequest(); // Instantiate ClientRequest

        request.setEnv(Environment.SANDBOX); // Use sandbox environment

        request.setConsumerKey(oauth_consumer_key); //Set consumer key
        request.setConsumerSecret(oauth_consumer_secret); // Set consumer secret
        token= client.getRequestToken(request); // Get request-token object
        oauth_request_token  = token.getToken(); // Get token string
        oauth_request_token_secret = token.getSecret(); // Get token secret    
        
    }
    
}