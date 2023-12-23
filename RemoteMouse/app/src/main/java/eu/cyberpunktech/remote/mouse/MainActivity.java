package eu.cyberpunktech.remote.mouse;

import static eu.cyberpunktech.remote.mouse.Variables.*;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
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
    private Sensor sensorMagneticField, sensorGravity;
    private double xA, yA, zA;
    private Button buttonDoubleClick, buttonLeft, buttonRight;
    private TextView tvHeading;
    private long oldTime, currentTime;
    private Sender sender;
    private boolean doubleClick;
    private Switch switchActive;

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
        oldTime = System.currentTimeMillis();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        if(sensorGravity == null)
        {
            MainActivity activity = this;
            Toast.makeText(activity, "Gravity sensor is not available.", Toast.LENGTH_LONG).show();
            activity.finish();
        }

        sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if(sensorMagneticField == null)
        {
            MainActivity activity = this;
            Toast.makeText(activity, "Magnetic sensor is not available.", Toast.LENGTH_LONG).show();
            activity.finish();
        }

        buttonDoubleClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    doubleClick = true;
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
        sensorManager.registerListener(this, sensorMagneticField, SensorManager.SENSOR_DELAY_UI);
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
            tvHeading.setText("Pitch: " + (int)(pitch*radiansToDegrees) + "°\nRoll: " + (int)(roll*radiansToDegrees) + "°");
            double normalizedPitch, normalizedRoll;
            normalizedPitch = (pitch*radiansToDegrees)/pitchRange;
            if(normalizedPitch > 1.0)
                normalizedPitch = 1.0;
            if(normalizedPitch < -1.0)
                normalizedPitch = -1.0;

            normalizedRoll = (roll*radiansToDegrees)/ rollRange;
            if(normalizedRoll > 1.0)
                normalizedRoll = 1.0;
            if(normalizedRoll < -1.0)
                normalizedRoll = -1.0;
            sender.setMessage("pitch:" + normalizedPitch +
                            ",yaw:" + normalizedRoll +
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
}