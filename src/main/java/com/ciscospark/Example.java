/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ciscospark;

// import com.ciscospark.*;
import java.net.URI;
import java.net.URL;

import java.net.*;
import java.io.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


class Example {
    
    public static void main(String[] args) throws MalformedURLException, ParserConfigurationException, URISyntaxException, SAXException, IOException {
        // To obtain a developer access token, visit http://developer.ciscospark.com
        // String accessToken = "<<secret>>";
        String accessToken = "ZjljMjdmOTktNGVlMS00ZWE2LTg4ZWMtYTY4ZTA0NTdhMzQyMzdiYjg0NzYtOTU1";

        // Initialize the client
        Spark spark = Spark.builder()
                .baseUrl(URI.create("https://api.ciscospark.com/v1"))
                .accessToken(accessToken)
                .build();
        
        // List the rooms that I'm in
     
        System.out.println("Here is a list of rooms that I'm in...");
        spark.rooms()
                .iterate()
                .forEachRemaining(room -> {
                    System.out.println(room.getTitle() + ", created " + room.getCreated() + ": " + room.getId());
                    System.out.println("TeamID: " + room.getTeamId());
                    System.out.println("IsLocked: " + room.getIsLocked());
                    System.out.println("Last activtiy on: " + room.getLastActivity());
                    System.out.println("");
                    
                    // Message message = (Message)spark.messages()).setRoomId(room.getId());
                    System.out.println("example");
           
                });
        
        
        /* 
        String msgId = "msgid" ;
        spark.rooms().
        
        
        // List the details for spefic message ID
        Message message = spark.messages().path("/"+ msgId, Message.class).get();
        String text = message.getText();
        //String message = spark.messages().path("/"+ msgId).get().getText();
        System.out.println(text);
        
        // String AuthValue = "Bearer " + accessToken;
        /* 
        String myRequestBinURL = "http://requestb.in/1f4pf6l1";
        String RichsBOTRoom = "Y2lzY29zcGFyazovL3VzL1JPT00vYTBlNDk1MzAtZDFmMC0xMWU2LWEyNmYtYWY4MTVkNzAyMjc4";
        
        Webhook myWebhook = (Webhook) spark.webhooks();
   
       
        URL url = new URL("https://api.ciscospark.com/v1/webhooks");
        URI uri = url.toURI();
        */
        //URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
        /*
  
  
        myWebhook.setTargetUrl(uri);
        myWebhook.setResource("messages");
        myWebhook.setEvent("created");
        myWebhook.setName("Richs BOT Room");
        myWebhook.setFilter(RichsBOTRoom);
        Webhook result = spark.webhooks().post(myWebhook);
        */ 
        
        /*
        try { 
          
            URL webHooksURL = new URL("https://api.ciscospark.com/v1/webhooks");
            
            connection = (HttpURLConnection) webHooksURL.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoInput(true);  // default
            connection.setDoOutput(true);
            
            connection.setRequestProperty("Authorization", AuthValue);
            connection.addRequestProperty("Content-type", "application/json");
            
            connection.addRequestProperty("resource", "message");
            connection.addRequestProperty("event", "created");
            connection.addRequestProperty("filter", RichsBOTRoom);
            connection.addRequestProperty("targetURL", myRequestBinURL);
            connection.addRequestProperty("name", "RichTestBot");
            
            DataOutputStream output;
            try {
                output = new DataOutputStream(connection.getOutputStream());
                output.close();
                
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
            
            System.out.println("Response code: " + connection.getResponseCode());
            System.out.println("Response message: " + connection.getResponseMessage());
          
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        
        /* Create a new room
        
        Room room = new Room();
        room.setTitle("Richs BOT Room");
        room = spark.rooms().post(room);
        
        */
        
        /* Add a coworker to Rich's BOT Room.
        Membership membership = new Membership();
        membership.setRoomId("Y2lzY29zcGFyazovL3VzL1JPT00vYTBlNDk1MzAtZDFmMC0xMWU2LWEyNmYtYWY4MTVkNzAyMjc4");
        membership.setPersonEmail("wlardner@cisco.com");
        spark.memberships().post(membership);
        
        */
       
        //List the members of the room
      
        /*
        spark.memberships()
                .queryParam("roomId", room.getId())
                .iterate()
                .forEachRemaining(member -> {
                    System.out.println(member.getPersonEmail());
                });

        */
        
        /* Post a text message to the room
        
        Message message = new Message();
        // message.setRoomId(room.getId());
        message.setRoomId("Y2lzY29zcGFyazovL3VzL1JPT00vYTBlNDk1MzAtZDFmMC0xMWU2LWEyNmYtYWY4MTVkNzAyMjc4");
        message.setText("Just added Wade Lardner...."  );
        spark.messages().post(message);
       
        
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


        // Add a coworker to the team
        TeamMembership teamMembership = new TeamMembership();
        teamMembership.setTeamId(team.getId());
        teamMembership.setPersonEmail("wile_e_coyote@acme.com");
        spark.teamMemberships().post(teamMembership);


        // List the members of the team
        spark.teamMemberships()
                .queryParam("teamId", team.getId())
                .iterate()
                .forEachRemaining(member -> {
                    System.out.println(member.getPersonEmail());
                });

        */
      
    
    }
}
