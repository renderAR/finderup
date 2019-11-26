package com.example.mypc.specialproject;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class NotificationAdapter extends FirestoreRecyclerAdapter<Notification, NotificationAdapter.NotificationHolder> {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private View v;

    public NotificationAdapter(@NonNull FirestoreRecyclerOptions<Notification> options) { super(options); }


    @NonNull
    @Override
    public NotificationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification, parent, false);
        return new NotificationHolder(v);
    }

    @Override
    protected void onBindViewHolder(@NonNull NotificationHolder notificationHolder, int i, @NonNull final Notification notification) {
        notificationHolder.content.setText(notification.getContent());

        Date notifDate = notification.getDate().toDate();
        Date now = Timestamp.now().toDate();
        SimpleDateFormat mdyFormat = new SimpleDateFormat ("MMM dd, yyyy");
        SimpleDateFormat hmFormat = new SimpleDateFormat("hh:mm a");
        SimpleDateFormat yFormat = new SimpleDateFormat ("yyyy");
        SimpleDateFormat mdFormat = new SimpleDateFormat("MMM dd");
        String timeStr = "";
        if(mdyFormat.format(notifDate).equals(mdyFormat.format(now))) timeStr += hmFormat.format(notifDate);
        else{
            if(yFormat.format(notifDate).equals(yFormat.format(now))) timeStr += mdFormat.format(notifDate) + " at " + hmFormat.format(notifDate);
            else timeStr += mdyFormat.format(notifDate) + " at " + hmFormat.format(notifDate);
        }
        notificationHolder.date.setText(timeStr);

        if(notification.isStatus()){
            notificationHolder.card.setBackgroundColor(Color.parseColor("#CCCCCC"));
        }else{
            notificationHolder.card.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }
        notificationHolder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!notification.isStatus()){
                    db.collection("users").document(user.getEmail().replace(".",",")).collection("notifications").document(notification.getType() + notification.getSource())
                            .update("status", true);
                }

                db.collection("reports").document(notification.getSource()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(!task.isSuccessful()) return;
                        if(task.getResult().exists()){
                            Report report = task.getResult().toObject(Report.class);

                            Intent intent = new Intent(v.getContext(), Activity_ViewReport.class);
                            intent.putExtra("reportId", notification.getSource());
                            intent.putExtra("notifType", notification.getType());
                            v.getContext().startActivity(intent);
                        }else{
                            Toast.makeText(v.getContext(), "Report not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    class NotificationHolder extends RecyclerView.ViewHolder {
        RelativeLayout card;
        TextView content;
        TextView date;

        public NotificationHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.notif_card);
            content = itemView.findViewById(R.id.notif_content);
            date = itemView.findViewById(R.id.notif_date);
        }
    }
}
