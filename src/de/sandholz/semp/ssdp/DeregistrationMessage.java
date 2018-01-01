/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sandholz.semp.ssdp;

import de.sandholz.semp.core.Param;

/**
 * This class builds the SSDP Messages for deregistration. For each search target
 * announced in the registration process, one NOTIFY message has to be sent 
 * during deregistration. One for the root-device, one for the device-UUID and
 * one for every device-type and service implemented. Usually three messages
 * (root-device, device UUID, 1x device-type) have to be sent.
 * 
 * @author Markus Sandholz
 */
public class DeregistrationMessage {
    
    //class variables
    private static String NT = "NT: ";
    private final static String NTS = "ssdp:byebye";
    private static String USN = "USN: ";
    
    // methods
    /**
     * Generates a SSDP NOTIFY Message for deregistration. For each search target
     * announced in the registration process, one NOTIFY message has to be sent 
     * during deregistration. One for the root-device, one for the device-UUID and
     * one for every device-type and service implemented.
     * 
     * @param mst ROOT_DEVICE, DEVICE_UUID or DEVICE_TYPE
     * @return Deregistration Message as String.
     */
    public static String getMessage(MessageSubTypeRef mst) {
    
       
        switch (mst) {
          case ROOT_DEVICE:        	  
        	  NT = Param.SSDP_PRODUCT_NAME.getValue();
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
        message.append("NOTIFY * HTTP/1.1\r\n");
        message.append("HOST: ").append(Param.SSDP_MULTICAST_ADDRESS.getValue()).append("\r\n");
        message.append("NT: ").append(NT).append("\r\n");
        message.append("NTS: ").append(NTS).append("\r\n");
        message.append("USN: " ).append(USN).append("\r\n\r\n");
    
        return message.toString();
    }
}
