package com.viktor.vano.remote.mouse.server;

public class Variables {

    public static final String version = "v20231223";
    public static final int width = 400;
    public static final int height = 250;
    public static boolean mouseActive = false;
    public static final int port = 8080;
    public static double y, x;
    public static int noCommandCount = 0;
    public static final Monitor selectedMonitor = new Monitor(0,0,0,0);
}
