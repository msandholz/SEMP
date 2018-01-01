/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sandholz.semp.http;

/**
 * Information on the characteristics of the device. 
 * 
 * @author Markus Sandholz
 */
public class DeviceCharacteristics {

    // instance variables
    private int maxPowerConsumption = 0; // Nominal maximum power consumption of the device in Watts. If the device is controllable with regard to power consumption, the recommendation of the energy management system will never exceed this value. 
    private int minOnTime = -1; // If the device is switched on, it has to remain in this status for at least MinOnTime seconds. 
    private int minOffTime = -1; // If the device is switched off, it has to remain in this status for at least MinOffTime seconds. 

    // constructors
    /**
     * Information on the characteristics of the device. 
     * @param maxPowerConsumption Nominal maximum power consumption of the device in Watts. If the device is controllable with regard to power consumption, the recommendation of the energy management system will never exceed this value. 
     */
    public DeviceCharacteristics(int maxPowerConsumption) {
        this.maxPowerConsumption = maxPowerConsumption;
    }
    
    /**
     * Information on the characteristics of the device. 
     * @param maxPowerConsumption Nominal maximum power consumption of the device in Watts. If the device is controllable with regard to power consumption, the recommendation of the energy management system will never exceed this value. 
     * @param minOnTime If the device is switched on, it has to remain in this status for at least MinOnTime seconds.
     * @param minOffTime If the device is switched off, it has to remain in this status for at least MinOffTime seconds.
     */
    public DeviceCharacteristics(int maxPowerConsumption, int minOnTime, int minOffTime) {
        this.maxPowerConsumption = maxPowerConsumption;
        this.minOnTime = minOnTime;
        this.minOffTime = minOffTime;
    }

    // methods
    /**
     * Builds the xml-structure of the device characteristics part.
     * @return xml-structure as string.
     */
    public String getMessage() {

        StringBuilder sb = new StringBuilder();

        sb.append("  <Characteristics>\r\n");
        sb.append("    <MaxPowerConsumption>").append(String.valueOf(this.maxPowerConsumption)).append("</MaxPowerConsumption>\r\n");
        if(this.minOnTime > -1) sb.append("    <MinOnTime>").append(String.valueOf(this.minOnTime)).append("</MinOnTime>\r\n");
        if(this.minOffTime > -1) sb.append("    <MinOffTime>").append(String.valueOf(this.minOffTime)).append("</MinOffTime>\r\n");
        sb.append("  </Characteristics>\r\n");

        return sb.toString();
    }
}
