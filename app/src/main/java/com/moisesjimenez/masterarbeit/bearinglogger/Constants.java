package com.moisesjimenez.masterarbeit.bearinglogger;

/**
 * Created by mjimenez on 21.05.2015.
 */
public class Constants {
    public static final String  dateFormatString =                  "ddMMyyyy_HHmmss",
                                applicationDomain =                 "com.moisesjimenez.masterarbeit.bearinglogger.",
                                wakeLockName =                      applicationDomain + "power.WakeLockName",
                                intentLaunchService =               applicationDomain + "intents.LaunchService",
                                intentStopLog =                     applicationDomain + "intents.StopLog",
                                intentWriteAzimutString =           applicationDomain + "intents.WriteAzimutString",
                                intentWriteStepCountString =        applicationDomain + "intents.WriteStepCountString",
                                extraAzimut =                       applicationDomain + "extras.azimut",
                                extraStepCount=                     applicationDomain + "extras.stepCount",
                                sharedPreferencesName =             applicationDomain + "sharedPreferences.name",
                                sharedPreferencesLogFileName =      applicationDomain + "sharedPreferences.logfile.name",
                                sharedPreferencesLogFileStarted =   applicationDomain + "sharedPreferences.logfile.started",
                                logBaseFileName =                   "logfile_",
                                stepCountLogFileName =                   "stepcount.csv";
}
