package org.firstinspires.ftc.teamcode;

/**
 * Created by FTC Team 11574 on 1/28/2017.
 */
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.OpticalDistanceSensor;

// set name to show on phone
@Autonomous(name="Drive and Push Beacon Blue Alliance", group="Autonomous")
public class Drive_and_Push_Beacon_Blue_Alliance extends LinearOpMode {
    final private static int ENCODER_CPR = 1120;  // Encoder Counters per Revolution
    final private static double GEAR_RATIO = 1.0;   // Gear Ratio - 1:1 - Direct Drive
    final private static double WHEEL_DIAMETER = 3.937;  // Diameter of the wheel in inches
    final private static double WHEEL_CIRCUMFERENCE = Math.PI * WHEEL_DIAMETER;
    final private static double STRAFE_SLIPPAGE_FACTOR = 1.08;  // slippage of motors when strafing

    final private static int COLOR_UNKNOWN = 0;
    final private static int COLOR_RED = 1;
    final private static int COLOR_BLUE = 2;
    final private static String[] COLOR_NAMES = {
            "unknown", "red", "blue"
    };

// This is Blue because our alliance color is blue.
    int COLOR_TEAM = COLOR_BLUE;

    DcMotor motor[];
    ColorSensor color;
    OpticalDistanceSensor distance;

    final private static int MOTOR_COUNT = 4;
    final private static int mFL = 0;
    final private static int mFR = 1;
    final private static int mBL = 2;
    final private static int mBR = 3;
    final private static String[] MOTOR_NAMES = {
            "mFL", "mFR", "mBL", "mBR"
    };
    final private static DcMotorSimple.Direction MOTOR_DIRECTIONS[] = {
            DcMotor.Direction.FORWARD, // mFL
            DcMotor.Direction.REVERSE, // mFR
            DcMotor.Direction.FORWARD, // mBL
            DcMotor.Direction.REVERSE, // mBR
    };

    final private static int DRIVE_FORWARD  = 0;
    final private static int DRIVE_BACKWARD = 1;
    final private static int TURN_LEFT      = 2;
    final private static int TURN_RIGHT     = 3;
    final private static int STRAFE_LEFT    = 4;
    final private static int STRAFE_RIGHT   = 5;

    final private static double DRIVE_DIRECTIONS[][] = {
            //mFL,  mFR,   mBL,   mBR
            {+1.00, +1.00, +1.00, +1.00}, // DRIVE_FORWARD
            {-1.00, -1.00, -1.00, -1.00}, // DRIVE_BACKWARD
            {+1.00, -1.00, +1.00, -1.00}, // TURN_LEFT
            {-1.00, +1.00, -1.00, +1.00}, // TURN_RIGHT
            {-1.00, +1.00, +1.00, -1.00}, // STRAFE_LEFT
            {+1.00, -0.92, -0.92, +1.04}, // STRAFE_RIGHT
    };

    private void stop_all_motors() {
        for(int i=0; i < MOTOR_COUNT; i++) {
            motor[i].setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        }
    }

    private int distance_to_count(double distance, double slippage) {
        return((int)(slippage * ENCODER_CPR * (distance / WHEEL_CIRCUMFERENCE) * GEAR_RATIO));
    }

    private void wait_for_one_stop() {
        while(true) {
            for(int i=0; i < MOTOR_COUNT; i++) {
                if(motor[i].isBusy() != true)
                    return;
            }
        }
    }

    private void wait_for_all_stop() {
        while(true) {
            for(int i=0; i < MOTOR_COUNT; i++) {
                if(motor[i].isBusy() == true)
                    continue;
                return;
            }
        }
    }

    private void set_motor_power(int motor_index, int direction, double power) {
        double motor_power = power;

        motor[motor_index].setPower(motor_power);
        //telemetry.addData(MOTOR_NAMES[motor_index] + "_power", motor_power);
    }

    private void drive_to_position(int direction, int count, double speed) {
        try {
            for(int i=0; i < MOTOR_COUNT; i++) {
                motor[i].setMode(DcMotor.RunMode.RUN_TO_POSITION);
                motor[i].setTargetPosition((int)((double)count * DRIVE_DIRECTIONS[direction][i]));
                set_motor_power(i, direction, 0.25 * speed * DRIVE_DIRECTIONS[direction][i]);
            }
            telemetry.update();

            Thread.sleep(50);
            for(int i=0; i < MOTOR_COUNT; i++) {
                set_motor_power(i, direction, 0.50 * speed * DRIVE_DIRECTIONS[direction][i]);
            }
            telemetry.update();

            Thread.sleep(10);
            for(int i=0; i < MOTOR_COUNT; i++) {
                set_motor_power(i, direction, 1.00 * speed * DRIVE_DIRECTIONS[direction][i]);
            }
            telemetry.update();

        } catch(InterruptedException e) {
        };
    }

    private void drive_distance_start(int direction, double distance, double speed) {
        double slippage = 1.0;
        if(direction == STRAFE_LEFT || direction == STRAFE_RIGHT)
            slippage = STRAFE_SLIPPAGE_FACTOR;
        drive_to_position(direction, distance_to_count(distance, slippage), speed);
    }

    private void drive_distance(int direction, double distance, double speed) {
        double slippage = 1.0;
        if(direction == STRAFE_LEFT || direction == STRAFE_RIGHT)
            slippage = STRAFE_SLIPPAGE_FACTOR;
        drive_to_position(direction, distance_to_count(distance, slippage), speed);
        wait_for_one_stop();
        stop_all_motors();
    }

    public void robotInit() {
        motor = new DcMotor[MOTOR_COUNT];

        for(int i=0; i < MOTOR_COUNT; i++) {
            motor[i] = hardwareMap.dcMotor.get(MOTOR_NAMES[i]);
            motor[i].setDirection(MOTOR_DIRECTIONS[i]);
        }

        color = hardwareMap.colorSensor.get("color");
        color.enableLed(false);

        distance = hardwareMap.opticalDistanceSensor.get("distance");

        stop_all_motors();
    }

    @Override
    public void runOpMode() throws InterruptedException {
        double c1_r, c1_b, c2_r, c2_b;

        robotInit();
        waitForStart();
        if (!isStarted())
            return;
// This next part sets up a function to use strafe right and left for either color.
        int strafe_away, strafe_back;
        if (COLOR_TEAM == COLOR_RED) {
            strafe_away = STRAFE_RIGHT;
            strafe_back = STRAFE_LEFT;
        } else {
            strafe_away = STRAFE_LEFT;
            strafe_back = STRAFE_RIGHT;
        }
// This is where we first align the robot to hit the First beacon.  Change if needed.
        drive_distance(strafe_away, 38.0, 0.6);
        drive_distance(DRIVE_FORWARD, 33.0, 0.6);
        drive_distance(strafe_away, 14.5, 0.6);
        drive_distance(DRIVE_FORWARD, 3.0, 0.4);

        int color_samples = 100;

        drive_distance(strafe_away, 4.5, 0.2);
        c1_r = 0.0; c1_b = 0.0;
        for(int i=0; i < color_samples; i++) {
            c1_r += color.red();
            c1_b += color.blue();
            Thread.sleep(2);
        }
        c1_r /= color_samples;
        c1_b /= color_samples;



        drive_distance(strafe_away, 5.0, 0.2);

        c2_r = 0.0; c2_b = 0.0;
        for(int i=0; i < color_samples; i++) {
            c2_r += color.red();
            c2_b += color.blue();
            Thread.sleep(2);
        }
        c2_r /= color_samples;
        c2_b /= color_samples;

// b1=first beacon, b2=second beacon
        int b1_color, b2_color;
        if(c1_b > c1_r)
            b1_color = COLOR_BLUE;
        else if(c1_r > c1_b)
            b1_color = COLOR_RED;
        else
            b1_color = COLOR_UNKNOWN;

        if(c2_b > c2_r)
            b2_color = COLOR_BLUE;
        else if(c2_r > c2_b)
            b2_color = COLOR_RED;
        else
            b2_color = COLOR_UNKNOWN;

        telemetry.addData("b1_col", COLOR_NAMES[b1_color]);
        telemetry.addData("b2_col", COLOR_NAMES[b2_color]);
        telemetry.update();

/* This next part will strafe past the beacon then figure out which beacon button
   to push.  Once beacon button is pushed it will drive backward, strafe to ramp
   and drive forward onto ramp
  */
        if(b1_color == COLOR_TEAM) {
            drive_distance(strafe_back, 8.0, 0.2);
            drive_distance(DRIVE_FORWARD, 2.0, 0.2);
            drive_distance(DRIVE_BACKWARD, 8.5, 0.2);
            drive_distance(strafe_back, 43.0, 0.6);
            drive_distance(DRIVE_FORWARD, 4.0, 0.4);

        } else if(b2_color == COLOR_TEAM) {
            drive_distance(strafe_back, 3.0, 0.2);
            drive_distance(DRIVE_FORWARD, 2.0, 0.2);
            drive_distance(DRIVE_BACKWARD, 8.5, 0.2);
            drive_distance(strafe_back, 46.0, 0.6);
            drive_distance(DRIVE_FORWARD, 4.0, 0.4);
        }
/* This is going forward towards second beacon, change this if to close or to far away
        drive_distance(DRIVE_FORWARD, 3.5, 0.4);

        // Drive past beacon checking for colors
        drive_distance(strafe_away, 4.5, 0.2);
        c1_r = 0.0; c1_b = 0.0;
        for(int i=0; i < color_samples; i++) {
            c1_r += color.red();
            c1_b += color.blue();
            Thread.sleep(2);
        }
        c1_r /= color_samples;
        c1_b /= color_samples;

        drive_distance(strafe_away, 5.0, 0.2);

        c2_r = 0.0; c2_b = 0.0;
        for(int i=0; i < color_samples; i++) {
            c2_r += color.red();
            c2_b += color.blue();
            Thread.sleep(2);
        }
        c2_r /= color_samples;
        c2_b /= color_samples;

        if(c1_b > c1_r)
            b1_color = COLOR_BLUE;
        else if(c1_r > c1_b)
            b1_color = COLOR_RED;
        else
            b1_color = COLOR_UNKNOWN;

        if(c2_b > c2_r)
            b2_color = COLOR_BLUE;
        else if(c2_r > c2_b)
            b2_color = COLOR_RED;
        else
            b2_color = COLOR_UNKNOWN;

        telemetry.addData("b1_col", COLOR_NAMES[b1_color]);
        telemetry.addData("b2_col", COLOR_NAMES[b2_color]);
        telemetry.update();

        if(b1_color == COLOR_TEAM) {
            drive_distance(strafe_back, 8.0, 0.2);
            drive_distance(DRIVE_FORWARD, 2.0, 0.2);
            Thread.sleep(50);

        } else if(b2_color == COLOR_TEAM) {
            drive_distance(strafe_away, 3.0, 0.2);
            drive_distance(DRIVE_FORWARD, 2.0, 0.2);
            Thread.sleep(50);
        }

        // Drive back to ramp.
        drive_distance(DRIVE_BACKWARD, 8.0, 0.4);
        drive_distance(strafe_back, 84.0, 0.8);
*/
        idle();
    }

    // Stop.
}


