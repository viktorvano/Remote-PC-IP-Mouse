package eu.cyberpunktech.remote.mouse;

import androidx.annotation.NonNull;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

class Sender extends Thread
{
    private boolean active;
    private String IP;
    private int port;
    private String message;
    private int periodMillis;

    public Sender(String serverIP, int serverPort, int periodMillis)
    {
        this.IP = serverIP;
        this.active = true;
        this.message = "";
        this.port = serverPort;
        this.periodMillis = periodMillis;
    }

    public void stopThread()
    {
        active = false;
    }

    public void setMessage(@NonNull String message)
    {
        this.message = message;
    }

    @Override
    public void run() {
        super.run();
        while(active)
        {
            try{
                Thread.sleep(this.periodMillis);
            }catch (Exception e)
            {
                e.printStackTrace();
            }
            sendDataToServer(this.IP, this.message);
        }
    }

    private void sendDataToServer(String IP, String message)
    {
        try
        {
            // need host and port, we want to connect to the ServerSocket at port 7777
            Socket socket = new Socket();
            socket.setSoTimeout(800);
            socket.connect(new InetSocketAddress(IP, 8080), 800);
            System.out.println("Connected!");

            // get the output stream from the socket.
            OutputStream outputStream = socket.getOutputStream();
            // create a data output stream from the output stream so we can send data through it
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

            System.out.println("Sending string to the ServerSocket");

            // write the message we want to send
            dataOutputStream.writeUTF(message);
            dataOutputStream.flush(); // send the message
            dataOutputStream.close(); // close the output stream when we're done.

            System.out.println("Closing socket.");
            socket.close();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
