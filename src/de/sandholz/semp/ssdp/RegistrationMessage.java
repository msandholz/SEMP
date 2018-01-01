/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sandholz.semp.ssdp;

import de.sandholz.semp.core.Param;

/**
 * On Connection a UDP datagram with a NOTIFY message of the type "ssdp:alive"
 * has to be sent to port 1900 of the SSDP multicast address (239.255.255.250).
 * According to SSDP a Controlled Device (here: SEMP gateway) has to send one 
 * SSDP NOTIFY message to announce the so-called root-device, one to announce 
 * its device-UUID and one message for every implemented device-type. 
 * For example if a device implements a device-type, it has to send at least 
 * three NOTIFY messages.
 * 
 * @author Markus Sandholz
 */
public class RegistrationMessage {
    
     //class variables
    private static String NT = "NT: ";
    private final static String NTS = "ssdp:alive";
    private static String USN = "USN: ";
    
    
    // methods
    /**
     * Generates a SSDP NOTIFY Message for registration. According to SSDP a
     * Controlled Device (here: SEMP gateway) has to send one SSDP NOTIFY message 
     * to announce the so-called root-device, one to announce its device-UUID and
     * one message for every implemented device-type. For example if a device 
     * implements a device-type, it has to send at least three NOTIFY messages.
     * 
     * @param mst ROOT_DEVICE, DEVICE_UUID or DEVICE_TYPE
     * @return Registration Message as String.
     */
    public static String getMessage(MessageSubTypeRef mst) {
    
        switch (mst) {
          case ROOT_DEVICE:        	  
        	  NT = Param.SSDP_NT_ROOT_DEVICE.getValue();
                  USN = "uuid:"+Param.SSDP_UUID.getValue()+"::"+Param.SSDP_NT_ROOT_DEVICE.getValue();
              break;
          case DEVICE_UUID:  
        	  NT = "uuid:"+Param.SSDP_UUID.getValue();
                  USN = "uuid:"+Param.SSDP_UUID.getValue();
              break;
          case DEVICE_TYPE:  
        	  NT = Param.SSDP_NT_DEVICE_TYPE.getValue();
                  USN = "uuid:"+Param.SSDP_UUID.getValue()+"::"+Param.SSDP_NT_DEVICE_TYPE.getValue();
              break;
          default:
        }
	                       
        StringBuilder message = new StringBuilder();
        message.append("NOTIFY * HTTP/1.1").append("\r\n");
        message.append("HOST: ").append(Param.SSDP_MULTICAST_ADDRESS.getValue()).append("\r\n");      
        message.append("CACHE-CONTROL: max-age=").append(Param.SSDP_CACHE_CONTROL.getValue()).append("\r\n");
        message.append("SERVER: ").append(Param.HOST_OS.getValue()).append(" UPnP/1.0 ").append(Param.SSDP_PRODUCT_NAME.getValue()).append("/").append(Param.SSDP_PRODUCT_VERSION.getValue()).append("\r\n");        
        message.append("NTS: ").append(NTS).append("\r\n");    
        message.append("LOCATION: http://").append(Param.HTTP_ADDRESS.getValue()).append(":").append(Param.HTTP_PORT.getValue()).append(Param.SSDP_LOCATION_PATH.getValue()).append("\r\n");
        message.append("NT: ").append(NT).append("\r\n");
        message.append("USN: " ).append(USN).append("\r\n\r\n");
    
        return message.toString();
    }
}