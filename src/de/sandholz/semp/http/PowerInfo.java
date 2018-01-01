package de.sandholz.semp.http;

/**
 * Information about a power consumption. 
 * @author Markus Sandholz
 *
 */
public class PowerInfo {
	
	// instance variables
	private int averagePower = 0; // Real average power within the interval in Watts. 
	private int minPower = -1; // Minimum power value within the interval in Watts. 
	private int maxPower = -1; // Maximum power within the interval in Watts. 
	private long timeStamp = 0; // Timestamp that represents the end of the averaging interval. Although this element is marked as optional it is mandatory in PowerConsumption:PowerInfo. 
	private int averagingInterval = 60; // Length of the averaging interval in seconds. Although this element is marked as optional it is mandatory in PowerConsumption:PowerInfo. 


	// constructor
	/**
	 * Information about a power consumption. 
	 * @param averagePower Real average power within the interval in Watts.
	 * @param averagingInterval Length of the averaging interval in seconds.
	 * @param absoluteTimeStamp Timestamp that represents the end of the averaging interval. Although this element is marked as optional it is mandatory in PowerConsumption:PowerInfo.   
	 */
	public PowerInfo (int averagePower, int averagingInterval, boolean absoluteTimeStamp) {

		this.setPowerInfo(averagePower, averagingInterval, absoluteTimeStamp);	
	}
	
	// accessor methods
	/**
	 * Returns the minimum power value within the interval in Watts. 
	 * @return Minimum power value within the interval in Watts. 
	 */
	public int getMinPower() {
		return minPower;
	}

	/**
	 * Set the minimum power value within the interval in Watts. 
	 * @param minPower Minimum power value within the interval in Watts. 
	 */
	public void setMinPower(int minPower) {
		this.minPower = minPower;
	}

	/**
	 * Returns the maximum power value within the interval in Watts.
	 * @return Maximum power value within the interval in Watts.
	 */
	public int getMaxPower() {
		return maxPower;
	}

	/**
	 * Set the maximum power value within the interval in Watts. 
	 * @param maxPower Maximum power value within the interval in Watts.
	 */
	public void setMaxPower(int maxPower) {
		this.maxPower = maxPower;
	}

	//methods
	/**
	 * Set the information about the power consumption.
	 * @param averagePower Real average power within the interval in Watts.
	 * @param averagingInterval Length of the averaging interval in seconds.
	 * @param absoluteTimeStamp Timestamp that represents the end of the averaging interval. Although this element is marked as optional it is mandatory in PowerConsumption:PowerInfo. 
	 */
	public void setPowerInfo(int averagePower, int averagingInterval, boolean absoluteTimeStamp) {
		
		this.averagePower = averagePower;
		this.averagingInterval = averagingInterval;
		
		if (absoluteTimeStamp) {
			
			long unixTime = System.currentTimeMillis() / 1000L;	
			this.timeStamp = unixTime;
			
		} else {
			
			this.timeStamp = 0;
		}
	}
	
	
	/**
	 * Builds the xml-structure of the &lt;PowerInfo&gt; part of &lt;DeviceStatus&gt;&lt;PowerConsumption&gt;.
	 * 
	 * @return xml-structure as string.
	 */
	public String getMessage() {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("   <PowerInfo>\r\n");
		sb.append("    <AveragePower>").append(this.averagePower).append("</AveragePower>\r\n");
		if(this.minPower >0) { sb.append("    <MinPower>").append(this.minPower).append("</MinPower>\r\n"); }
		if(this.maxPower >0) { sb.append("    <MaxPower>").append(this.maxPower).append("</MaxPower>\r\n"); }
		sb.append("    <Timestamp>").append(this.timeStamp).append("</Timestamp>\r\n");
		sb.append("    <AveragingInterval>").append(this.averagingInterval).append("</AveragingInterval>\r\n");
		sb.append("   </PowerInfo>\r\n");
		
		return sb.toString();
	}
}