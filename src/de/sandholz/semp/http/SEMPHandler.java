/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sandholz.semp.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import de.sandholz.semp.core.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import org.apache.logging.log4j.*;

/**
 * Data requests performed by an EM on a gateway:
 * 
 * Request all information (DeviceInfo, DeviceStatus, PlanningRequests of all devices):
 * o HTTP GET: &lt;baseURL&gt;/             
 * 
 * Request DeviceInfo of all devices:
 * o HTTP GET: &lt;baseURL&gt;/DeviceInfo   
 * 
 * Request DeviceInfo of specific device:
 * o HTTP GET: &lt;baseURL&gt;/DeviceInfo?DeviceId=&lt;DeviceID&gt;
 * 
 * Request DeviceStatus of all devices:
 * o HTTP GET: &lt;baseURL&gt;/DeviceStatus
 *
 * Request DeviceStatus of specific device:
 * o HTTP GET: &lt;baseURL&gt;/DeviceStatus?DeviceId=&lt;DeviceID&gt;
 * 
 * Request PlanningRequest of all devices:
 *  o HTTP GET: &lt;baseURL&gt;/PlanningRequest
 * 
 * Request PlanningRequest of specific device:
 * o HTTP GET: &lt;baseURL&gt;/PlanningRequest?DeviceId=&lt;DeviceID&gt;
 * 
 * @author Markus Sandholz
 */
public class SEMPHandler implements HttpHandler {
    
    // class variables
    private static final Logger log = LogManager.getLogger("SEMP"); 

    /**
    * Handle to respond the appropriate SEMP message by requested url parameters.
    * 
    * @param httpExchange http exchange handle
    * @throws IOException IOException
    */
    @Override 
    public void handle(HttpExchange httpExchange) throws IOException {
    
        log.debug("HTTP "+httpExchange.getRequestMethod()+": "+ httpExchange.getRequestURI()+ " from: "+httpExchange.getRemoteAddress().getHostName()+" ["+httpExchange.getRemoteAddress().getAddress().getHostAddress()+"]");

        SEMPGateway sg = SEMPGateway.getInstance();  

        if(httpExchange.getRequestMethod().equals("GET")) {   

            String requestURI = httpExchange.getRequestURI().toString();
            String refURI;
            String deviceID;
            String response = "";

            //log.debug("HTTP GET request: "+ httpExchange.getRequestURI()+ " from: "+httpExchange.getRemoteAddress().getHostName()+" ["+httpExchange.getRemoteAddress().getAddress().getHostAddress()+"]");    

            // check, if DeviceID is requested
            int i = requestURI.lastIndexOf("?DeviceId=");
            if(i > 0) {
                deviceID = requestURI.substring(i+10, requestURI.length());
            } else {    
                deviceID = "all";
            }       

            // Request all information (DeviceInfo, DeviceStatus, PlanningRequests of all devices): HTTP GET: <baseURL>/
            refURI = Param.HTTP_BASE_URL.getValue()+"/";
            if(requestURI.equals(refURI)) {     
                response = sg.getMessage(MessagePartRefType.all, "all");
            }

            // Request DeviceInfo of all devices: HTTP GET: <baseURL>/DeviceInfo
            // Request DeviceInfo of specific device: HTTP GET: <baseURL>/DeviceInfo?DeviceId=<DeviceID>
            refURI = Param.HTTP_BASE_URL.getValue()+"/DeviceInfo";
            if(requestURI.startsWith(refURI)) {
                response = sg.getMessage(MessagePartRefType.DeviceInfo, deviceID);
            }       

            // Request DeviceStatus of all devices: HTTP GET: <baseURL>/DeviceStatus
            // Request DeviceStatus of specific device:  HTTP GET: <baseURL>/DeviceStatus?DeviceId=<DeviceID>
            refURI = Param.HTTP_BASE_URL.getValue()+"/DeviceStatus";
            if(requestURI.startsWith(refURI)) {
                response = sg.getMessage(MessagePartRefType.DeviceStatus, deviceID);
            }

            // Request PlanningRequest of all devices: HTTP GET: <baseURL>/PlanningRequest
            // Request PlanningRequest of specific device: HTTP GET: <baseURL>/PlanningRequest?DeviceId=<DeviceID>
            refURI = Param.HTTP_BASE_URL.getValue()+"/PlanningRequest";
            if(requestURI.startsWith(refURI)) {
                response = sg.getMessage(MessagePartRefType.PlanningRequest, "all");
            }

            //log.info("HTTP Request to "+httpExchange.getRequestURI()+" from " + httpExchange.getRemoteAddress().getAddress().getHostAddress());

            httpExchange.getResponseHeaders().add( "Connection", "close" );
            httpExchange.getResponseHeaders().add( "Content-Type", "application/xml" );       
            httpExchange.sendResponseHeaders( 200, response.length() );

            try (OutputStream os = httpExchange.getResponseBody()) {
                os.write(response.getBytes());
            }

        } else {

            /*
            Data transmissions
                Data transmissions (e.g. used for recommendations) from an EM to a Gateway:
                o HTTP POST: <baseURL>/
            */

            BufferedReader br = null;
            StringBuilder sb = new StringBuilder();
            InputStream is=httpExchange.getRequestBody();
            String line;

            try {
                br = new BufferedReader(new InputStreamReader (is));

                while((line = br.readLine()) != null){
                    sb.append(line);
                }

                httpExchange.sendResponseHeaders(200,0);
                log.debug(sb.toString());
                sg.parseEM2Device(sb.toString());

            } catch (IOException e) {
                log.fatal(e.getMessage());

            } finally {
                if (br != null) {
                    try {
                            br.close();
                    } catch (IOException e) {

                        log.fatal(e.getMessage());
                    }
                }
            }
        }
    }  
}
