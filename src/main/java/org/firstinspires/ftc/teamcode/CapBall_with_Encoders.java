package org.firstinspires.ftc.teamcode;

/**
 * Created FTC Team 11574 1/20/2017.
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
@Autonomous(name="CapBall with Encoders", group="Autonomous")
public class CapBall_with_Encoders extends LinearOpMode {
    final private static int ENCODER_CPR = 1120;  // Encoder Counters per Revolution
    final private static double GEAR_RATIO = 1.0;   // Gear Ratio - 1:1 - Direct Drive
    final private static double WHEEL_DIAMETER = 3.937;  // Diameter of the wheel in inches
    final private static double WHEEL_CIRCUMFERENCE = Math.PI * WHEEL_DIAMETER;
    final private static double STRAFE_SLIPPAGE_FACTOR = 1.08;

    final private static int COLOR_UNKNOWN = 0;
    final private static int COLOR_RED = 1;
    final private static int COLOR_BLUE = 2;
    final private static String[] COLOR_NAMES = {
            "unknown", "red", "blue"
    };

    // Change this to COLOR_BLUE or COLOR_RED to choose your alliance.
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

        Thread.sleep(10000);   // Waits ten seconds and then starts the program.

  // Drive to push CapBall and Park.
        drive_distance(DRIVE_FORWARD, 64.0, 0.8);
        drive_distance(DRIVE_BACKWARD, 2.0, 0.2);
    }}