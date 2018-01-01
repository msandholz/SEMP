/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sandholz.semp.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import de.sandholz.semp.core.Param;
import de.sandholz.semp.core.SEMPGateway;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.apache.logging.log4j.*;


/**
 * Data requests performed by device status web page.
 * 
 * @author Markus Sandholz
 */
public class DeviceURLHandler implements HttpHandler {

    // class variables
    private static final Logger log = LogManager.getLogger("SEMP");
        
    SEMPGateway sg = SEMPGateway.getInstance();  
    
    // instance variable
    private String deviceURL;
    
    
    // methods
    /**
     * Handle to respond the status web site of the SEMP getway.
     * 
     * @param httpExchange http exchange handle
     * @throws IOException IOException
     */
    @Override 
    public void handle(HttpExchange httpExchange) throws IOException
    {
        String response = "";
        this.deviceURL = "http://"+ Param.HTTP_ADDRESS.getValue() +":"+Param.HTTP_PORT.getValue()+ Param.WEBSITE_URL.getValue(); 
                
        log.trace("HTTP "+httpExchange.getRequestMethod()+": "+ httpExchange.getRequestURI()+ " from: "+httpExchange.getRemoteAddress().getHostName()+" ["+httpExchange.getRemoteAddress().getAddress().getHostAddress()+"]");
    
        if(httpExchange.getRequestMethod().equals("GET")) {   
                 
            String requestURI = httpExchange.getRequestURI().toString();              
            String[] parts = requestURI.split("/");
                           
            if (parts.length>2){
                
                List<Device> deviceList = sg.getDeviceList();
                for(Device device: deviceList){
                
                    if(device.getDeviceID().equals(parts[2])){
                        if(parts[3].equals("on")) device.setMode(DeviceModeRefType.On);
                        if(parts[3].equals("off")) device.setMode(DeviceModeRefType.Off);
                        if(parts[3].equals("auto")) device.setMode(DeviceModeRefType.Auto);
                    }
                }                
            }

            httpExchange.getResponseHeaders().add( "Content-type", "text/html" );
                        
            response = "<html><head>"+getHTMLHead()+"</head><body>"+getHTMLBody()+"</body></html>";
            
            httpExchange.sendResponseHeaders(200, response.length());

            OutputStream os = httpExchange.getResponseBody();
            os.write( response.getBytes() );
            os.close();  
        }
    }

    
    /**
     * Build the header of the html page. 
     * 
     * @return Header part of html page as string.
     */
    private String getHTMLHead(){
        StringBuilder sbh = new StringBuilder();
        
        sbh.append("<title>"+Param.SEMP_GW_FRIENDLY_NAME.getValue()+"</title>\r\n");
        sbh.append("<meta http-equiv='Content-Type' content='text/html;charset=ISO-8859-1'>");
        sbh.append("<link rel='icon' type='image/png' href='/status/image/"+Param.WEBSITE_FAVICON.getValue()+"'>");
        //sbh.append("<link rel='icon' href='data:;base64,iVBORw0KGgo='>");
        sbh.append("<link rel='stylesheet' href='/status/css/"+Param.WEBSITE_CSS.getValue()+"'>\r\n");
	sbh.append("<script type='text/JavaScript'>\r\n");
        
        if(Param.WEBSITE_MODE.getValue().equals("expert")){
            sbh.append("function copyToClipBoard() {var urlField = document.querySelector('#log'); ");
            sbh.append("urlField.select(); document.execCommand('copy');}");
        }
        
        int timerValue = Integer.valueOf(Param.WEBSITE_REFRESH.getValue())*1000;
        
        if(timerValue > 0) {
            String url = "http://"+Param.HTTP_ADDRESS.getValue()+":"+Param.HTTP_PORT.getValue()+Param.WEBSITE_URL.getValue();
            
            sbh.append("function timer() {setTimeout(function(){ var url = window.location.href; window.location.assign(url.substring(0,url.indexOf('"+Param.WEBSITE_URL.getValue()+"'))+'"+Param.WEBSITE_URL.getValue()+"'); }, "+timerValue+");}\r\n");
            sbh.append("window.onload = timer;");
        } 
        sbh.append("</script>\r\n");
        
        
       return sbh.toString();        
    }
	
    /**
     * Build the body of the html page. 
     * 
     * @return Body part of html page as string.
     */
    private String getHTMLBody(){
        
        StringBuilder sbb = new StringBuilder();
        
        sbb.append("<div style='float:left;'><h2>"+Param.SEMP_GW_FRIENDLY_NAME.getValue()+"</h2><h3>by "+Param.SEMP_GW_MANUFACTORER.getValue()+"</h3></div>\r\n");
        sbb.append("<div class='logo' style='float:right;'><img src='/status/image/"+Param.WEBSITE_LOGO.getValue()+"'></div></div><div style='clear: both;'/>");
        sbb.append("<table><thead><tr><th>Verbraucher</th><th style='width:300px'>Betriebsmodus</th><th style='width:180px'>Status</th>");
        sbb.append("<th style='width:160px'><a href='https://www.sunnyportal.com/Homan/ConsumerBalance' target='_blank'>Aktueller Verbrauch</a></th>");
        sbb.append("<th><a href='https://www.sunnyportal.com/FixedPages/HoManLive.aspx' target='_blank'>Verbraucherplanung</a></th></tr></thead><tbody>");
        
        
        List<Device> deviceList = sg.getDeviceList();
            
        for(Device device: deviceList){
            
            String btnOn ="";
            String btnOff ="";
            String btnAuto ="";
            String statusText ="";
            
            if(device.deviceStatus.isEMSignalsAccepted()){
                
                btnAuto = "bAuto ";
                
                if(device.getDeviceStatus().equals(DeviceStatusRefType.On)){
                    statusText = "dot-on blink'></div>Eingeschaltet (Automatik)</div>";
                } else {
                    statusText = "dot-off blink'></div>Ausgeschaltet (Automatik)</div>";
                }
                
            } else {
                
                if (device.getMode().equals(DeviceModeRefType.On)){
                    btnOn = "bOn ";
                    statusText = "dot-on'></div>Eingeschaltet</div>";
                } else {

                    btnOff = "bOff ";
                    statusText = "dot-off'></div>Ausgeschaltet</div>";
                }  
            }
                            
            sbb.append("<tr><td>").append(device.deviceIdentification.getDeviceName()).append("</td><td>");            
           
            if(Param.WEBSITE_MODE.getValue().equals("fullaccess") || Param.WEBSITE_MODE.getValue().equals("expert")){
                
                btnOn = btnOn+ "hOn' href='"+Param.WEBSITE_URL.getValue()+"/"+device.getDeviceID()+"/on";
                btnOff = btnOff+ "hOff' href='"+Param.WEBSITE_URL.getValue()+"/"+device.getDeviceID()+"/off";
                btnAuto = btnAuto +"hAuto' href='"+Param.WEBSITE_URL.getValue()+"/"+device.getDeviceID()+"/auto"; 
            }
            
            sbb.append("<a class='btn "+btnOn+"'>Einschalten</a>");
            sbb.append("<a class='btn "+btnOff+"'>Ausschalten</a>");
            sbb.append("<a class='btn "+btnAuto+"'>Automatisch</a>");
          
            sbb.append("</td><td><div class='status'><div class='"+statusText+"<div style='clear: both;'/>");
            sbb.append("</td><td>").append(device.getAveragePower()).append(" W</td>");      
            sbb.append("</td><td class='planning'>").append(device.getEarliestStart()+" - "+device.getLatestEnd()+" Uhr");
            sbb.append("<br>[Min: ").append(String.valueOf(device.getMinRunningTime())).append(" Minuten | Max: ");
            sbb.append(String.valueOf(device.getMaxRunningTime())).append(" Minuten]</td></tr>\r\n");
        }    
        
        sbb.append("</tbody></table>");
        
        if(Param.WEBSITE_MODE.getValue().equals("expert")){
            sbb.append("<hr><table div class='container'><tr>");
            sbb.append("<td class='box'>Memory Utilization:<br><code>");
            int mb = 1024 * 1024;
            Runtime instance = Runtime.getRuntime();
            sbb.append("Max Memory: "+instance.maxMemory()/mb+" MByte<br>");
            sbb.append("Total Memory: "+instance.totalMemory()/mb+" MByte<br>");
            sbb.append("Used Memory: "+(instance.totalMemory()-instance.freeMemory())/mb+" MByte<br>");
            sbb.append("Free Memory: "+instance.freeMemory()/mb+ " MByte</code></td>");
            
            sbb.append("<td class='box'>Operating System:<br><code>");
            sbb.append("OS name: "+System.getProperty("os.name")+"<br>");
            sbb.append("OS version:"+System.getProperty("os.version")+"<br>");
            sbb.append("OS architecture: "+System.getProperty("os.arch")+"<br>");
            sbb.append("Started: "+sg.getStartTime()+"</code></td>");
             
            sbb.append("<td class='box'>Runtime Environment:<br><code>");
            sbb.append("JRE vendor: "+System.getProperty("java.vendor")+"<br>");
            sbb.append("JRE version: "+System.getProperty("java.version")+"<br>");
            sbb.append("JRE dir: "+System.getProperty("java.home")+"</code></td>");
            
            sbb.append("<td class='box'>Network:<br><code>");
            try {sbb.append("Hostname: "+InetAddress.getLocalHost().getHostName()+"<br>");} 
            catch (Exception e) {sbb.append("Hostname: unknown<br>");}          
            sbb.append("IP Address: "+Param.HTTP_ADDRESS.getValue()+":"+Param.HTTP_PORT.getValue()+"<br>");
            sbb.append("IP Multicast: "+Param.SSDP_MULTICAST_ADDRESS.getValue()+"</code></td>");
            sbb.append("</tr></table>");             
            
            sbb.append("</div><textarea id='log' wrap='off' readonly>");
            File file = new File(System.getProperty("user.dir")+"/log.txt.0"); // /logs/rolling.log
            if(file.exists()){
                try {
                    BufferedReader in = new BufferedReader (new InputStreamReader (new ReverseLineInputStream(file)));
                    for(int a=0; a<40; a++) {
                        String line = in.readLine();
                        if (line == null) {
                            break;
                        }
                        sbb.append(escapeHTML(line)).append("&#10;");
                        
                    }
                    in.close();
                } catch (Exception e) {
                    log.fatal(e.getMessage());
                }
            } else {
                sbb.append("Logfile don't exist!");
            }
                     
            sbb.append("</textarea><br><br><a class='btn hAuto' href='#' onClick='copyToClipBoard()'>Copy to Clipboard</a>&nbsp;");
            sbb.append("<a class='btn hAuto' href='"+Param.WEBSITE_URL.getValue()+"/log' download>Download Logfile</a>");
        }
        
        DateFormat dfmt = new SimpleDateFormat( "dd.MM.yyyy HH:mm:ss" );  
        sbb.append("<hr><div class='footer'>").append(dfmt.format(new Date())).append(" - (c) 2016 by Markus Sandholz</div>");
        
        return sbb.toString();
    }
    
    private static final String escapeHTML(String s){
	StringBuffer sb = new StringBuffer();
	int n = s.length();
	for (int i = 0; i < n; i++) {
	    char c = s.charAt(i);
	    switch (c) {
	        case '<': sb.append("&lt;"); break;
	        case '>': sb.append("&gt;"); break;
	        case '&': sb.append("&amp;"); break;
	        case '"': sb.append("&quot;"); break;
	        case 'à': sb.append("&agrave;");break;
	        case 'À': sb.append("&Agrave;");break;
	        case 'â': sb.append("&acirc;");break;
	        case 'Â': sb.append("&Acirc;");break;
	        case 'ä': sb.append("&auml;");break;
	        case 'Ä': sb.append("&Auml;");break;
	        case 'å': sb.append("&aring;");break;
	        case 'Å': sb.append("&Aring;");break;
	        case 'æ': sb.append("&aelig;");break;
	        case 'Æ': sb.append("&AElig;");break;
	        case 'ç': sb.append("&ccedil;");break;
	        case 'Ç': sb.append("&Ccedil;");break;
	        case 'é': sb.append("&eacute;");break;
	        case 'É': sb.append("&Eacute;");break;
	        case 'è': sb.append("&egrave;");break;
	        case 'È': sb.append("&Egrave;");break;
	        case 'ê': sb.append("&ecirc;");break;
	        case 'Ê': sb.append("&Ecirc;");break;
	        case 'ë': sb.append("&euml;");break;
	        case 'Ë': sb.append("&Euml;");break;
	        case 'ï': sb.append("&iuml;");break;
	        case 'Ï': sb.append("&Iuml;");break;
	        case 'ô': sb.append("&ocirc;");break;
	        case 'Ô': sb.append("&Ocirc;");break;
	        case 'ö': sb.append("&ouml;");break;
	        case 'Ö': sb.append("&Ouml;");break;
	        case 'ø': sb.append("&oslash;");break;
	        case 'Ø': sb.append("&Oslash;");break;
	        case 'ß': sb.append("&szlig;");break;
	        case 'ù': sb.append("&ugrave;");break;
	        case 'Ù': sb.append("&Ugrave;");break;         
	        case 'û': sb.append("&ucirc;");break;         
	        case 'Û': sb.append("&Ucirc;");break;
	        case 'ü': sb.append("&uuml;");break;
	        case 'Ü': sb.append("&Uuml;");break;
	        case '®': sb.append("&reg;");break;         
	        case '©': sb.append("&copy;");break;   
	        case '€': sb.append("&euro;"); break;
	        // be carefull with this one (non-breaking whitee space)
	        case ' ': sb.append("&nbsp;");break;         
	         
	        default:  sb.append(c); break;
	    }
	}
	return sb.toString();
    }
}