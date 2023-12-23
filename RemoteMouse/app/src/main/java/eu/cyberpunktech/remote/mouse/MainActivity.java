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
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor sensorMagneticField, sensorGravity;
    private double xA, yA, zA, xM, yM, zM;
    private Button buttonCalibrate, buttonInfinity, buttonDoubleClick, buttonLeft, buttonRight;
    private TextView tvHeading, tvMagneticField, tvCalibration;
    private ProgressBar progressBarCalibration;
    private long oldTime, currentTime;
    private int calibrationReadings = 0;
    private double oldPitch, oldRoll, integratedAngle;
    private boolean calibrate = true;
    private int headingReference, headingCompass;
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

        headingReference = 0;
        doubleClick = false;

        buttonCalibrate = findViewById(R.id.buttonCalibrate);
        buttonInfinity = findViewById(R.id.buttonInfinity);
        buttonDoubleClick = findViewById(R.id.buttonDoubleClick);
        buttonLeft = findViewById(R.id.buttonLeft);
        buttonRight = findViewById(R.id.buttonRight);
        tvHeading = findViewById(R.id.tvHeading);
        tvMagneticField = findViewById(R.id.tvMagneticField);
        tvCalibration = findViewById(R.id.textViewCalibration);
        progressBarCalibration = findViewById(R.id.progressBarCalibration);
        switchActive = findViewById(R.id.switchActive);
        oldTime = System.currentTimeMillis();

        buttonCalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                headingReference = headingCompass;
            }
        });

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

        if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
        {
            xM = event.values[0];
            yM = event.values[1];
            zM = event.values[2];
        }

        double pitch = Math.atan2(-xA,Math.sqrt(yA*yA+zA*zA));
        double roll = Math.atan2(yA,zA);

        double cos_roll = Math.cos(roll);
        double sin_roll = Math.sin(roll);

        double cos_pitch = Math.cos(pitch);
        double sin_pitch = Math.sin(pitch);

        double yHeading = (yM*cos_roll) - ((float)zM*sin_roll);
        double xHeading = (xM*cos_pitch) + ((float)yM*sin_roll*sin_pitch) + ((float)zM*cos_roll*sin_pitch);
        int heading = (int) (Math.atan2(yHeading,xHeading)*radiansToDegrees) - 90;

        while(heading>=360) heading -= 360;
        while(heading<0) heading += 360;
        heading = 360-heading;
        if(heading==360) heading = 0;
        headingCompass = heading;

        heading = headingReference - heading;
        while(heading >= 180 ){
            heading -= 180;
            heading = headingReference - heading;
        }

        tvHeading.setText("Pitch: " + (int)(roll*radiansToDegrees) + "°\nHeading: " + heading + "°");
        tvMagneticField.setText((int)Math.sqrt(xM*xM + yM*yM + zM*zM)+"uT");


        currentTime = System.currentTimeMillis();
        if((currentTime - oldTime) > 100)
        {
            tvHeading.setText("Pitch: " + (int)(roll*radiansToDegrees) + "°\nHeading: " + heading + "°");
            tvMagneticField.setText((int)Math.sqrt(xM*xM + yM*yM + zM*zM)+"uT");
            double normalizedPitch, normalizedYaw;
            normalizedPitch = (roll*radiansToDegrees)/pitchRange;
            if(normalizedPitch > 1.0)
                normalizedPitch = 1.0;
            if(normalizedPitch < -1.0)
                normalizedPitch = -1.0;

            normalizedYaw = -heading/yawRange;
            if(normalizedYaw > 1.0)
                normalizedYaw = 1.0;
            if(normalizedYaw < -1.0)
                normalizedYaw = -1.0;
            sender.setMessage("pitch:" + normalizedPitch +
                            ",yaw:" + normalizedYaw +
                            ",LMB:" + buttonLeft.isPressed() +
                            ",DoubleClick:" + doubleClick +
                            ",RMB:" + buttonRight.isPressed() +
                            ",Active:" + switchActive.isChecked());

            if(doubleClick)
            {
                doubleClick = false;
            }
        }

        if((currentTime - oldTime) > 100 && calibrate)
        {
            if(calibrationReadings == 0)
            {
                oldPitch = Math.abs(pitch*57.29577951);
                oldRoll = Math.abs(roll*57.29577951);
            }else
            {
                integratedAngle += Math.abs(oldPitch-Math.abs(pitch)) + Math.abs(oldRoll-Math.abs(roll));
                oldPitch = pitch;
                oldRoll = roll;
                progressBarCalibration.setProgress((int)(integratedAngle/2));
                oldTime = currentTime;

                if(progressBarCalibration.getProgress()==100)
                {
                    progressBarCalibration.setVisibility(View.GONE);
                    tvCalibration.setVisibility(View.GONE);
                    buttonInfinity.setVisibility(View.GONE);
                    tvHeading.setVisibility(View.VISIBLE);
                    calibrate = false;
                    sender.start();
                }
            }
            calibrationReadings++;
        }
    }

}