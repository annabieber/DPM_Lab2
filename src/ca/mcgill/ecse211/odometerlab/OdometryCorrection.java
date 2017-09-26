/* Student name: Francois-Eliott Roussseau and Anna Bieber
 * Student ID: 260670000 and 260678856
 * Group 52 
 */


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
	private static final double LINE_LIGHT = 0.15; //NEED TO CHANGE
	// The distance of the sensor from the wheel axle
	private static final double SENSOR_OFFSET = 6.6;  //NEED TO CHANGE // entre 5.2 et 7.2
	
	//
	private static final double ONE_QUARTER_PI = Math.PI / 4;
	private static final double THREE_QUARTER_PI = 3 * ONE_QUARTER_PI;
	private static final double FIVE_QUARTER_PI = 5 * ONE_QUARTER_PI;
	private static final double SEVEN_QUARTER_PI = 7 * ONE_QUARTER_PI;


	private static final long CORRECTION_PERIOD = 10;
	private Odometer odometer;
	private SampleProvider lsColor;
	private double error = 13.5;
	private static Port csPort = LocalEV3.get().getPort("S1");
	private SensorModes csSensor;
	private float[] lsData;


	// constructor
	public OdometryCorrection(Odometer odometer) {
		this.odometer = odometer;
		this.csSensor= new EV3ColorSensor(csPort);
		this.lsColor = csSensor.getMode("Red");
		this.lsData = new float[csSensor.sampleSize()];

	}

	// run method (required for Thread)
	public void run() {

		long correctionStart, correctionEnd;

		while (true) {
			correctionStart = System.currentTimeMillis();

			//TODO Place correction implementation here

			//CODE TO WRITE 

			lsColor.fetchSample(lsData, 0); //get's the light value from the sensor
			float reading = lsData[0]; 
					
			//method to adjust the robot according to ligh sensor values
			if (reading <= LINE_LIGHT ){
				Sound.beep(); //if detects a black line

				double theta = odometer.getTheta();
				
				
				if(Math.abs(odometer.getX()- 0) < error && theta < THREE_QUARTER_PI ) // first line crossed, sets the origin for x, we need the condition of theta to be less than 135 degrees  
				{
					odometer.setX(0.00 - SENSOR_OFFSET); // we put an offset error because our sensor is in front on our wheel axis 
				}
				else if(Math.abs(odometer.getX()-60.96) < error && theta < THREE_QUARTER_PI) //third line crossed x axis 
				{
					odometer.setX(60.96 - SENSOR_OFFSET);
				}
				if(Math.abs(odometer.getY()-0) < error && theta < ONE_QUARTER_PI) // sets the origin for y and theta needs to be less than 45 degrees
				{
					odometer.setY(0.00 - SENSOR_OFFSET);
				}
				else if(Math.abs(odometer.getY()-60.96) < error && theta <ONE_QUARTER_PI) //third line crossed in y axis 
				{
					odometer.setY(60.96 - SENSOR_OFFSET );   
				}


				if((odometer.getX() < 60.96 + error) && ((odometer.getX() > 60.96 - error)) && (theta < SEVEN_QUARTER_PI) && (theta > FIVE_QUARTER_PI)) // third line crossed going back left on x axis and theta needs to be bounded between 225 degrees and 315 degrees
				{
					odometer.setX(60.96 + SENSOR_OFFSET); // now we add the offset because x is decreasing (going down) 
				}

				else if((odometer.getY() < 60.96 + error) && ((odometer.getY() > 60.96 - error)) && (theta > THREE_QUARTER_PI) && (theta < FIVE_QUARTER_PI)) // third line crossed going back down  on y axis and theta needs to be bounded between 135 degrees and 225 degrees
					
				{
					odometer.setY(60.96 + SENSOR_OFFSET );
				}
			}


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



