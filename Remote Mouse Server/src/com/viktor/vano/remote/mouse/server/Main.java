package com.viktor.vano.remote.mouse.server;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.awt.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

import static com.viktor.vano.remote.mouse.server.Variables.*;

public class Main extends Application {
    private Pane pane;
    private Label labelIP;
    private RadioButton[] radioButtons;
    private final ToggleGroup monitorGroup = new ToggleGroup();
    private Mouse mouse;

    private final ArrayList<Monitor> monitors = new ArrayList<>();
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

        try
        {
            javafx.scene.image.Image icon = new javafx.scene.image.Image(getClass().getResourceAsStream("icon.png"));
            stage.getIcons().add(icon);
            System.out.println("Icon loaded from IDE...");
        }catch(Exception e)
        {
            try
            {
                javafx.scene.image.Image icon = new Image("com/viktor/vano/remote/mouse/server/icon.png");
                stage.getIcons().add(icon);
                System.out.println("Icon loaded from exported JAR...");
            }catch(Exception e1)
            {
                System.out.println("Icon failed to load...");
            }
        }

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
            radioButtons[i].setToggleGroup(monitorGroup);
            pane.getChildren().add(radioButtons[i]);
        }

        for(GraphicsDevice curGs : gs)
        {
            GraphicsConfiguration[] gc = curGs.getConfigurations();

            for(GraphicsConfiguration curGc : gc)
            {
                Rectangle bounds = curGc.getBounds();
                System.out.println("Monitor index: " + x);
                if(radioButtons[x] != null)
                {
                    radioButtons[x].setText("Monitor " + x + ": " + (int)bounds.getWidth() + "x" + (int)bounds.getHeight());
                    System.out.println("Start Coordinates: " + bounds.getX() + "," + bounds.getY() + " Resolution: " + bounds.getWidth() + "x" + bounds.getHeight());
                    monitors.add(new Monitor(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight()));
                }
                else
                    System.out.println("RadioButton[" + x + "] is null.");
            }
            x++;
        }

        Monitor allMonitors = new Monitor(monitors.get(0).startX, monitors.get(0).startY, monitors.get(0).resX, monitors.get(0).resY);
        for(Monitor monitor : monitors)
        {
            if(monitor.startX < allMonitors.startX)
            {
                allMonitors.startX = monitor.startX;
            }

            if(monitor.startY < allMonitors.startY)
            {
                allMonitors.startY = monitor.startY;
            }

            if(monitor.resX > allMonitors.resX)
            {
                allMonitors.resX = monitor.resX;
            }

            if(monitor.resY > allMonitors.resY)
            {
                allMonitors.resY = monitor.resY;
            }
        }
        monitors.add(allMonitors);

        radioButtons[0].setSelected(true);
        selectMonitor(0);

        monitorGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.toString().contains("All Monitors"))
            {
                selectMonitor(monitors.size() - 1);
            }else
            {
                try{
                    String[] strings = newValue.toString().split("Monitor ");
                    strings = strings[1].split(": ");
                    selectMonitor(Integer.parseInt(strings[0]));
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        mouse = new Mouse();
        mouse.start();
    }

    private void selectMonitor(int index)
    {
        selectedMonitor.startX = monitors.get(index).startX;
        selectedMonitor.startY = monitors.get(index).startY;
        selectedMonitor.resX = monitors.get(index).resX;
        selectedMonitor.resY = monitors.get(index).resY;
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        mouse.stopMouse();
    }
}
