/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cisco.mavensmartsbot;

import com.smartsheet.api.models.Folder;
import java.util.List;

/**
 *
 * @author richverjinski
 */
public class Program {
    String ProgramName = "";            // FY17 CtB Program Info
    String Description = "";            // FY17 CtB Program Info
    String Function = "";               // FY17 CtB Program Info
    String StrategicPillar = "";        // FY17 CtB Program Info
    String PM = "";                     // FY17 CtB Program Info
    String Archive = "";                // FY17 CtB Program Info
    
    String ExecSponsor = "";             // Master CtB Status Updates  
    String BizLead = "";                 // Master CtB Status Updates
    String ITLead = "";                  // Master CtB Status Updates

    String ProgramTrack = "";      // Master CtB Health Status & Risks 1
    String MonthOfUpdate = "";     // Master CtB Health Status & Risks 2
    String ProjHealth = "";        // Master CtB Health Status & Risks 3, look for Overall
    String HealthColor = "";       // Master CtB Health Status & Risks 4, Value.
                                    // 'Overall' will be in Proj Health column
                                    // 'Value' will be in Health Color column
    
    
    public void setProgramName(String pn) {
        ProgramName = pn; 
    }
    public String getProgramName() {
        return ProgramName;
    }
        
    public void setDescription(String des) {
        Description = des; 
    }
    
    public String getDescription() {
        return Description;
    }
    
    public void setFunction (String fun){
        Function = fun;
    }
    
    public String getFunction() {
        return Function;
    }
   
    public void setPillar (String pil){
        StrategicPillar = pil;
    }
    
    public String getPillar() {
        return StrategicPillar;
    } 
   
    public void setExecSponsor (String es){
        ExecSponsor = es;
    }
    
    public String getExecSponsor() {
        return ExecSponsor;
    } 
    public void setArchive (String ar){
        Archive = ar;
    }
    
    public String getArchive() {
        return Archive;
    } 
    
    public void setPM (String pm){
        PM = pm;
    }
    
    public String getPM() {
        return PM;
    } 
    
    public void setBizLead (String bl){
        BizLead = bl;
    }
    
    public String getBizLead() {
        return BizLead;
    } 
    
    public void setITLead (String it){
        ITLead = it;
    }
    
    public String getITLead() {
        return ITLead;
    } 

    public void setProgramTrack (String pt){
        ProgramTrack = pt;
    }
    public String getProgramTrack() {
        return ProgramTrack;
    } 
    
    public void setMonthOfUpdate (String mou){
        MonthOfUpdate = mou;
    }
    public String getMonthOfUpdate() {
        return MonthOfUpdate;
    } 
    
    public void setProjHealth (String ph){
        ProjHealth = ph;
    }
    public String getProjHealth() {
        return ProjHealth;
    } 
    
    public void setHealthColor (String hc){
        HealthColor = hc;
    }
    
    public String getHealthColor() {
        return HealthColor;
    } 
        
    
}
