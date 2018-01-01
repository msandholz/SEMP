/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sandholz.semp.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import de.sandholz.semp.core.Param;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Desktop
 */
public class DeviceImageHandler implements HttpHandler {
    
    // class variables
    private static final Logger log = LogManager.getLogger("SEMP");
    private static final String PATH = "/resources/";
    
    // instance variable
    private String deviceURL;
    
       // methods
    /**
     * Handle to respond the status web site of the SEMP getway.
     * 
     * @param httpExchange http exchange handle
     * @throws IOException IOException.
     */
    @Override 
    public void handle(HttpExchange httpExchange) throws IOException
    {
        String response = "";
              
        log.info("HTTP "+httpExchange.getRequestMethod()+": "+ httpExchange.getRequestURI()+ " from: "+httpExchange.getRemoteAddress().getHostName()+" ["+httpExchange.getRemoteAddress().getAddress().getHostAddress()+"]");
    
        if(httpExchange.getRequestMethod().equals("GET")) {  
                
            String requestURI = httpExchange.getRequestURI().toString(); 
            String imagePath = requestURI.substring(requestURI.lastIndexOf("/") + 1);
            String pathOutsideJAR = Param.PATH_OUTSIDE_JAR+imagePath;
 
            BufferedImage image;
           
            try {

                File logo = new File(pathOutsideJAR);
                InputStream inputStream;
                       
                if(logo.exists()){   
                    image = ImageIO.read(logo);

                } else {  
                    inputStream = Param.class.getResourceAsStream(Param.PATH_INSIDE_JAR+imagePath);
                    image = ImageIO.read(inputStream);
                    inputStream.close();                   
                }   

                httpExchange.getResponseHeaders().add("Content-type", "image/png");
                httpExchange.getResponseHeaders().add("Cache-Control", "max-age=43200");
                httpExchange.sendResponseHeaders(200, 0);
                OutputStream os = httpExchange.getResponseBody();
                ImageIO.write(image, "png", os);
                
                
                os.close();
                
            } catch (Exception e) {
                log.error("Unable to load logo from: " + pathOutsideJAR, e);
                //System.exit(1);
            }
        }
    }
}
 