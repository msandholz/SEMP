
import de.sandholz.semp.core.Param;
import de.sandholz.semp.core.SEMPGateway;
import de.sandholz.semp.gpio.ControlUnit;
import de.sandholz.semp.gpio.ControlUnitRefType;
import de.sandholz.semp.http.Device;
import de.sandholz.semp.http.DeviceListener;
import de.sandholz.semp.http.DeviceModeRefType;
import de.sandholz.semp.http.DeviceStatusRefType;
import de.sandholz.semp.http.DeviceTypeRefType;
import de.sandholz.semp.http.TimeFrame;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 *
 * @author Markus Sandholz
 */
public class Test {

    // class variable
    private static final Logger log = Logger.getLogger("SEMP");
    
    public static void main(String[] args) {

        // SEMP Gateway instantiieren
        SEMPGateway sempGateway = SEMPGateway.getInstance();

        // Planing Request definieren inkl. Startzeit, Endezeit, Minimal- und Maximal-Laufzeit in Minuten.
        TimeFrame tf = new TimeFrame("10:00", "12:00", 10, 15);
        
        // Device A definieren inkl. Device-Name und -Typ, der maximale Stromverbrauch in Watt und dem Planning-Request.
        Device deviceA = new Device("DeviceA", DeviceTypeRefType.HeatPump, 100, false, false, tf);
        
//        Control sc = new Control(deviceA,1);
        ControlUnit controlUnitA = new ControlUnit(ControlUnitRefType.A, deviceA);
        
        // Für Device A einen Listener anlegen, um bei einer Switch-Recommendation durch den Sunny Home Manager informiert zu werden.
        deviceA.addListener(new DeviceListener(){
           
            @Override
            public void handleEMChangeEvent(Device sempDevice){
               
                log.info("1. Event Handler["+sempDevice.getDeviceIdentification().getDeviceName()
                    + "] | Mode["+sempDevice.getMode().toString()
                    + "] | Status["+sempDevice.deviceStatus.getStatus()
                    + "] | RemainingTime["+sempDevice.isRemainingTime()
                    + "] | SwitchRecommendation["+sempDevice.getSwitchRecommendation()+"]");
                
                if(sempDevice.getMode().equals(DeviceModeRefType.On)){
                    controlUnitA.setLED();
                }
                
                if(sempDevice.getMode().equals(DeviceModeRefType.Off)){
                    controlUnitA.setLED();
                }
                
                if(sempDevice.getMode().equals(DeviceModeRefType.Auto)){
                                        
                    if(sempDevice.getSwitchRecommendation() && sempDevice.isRemainingTime()){
                        sempDevice.setStatus(DeviceStatusRefType.On);
                    }
                    
                    if(sempDevice.getSwitchRecommendation() && !sempDevice.isRemainingTime()){
                        sempDevice.setStatus(DeviceStatusRefType.Off);
                    }
                    
                    controlUnitA.setLED();                    
                }                
                
                log.info("2. Event Handler["+sempDevice.getDeviceIdentification().getDeviceName()
                    + "] | Mode["+sempDevice.getMode().toString()
                    + "] | Status["+sempDevice.deviceStatus.getStatus()
                    + "] | RemainingTime["+sempDevice.isRemainingTime()
                    + "] | SwitchRecommendation["+sempDevice.getSwitchRecommendation()+"]");
            }
        });   
         
        // Device A zum SEMP-Gateway zuweisen und SEMP-Gateway starten.
        sempGateway.addDevice(deviceA);      
        sempGateway.start();  
        
        // Stromverbrauch simulieren
        try {
            while (System.in.available() == 0) {
               Thread.sleep(500);

              
               //log.info("Voltage ACS712:  " + controlUnitA.getAveragePower());
               
               deviceA.deviceStatus.setAveragePower(controlUnitA.getAveragePower());
               
               
//               if(deviceA.deviceStatus.getStatus().equals(DeviceStatusRefType.On)) {
//                   deviceA.deviceStatus.setAveragePower(100);
//               }
//               
//                if(deviceA.deviceStatus.getStatus().equals(DeviceStatusRefType.Off)) {
//                   deviceA.deviceStatus.setAveragePower(0);
//               }
            }
        } catch (Exception e){System.out.println("x"+e.getMessage());} 
        
        sempGateway.stop();
    }
}
