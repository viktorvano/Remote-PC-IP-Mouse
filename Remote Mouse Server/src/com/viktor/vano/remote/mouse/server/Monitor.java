package com.viktor.vano.remote.mouse.server;

public class Monitor {
    public double startX;
    public double startY;
    public double resX;
    public double resY;

    public Monitor(double startX, double startY, double resX, double resY)
    {
        this.startX = startX;
        this.startY = startY;
        this.resX = resX;
        this.resY = resY;
    }
}
