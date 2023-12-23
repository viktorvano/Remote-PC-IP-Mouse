package com.viktor.vano.remote.mouse.server;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import static com.viktor.vano.remote.mouse.server.Variables.*;

public class Mouse extends Thread{
    private boolean active = true;
    private MyServer myServer;
    private Robot robot = null;
    private SmoothPointer smoothPointer = null;

    private static boolean RMB_isPressed = false;
    private static boolean LMB_isPressed = false;

    public Mouse()
    {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
            System.out.println("Failed to create a Robot...");
            System.exit(-10);
        }
        myServer = new MyServer(port);
        myServer.start();
        smoothPointer = new SmoothPointer();
        smoothPointer.start();
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
            if(myServer.isMessageReceived())
            {
                parseMessage(myServer.getMessage());
            }
        }
    }

    public void stopMouse()
    {
        this.active = false;
        myServer.stopServer();
        smoothPointer.stopSmoothPointer();
    }

    private void sleep(int milliseconds)
    {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //pitch:0.4098261636144374,yaw:-0.24444444444444444,LMB:false,DoubleClick:false,RMB:false
    private void parseMessage(String message)
    {
        String stringPitch, stringYaw, stringLMB, stringDoubleClick, stringRMB, stringMouseActive;
        String[] strings = message.split(",yaw:");
        strings = strings[0].split("pitch:");
        stringPitch = strings[1];

        strings = message.split(",LMB:");
        strings = strings[0].split(",yaw:");
        stringYaw = strings[1];

        strings = message.split(",DoubleClick:");
        strings = strings[0].split(",LMB:");
        stringLMB = strings[1];

        strings = message.split(",RMB:");
        strings = strings[0].split(",DoubleClick:");
        stringDoubleClick = strings[1];

        strings = message.split(",Active:");
        strings = strings[0].split(",RMB:");
        stringRMB = strings[1];

        strings = message.split(",Active:");
        stringMouseActive = strings[1];

        noCommandCount = 0;
        try {
            mouseActive = Boolean.parseBoolean(stringMouseActive);
        }catch (Exception e)
        {
            mouseActive = false;
        }

        System.out.println("Parsed message:");
        System.out.println("Pitch: " + stringPitch);
        System.out.println("Yaw: " + stringYaw);
        System.out.println("LMB: " + stringLMB);
        System.out.println("DoubleClick: " + stringDoubleClick);
        System.out.println("RMB: " + stringRMB);

        boolean booleanLMB, booleanDoubleClick, booleanRMB;
        try{
            if(mouseActive)
            {
                pitch = Double.parseDouble(stringPitch) * -(1080.0/2) + (1080.0/2);
                yaw = Double.parseDouble(stringYaw) * (1920.0/2) + (1920.0/2);
                booleanLMB = Boolean.parseBoolean(stringLMB);
                booleanDoubleClick = Boolean.parseBoolean(stringDoubleClick);
                booleanRMB = Boolean.parseBoolean(stringRMB);

                if(booleanDoubleClick)
                    doubleClick();

                if(booleanLMB && !LMB_isPressed)
                {
                    robot.mousePress(InputEvent.BUTTON1_MASK);
                    LMB_isPressed = true;
                }
                else if(!booleanLMB && LMB_isPressed)
                {
                    robot.mouseRelease(InputEvent.BUTTON1_MASK);
                    LMB_isPressed = false;
                }

                if(booleanRMB && !RMB_isPressed)
                {
                    robot.mousePress(InputEvent.BUTTON3_MASK);
                    RMB_isPressed = true;
                }
                else if(!booleanRMB && RMB_isPressed)
                {
                    robot.mouseRelease(InputEvent.BUTTON3_MASK);
                    RMB_isPressed = false;
                }

                //robot.mouseMove((int)yaw, (int)pitch);
                //System.out.println("\n\n\nMOUSE: " + (int)yaw + ", " + (int)pitch);
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void moveCursorTo(int x, int y)
    {
        final int targetX = x, targetY = y;
        Point mousePointer = MouseInfo.getPointerInfo().getLocation();
        x = mousePointer.x;
        y = mousePointer.y;
        while(x != targetX || y != targetY)
        {
            if(x < targetX)
                x++;
            else if(x > targetX)
                x--;

            if(y < targetY)
                y++;
            else if(y > targetY)
                y--;

            robot.mouseMove(x, y);
            sleep(10);

            //update cursor location
            mousePointer = MouseInfo.getPointerInfo().getLocation();
            x = mousePointer.x;
            y = mousePointer.y;
            System.out.println("Cursor currently at: " + x + ", " + y);
            System.out.println("Cursor target at: " + targetX + ", " + targetY);
        }
    }

    public void click()
    {
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
    }

    public void doubleClick()
    {
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        sleep(40);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
    }

    public void keyStrokeEnter()
    {
        keyStroke(KeyEvent.VK_ENTER);
    }

    public void windowsDesktop()
    {
        keyStroke(KeyEvent.VK_WINDOWS, KeyEvent.VK_D);
    }

    public void windowsStart()
    {
        keyStroke(KeyEvent.VK_WINDOWS);
    }

    public void closeAppAltF4()
    {
        keyStroke(KeyEvent.VK_ALT, KeyEvent.VK_F4);
    }

    public void typeString(String text)
    {
        for(int i=0; i<text.length(); i++)
        {
            charKeyStroke(text.charAt(i));
            sleep(20);
        }
    }

    private void keyStroke(int key)
    {
        robot.keyPress(key);
        robot.keyRelease(key);
        sleep(10);
    }

    private void keyStroke(int key1, int key2)
    {
        robot.keyPress(key1);
        robot.keyPress(key2);
        sleep(10);
        robot.keyRelease(key1);
        robot.keyRelease(key2);
        sleep(10);
    }

    //Works with ENG US Keyboard Layout
    public void charKeyStroke(char character)
    {
        switch (character) {
            case 'a': keyStroke(KeyEvent.VK_A); break;
            case 'b': keyStroke(KeyEvent.VK_B); break;
            case 'c': keyStroke(KeyEvent.VK_C); break;
            case 'd': keyStroke(KeyEvent.VK_D); break;
            case 'e': keyStroke(KeyEvent.VK_E); break;
            case 'f': keyStroke(KeyEvent.VK_F); break;
            case 'g': keyStroke(KeyEvent.VK_G); break;
            case 'h': keyStroke(KeyEvent.VK_H); break;
            case 'i': keyStroke(KeyEvent.VK_I); break;
            case 'j': keyStroke(KeyEvent.VK_J); break;
            case 'k': keyStroke(KeyEvent.VK_K); break;
            case 'l': keyStroke(KeyEvent.VK_L); break;
            case 'm': keyStroke(KeyEvent.VK_M); break;
            case 'n': keyStroke(KeyEvent.VK_N); break;
            case 'o': keyStroke(KeyEvent.VK_O); break;
            case 'p': keyStroke(KeyEvent.VK_P); break;
            case 'q': keyStroke(KeyEvent.VK_Q); break;
            case 'r': keyStroke(KeyEvent.VK_R); break;
            case 's': keyStroke(KeyEvent.VK_S); break;
            case 't': keyStroke(KeyEvent.VK_T); break;
            case 'u': keyStroke(KeyEvent.VK_U); break;
            case 'v': keyStroke(KeyEvent.VK_V); break;
            case 'w': keyStroke(KeyEvent.VK_W); break;
            case 'x': keyStroke(KeyEvent.VK_X); break;
            case 'y': keyStroke(KeyEvent.VK_Y); break;
            case 'z': keyStroke(KeyEvent.VK_Z); break;
            case 'A': keyStroke(KeyEvent.VK_SHIFT, KeyEvent.VK_A); break;
            case 'B': keyStroke(KeyEvent.VK_SHIFT, KeyEvent.VK_B); break;
            case 'C': keyStroke(KeyEvent.VK_SHIFT, KeyEvent.VK_C); break;
            case 'D': keyStroke(KeyEvent.VK_SHIFT, KeyEvent.VK_D); break;
            case 'E': keyStroke(KeyEvent.VK_SHIFT, KeyEvent.VK_E); break;
            case 'F': keyStroke(KeyEvent.VK_SHIFT, KeyEvent.VK_F); break;
            case 'G': keyStroke(KeyEvent.VK_SHIFT, KeyEvent.VK_G); break;
            case 'H': keyStroke(KeyEvent.VK_SHIFT, KeyEvent.VK_H); break;
            case 'I': keyStroke(KeyEvent.VK_SHIFT, KeyEvent.VK_I); break;
            case 'J': keyStroke(KeyEvent.VK_SHIFT, KeyEvent.VK_J); break;
            case 'K': keyStroke(KeyEvent.VK_SHIFT, KeyEvent.VK_K); break;
            case 'L': keyStroke(KeyEvent.VK_SHIFT, KeyEvent.VK_L); break;
            case 'M': keyStroke(KeyEvent.VK_SHIFT, KeyEvent.VK_M); break;
            case 'N': keyStroke(KeyEvent.VK_SHIFT, KeyEvent.VK_N); break;
            case 'O': keyStroke(KeyEvent.VK_SHIFT, KeyEvent.VK_O); break;
            case 'P': keyStroke(KeyEvent.VK_SHIFT, KeyEvent.VK_P); break;
            case 'Q': keyStroke(KeyEvent.VK_SHIFT, KeyEvent.VK_Q); break;
            case 'R': keyStroke(KeyEvent.VK_SHIFT, KeyEvent.VK_R); break;
            case 'S': keyStroke(KeyEvent.VK_SHIFT, KeyEvent.VK_S); break;
            case 'T': keyStroke(KeyEvent.VK_SHIFT, KeyEvent.VK_T); break;
            case 'U': keyStroke(KeyEvent.VK_SHIFT, KeyEvent.VK_U); break;
            case 'V': keyStroke(KeyEvent.VK_SHIFT, KeyEvent.VK_V); break;
            case 'W': keyStroke(KeyEvent.VK_SHIFT, KeyEvent.VK_W); break;
            case 'X': keyStroke(KeyEvent.VK_SHIFT, KeyEvent.VK_X); break;
            case 'Y': keyStroke(KeyEvent.VK_SHIFT, KeyEvent.VK_Y); break;
            case 'Z': keyStroke(KeyEvent.VK_SHIFT, KeyEvent.VK_Z); break;
            case '`': keyStroke(KeyEvent.VK_BACK_QUOTE); break;
            case '0': keyStroke(KeyEvent.VK_0); break;
            case '1': keyStroke(KeyEvent.VK_1); break;
            case '2': keyStroke(KeyEvent.VK_2); break;
            case '3': keyStroke(KeyEvent.VK_3); break;
            case '4': keyStroke(KeyEvent.VK_4); break;
            case '5': keyStroke(KeyEvent.VK_5); break;
            case '6': keyStroke(KeyEvent.VK_6); break;
            case '7': keyStroke(KeyEvent.VK_7); break;
            case '8': keyStroke(KeyEvent.VK_8); break;
            case '9': keyStroke(KeyEvent.VK_9); break;
            case '-': keyStroke(KeyEvent.VK_MINUS); break;
            case '=': keyStroke(KeyEvent.VK_EQUALS); break;
            case '~': keyStroke(KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_QUOTE); break;
            case '!': keyStroke(KeyEvent.VK_EXCLAMATION_MARK); break;
            case '@': keyStroke(KeyEvent.VK_AT); break;
            case '#': keyStroke(KeyEvent.VK_NUMBER_SIGN); break;
            case '$': keyStroke(KeyEvent.VK_DOLLAR); break;
            case '%': keyStroke(KeyEvent.VK_SHIFT, KeyEvent.VK_5); break;
            case '^': keyStroke(KeyEvent.VK_CIRCUMFLEX); break;
            case '&': keyStroke(KeyEvent.VK_AMPERSAND); break;
            case '*': keyStroke(KeyEvent.VK_ASTERISK); break;
            case '(': keyStroke(KeyEvent.VK_LEFT_PARENTHESIS); break;
            case ')': keyStroke(KeyEvent.VK_RIGHT_PARENTHESIS); break;
            case '_': keyStroke(KeyEvent.VK_UNDERSCORE); break;
            case '+': keyStroke(KeyEvent.VK_PLUS); break;
            case '\t': keyStroke(KeyEvent.VK_TAB); break;
            case '\n': keyStroke(KeyEvent.VK_ENTER); break;
            case '[': keyStroke(KeyEvent.VK_OPEN_BRACKET); break;
            case ']': keyStroke(KeyEvent.VK_CLOSE_BRACKET); break;
            case '\\': keyStroke(KeyEvent.VK_BACK_SLASH); break;
            case '{': keyStroke(KeyEvent.VK_SHIFT, KeyEvent.VK_OPEN_BRACKET); break;
            case '}': keyStroke(KeyEvent.VK_SHIFT, KeyEvent.VK_CLOSE_BRACKET); break;
            case '|': keyStroke(KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_SLASH); break;
            case ';': keyStroke(KeyEvent.VK_SEMICOLON); break;
            case ':': keyStroke(KeyEvent.VK_SHIFT, KeyEvent.VK_SEMICOLON); break;
            case '\'': keyStroke(KeyEvent.VK_QUOTE); break;
            case '"': keyStroke(KeyEvent.VK_QUOTEDBL); break;
            case ',': keyStroke(KeyEvent.VK_COMMA); break;
            case '<': keyStroke(KeyEvent.VK_SHIFT, KeyEvent.VK_COMMA); break;
            case '.': keyStroke(KeyEvent.VK_PERIOD); break;
            case '>': keyStroke(KeyEvent.VK_SHIFT, KeyEvent.VK_PERIOD); break;
            case '/': keyStroke(KeyEvent.VK_SLASH); break;
            case '?': keyStroke(KeyEvent.VK_SHIFT, KeyEvent.VK_SLASH); break;
            case ' ': keyStroke(KeyEvent.VK_SPACE); break;
            default:
                System.out.println("Cannot type this char: " + character + "its numeric value: " + (int)character);
        }
    }
}
