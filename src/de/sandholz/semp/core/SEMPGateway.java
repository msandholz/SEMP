/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sandholz.semp.core;

import com.sun.net.httpserver.HttpServer;
//import org.apache.logging.log4j.*;
import de.sandholz.semp.http.*;
import de.sandholz.semp.ssdp.*;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

/**
 *
 * @author Markus Sandholz
 */
public class SEMPGateway {
    
    // class variable
    //private static final Logger log = LogManager.getLogger("SEMP");
    private static final Logger log = Logger.getLogger("SEMP");
    private static SEMPGateway instance = null;
    private static int deviceSubID = 0;
    private static List<Device> deviceList = new ArrayList<Device>();
    private static Date startTime= null;

    
    // instance varible
    private Timer timer = new Timer();
    private SSDPSender ssdpSender;
    private SSDPReceiver ssdpReceiver;
    private Thread receiverThread;
    private HttpServer httpServer;

            
    		
    // constructor
    private SEMPGateway(){}	

	
    // methods
    
    /**
     * Singelton pattern for the SEMP Gateway. 
     * Only one instance of the SEMP Object could exsist. 
     * 
     * @return Instance of the SEMPGateway object. 
     */
    public static SEMPGateway getInstance(){
	
        if(instance == null){
            instance = new SEMPGateway();
            //log.info("SEMP Gateway initialized!");
        }      
        
        return instance;
    }
    
    public void delInstance(){
        
        instance = null;
    }
    
    public String getStartTime(){
        Date currentTime = new Date(); 
		
	int fDays = 1000*60*60*24;
	int fHour = 1000*60*60;
	int fMin = 1000*60;
        
        long diffTime = currentTime.getTime()-startTime.getTime();
		
	int days = (int) (diffTime/fDays);
	int hours = (int) ((diffTime-days*fDays)/fHour);
	int minutes = (int) ((diffTime-days*fDays-hours*fHour)/fMin);
		
	SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yy-HH:mm ");
        String retVal = formatter.format(startTime)+"["+String.valueOf(days)+"d "+String.valueOf(hours)+"h "+String.valueOf(minutes)+"m]";  
	      			
	return retVal;
    }
    
    /**
     * Add controlable devices to the SEMP Gateway.
     * 
     * @param device Device
     */
    public void addDevice(Device device) {
	
        if(instance.deviceSubID < 9) {
			
            device.setDeviceID(Param.SEMP_GW_DEVICE_ID.getValue()+String.valueOf(deviceSubID));
            SEMPGateway.deviceList.add(device);
            instance.deviceSubID++;
			
	} else {
           log.warning("Max num of devices reached!");
	}
    }
    
    /**
    *  Returns the XML Message
    * 
    * @param messagePartRefType MessagePartRefType
    * @param deviceID ID of the device
    * @return String XML Structure
    */
    public String getMessage(MessagePartRefType messagePartRefType, String deviceID) {
		
	StringBuilder sbRoot = new StringBuilder();
        StringBuilder sbDeviceInfo = new StringBuilder();
        StringBuilder sbDeviceStatus = new StringBuilder();
		
	sbRoot.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");		
	sbRoot.append("<Device2EM xmlns=\"http://www.sma.de/communication/schema/SEMP/v1\">\r\n");
		
	for(Device device: deviceList){
            String id = device.getDeviceID();
			
            if(deviceID.equals(id) || deviceID.equals("all")) {
				
		if(messagePartRefType.equals(MessagePartRefType.DeviceInfo) || messagePartRefType.equals(MessagePartRefType.all)) {
                    sbDeviceInfo.append(device.getMessageDeviceInfoPart());
		} 
			
		if(messagePartRefType.equals(MessagePartRefType.DeviceStatus) || messagePartRefType.equals(MessagePartRefType.all)) {
                    sbDeviceStatus.append(device.getMessageDeviceStatusPart());
		} 
                
                if(messagePartRefType.equals(MessagePartRefType.PlanningRequest) || messagePartRefType.equals(MessagePartRefType.all)) {
                    sbDeviceStatus.append(device.getMessageDevicePlanningPart());
		} 
            }		
				
            //for(device in EMPGateway.deviceList){

	}
        
        sbRoot.append(sbDeviceInfo).append(sbDeviceStatus);
        
        
	sbRoot.append("</Device2EM>");	
		
        if(Param.SEMP_XSD_SCHEMA.getValue().equals("none")){
            
            log.finest("SEMP message validation: Disabled!\n"+sbRoot.toString());
            
        } else {  
            this.parseDevice2EM(sbRoot.toString());       
        }
        
	return sbRoot.toString();
    }
    
    public List<Device> getDeviceList(){
        return deviceList;
    }
        

    /**
    * Starts the SEMP Gateway. 
    * 
    * At least one device must be added!
    */
    public void start() {

            startHTTPServer(); // Start the HTTP Server                
            startSSDPService(); // Start the SSDP Service 
            startTime = new Date();
            log.info("SEMP Gateway started succesfully!");     
    }
    
    /**
     * Stops the SEMP Gateway.
     */
    public void stop() {
        // Stop the SSDP part
        timer.cancel();
	ssdpReceiver.stopThread();
        
        // Stop the HTTP part.
        httpServer.stop(0);
        log.info("SEMP Gateway stopped!");
    }         

    /**
    * Checks, if at least one controlable device exists.
    * 
    * @return boolean
    */
    private static boolean isEmpty() {
	return SEMPGateway.deviceList.isEmpty();	
    }
    
    
    /**
    * Validate the Device2EM-Message against the XSD-Schema.
    * 
    * @return boolean
    */
    public boolean parseDevice2EM(String Device2EM){
       
       String PATH = "/resources/"+Param.SEMP_XSD_SCHEMA.getValue();
        
       try {

           SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);                 
            
           Source schemaFile = new StreamSource(this.getClass().getResourceAsStream(PATH));
           Schema schema = factory.newSchema(schemaFile);
           Validator validator = schema.newValidator();
           validator.validate(new StreamSource(new StringReader(Device2EM)));
           log.finest("SEMP message validation: OK!\n"+Device2EM);
           
        } catch (Exception e) {
            log.warning("SEMP message validation: "+e.getMessage()+"\n"+Device2EM);
            return false;
        }
        return true;
    }
    
    
    /**
    * Validate the EM2Device-Message and notify redistered Device listeners.
    * @param EM2Device XML Message received by SEMPGateway. 
    */
    public void parseEM2Device(String EM2Device){
    
        final String DEVICEID_Start = "<DeviceId>";
        final String DEVICEID_End = "</DeviceId>";
        String DeviceID = "";
		
        final String RECOM_Start = "<On>";
        final String RECOM_End = "</On>";
        String Recom = "";
		
        int pos_start = 0;
        int pos_end = 0;
		
        while (EM2Device.indexOf(DEVICEID_Start) > -1) {
            pos_start = EM2Device.indexOf(DEVICEID_Start) + DEVICEID_Start.length();
            pos_end = EM2Device.indexOf(DEVICEID_End);				
            DeviceID = EM2Device.substring(pos_start, pos_end);			
			
            pos_start = EM2Device.indexOf(RECOM_Start) + RECOM_Start.length();
            pos_end = EM2Device.indexOf(RECOM_End);				
            Recom = EM2Device.substring(pos_start, pos_end);	

            notifyDeviceListeners(DeviceID, Boolean.valueOf(Recom));
            EM2Device = EM2Device.substring(pos_end+RECOM_End.length());
	}		
    }   
     
    
    /**
    * Starts the SSDP service für UPnP discovery.
    */
    private static void startSSDPService(){
        
        instance.ssdpSender = new SSDPSender();
        
        instance.ssdpReceiver = new SSDPReceiver(instance.ssdpSender);     
        instance.receiverThread = new Thread(instance.ssdpReceiver);
        instance.receiverThread.start();
        
        int resent = Integer.valueOf(Param.SSDP_CACHE_CONTROL.getValue())/2*1000;
        
        instance.timer.scheduleAtFixedRate(instance.ssdpSender, 250, resent);
    }
    
    /**
     * Starts the HTTP Server for SEMP web services.
     */
    private static void startHTTPServer(){
        try {  
            
            InetSocketAddress isa = new InetSocketAddress(Param.HTTP_ADDRESS.getValue(),Integer.valueOf(Param.HTTP_PORT.getValue()));
            instance.httpServer = HttpServer.create(isa, 0);
            
            instance.httpServer.createContext(Param.SSDP_LOCATION_PATH.getValue(), new UPnPDeviceDescriptionHandler());
            instance.httpServer.createContext(Param.HTTP_BASE_URL.getValue(), new SEMPHandler());     
            
            if(!Param.WEBSITE_URL.getValue().equals("none")) {
                        
                instance.httpServer.createContext(Param.WEBSITE_URL.getValue()+"/css", new DeviceCSSHandler());
                instance.httpServer.createContext(Param.WEBSITE_URL.getValue()+"/image", new DeviceImageHandler());
                instance.httpServer.createContext(Param.WEBSITE_URL.getValue()+"/log", new DeviceLogHandler());
                instance.httpServer.createContext(Param.WEBSITE_URL.getValue(), new DeviceURLHandler());           
            }
            
            instance.httpServer.createContext("/", new ErrorHandler());  
            instance.httpServer.start();

            log.info("SEMP receiver thread listening on http://" + Param.HTTP_ADDRESS.getValue() +":"+Param.HTTP_PORT.getValue()+ Param.HTTP_BASE_URL.getValue());
        
        } catch (Exception e) {  
            log.severe("SEMP receiver do not start!"+e.getMessage());
        }
    }
    
    
    /**
     * Notifies all registered device listener.
     * 
     * @param deviceID
     * @param switchRecommendation 
     */
    private void notifyDeviceListeners(String deviceID, boolean switchRecommendation){
               
        for(Device device: deviceList){
          
            if(device.getDeviceID().equals(deviceID))
            {                       
                log.info("EM2Device message switch recommendation: "+device.deviceIdentification.getDeviceName() + "["+deviceID+"] = "+switchRecommendation);
                device.setSwitchRecommendation(switchRecommendation);
                device.notifyListeners();
                break;
            }
        }
    }
}