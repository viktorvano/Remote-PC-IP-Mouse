package com.viktor.vano.remote.mouse.server;

import java.awt.*;

import static com.viktor.vano.remote.mouse.server.Variables.*;

public class SmoothPointer extends Thread{
    private boolean active = true;
    private double[] yArray, xArray;
    private final int filterSize = 40;
    private double Y, X;
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
        this.Y = 0.0;
        this.X = 0.0;
        this.yArray = new double[filterSize];
        this.xArray = new double[filterSize];
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
                this.yArray[i-1] = this.yArray[i];
                this.xArray[i-1] = this.xArray[i];
            }

            this.yArray[filterSize-1] = Variables.y;
            this.xArray[filterSize-1] = Variables.x;

            this.Y = 0.0;
            this.X = 0.0;
            for(int i=0; i<this.filterSize; i++)
            {
                this.Y += this.yArray[i];
                this.X += this.xArray[i];
            }
            this.Y /= (double)filterSize;
            this.X /= (double)filterSize;

            noCommandCount++;
            if(noCommandCount > 300)
                mouseActive = false;

            if(mouseActive)
                robot.mouseMove((int)this.X, (int)this.Y);
        }
    }
}
