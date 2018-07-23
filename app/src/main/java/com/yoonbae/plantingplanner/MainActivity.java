package com.yoonbae.plantingplanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;


import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.DatePicker;
import com.applandeo.materialcalendarview.builders.DatePickerBuilder;
import com.applandeo.materialcalendarview.exceptions.OutOfDateRangeException;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        calendarView();

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent = null;
                switch (item.getItemId()) {
                    case R.id.action_diary:
                        return true;
                    case R.id.action_add:
                        intent = new Intent(MainActivity.this, AddActivity.class);
                        startActivity(intent);
                    case R.id.action_list:
                        intent = new Intent(MainActivity.this, ListActivity.class);
                        startActivity(intent);
                }
                return false;
            }
        });
    }

    private void calendarView() {
        CalendarView calendarView = findViewById(R.id.calendarView);
        Calendar calendar = Calendar.getInstance();

        try {
            calendarView.setDate(calendar.getTime());
        } catch (OutOfDateRangeException e) {
            e.printStackTrace();
        }
    }

}
