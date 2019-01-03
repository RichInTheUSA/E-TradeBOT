/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.RichInTheUSA.EtradeBot;

import static com.RichInTheUSA.EtradeBot.Credentials.getEtradeConsumerKey;
import static com.RichInTheUSA.EtradeBot.Credentials.getEtradeConsumerSecret;
import static com.RichInTheUSA.EtradeBot.ETradeBot.e;
import com.etrade.etws.account.Account;
import com.etrade.etws.account.AccountBalanceResponse;
import com.etrade.etws.account.AccountListResponse;
import com.etrade.etws.account.AccountPosition;
import com.etrade.etws.account.AccountPositionsRequest;
import com.etrade.etws.account.AccountPositionsResponse;
import com.etrade.etws.account.Alert;
import com.etrade.etws.account.Balance;
import com.etrade.etws.account.CashAccountBalance;
import com.etrade.etws.account.GetAlertDetailsResponse;
import com.etrade.etws.account.GetAlertsResponse;
import com.etrade.etws.account.MarginAccountBalance;
import com.etrade.etws.account.MarginLevel;
import static com.etrade.etws.account.SecurityTypeEnum.EQ;
import static com.etrade.etws.account.SecurityTypeEnum.MF;
import static com.etrade.etws.account.SecurityTypeEnum.OPTN;
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
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
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
    
    NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
    
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
        } catch (ETWSException e) {
            outboundMessage = "Hmmm, had trouble getting the Account information.";
            Logger.getLogger(ETradeBot.class.getName()).log(Level.SEVERE, null, e);
        } catch (IOException eio) {  
            Logger.getLogger(ETradeBot.class.getName()).log(Level.SEVERE, null, eio);
        }
        outboundMessage = obm.toString();
        
        System.out.println("Im in ShowAccountList " + outboundMessage);
        return obm.toString();
    }
    
    public String showBalances(String roomId, String message, String personId) throws IOException, ETWSException {
        StringBuilder obm = new StringBuilder();
        String outboundMessage = null;
        
        System.out.println("Im in ShowBalances");
        
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
                    AccountBalanceResponse balance = account_client.getAccountBalance(a.getAccountId());
                    obm.append("Account Id: ").append(balance.getAccountId()).append("  \n");
                    obm.append("Account Type: ").append(balance.getAccountType()).append("  \n");
                    obm.append("Account Option Level: ").append(balance.getOptionLevel()).append("  \n");
                    
                    
                    Balance b = balance.getAccountBalance();
                    obm.append("Cash Available for Withdrawal: ").append(b.getCashAvailableForWithdrawal().toString()).append("  \n");
                    
                    /*
                    obm.append("Cash Call: ").append(b.getTotalCash().toString()).append("  \n"); 
                    obm.append("Funds Witheld from Purchase Power: ").append(b.getFundsWithheldFromPurchasePower().toString()).append("  \n");
                    obm.append("Funds Witheld from Withdrawal: ").append(b.getFundsWithheldFromWithdrawal().toString()).append("  \n");                   
                    */
                    
                    if (balance.getAccountType() == MarginLevel.CASH) { 
                        CashAccountBalance cab = balance.getCashAccountBalance();
                        obm.append("Cash Available for Investment ").append(cab.getCashAvailableForInvestment().toString()).append("  \n"); 
                        obm.append("Cash Balance ").append(cab.getCashBalance().toString()).append("  \n"); 
                        //obm.append("Cash Settled for Investment ").append(cab.getSettledCashForInvestment().toString()).append("  \n"); 
                        //obm.append("Cash Unsettled for Investment ").append(cab.getUnSettledCashForInvestment().toString()).append("  \n"); 
                    }
                    
                    if (balance.getAccountType() == MarginLevel.MARGIN) { 
                        MarginAccountBalance mab = balance.getMarginAccountBalance();
                        obm.append("Margin Balance ").append(mab.getMarginBalance().toString()).append("  \n"); 
                    }
 
                    //obm.append("Cash Available for Investment: ").append(cab.getCashAvailableForInvestment().toString()).append("  \n");
                    //obm.append("Cash Balance: ").append(cab.getCashBalance().toString()).append("  \n");
                    //obm.append("Cash Settled for Investment: ").append(cab.getSettledCashForInvestment().toString()).append("  \n");
                    //obm.append("Cash Unsettled for Investment: ").append(cab.getUnSettledCashForInvestment().toString()).append("  \n");

                     
                    obm.append("===================  \n");
                }
        } catch (ETWSException e) {
            outboundMessage = "Hmmm, had trouble getting the Balance information.";
            Logger.getLogger(ETradeBot.class.getName()).log(Level.SEVERE, null, e);
        } catch (IOException eio) {  
            Logger.getLogger(ETradeBot.class.getName()).log(Level.SEVERE, null, eio);
        }
        
        outboundMessage = obm.toString();
        return obm.toString();
    }
    
    public String showPositions(String roomId, String message, String personId) throws IOException, ETWSException {
        StringBuilder obm = new StringBuilder();
        String outboundMessage = null;

        System.out.println("Im in ShowPositions");

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
                    String accountId = a.getAccountId();


                    AccountPositionsResponse aprs = null;
                    AccountPositionsRequest apr = new AccountPositionsRequest();

                    apr.setCount("10");
                    //apr.setMarker("Your marker value");
                    //apr.setSymbol("Your symbol");
                    //apr.setTypeCode("Your type code"); // EQ, OPTN, MF, BOND
                    aprs = account_client.getAccountPositions(accountId,apr);

                    obm.append("===================  \n");
                    obm.append("Account ID: ").append(aprs.getAccountId()).append("  \n");
                    obm.append("===================  \n");                   
                    List<AccountPosition> l = aprs.getResponse();
                    Iterator<AccountPosition> api = l.iterator();

                    while (api.hasNext()) {
                        AccountPosition ap = api.next();
                        obm.append("Position: ").append(ap.getProductId().getSymbol()).append(", Qty: ").append(ap.getQty().toString());
                        obm.append(", Current Price: ").append(format.format(ap.getCurrentPrice())).append(", Market Value: ").append(format.format(ap.getMarketValue())).append("  \n");
                    }

                    obm.append("===================  \n\n");
                }
        } catch (ETWSException e) {
            outboundMessage = "Hmmm, had trouble getting the Balance information.";
            Logger.getLogger(ETradeBot.class.getName()).log(Level.SEVERE, null, e);
        } catch (IOException eio) {  
            Logger.getLogger(ETradeBot.class.getName()).log(Level.SEVERE, null, eio);
        }

        outboundMessage = obm.toString();
        return obm.toString();
    }

    public String listAlerts(String roomId, String message, String personId) throws IOException, ETWSException {
        StringBuilder obm = new StringBuilder();
        String outboundMessage = null;

        System.out.println("Im in ListAlerts");

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
            GetAlertsResponse alertResponse = account_client.getAlerts();

            List<Alert> alertList = alertResponse.getResponse();
            Iterator<Alert> lert = alertList.iterator();

            obm.append("===================  \n");
            obm.append("Account Alerts").append("  \n");

            while (lert.hasNext()) {
                Alert a = lert.next();
                obm.append("Alert Id: ").append(a.getAlertId());
                obm.append(", Subject: ").append(a.getSubject()).append(", Symbol: ").append("  \n");
            }
            obm.append("===================  \n\n");
        
        } catch (ETWSException e4) {
            outboundMessage = "Hmmm, had trouble getting the Alert information.";
            Logger.getLogger(ETradeBot.class.getName()).log(Level.SEVERE, null, e4);
        } catch (IOException eio4) {  
            Logger.getLogger(ETradeBot.class.getName()).log(Level.SEVERE, null, eio4);
        }
        outboundMessage = obm.toString();
        return obm.toString();
    }    
   
    public String readAlerts(String roomId, String message, String personId) throws IOException, ETWSException {
        StringBuilder obm = new StringBuilder();
        String outboundMessage = null;

        System.out.println("Im in ReadAlerts");

        if (ETradeInitialized == false ) {
            outboundMessage = needToInit(roomId, message, personId);
            return outboundMessage;
        }
        if (ETradeAuthenticated == false ) {
            outboundMessage = needToAuth(roomId, message, personId);
            return outboundMessage;
        }        
        
        StringTokenizer st = new StringTokenizer(message);
        String alert = null;
        Boolean found = false;
        
        while (st.hasMoreTokens()) {
            alert = st.nextToken();
            if (alert.equals("alerts") || alert.equals("alert")) {
                found = true;
                alert = st.nextToken();
                if (alert == "") {
                  obm.append("You must include an alert number. Try **list alerts** to get the numbers.  \n\n");  
                }
            }
           
            if (found == true) {
            
                try {     
                    AccountsClient account_client = new AccountsClient(request);
                    System.out.println("Alert #: " + alert);
                    GetAlertDetailsResponse alertDetails = account_client.getAlertDetail(Long.valueOf(alert));

                    obm.append("===================  \n");
                    obm.append("Alert Subject: ").append(alertDetails.getSubject()).append(", ID: ").append(alertDetails.getAlertId()).append("  \n");
                    obm.append("Alert Message: ").append(alertDetails.getMsgText()).append("  \n\n");
                    obm.append("===================  \n\n");

                } catch (ETWSException e5) {
                    outboundMessage = "Hmmm, had trouble getting the Alert information.";
                    Logger.getLogger(ETradeBot.class.getName()).log(Level.SEVERE, null, e5);
                } catch (IOException eio5) {  
                    Logger.getLogger(ETradeBot.class.getName()).log(Level.SEVERE, null, eio5);
                }
            }
        }
        
        outboundMessage = obm.toString();
        return obm.toString();
    }         
}
