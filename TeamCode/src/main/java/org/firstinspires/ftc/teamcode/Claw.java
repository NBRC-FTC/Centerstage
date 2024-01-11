package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class Claw {
    HardwareMap hardwareMap;
    Servo claw1;
    Servo claw0;

    static final double     CLAW_OPEN    = 0.75;
    static final double     CLAW_CLOSED    = 0.60;

    public Claw (HardwareMap hardwareMap) {
        this.hardwareMap = hardwareMap;
        claw1 = hardwareMap.get(Servo.class, "claw1");
        claw0 = hardwareMap.get(Servo.class, "claw0");
        claw1.setDirection((Servo.Direction.REVERSE));

        claw0.setPosition(CLAW_OPEN);
        claw1.setPosition(CLAW_OPEN);
    }

    public void closeClaw(){
        claw0.setPosition(CLAW_CLOSED);
        claw1.setPosition(CLAW_CLOSED);
    }

    public void openClaw(){
        claw0.setPosition(CLAW_OPEN);
        claw1.setPosition(CLAW_OPEN);
    }
}