/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sandholz.semp.http;

  
/**
 * Capability of the device with regard to deriving information about its current power consumption, e.g. measurement or estimation 
 * 
 * @author Markus Sandholz
 *
 */

public enum PowerMeasurementRefType { 
    Measurement,
    Estimation,
    None
}
