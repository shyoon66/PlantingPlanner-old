package com.yoonbae.plantingplanner;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.perf.metrics.AddTrace;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;
import com.prolificinteractive.materialcalendarview.format.MonthArrayTitleFormatter;
import com.yoonbae.plantingplanner.adapter.ListViewAdapter;
import com.yoonbae.plantingplanner.decorator.EventDecorator;
import com.yoonbae.plantingplanner.decorator.HighlightWeekendsDecorator;
import com.yoonbae.plantingplanner.decorator.OneDayDecorator;
import com.yoonbae.plantingplanner.vo.Plant;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements OnDateSelectedListener, OnMonthChangedListener, AdapterView.OnItemClickListener {

    private MaterialCalendarView materialCalendarView;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private List<Plant> plantList;
    private ArrayList<CalendarDay> eventDayList = new ArrayList<>();
    private ArrayList<Map<String, Object>> eventPlantList = new ArrayList<>();
    private boolean isFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();

        calendarView();
        CalendarThread calendarThread = new CalendarThread();
        calendarThread.start();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavView);
        //BottomNavigationViewHelper.removeShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Intent intent;
            switch (item.getItemId()) {
                case R.id.action_list:
                    intent = new Intent(MainActivity.this, ListActivity.class);
                    startActivity(intent);
                    break;
                case R.id.action_myInfo:
                    intent = new Intent(MainActivity.this, MyInfoActivity.class);
                    startActivity(intent);
                    break;
            }

            return false;
        });
    }

    private class CalendarThread extends Thread {
        @Override
        public void run() {
            getPlantList();
        }
    }

    private void calendarView() {
        materialCalendarView = findViewById(R.id.calendarView);
        materialCalendarView.setTitleFormatter(new MonthArrayTitleFormatter(getResources().getTextArray(R.array.months_array)));
        materialCalendarView.state().edit()
            .setFirstDayOfWeek(DayOfWeek.SUNDAY)
            .setMinimumDate(CalendarDay.from(2018, 1, 1))
            .setMaximumDate(CalendarDay.from(2030, 12, 31))
            .setCalendarDisplayMode(CalendarMode.MONTHS)
            .commit();

        materialCalendarView.addDecorators(new HighlightWeekendsDecorator(), new OneDayDecorator());
        materialCalendarView.setOnDateChangedListener(this);
        materialCalendarView.setOnMonthChangedListener(this);
    }

    @AddTrace(name = "calendarEvent")
    private void getPlantList() {
        plantList = new ArrayList<>();
        firebaseDatabase.getReference().child("plant").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String fUid = "";
                if(firebaseUser != null)
                    fUid = firebaseUser.getUid();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Plant plant = snapshot.getValue(Plant.class);
                    String dUid = "";
                    if(plant != null) {
                        plant.setKey(snapshot.getKey());
                        dUid = plant.getUid();
                    }

                    if(fUid.equals(dUid))
                        plantList.add(plant);
                }

                calendarEvent();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Failed to read value
                Log.w("Hello", "Failed to read value.", databaseError.toException());
            }
        });
    }

    private void calendarEvent() {
        materialCalendarView.setDateSelected(CalendarDay.today(), true);
        onMonthChanged(materialCalendarView, CalendarDay.today());
        onDateSelected(materialCalendarView, CalendarDay.today(), true);
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

    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        Map<String, String> eventPlantMap = (Map<String, String>) parent.getItemAtPosition(position);
        String key = eventPlantMap.get("key");

        for(Plant plant : plantList) {
            if(plant.getKey().equals(key)) {
                Intent intent = new Intent(MainActivity.this, ViewActivity.class);
                intent.putExtra("FLAG", "U");
                intent.putExtra("name", plant.getName());
                intent.putExtra("kind", plant.getKind());
                intent.putExtra("intro", plant.getIntro());
                intent.putExtra("imageUrl", plant.getImageUrl());
                intent.putExtra("uid", plant.getUid());
                intent.putExtra("adoptionDate", plant.getAdoptionDate());
                intent.putExtra("alarm", plant.getAlarm());
                intent.putExtra("alarmDate", plant.getAlarmDate());
                intent.putExtra("alarmTime", plant.getAlarmTime());
                intent.putExtra("period", plant.getPeriod());
                intent.putExtra("alarmId", plant.getAlarmId());

                startActivity(intent);
                break;
            }
        }
    }

    @Override
    @AddTrace(name = "onDateSelected")
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
        ListView listview = findViewById(R.id.listview);
        ListViewAdapter adapter = new ListViewAdapter();
        boolean flag = false;

        for(Map<String, Object> eventPlant : eventPlantList) {
            if(eventPlant != null && date.equals(eventPlant.get("eventDay"))) {
                flag = true;
                adapter.addItem(eventPlant.get("name").toString(), eventPlant.get("alarm").toString(), eventPlant.get("key").toString());
            }
        }

        if(flag) {
            listview.setAdapter(adapter);
            listview.setOnItemClickListener(this);
        } else {
            listview.setAdapter(null);
        }

        //If you change a decorate, you need to invalidate decorators
/*        OneDayDecorator oneDayDecorator = new OneDayDecorator();
        oneDayDecorator.setDate(date.getDate());
        widget.addDecorator(oneDayDecorator);
        widget.invalidateDecorators();*/
    }

    @Override
    public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
        if (eventDayList.size() > 0) {
            LocalDate eventDay;
            ArrayList<Map<String, Object>> compareEventPlantList = eventPlantList;
            eventDayList.clear();
            eventPlantList.clear();

            LocalDate nextMonth = date.getDate().plusMonths(1);
            Calendar cal = Calendar.getInstance();
            cal.set(date.getYear(), date.getMonth() - 1, date.getDay());
            int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            LocalDate lastDate = LocalDate.of(date.getYear(), date.getMonth(), lastDay);

            for (Plant plant : plantList) {
                LocalDate lastEventDay = LocalDate.of(1900, 1, 1);
                String key = plant.getKey();

                for (Map<String, Object> compareEventPlant : compareEventPlantList) {
                    CalendarDay plantEventDay = (CalendarDay) compareEventPlant.get("eventDay");
                    if (key.equals(compareEventPlant.get("key")) && plantEventDay.getDate().isAfter(lastEventDay))
                        lastEventDay = plantEventDay.getDate();
                }

                String alarmDate = plant.getAlarmDate();
                String[] alarmDateArr = alarmDate.split("-");
                int year = Integer.parseInt(alarmDateArr[0]);
                int month = Integer.parseInt(alarmDateArr[1]);
                int dayOfYear = Integer.parseInt(alarmDateArr[2]);
                LocalDate alarmDt = LocalDate.of(year, month, dayOfYear);

                if (lastDate.isBefore(alarmDt))
                    continue;

                if (date.getDate().isAfter(lastEventDay)) {
                    int pod = getPeriod(plant.getPeriod());
                    eventDay = lastEventDay.plusDays(pod);
                    String name = plant.getName();
                    String alarm = plant.getAlarmTime() + " 물주기 알람";

                    while (eventDay.isBefore(nextMonth)) {
                        CalendarDay day = CalendarDay.from(eventDay);
                        eventDayList.add(day);
                        Map<String, Object> eventPlantMap = new HashMap<>();
                        eventPlantMap.put("name", name);
                        eventPlantMap.put("alarm", alarm);
                        eventPlantMap.put("eventDay", day);
                        eventPlantMap.put("key", key);
                        eventPlantList.add(eventPlantMap);
                        eventDay = eventDay.plusDays(pod);
                    }
                } else {
                    LocalDate monthDate = date.getDate();
                    LocalDate firstEventDay = LocalDate.of(2300, 1, 1);
                    for (Map<String, Object> compareEventPlant : compareEventPlantList) {
                        CalendarDay plantEventDay = (CalendarDay) compareEventPlant.get("eventDay");
                        if (key.equals(compareEventPlant.get("key")) && plantEventDay.getDate().isBefore(firstEventDay))
                            firstEventDay = plantEventDay.getDate();
                    }

                    int pod = getPeriod(plant.getPeriod());
                    eventDay = firstEventDay.minusDays(pod);
                    String name = plant.getName();
                    String alarm = plant.getAlarmTime() + " 물주기 알람";

                    while (eventDay.isAfter(monthDate) && (eventDay.isEqual(alarmDt) || eventDay.isAfter(alarmDt))) {
                        CalendarDay day = CalendarDay.from(eventDay);
                        eventDayList.add(day);
                        Map<String, Object> eventPlantMap = new HashMap<>();
                        eventPlantMap.put("name", name);
                        eventPlantMap.put("alarm", alarm);
                        eventPlantMap.put("eventDay", day);
                        eventPlantMap.put("key", key);
                        eventPlantList.add(eventPlantMap);
                        eventDay = eventDay.minusDays(pod);
                    }
                }
            }
        } else {
            for(Plant plant : plantList) {
                LocalDate nextMonth = date.getDate().plusMonths(1);
                String alarmDate = plant.getAlarmDate();
                String[] alarmDateArr = alarmDate.split("-");
                int year = Integer.parseInt(alarmDateArr[0]);
                int month = Integer.parseInt(alarmDateArr[1]);
                int dayOfYear = Integer.parseInt(alarmDateArr[2]);
                LocalDate eventDay = LocalDate.of(year, month, dayOfYear);

                LocalDate currentMonth = LocalDate.of(date.getYear(), date.getMonth(), 1);
                int pod = getPeriod(plant.getPeriod());

                while(isFirst && eventDay.isBefore(currentMonth))
                    eventDay = eventDay.plusDays(pod);

                while(eventDay.isEqual(currentMonth) || (eventDay.isAfter(currentMonth) && eventDay.isBefore(nextMonth))) {
                    Map<String, Object> eventPlantMap = new HashMap<>();
                    String name = plant.getName();
                    eventPlantMap.put("name", name);

                    String alarm = plant.getAlarmTime() + " 물주기 알람";
                    eventPlantMap.put("alarm", alarm);

                    CalendarDay day = CalendarDay.from(eventDay);
                    eventPlantMap.put("eventDay", day);
                    eventDayList.add(day);

                    String key = plant.getKey();
                    eventPlantMap.put("key", key);

                    eventPlantList.add(eventPlantMap);
                    eventDay = eventDay.plusDays(pod);
                }
            }

            isFirst = false;
        }

        materialCalendarView.addDecorator(new EventDecorator(Color.RED, eventDayList, MainActivity.this));
    }

}
