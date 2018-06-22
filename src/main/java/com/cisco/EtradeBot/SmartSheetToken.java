/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cisco.EtradeBot;

import com.ciscospark.Person;
import java.io.Serializable;

/**
 *
 * @author richverjinski
 */
public class SmartSheetToken extends Person implements Serializable {
    String AccessToken;
    
    public void setSSToken(String SSToken) {
        AccessToken = SSToken; 
    }
    
    public String getSSToken() {
        return AccessToken;
    }
    
}
