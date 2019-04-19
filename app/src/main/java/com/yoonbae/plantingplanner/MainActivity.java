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
    private FirebaseUser firebaseUser;
    private List<Plant> plantList;
    private List<CalendarDay> eventDayList = new ArrayList<>();
    private List<Map<String, Object>> eventPlantList = new ArrayList<>();
    private boolean isFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();

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
            calendarView();
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
        // 비교대상
        List<CalendarDay> compareEventDayList = new ArrayList<>(eventDayList);
        List<Map<String, Object>> compareEventPlanList = new ArrayList<>(eventPlantList);
        eventDayList.clear();
        eventPlantList.clear();

        for(Plant plant : plantList) {
            // 알람 여부 체크
            String isAlarm = plant.getAlarm();
            if (!"Y".equals(isAlarm))
                continue;

            String alarmDate = plant.getAlarmDate();
            Map<String, Integer> alarmDateInfo = getAlarmDateInfo(alarmDate);
            int year = alarmDateInfo.get("year");
            int month = alarmDateInfo.get("month");
            int dayOfMonth = alarmDateInfo.get("dayOfMonth");
            LocalDate alarmDt = LocalDate.of(year, month, dayOfMonth);

            LocalDate nextMonth;
            LocalDate currentMonth;
            if(isFirst) {
                currentMonth = LocalDate.of(date.getYear(), date.getMonth(), 1);
                nextMonth = currentMonth.plusMonths(1);
            } else {
                currentMonth = date.getDate();
                nextMonth = currentMonth.plusMonths(1);
            }

            int pod = getPeriod(plant.getPeriod());
            LocalDate eventDay;
            String name = plant.getName();
            String alarm = plant.getAlarmTime() + " 물주기 알람";
            String key = plant.getKey();
            if (compareEventDayList.size() > 0) {
                CalendarDay lastEventDay = compareEventDayList.get(compareEventDayList.size() - 1);
                if (currentMonth.isAfter(lastEventDay.getDate())) {   // 마지막 이벤트 날짜가 현재 달의 날 보다 이전이면 각 식물의 마지막 이벤트 날짜를 구하고 그 날짜부터 시작해서 새로운 이벤트 날짜를 생성한다.
                    LocalDate lastEventDate = LocalDate.of(1900, 1, 1);
                    for (Map<String, Object> compareEventPlan : compareEventPlanList) {
                        CalendarDay plantEventDay = (CalendarDay) compareEventPlan.get("eventDay");
                        if (key.equals(compareEventPlan.get("key")) && (plantEventDay != null && plantEventDay.getDate().isAfter(lastEventDate)))
                            lastEventDate = plantEventDay.getDate();
                    }

                    // 이벤트 첫 날짜 구하기
                    eventDay = lastEventDate;
                    while(eventDay.isBefore(currentMonth) && (eventDay.isEqual(alarmDt) || eventDay.isAfter(alarmDt)))
                        eventDay = eventDay.plusDays(pod);

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
                    LocalDate firstEventDay = LocalDate.of(2300, 1, 1);
                    for (Map<String, Object> compareEventPlan : compareEventPlanList) {
                        CalendarDay plantEventDay = (CalendarDay) compareEventPlan.get("eventDay");
                        if (key.equals(compareEventPlan.get("key")) && plantEventDay != null && (plantEventDay.getDate().isBefore(firstEventDay))
                                && (plantEventDay.getDate().isEqual(alarmDt) || plantEventDay.getDate().isAfter(alarmDt)))
                            firstEventDay = plantEventDay.getDate();
                    }

                    // 이벤트 마지막 날짜 구하기
                    eventDay = firstEventDay;
                    while(eventDay.isAfter(nextMonth) && (eventDay.isEqual(alarmDt) || eventDay.isAfter(alarmDt)))
                        eventDay = eventDay.minusDays(pod);

                    while ((eventDay.isEqual(currentMonth) || eventDay.isAfter(currentMonth)) && (eventDay.isEqual(alarmDt) || eventDay.isAfter(alarmDt))) {
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
            } else {
                eventDay = LocalDate.of(year, month, dayOfMonth);
                while (eventDay.isBefore(currentMonth))
                    eventDay = eventDay.plusDays(pod);

                while (eventDay.isBefore(nextMonth)) {
                    Map<String, Object> eventPlantMap = new HashMap<>();
                    eventPlantMap.put("name", name);
                    eventPlantMap.put("alarm", alarm);

                    CalendarDay day = CalendarDay.from(eventDay);
                    eventPlantMap.put("eventDay", day);
                    eventDayList.add(day);
                    eventPlantMap.put("key", key);

                    eventPlantList.add(eventPlantMap);
                    eventDay = eventDay.plusDays(pod);
                }
            }
        }

        if(isFirst)
            isFirst = false;

        materialCalendarView.addDecorator(new EventDecorator(Color.RED, eventDayList, MainActivity.this));
    }

    private Map<String, Integer> getAlarmDateInfo(String alarmDate) {
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

}
