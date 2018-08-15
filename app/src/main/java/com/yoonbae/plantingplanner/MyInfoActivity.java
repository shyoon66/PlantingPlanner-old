package com.yoonbae.plantingplanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yoonbae.plantingplanner.com.yoonbae.plantingplanner.adapter.MyRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class MyInfoActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView id;
    private FirebaseAuth mFirebaseAuth;
    private ListView listView;
    private TextView logoutTextView;
    private ArrayList<HashMap<String,String>> Data = new ArrayList<HashMap<String, String>>();
    private HashMap<String,String> InputData1 = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_info);

        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mFirebaseAuth.getCurrentUser();

        Uri photoUrl = user.getPhotoUrl();
        imageViewLayout(photoUrl);

        bottomNavigationView();
        listViewLayout();

        id = findViewById(R.id.id);
        id.setText(user.getEmail());
    }

    private void bottomNavigationView() {
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent = null;
                switch (item.getItemId()) {
                    case R.id.action_calendar:
                        intent = new Intent(MyInfoActivity.this, MainActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.action_list:
                        intent = new Intent(MyInfoActivity.this, ListActivity.class);
                        startActivity(intent);
                        break;
                }
                return false;
            }
        });
    }

    private void listViewLayout() {
        listView = findViewById(R.id.List_view);

        //데이터 초기화
        InputData1.put("menu","로그아웃");
        Data.add(InputData1);

        //simpleAdapter 생성
        SimpleAdapter simpleAdapter = new SimpleAdapter(this,Data, android.R.layout.simple_list_item_1, new String[]{"menu"}, new int[]{android.R.id.text1});
        listView.setAdapter(simpleAdapter);

        //onItemClickListener를 추가
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(position == 0) {
                String items[] = {"예", "아니오"};
                Context context = view.getContext();
                AlertDialog.Builder ab = new AlertDialog.Builder(context);
                ab.setTitle("로그아웃 하시겠습니까?");
                ab.setItems(items, new DialogInterface.OnClickListener() {    // 목록 클릭시 설정
                    public void onClick(DialogInterface dialog, int index) {
                        if(index == 0) {
                            mFirebaseAuth.signOut();
                            Intent intent = new Intent(MyInfoActivity.this, AuthActivity.class);
                            startActivity(intent);
                        } else if(index == 1) {
                            dialog.dismiss();
                        }

                        dialog.dismiss();
                    }
                });
                ab.show();
            }
            }
        });
    }

    private void imageViewLayout(Uri photoUri) {
        imageView = findViewById(R.id.imageView);
        imageView.setBackground(new ShapeDrawable(new OvalShape()));
        imageView.setClipToOutline(true);
        Glide.with(imageView.getContext()).load(photoUri).into(imageView);
    }
}
