package com.yoonbae.plantingplanner;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yoonbae.plantingplanner.service.AlarmService;
import com.yoonbae.plantingplanner.vo.Plant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyInfoActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase firebaseDatabase;
    private ArrayList<HashMap<String,String>> Data = new ArrayList<>();
    private HashMap<String,String> InputData1 = new HashMap<>();
    private HashMap<String,String> InputData2 = new HashMap<>();
    private String token;
    private AuthCredential credential;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_info);

        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");  // test ad
//        MobileAds.initialize(getApplicationContext(), "ca-app-pub-앱 ID");  // real ad
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
//                .addTestDevice("테스트 기기의 Device ID")  // Galaxy Nexus-4 device ID
                .build();
        mAdView.loadAd(adRequest);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        Uri photoUrl = null;
        if(mFirebaseUser != null)
            photoUrl = mFirebaseUser.getPhotoUrl();

        imageViewLayout(photoUrl);
        bottomNavigationView();
        listViewLayout();

        TextView id = findViewById(R.id.id);
        id.setText(mFirebaseUser.getEmail());
    }

    private void bottomNavigationView() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavView);
        bottomNavigationView.setSelectedItemId(R.id.action_myInfo);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Intent intent;
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
        });
    }

    private void listViewLayout() {
        ListView listView = findViewById(R.id.List_view);

        //데이터 초기화
        InputData1.put("menu","로그아웃");
        InputData2.put("menu", "계정삭제");
        Data.add(InputData1);
        Data.add(InputData2);

        //simpleAdapter 생성
        SimpleAdapter simpleAdapter = new SimpleAdapter(this,Data, android.R.layout.simple_list_item_1, new String[]{"menu"}, new int[]{android.R.id.text1});
        listView.setAdapter(simpleAdapter);

        //onItemClickListener를 추가
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if(position == 0) {
                String[] items = {"예", "아니오"};
                Context context = view.getContext();
                AlertDialog.Builder ab = new AlertDialog.Builder(context);
                ab.setTitle("로그아웃 하면 알람이 울리지 않습니다.\n로그아웃 하시겠습니까?");

                // 목록 클릭시 설정
                ab.setItems(items, (dialog, index) -> {
                    if(index == 0) {
                        cancelAlarm();
                        mFirebaseAuth.signOut();

                        LoginManager loginManager = LoginManager.getInstance();
                        if(loginManager != null)
                            loginManager.logOut();

                        Intent intent = new Intent(MyInfoActivity.this, AuthActivity.class);
                        startActivity(intent);
                    }

                    dialog.dismiss();
                });

                ab.show();
            } else if(position == 1) {
                String[] items = {"예", "아니오"};
                Context context = view.getContext();
                AlertDialog.Builder ab = new AlertDialog.Builder(context);
                ab.setTitle("계정을 삭제하면 식물정보가 모두 삭제됩니다. 계정을 삭제 하시겠습니까?");

                // 목록 클릭시 설정
                ab.setItems(items, (dialog, index) -> {
                    if(index == 0 && mFirebaseUser != null) {
                        cancelAlarm();
                        List<String> providers = mFirebaseUser.getProviders();
                        if(providers != null && providers.size() > 0) {
                            final String provider = providers.get(0);

                            if ("google.com".equals(provider)) {
                                token = "677847193937-qr7av5jubvngm6j73cc5oh6mebp2qcua.apps.googleusercontent.com";
                                credential = GoogleAuthProvider.getCredential(token, null);

/*                                        mFirebaseUser.getIdToken(true).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                                @Override
                                public void onSuccess(GetTokenResult getTokenResult) {
                                    System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!11");
                                    token = getTokenResult.getToken();
                                    credential = GoogleAuthProvider.getCredential(token, null);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MyInfoActivity.this, "계정 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
                                }
                            });*/
                            } else if ("facebook.com".equals(provider)) {
                                token = AccessToken.getCurrentAccessToken().getToken();
                                credential = FacebookAuthProvider.getCredential(token);
                            }

                            //final String uid = mFirebaseUser.getUid();
                            mFirebaseUser.reauthenticate(credential).addOnCompleteListener(task -> {
/*                                String uid = mFirebaseUser.getUid();
                                deleteFirebaseStoarge(uid);
                                deleteFirebaseDataBase(uid);*/

                                if ("facebook.com".equals(provider)) {
                                    //mFirebaseAuth.signOut();

                                    LoginManager loginManager = LoginManager.getInstance();
                                    if (loginManager != null)
                                        loginManager.logOut();
                                }

                                // 사용자 삭제
                                mFirebaseUser.delete().addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        if ("google.com".equals(provider))
                                            mFirebaseAuth.signOut();

                                        startActivity(new Intent(MyInfoActivity.this, AuthActivity.class));
                                        finish();
                                    }
                                });
                            });
                        }
                    }

                    dialog.dismiss();
                });

                ab.show();
            }
        });
    }

    private void cancelAlarm() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabase.getReference().child("plant").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String fUid = mFirebaseUser.getUid();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Plant plant = snapshot.getValue(Plant.class);

                    if(plant != null) {
                        String dUid = plant.getUid();

                        if(fUid.equals(dUid)) {
                            int alarmId = plant.getAlarmId();
                            AlarmService.INSTANCE.cancelAlarm(MyInfoActivity.this, alarmId);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Failed to read value
                Log.w("Hello", "Failed to read value.", databaseError.toException());
            }
        });
    }

    private void deleteFirebaseStoarge(final String uid) {
        firebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        final StorageReference storageRef = firebaseStorage.getReferenceFromUrl("gs://planting-planner.appspot.com");

        firebaseDatabase.getReference().child("plant").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Plant plant = snapshot.getValue(Plant.class);
                    String dUid = plant.getUid();

                    if(uid.equals(dUid)) {
                        String imageUrl = plant.getImageUrl();
                        StorageReference desertRef = storageRef.child(imageUrl);
                        desertRef.delete().addOnSuccessListener(aVoid -> {

                        }).addOnFailureListener(exception -> {
                            // Uh-oh, an error occurred!
                            Log.w("Hello", "Failed to delete value.");
                            exception.printStackTrace();
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Failed to read value
                Log.w("Hello", "Failed to read value.", databaseError.toException());
            }
        });
    }

    private void deleteFirebaseDataBase(final String uid) {
        firebaseDatabase.getReference().child("plant").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Plant plant = snapshot.getValue(Plant.class);
                    String dUid = plant.getUid();

                    if(uid.equals(dUid)) {
                        firebaseDatabase.getReference().child("plant").child(dUid).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Failed to read value
                Log.w("Hello", "Failed to read value.", databaseError.toException());
            }
        });
    }

    private void imageViewLayout(Uri photoUri) {
        ImageView imageView = findViewById(R.id.imageView);
        imageView.setBackground(new ShapeDrawable(new OvalShape()));
        imageView.setClipToOutline(true);
        Glide.with(imageView.getContext()).load(photoUri).into(imageView);
    }

}
