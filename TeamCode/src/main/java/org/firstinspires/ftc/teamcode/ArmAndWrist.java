package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class ArmAndWrist {
    DcMotor arm_drive;
    DcMotor wrist_drive;
    int wristPos;
    int armPos;
    HardwareMap hardwareMap;

    public ArmAndWrist(HardwareMap hardwareMap){

        this.hardwareMap = hardwareMap;
        arm_drive = hardwareMap.get(DcMotor.class,"arm_drive");
        wrist_drive = hardwareMap.get(DcMotor.class, "wrist_drive");

        wrist_drive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        wristPos = 0;
        wrist_drive.setDirection(DcMotor.Direction.REVERSE);

        arm_drive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        armPos = 0;
        arm_drive.setDirection(DcMotor.Direction.REVERSE);
    }
    public void startPosition (){
        wristPos = 0;
        armPos = 0;
        moveWrist();
        moveArm();
    }
    public void carryPosition(){
        wristPos = 0;
        armPos = 180;
        moveWrist();
        moveArm();
    }

    private void moveWrist(){
        wrist_drive.setTargetPosition(wristPos);
        wrist_drive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        wrist_drive.setPower(0.15);
    }

    private void moveArm(){
        arm_drive.setTargetPosition(armPos);
        arm_drive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        arm_drive.setPower(0.15);
    }

}
