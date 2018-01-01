/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sandholz.semp.ssdp;

import de.sandholz.semp.core.Param;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.TimerTask;
import java.util.logging.Logger;


/**
 * Sender for SSDP Messages.
 * 
 * @author Markus Sandholz
 */
public class SSDPSender extends TimerTask{
    
    // class variables
    private static final Logger log = Logger.getLogger("SEMP");
    
    // methods
    /**
     * Runs the SSDP sender as timer task.
     */
    @Override
    public void run() {
        sendNotify();
    }
    
    /**
     * Send a SSDP notify message.
     */
    public void sendNotify() {       

        log.fine("SSDP NOTIFY messages send to ["+ Param.SSDP_MULTICAST_ADDRESS.getValue()+"]");
        send(true, RegistrationMessage.getMessage(MessageSubTypeRef.ROOT_DEVICE));
        send(true, RegistrationMessage.getMessage(MessageSubTypeRef.DEVICE_UUID));
        send(true, RegistrationMessage.getMessage(MessageSubTypeRef.DEVICE_TYPE));
    }
    
    /**
    * Send a SSDP de-register message.
    */
    public void sendDeregister(){
        
        log.fine("SSDP DEREGISTRATION messages send to ["+ Param.SSDP_MULTICAST_ADDRESS.getValue()+"]");
        send(true, DeregistrationMessage.getMessage(MessageSubTypeRef.ROOT_DEVICE));
        send(true, DeregistrationMessage.getMessage(MessageSubTypeRef.DEVICE_UUID));
        send(true, DeregistrationMessage.getMessage(MessageSubTypeRef.DEVICE_TYPE));

    }
    
    /**
    * Send a SSDP m-search-response message by device.
    */
    public void sendMSearchResponseByDeviceType() {

        log.fine("SSDP M-SEARCH-RESPONSE (search by device-type) message send to ["+ Param.SSDP_UNICAST_ADDRESS.getValue()+"]");
        send(false, MSearchResponseMessage.getMessage(MessageSubTypeRef.DEVICE_TYPE));
    }
    
    /**
     * Send SSDP message.
     * 
     * @param Multicast true=multicast / false=unicast
     * @param SSDPMessage SSDP Message as string.
     */
    private void send(boolean Multicast, String SSDPMessage){
        
        try {

            DatagramPacket dp;
            
            byte[] txbuf = SSDPMessage.getBytes("UTF-8");
            
            log.finest("SSDP Message:\n"+SSDPMessage);
            
            
            if(Multicast) 
            {
                String[] parts = Param.SSDP_MULTICAST_ADDRESS.getValue().split(":");
                InetAddress multicastAdress = InetAddress.getByName(parts[0]); // multicast address for SSDP  
                Integer multicastPort = Integer.valueOf(parts[1]); // standard port for SSDP
                 
                MulticastSocket socket = new MulticastSocket(multicastPort);
                socket.setReuseAddress(true);
                //socket.setSoTimeout(15000);
                socket.joinGroup(multicastAdress);
                dp = new DatagramPacket(txbuf, txbuf.length, multicastAdress, multicastPort);
                socket.send(dp);
                
            } else {
                
                String[] parts = Param.SSDP_UNICAST_ADDRESS.getValue().split(":");
                InetAddress unicastAdress = InetAddress.getByName(parts[0]); // unicast address for SSDP  
                Integer unicastPort = Integer.valueOf(parts[1]); // standard port for SSDP
                
                DatagramSocket socket = new DatagramSocket(); 
                    
                dp = new DatagramPacket(txbuf, txbuf.length, unicastAdress, unicastPort);
                socket.send(dp); 
            }
                                
        } catch (IOException e){
            
            log.severe("Unable to send SSDP Message: "+e.getMessage());
        }   
    }
}
