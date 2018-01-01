/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sandholz.semp.gpio;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.trigger.GpioCallbackTrigger;
import com.pi4j.io.spi.*;
import de.sandholz.semp.http.Device;
import de.sandholz.semp.http.DeviceModeRefType;
import de.sandholz.semp.http.DeviceStatusRefType;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Markus Sandholz
 */
public class ControlUnit {
    
    // class variables
    private static final Logger log = Logger.getLogger("SEMP");
    private static SpiDevice spi = null;
    private static GpioController gpio;
    private static final int INVALID_VALUE = -1;
    private static byte INI_CMD = (byte)0xD0; //11010000
    private GpioPinDigitalInput CONTACT;    
    
    // Instance variables
    private short adcChannel = 1;    
    private GpioPinDigitalOutput LEDgreen;
    private GpioPinDigitalOutput LEDred;
    private GpioPinDigitalOutput RELAY;
    private GpioPinDigitalInput KEY;
    private Device sempDevice;
    private int offSet = 0;
    
    private int KEYmode = 0;
    
        
    public ControlUnit(ControlUnitRefType controlUnit, Device sempDevice) {
                
        try {
            this.sempDevice = sempDevice;
            spi = SpiFactory.getInstance(SpiChannel.CS0, SpiDevice.DEFAULT_SPI_SPEED, SpiMode.MODE_0);
            gpio = GpioFactory.getInstance();
            CONTACT = gpio.provisionDigitalInputPin(RaspiPin.GPIO_08, PinPullResistance.PULL_DOWN);
            
            switch (controlUnit){
                
                case A:
                    this.adcChannel = (short) ((0 + 8) << 4);
                    LEDgreen = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "LED green", PinState.LOW);
                    LEDred = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_28, "LED red", PinState.LOW);
                    KEY = gpio.provisionDigitalInputPin(RaspiPin.GPIO_21, PinPullResistance.PULL_UP);
                    RELAY = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_16, "Relay", PinState.LOW);
                    break;                        
                    
                case B:
                    this.adcChannel =(short) ((1 + 8) << 4);
                    LEDgreen = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "LED green", PinState.LOW);
                    LEDred = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_25, "LED red", PinState.LOW);
                    KEY = gpio.provisionDigitalInputPin(RaspiPin.GPIO_22, PinPullResistance.PULL_UP);
                    RELAY = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07, "Relay", PinState.LOW);
                    break;
                    
                case C:
                    this.adcChannel = (short) ((2 + 8) << 4);
                    LEDgreen = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04, "LED green", PinState.LOW);
                    LEDred = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_27, "LED red", PinState.LOW);
                    KEY = gpio.provisionDigitalInputPin(RaspiPin.GPIO_26, PinPullResistance.PULL_UP);
                    RELAY = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_15, "Relay", PinState.LOW);
                    break;
                    
                case D:
                    this.adcChannel = (short) ((3 + 8) << 4);
                    LEDgreen = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_03, "LED green", PinState.LOW);
                    LEDred = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_24, "LED red", PinState.LOW);
                    KEY = gpio.provisionDigitalInputPin(RaspiPin.GPIO_23, PinPullResistance.PULL_UP);
                    RELAY = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_09, "Relay", PinState.LOW);
                    break;
            }
            
            this.getOffSet();
            
            // Create a gpio callback trigger on gpio pin#4; when #4 changes state, perform a callback
            // invocation on the user defined 'Callable' class instance
            KEY.addTrigger(new GpioCallbackTrigger(new Callable<Void>() {
                public Void call() throws Exception {
                    
                    if(KEY.getState().isLow()){
                        
                        if(sempDevice.getMode().equals(DeviceModeRefType.Off)){KEYmode = 0;}
                        if(sempDevice.getMode().equals(DeviceModeRefType.On)){KEYmode = 2;}
                        if(sempDevice.getMode().equals(DeviceModeRefType.Auto)){KEYmode = 1;}
                        
                        KEYmode++;
                        if (KEYmode>2) {KEYmode=0;}
                        
                        switch(KEYmode){
                            case 0:
                                sempDevice.setMode(DeviceModeRefType.Off);
                                break;
                            case 1:
                                sempDevice.setMode(DeviceModeRefType.Auto);
                                break;
                            case 2:
                                sempDevice.setMode(DeviceModeRefType.On);
                                break;
                        }
                    }
                    return null;
                }
            }));
        } catch (IOException ex) {
            System.out.println("1"+ex.getMessage());
        }
    }
    
    public void setLED(){
        
        if(sempDevice.getMode().equals(DeviceModeRefType.Auto)){
            if(sempDevice.getDeviceStatus().equals(DeviceStatusRefType.Off)) {
                this.LEDgreen.low();
                this.LEDgreen.blink(0);
                this.LEDred.blink(500);
            } 
                    
            if(sempDevice.getDeviceStatus().equals(DeviceStatusRefType.On)) {
                this.LEDgreen.blink(500);
                this.LEDred.low();
                this.LEDred.blink(0);
            } 
        } else {            
            if(sempDevice.getMode().equals(DeviceModeRefType.On)){
                this.LEDgreen.high();
                this.LEDgreen.blink(0);
                this.LEDred.low();
                this.LEDred.blink(0);
            }
                
            if(sempDevice.getMode().equals(DeviceModeRefType.Off)){
                this.LEDgreen.low();
                this.LEDgreen.blink(0);
                this.LEDred.high();
                this.LEDred.blink(0);
            }
        }     
        
        
    }
    
    public void switchOn(){
        this.RELAY.high();
    }
    
    public void switchOff(){
        this.RELAY.low();
    }
    
    public int read(){
        long start = System.currentTimeMillis();
        long stop = start;
        int i = 0;
        double RMS = 0;
        int offset = 512;
        int result = readAnalogValue();
        
        double scaleFactor = 3.3/1024;
        
        while((stop-start)<20) {
            i++;
            RMS = RMS + Math.pow((readAnalogValue()-offset)*scaleFactor,2);
            stop = System.currentTimeMillis();
        }

	result = (int) Math.sqrt(RMS/i);   
        //System.out.println(i + " Result: "+result);
        return result;
    }
    
    public int readVoltage(){
        double R1 = 1.8;
        double R2 = 3.3;        
        double factor1 = 5*R2/(R1+R2);
        
        int result = (int) (readAnalogValue()*factor1);
        return result;
    }
    
    public int getAveragePower(){
        long start = System.currentTimeMillis();
        long stop = start;
        
        double factor = 0.0498046875;        
        
        int result = 0;
        int i = 0;      
        double RMS = 0;
        
        readAnalogValue();
               
        while((stop-start)<60) {
            i++;
            result = readAnalogValue()-this.offSet;
            if(result <5 && result >-5) result = 0;
            
            RMS = RMS + Math.pow((result*factor),2);
            
            //RMS = RMS + Math.pow(result,2);
            stop = System.currentTimeMillis();
        }

        result = (int) (Math.sqrt(RMS/i)*1000);
        
        if(i<300) { log.warning("Too less currency samples ! ("+String.valueOf(i)+")");  }
        log.info("Strom: "+result);
        return result*230/1000;        
    }
    
    /*
    * Gather, calculate and set OffSet Value.
    */
    private void getOffSet() {
        long start = System.currentTimeMillis();
        long stop = start;
        int i = 1;
        int result = readAnalogValue();
        
        this.switchOff();
        
        while((stop-start)<20) {
            i++;
            result = result + readAnalogValue();
            stop = System.currentTimeMillis();
        }
        
        this.offSet = result/i;
        log.finer("Set Offset to: "+this.offSet);
    }
       
    /*
    * Read the analog value of the ADC.
    */
    private int readAnalogValue() {               
        short[] data = new short[] { 1, this.adcChannel, 0 };
        short[] result;
        
        try {
            result = spi.write(data);
        } catch (IOException e) {
            return INVALID_VALUE;
        }
        int analogValue = ((result[1] & 3) << 8) + result[2];
        
        return analogValue;
    }
}
