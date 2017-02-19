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
import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cRangeSensor;
import com.qualcomm.robotcore.hardware.I2cAddr;

// TODO(jeremycole): All the loops here should ensure they exit when the timer expires.

public class Mecanum_Wheels_Generic extends LinearOpMode {
    // Numer of encoder counts per wheel revolution.
    final private static int ENCODER_CPR = 1120;

    // Gear Ratio; 1:1 - Direct Drive.
    final private static double GEAR_RATIO = 1.0;

    // Diameter of the wheels, in inches.
    final private static double WHEEL_DIAMETER = 3.937;

    // Circumference of the wheels, in inches.
    final private static double WHEEL_CIRCUMFERENCE = Math.PI * WHEEL_DIAMETER;

    // The factor of slippage of wheels when strafing. Measured to be about 8%.
    final private static double STRAFE_SLIPPAGE_FACTOR = 1.08;

    // The number of color samples to read.
    // TODO(jeremycole): Tune to save a bit of time. Checking 100 samples is probably overkill.
    final private static int COLOR_SAMPLE_COUNT = 100;

    // The amount of time to sleep in between color samples.
    final private static int COLOR_SAMPLE_SLEEP = 2;

    // The difference in brightness from dark floor mat to white tape.
    // TODO(jeremycole): Adjust once the Tape_color sensor is lowered.
    final public static double TAPE_ALPHA_DIFFERENCE = 2.0;

    // Each of the colors we need to know about, only red and blue.
    // TODO(jeremycole): This could probably be an enum.
    final public static int COLOR_UNKNOWN = 0;
    final public static int COLOR_RED = 1;
    final public static int COLOR_BLUE = 2;
    final public static String[] COLOR_NAMES = {
            "unknown", "red", "blue"
    };

    // Each of the motors on the robot.
    // TODO(jeremycole): This could probably be an enum.
    final private static int MOTOR_COUNT = 4;
    final private static int mFL = 0;
    final private static int mFR = 1;
    final private static int mBL = 2;
    final private static int mBR = 3;
    final private static String[] MOTOR_NAMES = {
            "mFL", "mFR", "mBL", "mBR"
    };

    // The direction that each motor on the robot is oriented. The right-side motors are mounted
    // backwards relative to the left side ones.
    final private static DcMotorSimple.Direction MOTOR_DIRECTIONS[] = {
            DcMotor.Direction.FORWARD, // mFL
            DcMotor.Direction.REVERSE, // mFR
            DcMotor.Direction.FORWARD, // mBL
            DcMotor.Direction.REVERSE, // mBR
    };

    // Each driving direction supported by driving functions.
    // TODO(jeremycole): This could probably be an enum.
    final public static int DRIVE_FORWARD  = 0;
    final public static int DRIVE_BACKWARD = 1;
    final public static int TURN_LEFT      = 2;
    final public static int TURN_RIGHT     = 3;
    final public static int STRAFE_LEFT    = 4;
    final public static int STRAFE_RIGHT   = 5;

    // This serves as both a matrix of the motor directions needed to drive in each direction
    // which is supported (via the sign, + or -) as well as a correction table to allow driving
    // straighter in each supported direction. Reduce the values here to slow the motors slightly
    // or increase the values to speed them up.
    final private static double DRIVE_DIRECTIONS[][] = {
            // mFL,  mFR,   mBL,   mBR
            { +1.00, +1.00, +1.00, +1.00 }, // DRIVE_FORWARD
            { -1.00, -1.00, -1.00, -1.00 }, // DRIVE_BACKWARD
            { +1.00, -1.00, +1.00, -1.00 }, // TURN_LEFT
            { -1.00, +1.00, -1.00, +1.00 }, // TURN_RIGHT
            { -1.00, +1.00, +1.00, -1.00 }, // STRAFE_LEFT
            { +1.00, -0.92, -0.92, +1.04 }, // STRAFE_RIGHT
    };

    // An array of DcMotors to represent all of the motors.
    DcMotor motor[];

    // The front-facing beacon color sensor.
    ColorSensor Beacon_color;

    // The bottom-mounted floor/tape color sensor.
    ColorSensor Tape_color;

    // The front-facing range (ultrasonic + optical) sensor.
    ModernRoboticsI2cRangeSensor range;

    // Convert a distance, in inches, into an encoder count, including a wheel slippage correction
    // factor.
    public int distance_to_count(double distance, double slippage) {
        return((int)(slippage * ENCODER_CPR * (distance / WHEEL_CIRCUMFERENCE) * GEAR_RATIO));
    }

    // Stop all motors immediately.
    public void stop_all_motors() {
        for(int i=0; i < MOTOR_COUNT; i++) {
            motor[i].setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        }
    }

    // Set the power of a given motor.
    public void set_motor_power(int motor_index, int direction, double power) {
        motor[motor_index].setPower(power);
    }

    // Check if at least one motor is stopped (because it has reached its desired position).
    public boolean one_motor_stopped() {
        for(int i=0; i < MOTOR_COUNT; i++) {
            if(motor[i].isBusy() != true)
                return true;
        }
        return false;
    }

    // Wait for at least one motor to stop.
    public void wait_for_one_stop() {
        while(true) {
            if (one_motor_stopped())
                return;
        }
    }

    // Check if all motors are stopped (because they have all reached their desired positions).
    public boolean all_motors_stopped() {
        for(int i=0; i < MOTOR_COUNT; i++) {
            if(motor[i].isBusy() == true)
                return false;
        }
        return true;
    }

    // Wait for all motors to stop.
    public void wait_for_all_stop() {
        while(true) {
            if (all_motors_stopped())
                return;
        }
    }

    // Check if at least one encoder has reached its desired position.
    public boolean one_encoder_satisfied() {
        for(int i=0; i < MOTOR_COUNT; i++) {
            if(motor[i].getCurrentPosition() >= motor[i].getTargetPosition())
                return true;
        }
        return false;
    }

    // Wait for at least one encoder to have reached its desired position.
    public void wait_for_one_encoder_satisfied() {
        while(true) {
            if (one_encoder_satisfied())
                return;
        }
    }

    // Drive in a given direction at a given speed until at least one encoder reaches the
    // given count.
    public void drive_to_position(int direction, int count, double speed) {
        try {
            for(int i=0; i < MOTOR_COUNT; i++) {
                motor[i].setMode(DcMotor.RunMode.RUN_TO_POSITION);
                motor[i].setTargetPosition((int)((double)count * DRIVE_DIRECTIONS[direction][i]));
                set_motor_power(i, direction, 0.20 * speed * DRIVE_DIRECTIONS[direction][i]);
            }
            telemetry.update();

            Thread.sleep(100);
            for(int i=0; i < MOTOR_COUNT; i++) {
                set_motor_power(i, direction, 0.50 * speed * DRIVE_DIRECTIONS[direction][i]);
            }
            telemetry.update();

            Thread.sleep(30);
            for(int i=0; i < MOTOR_COUNT; i++) {
                set_motor_power(i, direction, 1.00 * speed * DRIVE_DIRECTIONS[direction][i]);
            }
            telemetry.update();

        } catch(InterruptedException e) {
            // Do nothing if we returned from sleep early.
        };
    }

    // Start driving in a given direction at a given speed for a maximum of the given distance,
    // but return immediately rather than waiting to reach the position.
    public void drive_distance_start(int direction, double distance, double speed) {
        double slippage = 1.0;
        if(direction == STRAFE_LEFT || direction == STRAFE_RIGHT)
            slippage = STRAFE_SLIPPAGE_FACTOR;
        drive_to_position(direction, distance_to_count(distance, slippage), speed);
    }

    // Drive in a given direction at a given speed until reaching the given distance.
    public void drive_distance(int direction, double distance, double speed) {
        drive_distance_start(direction, distance, speed);
        wait_for_one_stop();
        stop_all_motors();
    }

    /*
        TODO(jeremycole): Tune drive_distance_without_stopping.
        This should ask the motors to drive slightly further and returns once the distance
        requested has been driven, so that the next driving move can be made without stopping.
        Optimizing this should save something like 500ms or so per move.
    */

    // Drive in a given direction at a given speed until reaching a given distance, but don't
    // stop, so that the next move can be transitioned into very smoothly.
    // TODO(jeremycole): This doesn't actually work yet. The encoder won't be quite satisfied.
    public void drive_distance_without_stopping(int direction, double distance, double speed) {
        drive_distance_start(direction, distance + 2.0, speed);
        wait_for_one_encoder_satisfied();
    }

    // Drive towards a wall in a given direction at a given speed until reaching the desired range.
    // As a safety measure, stop after max_distance to avoid crashing too badly.
    // TODO(jeremycole): Since the sensor is known to be front-mounted, this probably doesn't need
    //                   the direction argument to be passed at all. It will always be forward.
    public void drive_until_lt_range(int direction, double desired_range, double max_distance, double speed) {
        drive_distance_start(direction, max_distance, speed);
        while(!one_motor_stopped()) {
            double current_range = (range.cmUltrasonic() / 2.54);
            if(current_range <= desired_range)
                break;
        }
        stop_all_motors();
    }

    // Drive away from a wall in a given direction at a given speed until reaching the desired
    // range. As a safety measure, stop after max_distance to avoid crashing too badly.
    // TODO(jeremycole): Since the sensor is known to be front-mounted, this probably doesn't need
    //                   the direction argument to be passed at all. It will always be backward.
    public void drive_until_gt_range(int direction, double desired_range, double max_distance, double speed) {
        drive_distance_start(direction, max_distance, speed);
        while(!one_motor_stopped()) {
            double current_range = (range.cmUltrasonic() / 2.54);
            if(current_range >= desired_range)
                break;
        }
        stop_all_motors();
    }

    // Drive in a given direction until the bottom-mounted color sensor sees an alpha greater than
    // the given desired_alpha (such as a tape line). As a safety measure, stop after max_distance
    // regardless to avoid crashing too badly.
    public void drive_until_gt_alpha(int direction, double desired_alpha, double max_distance, double speed) {
        drive_distance_start(direction, max_distance, speed);
        while(!one_motor_stopped()) {
            double current_alpha = Tape_color.alpha();
            if(current_alpha >= desired_alpha)
                break;
        }
        stop_all_motors();
    }

    // Figure out the color (red or blue) that the beacon color sensor is seeing. In order to
    // get a somewhat more accurate color reading, multiple readings are taken and averaged.
    public int read_beacon_color() {
        // Accumulator variables for red and blue values.
        double r = 0.0, b = 0.0;

        // Loop reading from the color sensor accumulating the readings into r and b.
        for(int i=0; i < COLOR_SAMPLE_COUNT; i++) {
            // Accumulate a sum for each color.
            r += Beacon_color.red();
            b += Beacon_color.blue();

            // Sleep between samples to allow the sensor to see something new.
            try {
                Thread.sleep(COLOR_SAMPLE_SLEEP);
            } catch (InterruptedException e) {
                // Ignore the canceled sleep since we don't care too much about the amount of time
                // that we actually slept.
            }
        }

        // Produce an average red and blue value by dividing the accumulated sum in the r and b
        // variables by the number of samples taken.
        r /= COLOR_SAMPLE_COUNT;
        b /= COLOR_SAMPLE_COUNT;

        if(b > r)       // The sensor saw more blue than red.
            return COLOR_BLUE;
        else if(r > b)  // The sensor saw more red than blue.
            return COLOR_RED;

        // Most likely, we didn't get blue *or* red data, or they were the same value, so it is
        // not possible to tell the color.
        return COLOR_UNKNOWN;
    }

    // Initialize the robot and all its sensors.
    public void robotInit() {
        // Initialize all motors in a loop.
        motor = new DcMotor[MOTOR_COUNT];
        for(int i=0; i < MOTOR_COUNT; i++) {
            motor[i] = hardwareMap.dcMotor.get(MOTOR_NAMES[i]);
            motor[i].setDirection(MOTOR_DIRECTIONS[i]);
        }

        // Make sure everything starts out stopped.
        stop_all_motors();

        // Initialize the front-facing beacon color sensor.
        Beacon_color = hardwareMap.colorSensor.get("Beacon_color");
        Beacon_color.setI2cAddress(I2cAddr.create8bit(0x3c));
        Beacon_color.enableLed(false);

        // Initialize the bottom-mounted tape color sensor.
        Tape_color = hardwareMap.colorSensor.get("Tape_color");
        Tape_color.setI2cAddress(I2cAddr.create8bit(0x3a));
        Tape_color.enableLed(true);

        // Initialize the front-facing range sensor.
        range = hardwareMap.get(ModernRoboticsI2cRangeSensor.class, "range");
    }

    @Override
    public void runOpMode() throws InterruptedException {
        // Do nothing. This should be overridden in a subclass.
    }
}

