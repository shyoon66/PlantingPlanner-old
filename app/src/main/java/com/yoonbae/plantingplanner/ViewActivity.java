package com.yoonbae.plantingplanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class ViewActivity extends AppCompatActivity {

    private TextView mName;
    private TextView mKind;
    private TextView mIntro;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String kind = intent.getStringExtra("kind");
        String intro = intent.getStringExtra("intro");
        String imageUrl = intent.getStringExtra("imageUrl");

        mName = findViewById(R.id.name);
        mName.setText(name);
        mKind = findViewById(R.id.kind);
        mKind.setText(kind);
        mIntro = findViewById(R.id.intro);
        mIntro.setText(intro);
        imageView = findViewById(R.id.imageView);
        Glide.with(imageView).load(imageUrl).into(imageView);
    }
}
