package com.viktor.vano.remote.mouse.server;

import java.awt.*;

import static com.viktor.vano.remote.mouse.server.Variables.*;

public class SmoothPointer extends Thread{
    private boolean active = true;
    private double[] pitchArray, yawArray;
    private final int filterSize = 40;
    private double pitch, yaw;
    private final double smoothingFactor = 0.97;
    private final double newValueWeight = 1.0-smoothingFactor;
    private Robot robot = null;

    public SmoothPointer()
    {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
            System.out.println("Failed to create a Robot...");
            System.exit(-22);
        }
        this.pitch = 0.0;
        this.yaw = 0.0;
        this.pitchArray = new double[filterSize];
        this.yawArray = new double[filterSize];
    }

    public void stopSmoothPointer()
    {
        this.active = false;
    }

    @Override
    public void run() {
        super.run();
        while (active)
        {
            try {
                Thread.sleep(10);
            }catch (Exception e){
                e.printStackTrace();
            }

            for(int i=1; i<this.filterSize; i++)
            {
                this.pitchArray[i-1] = this.pitchArray[i];
                this.yawArray[i-1] = this.yawArray[i];
            }

            this.pitchArray[filterSize-1] = Variables.x;
            this.yawArray[filterSize-1] = Variables.y;

            this.pitch = 0.0;
            this.yaw = 0.0;
            for(int i=0; i<this.filterSize; i++)
            {
                this.pitch += this.pitchArray[i];
                this.yaw += this.yawArray[i];
            }
            this.pitch /= (double)filterSize;
            this.yaw /= (double)filterSize;
            //this.pitch = this.smoothingFactor*this.pitch + this.newValueWeight*Variables.pitch;
            //this.yaw = this.smoothingFactor*this.yaw + this.newValueWeight*Variables.yaw;

            noCommandCount++;
            if(noCommandCount > 300)
                mouseActive = false;

            if(mouseActive)
                robot.mouseMove((int)this.yaw, (int)this.pitch);
        }
    }
}
