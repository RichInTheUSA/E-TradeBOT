/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cisco.mavensmartsbot;

import static com.cisco.mavensmartsbot.SSBot.myGetDisplayName;
import com.ciscospark.Message;
import com.smartsheet.api.*;
import com.smartsheet.api.models.*;
import com.smartsheet.api.models.enums.SourceInclusion;
import com.smartsheet.api.oauth.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author richverjinski
 */
public class SmartSheet {
    
    public static Smartsheet smartsheet = null;
    public static ArrayList<SmartSheetToken> listOfSmartSheetTokens = new ArrayList<SmartSheetToken>();
    public static List<Program> portfolio = new ArrayList();
    
    public static void initializeSmartSheetAPI() throws SmartsheetException {
        
    // Initialize SmartSheetTokens data.
    // SmartSheetToken data is stored in a file called sstoken.dat.  If the file
    // is empty, the bot will create a file once it has data.  If it's
    // not empty, the bot will initialize using the specified file.
     try { 
        // Open up the file if you can.
        FileInputStream fis = new FileInputStream("sstoken.dat");
        ObjectInputStream ois = new ObjectInputStream(fis);
            
        // Read all the SmartSheetToken objects
        while (fis.available() > 0) {
        SmartSheetToken tok = (SmartSheetToken)ois.readObject();
                listOfSmartSheetTokens.add(tok);
        }
            
        // Close up the files
        ois.close();
        fis.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SmartSheet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SmartSheet.class.getName()).log(Level.SEVERE, null, ex);        
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SmartSheet.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Print the initialized data, as read in from the file.
        for (SmartSheetToken t: listOfSmartSheetTokens) {
            System.out.println (t.getDisplayName() + " smartsheet token is: " + t.getSSToken());
            String[] emails = t.getEmails();
            System.out.println("emails: " + emails[0]);
        }
        
        if (listOfSmartSheetTokens.isEmpty()) {
            System.out.println("The file was empty.");
        } 
        System.out.println("\n");
        
        // Set the Access Token and initialize the API.
        // This is Rich Verjinski's token.  We should get standalone token for this project.
        Token token = new Token();
        
        token.setAccessToken(Credentials.getMySSToken());
        smartsheet = new SmartsheetBuilder().setAccessToken(token.getAccessToken()).build(); 
        
        // Now let's build our Portfolio!
        buildPortfolio();
        
    }
    
    /**
     *
     * @param m
     * @return
     * @throws SmartsheetException
     */
            
    public static String processSmartSheetRequest(String RoomId, Message m, String personId) throws SmartsheetException {
        StringBuilder response = new StringBuilder();
        
        System.out.println("\nReceived from: " + m.getPersonEmail());
        System.out.println("\nReceived: " + m.getText());
        
        String message = m.getText().toLowerCase();
        if (message.contains("set-token")) {response.append(setMyToken(m)); }
        
        // Moved this out of the checkToken() if statement, so that folks can run the portfolio command
        // using the default SS Token. 
        if (message.contains("portfolio") || message.contains("program") ) {
            setDefaultToken();
            response.append(processPortfolioInfo(RoomId, m, personId));
        } else {
       
            //Check to make sure a SS Token is on file for user submitting request
            if(checkToken(m)) {
                //Sets the correct SS Token for current user and rebuilds Smartsheet
                setCurrentToken(m);

                //Handles message requests.  If Access Token is invalid, returns an error.
                try {
                    if (message.contains("folders")) { response.append(showMyFolders(m)); }
                    if (message.contains("workspaces")) { response.append(showMyWorkspaces(m)); }
                    if (message.contains("sheets")) { response.append(showMySheets(m)); }
                    if (message.contains("columns")) { response.append(showMyColumns(m)); }
                    if (message.contains("search")) {response.append(search(m)); }

                    return response.toString();
                } 
                catch (AuthorizationException ex){
                    return "Smartsheet Access Token in invalid. Please use **Set-Token** feature to set the correct token.";
                }
            } else {
                return "This command reqires a SmartSheet token to access your smartsheets.  \n" +
                       "Try **help**, to see how to use **set-token**.";
            }
        }
        return "";
    } 
    
    //Sets the correct SS Token for current user and rebuilds Smartsheet
    public static void setCurrentToken(Message m) {
        String user = m.getPersonId();
        for (SmartSheetToken tok: listOfSmartSheetTokens){
            if (user.equals(tok.getId())) {
                Token token = new Token();
                token.setAccessToken(tok.getSSToken());
                smartsheet = new SmartsheetBuilder().setAccessToken(token.getAccessToken()).build();
            }
        }
    }
    
    public static void setDefaultToken() {
        Token token = new Token();
        token.setAccessToken(Credentials.getMySSToken());
        smartsheet = new SmartsheetBuilder().setAccessToken(token.getAccessToken()).build();
    }
    
    //Checks to make sure the user sending a Smartsheet request has a SS Token on file
    public static Boolean checkToken(Message m){
        String pers = m.getPersonId();
        return listOfSmartSheetTokens.stream().anyMatch((t) -> (t.getId().equals(pers)));
    }
    
    //Parses a message and finds a Token
    public static String findTokenInMessage(Message m){
        String message = m.getText();
        String words[] = message.split(" ");
        for(String word : words) {
            if (word.length() == 26 || word.length() == 25) {
                return word;
            } 
        }
        return "Token entered incorrectly. Please try again.";
    }
    
    //Adds a user submitted SS Token to the local "listOfSmartSheetTokens" arraylist, and the sstoken.dat file
    public static String setMyToken(Message m) {
        StringBuilder response = new StringBuilder();
        
        // Get the person's email address
        String[] emails = new String[10];
        emails[0] = m.getPersonEmail();
        System.out.println(m.getPersonEmail());
        
        //Get the token from the message
        String message = m.getText().toLowerCase();
        
        String newToken = findTokenInMessage(m);
        if (newToken.length() != 26) {
            return newToken;
        }
        
        // Create a SmartShetToken object for managing people.
        SmartSheetToken tok = new SmartSheetToken();
        
        tok.setSSToken(newToken);
        tok.setId(m.getPersonId());
        tok.setDisplayName(myGetDisplayName(m.getPersonId()));
        tok.setEmails(emails);
            
        //Check to see if there was already an entry.
        //If so, remove it and replace it.
        boolean removed = listOfSmartSheetTokens.removeIf(per->per.getId().equals(tok.getId()));
        listOfSmartSheetTokens.add(tok);

            
        try {                    
            try (FileOutputStream fos = new FileOutputStream("sstoken.dat");
                 ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                listOfSmartSheetTokens.forEach((per) -> {
                    try {
                        oos.writeObject(per);
                    } catch (IOException ex) {
                        Logger.getLogger(SmartSheet.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SmartSheet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SmartSheet.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        response.append("Updating **" + tok.getDisplayName() + "**  \n");
        response.append("Setting **").append(tok.getSSToken()).append("** as the SmartSheet token.  \n");
        return response.toString();
    }
    
    public static String showMyFolders(Message m) throws SmartsheetException {
        StringBuilder s = new StringBuilder();
        Home home = smartsheet.homeResources().getHome(EnumSet.of(SourceInclusion.SOURCE));
    
        List<Folder> homeFolders = home.getFolders();
        s.append("Folders...  \n");
        homeFolders.forEach((folder) -> {
            s.append("-  ").append(folder.getName()).append("  \n");
        }); 
        s.append("  \n\n");
   
        return s.toString();
    }
    
    public static String showMyWorkspaces(Message m) throws SmartsheetException {
        StringBuilder s = new StringBuilder();
        PaginationParameters parameters = new PaginationParameters.PaginationParametersBuilder().setIncludeAll(true).build();
        PagedResult<Workspace> ws = smartsheet.workspaceResources().listWorkspaces(parameters);
    
        s.append("Workspaces...  \n");
        ws.getData().forEach((w) -> {
            s.append("-  ").append(w.getName()).append("  \n");
        });
        return s.toString();
    }
    
    public static String showMySheets(Message m) throws SmartsheetException {
        StringBuilder result = new StringBuilder();
        String message = m.getText().toLowerCase();
     
        result.append("SmartSheets...  \n");
        PagedResult<Sheet> homeSheets = smartsheet.sheetResources().listSheets(EnumSet.of(SourceInclusion.SOURCE), null);
        homeSheets.getData().forEach((sheet) -> {
            result.append("-  ").append(sheet.getName());
            if (message.contains("id")) {
                //System.out.println(sheet.getId());
                result.append(", Id: ").append(sheet.getId());
            }
            //s.append(" Owner: ").append(sheet.getOwner());
            // number of rows
            result.append("  \n");
        }); 
        return result.toString();
    }
    
    public static String showMyColumns(Message m) throws SmartsheetException {
        StringBuilder response = new StringBuilder();
        String message = m.getText().toLowerCase();
        final String delimiter = ", ";
     
        response.append("Columns...  \n");
        PagedResult<Sheet> homeSheets = smartsheet.sheetResources().listSheets(EnumSet.of(SourceInclusion.SOURCE), null);
        homeSheets.getData().forEach((theSheet) -> {
     
            try {
                System.out.println("working with sheet:" + theSheet.getId());
                Sheet st = smartsheet.sheetResources().getSheet(theSheet.getId(), null, null, null, null, null, null, null);
                
                List<Column> columnList = st.getColumns();
                String columnHeader = null;
                System.out.println("got past getColumns");
                for (Column col : columnList) {
                    columnHeader = columnHeader == null ? col.getTitle() : columnHeader + delimiter + col.getTitle();
                    //response.append(columnHeader).append(", ");
                }
                response.append(columnHeader).append("  \n");
            } catch (SmartsheetException ex) {
                System.out.println("got an exception");
                Logger.getLogger(SmartSheet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }); 
        return response.toString();
    }   
    
    
    public static String search(Message m) throws SmartsheetException {
        StringBuilder result = new StringBuilder();
        StringBuilder searchString = new StringBuilder();
        String word;
        Boolean restOfPhrase = false;
        
        String message = m.getText().toLowerCase();
     
        StringTokenizer st = new StringTokenizer(message);
        while (st.hasMoreTokens()) {
            word = st.nextToken();
            if (restOfPhrase) {
                searchString.append(" ").append(word);
            }
            if (word.equals("search")) {
                restOfPhrase = true;
                searchString.append(st.nextToken());
            }
        }
        System.out.println("Search: " + searchString.toString() + " \n");
        
        result.append("Search Results...  \n");
        SearchResult sr = smartsheet.searchResources().search(searchString.toString());
        result.append("Total count of results for **").append(searchString.toString()).append("**: ").append(sr.getTotalCount()).append("  \n");
        
        if (sr.getTotalCount() > 0) {
            List<SearchResultItem> listOfResults = sr.getResults();
            listOfResults.forEach((resultItem) -> {
                result.append("-  Found in ").append(resultItem.getContextData()).append(" of ");
                result.append(resultItem.getParentObjectType()).append(": ").append(resultItem.getParentObjectName()).append("  \n");
            });  
        } else {
            result.append("No results found");
        }
        
        return result.toString();
    }
    
    public static void buildPortfolio () {

        // These are the 3 sheets to get info from.
        long FY17CtBProgramInfo =         3292056496957316L;
        long MasterCtBStatusUpdates =     8388125387974532L;  
        long MasterCtBHealthStatusRisks = 8939530469304196L;
             
        // Get data from FY17 CtB Program Info first
        Sheet s = null;
        try {
            s = smartsheet.sheetResources().getSheet(FY17CtBProgramInfo, null, null, null, null, null, null, null);

            // Print the column titles as a delimited line of text.
            List<Column> columnList = s.getColumns();
            String columnHeader = null;
            for (Column col : columnList) {
                columnHeader = columnHeader == null ? col.getTitle() : columnHeader + ", " + col.getTitle();
            }
            // System.out.println("Columns: " + columnHeader + "  \n\n");

            // Parse each Row, then each cell of each row.
            List<Row> rowList = s.getRows();
            for (Row row: rowList){
                List<Cell> cellList = row.getCells();

                Program pgm;
                boolean found = false;
                int index = 0;
                
                // Parset the first column, this should be the program name.
                String programName = Objects.toString(cellList.get(0).getValue() != null ? cellList.get(0).getValue() : cellList.get(0).getDisplayValue());
                
                // Check to see if the program name is null... if so, skip parsing the rest of the row.
                if (programName.equals("null")) continue;                 

                // Check to see if this program is in our portfolio.
                for (int i = 0; i < portfolio.size(); i++) {
                    if (portfolio.get(i).getProgramName().equals(programName)) {
                        found = true;
                        index = i;
                    }
                }
                
                // If already in the portfolio, then we will update it.
                // if not, then create a new Program object and add it to the portfolio.
                if (found) {
                    pgm = portfolio.get(index);  
                } else {
                    pgm = new Program();
                    pgm.setProgramName(cellList.get(0).getDisplayValue());
                    portfolio.add(pgm);
                }
                
                // Set the Description if there is one.
                // If there is more than one description, concatenate them.
                String description = Objects.toString(cellList.get(1).getValue() != null ? cellList.get(1).getValue() : cellList.get(1).getDisplayValue());
                if ( pgm.getDescription().contains(description) || description.equals("null")) {
                    //System.out.println("do nothing");
                } else {
                    if (pgm.getDescription().isEmpty()) {
                        pgm.setDescription(description);
                    } else {
                        pgm.setDescription(pgm.getDescription() + ", " + description);
                    }                
                }
                
                // Set the Function if there is one
                // If there is more than one description, concatenate them.                
                String function = Objects.toString(cellList.get(2).getValue() != null ? cellList.get(2).getValue() : cellList.get(2).getDisplayValue());
                if ( pgm.getFunction().contains(function) || function.equals("null")) {
                    //System.out.println("do nothing");
                } else {
                    if (pgm.getFunction().isEmpty()) {
                        pgm.setFunction(function);
                    } else {
                        pgm.setFunction( pgm.getFunction() + ", " + function);
                    }
                }
        
                String pillar = Objects.toString(cellList.get(3).getValue() != null ? cellList.get(3).getValue() : cellList.get(3).getDisplayValue());
                if ( pgm.getPillar().contains(pillar) || pillar.equals("null")) {
                    //System.out.println("do nothing");
                } else {
                   if (pgm.getPillar().isEmpty()) {
                        pgm.setPillar(pillar);
                   } else {                    
                        pgm.setPillar( pgm.getPillar() + ", " + pillar );
                   }

                }

                // Set the Program Manager if there is one.
                // If there is more than one description, concatenate them.      
                String pm = Objects.toString(cellList.get(5).getValue() != null ? cellList.get(5).getValue() : cellList.get(5).getDisplayValue());
                if (pgm.getPM().contains( pm ) || pm.equals("null")) {
                    //System.out.println("do nothing");
                } else {
                   if (pgm.getPM().isEmpty()) {
                        pgm.setPM(pm);
                   } else {                    
                        pgm.setPM( pgm.getPM() + ", " + pm );
                   }
                }
                // Set the Archive state if there is one.
                // If there is more than one description, concatenate them.      
                String archive = Objects.toString(cellList.get(6).getValue() != null ? cellList.get(6).getValue() : cellList.get(6).getDisplayValue());
                if (pgm.getArchive().contains( archive ) || archive.equals("null")) {
                    //System.out.println("do nothing");
                } else {
                   if (pgm.getArchive().isEmpty()) {
                        pgm.setArchive(archive);
                   } else {                    
                        pgm.setArchive( pgm.getArchive() + ", " + archive );
                   }
                }                
    
     
            }  
        } catch (SmartsheetException ex) {
            Logger.getLogger(SmartSheet.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Ok, now get data from the next sheet.  MasterCtBStatusUpdates
        
        try { 
            s = smartsheet.sheetResources().getSheet(MasterCtBStatusUpdates, null, null, null, null, null, null, null);

            // Print the column titles as a delimited line of text.
            List<Column> columnList = s.getColumns();
            String columnHeader = null;
            for (Column col : columnList) {
                columnHeader = columnHeader == null ? col.getTitle() : columnHeader + ", " + col.getTitle();
            }
            // System.out.println("Columns: " + columnHeader + "  \n\n");

            // Parse each Row, then each cell of each row.
            List<Row> rowList = s.getRows();
            for (Row row: rowList){
                List<Cell> cellList = row.getCells();

                Program pgm;
                boolean found = false;
                int index = 0;
                
                // Parset the second column, this should be the program name.
                String programName = Objects.toString(cellList.get(1).getValue() != null ? cellList.get(1).getValue() : cellList.get(1).getDisplayValue());
                
                // Check to see if the program name is null... if so, skip parsing the rest of the row.
                if (programName.equals("null")) continue;                 

                // Check to see if this program is in our portfolio.
                for (int i = 0; i < portfolio.size(); i++) {
                    if (portfolio.get(i).getProgramName().equals(programName)) {
                        found = true;
                        index = i;
                    }
                }
                
                // If already in the portfolio, then we will update it.
                // if not, then create a new Program object and add it to the portfolio.
                if (found) {
                    pgm = portfolio.get(index);  
                } else {
                    pgm = new Program();
                    pgm.setProgramName(cellList.get(0).getDisplayValue());
                    portfolio.add(pgm);
                }                

                // Set the Exec Sponsor if there is one.
                // If there is more than one description, concatenate them.               
                String execSponsor = Objects.toString(cellList.get(8).getValue() != null ? cellList.get(8).getValue() : cellList.get(8).getDisplayValue());
                if ( pgm.getExecSponsor().contains( execSponsor ) || execSponsor.equals("null")) {
                    //System.out.println("do nothing");
                } else {
                    if (pgm.getExecSponsor().isEmpty()) {
                        pgm.setExecSponsor(execSponsor);
                    } else {
                        pgm.setExecSponsor( pgm.getExecSponsor() + ", " + execSponsor );
                    }
                }
                
                // Set the Business Lead if there is one.
                // If there is more than one description, concatenate them.                        
                String bizLead = Objects.toString(cellList.get(9).getValue() != null ? cellList.get(9).getValue() : cellList.get(9).getDisplayValue());
                if (pgm.getBizLead().contains( bizLead ) || bizLead.equals("null")) {
                    //System.out.println("do nothing");
                } else {
                   if (pgm.getBizLead().isEmpty()) {
                        pgm.setBizLead(bizLead);
                   } else {                    
                        pgm.setBizLead( pgm.getBizLead() + ", " + bizLead );
                   }
                }



                // Set the IT Lead if there is one.
                // If there is more than one description, concatenate them.                      
                String ITLead = Objects.toString(cellList.get(11).getValue() != null ? cellList.get(11).getValue() : cellList.get(11).getDisplayValue());
                if (pgm.getITLead().contains( ITLead ) || ITLead.equals("null")) {
                    //System.out.println("do nothing");
                } else {
                   if (pgm.getITLead().isEmpty()) {
                        pgm.setITLead(ITLead);
                   } else {                    
                        pgm.setITLead( pgm.getITLead() + ", " + ITLead );
                   }
                }                
    
     
            }  
        } catch (SmartsheetException ex) {
            Logger.getLogger(SmartSheet.class.getName()).log(Level.SEVERE, null, ex);
        }  

        // Ok, now get data from the next sheet.  MasterCtBHealthStatusRisks
        
        try { 
            s = smartsheet.sheetResources().getSheet(MasterCtBHealthStatusRisks, null, null, null, null, null, null, null);

            // Print the column titles as a delimited line of text.
            List<Column> columnList = s.getColumns();
            String columnHeader = null;
            for (Column col : columnList) {
                columnHeader = columnHeader == null ? col.getTitle() : columnHeader + ", " + col.getTitle();
            }
            // System.out.println("Columns: " + columnHeader + "  \n\n");

            // Parse each Row, then each cell of each row.
            List<Row> rowList = s.getRows();
            for (Row row: rowList){
                List<Cell> cellList = row.getCells();

                Program pgm;
                boolean found = false;
                int index = 0;
                
                // Parset the first column, this should be the program name.
                String programName = Objects.toString(cellList.get(1).getValue() != null ? cellList.get(1).getValue() : cellList.get(1).getDisplayValue());
                
                // Check to see if the program name is null... if so, skip parsing the rest of the row.
                if (programName.equals("null")) continue;                 

                // Check to see if this program is in our portfolio.
                for (int i = 0; i < portfolio.size(); i++) {
                    if (portfolio.get(i).getProgramName().equals(programName)) {
                        found = true;
                        index = i;
                    }
                }
                
                // If already in the portfolio, then we will update it.
                // if not, then create a new Program object and add it to the portfolio.
                if (found) {
                    pgm = portfolio.get(index);  
                } else {
                    pgm = new Program();
                    pgm.setProgramName(programName);
                    portfolio.add(pgm);
                }                

                // Ok, let's get the Month of Update and just look for "March"
                // Let's look for Project Health and just look for "Overall"
                // Otherwise skip.
                
                String monthOfUpdate = Objects.toString(cellList.get(2).getValue() != null ? cellList.get(2).getValue() : cellList.get(2).getDisplayValue());
                String projectHealth = Objects.toString(cellList.get(3).getValue() != null ? cellList.get(3).getValue() : cellList.get(3).getDisplayValue());
                String healthColor = Objects.toString(cellList.get(4).getValue() != null ? cellList.get(4).getValue() : cellList.get(4).getDisplayValue());  
                
                if (monthOfUpdate.equals(getCurrentMonth()) && projectHealth.equals("Overall")) {
                    pgm.setMonthOfUpdate(monthOfUpdate);
                    pgm.setProjHealth(projectHealth);
                    pgm.setHealthColor(healthColor);
                } else {
                  continue;
                }
            }  
                
        } catch (SmartsheetException ex) {
            Logger.getLogger(SmartSheet.class.getName()).log(Level.SEVERE, null, ex);
        }        
        
        
        // Now dump the portfolio... 
        // We should not have duplicates!!!
        
        portfolio.forEach((pg) -> {
            System.out.println("Program: " + pg.getProgramName());
            System.out.println("Description: " + pg.getDescription());
            System.out.println("Functions: " + pg.getFunction());
            System.out.println("Pillar: " + pg.getPillar());
            System.out.println("Program Manager: " + pg.getPM());
            System.out.println("Archive: " + pg.getArchive());
            
            System.out.println("Exec Sponsor: " + pg.getExecSponsor());
            System.out.println("Program Manager: " + pg.getPM());
            System.out.println("Biz Lead: " + pg.getBizLead());
            System.out.println("IT Lead: " + pg.getITLead());
 
            System.out.println("Month of Update: " + pg.getMonthOfUpdate());
            System.out.println("Project Health: " + pg.getProjHealth());
            System.out.println("Health Color: " + pg.getHealthColor() + "\n");            
            
        });
    }
    
    public static String processPortfolioInfo(String RoomId, Message m, String personId) {

        StringBuilder response = new StringBuilder();
        String message = m.getText().toLowerCase();
        
        ArrayList<String> contents = new ArrayList();
        ArrayList<String> pivots = new ArrayList();
        
        // smartsheet show portfolio with <list of attributes>  by <pivots>
        // smartsheet show portffolio with function and pillar by pm
        //
        // Output by PMs:
        // PM: Rich
        //  Program A
        //  * function: SCT
        //  * pillar: X
        // 
        //  Program B
        //  * function: SCT
        //  * pillar: Y
        //        
        // PM: Ben
        //  Program C
        //  * function: SCT
        //  * pillar: X
        // 
        //  Program D
        //  * function: SCT
        //  * pillar: Y        
        
        
        // Get all the words between show and by.
        if (message.contains("by") ) {
            String word;
            StringTokenizer st = new StringTokenizer(message);
            while (st.hasMoreTokens()) {
                word = st.nextToken();            
                if ( word.equals("with")) {          // Parse to the word 'with'
                    while (st.hasMoreTokens()){
                        word = st.nextToken();       // Collect contents
                        if (word.equals("by")) {     // Parse to the word 'by'
                                                     // Collect pivots
                            while (st.hasMoreTokens()) {
                                word = st.nextToken();
                                pivots.add(word);
                            }
                        } else {
                            contents.add(word);
                        }
                    }
                }
            }
            System.out.println("\nContents: ");
            contents.forEach((s)-> {
               System.out.println(s + " ");
            });
            System.out.println("\nPivots: ");
            pivots.forEach((p)-> {
               System.out.println(p + " ");
            });
            System.out.println("\n  This feature is not implemented yet.");
            
        } else if (message.contains("program")) {
            String word;
            StringTokenizer st = new StringTokenizer(m.getText());
            StringBuilder progName = new StringBuilder();
            while (st.hasMoreTokens()) {
                word = st.nextToken();            
                if ( word.equals("program")) { 
                    progName.append(st.nextToken().toLowerCase()); // Get the next token
                   
                    for (Program prog : portfolio) {
                        if (prog.getProgramName().toLowerCase().contains(progName)) {
                            
                            if ( message.contains("archive") || !prog.getArchive().contains("Archive") ) {
                                response.append("**").append(prog.getProgramName()).append("** \n"); 
                                if (message.contains("all") || message.contains("description")){
                                    response.append("-  ").append("Description: ").append(prog.getDescription()).append("  \n");
                                }
                                if (message.contains("all") || message.contains("function")){
                                    response.append("-  ").append("Function: ").append(prog.getFunction()).append("  \n");
                                }
                                if (message.contains("all") || message.contains("pillar")){
                                    response.append("-  ").append("Strategic Pillar: ").append(prog.getPillar()).append("  \n");
                                }
                                if (message.contains("all") || message.contains("sponsor")){
                                    response.append("-  ").append("Exec Sponsor: ").append(prog.getExecSponsor()).append("  \n");
                                }
                                if (message.contains("all") || message.contains("pm")){
                                    response.append("-  ").append("Program Manager: ").append(prog.getPM()).append("  \n");
                                }
                                if (message.contains("all") || message.contains("biz lead")){
                                    response.append("-  ").append("Business Lead: ").append(prog.getBizLead()).append("  \n");
                                }
                                if (message.contains("all") || message.contains("it lead")){
                                    response.append("-  ").append("IT Lead: ").append(prog.getITLead()).append("  \n");
                                }  
                                if (message.contains("all") || message.contains("last update")){
                                    response.append("-  ").append("Last Update: ").append(prog.getMonthOfUpdate()).append("  \n");
                                }  
                                if (message.contains("all") || message.contains("health")){
                                    response.append("-  ").append("Program Health: ").append(prog.getProjHealth()).append("  \n");
                                } 
                                if (message.contains("archive")){
                                    response.append("-  ").append("Archived?: ").append(prog.getArchive()).append("  \n");
                                }                          
                                if (message.contains("all") || message.contains("color")){
                                    response.append("-  ").append("Health Color: ").append(prog.getHealthColor()).append("  \n");
                                }  
                                response.append("\n");                            
                            }
                        }    
                    }
                    SSBot.sendMessage(RoomId, response.toString(), personId);
                }           
            }
        
        } else if ( message.contains("without updates") || message.contains("red") || message.contains("yellow") || message.contains("green") ) {
            
            if (message.contains("green")) {
                response.append("**Programs with Green status are:** \n");
                
                for (Program prog : portfolio) {
                    if (prog.getHealthColor().equals("Green") && !prog.getArchive().contains("Archive")) {
                        response.append("-  ").append(prog.getProgramName()).append("  \n"); 
                    }
                }
    
                SSBot.sendMessage(RoomId, response.toString(), personId);
                response = new StringBuilder();
            } else if (message.contains("yellow")) {
                response.append("**Programs with Yellow status are:** \n");
                
                for (Program prog : portfolio) {
                    if (prog.getHealthColor().equals("Yellow") && !prog.getArchive().contains("Archive")) {
                        response.append("-  ").append(prog.getProgramName()).append("  \n"); 
                    }
                }
                SSBot.sendMessage(RoomId, response.toString(), personId);
                response = new StringBuilder(); 
            }  else if (message.contains("red")) {
                response.append("**Programs with Red status are:** \n");
                
                for (Program prog : portfolio) {
                    if (prog.getHealthColor().equals("Red") && !prog.getArchive().contains("Archive")) {
                        response.append("-  ").append(prog.getProgramName()).append("  \n"); 
                    }
                }
                SSBot.sendMessage(RoomId, response.toString(), personId);
                response = new StringBuilder(); 
            }  else {
                response.append("**Programs without updated status for "+ getCurrentMonth() + " are:** \n");
                for (Program prog : portfolio) {
                    if ( !prog.getHealthColor().equals("Red") &&  !prog.getHealthColor().equals("Yellow") && !prog.getHealthColor().equals("Green") && !prog.getArchive().contains("Archive") ){
                        response.append("-  ").append(prog.getProgramName()); 
                        if (prog.getPM().equals("") || prog.getPM().equals("null")) {
                            response.append(".  PM needs to be updated.  \n");
                        } else {
                            response.append(".  Please contact PM(s): " + prog.getPM() + "  \n");                           
                        }
                    }
                }
                SSBot.sendMessage(RoomId, response.toString(), personId);
                response = new StringBuilder(); 
            }
            
        } else {
       
            response.append("Portfolio consists of the following programs:  \n");                                                                                                                                                               
            int counter = 0;
            for (Program prog : portfolio) {
                
                if ( message.contains("archive") || !prog.getArchive().contains("Archive") ) {
                    
                    // Always show the Program Name
                    response.append("**" + prog.getProgramName()).append("**  \n");

                    if (message.contains("all") || message.contains("description")){
                        response.append("-  ").append("Description: ").append(prog.getDescription()).append("  \n");
                    }
                    if (message.contains("all") || message.contains("function")){
                        response.append("-  ").append("Function: ").append(prog.getFunction()).append("  \n");
                    }
                    if (message.contains("all") || message.contains("pillar")){
                        response.append("-  ").append("Strategic Pillar: ").append(prog.getPillar()).append("  \n");
                    }
                    if (message.contains("all") || message.contains("sponsor")){
                        response.append("-  ").append("Exec Sponsor: ").append(prog.getExecSponsor()).append("  \n");
                    }
                    if (message.contains("all") || message.contains("pm")){
                        response.append("-  ").append("Program Manager: ").append(prog.getPM()).append("  \n");
                    }
                    if (message.contains("all") || message.contains("biz lead")){
                        response.append("-  ").append("Business Lead: ").append(prog.getBizLead()).append("  \n");
                    }
                    if (message.contains("all") || message.contains("it lead")){
                        response.append("-  ").append("IT Lead: ").append(prog.getITLead()).append("  \n");
                    }  
                    if (message.contains("all") || message.contains("last update")){
                        response.append("-  ").append("Last Update: ").append(prog.getMonthOfUpdate()).append("  \n");
                    }  
                    if (message.contains("all") || message.contains("health")){
                        response.append("-  ").append("Program Health: ").append(prog.getProjHealth()).append("  \n");
                    }  
                    if (message.contains("archive")){
                        response.append("-  ").append("Archive: ").append(prog.getArchive()).append("  \n");
                    }                  
                    if (message.contains("all") || message.contains("color")){
                        response.append("-  ").append("Health Color: ").append(prog.getHealthColor()).append("  \n");
                    }  
                    response.append("\n");

                    counter++;
                    if (counter > 7) {
                        counter = 0;
                        SSBot.sendMessage(RoomId, response.toString(), personId);
                        response = new StringBuilder();
                    }
                }
            }   
        }   
     
       
        response.append("\n\n**Your request has been completed**");
        
        return response.toString();
    }
    
    public static String getCurrentMonth(){
        Calendar mCalendar = Calendar.getInstance();    
        String month = mCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
        return month;
    }
/*    
    for (Cell cell : cellList) {
            String cellOutput = Objects.toString(cell.getValue() != null ? cell.getValue() : cell.getDisplayValue());
            rowOutput = rowOutput == null ? cellOutput : rowOutput + delimiter + cellOutput;
        }        
*/      
        
        /* Create folder in home
        Folder folder = new Folder.CreateFolderBuilder().setName("BotTestFolder").build();
        folder = smartsheet.homeResources().folderResources().createFolder(folder);
        System.out.println("Folder ID: " + folder.getId() + ", Folder Name: " + folder.getName());
        // Setup checkbox Column Object
        Column checkboxColumn = new Column.AddColumnToSheetBuilder()
        .setType(ColumnType.CHECKBOX)
        .setTitle("Finished")
        .build();
        // Setup text Column Object
        Column textColumn = new Column.AddColumnToSheetBuilder()
        .setPrimary(true)
        .setTitle("To Do List")
        .setType(ColumnType.TEXT_NUMBER)
        .build();
        // Add the 2 Columns (flag & text) to a new Sheet Object
        Sheet sheet = new Sheet.CreateSheetBuilder()
        .setName("New Sheet")
        .setColumns(Arrays.asList(checkboxColumn, textColumn))
        .build();
        // Send the request to create the sheet @ Smartsheet
        sheet = smartsheet.sheetResources().createSheet(sheet);
         */
    
/**
 * This provides an example of how to use OAuth to generate a Token from a third party application. It handles
 * requesting the authorization code, sending the user to a specific website to request access and then getting
 * the access token to use for all future requests.
     * @throws com.smartsheet.api.SmartsheetException
     * @throws java.io.UnsupportedEncodingException
     * @throws java.net.URISyntaxException
     * @throws java.security.NoSuchAlgorithmException
 
public static void OAuthExample() throws SmartsheetException, UnsupportedEncodingException, URISyntaxException,
        NoSuchAlgorithmException {

    // Setup the information that is necessary to request an authorization code
    OAuthFlow oauth = new OAuthFlowBuilder()
                            .setClientId("YOUR_CLIENT_ID")
                            .setClientSecret("YOUR_CLIENT_SECRET")
                            .setRedirectURL("https://YOUR_DOMAIN.com/").build();

    // Create the URL that the user will go to grant authorization to the application
    String url = oauth.newAuthorizationURL(EnumSet.of(com.smartsheet.api.oauth.AccessScope.CREATE_SHEETS,
            com.smartsheet.api.oauth.AccessScope.WRITE_SHEETS), "key=YOUR_VALUE");

    // Take the user to the following URL
    System.out.println(url);

    // After the user accepts or declines the authorization they are taken to the redirect URL. The URL of the page
    // the user is taken to can be used to generate an authorization Result object.
    String authorizationResponseURL = "https://yourDomain.com/?code=l4csislal82qi5h&expires_in=239550&state=key%3D12344";

    // On this page pass in the full URL of the page to create an authorizationResult object
    AuthorizationResult authResult = oauth.extractAuthorizationResult(authorizationResponseURL);

    // Get the token from the authorization result
    Token token = oauth.obtainNewToken(authResult);

    // Save the token or use it.
    */
    
}
    
