/* This program was created by FTC Team 11574.  Autonomous program to use encoders to
*  drive to center vortex, knock capball off and park on the center board
*  Created January 2017*/

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;

// set name to show on phone
@Autonomous(name="Encoders_CapBall", group="Autonomous")

// declaring variables
public class Encoders_CapBall extends OpMode {

    DcMotor leftfrontdrivemotor;
    DcMotor leftreardrivemotor;
    DcMotor rightfrontdrivemotor;
    DcMotor rightreardrivemotor;

    /* This is our calculations.  We have this so if we need to change
    distance or gear ratio we easily can.
    */

    final static int ENCODER_CPR = 1120; //Encoder Counters per Revolutionk
    final static double GEAR_RATIO = 1;  // Gear Ratio - 1:1 - Direct Drive
    final static int WHEEL_DIAMETER = 4;  // Diameter of the wheel in inches
    final static int DISTANCE = 54;      // Distance in inches to drive too

    final static double CIRCUMFERENCE = Math.PI * WHEEL_DIAMETER;
    final static double ROTATIONS = DISTANCE / CIRCUMFERENCE;
    final static double COUNTS = ENCODER_CPR * ROTATIONS * GEAR_RATIO;

    // This is called when init button is  pressed.
    // It calls the configuration from the phone so the robot knows which motors are which.
    @Override
    public void init() {
        leftfrontdrivemotor = hardwareMap.dcMotor.get("mFL");
        rightfrontdrivemotor = hardwareMap.dcMotor.get("mFR");
        rightreardrivemotor = hardwareMap.dcMotor.get("mBR");
        leftreardrivemotor = hardwareMap.dcMotor.get("mBL");

        // Right motors reversed
        rightfrontdrivemotor.setDirection(DcMotor.Direction.REVERSE);
        rightreardrivemotor.setDirection(DcMotor.Direction.REVERSE);

        // reset encoder count kept by motors.
        leftfrontdrivemotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightfrontdrivemotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftreardrivemotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightreardrivemotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
    }
    // wait for start button.
    @Override
    public void start() {

        // set target position equal to counts calculated from above
        leftfrontdrivemotor.setTargetPosition((int) COUNTS);
        rightfrontdrivemotor.setTargetPosition((int) COUNTS);
        leftreardrivemotor.setTargetPosition((int) COUNTS);
        rightreardrivemotor.setTargetPosition((int) COUNTS);

        // set speed of motors
        leftfrontdrivemotor.setPower(0.6);
        leftreardrivemotor.setPower(0.6);
        rightfrontdrivemotor.setPower(0.6);
        rightreardrivemotor.setPower(0.6);

        // set motor to run to target encoder position and stop with brakes on.
        // RUN_WITH_ENCODER will stop with coast.
        leftfrontdrivemotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rightfrontdrivemotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        leftreardrivemotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rightreardrivemotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);

    }
    @Override
    public void loop() {
        telemetry.addData("Motor Target", COUNTS);
        telemetry.addData("Left Front Position", leftfrontdrivemotor.getCurrentPosition());
        telemetry.addData("Right Front Position", rightfrontdrivemotor.getCurrentPosition());
        telemetry.addData("Left Rear Position", leftreardrivemotor.getCurrentPosition());
        telemetry.addData("Right Rear Position", rightreardrivemotor.getCurrentPosition());

    }
}