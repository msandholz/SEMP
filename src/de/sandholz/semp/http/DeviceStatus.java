/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sandholz.semp.http;


/**
 * A DeviceStatus encapsulates the status information of a device, i.e. all measurements and properties representing the current status of the device
 * 
 * @author Markus Sandholz
 */
public class DeviceStatus {
	
    // instance variables
    private boolean EMSignalsAccepted = false; // Bool that indicates if the device is currently considering the control signals or recommendations provided by the energy manager or if it is in a mode which ignores the signals or recommendations. 
    private DeviceStatusRefType status = DeviceStatusRefType.Off;
    private int averagePower = 0;

    // constructor
    /**
     * A DeviceStatus encapsulates the status information of a device, i.e. all measurements and properties representing the current status of the device. 
     *
     * NOTE: ErrorCode and PowerConsumption are currently not implemented.
     */
    public DeviceStatus (){
        
    }


    // accessor methods
    public boolean isEMSignalsAccepted() {
        return EMSignalsAccepted;
    }

    public void setEMSignalsAccepted(boolean eMSignalsAccepted) {
        EMSignalsAccepted = eMSignalsAccepted;
    }

    public DeviceStatusRefType getStatus() {
        return status;
    }

    public void setStatus(DeviceStatusRefType status) {
        this.status = status;
    }

    public int getAveragePower(){
        return this.averagePower;
    }

    public void setAveragePower(int averagePower){
        this.averagePower = averagePower;
    }

    // methods
    /**
     * Builds the xml-structure of the device status part.
     * @param deviceID ID of the device.
     * @return xml-structure as string.
     */
    public String getMessage(String deviceID) {

        StringBuilder sb = new StringBuilder();

        sb.append("<DeviceStatus>\r\n");
        sb.append("  <DeviceId>").append(deviceID).append("</DeviceId>\r\n");
        sb.append("  <EMSignalsAccepted>").append(this.EMSignalsAccepted).append("</EMSignalsAccepted>\r\n");
        sb.append("  <Status>").append(this.status).append("</Status>\r\n");               
        sb.append("  <PowerConsumption>\r\n");
        sb.append("    <PowerInfo>\r\n");
        sb.append("      <AveragePower>").append(averagePower).append("</AveragePower>\r\n");
        sb.append("      <Timestamp>0</Timestamp>\r\n");
        sb.append("     <AveragingInterval>60</AveragingInterval>\r\n");
        sb.append("    </PowerInfo>\r\n");
        sb.append("  </PowerConsumption>\r\n");

        sb.append("</DeviceStatus>\r\n");

        return sb.toString();
    }
}