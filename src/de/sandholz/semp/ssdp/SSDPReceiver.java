/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sandholz.semp.ssdp;

import de.sandholz.semp.core.Param;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.logging.Logger;


/**
 * Receiver for SSDP Messages. The receiver is listening at SSDP_MULTICAST_ADDRESS 239.255.255.250:1900.
 * 
 * @author Markus Sandholz
 */
public class SSDPReceiver implements Runnable {
    
    // class variables
    //private static final Logger log = LogManager.getLogger("SEMP");
    private static final Logger log = Logger.getLogger("SEMP");
    private final static String SSDP_MSEARCH_REQUEST = "M-SEARCH * HTTP/1.1"; 
    private final static String SSDP_MSEARCH_RESPONSE ="HTTP/1.1 200 OK";
    
    // instance variables
    private boolean finished = false;
    private SSDPSender ssdpSender;
    private InetAddress multicastAddress;
    private Integer multicastPort;
    
    
    
    // constructor
    /**
     * Constructor for a SSDP Reciever class.
     * 
     * @param ssdpSender Object of SSDP Sender
     */
    public SSDPReceiver(SSDPSender ssdpSender){
        this.ssdpSender = ssdpSender; 
        
        try {
            String[] parts = Param.SSDP_MULTICAST_ADDRESS.getValue().split(":");
            this.multicastAddress = InetAddress.getByName(parts[0]); // multicast address for SSDP  
            this.multicastPort = Integer.valueOf(parts[1]); // standard port for SSDP
        } catch (Exception e) {
            
            log.severe("Unabel to start SSDP Receiver: "+ e.getMessage());
        }
    }

    
    //methods
    /**
     * Stops the SSDP receicver thread.
     */
    public void stopThread() {
    	finished = true;
        
        log.info("SSDP Receiver thread stopped!");
    }
    
    /**
     * Run the SSDP receiver thread.
     */
    @Override
    public void run() {
	
        log.info("SSDP Receiver thread listening on: ["+ this.multicastAddress.getHostAddress()+":"+this.multicastPort.toString()+"] - Waiting for incoming data..."); 
        MulticastSocket socket;               
	    
        try {
	        //1. creating a server socket, parameter is local port number
                socket = new MulticastSocket(this.multicastPort);
                socket.setReuseAddress(true);
                socket.joinGroup(this.multicastAddress);
	         
	        //buffer to receive incoming data
	        byte[] buffer = new byte[8192];
	        DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
	         
	        //communication loop
	        do {
	            socket.receive(incoming);
	            byte[] data = incoming.getData();
	            String incomingMessage = new String(data, 0, incoming.getLength());
                    
                    //log.debug("Data reveived from: "+incoming.getAddress().getHostAddress() + ":" + incoming.getPort());
                   
                    parseMessage(incomingMessage, incoming.getAddress().getHostAddress(), incoming.getPort());
                    
	        } while(!finished);
	    } catch(IOException e){
	        log.severe("Unable to start SSDP Receiver thread: " + e.getMessage());
	    }	
	}
    
    /**
     * Parse the incoming SSDP messages, check the message typ and send the appropriate response message. 
     * 
     * @param incomingMessage Received SSDP message.
     * @param senderIPAddress IP Adress of the sender.
     * @param senderPort Port of the sender.
     */
    private void parseMessage(String incomingMessage, String senderIPAddress, int senderPort) {
        
        // check, if message is a M-Search-Response message
        if (incomingMessage.indexOf(SSDP_MSEARCH_REQUEST) == 0) {
            
            Param.SSDP_UNICAST_ADDRESS.setValue(senderIPAddress+":"+String.valueOf(senderPort));
            
            // search by device-type
            if(incomingMessage.indexOf(Param.SSDP_NT_DEVICE_TYPE.getValue()) > 0) {
                      
                //log.debug("Received SSDP M-SEARCH-REQUEST (search by device-type) from "+ Param.SSDP_UNICAST_ADDRESS.getValue()+"\n"+incomingMessage);
                
                log.fine("SSDP M-SEARCH-REQUEST (search by device-type) received from ["+ Param.SSDP_UNICAST_ADDRESS.getValue()+"]");
                ssdpSender.sendMSearchResponseByDeviceType();
            }
            
            // search by device-UUID
            if(incomingMessage.indexOf("ST: uuid:"+Param.SSDP_UUID.getValue()) > 0) {
                log.fine("SSDP M-SEARCH-REQUEST (search by device-UUID) received from ["+Param.SSDP_UNICAST_ADDRESS.getValue()+"] - No further Messages implemented!");
            }
                        
            // search for all SSDP enabled devices
            if(incomingMessage.indexOf("ST: ssdp:all") > 0) {
               log.fine("SSDP M-SEARCH-REQUEST (search for all SSDP enabled devices) received from ["+Param.SSDP_UNICAST_ADDRESS.getValue()+"] - No further Messages implemented!");
            }

        } else {
            
            log.finest("Received message are no SSDP M-SEARCH-REQUEST.");            
        }
    }   
}