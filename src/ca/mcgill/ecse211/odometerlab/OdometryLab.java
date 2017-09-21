// Lab2.java

package ca.mcgill.ecse211.odometerlab;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.*;
//import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class OdometryLab {
	
	private static final Port usPort = LocalEV3.get().getPort("S1");
  private static final EV3LargeRegulatedMotor leftMotor =
      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
  
  private static final EV3LargeRegulatedMotor rightMotor =
      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));

  public static final double WHEEL_RADIUS = 2.1;
  public static final double TRACK = 15.8;

  public static void main(String[] args) {
    int buttonChoice;

    
    // Setup ultrasonic sensor
    // There are 4 steps involved:
    // 1. Create a port object attached to a physical port (done already above)
    // 2. Create a sensor instance and attach to port
    // 3. Create a sample provider instance for the above and initialize operating mode
    // 4. Create a buffer for the sensor data

    @SuppressWarnings("resource") // Because we don't bother to close this resource
    SensorModes usSensor = new EV3ColorSensor(usPort); // usSensor is the instance
    SampleProvider usColor = usSensor.getMode("Red"); // usDistance provides samples from
                                                              // this instance
    float[] usData = new float[usColor.sampleSize()]; // usData is the buffer in which data are
                                                         // returned
    final TextLCD t = LocalEV3.get().getTextLCD();
    Odometer odometer = new Odometer(leftMotor, rightMotor);
    OdometryDisplay odometryDisplay = new OdometryDisplay(odometer, t);
    OdometryCorrection odometryCorrection = new OdometryCorrection(odometer, usData);

    do {
      // clear the display
      t.clear();

      // ask the user whether the motors should drive in a square or float
      t.drawString("< Left | Right >", 0, 0);
      t.drawString("       |        ", 0, 1);
      t.drawString(" Float | Drive  ", 0, 2);
      t.drawString("motors | in a   ", 0, 3);
      t.drawString("       | square ", 0, 4);

      buttonChoice = Button.waitForAnyPress();
    } while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT);

    if (buttonChoice == Button.ID_LEFT) {

      leftMotor.forward();
      leftMotor.flt();
      rightMotor.forward();
      rightMotor.flt();

      odometer.start();
      odometryDisplay.start();

    } else {
      // clear the display
      t.clear();

      // ask the user whether the motors should drive in a square or float
      t.drawString("< Left | Right >", 0, 0);
      t.drawString("  No   | with   ", 0, 1);
      t.drawString(" corr- | corr-  ", 0, 2);
      t.drawString(" ection| ection ", 0, 3);
      t.drawString("       |        ", 0, 4);
      
      buttonChoice = Button.waitForAnyPress();
      
      odometer.start();
      odometryDisplay.start();
      
      if(buttonChoice == Button.ID_RIGHT){  //should be == not !=,maybe change left and right
        odometryCorrection.start();
      }
      
      // spawn a new Thread to avoid SquareDriver.drive() from blocking
      (new Thread() {
        public void run() {
          SquareDriver.drive(leftMotor, rightMotor, WHEEL_RADIUS, WHEEL_RADIUS, TRACK);
        }
      }).start();
    }

    while (Button.waitForAnyPress() != Button.ID_ESCAPE);
    System.exit(0);
  }
}
