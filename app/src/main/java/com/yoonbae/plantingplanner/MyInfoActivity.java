package com.yoonbae.plantingplanner;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yoonbae.plantingplanner.com.yoonbae.plantingplanner.adapter.MyRecyclerViewAdapter;

public class MyInfoActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView id;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_info);

        imageView = findViewById(R.id.imageView);
        imageView.setBackground(new ShapeDrawable(new OvalShape()));
        imageView.setClipToOutline(true);

        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        Glide.with(imageView.getContext()).load(user.getPhotoUrl()).into(imageView);
        
        id = findViewById(R.id.id);
        id.setText(user.getEmail());
    }
}
