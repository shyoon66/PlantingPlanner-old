package com.yoonbae.plantingplanner;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AddActivity extends AppCompatActivity {

    private static final int CAMERA_CODE = 10;
    private static final int GALLERY_CODE = 100;
    private String mCurrentPhotoPath;
    DatePicker mDate;
    TextView mTxtDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get the ActionBar here to configure the way it behaves.
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);    // 커스터마이징 하기 위해 필요
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);      // 뒤로가기 버튼, 디폴트로 true만 해도 백버튼이 생김
        //actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_left);
        requestPermission();

        Button button = (Button) findViewById(R.id.imgAddButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Context context = view.getContext();
                final String items[] = {"카메라로 찍기", "앨범에서 가져오기", "취소"};
                AlertDialog.Builder ab = new AlertDialog.Builder(AddActivity.this);
                ab.setTitle("사진 선택");
                ab.setSingleChoiceItems(items, 0,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if (whichButton == 0) {
                                    chkCameraPermission(context);
                                } else if (whichButton == 1) {
                                    pickUpPicture();
                                }

                                dialog.dismiss();
                            }
                        });
                ab.show();
            }
        });

        mDate = (DatePicker) findViewById(R.id.datepicker);
        mTxtDate = (TextView) findViewById(R.id.txtdate);

        //처음 DatePicker를 오늘 날짜로 초기화한다.
        //그리고 리스너를 등록한다.
        mDate.init(mDate.getYear(), mDate.getMonth(), mDate.getDayOfMonth(),
            new DatePicker.OnDateChangedListener() {
                //값이 바뀔때마다 텍스트뷰의 값을 바꿔준다.
                @Override
                public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    //monthOfYear는 0값이 1월을 뜻하므로 1을 더해줌 나머지는 같다.
                    mTxtDate.setText(String.format("%d/%d/%d", year, monthOfYear + 1, dayOfMonth));
                }
        });

        //선택기로부터 날짜 조사
        findViewById(R.id.txtdate).setOnClickListener(new View.OnClickListener() {
            //버튼 클릭시 DatePicker로부터 값을 읽어와서 Toast메시지로 보여준다.
            @Override
            public void onClick(View v) {
                String result = String.format("%d년 %d월 %d일", mDate.getYear(), mDate.getMonth() + 1, mDate.getDayOfMonth());
                Toast.makeText(AddActivity.this, result, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_add,menu);
        //return super.onCreateOptionsMenu(menu);

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    void pickUpPicture() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_CODE);
    }

    void chkCameraPermission(Context context) {
        boolean camera = (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
        boolean write = (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);

        if(camera && write) {
            // 사진찍는 인텐트 코드 넣기
            takePicture();
        } else {
            Toast.makeText(AddActivity.this, "카메라 권한 및 쓰기 권한이 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    void requestPermission() {
        String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ArrayList<String> listPermissionNeeded = new ArrayList<>();

        for(String permission : permissions) {
            if(ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
                // 권한이 허가가 안됐을 경우 요청할 권한을 찾는 부분
                listPermissionNeeded.add(permission);
            }
        }

        if(!listPermissionNeeded.isEmpty()) {
            // 권한 요청 하는 부분
            ActivityCompat.requestPermissions(this, listPermissionNeeded.toArray(new String[listPermissionNeeded.size()]), 1);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% storageDir " + storageDir.getAbsolutePath());
        //File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg", /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@222 mCurrentPhotoPath = " + mCurrentPhotoPath);
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ImageView imageView = findViewById(R.id.imageView);

        if(resultCode == CAMERA_CODE) {
           imageView.setImageBitmap(BitmapFactory.decodeFile(mCurrentPhotoPath));
        } else if(requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& uri = " + uri);
            imageView.setImageURI(uri);
        }
    }

    void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            File photoFile = createImageFile();
            Uri photoUri = FileProvider.getUriForFile(this, "com.yoonbae.plantingplanner.fileprovider", photoFile);
            System.out.println("######################################3 photoUri = " + photoUri);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(intent, CAMERA_CODE);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
