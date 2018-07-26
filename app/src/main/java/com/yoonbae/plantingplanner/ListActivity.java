package com.yoonbae.plantingplanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yoonbae.plantingplanner.com.yoonbae.plantingplanner.adapter.MyRecyclerViewAdapter;
import com.yoonbae.plantingplanner.com.yoonbae.plantingplanner.vo.Plant;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    private FirebaseDatabase firebaseDatabase;
    private List<Plant> plantList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        final RecyclerView recyclerView = findViewById(R.id.main_recyclerView);

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabase.getReference().child("plant").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                plantList = new ArrayList<Plant>();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Plant value = snapshot.getValue(Plant.class);
                    Plant plant = new Plant();
                    plant.setName(value.getName());
                    plant.setKind(value.getKind());
                    plant.setImageUrl(value.getImageUrl());
                    plant.setIntro(value.getIntro());
                    plant.setStartDate(value.getStartDate());
                    plant.setPeriod(value.getPeriod());
                    plant.setUid(value.getUid());
                    plant.setUserId(value.getUserId());
                    plantList.add(plant);
                }

                recyclerView.setLayoutManager(new LinearLayoutManager(ListActivity.this));
                MyRecyclerViewAdapter myRecyclerViewAdapter = new MyRecyclerViewAdapter(plantList, ListActivity.this);
                recyclerView.setAdapter(myRecyclerViewAdapter);
                myRecyclerViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Failed to read value
                Log.w("Hello", "Failed to read value.", databaseError.toException());
            }
        });
    }
}
