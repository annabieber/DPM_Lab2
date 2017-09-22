/*The implementation of the p-controller was done in a similar manner with the exception of the motor speed. It was calculated proportional to the distance error measured and subtracted for one motor and added to the other motor. 
 * OdometryCorrection.java
 */
package ca.mcgill.ecse211.odometerlab;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class OdometryCorrection extends Thread {

	// Max light value reading for a grid line
	private static final int LINE_LIGHT = 500; //NEED TO CHANGE
	// The distance of the sensor from the wheel axle
	private static final double SENSOR_OFFSET = 0;  //NEED TO CHANGE // entre 5.2 et 7.2
	// Spacing of the tiles in centimeters
	private static final double TILE_SPACING = 30.48;
	// Half the said spacing
	private static final double HALF_TILE_SPACING = TILE_SPACING / 2;
	// various pi ratios
	private static final double TWO_PI = Math.PI * 2;
	private static final double ONE_QUARTER_PI = Math.PI / 4;
	private static final double THREE_QUARTER_PI = 3 * ONE_QUARTER_PI;
	private static final double FIVE_QUARTER_PI = 5 * ONE_QUARTER_PI;
	private static final double SEVEN_QUARTER_PI = 7 * ONE_QUARTER_PI;

	private static final long CORRECTION_PERIOD = 25;
	private Odometer odometer;
	//private float[] usData;
	private SampleProvider lsColor;
	  private double error = 10.0;
	  private static Port csPort = LocalEV3.get().getPort("S1");
	  private SensorModes csSensor;
	  private float[] lsData;
	
	//private SensorModes lsSensor;

	// constructor
	public OdometryCorrection(Odometer odometer) {
		this.odometer = odometer;
		this.csSensor= new EV3ColorSensor(csPort);
		this.lsColor = csSensor.getMode("Red");
		this.lsData = new float[csSensor.sampleSize()];
		//this.usData = usData; 

	}

	// run method (required for Thread)
	public void run() {
		long correctionStart, correctionEnd;
		// set the line as un-crossed
		boolean crossed = false;

		//if (crossed == true) {
		//     Sound.playNote(Sound.FLUTE, 440, 250);
		//}  
		while (true) {
			correctionStart = System.currentTimeMillis();

			//TODO Place correction implementation here

			//CODE TO WRITE 
			
			lsColor.fetchSample(lsData, 0);
			
			float reading = lsData[0];
			//System.out.println(reading);			
			

			// check if the light value corresponds to a line and it has yet to be crossed
			if (reading <= LINE_LIGHT && !crossed) {  
				// wrap theta to 0 <= theta < 2i
				double theta = odometer.getTheta();
				// check which line direction we just crossed using the heading

				if (theta >= ONE_QUARTER_PI && theta < THREE_QUARTER_PI || theta >= FIVE_QUARTER_PI && theta < SEVEN_QUARTER_PI) {
					//Sound.playNote(Sound.FLUTE, 440, 250);
					// cross horizontal line
					double sensorYOffset = Math.sin(theta) * SENSOR_OFFSET;
					// offset y to account for sensor distance
					double y = odometer.getY() + sensorYOffset;
					// snap y to closest line
					y = Math.round((y + HALF_TILE_SPACING) / TILE_SPACING) * TILE_SPACING - HALF_TILE_SPACING;
					// correct y, removing the offset
					odometer.setY(y - sensorYOffset / 2);

				} else {
					//Sound.playNote(Sound.FLUTE, 880, 250);
					// cross vertical line
					double sensorXOffset = Math.cos(theta) * SENSOR_OFFSET;
					// offset x to account for sensor distance
					double x = odometer.getX() + sensorXOffset;
					// snap x to closest line
					x = Math.round((x + HALF_TILE_SPACING) / TILE_SPACING) * TILE_SPACING - HALF_TILE_SPACING;
					// correct x, removing the offset
					odometer.setX(x - sensorXOffset / 2);
				}

				// set the line as crossed to prevent repeated events
				crossed = true;

			} else {
				// mark the line as done being crossed
				crossed = false;

				// this ensure the odometry correction occurs only once every period
				correctionEnd = System.currentTimeMillis();
				if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
					try {
						Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
					} catch (InterruptedException e) {
						// there is nothing to be done here because it is not
						// expected that the odometry correction will be
						// interrupted by another thread
					}
				}

			}
		}
	}
}


