/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sandholz.semp.ssdp;

import de.sandholz.semp.core.Param;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Generates a M-Search-Response messages. Responses for M-Search requests
 * are similar to NOTIFY message. An M-Search response is sent in a unicast
 * UDP datagram to the device that started the search - in SEMP usually the EM.
 * The message will be adressed to the source IP-adress and port of the 
 * request message.
 * 
 * @author Markus Sandholz
 */
public class MSearchResponseMessage {
    
    //class variables
    public final static String SSDP_MSEARCH_REQUEST = "M-SEARCH * HTTP/1.1"; 
    public final static String SSDP_MSEARCH_RESPONSE ="HTTP/1.1 200 OK";
    private static String ST = "ST: ";
    private static String USN = "USN: ";
        
    // methods
    /**
     * Generates a M-Search-Response messages. Responses for M-Search requests
     * are similar to NOTIFY message. An M-Search response is sent in a unicast
     * UDP datagram to the device that started the search - in SEMP usually the EM.
     * The message will be adressed to the source IP-adress and port of the 
     * request message.
     * 
     * @param mst ROOT_DEVICE, DEVICE_UUID or DEVICE_TYPE
     * @return Registration Message as String.
     */
    public static String getMessage(MessageSubTypeRef mst) {
       
       Date d = new Date();
             
       DateFormat dfmt = new SimpleDateFormat("MMM d HH:mm:ss YYYY", Locale.ENGLISH);
       DateFormat dfday = new SimpleDateFormat("EEEE", Locale.ENGLISH);
      
       String date = dfday.format(d).substring(0,3) +" "+ dfmt.format(d);
        
        
        switch (mst) {
          case ROOT_DEVICE:        	  
        	ST = Param.SSDP_NT_ROOT_DEVICE.getValue();
                USN = "uuid:"+Param.SSDP_UUID.getValue()+"::"+Param.SSDP_NT_ROOT_DEVICE.getValue();
              break;
          case DEVICE_UUID:  
        	ST = "uuid:"+Param.SSDP_UUID.getValue();
                USN = "uuid:"+Param.SSDP_UUID.getValue();
              break;
          case DEVICE_TYPE:  
        	ST = Param.SSDP_NT_DEVICE_TYPE.getValue();
                USN = "uuid:"+Param.SSDP_UUID.getValue()+"::"+Param.SSDP_NT_DEVICE_TYPE.getValue();
              break;
          default:
        }
	   
                       
        StringBuilder message = new StringBuilder();
        message.append(SSDP_MSEARCH_RESPONSE).append("\r\n");
        message.append("CACHE-CONTROL: max-age=").append(Param.SSDP_CACHE_CONTROL.getValue()).append("\r\n"); 
        message.append("DATE: ").append(date).append("\r\n");
        message.append("EXT: ").append("\r\n");
        message.append("LOCATION: http://").append(Param.HTTP_ADDRESS.getValue()).append(":").append(Param.HTTP_PORT.getValue()).append(Param.SSDP_LOCATION_PATH.getValue()).append("\r\n");
        //message.append("LOCATION: http://").append(Param.HTTP_ADDRESS.getValue()).append(":").append(Param.HTTP_PORT.getValue()).append("/uuid:").append(Param.SSDP_UUID.getValue()).append("/description.xml\r\n");
        
        
        message.append("SERVER: ").append(Param.HOST_OS.getValue()).append(" UPnP/1.0 ").append(Param.SSDP_PRODUCT_NAME.getValue()).append("/").append(Param.SSDP_PRODUCT_VERSION.getValue()).append("\r\n");        message.append("ST: ").append(ST).append("\r\n");
        message.append("USN: ").append(USN).append("\r\n\r\n");
    
        return message.toString();
    }
}
