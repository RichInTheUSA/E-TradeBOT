/*
 * This file defines the Apache Spark server requests
 * and then invokes respective 
 */
package com.cisco.EtradeBot;

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