package com.moisesjimenez.masterarbeit.bearinglogger;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Moises Jimenez on 20.05.2015.
 */
public class IOService extends IntentService {
    private boolean started = false;
    private String currentFileNameString = "", dateString= "";
    private SimpleDateFormat simpleDateFormat;
    private SharedPreferences sharedPreferences;
    private File logFile;
    private FileOutputStream fileOutputStream;

    public IOService(){
        super("IOService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        if(sharedPreferences == null)
            sharedPreferences = getSharedPreferences(Constants.sharedPreferencesName,0);
        if(action == Constants.intentWriteString){
            started = sharedPreferences.getBoolean(Constants.sharedPreferencesLogFileStarted,false);
            if(!started){
                started = true;
                simpleDateFormat = new SimpleDateFormat(Constants.dateFormatString, Locale.GERMANY);
                dateString = simpleDateFormat.format(new Date());
                currentFileNameString = Constants.logBaseFileName + dateString + ".csv";
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(Constants.sharedPreferencesLogFileStarted,started);
                editor.putString(Constants.sharedPreferencesLogFileName, currentFileNameString);
                editor.commit();
                logFile = new File(getExternalFilesDir(null),currentFileNameString);
            }else{
                currentFileNameString = sharedPreferences.getString(Constants.sharedPreferencesLogFileName,"");
                logFile = new File(getExternalFilesDir(null),currentFileNameString);
            }
            String toWrite = intent.getStringExtra(Constants.extraAzimut);
            try {
                PrintStream printStream = new PrintStream(new BufferedOutputStream(new FileOutputStream(logFile,true)));
                printStream.append(toWrite+"\n");
                printStream.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }else if(action == Constants.intentStopLog){
            started = false;
            sharedPreferences = getSharedPreferences(Constants.sharedPreferencesName,0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(Constants.sharedPreferencesLogFileStarted,started);
            editor.commit();
        }
    }
}
