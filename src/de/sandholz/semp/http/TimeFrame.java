/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sandholz.semp.http;

import java.util.Calendar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Sequence of timeframes that constitute the planning request. Timeframes for one device must not 
 * overlap (in terms of earliestStart and latestEnd) as a device can only run once at a time. 
 * In case timeframes overlap the EM has to modify them (e.g. merge or strip) which might lead to 
 * unexpected behavior. 
 * 
 * @author Markus Sandholz
 *
 */
public class TimeFrame {

    // class variable
    private static final Logger log = LogManager.getLogger("SEMP");
    
    // instance variables
    private String earliestStart = null; // Represents the earliest possible time the device can be switched on by the EM. 	
    private String latestEnd = null; // Represents the latest possible end time the requested minimum runtime (MinRunningTime) must be allocated to the device.
    private long earliestStartInSec = 0;
    private long latestEndInSec = 0;    
    private int minRunningTime = 0; // Minimum running time within the timeframe in seconds. If MinRunningTime is 0, the operation of the device in this timeframe is optional. 
    private int maxRunningTime = 0; // Maximum running time within the timeframe in seconds. If MinRunningTime equals MaxRunningTime, all of the given runtime is required.
    private int minRemainingTime = 0;
    private int maxRemainingTime = 0;
    private boolean isPlannedTime = false;   

    //private long timeStampInSec = 0;
    //private long RTRun = 0;
    //private boolean active = false;
    
// constructor
    /**
    * Sequence of timeframes that constitute the planning request. Timeframes for one device must not 
    * overlap (in terms of earliestStart and latestEnd) as a device can only run once at a time. 
    * In case timeframes overlap the EM has to modify them (e.g. merge or strip) which might lead to 
    * unexpected behavior. 
    * 
    * @param earliestStart Represents the earliest possible time the device can be switched on by the EM. 	
    * @param latestEnd Represents the latest possible end time the requested minimum runtime (MinRunningTime) must be allocated to the device.
    * @param minRunningTime Minimum running time within the timeframe in seconds. If MinRunningTime is 0, the operation of the device in this timeframe is optional. 
    * @param maxRunningTime Maximum running time within the timeframe in seconds. If MinRunningTime equals MaxRunningTime, all of the given runtime is required.
    */
    public TimeFrame(String earliestStart, String latestEnd, int minRunningTime, int maxRunningTime){
       
        this.earliestStart = earliestStart;
        this.latestEnd = latestEnd;
       
        this.minRunningTime = minRunningTime * 60;
        this.maxRunningTime = maxRunningTime * 60;
    }

    //accessor methods
    
    public String getEarliestStart(){
        return this.earliestStart;
    }
    
    public String getLatestEnd(){
        return this.latestEnd;
    }
    
    public int getMinRunningTime(){
        return this.minRunningTime / 60;
    }
    
    public int getMaxRunningTime(){
        return this.maxRunningTime / 60;
    }
    
    public boolean isPlannedTime() {
        return this.isPlannedTime;
    }
    
    
    //methods 
    
    /**
    * Builds the xml-structure of the &lt;PowerInfo&gt; part of &lt;DeviceStatus&gt;&lt;PowerConsumption&gt;.
    * 
    * @param deviceID The ID of the SEMP Device.
    * @param RTRun RunningTime of SEMP Device.
    * @return xml-structure as string.
    */
    public String getMessage(String deviceID, int RTRun) {

        this.calcTime(RTRun);      
        StringBuilder sb = new StringBuilder();	

        sb.append("   <Timeframe>\r\n");
        sb.append("    <DeviceId>").append(deviceID).append("</DeviceId>\r\n");
        sb.append("    <EarliestStart>").append(this.earliestStartInSec).append("</EarliestStart>\r\n");
        sb.append("    <LatestEnd>").append(this.latestEndInSec).append("</LatestEnd>\r\n");
        sb.append("    <MinRunningTime>").append(this.minRemainingTime).append("</MinRunningTime>\r\n");
        sb.append("    <MaxRunningTime>").append(this.minRemainingTime).append("</MaxRunningTime>\r\n");
        sb.append("   </Timeframe>\r\n");

        //log.info("EaliestStart["+this.earliestStart+"] | LatestEnd["+this.latestEnd+"] | MinRunningTime["+this.minRemainingTime+"] | MaxRunningTime["+this.maxRemainingTime+"] | RTRun["+RTRun+"] | isPlannedTime["+this.isPlannedTime()+"]");
        
        return sb.toString();
    }
    
    /**
    * Calculates the relative Timestamps based on the current time.
    */ 
    private void calcTime(int RTRun){
        
        Calendar cal = Calendar.getInstance();
        
        String[] startTime = this.earliestStart.split(":");
        String[] endTime = this.latestEnd.split(":");
        

        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0); 
        long currentTime = cal.getTimeInMillis()/1000;
              
        cal.set(Calendar.MINUTE, Integer.parseInt(startTime[1]));
	cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startTime[0]));
        long earliestStartTime = cal.getTimeInMillis()/1000;

        cal.set(Calendar.MINUTE, Integer.parseInt(endTime[1]));
	cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endTime[0]));        
        long latestEndTime = cal.getTimeInMillis()/1000;
        
        
        if (currentTime > latestEndTime){
           
            cal.add(Calendar.DAY_OF_YEAR, 1);
            cal.set(Calendar.MINUTE, Integer.parseInt(startTime[1]));
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startTime[0]));
            this.earliestStartInSec = cal.getTimeInMillis()/1000-currentTime;
            
            cal.set(Calendar.MINUTE, Integer.parseInt(endTime[1]));
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endTime[0]));
            this.latestEndInSec = cal.getTimeInMillis()/1000-currentTime;
            
            this.minRemainingTime = this.minRunningTime;
            this.maxRemainingTime = this.maxRunningTime;     
            
            this.isPlannedTime = false;
                       
       } else if (currentTime < earliestStartTime){
           
            this.earliestStartInSec = earliestStartTime-currentTime;
            this.latestEndInSec = latestEndTime-currentTime;
           
            this.minRemainingTime = this.minRunningTime;
            this.maxRemainingTime = this.maxRunningTime;     
            
            this.isPlannedTime = false;
           
       } else {

            this.earliestStartInSec = 0;
            this.latestEndInSec = latestEndTime-currentTime;
            
            this.minRemainingTime = this.minRunningTime-RTRun;
            
            if(this.minRunningTime-RTRun < 0){
              this.minRemainingTime = 0;  
            }
                
            this.maxRemainingTime = this.maxRunningTime-RTRun;
            
            if(this.maxRunningTime-RTRun < 0){
              this.maxRemainingTime = 0;  
            }
            
            this.isPlannedTime = true;
            
       }   
    }        
}