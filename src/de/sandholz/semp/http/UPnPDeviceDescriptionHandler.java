/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sandholz.semp.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import de.sandholz.semp.core.Param;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.logging.log4j.*;

/**
 * A Device-Description is an XML document described by the UPnP spec. 
 * Note that the semp:X_SEMPSERVICE element is not described by UPnP. 
 * It is a SEMP specific extension that describes the SEMP web service implemented by a gateway.
 *  
 * @author Markus Sandholz
 *
 */
public class UPnPDeviceDescriptionHandler implements HttpHandler{

    // class variables
    private static final Logger log = LogManager.getLogger("SEMP");  
    private final static String SEMP_TRANSPORT = "HTTP/Pull"; // specifies the communication mechanism. It must be set to „HTTP/Pull“. 
    private final static String SEMP_EXCHANGE_FORMAT = "XML"; // specifies the serialization mechanism. It must be set to „XML“.
    private final static String SEMP_WS_VERSION = "1.1.0"; // specifies the version of the web service and its data-structures. Use the value specified in the SEMP XSD file. The format is “<major>.<minor>.<release>”, e.g. “1.1.0”.
	
    // methods
    /**
     * Handle to send the XML-description of the UPnP Device.
     * 
     * @param httpExchange http Exchange handle.
     * @throws IOException IOException
     */
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
                 
        String response = getMessage();
               
        httpExchange.getResponseHeaders().add( "Connection", "close" );
        httpExchange.getResponseHeaders().add( "Content-Type", "application/xml" );       
        httpExchange.sendResponseHeaders( 200, response.length() );

        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(response.getBytes());
        }
        
        log.trace("HTTP Request to "+httpExchange.getRequestURI()+" from "+httpExchange.getRemoteAddress().getHostName()+" ["+httpExchange.getRemoteAddress().getAddress().getHostAddress()+"]\n"+response);
     }

	
    /**
     * This method builds the xml-structured message that describes the UPnP-device.  Further more, it 
     * contains a SEMP specific extension that describes the SEMP web service implemented by a gateway.
     * 
     * @return UPnP Device description (e.g. description.xml) as string.
     */
    private static String getMessage() {

        StringBuilder sb = new StringBuilder();

        sb.append("<?xml version=\"1.0\"?>\r\n");
        sb.append("<root xmlns=\"urn:schemas-upnp-org:device-1-0\">\r\n");
        sb.append("  <specVersion><major>1</major><minor>0</minor></specVersion>\r\n");

        sb.append("  <device>\r\n");	
        sb.append("    <deviceType>").append(Param.SSDP_NT_DEVICE_TYPE.getValue()).append("</deviceType>\r\n");
        sb.append("    <friendlyName>").append(Param.SEMP_GW_FRIENDLY_NAME.getValue()).append("</friendlyName>\r\n");
        sb.append("    <manufacturer>").append(Param.SEMP_GW_MANUFACTORER.getValue()).append("</manufacturer>\r\n");
        /*
         * Optional Fields. Currently not implemented!
         */
        //<manufacturerURL>http://www.somecompany.xy</manufacturerURL>
        //<modelDescription>SEMP Gateway Demo</modelDescription>

        sb.append("    <modelName>").append("SEMP GWD").append("</modelName>\r\n"); // Ändern???

        /*
         * Optional Fields. Currently not implemented!
         */
        //<modelNumber>1.0.0</modelNumber>
        //<modelURL>http://www.somecompany.xy/modelZ</modelURL>
        //<serialNumber>53-4D-41-53-4D-41</serialNumber>

        sb.append("    <UDN>uuid:").append(Param.SSDP_UUID.getValue()).append("</UDN>\r\n");

        /*
         * The service list (element “serviceList”) contains all UPnP services the device implements. 
         * UPnP services are based on a special UPnP specific RPC-mechanism.
         * 
         * The SEMP web service does not use the UPnP RPC mechanism. As a result it is not listed 
         * in the service list. No UPnP service has to be implemented for the SEMP gateway device type.
         * 
         * According to the UPnP 1.0 spec the “serviceList” element can be empty or omitted. However 
         * some Control-Points do not comply with this, e.g. Windows XP is not able to handle devices 
         * without services correctly. As a result a NULL-service (a dummy service) should be specified.
         */		
        sb.append("    <serviceList>\r\n");
        sb.append("      <service>\r\n");
        sb.append("        <serviceType>urn:schemas-simple-energy-management-protocol:service:NULL:1:service:NULL:1</serviceType>\r\n");
        sb.append("        <serviceId>urn:schemas-simple-energy-management-protocol:serviceId:NULL:serviceId:NULL</serviceId>\r\n");
        sb.append("        <SCPDURL>/XD/NULL.xml</SCPDURL>\r\n");
        sb.append("        <controlURL>/UD/?0</controlURL>\r\n");
        sb.append("        <eventSubURL></eventSubURL>\r\n");
        sb.append("      </service>\r\n");
        sb.append("    </serviceList>\r\n");
        sb.append("    <presentationURL>index.html</presentationURL>\r\n"); // Ändern				

        /*
         * The element “semp:X_SEMPSERVICE” is an extension of the UPnP Device-Description specific to SEMP. 
         * It contains information on how to establish communication via the SEMP web service that must be 
         * implemented by a SEMP gateway. The element must be provided by all devices of the type 
         * “schemas-simple-energy-management-protocol:device:Gateway”.
         */
        sb.append("    <semp:X_SEMPSERVICE xmlns:semp=\"urn:schemas-simple-energy-management-protocol:service-1-0\">\r\n");
        sb.append("        <semp:server>http://").append(Param.HTTP_ADDRESS.getValue()).append(":").append(Param.HTTP_PORT.getValue()).append("</semp:server>\r\n"); // contains the address and port of the HTTP server that implements the SEMP web service.
        sb.append("        <semp:basePath>").append(Param.HTTP_BASE_URL.getValue()).append("</semp:basePath>\r\n"); // contains the prefix for SEMP request URLs.
        sb.append("        <semp:transport>").append(SEMP_TRANSPORT).append("</semp:transport>\r\n"); 
        sb.append("        <semp:exchangeFormat>").append(SEMP_EXCHANGE_FORMAT).append("</semp:exchangeFormat>\r\n"); 
        sb.append("        <semp:wsVersion>").append(SEMP_WS_VERSION).append("</semp:wsVersion>\r\n"); 
        sb.append("    </semp:X_SEMPSERVICE>\r\n");

        sb.append("  </device>\r\n");
        sb.append("</root>");

        return sb.toString();
    }
}