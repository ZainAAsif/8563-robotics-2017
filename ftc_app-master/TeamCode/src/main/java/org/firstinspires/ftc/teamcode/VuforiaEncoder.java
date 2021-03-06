package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.navigation.RelicRecoveryVuMark;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;
import org.firstinspires.ftc.teamcode.Hardware.Motors;

/*
 * Created by zain- on 10/1/2017.
 */

@Disabled
@Autonomous(name="VuforiaEncoder")
public class VuforiaEncoder extends LinearOpMode {

// Declares robot object to get information from DriveMotors.java
    Motors robot = new Motors();

    private ElapsedTime     runtime = new ElapsedTime();

   RelicRecoveryVuMark vuMark;

// Setting statistics of the motors/robot & doing basic math required in the program
    static final double     COUNTS_PER_MOTOR_REV    = 1440 ;
    static final double     DRIVE_GEAR_REDUCTION    = 1.0 ;
    static final double     WHEEL_DIAMETER_INCHES   = 5 ;
    static final double     COUNTS         = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) / (WHEEL_DIAMETER_INCHES * Math.PI);
    static final double     DRIVE_SPEED             = .6;
    static final double     TURN_SPEED              = 0.5;

    public static final String TAG = "Vuforia Test";

    VuforiaLocalizer vuforia;

    @Override
    public void runOpMode() throws InterruptedException {

// Pulls hardware from robot object
        robot.init(hardwareMap);

// Updates telemetry to debug/make sure encoders reset
        telemetry.addData("Status", "Resetting Encoders");
        telemetry.update();

// Making sure encoders are at 0 position
//        robot.left.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
//        robot.right.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

//        robot.left.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
//        robot.right.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

//      Telemetry debugging to find out if encoders didn't reset
//        telemetry.addData("Path0", "Starting at %7d :%7d",
//                robot.left.getCurrentPosition(),
//                robot.right.getCurrentPosition());
//        telemetry.update();

        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters(cameraMonitorViewId);

        parameters.vuforiaLicenseKey = "AXwlB+P/////AAAAGW96tnY1DkbXgYvfIAP7K01bMwKO6qkYdaF3ZVJS9Ln1WMSI2fTzpeO0aFDeRvKk/BJaqDdAppbDU1BZO2psTZH7YrMhdKMzK7DYYVQXizfnupwhetlWGPf3pf5JuHDZaBls+I1v9gfTaVIwTYqNoDezto+Tw5pAr7kF9r8lasrl+7xbsD0kmGqtx75aV/ddgCNJxWWWNXMb3ajSe35888+5SjfJKeLaQjLGbWMF6wMU42ZQVZJ5kyXSfh80wpJcd7pTdZ/yXve0G+EJNaX7v+FkB3y1fkIR0REoYHDUct/a2z2dhXou8EYQZUHDoghUNQnyDfy7nMaL4sxoqsS9fsf9AxVtCAWkpKQiWmruLswx";

        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;
        this.vuforia = ClassFactory.createVuforiaLocalizer(parameters);

        VuforiaTrackables relicTrackables = this.vuforia.loadTrackablesFromAsset("RelicVuMark");
        VuforiaTrackable relicTemplate = relicTrackables.get(0);
        relicTemplate.setName("relicVuMarkTemplate");

        waitForStart();

        relicTrackables.activate();

        //If there is a visible VuMark in the camera, it will return either LEFT, CENTER, or RIGHT
        while (opModeIsActive()) {
            vuMark = RelicRecoveryVuMark.from(relicTemplate);
            if (vuMark != RelicRecoveryVuMark.UNKNOWN) {

                //If a VuMark is visible, it will tell you which column on the driver station.
                telemetry.addData("VuMark", "%s visible", vuMark);
            }

            else {
                telemetry.addData("VuMark", "Not Visible");
            }
            telemetry.update();

            //Reverse movement is entered as a negative value

            //Drives 48 inches in a straight line, times out after 6 seconds.
            if (vuMark == RelicRecoveryVuMark.LEFT) {
                encoderDrive(DRIVE_SPEED, 48, 48, 6.0);
            }
//        sleep(2000);
//        encoderDrive(TURN_SPEED, -4, 4, 5);
//        sleep(2000);
//        encoderDrive(DRIVE_SPEED, 48, 48, 5);
//        sleep(2000);

//Turns right for 12 inches of wheel movement, then waits for 5 seconds
// encoderDrive(TURN_SPEED, 12, -12, 4.0);
//        telemetry.addData("Path", "Complete");
//        telemetry.update();
        }
    }

//Creates an encoderDrive object to use in programs, having it do the math for you with a few basic inputs.
    public void encoderDrive(double speed,
                             double leftInches, double rightInches,
                             double timeoutS) {
// Declaring Integers
        int newLeftTarget;
        int newRightTarget;

// Loops as long as OpMode is running
        if (opModeIsActive()) {
// Uses math stated previously to calculate counts to move
            newLeftTarget = robot.left.getCurrentPosition() + (int)(leftInches * COUNTS);
            newRightTarget = robot.right.getCurrentPosition() + (int)(rightInches * COUNTS);


// Sets encoder target position to new calculated position
            robot.left.setTargetPosition(newLeftTarget);
            robot.right.setTargetPosition(newRightTarget);

// Runs the motors to get to the desired target position
            robot.left.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.right.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            runtime.reset();

// Sets the power of the motors to the absolute value of the speed
            robot.left.setPower(Math.abs(speed));
            robot.right.setPower(Math.abs(speed));

// Loops as long as OpMode is running
            while (opModeIsActive() &&

// Updates telemetry with new positions and current positions as long as the code and both motors are still running
                    (runtime.seconds() < timeoutS) &&
                    (robot.left.isBusy()&& robot.right.isBusy())) {

// Displays new target positions on the telemetry for debugging
                telemetry.addData("Path1",  "Running to %7d :%7d", newLeftTarget,  newRightTarget);
                telemetry.addData("Path2",  "Running at %7d :%7d",
                        robot.left.getCurrentPosition(),
                        robot.right.getCurrentPosition());
                telemetry.update();
            }

// Sets the power of the motor to 0 to make sure there is no movement
            robot.left.setPower(0);
            robot.right.setPower(0);

            robot.left.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            robot.right.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

            sleep(250);
        }
    }
}
    