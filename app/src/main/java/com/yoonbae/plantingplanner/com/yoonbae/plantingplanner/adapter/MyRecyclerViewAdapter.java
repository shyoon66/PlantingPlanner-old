package com.yoonbae.plantingplanner.com.yoonbae.plantingplanner.adapter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.yoonbae.plantingplanner.AddActivity;
import com.yoonbae.plantingplanner.BroadcastD;
import com.yoonbae.plantingplanner.R;
import com.yoonbae.plantingplanner.ViewActivity;
import com.yoonbae.plantingplanner.com.yoonbae.plantingplanner.vo.Plant;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Plant> plantList;
    private Context context;
    private FirebaseStorage storage;

    public MyRecyclerViewAdapter(List<Plant> plantList, Context context) {
        this.plantList = plantList;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cardview, parent, false);
        return new RowCell(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        final Plant plant = plantList.get(position);
        ((RowCell)holder).name.setText(plant.getName());
        ((RowCell)holder).kind.setText(plant.getKind());
        Glide.with(((RowCell) holder).imageView.getContext()).load(plant.getImageUrl()).into(((RowCell) holder).imageView);

        ((RowCell)holder).imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Intent intent = new Intent(context, ViewActivity.class);
            intent.putExtra("name", plant.getName());
            intent.putExtra("kind", plant.getKind());
            intent.putExtra("intro", plant.getIntro());
            intent.putExtra("imageUrl", plant.getImageUrl());
            intent.putExtra("uid", plant.getUid());
            intent.putExtra("adoptionDate", plant.getAdoptionDate());
            intent.putExtra("alarm", plant.getAlarm());
            intent.putExtra("alarmDate", plant.getAlarmDate());
            intent.putExtra("alarmTime", plant.getAlarmTime());
            intent.putExtra("period", plant.getPeriod());
            intent.putExtra("key", plant.getKey());
            context.startActivity(intent);
            }
        });

        ((RowCell)holder).imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String items[] = {"식물수정", "식물삭제", "취소"};
                AlertDialog.Builder ab = new AlertDialog.Builder(context);
                ab.setTitle("");
                ab.setItems(items, new DialogInterface.OnClickListener() {    // 목록 클릭시 설정
                    public void onClick(DialogInterface dialog, int index) {
                    if(index == 0) {
                        Intent intent = new Intent(context, AddActivity.class);
                        intent.putExtra("FLAG", "U");
                        intent.putExtra("name", plant.getName());
                        intent.putExtra("kind", plant.getKind());
                        intent.putExtra("intro", plant.getIntro());
                        intent.putExtra("imageUrl", plant.getImageUrl());
                        intent.putExtra("uid", plant.getUid());
                        intent.putExtra("adoptionDate", plant.getAdoptionDate());
                        intent.putExtra("alarm", plant.getAlarm());
                        intent.putExtra("alarmDate", plant.getAlarmDate());
                        intent.putExtra("alarmTime", plant.getAlarmTime());
                        intent.putExtra("period", plant.getPeriod());
                        intent.putExtra("alarmId", plant.getAlarmId());
                        intent.putExtra("key", plant.getKey());
                        context.startActivity(intent);
                    } else if(index == 1) {
                        deletePlant(position);
                    } else {
                        dialog.dismiss();
                    }

                    dialog.dismiss();
                    }
                });

                ab.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return plantList.size();
    }

    private class RowCell extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView name;
        public TextView kind;
        ImageButton imageButton;

        public RowCell(View view) {
            super(view);
            imageView = view.findViewById(R.id.cardview_imageview);
            name = view.findViewById(R.id.cardview_name);
            kind = view.findViewById(R.id.cardview_kind);
            imageButton = view.findViewById(R.id.cardview_btn);
        }
    }

    private void deletePlant(final int position) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String key = plantList.get(position).getKey();

        database.getReference().child("plant").child(key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                String imageName = plantList.get(position).getImageName();
                storage = FirebaseStorage.getInstance();
                storage.getReference().child("images").child(imageName).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "식물이 삭제됐습니다.", Toast.LENGTH_SHORT).show();
                        int alarmId = plantList.get(position).getAlarmId();
                        cancleAlarm(alarmId);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "식물 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "식물 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cancleAlarm(int alarmId) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, BroadcastD.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if(sender != null) {
            am.cancel(sender);
            sender.cancel();
        }
    }

}
