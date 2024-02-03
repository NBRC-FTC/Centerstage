/* Copyright (c) 2022 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
//import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;

/*
 *  This OpMode illustrates the concept of driving an autonomous path based on Gyro (IMU) heading and encoder counts.
 *  The code is structured as a LinearOpMode
 *
 *  The path to be followed by the robot is built from a series of drive, turn or pause steps.
 *  Each step on the path is defined by a single function call, and these can be strung together in any order.
 *
 *  The code REQUIRES that you have encoders on the drive motors, otherwise you should use: RobotAutoDriveByTime;
 *
 *  This code uses the Universal IMU interface so it will work with either the BNO055, or BHI260 IMU.
 *  To run as written, the Control/Expansion hub should be mounted horizontally on a flat part of the robot chassis.
 *  The REV Logo should be facing UP, and the USB port should be facing forward.
 *  If this is not the configuration of your REV Control Hub, then the code should be modified to reflect the correct orientation.
 *
 *  This sample requires that the drive Motors have been configured with names : left_drive and right_drive.
 *  It also requires that a positive power command moves both motors forward, and causes the encoders to count UP.
 *  So please verify that both of your motors move the robot forward on the first move.  If not, make the required correction.
 *  See the beginning of runOpMode() to set the FORWARD/REVERSE option for each motor.
 *
 *  This code uses RUN_TO_POSITION mode for driving straight, and RUN_USING_ENCODER mode for turning and holding.
 *  Note: This code implements the requirement of calling setTargetPosition() at least once before switching to RUN_TO_POSITION mode.
 *
 *  Notes:
 *
 *  All angles are referenced to the coordinate-frame that is set whenever resetHeading() is called.
 *  In this sample, the heading is reset when the Start button is touched on the Driver station.
 *  Note: It would be possible to reset the heading after each move, but this would accumulate steering errors.
 *
 *  The angle of movement/rotation is assumed to be a standardized rotation around the robot Z axis,
 *  which means that a Positive rotation is Counter Clockwise, looking down on the field.
 *  This is consistent with the FTC field coordinate conventions set out in the document:
 *  https://ftc-docs.firstinspires.org/field-coordinate-system
 *
 *  Control Approach.
 *
 *  To reach, or maintain a required heading, this code implements a basic Proportional Controller where:
 *
 *      Steering power = Heading Error * Proportional Gain.
 *
 *      "Heading Error" is calculated by taking the difference between the desired heading and the actual heading,
 *      and then "normalizing" it by converting it to a value in the +/- 180 degree range.
 *
 *      "Proportional Gain" is a constant that YOU choose to set the "strength" of the steering response.
 *
 *  Use Android Studio to Copy this Class, and Paste it into your "TeamCode" folder with a new name.
 *  Remove or comment out the @Disabled line to add this OpMode to the Driver Station OpMode list
 */

@Autonomous(name="FTC 24007: Auto Drive By Gyro 01072024", group="Robot")
//@Disabled
public class FTC_24007_Auto_01072024 extends LinearOpMode {

    /* Declare OpMode members. */
    private DcMotor         leftDrive   = null;
    private DcMotor         rightDrive  = null;
    private IMU             imu         = null;      // Control/Expansion Hub IMU

    private double          headingError  = 0;

    // These variable are declared here (as class members) so they can be updated in various methods,
    // but still be displayed by sendTelemetry()
    private double  targetHeading = 0;
    private double  driveSpeed    = 0;
    private double  turnSpeed     = 0;
    private double  leftSpeed     = 0;
    private double  rightSpeed    = 0;
    private int     leftTarget    = 0;
    private int     rightTarget   = 0;

    /*public enum START_POSITION{
        BLUE_NEAR,
        BLUE_FAR,
        RED_NEAR,
        RED_FAR,
        }

    public static START_POSITION startPosition;
     */
    int startPosition;
    public static String TEAM_NAME = "EDIT TEAM NAME"; //TODO: Enter team Name
    public static int TEAM_NUMBER = 0; //TODO: Enter team Number







    // Calculate the COUNTS_PER_INCH for your specific drive train.
    // Go to your motor vendor website to determine your motor's COUNTS_PER_MOTOR_REV
    // For external drive gearing, set DRIVE_GEAR_REDUCTION as needed.
    // For example, use a value of 2.0 for a 12-tooth spur gear driving a 24-tooth spur gear.
    // This is gearing DOWN for less speed and more torque.
    // For gearing UP, use a gear ratio less than 1.0. Note this will affect the direction of wheel rotation.
    static final double     COUNTS_PER_MOTOR_REV    = 537.7 ;   // eg: GoBILDA 312 RPM Yellow Jacket
    static final double     DRIVE_GEAR_REDUCTION    = 1.0 ;     // No External Gearing.
    static final double     WHEEL_DIAMETER_INCHES   = 3.78;     // For figuring circumference
    static final double     COUNTS_PER_INCH         = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
                                                      (WHEEL_DIAMETER_INCHES * 3.1415);

    // These constants define the desired driving/control characteristics
    // They can/should be tweaked to suit the specific robot drive train.
    static final double     DRIVE_SPEED             = 0.2;     // Max driving speed for better distance accuracy.
    static final double     FAST_DRIVE_SPEED             = 0.5;     // Max driving speed for better distance accuracy.
    static final double     TURN_SPEED              = 0.2;     // Max Turn speed to limit turn rate
    static final double     HEADING_THRESHOLD       = 1.0 ;    // How close must the heading get to the target before moving to next step.
                                                               // Requiring more accuracy (a smaller number) will often make the turn take longer to get into the final position.
    // Define the Proportional control coefficient (or GAIN) for "heading control".
    // We define one value when Turning (larger errors), and the other is used when Driving straight (smaller errors).
    // Increase these numbers if the heading does not corrects strongly enough (eg: a heavy robot or using tracks)
    // Decrease these numbers if the heading does not settle on the correct value (eg: very agile robot with omni wheels)
    static final double     P_TURN_GAIN            = 0.02;     // Larger is more responsive, but also less stable
    static final double     P_DRIVE_GAIN           = 0.03;     // Larger is more responsive, but also less stable


    @Override
    public void runOpMode() {

        //Key Pay inputs to selecting Starting Position of robot
        selectStartingPosition();
        telemetry.addData("Selected Starting Position", startPosition);

        // Initialize the drive system variables.
        leftDrive  = hardwareMap.get(DcMotor.class, "left_drive");
        rightDrive = hardwareMap.get(DcMotor.class, "right_drive");

        // To drive forward, most robots need the motor on one side to be reversed, because the axles point in opposite directions.
        // When run, this OpMode should start both motors driving forward. So adjust these two lines based on your first test drive.
        // Note: The settings here assume direct drive on left and right wheels.  Gear Reduction or 90 Deg drives may require direction flips
        leftDrive.setDirection(DcMotor.Direction.FORWARD);
        rightDrive.setDirection(DcMotor.Direction.REVERSE);

        /* The next two lines define Hub orientation.
         * The Default Orientation (shown) is when a hub is mounted horizontally with the printed logo pointing UP and the USB port pointing FORWARD.
         *
         * To Do:  EDIT these two lines to match YOUR mounting configuration.
         */
        RevHubOrientationOnRobot.LogoFacingDirection logoDirection = RevHubOrientationOnRobot.LogoFacingDirection.UP;
        RevHubOrientationOnRobot.UsbFacingDirection  usbDirection  = RevHubOrientationOnRobot.UsbFacingDirection.FORWARD;
        RevHubOrientationOnRobot orientationOnRobot = new RevHubOrientationOnRobot(logoDirection, usbDirection);

        // Now initialize the IMU with this mounting orientation
        // This sample expects the IMU to be in a REV Hub and named "imu".
        imu = hardwareMap.get(IMU.class, "imu");
        imu.initialize(new IMU.Parameters(orientationOnRobot));

        // Ensure the robot is stationary.  Reset the encoders and set the motors to BRAKE mode
        leftDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        Vision vision = new Vision(hardwareMap, telemetry, startPosition);

        telemetry.addData("Selected Starting Position", startPosition);

        //Activate Camera Vision that uses Open CV Vision processor for Team Element detection
        vision.initOpenCV();

        // Wait for the DS start button to be touched.
        telemetry.addLine("Open CV Vision for Red/Blue Team Element Detection");
        telemetry.addData("DS preview on/off", "3 dots, Camera Stream");
        telemetry.addLine("The starting point of the robot is assumed to be on the starting tile, " +
                "and along the edge farther from the truss legs. ");
        telemetry.addLine("You should also have a webcam connected and positioned in a way to see " +
                "the middle spike mark and the spike mark away from the truss (and ideally nothing else). " +
                "We assumed the camera to be in the center of the robot. ");
        telemetry.addData(">", "Touch Play to start OpMode");
        telemetry.update();
        //waitForStart();

        while (!isStopRequested() && !opModeIsActive()) {
            telemetry.addData("Selected Starting Position", startPosition);
            telemetry.addData(">", "Robot Heading = %4.0f", getHeading());
//            telemetry.update();
            //Run Open CV Object Detection and keep watching for the Team Element on the spike marks.
            vision.runOpenCVObjectDetection();
        }

        ArmAndWrist armAndWrist = new ArmAndWrist(hardwareMap);
        Claw claw =  new Claw(hardwareMap);

        // Set the encoders for closed loop speed control, and reset the heading.
        leftDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        imu.resetYaw();

        // Step through each leg of the path,
        // Notes:   Reverse movement is obtained by setting a negative distance (not speed)
        //          holdHeading() is used after turns to let the heading stabilize
        //          Add a sleep(2000) after any step to keep the telemetry data visible for review

        holdHeading(TURN_SPEED, 0, 1);   // Hold 0 Deg heading for .5 seconds
        claw.closeClaw(); // Close Claw
        holdHeading(TURN_SPEED, 0, 1);   // Hold 0 Deg heading for .5 seconds
        armAndWrist.carryPosition(); // Move Arm and Wrist to Close position
        holdHeading(TURN_SPEED, 0, 1);   // Hold 0 Deg heading for .5 seconds
        driveStraight(DRIVE_SPEED, 3.5, 0);  // Drive forward 3.5 inches
        driveStraight(DRIVE_SPEED, 12, 0);  // Drive forward 12 inches

        switch (startPosition) {
            case 0://BLUE_NEAR

                switch(vision.identifiedSpikeMarkLocation){
                    case LEFT:
                        driveStraight(DRIVE_SPEED, 3.5, 0);
                        turnToHeading(TURN_SPEED, 30);
                        holdHeading(TURN_SPEED, 30, 1);
                        driveStraight(DRIVE_SPEED, 5, 30);    // Drive Forward 2"
                        armAndWrist.startPosition();
                        holdHeading(DRIVE_SPEED, 30,1);
                        claw.openClaw();
                        holdHeading(TURN_SPEED, 30, 1);   // Hold 0 Deg heading for .5 seconds
                        armAndWrist.carryPosition();
                        holdHeading(TURN_SPEED, 30, 1);
                        driveStraight(DRIVE_SPEED, -4.5, 30);
                        turnToHeading(TURN_SPEED, 0);
                        holdHeading(TURN_SPEED, 0.0, 1);
                        driveStraight(DRIVE_SPEED, -3.5, 0);
                        break;

                    case MIDDLE:
                        driveStraight(DRIVE_SPEED, 11.5, 0);  // Drive forward 12 inches
                        armAndWrist.startPosition();
                        holdHeading(DRIVE_SPEED, 0,1);
                        claw.openClaw();
                        holdHeading(TURN_SPEED, 0, 1);   // Hold 0 Deg heading for .5 seconds
                        armAndWrist.carryPosition();
                        driveStraight(DRIVE_SPEED, -11.5, 0);
                        break;

                    case RIGHT:
                        driveStraight(DRIVE_SPEED, 4, 0);    // Drive Forward 2"
                        turnToHeading(TURN_SPEED, -53);
                        holdHeading(TURN_SPEED, -53, 1);
                        driveStraight(DRIVE_SPEED, 4, -53);    // Drive Forward 2"
                        armAndWrist.startPosition();
                        holdHeading(DRIVE_SPEED, -53,1);
                        claw.openClaw();
                        holdHeading(TURN_SPEED, -53, 1);   // Hold 0 Deg heading for 1 seconds
                        armAndWrist.carryPosition();
                        driveStraight(DRIVE_SPEED, -4, -53);    // Drive Forward 2"
                        turnToHeading(TURN_SPEED, 0);
                        holdHeading(TURN_SPEED, 0, 1);   // Hold 0 Deg heading for 1 seconds
                        driveStraight(DRIVE_SPEED, -4, 0);    // Drive Forward 2"
                        break;

                }

                driveStraight(DRIVE_SPEED, -10, 0);  // Drive forward -10 inches
                turnToHeading(TURN_SPEED, 90);               // Turn  CCW to 90 Degrees
                holdHeading(TURN_SPEED, 90, 1);   // Hold 90 Deg heading for a 1 second
                driveStraight(DRIVE_SPEED, 41, 90); // Drive Forward 44"
                armAndWrist.startPosition();
                break;

            case 1: //RED_NEAR

                switch(vision.identifiedSpikeMarkLocation){
                    case LEFT:
                        driveStraight(DRIVE_SPEED, 3.5, 0);
                        turnToHeading(TURN_SPEED, 30);
                        holdHeading(TURN_SPEED, 30, 1);
                        driveStraight(DRIVE_SPEED, 5, 30);    // Drive Forward 2"
                        armAndWrist.startPosition();
                        holdHeading(DRIVE_SPEED, 30,1);
                        claw.openClaw();
                        holdHeading(TURN_SPEED, 30, 1);   // Hold 0 Deg heading for .5 seconds
                        armAndWrist.carryPosition();
                        holdHeading(TURN_SPEED, 30, 1);
                        driveStraight(DRIVE_SPEED, -4.5, 30);
                        turnToHeading(TURN_SPEED, 0);
                        holdHeading(TURN_SPEED, 0.0, 1);
                        driveStraight(DRIVE_SPEED, -3.5, 0);
                        break;

                    case MIDDLE:
                        driveStraight(DRIVE_SPEED, 11.5, 0);  // Drive forward 12 inches
                        armAndWrist.startPosition();
                        holdHeading(DRIVE_SPEED, 0,1);
                        claw.openClaw();
                        holdHeading(TURN_SPEED, 0, 1);   // Hold 0 Deg heading for .5 seconds
                        armAndWrist.carryPosition();
                        driveStraight(DRIVE_SPEED, -11.5, 0);
                        break;

                    case RIGHT:
                        driveStraight(DRIVE_SPEED, 4, 0);    // Drive Forward 2"
                        turnToHeading(TURN_SPEED, -53);
                        holdHeading(TURN_SPEED, -53, 1);
                        driveStraight(DRIVE_SPEED, 4, -53);    // Drive Forward 2"
                        armAndWrist.startPosition();
                        holdHeading(DRIVE_SPEED, -53,1);
                        claw.openClaw();
                        holdHeading(TURN_SPEED, -53, 1);   // Hold 0 Deg heading for 1 seconds
                        armAndWrist.carryPosition();
                        driveStraight(DRIVE_SPEED, -4, -53);    // Drive Forward 2"
                        turnToHeading(TURN_SPEED, 0);
                        holdHeading(TURN_SPEED, 0, 1);   // Hold 0 Deg heading for 1 seconds
                        driveStraight(DRIVE_SPEED, -4, 0);    // Drive Forward 2"
                        break;
                }
                driveStraight(DRIVE_SPEED, -11, 0.0);  // Drive forward 12 inches
                turnToHeading(TURN_SPEED, -90.0);               // Turn  CCW to 90 Degrees
                holdHeading(TURN_SPEED, -90.0, 1);   // Hold 90 Deg heading for a 1/2 second
                driveStraight(DRIVE_SPEED, 44, -90.0); // Drive Forward 44"
                armAndWrist.startPosition();
                break;

            case 2: //BLUE_FAR

                switch(vision.identifiedSpikeMarkLocation){
                    case LEFT:
                        driveStraight(DRIVE_SPEED, 3.5, 0);
                        turnToHeading(TURN_SPEED, 30);
                        holdHeading(TURN_SPEED, 30, 1);
                        driveStraight(DRIVE_SPEED, 5, 30);    // Drive Forward 2"
                        armAndWrist.startPosition();
                        holdHeading(DRIVE_SPEED, 30,1);
                        claw.openClaw();
                        holdHeading(TURN_SPEED, 30, 1);   // Hold 0 Deg heading for .5 seconds
                        armAndWrist.carryPosition();
                        holdHeading(TURN_SPEED, 30, 1);
                        driveStraight(DRIVE_SPEED, -4.5, 30);
                        turnToHeading(TURN_SPEED, 0);
                        holdHeading(TURN_SPEED, 0.0, 1);
                        driveStraight(DRIVE_SPEED, -3.5, 0);
                        break;

                    case MIDDLE:
                        driveStraight(DRIVE_SPEED, 11.5, 0);  // Drive forward 12 inches
                        armAndWrist.startPosition();
                        holdHeading(DRIVE_SPEED, 0,1);
                        claw.openClaw();
                        holdHeading(TURN_SPEED, 0, 1);   // Hold 0 Deg heading for .5 seconds
                        armAndWrist.carryPosition();
                        driveStraight(DRIVE_SPEED, -11.5, 0);
                        break;

                    case RIGHT:
                        driveStraight(DRIVE_SPEED, 4, 0);    // Drive Forward 2"
                        turnToHeading(TURN_SPEED, -53);
                        holdHeading(TURN_SPEED, -53, 1);
                        driveStraight(DRIVE_SPEED, 4, -53);    // Drive Forward 2"
                        armAndWrist.startPosition();
                        holdHeading(DRIVE_SPEED, -53,1);
                        claw.openClaw();
                        holdHeading(TURN_SPEED, -53, 1);   // Hold 0 Deg heading for 1 seconds
                        armAndWrist.carryPosition();
                        driveStraight(DRIVE_SPEED, -4, -53);    // Drive Forward 2"
                        turnToHeading(TURN_SPEED, 0);
                        holdHeading(TURN_SPEED, 0, 1);   // Hold 0 Deg heading for 1 seconds
                        driveStraight(DRIVE_SPEED, -4, 0);    // Drive Forward 2"
                        break;
                }

                driveStraight(DRIVE_SPEED, -12, 0);  // Drive forward 12 inches
                turnToHeading(TURN_SPEED, -45);
                holdHeading(TURN_SPEED, -60, 0.5);
                driveStraight( FAST_DRIVE_SPEED, 26, -60);
                turnToHeading(TURN_SPEED, 0);
                holdHeading(TURN_SPEED, 0, 0.5);
                driveStraight(DRIVE_SPEED, 30, 0);
                turnToHeading(TURN_SPEED, 90);
                holdHeading(TURN_SPEED, 90, 0.5);
                driveStraight(FAST_DRIVE_SPEED, 105, 90);
                claw.openClaw();
                break;

            case 3: //RED_FAR

                switch(vision.identifiedSpikeMarkLocation){
                    case LEFT:

                        break;
                    case MIDDLE:

                        break;
                    case RIGHT:

                        break;
                }

                break;
        }

        telemetry.addData("Path", "Complete");
        telemetry.update();
        sleep(1000);  // Pause to display last telemetry message.
    }

    /*
     * ====================================================================================================
     * Driving "Helper" functions are below this line.
     * These provide the high and low level methods that handle driving straight and turning.
     * ====================================================================================================
     */

    // **********  HIGH Level driving functions.  ********************

    /**
    *  Drive in a straight line, on a fixed compass heading (angle), based on encoder counts.
    *  Move will stop if either of these conditions occur:
    *  1) Move gets to the desired position
    *  2) Driver stops the OpMode running.
    *
    * @param maxDriveSpeed MAX Speed for forward/rev motion (range 0 to +1.0) .
    * @param distance   Distance (in inches) to move from current position.  Negative distance means move backward.
    * @param heading      Absolute Heading Angle (in Degrees) relative to last gyro reset.
    *                   0 = fwd. +ve is CCW from fwd. -ve is CW from forward.
    *                   If a relative angle is required, add/subtract from the current robotHeading.
    */
    public void driveStraight(double maxDriveSpeed,
                              double distance,
                              double heading) {

        // Ensure that the OpMode is still active
        if (opModeIsActive()) {

            // Determine new target position, and pass to motor controller
            int moveCounts = (int)(distance * COUNTS_PER_INCH);
            leftTarget = leftDrive.getCurrentPosition() + moveCounts;
            rightTarget = rightDrive.getCurrentPosition() + moveCounts;

            // Set Target FIRST, then turn on RUN_TO_POSITION
            leftDrive.setTargetPosition(leftTarget);
            rightDrive.setTargetPosition(rightTarget);

            leftDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            rightDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            // Set the required driving speed  (must be positive for RUN_TO_POSITION)
            // Start driving straight, and then enter the control loop
            maxDriveSpeed = Math.abs(maxDriveSpeed);
            moveRobot(maxDriveSpeed, 0);

            // keep looping while we are still active, and BOTH motors are running.
            while (opModeIsActive() &&
                   (leftDrive.isBusy() && rightDrive.isBusy())) {

                // Determine required steering to keep on heading
                turnSpeed = getSteeringCorrection(heading, P_DRIVE_GAIN);

                // if driving in reverse, the motor correction also needs to be reversed
                if (distance < 0)
                    turnSpeed *= -1.0;

                // Apply the turning correction to the current driving speed.
                moveRobot(driveSpeed, turnSpeed);

                // Display drive status for the driver.
                sendTelemetry(true);
            }

            // Stop all motion & Turn off RUN_TO_POSITION
            moveRobot(0, 0);
            leftDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            rightDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
    }

    /**
     *  Spin on the central axis to point in a new direction.
     *  <p>
     *  Move will stop if either of these conditions occur:
     *  <p>
     *  1) Move gets to the heading (angle)
     *  <p>
     *  2) Driver stops the OpMode running.
     *
     * @param maxTurnSpeed Desired MAX speed of turn. (range 0 to +1.0)
     * @param heading Absolute Heading Angle (in Degrees) relative to last gyro reset.
     *              0 = fwd. +ve is CCW from fwd. -ve is CW from forward.
     *              If a relative angle is required, add/subtract from current heading.
     */
    public void turnToHeading(double maxTurnSpeed, double heading) {

        // Run getSteeringCorrection() once to pre-calculate the current error
        getSteeringCorrection(heading, P_DRIVE_GAIN);

        // keep looping while we are still active, and not on heading.
        while (opModeIsActive() && (Math.abs(headingError) > HEADING_THRESHOLD)) {

            // Determine required steering to keep on heading
            turnSpeed = getSteeringCorrection(heading, P_TURN_GAIN);

            // Clip the speed to the maximum permitted value.
            turnSpeed = Range.clip(turnSpeed, -maxTurnSpeed, maxTurnSpeed);

            // Pivot in place by applying the turning correction
            moveRobot(0, turnSpeed);

            // Display drive status for the driver.
            sendTelemetry(false);
        }

        // Stop all motion;
        moveRobot(0, 0);
    }

    /**
     *  Obtain & hold a heading for a finite amount of time
     *  <p>
     *  Move will stop once the requested time has elapsed
     *  <p>
     *  This function is useful for giving the robot a moment to stabilize it's heading between movements.
     *
     * @param maxTurnSpeed      Maximum differential turn speed (range 0 to +1.0)
     * @param heading    Absolute Heading Angle (in Degrees) relative to last gyro reset.
     *                   0 = fwd. +ve is CCW from fwd. -ve is CW from forward.
     *                   If a relative angle is required, add/subtract from current heading.
     * @param holdTime   Length of time (in seconds) to hold the specified heading.
     */
    public void holdHeading(double maxTurnSpeed, double heading, double holdTime) {

        ElapsedTime holdTimer = new ElapsedTime();
        holdTimer.reset();

        // keep looping while we have time remaining.
        while (opModeIsActive() && (holdTimer.time() < holdTime)) {
            // Determine required steering to keep on heading
            turnSpeed = getSteeringCorrection(heading, P_TURN_GAIN);

            // Clip the speed to the maximum permitted value.
            turnSpeed = Range.clip(turnSpeed, -maxTurnSpeed, maxTurnSpeed);

            // Pivot in place by applying the turning correction
            moveRobot(0, turnSpeed);

            // Display drive status for the driver.
            sendTelemetry(false);
        }

        // Stop all motion;
        moveRobot(0, 0);
    }

    // **********  LOW Level driving functions.  ********************

    /**
     * Use a Proportional Controller to determine how much steering correction is required.
     *
     * @param desiredHeading        The desired absolute heading (relative to last heading reset)
     * @param proportionalGain      Gain factor applied to heading error to obtain turning power.
     * @return                      Turning power needed to get to required heading.
     */
    public double getSteeringCorrection(double desiredHeading, double proportionalGain) {
        targetHeading = desiredHeading;  // Save for telemetry

        // Determine the heading current error
        headingError = targetHeading - getHeading();

        // Normalize the error to be within +/- 180 degrees
        while (headingError > 180)  headingError -= 360;
        while (headingError <= -180) headingError += 360;

        // Multiply the error by the gain to determine the required steering correction/  Limit the result to +/- 1.0
        return Range.clip(headingError * proportionalGain, -1, 1);
    }

    /**
     * Take separate drive (fwd/rev) and turn (right/left) requests,
     * combines them, and applies the appropriate speed commands to the left and right wheel motors.
     * @param drive forward motor speed
     * @param turn  clockwise turning motor speed.
     */
    public void moveRobot(double drive, double turn) {
        driveSpeed = drive;     // save this value as a class member so it can be used by telemetry.
        turnSpeed  = turn;      // save this value as a class member so it can be used by telemetry.

        leftSpeed  = drive - turn;
        rightSpeed = drive + turn;

        // Scale speeds down if either one exceeds +/- 1.0;
        double max = Math.max(Math.abs(leftSpeed), Math.abs(rightSpeed));
        if (max > 1.0)
        {
            leftSpeed /= max;
            rightSpeed /= max;
        }

        leftDrive.setPower(leftSpeed);
        rightDrive.setPower(rightSpeed);
    }

    /**
     *  Display the various control parameters while driving
     *
     * @param straight  Set to true if we are driving straight, and the encoder positions should be included in the telemetry.
     */
    private void sendTelemetry(boolean straight) {

        if (straight) {
            telemetry.addData("Motion", "Drive Straight");
            telemetry.addData("Target Pos L:R",  "%7d:%7d",      leftTarget,  rightTarget);
            telemetry.addData("Actual Pos L:R",  "%7d:%7d",      leftDrive.getCurrentPosition(),
                    rightDrive.getCurrentPosition());
        } else {
            telemetry.addData("Motion", "Turning");
        }

        telemetry.addData("Heading- Target : Current", "%5.2f : %5.0f", targetHeading, getHeading());
        telemetry.addData("Error  : Steer Pwr",  "%5.1f : %5.1f", headingError, turnSpeed);
        telemetry.addData("Wheel Speeds L : R", "%5.2f : %5.2f", leftSpeed, rightSpeed);
        telemetry.update();
    }

    /**
     * read the Robot heading directly from the IMU (in degrees)
     */
    public double getHeading() {
        YawPitchRollAngles orientation = imu.getRobotYawPitchRollAngles();
        return orientation.getYaw(AngleUnit.DEGREES);
    }

    public void selectStartingPosition() {
        telemetry.setAutoClear(true);
        telemetry.clearAll();
        //******select start pose*****
        while(!isStopRequested()){
            telemetry.addData("Initializing FTC Wires (ftcwires.org) Autonomous adopted for Team:",
                    TEAM_NAME, " ", TEAM_NUMBER);
            telemetry.addData("---------------------------------------","");
            telemetry.addLine("This Auto program uses Open CV Vision Processor for Team Element detection");
            telemetry.addData("Select Starting Position using XYAB on Logitech on gamepad 1:","");
            telemetry.addData("    Blue Near   ", "(X)");
            telemetry.addData("    Blue Far ", "(Y)");
            telemetry.addData("    Red Far    ", "(B)");
            telemetry.addData("    Red Near  ", "(A)");
            if(gamepad1.x){
                startPosition = 0; //START_POSITION.BLUE_NEAR;
                break;
            }
            if(gamepad1.y){
                startPosition = 2;//START_POSITION.BLUE_FAR;
                break;
            }
            if(gamepad1.b){
                startPosition = 3;//START_POSITION.RED_FAR;
                break;
            }
            if(gamepad1.a){
                startPosition = 1;//START_POSITION.RED_NEAR;
                break;
            }
            telemetry.update();
        }
        telemetry.clearAll();
    }
}

