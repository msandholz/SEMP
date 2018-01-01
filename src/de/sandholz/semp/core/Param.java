/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sandholz.semp.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.*;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;


/**
 *
 * @author Markus Sandholz
 */
public enum Param {
    
    LOG_LEVEL,
    CONSOLE_OUTPUT,
    DEVICE_STATUS_ON_BOOT,
    SSDP_PRODUCT_NAME,
    SSDP_PRODUCT_VERSION,
    SSDP_UUID,
    SSDP_NT_ROOT_DEVICE,
    SSDP_NT_DEVICE_TYPE,
    SSDP_MULTICAST_ADDRESS,
    SSDP_UNICAST_ADDRESS,
    SSDP_CACHE_CONTROL,
    SSDP_LOCATION_PATH,
    HOST_OS,
    HTTP_ADDRESS,
    HTTP_PORT,
    HTTP_BASE_URL,
    WEBSITE_MODE,
    WEBSITE_URL,
    WEBSITE_REFRESH,
    WEBSITE_LOGO,
    WEBSITE_FAVICON,
    WEBSITE_CSS,
    SEMP_XSD_SCHEMA,
    SEMP_GW_FRIENDLY_NAME,
    SEMP_GW_MANUFACTORER,
    SEMP_GW_DEVICE_ID;


    // class variable
    private static final Logger log = Logger.getLogger("SEMP");
    public static final String PATH_INSIDE_JAR = "/resources/";
    public static final String PATH_OUTSIDE_JAR = System.getProperty("user.dir")+PATH_INSIDE_JAR;
    public static final String CONFIG_FILE_NAME = "config.xml";
    private static Properties properties;

    // instance variable
    private String value;

    // methods
    private void init() {
        if (properties == null) {
            properties = new Properties();
            
            try {               
                File dir = new File(PATH_OUTSIDE_JAR);
                if(!dir.exists()){              
                    dir.mkdir();
                }
                                
                File file = new File(PATH_OUTSIDE_JAR+CONFIG_FILE_NAME);
                
                if(!file.exists() || file.length()==0){
                    
                    InputStream orgConfigFile = Param.class.getResourceAsStream(PATH_INSIDE_JAR+CONFIG_FILE_NAME);
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(orgConfigFile), 1);

                    PrintWriter newConfigFile = new PrintWriter(new FileWriter(PATH_OUTSIDE_JAR+CONFIG_FILE_NAME));

                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        newConfigFile.println(line);
                    }

                    orgConfigFile.close();
                    newConfigFile.close();

                } 
                InputStream inputStream = new FileInputStream(PATH_OUTSIDE_JAR+CONFIG_FILE_NAME);
            	properties.loadFromXML(inputStream);
                            
                setLogFormatter();
            }
            catch (Exception e) {
                log.severe("Unable to load config file from: " + PATH_OUTSIDE_JAR+CONFIG_FILE_NAME + " "+e.getMessage());
                System.exit(1);
            }
        }
        value = (String) properties.get(this.toString());
    }

    public String getValue() {
        if (value == null) {
            init();
        }
        
        if (value.equals("auto")) {value = getAutoValue();}         	
        	
        return value;
    }
     
    public void setValue (String value){
        this.value = value;
    }       
    
    private String getAutoValue(){
        
        String autoValue = "auto";
        
            switch (this.toString())
            {
                case "HOST_OS" :                 
                    autoValue = System.getProperty("os.name")+"/"+System.getProperty("os.version");
                    break;
                
                case "SSDP_UUID" :
                    autoValue = String.valueOf(UUID.randomUUID());
                    break;
                    
                case "HTTP_ADDRESS" :                   
                    
                    try 
                    {
                        Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
                        
                        while (nis.hasMoreElements())
                        {
                            NetworkInterface ni = nis.nextElement();
                            for(InetAddress ip : Collections.list(ni.getInetAddresses()))
                            {
                                if(!ip.isLoopbackAddress() && ip.isSiteLocalAddress()) 
                                {
                                  autoValue = ip.getHostAddress();
                                }
                            }
                        }
                    }
                    catch (Exception e){ log.severe("Unable to get local IP for config param: " + this.toString()+"=auto "+e.getMessage());}
                    break;
                
                default: 
                    log.warning("No method for config param: " + this.toString()+"=auto");
            }
                
        return autoValue;
    }    

    private static void setLogFormatter(){
		
        Level logLevel;
        switch(Param.LOG_LEVEL.getValue()){
            case "ALL":
                logLevel = Level.ALL;
                break;
            case "SEVERE":
                logLevel = Level.SEVERE;
                break;
            case "WARNING":
                logLevel = Level.WARNING;
                break;
            case "INFO":
                logLevel = Level.INFO;
                break;    
            case "CONFIG":
                logLevel = Level.CONFIG;
                break; 
            case "FINE":
                logLevel = Level.FINE;
                break;
            case "FINER":
                logLevel = Level.FINER;
                break;
            case "FINEST":
                logLevel = Level.FINEST;
                break;
            default: logLevel = Level.OFF;
        }
        Param.CONSOLE_OUTPUT.getValue().equals("true");  
        
	log.setUseParentHandlers(false);
	log.setLevel(logLevel);
		
	final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");//dd/MM/yyyy
	    
	Formatter SEMPFormat = new Formatter() {
            public String format(LogRecord record) {
	    	String level = "["+record.getLevel().toString()+ "]";
	    	
	    	while(level.length() < 10) {level = level + " ";}
                
	    	StringBuilder str = new StringBuilder();
	    	str.append(level);
	    	str.append(sdfDate.format(record.getMillis()));
                str.append(" [").append(Thread.currentThread().getName()).append("] ");
                str.append(record.getLoggerName()).append(" (");
                
                String fullClassName = record.getSourceClassName();              
  	    	str.append(fullClassName.substring(fullClassName.lastIndexOf('.')+1)).append(".");
	    	
                str.append(record.getSourceMethodName()).append(") - ");
	    	str.append(record.getMessage()).append("\r\n");  
	    	return str.toString();
	    }
	};
	    
	if(Param.CONSOLE_OUTPUT.getValue().equals("true")) {
            
	    Handler conHandler = new ConsoleHandler();
	    conHandler.setLevel(logLevel);
            conHandler.setFormatter(SEMPFormat);
            log.addHandler(conHandler);	
	}
	   
	Handler fileHandler;
            try {
		fileHandler = new FileHandler("log.txt",5000000,5,false);
                fileHandler.setLevel(logLevel);
                fileHandler.setFormatter(SEMPFormat);
                log.addHandler(fileHandler);
		    
            } catch (Exception e) {
		log.severe("Unable write in logfile. " +e.getMessage());
            } 
    }
}