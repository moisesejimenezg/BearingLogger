package com.moisesjimenez.masterarbeit.bearinglogger;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by Moises Jimenez on 20.05.2015.
 */
public class IOService extends IntentService {

    public IOService(){
        super("IOService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        if(action == getResources().getString(R.string.WriteString)){
            
        }
    }
}
