package com.yoonbae.plantingplanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");  // test ad
//        MobileAds.initialize(getApplicationContext(), "ca-app-pub-앱 ID");  // real ad
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
//                .addTestDevice("테스트 기기의 Device ID")  // Galaxy Nexus-4 device ID
                .build();
        mAdView.loadAd(adRequest);

        // Get the ActionBar here to configure the way it behaves.
        ActionBar actionBar = getSupportActionBar();

        if(actionBar != null) {
            //actionBar.setIcon(R.drawable.baseline_keyboard_arrow_left_black_24);
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);    // 커스터마이징 하기 위해 필요
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);      // 뒤로가기 버튼, 디폴트로 true만 해도 백버튼이 생김
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_keyboard_arrow_left_black_24);
        }

        Intent intent = getIntent();
        setValues(intent);
    }

    private void setValues(Intent intent) {
        TextView mName = findViewById(R.id.name);
        String name = intent.getStringExtra("name");
        mName.setText(name);

        TextView mKind = findViewById(R.id.kind);
        String kind = intent.getStringExtra("kind");
        mKind.setText(kind);

        TextView mIntro = findViewById(R.id.intro);
        String intro = intent.getStringExtra("intro");
        mIntro.setText(intro);

        TextView mAdoptionDate = findViewById(R.id.adoptionDate);
        String adoptionDate = intent.getStringExtra("adoptionDate");
        mAdoptionDate.setText(adoptionDate);

        TextView mAlarmDate = findViewById(R.id.alarmDate);
        String alarmDate = intent.getStringExtra("alarmDate");
        mAlarmDate.setText(alarmDate);

        TextView mAlarmPeriod = findViewById(R.id.alarmPeriod);
        String period = intent.getStringExtra("period");
        mAlarmPeriod.setText(period);

        TextView mAlarmTime = findViewById(R.id.alarmTime);
        String alarmTime = intent.getStringExtra("alarmTime");
        mAlarmTime.setText(alarmTime);

        ImageView imageView = findViewById(R.id.imageView);
        String imageUrl = intent.getStringExtra("imageUrl");
        Glide.with(imageView).load(imageUrl).into(imageView);

        Switch mAlarm = findViewById(R.id.alarm);
        mAlarm.setClickable(false);

        String alarm = intent.getStringExtra("alarm");
        if("Y".equals(alarm))
            mAlarm.setChecked(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                Intent intent = new Intent(ViewActivity.this, ListActivity.class);
                startActivity(intent);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
