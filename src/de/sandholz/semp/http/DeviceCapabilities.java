/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sandholz.semp.http;

/**
 * This class encapsulates information about the capabilities of the device. 
 * 
 * @author Markus Sandholz
 */
public class DeviceCapabilities {

    // instance variables
    private PowerMeasurementRefType powerMeasurementType = PowerMeasurementRefType.None; // Capability of the device with regard to deriving information about its current power consumption, e.g. measurement or estimation 
    private boolean absoluteTimeStamp = false; // Bool that indicates if the device is able to deal with absolute timestamps or only with relative timestamps 
    private boolean interruptsAllowed = false; // Specifies if a run of the device can be interrupted or not. 
    private boolean optionalEnergy = false; // Specifies options related to planning requests. 

    // constructor	
    /**
     * This object encapsulates information about the capabilities of the device.
     * @param powerMeasurementType Capability of the device with regard to deriving information about its current power consumption, e.g. measurement or estimation.
     * @param absoluteTimeStamp Bool that indicates if the device is able to deal with absolute timestamps or only with relative timestamps .
     * @param interruptsAllowed Specifies if a run of the device can be interrupted or not. 
     * @param optionalEnergy Specifies options related to planning requests. 
     */
    public DeviceCapabilities(PowerMeasurementRefType powerMeasurementType, boolean absoluteTimeStamp, boolean interruptsAllowed, boolean optionalEnergy ) {
        this.powerMeasurementType = powerMeasurementType;
        this.absoluteTimeStamp = absoluteTimeStamp;
        this.interruptsAllowed = interruptsAllowed;
        this.optionalEnergy = optionalEnergy;
    }

    // methods
    /**
     * Builds the xml-structure of the device capabilities part.
     * @return xml-structure as string.
     */
    public String getMessage() {

        StringBuilder sb = new StringBuilder();

        sb.append("  <Capabilities>\r\n");
        sb.append("    <CurrentPower><Method>").append(this.powerMeasurementType).append("</Method></CurrentPower>\r\n");
        sb.append("    <Timestamps><AbsoluteTimestamps>").append(this.absoluteTimeStamp).append("</AbsoluteTimestamps></Timestamps>\r\n");
        sb.append("    <Interruptions><InterruptionsAllowed>").append(this.interruptsAllowed).append("</InterruptionsAllowed></Interruptions>\r\n");
        sb.append("    <Requests><OptionalEnergy>").append(this.optionalEnergy).append("</OptionalEnergy></Requests>\r\n");
        sb.append("  </Capabilities>\r\n");

        return sb.toString();
    }
}
