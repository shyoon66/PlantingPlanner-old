package com.yoonbae.plantingplanner.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.yoonbae.plantingplanner.BroadcastD;

public enum AlarmService {

    INSTANCE;

    public void cancleAlarm(Context context, int alarmId) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, BroadcastD.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if(sender != null) {
            am.cancel(sender);
            sender.cancel();
        }
    }
}
