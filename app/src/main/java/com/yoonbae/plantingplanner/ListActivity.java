package com.yoonbae.plantingplanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.yoonbae.plantingplanner.com.yoonbae.plantingplanner.adapter.MyRecyclerViewAdapter;
import com.yoonbae.plantingplanner.com.yoonbae.plantingplanner.vo.Plant;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private List<Plant> plantList;
    private List<String> keyList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get the ActionBar here to configure the way it behaves.
        ActionBar actionBar = getSupportActionBar();
        //actionBar.setIcon(R.drawable.baseline_add_black_24);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);    // 커스터마이징 하기 위해 필요
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);      // 뒤로가기 버튼, 디폴트로 true만 해도 백버튼이 생김
        //actionBar.setHomeAsUpIndicator(R.drawable.baseline_keyboard_arrow_left_black_24);

        final RecyclerView recyclerView = findViewById(R.id.main_recyclerView);
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavView);
        bottomNavigationView.setSelectedItemId(R.id.action_list);
        //BottomNavigationViewHelper.removeShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent = null;
                switch (item.getItemId()) {
                    case R.id.action_calendar:
                        intent = new Intent(ListActivity.this, MainActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.action_myInfo:
                        intent = new Intent(ListActivity.this, MyInfoActivity.class);
                        startActivity(intent);
                        break;
                }

                return false;
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabase.getReference().child("plant").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                plantList = new ArrayList<Plant>();
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
                        plant.setAlarmId(value.getAlarmId());
                        plant.setUid(value.getUid());
                        plant.setUserId(value.getUserId());

                        plantList.add(plant);
                        keyList.add(snapshot.getKey());
                    }
                }

                recyclerView.setLayoutManager(new LinearLayoutManager(ListActivity.this));
                MyRecyclerViewAdapter myRecyclerViewAdapter = new MyRecyclerViewAdapter(plantList, keyList,ListActivity.this);
                recyclerView.setAdapter(myRecyclerViewAdapter);
                myRecyclerViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Failed to read value
                Log.w("Hello", "Failed to read value.", databaseError.toException());
            }
        });

/*        FloatingActionButton floatingActionButton = findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListActivity.this, AddActivity.class);
                intent.putExtra("FLAG", "I");
                startActivity(intent);
            }
        });*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                Intent intent = new Intent(ListActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.action_insert: {
                Intent intent = new Intent(ListActivity.this, AddActivity.class);
                intent.putExtra("FLAG", "I");
                startActivity(intent);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

}
