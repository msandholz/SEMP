/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sandholz.semp.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.logging.log4j.*;


/**
 * Class to catch unsupported ulr requests with an error message.
 * 
 * @author Markus Sandholz
 */
public class ErrorHandler implements HttpHandler {
    
    // class variables
    private static final Logger log = LogManager.getLogger("SEMP");
    
    /**
     * Handle to respond error messages.
     * 
     * @param httpExchange http exchange handle
     * @throws IOException IOException
     */
    @Override 
    public void handle( HttpExchange httpExchange ) throws IOException
    {
    
        String warning = "501 Not Implemented: the requested URL "+httpExchange.getRequestURI()+" is not supported! Client: "+httpExchange.getRemoteAddress().getHostName()+" ["+httpExchange.getRemoteAddress().getAddress().getHostAddress()+"]"; 
      
        log.debug(warning);
    
        httpExchange.getResponseHeaders().add( "Connection", "close" );
        httpExchange.getResponseHeaders().add( "Content-type", "text/html" );
        String response = "<hmtl>" + warning + "</html>";
        httpExchange.sendResponseHeaders( 501, response.length() );

        OutputStream os = httpExchange.getResponseBody();
        os.write( response.getBytes() );
        os.close();
  }
}
