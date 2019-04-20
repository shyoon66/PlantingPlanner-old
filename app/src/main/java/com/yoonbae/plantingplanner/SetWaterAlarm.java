package com.yoonbae.plantingplanner;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yoonbae.plantingplanner.service.AlarmService;
import com.yoonbae.plantingplanner.vo.Plant;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SetWaterAlarm extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        firebaseDatabase.getReference().child("plant").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(firebaseUser != null) {
                    String fUid = firebaseUser.getUid();

                    for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Plant plant = snapshot.getValue(Plant.class);

                        if(plant != null) {
                            String dUid = plant.getUid();
                            String alarm = plant.getAlarm();
                            if(fUid.equals(dUid) && "Y".equals(alarm)) {
                                String alarmDate = plant.getAlarmDate();
                                String alarmTime = plant.getAlarmTime();
                                int pod = getPeriod(plant.getPeriod());
                                String name = plant.getName();
                                int alarmId = plant.getAlarmId();
                                setAlarm(alarmDate, alarmTime, pod, name, alarmId);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Failed to read value
                Log.w("Hello", "Failed to read value.", databaseError.toException());
            }
        });
    }

    private void setAlarm(String alarmDate, String alarmTime, int pod, String name, int alarmId) {
        Map<String, Integer> alarmDateMap = getAlarmDate(alarmDate);
        Map<String, Integer> alarmTimeMap = getAlarmTime(alarmTime);
        if(alarmDateMap != null && alarmTimeMap != null) {
            int year = alarmDateMap.get("year");
            int month = alarmDateMap.get("month");
            int dayOfMonth = alarmDateMap.get("dayOfMonth");
            int hourOfDay = alarmTimeMap.get("hourOfDay");
            int minute = alarmTimeMap.get("minute");

            LocalDateTime alarmDateTime = LocalDateTime.of(year, month, dayOfMonth, hourOfDay, minute);
            LocalDateTime nowDateTime = LocalDateTime.now();
            while(alarmDateTime.isBefore(nowDateTime) || alarmDateTime.isEqual(nowDateTime))
                alarmDateTime.plusDays(pod);

            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth, hourOfDay, minute);
            long alarmTimeInMillis = calendar.getTimeInMillis();
            long intervalMillis = pod * 24 * 60 * 60 * 1000;
            AlarmService.INSTANCE.setAlarm(getApplicationContext(), alarmTimeInMillis, intervalMillis, name, alarmId);
        }
    }

    private Map<String, Integer> getAlarmDate(String alarmDate) {
        String[] alarmDateArr = alarmDate.split("-");
        int year = Integer.parseInt(alarmDateArr[0]);
        int month = Integer.parseInt(alarmDateArr[1]);
        int dayOfMonth = Integer.parseInt(alarmDateArr[2]);
        Map<String, Integer> resultMap = new HashMap<>();
        resultMap.put("year", year);
        resultMap.put("month", month);
        resultMap.put("dayOfMonth", dayOfMonth);
        return resultMap;
    }

    private Map<String, Integer> getAlarmTime(String alarmTime) {
        int hourOfDay = Integer.parseInt(alarmTime.substring(0, alarmTime.indexOf("시")));
        int minute = Integer.parseInt(alarmTime.substring(alarmTime.indexOf("시") + 2, alarmTime.length() - 1));
        Map<String, Integer> resultMap = new HashMap<>();
        resultMap.put("hourOfDay", hourOfDay);
        resultMap.put("minute", minute);
        return resultMap;
    }

    private int getPeriod(String period) {
        int pod;

        if("매일".equals(period))
            pod = 1;
        else if("이틀".equals(period))
            pod = 2;
        else
            pod = Integer.parseInt(period.substring(0, period.length() - 1));

        return pod;
    }

}
