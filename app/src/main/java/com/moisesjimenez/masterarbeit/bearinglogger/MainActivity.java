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
    private Switch startSwitch;
    private TextView bearingTextView;
    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;
    private Context context;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private Sensor magnetometerSensor;
    private float[] mGravity = null;
    private float[] mGeomagnetic = null;

    private float azimut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getBaseContext();
        startSwitch = (Switch)findViewById(R.id.startSwitch);
        bearingTextView = (TextView)findViewById(R.id.bearingTextView);
//        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        startSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sensorManager.registerListener(MainActivity.this, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
                    sensorManager.registerListener(MainActivity.this, magnetometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
//                    Intent intent = new Intent(context, AlarmReceiver.class);
//                    alarmIntent = PendingIntent.getBroadcast(context,0,intent,0);
//                    alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
//                            1 * 1000,
//                            1 * 1000, alarmIntent);
                } else {
                    sensorManager.unregisterListener(MainActivity.this);
                    Intent serviceIntent = new Intent(MainActivity.this, IOService.class);
                    serviceIntent.setAction(Constants.intentStopLog);
                    startService(serviceIntent);
//                    if (alarmManager!= null) {
//                        alarmManager.cancel(alarmIntent);
//                    }
                }
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
                azimut = orientation[0]; // orientation contains: azimut, pitch and roll
                Intent serviceIntent = new Intent(this,IOService.class);
                serviceIntent.setAction(Constants.intentWriteString);
                serviceIntent.putExtra(Constants.extraAzimut, System.currentTimeMillis() + "," + Float.toString(azimut));
                startService(serviceIntent);
                bearingTextView.setText(Float.toString(azimut));
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
