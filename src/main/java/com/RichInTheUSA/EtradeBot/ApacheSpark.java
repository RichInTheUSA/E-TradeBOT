/*
 * Big Picture:   This BOT is based on the Apache Spark server, which is
 * used to receive notifications from the variuos APIs.  This file defines
 * the various messages that the server can handle, and then kicks off other
 * java methods to act on the requests.
 *
 * Copyright 2019, Rich Verjinski
 */
package com.RichInTheUSA.EtradeBot;

/**
 * Imports
 */
import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import spark.Request;
import spark.Response;
import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;


/**
 * ApacheSpak Class
 * 
 * For each of the messages that the BOT can receive, this file specifies 
 * the response to send back to the client, as well as the java method
 * to call to process the request.
 
 * 
 * @author Rich Verjinski
 */
public class ApacheSpark {
    
    /**
     *  Manages the Apache Spark Configuration
     */
    public ApacheSpark() {
        
      // Get the port number that the server should listen on and set it.
      int p = Credentials.getApacheSparkPort();
      port(p); 
    
      // The following are commands which can be issued to the web server.
      
      get("/help", (Request request, Response response) -> {
          response.status(200);
          return ("EtradeBot.help()");
          //return (ETradeBot.help()); 
      });
    
      get("/adminhelp", (request, response) -> {
        response.status(200);
        return (ETradeBot.adminHelp()); 
      });
    
      get("/createwebhook", (request, response) -> {
        response.status(200);
        return ETradeBot.createWebHook(""); 
      });
    
      get("/hello", (request, response) -> {
        response.status(200);
        return (ETradeBot.hello());
      });
    
      get("/support", (request, response) -> {
        response.status(200);
        return (ETradeBot.support());
      });
      
      get("/privacy", (request, response) -> {
        response.status(200);
        return (ETradeBot.privacy());
      });
    
      get("/listrooms", (Request request, Response response) -> {
        response.status(200);
        return ETradeBot.listRooms();
      });
    
      get("/listwebhooks", (Request request, Response response) -> {
        response.status(200);
        return ETradeBot.listWebHooks();
      });
    
      get("/deletewebhook/:name", (Request request, Response response) -> {
        response.status(200);
        return ETradeBot.deleteWebHook(request.params(":name"));
      });
      
      get("/deleteallwebhooksbut/:name", (Request request, Response response) -> {
        response.status(200);
        return ETradeBot.deleteAllWebHooksBut(request.params(":name"));
      });      
    
      get("/getmessages", (Request request, Response response) -> {
        response.status(200);
        return ETradeBot.getMessages();
      });
     
    // Messages from the Cisco Webex Server come as a POST with the keyword
    // webhook and a message body in JSON format.
    // This code calls processMessages to take action.
      
    post("/webhook", (Request request, Response response) -> {
        response.status(200); 
        
        JsonObject messageData;
        try (JsonReader jsonreader = Json.createReader(new StringReader(request.body()))) {
            JsonObject messageBody = jsonreader.readObject();
            messageData = messageBody.getJsonObject("data");
                        
            ETradeBot.processMessages( messageData.getString("roomId"),
                    messageData.getString("id"),
                    messageData.getString("personId"));
            return ("200");
        }
               
    });
    
    }  
}