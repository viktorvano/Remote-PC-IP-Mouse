package eu.cyberpunktech.remote.mouse;

import static eu.cyberpunktech.remote.mouse.Variables.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor sensorGravity;
    private double xA, yA, zA;
    private Button buttonDoubleClick, buttonLeft, buttonRight;
    private TextView tvHeading;
    private long oldTime, currentTime;
    private Sender sender;
    private boolean doubleClick;
    private Switch switchActive;
    private Button buttonConfiguration;
    private ConstraintLayout constraintLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        doubleClick = false;

        buttonDoubleClick = findViewById(R.id.buttonDoubleClick);
        buttonLeft = findViewById(R.id.buttonLeft);
        buttonRight = findViewById(R.id.buttonRight);
        tvHeading = findViewById(R.id.tvHeading);
        switchActive = findViewById(R.id.switchActive);
        buttonConfiguration = findViewById(R.id.buttonConfig);
        constraintLayout = findViewById(R.id.constraintLayout);
        oldTime = System.currentTimeMillis();

        loadData();
        int rgb = 255;
        rgb = (rgb << 8) + red;
        rgb = (rgb << 8) + green;
        rgb = (rgb << 8) + blue;
        constraintLayout.setBackgroundColor(rgb);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        if(sensorGravity == null)
        {
            MainActivity activity = this;
            Toast.makeText(activity, "Gravity sensor is not available.", Toast.LENGTH_LONG).show();
            activity.finish();
        }

        buttonDoubleClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    doubleClick = true;
            }
        });

        buttonConfiguration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(getApplicationContext(), ConfigurationActivity.class);
                startActivity(intent);
                finish();
            }
        });

        sender = new Sender(serverIP, 8080, 200);
        sender.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sender.stopThread();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
        // You must implement this callback in your code.
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Don't receive any more updates from either sensor.
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorGravity, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        if(event.sensor.getType() == Sensor.TYPE_GRAVITY)
        {
            xA = event.values[0];
            yA = event.values[1];
            zA = event.values[2];
        }

        double roll = Math.atan2(-xA,zA);
        double pitch = Math.atan2(yA,zA);

        currentTime = System.currentTimeMillis();
        if((currentTime - oldTime) > 100)
        {
            tvHeading.setText("Pitch: " + (int)(pitch*radiansToDegrees) +
                    "°\nRoll: " + (int)(roll*radiansToDegrees) + "°\nIP: " + serverIP);
            double normalizedPitch, normalizedRoll;
            normalizedPitch = (pitch*radiansToDegrees) / pitchRange;
            if(normalizedPitch > 1.0)
                normalizedPitch = 1.0;
            if(normalizedPitch < -1.0)
                normalizedPitch = -1.0;

            normalizedRoll = (roll*radiansToDegrees) / rollRange;
            if(normalizedRoll > 1.0)
                normalizedRoll = 1.0;
            if(normalizedRoll < -1.0)
                normalizedRoll = -1.0;
            sender.setMessage("X:" + normalizedPitch +
                            ",Y:" + normalizedRoll +
                            ",LMB:" + buttonLeft.isPressed() +
                            ",DoubleClick:" + doubleClick +
                            ",RMB:" + buttonRight.isPressed() +
                            ",Active:" + switchActive.isChecked());

            if(doubleClick)
            {
                doubleClick = false;
            }
        }
    }

    public void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(Variables.CONFIGURATION, MODE_PRIVATE);
        Variables.serverIP = sharedPreferences.getString(SERVER_IP, "192.168.1.15");
        Variables.red = sharedPreferences.getInt(COLOR_RED, 255);
        Variables.green = sharedPreferences.getInt(COLOR_GREEN, 255);
        Variables.blue = sharedPreferences.getInt(COLOR_BLUE, 255);
    }
}