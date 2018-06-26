/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cisco.EtradeBot;


import static com.cisco.EtradeBot.SmartSheet.processSmartSheetRequest;
import com.ciscospark.Message;
import com.ciscospark.Spark;
import com.ciscospark.Webhook;
import com.ciscospark.Person;
import com.etrade.etws.sdk.common.ETWSException;
import com.smartsheet.api.SmartsheetException;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author richverjinski
 */
public class ETradeBot {

    // Declare a few public static variables needed to talk to the Cisco Spark Server

    /**
     *  Reference to the Cisco Spark Server.
     */
    public static Spark spark = null;

    /**
     *  Reference to the Webhook URL that the Spark Server 
     *  will use to connect back to the bot.
     */
    public static URL myTargetURL = null;

    /**
     *  Reference to the Webhook URI... similar to the URL.
     */
    public static URI myTargetURI = null;
    
    
    /**
     *  This the person Id of the bot. When creating a new bot, I generally
     *  receive this via the API, and then capture it here.  It's used so that
     *  the bot does not respond to questions from itself (which can cause an infinite loop).
     * 
     */
    public static String myPersonId = Credentials.getMyPersonId();

    /**
     *  This is a secret key that's been assigned to the bot.  Each bot will have one.
     */
    public static String accessToken = Credentials.getMyAccessToken();
    
    public static ETrade e;
    
    /**
     * This is the main method of the Application.
     * @param args the command line arguments... but it does not take any.
     */
    public static void main(String[] args) throws ETWSException {
        
        //This section initializes the bot.
        System.out.println("Start Initializing");
        
        // Initialize SmartSheet API
        /*
        try {
            SmartSheet.initializeSmartSheetAPI();
        } catch (SmartsheetException ex) {
            Logger.getLogger(ETradeBot.class.getName()).log(Level.SEVERE, null, ex);
        }  
        System.out.println("... Initializing SmartSheet API");
      
        */
        // Initialize Cisco Spark API
        spark = Spark.builder()
        .baseUrl(URI.create("https://api.ciscospark.com/v1"))
        .accessToken(accessToken)
        .build();
        System.out.println("... Initializing Cisco Spark API");
      
        
        // Initialize the Apache Spark server
        ApacheSpark apache = new ApacheSpark();
        System.out.println("... Initializing Apache Spark API");
        
        System.out.println("Spark Bot Initialized!  \n\n");
    
            // Initialize the ETrade API
     
        e = new ETrade();
        System.out.println("... Initializing Etrade API");
        
        // e.getAccountList();
        // System.out.println("... Finished account list");
        
    }

    /**
     * Help  This returns a help message back to the user
     * @return
     */
    public static String help() {
        return "Try any of the following...  \n" +
                "-- show **portfolio** [** all | description | function | pillar | sponsor | pm | biz lead | it lead | last update | health | color | archive** ]  \n" +
                "-- show **portfolio** status [** without updates | red | yellow | green **]  \n" +   
                "-- show [** all | description | function | pillar | sponsor | pm | biz lead | it lead | last update | health | color | archive** ] **program** &lt; portion of program name&gt;  \n" +
                " Note, Archived programs will not be shown by default.  They will be shown if explicitly mentioned.  \n\n" +
                "You can also access your own smartsheets:  \n" +
                "-- show **folders**  \n" +
                "-- show **workspaces**  \n" +
                "-- show **sheets** [**id**]   \n" +
                "-- show **columns** [**sheet** <sheetname>]  \n" +
                "-- **search** &lt;data&gt;  \n" +
                "-- **set-token** &lt;smartsheet token&gt;.  " +
                " To get a token, log into smartsheet, then go to  \n" +
                " Account -> Personal Settings -> API Access, and generate" +
                " an access token.  Enter it using the **set-token** command.  \n\n" +
                " Admins can also type **admin help** for a list of additional commands";


    }
    
    /**
     * hello - Returns the same info as help.
     * @return
     */
    public static String hello() {
        return help();
    }
    
    /**
     * Admin Help - This method lists all various commands that are foundational
     *  to bots. 
     * @return String
     */
    public static String adminHelp() {
        return "Admin help - Lists commands for bot admins.  \n" 
                + "- Admin help - Guidance for Bot admins  \n" 
                + "- Hello - Returns the help message  \n"
                + "- Support - returns the person responsible for this app  \n"
                + "- List rooms - List rooms that this bot is used in  \n"
                + "- Create webhook [&lt;url&gt;]- Create a webhook  \n"                
                + "- List webhooks - List registered webhooks  \n"
                + "- Delete webhook &lt;webhook Id&gt; - Delete the webhook  \n"
                + "- Delete all webhooks but &lt;webhook Id&gt; - Delete all execpt the one specified.  \n"               
                + "- Get messages - Get messsages from all rooms  \n\n"
                + "If you are an admin and know the URL for the host running this bot."
                + " You can issue the same commands to the browser. "
                + "Just remove the spaces between words and use lower case, "
                + "so 'list webhooks' would be http://&lt;hostname.com&gt;:5671/listwebhooks";
    }
    
    /**
     * Support - This 
     * @return
     */
    public static String support() {
        return "Rich Verjinski, rverjins@cisco.com  \n";
    }
    
    /**
     * Privacy - provides a message to the use on data privacy 
     * @return String
     */
    public static String privacy() {
        return "This should tell the user how the data is being used (or not used).  \n" +
               "the support page should have a privacy statement";
    }
    
    /**
     * List Rooms - this method shows the various rooms and 1-1 conversations that the bot has had.
     * @return String
     */
    public static String listRooms() {
        StringBuilder s = new StringBuilder();
        s.append("<p>");
        spark.rooms()
                .iterate()
                .forEachRemaining(room -> {
                    s.append("Room Title: **").append(room.getTitle()).append("**  \n");
                    s.append("Created :").append(room.getCreated()).append("  \n");
                    // s.append("RoomId: ").append(room.getId()).append("  \n");
                    // s.append("TeamID: ").append(room.getTeamId()).append("  \n");
                    s.append("IsLocked: ").append(room.getIsLocked()).append("  \n");
                    s.append("Last activtiy on: ").append(room.getLastActivity().toString()).append("  \n\n");
                });
        return s.toString();
    }
    
    /**
     * List Webhooks - Webhooks are how the Spark Server notifies the bot that 
     * it has a message (or other activity).
     * 
     * Because the Spark Server is OUTSIDE of Cisco's firewall, and the spark bot
     * is inside the firewall, it's necessary to use ngrok.com to set up a 
     * tunnel to the bot web server.
     * @return String
     */
    public static String listWebHooks() {
        StringBuilder s = new StringBuilder();
        spark.webhooks()
             .iterate()
             .forEachRemaining (hook -> {
                 s.append("Webhook Name: **").append(hook.getName()).append("**  \n");
                 s.append("Webhook Id: ").append(hook.getId()).append("  \n");
                 s.append("Webhook URL: ").append(hook.getTargetUrl()).append("  \n\n");
             });   
        return s.toString();
    }
   
    /**
     * Delete Webhook - This is used for debugging and maintenance only, and 
     *  actually does not work right now.   It's supposed to check with the
     *  server and list each webhook... and then delete them!
     * @return
     */
    public static String deleteWebHook(String Id) {
        StringBuilder s = new StringBuilder();
        
        /* Delete all webhooks
        spark.webhooks()
             .iterate()
             .forEachRemaining (hook -> {
                s.append("Webhook Name: **").append(hook.getName()).append("**  \n");
                s.append("Webhook for Room: ").append(hook.getFilter()).append("  \n");
                s.append("Webhook URL: ").append(hook.getTargetUrl()).append("  \n");
               
                spark.webhooks().path("/" + hook.getId() ).delete();
                
             });
        */
        
        spark.webhooks().path("/" + Id ).delete();
        s.append("Deleted webhook with Id: " + Id + "  \n");
        return s.toString();
    }
   
    public static String deleteAllWebHooksBut(String Id) {
        StringBuilder s = new StringBuilder();
        
        //Delete all webhooks except the one passed.
        s.append("Searching for Webhooks to delete... \n");
        spark.webhooks()
             .iterate()
             .forEachRemaining (hook -> {
                if (! hook.getId().equals(Id)) {
                s.append("Deleting Webhook  \n");                    
                s.append("-  Name: ").append(hook.getName()).append("  \n");
                s.append("-  Id: ").append(hook.getId()).append("  \n");
                s.append("-  URL: ").append(hook.getTargetUrl()).append("  \n");                    
                  
                spark.webhooks().path("/" + hook.getId() ).delete();
                }
             });
        
        s.append("Completed task. \n");
        return s.toString();
    }

    /**
     *
     * Create Generic Webhook -  This method can be called from the local
     *   Apache Spark server web interface to register a webhook with the server.
     * @return String indicating success or failure.
     */
    public static String createWebHook(String Id) {
        
        // Convert the webhook URL to a URI.
        try {
            //  myTargetURL = new URL("http://6b421d72.ngrok.io/webhook");
            
            if (Id.equals("")) {
              myTargetURL = new URL(Credentials.getMyWebhook());
            } else {
              myTargetURL = new URL(Id);
            }
            myTargetURI = myTargetURL.toURI();
        } catch (MalformedURLException | URISyntaxException ex) {
            Logger.getLogger(ETradeBot.class.getName()).log(Level.SEVERE, null, ex);
        }
        StringBuilder s = new StringBuilder();
       
        // Create a new Webhook object, fill in the parameters, and post!
        Webhook myWebhook = new Webhook();
        myWebhook.setName("Etrade Webhook");
        myWebhook.setTargetUrl(myTargetURI);
        myWebhook.setResource("messages");
        myWebhook.setEvent("created");
        spark.webhooks().post(myWebhook);
        
        s.append("Created a new webhook");
        return s.toString();            
    }  
    
    /**
     * Get Messages - This method asks for a list of all the conversations it
     * has had, and then dumps out the commands that the users have issued to the bot.
     * @return String
     */
    public static String getMessages() {
        StringBuilder s = new StringBuilder(); 
        
        // Contact the Spark Server... then for each room, iteratively
        // get the messages and print them out.
        spark.rooms()
                .iterate()
                .forEachRemaining(room -> {
                    s.append("\n**Messages for Room: ").append(room.getTitle()).append("**  \n");
                    if (room.getTeamId() != null) {
                        try {
                          spark.messages()
                          .queryParam("roomId", room.getId())
                          .queryParam("mentionedPeople", "me" )  // not for 1-1 rooms
                          .iterate()
                          .forEachRemaining(message1 -> {
                            s.append("  ").append(message1.getText()).append("  \n");
                          });
                        } catch (Exception e) {
                            s.append("EXCEPTION  \n");
                        }
                    
                    } else {
                        s.append("Cant get messages due to spark limitation.  \n");
                        try {
                          spark.messages()
                          .queryParam("roomId", room.getId())
                          .queryParam("mentionedPeople", "")
                          .iterate()
                          .forEachRemaining(message1 -> {
                            s.append("  ").append(message1.getText()).append("  \n");
                          });
                        } catch (Exception e) {
                            s.append("EXCEPTION  \n");
                        } 
                    }    
                    s.append(" ");
                });
        return s.toString();
    }                    
  
    /**
     * Process Messages -  This is the method that is kicked off when a webhook
     * is received by the Apache Spark server. The apache server then calls 
     * this method to parse the message.
     * @param roomId  - The room that the message came from, so that bot can respond.
     * @param messageId - The message Id so that the message can be parsed.
     * @param personId - The person Id who sent the message.
     */
    public static void processMessages(String roomId, String messageId, String personId) {
        String outboundMessage = null;
        StringBuilder obm = new StringBuilder();
        String operator;
        StringBuilder inboundSring = new StringBuilder();
        
        if (personId.equals(myPersonId)) {
            // This checks to see if the message received is from the Bot itself.
            // If so.. ignore it, as we dont want get into an endless loop.
        } else {
            
            Message message = spark.messages().path("/"+ messageId, Message.class).get();
            System.out.println("\nReceived from : " + message.getPersonEmail());
            
            // Check to see if the user is in the correct domain.
            if ( message.getPersonEmail().contains(Credentials.getEmailDomain()) ) { 
                System.out.println("Received: " + message.getText());

                // Convert the message to all lower case... it's a lot easier to parse.
                String s = message.getText().toLowerCase();

                // Check for certain key words, which the bot supports
                if (s.contains("admin help")) { outboundMessage = adminHelp();}
                else if (s.contains("help")) { outboundMessage = help();}
                else if (s.contains("hello")) { outboundMessage = hello();}
                else if (s.contains("support")) { outboundMessage = support();}
                else if (s.contains("privacy")) { outboundMessage = privacy();}
                else if (s.contains("list rooms")) { outboundMessage = listRooms();}
                else if (s.contains("list webhook")) { outboundMessage = listWebHooks();}
                else if (s.contains("delete webhook")) { 

                    StringTokenizer st;
                    String word, theID = "";
                    st = new StringTokenizer(message.getText());
                    while (st.hasMoreTokens()) {
                        word = st.nextToken();
                        if (word.equals("webhook")) {
                            if (st.hasMoreTokens())
                            theID = st.nextToken();
                        }
                    }
                    
                    outboundMessage = deleteWebHook(theID);
                }
                else if (s.contains("delete all webhooks but")) { 
                    
                    StringTokenizer st;
                    String word, theID = "";
                    st = new StringTokenizer(message.getText());
                    while (st.hasMoreTokens()) {
                        word = st.nextToken();
                        if (word.equals("but")) {
                            if (st.hasMoreTokens())                            
                              theID = st.nextToken();
                        }
                    }                    
                    outboundMessage = deleteAllWebHooksBut(theID);
                }                
                else if (s.contains("create webhook")) {                     
                    StringTokenizer st;
                    String word, theHook = "";
                    st = new StringTokenizer(message.getText());
                    while (st.hasMoreTokens()) {
                        word = st.nextToken();
                        System.out.println(word + " ");
                        if (word.equals("webhook")) {
                            if (st.hasMoreTokens())
                              theHook = st.nextToken();
                        }
                    } 
                    System.out.println("theHook :*"+theHook+"*");
                    outboundMessage = createWebHook(theHook);
                
                }                
                else if (s.contains("get messages")) { 
                    outboundMessage = getMessages();
                }
                
                /**************************************************************/
                /* Add Etrade App Specific Commands here                      */
                /**************************************************************/
                
                else if (s.contains("authorize etrade"))  { 
                    
                    try { 
                        outboundMessage = e.authorizeETrade(roomId, s, personId);
                    } catch (ETWSException ex) {
                        Logger.getLogger(ETradeBot.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ie) {
                        Logger.getLogger(ETradeBot.class.getName()).log(Level.SEVERE, null, ie);
                    }
                
                }
                else if (s.contains("key"))  { 
                    
                    try { 
                        outboundMessage = e.parseETradeKey(roomId, s, personId);
                    } catch (ETWSException ex) {
                        Logger.getLogger(ETradeBot.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ie) {
                        Logger.getLogger(ETradeBot.class.getName()).log(Level.SEVERE, null, ie);
                    }
                
                }
                else if (s.contains("show accounts"))  { 
                    
                    try { 
                        outboundMessage = e.showAccountList(roomId, s, personId);
                    } catch (ETWSException ex) {
                        Logger.getLogger(ETradeBot.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ie) {
                        Logger.getLogger(ETradeBot.class.getName()).log(Level.SEVERE, null, ie);
                    }   
                } 
                else if (s.contains("revoke"))  { 
                    try { 
                        outboundMessage = e.revokeAccessToken(roomId, s, personId);
                    } catch (ETWSException ex) {
                        Logger.getLogger(ETradeBot.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ie) {
                        Logger.getLogger(ETradeBot.class.getName()).log(Level.SEVERE, null, ie);
                    }    
                    
                } else if (s.contains("show balances"))  { 
                    
                    try { 
                        outboundMessage = e.showAccountList(roomId, s, personId);
                    } catch (ETWSException ex) {
                        Logger.getLogger(ETradeBot.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ie) {
                        Logger.getLogger(ETradeBot.class.getName()).log(Level.SEVERE, null, ie);
                    }
                        
                }
                else {
                   obm.append("I heard you say: ").append(message.getText()).append("  \n");
                   obm.append("Not really sure what to do with that.  \n\n");
                   obm.append(help());
                   outboundMessage = obm.toString();
                }
            } else {
                obm.append("This bot only responds to users with email in the ").append(Credentials.getEmailDomain()).append(" domain.");
                outboundMessage = obm.toString();
                    
            }
            
            // Send the resulting message back to the user!
            ETradeBot.sendMessage( roomId, outboundMessage, personId);
            
        } 
    }  
    
    /**
     * My Get Display Name - takes a person Id, and then returns their 
     * display name.   ie. Rich Verjinski
     * @param personID
     * @return String
     */
    public static String myGetDisplayName(String personID) {
        Person p = new Person();
        p = spark.people().path("/" + personID).get();
        System.out.println("display name" + p.getDisplayName() );
        return (p.getDisplayName());
    }
    
    /**
     * My Get Email Name - takes a person ID and returns their email address.
     * Spark can have supposedly have multiple email addresses... but I've only
     * seen one per person.
     * @param personID
     * @return String  email address
     */
    public static String myGetEmailName(String personID) {
        // System.out.println("In processMention, processing:" + personID);
        Person p = new Person();
        p = spark.people().path("/" + personID).get();
        String[] emails = p.getEmails();
        System.out.println("emails: " + emails[0]);
        return (emails[0]);
    }    
    
    /**
     * Send Message - This method sends a response or a notification back to the user.
     * @param RoomId
     * @param outboundMessage
     * @param personId
     */
    public static void sendMessage (String RoomId, String outboundMessage, String personId) {
   
        Message message = new Message();
        message.setRoomId(RoomId);
        message.setMarkdown(outboundMessage);
        spark.messages().post(message);
    }
    
    
    // All the other code snippets below here are just example code..... 
    // Keeping it around for examples.  
    
        /* List the members of the team
        spark.teamMemberships()
                .queryParam("teamId", "Y2lzY29zcGFyazovL3VzL1RFQU0vYzQwYjI3MDAtZDFlNS0xMWU2LWIyNmEtM2I1MGVmMWFkOWRm")
                .iterate()
                .forEachRemaining(member -> {
                    System.out.println(member.getPersonEmail());
                });
        
        
        // List the details for spefic message ID
        /* 
        Message message = spark.messages().path("/"+ msgId, Message.class).get();
        String text = message.getText();
        //String message = spark.messages().path("/"+ msgId).get().getText();
        System.out.println(text);
        */
        
        /* 
        // Share a file with the room
        message = new Message();
        message.setRoomId(room.getId());
        message.setFiles(URI.create("http://example.com/hello_world.jpg"));
        spark.messages().post(message);
        
        */
        
        
        /* Create a new team
        Team team = new Team();
        team.setName("BOTTESTTEAM");
        team = spark.teams().post(team);
        */

        // Add a coworker to the team
        /*
        TeamMembership teamMembership = new TeamMembership();
        teamMembership.setTeamId(team.getId());
        teamMembership.setPersonEmail("wile_e_coyote@acme.com");
        spark.teamMemberships().post(teamMembership);
        */

        // List the members of the team
        /*
        spark.teamMemberships()
                .queryParam("teamId", team.getId())
                .iterate()
                .forEachRemaining(member -> {
                    System.out.println(member.getPersonEmail());
                });
        */  
    
}
