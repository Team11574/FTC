// Basic autonomous program that drives the Omni Wheel robot forward 2 seconds then stops.
// It will also wait 10 seconds before starting.
// We hope this will knock the cap ball off the board and park us on the center board.

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

// Below is the Annotation that registers this OpMode with the FtcRobotController app.
// @Autonomous classifies the OpMode as autonomous, name is the OpMode title and the
// optional group places the OpMode into the Mecanum Wheels group.


@Autonomous(name="Mecanum Wheels CapBall", group="Mecanum Wheels")
//@Disabled
public class Mecanum_Wheels_CapBall extends LinearOpMode
{
    DcMotor leftfrontdrivemotor;
    DcMotor leftreardrivemotor;
    DcMotor rightfrontdrivemotor;
    DcMotor rightreardrivemotor;


    // This is called when init button is  pressed.
    // It calls the configuration from the phone so the robot knows which motors are which.

    @Override
    public void runOpMode() throws InterruptedException
    {
        leftfrontdrivemotor = hardwareMap.dcMotor.get("mFL");
        leftreardrivemotor = hardwareMap.dcMotor.get("mBL");
        rightfrontdrivemotor = hardwareMap.dcMotor.get("mFR");
        rightreardrivemotor = hardwareMap.dcMotor.get("mBR");
        rightfrontdrivemotor.setDirection(DcMotor.Direction.REVERSE);
        rightreardrivemotor.setDirection(DcMotor.Direction.REVERSE);

        telemetry.addData("Mode", "waiting");
        telemetry.update();

        // wait for start button.

        waitForStart();

        {
            Thread.sleep(10000);   // Waits ten seconds and then starts the program.

            // set both motors to 30% power and drives robot forward for 3 seconds.

            leftfrontdrivemotor.setPower(0.30);
            leftreardrivemotor.setPower(0.30);
            rightfrontdrivemotor.setPower(0.30);
            rightreardrivemotor.setPower(0.30);

            sleep(2500);        // wait for 2.5 seconds.

            // set motor power to zero to stop motors.

            leftfrontdrivemotor.setPower(0.0);
            leftreardrivemotor.setPower(0.0);
            rightfrontdrivemotor.setPower(0.0);
            rightreardrivemotor.setPower(0.0);

            telemetry.addData("Mode", "running");
            telemetry.update();

            idle();
        }
    }
}