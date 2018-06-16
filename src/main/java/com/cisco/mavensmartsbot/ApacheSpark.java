/*
 * This file defines the Apache Spark server requests
 * and then invokes respective 
 */
package com.cisco.mavensmartsbot;

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
 *
 * @author Rich Verjinski
 */
public class ApacheSpark {
    
    /**
     *  Manages the Apache Spark Configuration
     */
    public ApacheSpark() {
        
      int p = Credentials.getApacheSparkPort();
      port(p); 
    
      get("/help", (request, response) -> {
          response.status(200);
          return ("SSBot.help()");
          //return (SSBot.help()); 
      });
    
      get("/adminhelp", (request, response) -> {
        response.status(200);
        return (SSBot.adminHelp()); 
      });
    
      get("/createwebhook", (request, response) -> {
        response.status(200);
        return SSBot.createWebHook(""); 
      });
    
      get("/hello", (request, response) -> {
        response.status(200);
        return (SSBot.hello());
      });
    
      get("/support", (request, response) -> {
        response.status(200);
        return (SSBot.support());
      });
      
      get("/privacy", (request, response) -> {
        response.status(200);
        return (SSBot.privacy());
      });
    
      get("/listrooms", (Request request, Response response) -> {
        response.status(200);
        return SSBot.listRooms();
      });
    
      get("/listwebhooks", (Request request, Response response) -> {
        response.status(200);
        return SSBot.listWebHooks();
      });
    
      get("/deletewebhook/:name", (Request request, Response response) -> {
        response.status(200);
        return SSBot.deleteWebHook(request.params(":name"));
      });
      
      get("/deleteallwebhooksbut/:name", (Request request, Response response) -> {
        response.status(200);
        return SSBot.deleteAllWebHooksBut(request.params(":name"));
      });      
    
      get("/getmessages", (Request request, Response response) -> {
        response.status(200);
        return SSBot.getMessages();
      });
        
    post("/webhook", (request, response) -> {
        response.status(200); 
        //System.out.println ("Got a hook, parsing data... ");
        JsonObject messageData;
        try (JsonReader jsonreader = Json.createReader(new StringReader(request.body()))) {
            JsonObject messageBody = jsonreader.readObject();
            messageData = messageBody.getJsonObject("data");
 
            SSBot.processMessages( messageData.getString("roomId"),
                    messageData.getString("id"),
                    messageData.getString("personId"));
            return ("200");
        }
               
    });
    
    }  
}