package com.moisesjimenez.masterarbeit.bearinglogger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.util.LinkedList;

import static android.util.FloatMath.cos;
import static android.util.FloatMath.sin;
import static java.lang.Math.atan;
import static java.lang.Math.atan2;


public class MainActivity extends Activity implements SensorEventListener{

    private Switch bearingSwitch, filterSwitch, logSwitch, stepCountSwitch;
    private TextView bearingTextView, stepCountTextView;
    private SeekBar filterWidthSeekBar;

    private SensorManager sensorManager;
    private Sensor accelerometerSensor, magnetometerSensor, stepDetectorSensor;

    private int FILTER_LENGTH = 1, stepCount = 0;
    private double azimut = 0.0f, tempAzimut = 0.0f;;
    private float x_coordinate = 0.0f;
    private float y_coordinate = 0.0f;
    private float[] mGravity = null;
    private float[] mGeomagnetic = null;
    private boolean willLog = false, willFilter = false, willCount = false;

    private String toWrite = "";

    private LinkedList<Double> samples = null;

    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bearingSwitch = (Switch)findViewById(R.id.bearingSwitch);
        filterSwitch = (Switch)findViewById(R.id.filterSwitch);
        logSwitch = (Switch)findViewById(R.id.logSwitch);
        stepCountSwitch = (Switch)findViewById(R.id.stepCountSwitch);
        filterWidthSeekBar = (SeekBar)findViewById(R.id.filterWidthSeekBar);
        filterWidthSeekBar.setProgress(1);
        filterWidthSeekBar.setMax(100);

        bearingTextView = (TextView)findViewById(R.id.bearingTextView);
        stepCountTextView = (TextView)findViewById(R.id.stepCountTextView);

        powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        bearingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sensorManager.registerListener(MainActivity.this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
                    sensorManager.registerListener(MainActivity.this, magnetometerSensor, SensorManager.SENSOR_DELAY_UI);
                    if (wakeLock == null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Constants.wakeLockName);
                        wakeLock.acquire();
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    }
                } else {
                    sensorManager.unregisterListener(MainActivity.this, magnetometerSensor);
                    sensorManager.unregisterListener(MainActivity.this, accelerometerSensor);
                    if (!stepCountSwitch.isChecked()) {
                        removeWakeLock();
                        resetGUIValues();
                    }
                }
            }
        });

        stepCountSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sensorManager.registerListener(MainActivity.this, stepDetectorSensor, SensorManager.SENSOR_DELAY_FASTEST);
                    if (wakeLock == null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Constants.wakeLockName);
                        wakeLock.acquire();
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    }
                } else {
                    sensorManager.unregisterListener(MainActivity.this, stepDetectorSensor);
                    writeDataOut(Constants.intentWriteStepCountString, Constants.extraStepCount, System.currentTimeMillis() + "," + stepCount);
                    stepCount = 0;
                    if (!bearingSwitch.isChecked()) {
                        removeWakeLock();
                        resetGUIValues();
                    }
                }
                willCount = isChecked;
            }
        });
        filterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                willFilter=isChecked;
                if(!isChecked)
                    samples.clear();
            }
        });
        logSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                willLog = isChecked;
                if(!willLog){
                    Intent serviceIntent = new Intent(MainActivity.this, IOService.class);
                    serviceIntent.setAction(Constants.intentStopLog);
                    startService(serviceIntent);
                }
            }
        });
        filterWidthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress == 0)
                    seekBar.setProgress(1);
                FILTER_LENGTH = seekBar.getProgress();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                FILTER_LENGTH = seekBar.getProgress();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR){
            stepCount++;
            stepCountTextView.setText(Integer.toString(stepCount));
        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                tempAzimut = orientation[0]; // orientation contains: azimut, pitch and roll
                if(willFilter) {
                    if(samples == null)
                        samples = new LinkedList<>();
                    samples.add(tempAzimut);
                    if(samples.size() > FILTER_LENGTH)
                        samples.remove();
                    x_coordinate = 0;
                    y_coordinate = 0;
                    for(double f : samples){
                        x_coordinate += Math.sin(f);
                        y_coordinate += Math.cos(f);

                    }
                    x_coordinate /= samples.size();
                    y_coordinate /= samples.size();
                    azimut = atan2(x_coordinate, y_coordinate);
                }
                else
                    azimut = tempAzimut;
                azimut = Math.toDegrees(azimut);
                tempAzimut = Math.toDegrees(tempAzimut);
                if(azimut<0)
                    azimut+=360;
                if(tempAzimut<0)
                    tempAzimut+=360;
                if(willLog) {
                    toWrite += System.currentTimeMillis() + ",";
                    if(willFilter)
                        toWrite += Double.toString(azimut) + ",";
                    toWrite += Double.toString(tempAzimut) + ",";
                    if(willCount)
                        toWrite += Integer.toString(stepCount);
                    writeDataOut(Constants.intentWriteAzimutString,Constants.extraAzimut,toWrite);
                }
                bearingTextView.setText(String.format("%03.0f",azimut)+"\u00b0");
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void resetGUIValues(){
        bearingTextView.setText(getString(R.string.defaultBearing));
        stepCountTextView.setText(getString(R.string.defaultStepCount));
    }

    private void removeWakeLock(){
        wakeLock.release();
        wakeLock = null;
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void writeDataOut(String intentString, String extraName, String extra){
        Intent serviceIntent = new Intent(this, IOService.class);
        serviceIntent.setAction(intentString);
        serviceIntent.putExtra(extraName,extra);
        startService(serviceIntent);
    }
}
