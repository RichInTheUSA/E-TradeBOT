
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ciscospark;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import static spark.Spark.*;

class Example2 {
    
   
    public static void main(String[] args) throws MalformedURLException, ParserConfigurationException, URISyntaxException, SAXException, IOException {
        
        // To obtain a developer access token, visit http://developer.ciscospark.com
        // String accessToken = "<<secret>>";
        String accessToken = "Yzk0YjVlZjYtMTgwNS00YWMwLTlhNzEtYThlZmQzMGVkNjgyOGI3MzMyNjItMzdl";
        
        // Initialize the Sparak client
        Spark spark = Spark.builder()
                .baseUrl(URI.create("https://api.ciscospark.com/v1"))
                .accessToken(accessToken)
                .build();
        
        port(5678);  // Spark will run on port 5678
        
        get("/hello", (request, response) -> {
            response.status(200);
            System.out.println("got a hello");
            return "Hello back";
        });
        
        post("/webhook", (request, response) -> {
            response.status(200); 
            System.out.println ("Got a hook, parsing data... ");
            JsonReader jsonreader = Json.createReader(new StringReader(request.body()));
            JsonObject messageBody = jsonreader.readObject();
            JsonObject messageData = messageBody.getJsonObject("data");
            System.out.println("MessageID: " + messageData.getString("id") );
            /* System.out.println (
                    "id: " + messageBody.getString("id") + "\n" +
                    "name: " + messageBody.getString("id") + "\n" +
                    "targetUrl: " + messageBody.getString("targetUrl") + "\n" +
                    "resource: " + messageBody.getString("resource") + "\n" +
                    "event: " + messageBody.getString("event") + "\n" +
                    "filter: " + messageBody.getString("filter") + "\n" +
                    "orgId: " + messageBody.getString("orgId") + "\n" +
                    "createdBy: " + messageBody.getString("createdBy") + "\n" +
                    "appId: "+ messageBody.getString("appId") + "\n" +
                    "ownedBy"+ messageBody.getString("ownedBy") + "\n" +
                    "creator"+ messageBody.getString("creator") + "\n" +
                    "status"+ messageBody.getString("status") + "\n" +
                    "created"+ messageBody.getString("created") + "\n" +
                    "actorId"+ messageBody.getString("actorId") + "\n" +
                    "id"+ messageData.getString("actorId") + "\n" +
                    "roomId"+ messageData.getString("roomId") + "\n" + 
                    "roomType"+ messageData.getString("roomType") + "\n" +
                    "personId"+ messageData.getString("personId") + "\n" +
                    "personEmail"+ messageData.getString("personEmail") + "\n" +
                    "mentionedPeople"+ messageData.getString("mentionedPeople") + "\n" +
                    "personId"+ messageData.getString("personId") + " "
            ); */
            jsonreader.close();
            Message message1 = spark.messages().path("/"+ messageData.getString("id"), Message.class).get();
                String text = message1.getText();
                //String message = spark.messages().path("/"+ msgId).get().getText();
                System.out.println(text);
            return "Got it!";
        });
        
        
        
        System.out.println("Let's poll each of the Spark rooms that I'm in...");
        System.out.println("");
        
        // Let's list our webhooks, and put the rooms into a collection
        ArrayList<String> listOfWebhooks = new ArrayList();
        URL myTargetURL = new URL("http://96.255.147.229:5678/webhook");
        URI myTargetURI = myTargetURL.toURI();
        
        
        spark.webhooks()
             .iterate()
             .forEachRemaining (hook -> {
                 System.out.println("webhook for room: " + hook.getFilter());
                 System.out.println("webhook name: " + hook.getName());
                 System.out.println("webhook is: " + hook.getTargetUrl());
                 System.out.println("");
                 listOfWebhooks.add(hook.getFilter());  // Collect the room names that have webhooks!
             });            
        
        spark.rooms()
                .iterate()
                .forEachRemaining(room -> {
                    System.out.println(room.getTitle() + ", created " + room.getCreated() + ": " + room.getId());
                    System.out.println("TeamID: " + room.getTeamId());
                    System.out.println("IsLocked: " + room.getIsLocked());
                    System.out.println("Last activtiy on: " + room.getLastActivity().toString());
                   
                    if ( listOfWebhooks.contains("roomId="+room.getId()) ) {
                        System.out.println("Webhook in place.");
                    } else {
                        Webhook myWebhook = new Webhook();
                        myWebhook.setName("Webhook for "+ room.getTitle());
                        myWebhook.setTargetUrl(myTargetURI);
                        myWebhook.setResource("messages");
                        myWebhook.setEvent("created");
                        myWebhook.setFilter("roomId="+room.getId());
                        spark.webhooks().post(myWebhook);
                        System.out.println("Created a webhook for: " + room.getTitle());
                    }
                    // Let's query each room, getting a list of messages...
                    spark.messages()
                        .queryParam("roomId", room.getId())
                        .queryParam("mentionedPeople", "me")
                        //.queryParam("before", room.getLastActivity().toString())
                        .iterate()
                        .forEachRemaining(message1 -> {
                            System.out.println(room.getTitle() + ": " + message1.getText());
                            
                            // Let's respond back to each message...
                            //Message message = new Message();
                            //message.setRoomId(room.getId());
                            //message.setText("Hi, I'm a bot. I heard your message."  );
                            //spark.messages().post(message);
                            
                        });
                    System.out.println("");
                    
                });
       
        
        
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
        
        // Set up a Webhook.
        //URL myURL = new URL("http://myraspberrypi.hopto.org");
        //URI myURI = myURL.toURI();
        
        //Webhook myWebhook = new Webhook();
 
        //myWebhook.setName("RichSecondWebHook");
        //myWebhook.setTargetUrl(myURI);
        //myWebhook.setResource("messages");
        //myWebhook.setEvent("created");
        //myWebhook.setFilter("roomId=Y2lzY29zcGFyazovL3VzL1JPT00vOWZjMDI1NTAtZDFlMy0xMWU2LWIzNDEtYzdhZDRiNjZmNWNj");
        // Post the webhook!
        //spark.webhooks().post(myWebhook);  // Already have one.
        
        
    }
    
}

