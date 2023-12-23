package eu.cyberpunktech.remote.mouse;

import static eu.cyberpunktech.remote.mouse.Variables.*;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class ConfigurationActivity extends AppCompatActivity {
    private EditText editTextServerIP;
    private Button buttonSave;
    private boolean goingToMainActivity = false;
    private SeekBar seekBarRed, seekBarGreen, seekBarBlue;
    private final int minOffset = 32;
    private ConstraintLayout constraintLayout;

    private final String backupServerIP = serverIP;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.configuration);

        editTextServerIP = findViewById(R.id.editTextIP);
        buttonSave = findViewById(R.id.buttonSave);

        editTextServerIP.setText(serverIP);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToMainActivity();
            }
        });

        editTextServerIP.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().contains(" "))
                {
                    editTextServerIP.setText(charSequence.toString().replace(" ", ""));
                }

                if(charSequence.toString().contains(","))
                {
                    editTextServerIP.setText(charSequence.toString().replace(",", "."));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        constraintLayout = findViewById(R.id.constraintLayout);
        changeBackground();

        seekBarRed = findViewById(R.id.seekBarRed);
        seekBarGreen = findViewById(R.id.seekBarGreen);
        seekBarBlue = findViewById(R.id.seekBarBlue);

        seekBarRed.setMax(255 - minOffset);
        seekBarRed.setProgress(red - minOffset);
        seekBarRed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                red = i + minOffset;
                changeBackground();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarGreen.setMax(255 - minOffset);
        seekBarGreen.setProgress(green - minOffset);
        seekBarGreen.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                green = i + minOffset;
                changeBackground();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarBlue.setMax(255 - minOffset);
        seekBarBlue.setProgress(blue - minOffset);
        seekBarBlue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                blue = i + minOffset;
                changeBackground();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void changeBackground()
    {
        int rgb = 255;
        rgb = (rgb << 8) + red;
        rgb = (rgb << 8) + green;
        rgb = (rgb << 8) + blue;
        constraintLayout.setBackgroundColor(rgb);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        goToMainActivity();
    }

    public void saveData() {
        if(editTextServerIP.getText().toString().length() < 8)
            serverIP = backupServerIP;
        else
            serverIP = editTextServerIP.getText().toString();

        SharedPreferences sharedPreferences = getSharedPreferences(CONFIGURATION, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(SERVER_IP, serverIP);
        editor.putInt(COLOR_RED, red);
        editor.putInt(COLOR_GREEN, green);
        editor.putInt(COLOR_BLUE, blue);

        editor.apply();

        Toast.makeText(this, "Data saved", Toast.LENGTH_SHORT).show();
    }

    void goToMainActivity()
    {
        if(!goingToMainActivity)
        {
            goingToMainActivity = true;
            saveData();
            final Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}