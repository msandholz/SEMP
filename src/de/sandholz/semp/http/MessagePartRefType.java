/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sandholz.semp.http;

 /*
 Human readable type of the device. See DeviceTypeRefType for well-known types. 
 * 
 * @author Markus Sandholz
 * 
 */

public enum MessagePartRefType {
	all, 
	DeviceInfo, 
	DeviceStatus, 
	PlanningRequest
}