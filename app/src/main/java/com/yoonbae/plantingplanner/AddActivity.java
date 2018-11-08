package com.yoonbae.plantingplanner;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
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

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yoonbae.plantingplanner.com.yoonbae.plantingplanner.vo.Plant;

import org.threeten.bp.LocalDateTime;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.loader.content.CursorLoader;

public class AddActivity extends AppCompatActivity {

    private static final int CAMERA_CODE = 10;
    private static final int GALLERY_CODE = 100;
    private final int REQUEST_WIDTH = 1024;
    private final int REQUEST_HEIGHT = 1024;

    private String mCurrentPhotoPath;
    private String flag;

    private ImageView imageView;
    private EditText mName;
    private EditText mKind;
    private EditText mIntro;
    private TextView mAdoptionDate;
    private Switch mAlarm;
    private TextView mAlarmDate;
    private Spinner mPeriodSpinner;
    private TextView mAlarmTime;
    private TextView mToolbar_title;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private String firebaseImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        imageView = findViewById(R.id.imageView);
        mName = findViewById(R.id.name);
        mKind = findViewById(R.id.kind);
        mIntro = findViewById(R.id.intro);
        mAlarm = findViewById(R.id.alarm);
        mPeriodSpinner = findViewById(R.id.periodSpinner);
        mToolbar_title = findViewById(R.id.toolbar_title);
        mFirebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        mPeriodSpinner.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ((TextView) mPeriodSpinner.getSelectedView()).setTextColor(Color.rgb(121, 121, 121));
                ((TextView) mPeriodSpinner.getSelectedView()).setTextSize(16);
                ((TextView) mPeriodSpinner.getSelectedView()).setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.waterPeriod));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Intent intent = getIntent();
        flag = intent.getExtras().getString("FLAG");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get the ActionBar here to configure the way it behaves.
        ActionBar actionBar = getSupportActionBar();
        //actionBar.setIcon(R.drawable.baseline_keyboard_arrow_left_black_24);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);    // 커스터마이징 하기 위해 필요
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);      // 뒤로가기 버튼, 디폴트로 true만 해도 백버튼이 생김
        actionBar.setHomeAsUpIndicator(R.drawable.baseline_keyboard_arrow_left_black_24);
        requestPermission();

        Button button = findViewById(R.id.imgAddButton);
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

        if("U".equals(flag)) {
            mToolbar_title.setText("식물수정");
            mName.setText(intent.getExtras().getString("name"));
            mKind.setText(intent.getStringExtra("kind"));
            mIntro.setText(intent.getStringExtra("intro"));
            Glide.with(imageView).load(intent.getStringExtra("imageUrl")).into(imageView);
        }

        Calendar calendar = Calendar.getInstance();
        mAdoptionDate = findViewById(R.id.adoptionDate);

        if("I".equals(flag)) {
            mAdoptionDate.setText(calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DATE));
        } else {
            String adoptionDate = intent.getStringExtra("adoptionDate");
            mAdoptionDate.setText(adoptionDate);
        }

        mAdoptionDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(mAdoptionDate);
            }
        });

        String alarm = intent.getExtras().getString("alarm");
        if("I".equals(flag)) {
            mAlarm.setChecked(true);
        } else if("U".equals(flag) && "Y".equals(alarm)) {
            mAlarm.setChecked(true);
        }

        mAlarmDate = findViewById(R.id.alarmDate);

        if("I".equals(flag)) {
            mAlarmDate.setText(calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DATE));
        } else {
            String alarmDate = intent.getExtras().getString("alarmDate");
            mAlarmDate.setText(alarmDate);
        }

        mAlarmDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(mAlarmDate);
            }
        });

        mAlarmTime = findViewById(R.id.alarmTime);
        TimeZone jst = TimeZone.getTimeZone ("JST");

        if("I".equals(flag)) {
            mAlarmTime.setText(calendar.get(Calendar.HOUR_OF_DAY) + "시 " + calendar.get(Calendar.MINUTE) + "분");
        } else {
            String alarmTime = intent.getExtras().getString("alarmTime");
            mAlarmTime.setText(alarmTime);
        }

        Resources res = getResources();
        String[] waterPeriodArr = res.getStringArray(R.array.waterPeriod);
        String period = intent.getStringExtra("period");

        for(int i = 0; i < waterPeriodArr.length; i++) {
            if(waterPeriodArr[i].equals(period)) {
                mPeriodSpinner.setSelection(i);
            }
        }

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

        TimePickerDialog timePickerDialog = new TimePickerDialog(AddActivity.this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                mAlarmTime.setText(i + "시 " + i1 + "분");
            }
        }, hour, minute, true);


        timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
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
        },year, month - 1, day); // 기본값 연월일

        datePickerDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());
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
                if("I".equals(flag)) {
                    insert();
                } else {
                    update();
                }

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
                uploadCameraImageByFirebase(path, requestCode);
            } else if(requestCode == GALLERY_CODE) {
                path = getPath(data.getData());
                uploadGalleryImageByFirebase(path, data.getData(), requestCode);
            }

            //uploadCameraImageByFirebase(path, data.getData(), requestCode);
        }
    }

    private Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private Bitmap resizeImage(String imgPath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        Bitmap orgImage = BitmapFactory.decodeFile(imgPath, options);
        Bitmap resizeBitmap = Bitmap.createScaledBitmap(orgImage, REQUEST_WIDTH, REQUEST_HEIGHT, true);
        return resizeBitmap;
    }

    private void uploadCameraImageByFirebase(String path, final int requestCode) {
        // Create a storage reference from our app
        final StorageReference storageRef = storage.getReferenceFromUrl("gs://planting-planner.appspot.com");
        final int frequestCode = requestCode;

        Bitmap resizeBitmap = resizeImage(path);
        final Uri file = getImageUri(this, resizeBitmap);

        //Uri file = Uri.fromFile(new File(mCurrentPhotoPath));
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
                cameraImageView(file, frequestCode);
                Toast.makeText(AddActivity.this, "사진 등록이 성공했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadGalleryImageByFirebase(String path, Uri uri, final int requestCode) {
        // Create a storage reference from our app
        final StorageReference storageRef = storage.getReferenceFromUrl("gs://planting-planner.appspot.com");
        final int frequestCode = requestCode;
        final Uri furi = uri;

        Bitmap resizeBitmap = resizeImage(path);
        Uri file = getImageUri(this, resizeBitmap);

        //Uri file = Uri.fromFile(new File(path));
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
                galleryImageView(furi, frequestCode);
                Toast.makeText(AddActivity.this, "사진 등록이 성공했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getPath(Uri uri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(this, uri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();
        int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(index);
    }

    private void cameraImageView(Uri uri, int frequestCode) {
        ImageView imageview = findViewById(R.id.imageView);

        if(frequestCode == CAMERA_CODE) {
            Glide.with(imageView.getContext()).load(uri).into(imageview);
        }
    }

    private void galleryImageView(Uri uri, int frequestCode) {
        ImageView imageview = findViewById(R.id.imageView);

        if(frequestCode == GALLERY_CODE) {
            Glide.with(imageView.getContext()).load(uri).into(imageview);
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
        final String name = mName.getText().toString();
        final String kind = mKind.getText().toString();
        final String intro = mIntro.getText().toString();
        final String uid = mFirebaseAuth.getCurrentUser().getUid();
        final String userId = mFirebaseAuth.getCurrentUser().getEmail();

        AlertDialog.Builder ab = new AlertDialog.Builder(AddActivity.this);
        ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        if(imageView.getDrawable() == null) {
            ab.setMessage("사진을 등록해 주세요.");
            ab.show();
            return;
        }

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

        Calendar nowCal = Calendar.getInstance();
        String alarmDate = mAlarmDate.getText().toString();
        String[] alarmDateArr = alarmDate.split("-");
        int year = Integer.parseInt(alarmDateArr[0]);
        int month = Integer.parseInt(alarmDateArr[1]) - 1;
        int day = Integer.parseInt(alarmDateArr[2]);
        String alarmTime = mAlarmTime.getText().toString();
        int hour = Integer.parseInt(alarmTime.substring(0, alarmTime.indexOf("시")));
        int minute = Integer.parseInt(alarmTime.substring(alarmTime.indexOf("시") + 2, alarmTime.length() - 1));
        Calendar alarmCal = Calendar.getInstance();
        alarmCal.set(year, month, day, hour, minute);

        if(alarmCal.before(nowCal)) {
            ab.setMessage("알람시작일시는 현재시간 이후로 설정해 주세요.");
           ab.show();
            return;
        }

        LocalDateTime localDateTime = LocalDateTime.now();
        final int alarmId = localDateTime.getDayOfYear() + localDateTime.getMonth().getValue() + localDateTime.getDayOfYear() + localDateTime.getHour() + localDateTime.getMinute() + localDateTime.getSecond() + localDateTime.getNano();
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

                final String alarmDate = mAlarmDate.getText().toString();
                final String period = mPeriodSpinner.getSelectedItem().toString();
                final String alarmTime = mAlarmTime.getText().toString();
                Plant plant = new Plant(name, kind, imageName, imageUrl, intro, adoptionDate, alarm, alarmDate, period, alarmTime, alarmId, uid, userId, "");
                database.getReference().child("plant").push().setValue(plant).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if(mAlarm.isChecked()) {
                            int pod = getPeriod(period);
                            long intervalMillis = pod * 24 * 60 * 60 * 1000;
                            long alarmTimeInMillis = getAlarmTimeInMillis(alarmDate, alarmTime, pod);
                            new AddActivity.AlarmHATT((getApplicationContext())).Alarm(alarmTimeInMillis, intervalMillis, name, alarmId);
                        }

                        String msg = "등록이 완료됐습니다.";
                        showDialogAfterWork(msg);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddActivity.this, "식물 등록이 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(AddActivity.this, "식물 등록이 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDialogAfterWork(String msg) {
        AlertDialog.Builder ab = new AlertDialog.Builder(AddActivity.this);
        ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent(AddActivity.this, ListActivity.class);
                startActivity(intent);
                finish();
            }
        });
        ab.setMessage(msg);
        ab.show();
    }

    private void update() {
        final String name = mName.getText().toString();
        final String kind = mKind.getText().toString();
        final String intro = mIntro.getText().toString();
        final String uid = mFirebaseAuth.getCurrentUser().getUid();
        final String userId = mFirebaseAuth.getCurrentUser().getEmail();

        AlertDialog.Builder ab = new AlertDialog.Builder(AddActivity.this);
        ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        if(imageView.getDrawable() == null) {
            ab.setMessage("사진을 등록해 주세요.");
            ab.show();
            return;
        }

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

        Calendar nowCal = Calendar.getInstance();
        final String alarmDate = mAlarmDate.getText().toString();
        String[] alarmDateArr = alarmDate.split("-");
        int year = Integer.parseInt(alarmDateArr[0]);
        int month = Integer.parseInt(alarmDateArr[1]) - 1;
        int day = Integer.parseInt(alarmDateArr[2]);
        final String alarmTime = mAlarmTime.getText().toString();
        int hour = Integer.parseInt(alarmTime.substring(0, alarmTime.indexOf("시")));
        int minute = Integer.parseInt(alarmTime.substring(alarmTime.indexOf("시") + 2, alarmTime.length() - 1));
        Calendar alarmCal = Calendar.getInstance();
        alarmCal.set(year, month, day, hour, minute);

        if(alarmCal.before(nowCal)) {
            ab.setMessage("알람시작일시는 현재시간 이후로 설정해 주세요.");
            ab.show();
            return;
        }

        Intent intent = getIntent();
        final int alarmId = intent.getExtras().getInt("alarmId");
        if(firebaseImagePath != null && !"".equals(firebaseImagePath)) {
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

                    final String alarmDate = mAlarmDate.getText().toString();
                    final String period = mPeriodSpinner.getSelectedItem().toString();
                    final String alarmTime = mAlarmTime.getText().toString();
                    Intent intent = getIntent();

                    String key = intent.getStringExtra("key");
                    Map<String, Object> updateMap = new HashMap<String, Object>();
                    updateMap.put("name", name);
                    updateMap.put("kind", kind);
                    updateMap.put("imageName", imageName);
                    updateMap.put("imageUrl", imageUrl);
                    updateMap.put("intro", intro);
                    updateMap.put("adoptionDate", adoptionDate);
                    updateMap.put("alarm", alarm);
                    updateMap.put("alarmDate", alarmDate);
                    updateMap.put("period", period);
                    updateMap.put("alarmTime", alarmTime);
                    database.getReference().child("plant").child(key).updateChildren(updateMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            if(mAlarm.isChecked()) {
                                cancleAlarm(alarmId);
                                int pod = getPeriod(period);
                                long intervalMillis = pod * 24 * 60 * 60 * 1000;
                                long alarmTimeInMillis = getAlarmTimeInMillis(alarmDate, alarmTime, pod);
                                new AddActivity.AlarmHATT((getApplicationContext())).Alarm(alarmTimeInMillis, intervalMillis, name, alarmId);
                            } else {
                                cancleAlarm(alarmId);
                            }

                            String msg = "수정이 완료됐습니다.";
                            showDialogAfterWork(msg);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AddActivity.this, "식물 수정이 실패했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(AddActivity.this, "식물 수정이 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            String adoptionDate = mAdoptionDate.getText().toString();

            String alarm = "N";
            if(mAlarm.isChecked()) {
                alarm = "Y";
            }

            //final String alarmDate = mAlarmDate.getText().toString();
            final String period = mPeriodSpinner.getSelectedItem().toString();
            //final String alarmTime = mAlarmTime.getText().toString();
            String key = intent.getStringExtra("key");
            Map<String, Object> updateMap = new HashMap<String, Object>();
            updateMap.put("name", name);
            updateMap.put("kind", kind);
            updateMap.put("intro", intro);
            updateMap.put("adoptionDate", adoptionDate);
            updateMap.put("alarm", alarm);
            updateMap.put("alarmDate", alarmDate);
            updateMap.put("period", period);
            updateMap.put("alarmTime", alarmTime);
            database.getReference().child("plant").child(key).updateChildren(updateMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    if(mAlarm.isChecked()) {
                        cancleAlarm(alarmId);
                        int pod = getPeriod(period);
                        long intervalMillis = pod * 24 * 60 * 60 * 1000;
                        long alarmTimeInMillis = getAlarmTimeInMillis(alarmDate, alarmTime, pod);
                        new AddActivity.AlarmHATT((getApplicationContext())).Alarm(alarmTimeInMillis, intervalMillis, name, alarmId);
                    } else {
                        cancleAlarm(alarmId);
                    }

                    String msg = "수정이 완료됐습니다.";
                    showDialogAfterWork(msg);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddActivity.this, "식물 수정이 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private class AlarmHATT {
        private Context context;

        private AlarmHATT(Context context) {
            this.context = context;
        }

        private void Alarm(Long timeInMillis, Long intervalMillis, String name, int alarmId) {
            AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, BroadcastD.class);
            intent.putExtra("name", name);
            intent.putExtra("alarmId", alarmId);
            PendingIntent sender = PendingIntent.getBroadcast(AddActivity.this, 0, intent, 0);

            //알람 예약
            am.setRepeating(AlarmManager.RTC_WAKEUP, timeInMillis, intervalMillis, sender);
        }
    }

    private int getPeriod(String period) {
        int pod = 0;

        if("매일".equals(period)) {
            pod = 1;
        } else if("이틀".equals(period)) {
            pod = 2;
        } else {
            pod = Integer.parseInt(period.substring(0, period.length() - 1));
        }

        return pod;
    }

    private long getAlarmTimeInMillis(String alarmDate, String alarmTime, int pod) {
        String[] alarmDateArr = alarmDate.split("-");
        int year = Integer.parseInt(alarmDateArr[0]);
        int month = Integer.parseInt(alarmDateArr[1]);
        int dayOfYear = Integer.parseInt(alarmDateArr[2]);

        int hourOfDay = Integer.parseInt(alarmTime.substring(0, alarmTime.indexOf("시")));
        int minute = Integer.parseInt(alarmTime.substring(alarmTime.indexOf("시") + 2, alarmTime.length() - 1));

        Calendar alarmCalendar = Calendar.getInstance();
        alarmCalendar.set(year, month - 1, dayOfYear, hourOfDay, minute);
        LocalDate localDate = LocalDate.of(year, month, dayOfYear);

        long alarmTimeInMillis = alarmCalendar.getTimeInMillis();
        long nowTimeInMillis = Calendar.getInstance().getTimeInMillis();

        while(alarmTimeInMillis < nowTimeInMillis) {
            localDate.plusDays(pod);
            alarmCalendar.set(localDate.getYear(), localDate.getMonth().getValue() - 1, localDate.getDayOfMonth(), hourOfDay, minute);
            alarmTimeInMillis = alarmCalendar.getTimeInMillis();
        }

        return alarmTimeInMillis;
    }

    private void cancleAlarm(int alarmId) {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(AddActivity.this, BroadcastD.class);
        PendingIntent sender = PendingIntent.getBroadcast(AddActivity.this, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if(sender != null) {
            am.cancel(sender);
            sender.cancel();
        }
    }
}
