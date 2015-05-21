package com.moisesjimenez.masterarbeit.bearinglogger;

/**
 * Created by mjimenez on 21.05.2015.
 */
public class Constants {
    public static final String  dateFormatString =                  "ddMMyyyy_HHmmss",
                                applicationDomain =                 "com.moisesjimenez.masterarbeit.bearinglogger.",
                                intentLaunchService =               applicationDomain + "intents.LaunchService",
                                intentStopLog =                     applicationDomain + "intents.StopLog",
                                intentWriteString =                 applicationDomain + "intents.WriteString",
                                extraAzimut =                       applicationDomain + "extras.azimut",
                                sharedPreferencesName =             applicationDomain + "sharedPreferences.name",
                                sharedPreferencesLogFileName =      applicationDomain + "sharedPreferences.logfile.name",
                                sharedPreferencesLogFileStarted =   applicationDomain + "sharedPreferences.logfile.started",
                                logBaseFileName =                   "logfile_";
}
