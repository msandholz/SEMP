/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sandholz.semp.http;

import de.sandholz.semp.core.Param;

/**
 * General information for identifying the device. 
 *
 * @author Markus Sandholz
 */
public class DeviceIdentification {

    // instance variables
    private String deviceName =""; // Human readable device name 
    private DeviceTypeRefType deviceType; // Human readable type of the device. See DeviceTypeRefType for well-known types. 

    private String deviceSerial = "2332532424"; // Vendor specific serial number of the device.
    private String deviceVendor = "Some Vendor"; // Human readable name of the device vendor. 

    // constructor
    /**
     * General information for identifying the device. 
     * NOTE: The device serial number is auto generated.
     * NOTE: The device vendor name is taken from Param.manufactorer.
     * @param deviceName Human readable device name
     * @param deviceType Human readable type of the device. See DeviceTypeRefType for well-known types. 
     */
    public DeviceIdentification(String deviceName, DeviceTypeRefType deviceType) {
            this.deviceName = deviceName;
            this.deviceType = deviceType;
    }
	
    // accessor methods
        
    /**
     * Get the name of the device.
     * 
     * @return String Name of the device.
     */
    public String getDeviceName(){
        return this.deviceName;
    }

    /**
     * Get the serial number of the device.
     * 
     * @return String Serialnumber of the device.
     */
    public String getSerialNumber(){
        return this.deviceSerial;
    }

    /**
     * Builds the xml-structure of the device identification part.
     * @param deviceID ID of the Device.
     * @return xml-structure as string.
     */
    public String getMessage(String deviceID){

        StringBuilder sb = new StringBuilder();

        sb.append("  <Identification>").append("\r\n");
        sb.append("    <DeviceId>").append(deviceID).append("</DeviceId>\r\n");
        sb.append("    <DeviceName>").append(this.deviceName).append("</DeviceName>\r\n");
        sb.append("    <DeviceType>").append(this.deviceType).append("</DeviceType>\r\n");
        sb.append("    <DeviceSerial>").append(this.deviceSerial).append("</DeviceSerial>\r\n");
        sb.append("    <DeviceVendor>").append(this.deviceVendor).append("</DeviceVendor>\r\n");
        sb.append("    <DeviceURL>http://").append(Param.HTTP_ADDRESS.getValue()).append(":").append(Param.HTTP_PORT.getValue()).append(Param.WEBSITE_URL.getValue()).append("</DeviceURL>\r\n");
        sb.append("  </Identification>\r\n");

        return sb.toString();
    }	
}