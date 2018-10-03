package com.yoonbae.plantingplanner;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.format.MonthArrayTitleFormatter;
import com.yoonbae.plantingplanner.com.yoonbae.plantingplanner.decorator.EventDecorator;
import com.yoonbae.plantingplanner.com.yoonbae.plantingplanner.decorator.HighlightWeekendsDecorator;
import com.yoonbae.plantingplanner.com.yoonbae.plantingplanner.decorator.OneDayDecorator;
import com.yoonbae.plantingplanner.com.yoonbae.plantingplanner.vo.Plant;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements OnDateSelectedListener {

    private MaterialCalendarView materialCalendarView;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private List<Plant> plantList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        calendarView();

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavView);
        //BottomNavigationViewHelper.removeShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent = null;
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
            }
        });
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    private void calendarView() {
        materialCalendarView = findViewById(R.id.calendarView);
        materialCalendarView.setTitleFormatter(new MonthArrayTitleFormatter(getResources().getTextArray(R.array.months_array)));
        materialCalendarView.state().edit()
                .setFirstDayOfWeek(DayOfWeek.SUNDAY)
                .setMinimumDate(CalendarDay.from(2000, 1, 1))
                .setMaximumDate(CalendarDay.from(2300, 12, 31))
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit();

        materialCalendarView.addDecorators(
                new HighlightWeekendsDecorator(),
                new OneDayDecorator());

        materialCalendarView.setOnDateChangedListener(this);
        plantList = new ArrayList<Plant>();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabase.getReference().child("plant").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String fUid = firebaseUser.getUid();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Plant value = snapshot.getValue(Plant.class);
                    String dUid = value.getUid();

                    if(fUid.equals(dUid)) {
                        Plant plant = new Plant();
                        plant.setName(value.getName());
                        plant.setKind(value.getKind());
                        plant.setImageName(value.getImageName());
                        plant.setImageUrl(value.getImageUrl());
                        plant.setIntro(value.getIntro());
                        plant.setAdoptionDate(value.getAdoptionDate());
                        plant.setAlarm(value.getAlarm());
                        plant.setAlarmDate(value.getAlarmDate());
                        plant.setPeriod(value.getPeriod());
                        plant.setAlarmTime(value.getAlarmTime());
                        plant.setUid(value.getUid());
                        plant.setUserId(value.getUserId());
                        plantList.add(plant);
                    }
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
        Calendar calendar = Calendar.getInstance();
        ArrayList<CalendarDay> eventDayList = new ArrayList<>();
        //calendar.add(Calendar.MONTH, -2);

        for(int i = 0; i < plantList.size(); i++) {
            Plant plant = plantList.get(i);
            String period = plant.getPeriod();
            int pod = 0;

            if("1일".equals(period)) {
                pod = 1;
            } else if("2일".equals(period)) {
                pod = 2;
            } else if("3일".equals(period)) {
                pod = 3;
            } else if("4일".equals(period)) {
                pod = 4;
            } else if("5일".equals(period)) {
                pod = 5;
            } else if("6일".equals(period)) {
                pod = 6;
            } else if("1주일".equals(period)) {
                pod = 7;
            } else if("2주일".equals(period)) {
                pod = 14;
            } else if("3주일".equals(period)) {
                pod = 21;
            } else if("1달".equals(period)) {
                pod = 30;
            } else if("2달".equals(period)) {
                pod = 60;
            }

            String alarmDate = plant.getAlarmDate();
            String[] alarmDateArr = alarmDate.split("-");

            int year = Integer.parseInt(alarmDateArr[0]);
            int month = Integer.parseInt(alarmDateArr[1]);
            int dayOfYear = Integer.parseInt(alarmDateArr[2]);
            calendar.set(year, month, dayOfYear);
            LocalDate date = LocalDate.of(year, month, dayOfYear);
            java.time.LocalDate localDate = java.time.LocalDate.of(year, month, dayOfYear);

            int max = (2300 - java.time.LocalDate.now().getYear()) * 365 / pod;

            for(int j = 0; j < max; j++) {
                CalendarDay day = CalendarDay.from(date);
                eventDayList.add(day);

                if(pod != 30 && pod != 60) {
                    localDate = localDate.plusDays(pod);
                    //calendar.add(Calendar.DATE, pod);
                } else if(pod == 30) {
                    localDate = localDate.plusMonths(1);
                    //calendar.add(Calendar.MONTH, 1);
                } else if(pod == 60) {
                    localDate = localDate.plusMonths(2);
                    //calendar.add(Calendar.MONTH, 2);
                }

                date = LocalDate.of(localDate.getYear(), localDate.getMonth().getValue(), localDate.getDayOfMonth());
            }
        }

        materialCalendarView.addDecorator(new EventDecorator(Color.RED, eventDayList, MainActivity.this));
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
        //If you change a decorate, you need to invalidate decorators
/*        OneDayDecorator oneDayDecorator = new OneDayDecorator();
        oneDayDecorator.setDate(date.getDate());
        widget.addDecorator(oneDayDecorator);
        widget.invalidateDecorators();*/
    }

}
