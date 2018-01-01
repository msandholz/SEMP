/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sandholz.semp.http;

import de.sandholz.semp.core.Param;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * SEPM device.
 * 
 * @author Markus Sandholz
 */
public class Device {

    private static final Logger log = LogManager.getLogger("SEMP"); 
    
    // instance varobales
    private String deviceID = ""; // Unique identification of the device. 
    private boolean switchRecommendation = false; // Switch on/off recommendation. If On=true, the EM recommends that the device should be switched on or run its program. For interruptible devices On=false is a switch-off recommendation indicating that the device should switch off or pause. 
    public DeviceIdentification deviceIdentification; // General information for identifying the device. 
    public DeviceCharacteristics deviceCharacteristics; // Information on the characteristics of the device. 
    public DeviceCapabilities deviceCapabilities; // This element encapsulates information about the capabilities of the device 
    public DeviceStatus deviceStatus; // A DeviceStatus encapsulates the status information of a device, i.e. all measurements and properties representing the current status of the device. 
    private final List<TimeFrame> planningRequest = new ArrayList<>();
    private final List<DeviceListener> deviceListener = new ArrayList<>();
    private int RTRun = 0; 
    private int timeStamp = 0;
    private boolean maxRemainingTime = true;
    
    // constructors
    /**
     * Contructor to initiate a device object.
     * 
     * @param deviceName Human readable name of the device
     * @param deviceType Type of the device
     * @param maxPowerConsumption  Maximum power consumption of the device
     * @param timeFrame Time frame that the EM can plan the activation of the device 
     */
    public Device(String deviceName, DeviceTypeRefType deviceType, int maxPowerConsumption, boolean interruptsAllowed, boolean optionalEnergy, TimeFrame timeFrame){	
	
        this.deviceID = Param.SEMP_GW_DEVICE_ID.getValue();
	this.deviceIdentification = new DeviceIdentification(deviceName, deviceType);
	this.deviceCharacteristics = new DeviceCharacteristics(maxPowerConsumption);
        this.deviceCapabilities = new DeviceCapabilities(PowerMeasurementRefType.Estimation, false, interruptsAllowed, optionalEnergy);
	this.deviceStatus = new DeviceStatus(); 
        this.planningRequest.add(timeFrame);
        
        switch(Param.DEVICE_STATUS_ON_BOOT.getValue()){
            case "AUTO":
                this.setMode(DeviceModeRefType.Auto);
                break;
            case "ON":
                this.setMode(DeviceModeRefType.On);
                break;
            default:
                this.setMode(DeviceModeRefType.Off);         
        }
    }
    
    // accessor methods
    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }
    
    public String getDeviceName() {
        return this.deviceIdentification.getDeviceName();
    }

    public void setSwitchRecommendation(boolean switchRecommendation) {
           this.switchRecommendation=switchRecommendation;
    }
    
    public boolean getSwitchRecommendation() {
           return this.switchRecommendation;
    }
    
    public void setAveragePower(int averagePower){
        this.deviceStatus.setAveragePower(averagePower);
    }
    
    public int getAveragePower(){
        return this.deviceStatus.getAveragePower();
    }
        
    public DeviceIdentification getDeviceIdentification() {
        return deviceIdentification;
    }

    public void setDeviceIdentification(DeviceIdentification deviceIdentification) {
        this.deviceIdentification = deviceIdentification;
    }

    public DeviceCharacteristics getDeviceCharacteristics() {
        return deviceCharacteristics;
    }

    public void setDeviceCharacteristics(DeviceCharacteristics deviceCharacteristics) {
        this.deviceCharacteristics = deviceCharacteristics;
    }

    public DeviceCapabilities getDeviceCapabilities() {
        return deviceCapabilities;
    }

    public void setDeviceCapabilities(DeviceCapabilities deviceCapabilities) {
        this.deviceCapabilities = deviceCapabilities;
    }

    public DeviceStatusRefType getDeviceStatus() {
        return deviceStatus.getStatus();
    }

    public void setDeviceStatus(DeviceStatus deviceStatus) {
        this.deviceStatus = deviceStatus;
    }
    
    public String getEarliestStart(){
        
        TimeFrame tf = this.planningRequest.get(0);
        return tf.getEarliestStart();
    }
    
    public String getLatestEnd(){
        
        TimeFrame tf = this.planningRequest.get(0);
        return tf.getLatestEnd();
    }
        
    public int getMinRunningTime(){
        
        TimeFrame tf = this.planningRequest.get(0);
        return tf.getMinRunningTime();
    }
                    
    public int getMaxRunningTime(){
        
        TimeFrame tf = this.planningRequest.get(0);
        return tf.getMaxRunningTime();
    }
    
    public boolean isRemainingTime(){
        return this.maxRemainingTime;
    }
    
    // methods
    /**
     * Set the mode of the device (on, off, auto).
     * @param deviceMode Mode of the device.
     */
    public void setMode(DeviceModeRefType deviceMode){    
        
        switch (deviceMode){
            
            case On:        
                this.deviceStatus.setEMSignalsAccepted(false);
                this.deviceStatus.setStatus(DeviceStatusRefType.On);
                this.notifyListeners();
                break;
                
            case Auto:            
                this.deviceStatus.setEMSignalsAccepted(true);
                this.deviceStatus.setStatus(DeviceStatusRefType.Off);
                this.notifyListeners();
                break;
            
            default:
                this.deviceStatus.setEMSignalsAccepted(false);
                this.deviceStatus.setStatus(DeviceStatusRefType.Off);
                this.notifyListeners();
        }
    }
    
    /**
     * Get the mode of the device (on, off, auto).
     * @return Mode of the device
     */
    public DeviceModeRefType getMode(){
      
        if (this.deviceStatus.isEMSignalsAccepted()) {
            return DeviceModeRefType.Auto;
        
        } else if (this.deviceStatus.getStatus() == DeviceStatusRefType.On) {     
            return DeviceModeRefType.On;
        
        } else {
            return DeviceModeRefType.Off;
        }        
    }

    /**
     * Set the device status.
     * @param deviceStatus
     */
    public void setStatus(DeviceStatusRefType deviceStatus){
        
        
        if(this.getMode().equals(DeviceModeRefType.Auto)) {
           
            this.deviceStatus.setStatus(deviceStatus);    
                        
            if(deviceStatus.equals(DeviceStatusRefType.On)){       
                this.startWatch();
            }
        
            if(deviceStatus.equals(DeviceStatusRefType.Off) && this.timeStamp > 0){
                this.stopWatch();
            }
        }
    }
    
    
    /**
     * Builds the xml-structure of the DeviceInfo part.
     * 
     * @return xml-structure as string (Part: DeviceInfo) 
     */
    public String getMessageDeviceInfoPart(){
		
	StringBuilder sb = new StringBuilder();
		
	sb.append("<DeviceInfo>").append("\r\n");
	sb.append(this.deviceIdentification.getMessage(this.deviceID));
	sb.append(this.deviceCharacteristics.getMessage());
	sb.append(this.deviceCapabilities.getMessage());	
	sb.append("</DeviceInfo>\r\n");
		
	return sb.toString();
    }	
    
    /**
     * Builds the xml-structure of the DeviceStatus part.
     * @return xml-structure as string (Part: DeviceStatus)
     */
    public String getMessageDeviceStatusPart(){

        return this.deviceStatus.getMessage(deviceID);		
    }
	
    /**
     * Builds the xml-structure of the PlanningRequest part.
     * @return xml-structure as string (Part: PlanningRequest)
     */
    public String getMessageDevicePlanningPart(){
	
        StringBuilder sb = new StringBuilder();  
              
        if (this.getMode().equals(DeviceModeRefType.Auto)) {
                       
            sb.append("<PlanningRequest>").append("\r\n");
        
            for(TimeFrame tf:planningRequest){
                if(!tf.isPlannedTime()){ resetWatch(); }           
                
                int run = 0;
                if(this.timeStamp>0){
                    Calendar cal = Calendar.getInstance();
                    int currentTime = (int) cal.getTimeInMillis()/1000;
                    run = this.RTRun + currentTime - this.timeStamp;
                } 
                               
                sb.append(tf.getMessage(this.deviceID, run));       
                
                if((tf.getMaxRunningTime()*60) < run && this.getDeviceStatus().equals(DeviceStatusRefType.On)) {
                    this.maxRemainingTime = false;
                    notifyListeners();
                }
            }
            
            sb.append("</PlanningRequest>").append("\r\n");
        }
             
        return sb.toString();		
    }

    /**
     * Notify all registered Listeners.
     */
    public void notifyListeners(){
       
        if(!deviceListener.isEmpty()){
            for (DeviceListener dl : deviceListener)
            dl.handleEMChangeEvent(this);
        }
    }
	
    /**
     * Add a Listener to the device.
     * @param toAdd Device listener.
     */
    public void addListener(DeviceListener toAdd){
	deviceListener.add(toAdd);
        this.notifyListeners();
    }
    
    
    /**
    * Clear all Listener for this device.
    */
    public void clearListener(){
	deviceListener.clear();		
    }
    
    /**
     * Builds the xml-structure of the whole SEMP message.
     * @return xml-structure as string.
     */
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        
        sb.append(this.getMessageDeviceInfoPart());
        sb.append(this.getMessageDeviceStatusPart());
        sb.append(this.getMessageDevicePlanningPart());
        
        return sb.toString();
    }
    
    private void startWatch(){
        Calendar cal = Calendar.getInstance();              
        this.timeStamp = (int) cal.getTimeInMillis()/1000;
    }
    
    private void stopWatch(){
        if(this.timeStamp>0) {
            Calendar cal = Calendar.getInstance();
            int currentTime = (int) cal.getTimeInMillis()/1000;            
            this.RTRun = this.RTRun + currentTime - this.timeStamp;
        }
    }
    
    private void resetWatch(){
        this.RTRun = 0;
        this.timeStamp = 0;
        this.maxRemainingTime = true;
    }
}