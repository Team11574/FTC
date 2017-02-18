// Basic autonomous program that drives the Mecanum Wheel robot forward .5 seconds then stops.
// Then turns the robot to the right and drives forward onto the ramp for 5 points

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.util.ElapsedTime;

// Below is the Annotation that registers this OpMode with the FtcRobotController app.
// @Autonomous classifies the OpMode as autonomous, name is the OpMode title and the
// optional group places the OpMode into the Mecanum Wheels group.
// We called this Blue Ramp Mecanum Wheels because it is for if we are on the Blue Alliance Team.


@Autonomous(name="Mecanum Wheels Blue Ramp", group="Mecanum Wheels")
//@Disabled
public class Mecanum_Wheels_Blue_Ramp extends LinearOpMode
{
    DcMotor leftfrontdrivemotor;
    DcMotor leftreardrivemotor;
    DcMotor rightfrontdrivemotor;
    DcMotor rightreardrivemotor;

    private ElapsedTime     runtime = new ElapsedTime();

    static final double     FORWARD_SPEED = 0.6;
    static final double     TURN_SPEED    = 0.5;

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
// Step through each leg of the path, ensuring that the Auto mode has not been stopped along the way

        // Step 1:  Drive forward for .75 seconds
        leftfrontdrivemotor.setPower(0.30);
        leftreardrivemotor.setPower(0.30);
        rightfrontdrivemotor.setPower(0.30);
        rightreardrivemotor.setPower(0.30);

        runtime.reset();
        while (opModeIsActive() && (runtime.seconds() < .75)) {
            telemetry.addData("Path", "Leg 1: %2.5f S Elapsed", runtime.seconds());
            telemetry.update();
            idle();
        }

        // Step 2:  Turn right for 0.75 seconds
        leftfrontdrivemotor.setPower(TURN_SPEED);
        leftreardrivemotor.setPower(TURN_SPEED);
        rightfrontdrivemotor.setPower(-TURN_SPEED);
        rightreardrivemotor.setPower(-TURN_SPEED);
        runtime.reset();
        while (opModeIsActive() && (runtime.seconds() < 0.75)) {
            telemetry.addData("Path", "Leg 2: %2.5f S Elapsed", runtime.seconds());
            telemetry.update();
            idle();
        }

        // Step 3:  Drive Forward for .5 Second
        leftfrontdrivemotor.setPower(FORWARD_SPEED);
        leftreardrivemotor.setPower(FORWARD_SPEED);
        rightfrontdrivemotor.setPower(FORWARD_SPEED);
        rightreardrivemotor.setPower(FORWARD_SPEED);

        runtime.reset();
        while (opModeIsActive() && (runtime.seconds() < .5)) {
            telemetry.addData("Path", "Leg 3: %2.5f S Elapsed", runtime.seconds());
            telemetry.update();
            idle();
        }

        telemetry.addData("Path", "Complete");
        telemetry.update();
        sleep(1000);
        idle();
    }
}