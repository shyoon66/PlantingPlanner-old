package com.yoonbae.plantingplanner;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.loader.content.CursorLoader;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yoonbae.plantingplanner.com.yoonbae.plantingplanner.vo.Plant;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class AddActivity extends AppCompatActivity {

    private static final int CAMERA_CODE = 10;
    private static final int GALLERY_CODE = 100;
    private String mCurrentPhotoPath;
    private EditText mName;
    private EditText mKind;
    private EditText mIntro;
    private TextView mAdoptionDate;
    private Switch mAlarm;
    private TextView mAlarmDate;
    private Spinner mPeriodSpinner;
    private TextView mAlarmTime;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private String firebaseImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        mName = findViewById(R.id.name);
        mKind = findViewById(R.id.kind);
        mIntro = findViewById(R.id.intro);
        mAlarm = findViewById(R.id.alarm);
        mPeriodSpinner = findViewById(R.id.periodSpinner);
        mFirebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get the ActionBar here to configure the way it behaves.
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);    // 커스터마이징 하기 위해 필요
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);      // 뒤로가기 버튼, 디폴트로 true만 해도 백버튼이 생김
        requestPermission();

        Button button = (Button) findViewById(R.id.imgAddButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Context context = view.getContext();
                final String items[] = {"카메라로 찍기", "앨범에서 가져오기", "취소"};
                AlertDialog.Builder ab = new AlertDialog.Builder(AddActivity.this);
                ab.setTitle("사진 선택");
                ab.setItems(items, new DialogInterface.OnClickListener() {    // 목록 클릭시 설정
                    public void onClick(DialogInterface dialog, int index) {
                        if(index == 0) {
                            chkCameraPermission(context);
                        } else if(index == 1) {
                            pickUpPicture();
                        }

                        dialog.dismiss();
                    }
                });
                ab.show();
            }
        });

        Calendar calendar = Calendar.getInstance();
        mAdoptionDate = findViewById(R.id.adoptionDate);
        mAdoptionDate.setText(calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DATE));
        mAdoptionDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(mAdoptionDate);
            }
        });

        mAlarmDate = findViewById(R.id.alarmDate);
        mAlarmDate.setText(calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DATE));
        mAlarmDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(mAlarmDate);
            }
        });

        mAlarmTime = findViewById(R.id.alarmTime);
        TimeZone jst = TimeZone.getTimeZone ("JST");
        Calendar cal = Calendar.getInstance (jst); // 주어진 시간대에 맞게 현재 시각으로 초기화된 GregorianCalender 객체를 반환.// 또는 Calendar now = Calendar.getInstance(Locale.KOREA);
        mAlarmTime.setText(calendar.get(Calendar.HOUR_OF_DAY) + "시 " + calendar.get(Calendar.MINUTE) + "분");
        mAlarmTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePicker();
            }
        });
    }

    private void showTimePicker() {
        Calendar mCurrentTime = Calendar.getInstance();
        int hour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mCurrentTime.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(AddActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                mAlarmTime.setText(i + "시 " + i1 + "분");
            }
        }, hour, minute, true);

        timePickerDialog.show();
    }

    private void showDialog(final TextView pDate) {
        String date = pDate.getText().toString();
        int year = Integer.parseInt(date.substring(0, 4));
        int month = Integer.parseInt(date.substring(date.indexOf("-") + 1, date.indexOf("-") + 3).replace("-", ""));
        int day = Integer.parseInt(date.substring(date.length() - 2, date.length()).replace("-", ""));

        DatePickerDialog datePickerDialog = new DatePickerDialog(AddActivity.this, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear,int dayOfMonth) {
                pDate.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
            }
        },year, month, day); // 기본값 연월일

        datePickerDialog.getDatePicker().setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        datePickerDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                Intent intent = new Intent(AddActivity.this, ListActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.action_insert: {
                insert();
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
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String path = null;

        if(resultCode == RESULT_OK) {
            if(requestCode == CAMERA_CODE) {
                path = mCurrentPhotoPath;
            } else if(requestCode == GALLERY_CODE) {
                path = getPath(data.getData());
            }

            uploadImageByFirebase(path, data.getData(), requestCode);
        }
    }

    private String getPath(Uri uri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(this, uri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();
        int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(index);
    }

    private void uploadImageByFirebase(String path, Uri uri, final int requestCode) {
        // Create a storage reference from our app
        final StorageReference storageRef = storage.getReferenceFromUrl("gs://planting-planner.appspot.com");
        final Uri furi = uri;
        final int frequestCode = requestCode;

        Uri file = Uri.fromFile(new File(path));
        firebaseImagePath = "images/" + file.getLastPathSegment();
        StorageReference riversRef = storageRef.child(firebaseImagePath);
        UploadTask uploadTask = riversRef.putFile(file);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(AddActivity.this, "사진 등록이 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageView(furi, frequestCode);
                Toast.makeText(AddActivity.this, "사진 등록이 성공했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void imageView(Uri uri, int frequestCode) {
        ImageView imageview = findViewById(R.id.imageView);

        if(frequestCode == CAMERA_CODE) {
            imageview.setImageBitmap(BitmapFactory.decodeFile(mCurrentPhotoPath));
        } else if(frequestCode == GALLERY_CODE) {
            imageview.setImageURI(uri);
        }
    }

    void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            File photoFile = createImageFile();
            Uri photoUri = FileProvider.getUriForFile(this, "com.yoonbae.plantingplanner.fileprovider", photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(intent, CAMERA_CODE);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void insert() {
        AlertDialog.Builder ab = new AlertDialog.Builder(AddActivity.this);
        ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        final String name = mName.getText().toString();
        final String kind = mKind.getText().toString();
        final String intro = mIntro.getText().toString();
        final String uid = mFirebaseAuth.getCurrentUser().getUid();
        final String userId = mFirebaseAuth.getCurrentUser().getEmail();

        if("".equals(name)) {
            ab.setMessage("이름을 입력해주세요.");
            ab.show();
            return;
        }

        if("".equals(kind)) {
            ab.setMessage("종류를 입력해주세요.");
            ab.show();
            return;
        }

        if("".equals(intro)) {
            ab.setMessage("소개를 입력해주세요.");
            ab.show();
            return;
        }

        StorageReference storageRef = storage.getReferenceFromUrl("gs://planting-planner.appspot.com");
        Task<Uri> uriTask = storageRef.child(firebaseImagePath).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String imageUrl = uri.toString();
                String[] pathStrArr = uri.getPath().split("/");
                String imageName = pathStrArr[pathStrArr.length - 1];
                String adoptionDate = mAdoptionDate.getText().toString();

                String alarm = "N";
                if(mAlarm.isChecked()) {
                    alarm = "Y";
                }

                String alarmDate = mAlarmDate.getText().toString();
                String period = mPeriodSpinner.getSelectedItem().toString();
                String alarmTime = mAlarmTime.getText().toString();
                Plant plant = new Plant(name, kind, imageName, imageUrl, intro, adoptionDate, alarm, alarmDate, period, alarmTime, uid, userId);
                database.getReference().child("plant").push().setValue(plant);
                showDialogAfterinsert();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(AddActivity.this, "식물 등록이 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDialogAfterinsert() {
        AlertDialog.Builder ab = new AlertDialog.Builder(AddActivity.this);
        ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent(AddActivity.this, ListActivity.class);
                startActivity(intent);
            }
        });
        ab.setMessage("등록이 완료됐습니다.");
        ab.show();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
}
