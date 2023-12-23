package com.viktor.vano.remote.mouse.server;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.awt.*;
import java.net.DatagramSocket;
import java.net.InetAddress;

import static com.viktor.vano.remote.mouse.server.Variables.*;

public class Main extends Application {
    private Pane pane;
    private Label labelIP;
    private RadioButton[] radioButtons;
    private Mouse mouse;

    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage stage){
        pane = new Pane();

        Scene scene = new Scene(pane, width, height);
        stage.setTitle("Remote Mouse " + version);
        stage.setScene(scene);
        stage.show();
        stage.setMinWidth(stage.getWidth());
        stage.setMinHeight(stage.getHeight());
        stage.setMaxWidth(stage.getWidth());
        stage.setMaxHeight(stage.getHeight());
        stage.setResizable(false);

        labelIP = new Label("");
        labelIP.setLayoutX(50);
        labelIP.setLayoutY(50);
        labelIP.setFont(Font.font("Arial", 20));
        pane.getChildren().add(labelIP);

        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            String ip = socket.getLocalAddress().getHostAddress();
            System.out.println("IP: " + ip);
            labelIP.setText("IP Address: " + ip);
        } catch (Exception e) {
            e.printStackTrace();
        }

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        radioButtons = new RadioButton[gs.length + 1];
        int x = 0;
        for (int i=0; i < radioButtons.length; i++)
        {
            radioButtons[i] = new RadioButton("All Monitors");
            radioButtons[i].setLayoutX(50);
            radioButtons[i].setLayoutY(100 + 25*i);
            pane.getChildren().add(radioButtons[i]);
        }
        radioButtons[0].setSelected(true);
        for(GraphicsDevice curGs : gs)
        {
            GraphicsConfiguration[] gc = curGs.getConfigurations();

            for(GraphicsConfiguration curGc : gc)
            {
                Rectangle bounds = curGc.getBounds();
                System.out.println("Monitor index: " + x);
                if(radioButtons[x] != null)
                    radioButtons[x].setText("Monitor " + x + ": " + (int)bounds.getWidth() + "x" + (int)bounds.getHeight());
                else
                    System.out.println("RadioButton[" + x + "] is null.");
                System.out.println(bounds.getX() + "," + bounds.getY() + " " + bounds.getWidth() + "x" + bounds.getHeight());
            }
            x++;
        }

        mouse = new Mouse();
        mouse.start();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        mouse.stopMouse();
    }
}
