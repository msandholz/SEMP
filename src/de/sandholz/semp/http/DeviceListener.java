/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sandholz.semp.http;

/**
 * Interface for the listeners of the SEMP Devices.
 * 
 * @author Markus Sandholz
 */
public interface DeviceListener {
    /**
     * Call back function.
     * @param sempDevice Device with all relatind data.
     */
    public void handleEMChangeEvent(Device sempDevice);
}