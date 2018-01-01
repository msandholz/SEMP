/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sandholz.semp.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import de.sandholz.semp.core.Param;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Desktop
 */
public class DeviceCSSHandler implements HttpHandler {
    
    // class variables
    private static final Logger log = LogManager.getLogger("SEMP");
      
    
    // methods
    /**
     * Handle to respond the status web site of the SEMP getway.
     * 
     * @param httpExchange http exchange handle
     * @throws IOException IOExeption.
     */
    @Override 
    public void handle(HttpExchange httpExchange) throws IOException
    {
        String response = "";
              
        log.trace("HTTP "+httpExchange.getRequestMethod()+": "+ httpExchange.getRequestURI()+ " from: "+httpExchange.getRemoteAddress().getHostName()+" ["+httpExchange.getRemoteAddress().getAddress().getHostAddress()+"]");
    
        if(httpExchange.getRequestMethod().equals("GET")) {  
            
            response = this.readCSSFile();

            httpExchange.getResponseHeaders().add("Content-type", "text/css");  
            httpExchange.getResponseHeaders().add("Cache-Control", "max-age=43200");
            httpExchange.sendResponseHeaders(200, response.length());

            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
    
    private String readCSSFile(){
        
        String pathOutsideJAR = Param.PATH_OUTSIDE_JAR+Param.WEBSITE_CSS.getValue();
        StringBuilder cssString = new StringBuilder();        

        try {

            File file = new File(pathOutsideJAR);
            InputStream inputStream;
            
            if(file.exists()){   
                inputStream = new FileInputStream(pathOutsideJAR);
                
            } else {  
                inputStream = Param.class.getResourceAsStream(Param.PATH_INSIDE_JAR+"semp.css");
            }    

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream), 1);
                
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                       cssString.append(line);
                }
                
                inputStream.close();
            
        } catch (Exception e) {
                log.error("Unable to load css-file from: " + System.getProperty("user.dir")+ Param.WEBSITE_CSS.getValue(), e);
                //System.exit(1);
        }

        return cssString.toString(); 
    }  
}