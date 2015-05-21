package com.moisesjimenez.masterarbeit.bearinglogger;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;


public class MainActivity extends Activity implements SensorEventListener{
    private int FILTER_LENGTH = 1;

    private Switch startSwitch, filterSwitch;
    private TextView bearingTextView;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private Sensor magnetometerSensor;
    private float[] mGravity = null;
    private float[] mGeomagnetic = null;
    private int filterCount = 0;

    private float azimut = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startSwitch = (Switch)findViewById(R.id.startSwitch);
        filterSwitch = (Switch)findViewById(R.id.filterSwitch);
        bearingTextView = (TextView)findViewById(R.id.bearingTextView);
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        startSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sensorManager.registerListener(MainActivity.this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
                    sensorManager.registerListener(MainActivity.this, magnetometerSensor, SensorManager.SENSOR_DELAY_UI);
                } else {
                    sensorManager.unregisterListener(MainActivity.this);
                    Intent serviceIntent = new Intent(MainActivity.this, IOService.class);
                    serviceIntent.setAction(Constants.intentStopLog);
                    startService(serviceIntent);
                }
            }
        });
        filterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked){
                if (isChecked) {
                    FILTER_LENGTH = 10;
                }else{
                    FILTER_LENGTH = 1;
                }
                filterCount = 0;
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
        float tempAzimut = 0.0f;
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                filterCount++;
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                tempAzimut = (float)Math.toDegrees(orientation[0]); // orientation contains: azimut, pitch and roll
                if(tempAzimut<0)
                    tempAzimut+=360;
                azimut+=tempAzimut;
                if(filterCount == FILTER_LENGTH) {
                    filterCount = 0;
                    Intent serviceIntent = new Intent(this, IOService.class);
                    serviceIntent.setAction(Constants.intentWriteString);
                    azimut/=10;
                    serviceIntent.putExtra(Constants.extraAzimut, System.currentTimeMillis() + "," + Float.toString(azimut));
                    startService(serviceIntent);
                    bearingTextView.setText(Float.toString(azimut));
                    azimut = 0;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
