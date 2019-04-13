package com.yoonbae.plantingplanner.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.yoonbae.plantingplanner.BroadcastD;

public enum AlarmService {

    INSTANCE;

    // 알람 등록
    public void setAlarm(Context context, Long timeInMillis, Long intervalMillis, String name, int alarmId) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, BroadcastD.class);
        intent.putExtra("name", name);
        intent.putExtra("alarmId", alarmId);
        PendingIntent sender = PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //알람 예약
        am.setRepeating(AlarmManager.RTC_WAKEUP, timeInMillis, intervalMillis, sender);
    }

    // 알람 취소
    public void cancelAlarm(Context context, int alarmId) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, BroadcastD.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if(sender != null) {
            am.cancel(sender);
            sender.cancel();
        }
    }
}
