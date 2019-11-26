package com.example.mypc.specialproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.annotation.Nullable;

public class Activity_ViewReport extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String reportId;

    private Report reportRef;
    private boolean owner = false;
    private boolean visited = false;

    TextView textStatus;
    TextView textTitle;
    TextView textReporter;
    TextView textDate;
    TextView textContent;
    TextView textPMimage;
    RelativeLayout textPM;
    ImageView imageItem ;
    RelativeLayout linkmain;
    TextView linkLabel;
    TextView linkUser;
    TextView linkPMimage;
    RelativeLayout linkPM;
    RelativeLayout main;

    @Override
    protected void onStart() {
        super.onStart();
        db.collection("reports").document(reportId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(e!=null) return;

                if(documentSnapshot.exists()){
                    reportRef = documentSnapshot.toObject(Report.class);

                    if(reportRef.isResolved()){
                        textStatus.setTextColor(Color.parseColor("#00c700"));
                        if(reportRef.getStatus() == 1) textStatus.setText("Resolved - Formerly Lost");
                        else textStatus.setText("Resolved - Formerly Found");
                    }else{
                        if(reportRef.getStatus() == 1){
                            textStatus.setText("Lost");
                            textStatus.setTextColor(Color.parseColor("#ede600"));
                        }
                        else{
                            textStatus.setText("Found");
                            textStatus.setTextColor(Color.parseColor("#00A0FF"));
                        }
                    }

                    textTitle.setText(reportRef.getTitle());

                    String content = "";
                    content += "Item Description:\n\t\t" + reportRef.getContent();
                    if(reportRef.getLocation().length()>0){
                        switch(reportRef.getStatus()){
                            case 1:
                                if(!reportRef.isResolved()) content += "\n\nItem Lost At:\n\t\t" + reportRef.getLocation();
                                else content += "\n\nItem Formerly Lost At:\n\t\t" + reportRef.getLocation();
                                break;
                            default:
                                if(!reportRef.isResolved()) content += "\n\nItem Found At:\n\t\t" + reportRef.getLocation();
                                else content += "\n\nItem Formerly Found At:\n\t\t" + reportRef.getLocation();
                        }
                    }
                    if(reportRef.getOther().length()>0) content += "\n\nOther Info:\n\t\t" + reportRef.getOther();
                    textContent.setText(content);


                    if(reportRef.getReporterID().equals(user.getEmail().replace(".",","))){
                        owner = true;
                        textReporter.setText("You ");
                        textReporter.setTextColor(Color.parseColor("#D15050"));
                        if(reportRef.getReporterEmail().contains("up.edu.ph")){
                            textReporter.setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.ic_check_circle, 0);
                        }else{
                            textReporter.setCompoundDrawablesWithIntrinsicBounds(0,0, 0, 0);
                        }
                        textPMimage.setVisibility(View.GONE);
                        textPM.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) { }
                        });
                    }else{
                        owner = false;
                        textReporter.setText(reportRef.getReporterName() + " ");
                        textReporter.setTextColor(Color.parseColor("#222222"));
                        if(reportRef.getReporterEmail().contains("up.edu.ph")){
                            textReporter.setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.ic_check_circle, 0);
                        }else{
                            textReporter.setCompoundDrawablesWithIntrinsicBounds(0,0, 0, 0);
                        }
                        textPMimage.setVisibility(View.VISIBLE);
                        textPM.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(Activity_ViewReport.this, Activity_ViewChatMessages.class);
                                intent.putExtra("receiverId", reportRef.getReporterID());
                                intent.putExtra("receiverName", reportRef.getReporterName());
                                intent.putExtra("receiverEmail", reportRef.getReporterEmail());
                                intent.putExtra("receiverPhotoUrl", reportRef.getReporterPhotoUrl());
                                startActivity(intent);
                            }
                        });
                    }
                    invalidateOptionsMenu();

                    if(reportRef.isResolved() && reportRef.getLinkedUser() != null){
                        linkLabel.setText((reportRef.getStatus() == 1 ? "Returned by " : "Claimed by "));

                        if(reportRef.getLinkedUser().get(0).equals(user.getEmail().replace(".",","))){
                            linkUser.setText("You ");
                            linkUser.setTextColor(Color.parseColor("#D15050"));
                            linkPMimage.setVisibility(View.GONE);
                            linkPM.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) { }
                            });
                        }else{
                            linkUser.setText(reportRef.getLinkedUser().get(2) + " ");
                            linkUser.setTextColor(Color.parseColor("#222222"));
                            linkPMimage.setVisibility(View.VISIBLE);
                            linkPM.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(Activity_ViewReport.this, Activity_ViewChatMessages.class);
                                    intent.putExtra("receiverId", reportRef.getLinkedUser().get(0));
                                    intent.putExtra("receiverName", reportRef.getLinkedUser().get(2));
                                    intent.putExtra("receiverEmail", reportRef.getLinkedUser().get(3));
                                    intent.putExtra("receiverPhotoUrl", reportRef.getLinkedUser().get(4));
                                    startActivity(intent);
                                }
                            });
                        }

                        if(reportRef.getLinkedUser().get(3).contains("up.edu.ph")){
                            linkUser.setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.ic_check_circle, 0);
                        }else{
                            linkUser.setCompoundDrawablesWithIntrinsicBounds(0,0, 0, 0);
                        }
                        linkLabel.setVisibility(View.VISIBLE);
                        linkUser.setVisibility(View.VISIBLE);
                        linkPM.setVisibility(View.VISIBLE);
                    }else{
                        linkPM.setVisibility(View.GONE);
                        linkLabel.setVisibility(View.GONE);
                        linkUser.setVisibility(View.GONE);
                        linkPMimage.setVisibility(View.GONE);
                        linkPM.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) { }
                        });
                    }

                    Date timeAdded = reportRef.getTimestampAdded().toDate();
                    Date now = Timestamp.now().toDate();
                    SimpleDateFormat mdyFormat = new SimpleDateFormat ("MMM dd, yyyy");
                    SimpleDateFormat hmFormat = new SimpleDateFormat("hh:mm a");
                    SimpleDateFormat yFormat = new SimpleDateFormat ("yyyy");
                    SimpleDateFormat mdFormat = new SimpleDateFormat("MMM dd");
                    String timeStr = "";
                    if(mdyFormat.format(timeAdded).equals(mdyFormat.format(now))) timeStr += hmFormat.format(timeAdded);
                    else{
                        if(yFormat.format(timeAdded).equals(yFormat.format(now))) timeStr += mdFormat.format(timeAdded) + " at " + hmFormat.format(timeAdded);
                        else timeStr += mdyFormat.format(timeAdded) + " at " + hmFormat.format(timeAdded);
                    }
                    if(reportRef.getTimestampUpdated().compareTo(reportRef.getTimestampAdded()) != 0){
                        Date timestampUpdated = reportRef.getTimestampUpdated().toDate();
                        if(mdyFormat.format(timestampUpdated).equals(mdyFormat.format(now))) timeStr += " · Updated " + hmFormat.format(timestampUpdated);
                        else{
                            if(yFormat.format(timestampUpdated).equals(yFormat.format(now))) timeStr += " · Updated on " + mdFormat.format(timestampUpdated);
                            else timeStr += " · Updated on " + mdyFormat.format(timestampUpdated);
                        }
                    }
                    textDate.setText(timeStr + " ");


                    if(reportRef.getImageUrl() != null) {
                        CircularProgressDrawable progress = new CircularProgressDrawable(imageItem.getContext());
                        imageItem.layout(0,0,0,0);
                        Glide.with(Activity_ViewReport.this).load(reportRef.getImageUrl()).into(imageItem);
                        imageItem.setVisibility(View.VISIBLE);
                    }

                    main.setVisibility(View.VISIBLE);
                    if(getIntent().getExtras().getInt("notifType") == 2 && !visited) loadComments();
                }else{
                    finish();
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewreport);

        setTitle("View Report");
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);

        main = findViewById(R.id.reportmain_main);
        textStatus = findViewById(R.id.reportmain_status);
        textTitle = findViewById(R.id.reportmain_title);
        textReporter = findViewById(R.id.reportmain_reporter);
        textDate = findViewById(R.id.reportmain_date);
        textContent = findViewById(R.id.reportmain_contenttext);
        textPMimage =  findViewById(R.id.reportmain_pm);
        textPM =  findViewById(R.id.reportmain_reportersection);
        imageItem = findViewById(R.id.reportmain_image);
        linkmain = findViewById(R.id.reportmain_linkmain);
        linkLabel = findViewById(R.id.reportmain_linklabel);
        linkUser = findViewById(R.id.reportmain_linkuser);
        linkPMimage = findViewById(R.id.reportmain_linkpm);
        linkPM = findViewById(R.id.reportmain_link);

        reportId = getIntent().getExtras().getString("reportId");

        db.collection("reports").document(reportId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().exists()){
                        reportRef = task.getResult().toObject(Report.class);

                        if(reportRef.isResolved()){
                            textStatus.setTextColor(Color.parseColor("#15DD15"));
                            if(reportRef.getStatus() == 1) textStatus.setText("Resolved - Formerly Lost");
                            else textStatus.setText("Resolved - Formerly Found");
                        }else{
                            if(reportRef.getStatus() == 1){
                                textStatus.setText("Lost");
                                textStatus.setTextColor(Color.parseColor("#ede600"));
                            }
                            else{
                                textStatus.setText("Found");
                                textStatus.setTextColor(Color.parseColor("#00A0FF"));
                            }
                        }

                        textTitle.setText(reportRef.getTitle());

                        String content = "";
                        content += "Item Description:\n\t\t" + reportRef.getContent();
                        if(reportRef.getLocation().length()>0){
                            switch(reportRef.getStatus()){
                                case 1:
                                    if(!reportRef.isResolved()) content += "\n\nItem Lost At:\n\t\t" + reportRef.getLocation();
                                    else content += "\n\nItem Formerly Lost At:\n\t\t" + reportRef.getLocation();
                                    break;
                                default:
                                    if(!reportRef.isResolved()) content += "\n\nItem Found At:\n\t\t" + reportRef.getLocation();
                                    else content += "\n\nItem Formerly Found At:\n\t\t" + reportRef.getLocation();
                            }
                        }
                        if(reportRef.getOther().length()>0) content += "\n\nOther Info:\n\t\t" + reportRef.getOther();
                        textContent.setText(content);



                        if(reportRef.getReporterID().equals(user.getEmail().replace(".",","))){
                            owner = true;
                            textReporter.setText("You ");
                            textReporter.setTextColor(Color.parseColor("#D15050"));
                            if(reportRef.getReporterEmail().contains("up.edu.ph")){
                                textReporter.setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.ic_check_circle, 0);
                            }else{
                                textReporter.setCompoundDrawablesWithIntrinsicBounds(0,0, 0, 0);
                            }
                            textPMimage.setVisibility(View.GONE);
                            textPM.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) { }
                            });
                        }else{
                            owner = false;
                            textReporter.setText(reportRef.getReporterName() + " ");
                            textReporter.setTextColor(Color.parseColor("#222222"));
                            if(reportRef.getReporterEmail().contains("up.edu.ph")){
                                textReporter.setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.ic_check_circle, 0);
                            }else{
                                textReporter.setCompoundDrawablesWithIntrinsicBounds(0,0, 0, 0);
                            }
                            textPMimage.setVisibility(View.VISIBLE);
                            textPM.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(Activity_ViewReport.this, Activity_ViewChatMessages.class);
                                    intent.putExtra("receiverId", reportRef.getReporterID());
                                    intent.putExtra("receiverName", reportRef.getReporterName());
                                    intent.putExtra("receiverEmail", reportRef.getReporterEmail());
                                    intent.putExtra("receiverPhotoUrl", reportRef.getReporterPhotoUrl());
                                    startActivity(intent);
                                }
                            });
                        }
                        invalidateOptionsMenu();

                        if(reportRef.isResolved() && reportRef.getLinkedUser() != null){
                            linkLabel.setText((reportRef.getStatus() == 1 ? "Returned by " : "Claimed by "));

                            if(reportRef.getLinkedUser().get(0).equals(user.getEmail().replace(".",","))){
                                linkUser.setText("You ");
                                linkUser.setTextColor(Color.parseColor("#D15050"));
                                linkPMimage.setVisibility(View.GONE);
                                linkPM.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) { }
                                });
                            }else{
                                linkUser.setText(reportRef.getLinkedUser().get(2) + " ");
                                linkUser.setTextColor(Color.parseColor("#222222"));
                                linkPMimage.setVisibility(View.VISIBLE);
                                linkPM.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(Activity_ViewReport.this, Activity_ViewChatMessages.class);
                                        intent.putExtra("receiverId", reportRef.getLinkedUser().get(0));
                                        intent.putExtra("receiverName", reportRef.getLinkedUser().get(2));
                                        intent.putExtra("receiverEmail", reportRef.getLinkedUser().get(3));
                                        intent.putExtra("receiverPhotoUrl", reportRef.getLinkedUser().get(4));
                                        startActivity(intent);
                                    }
                                });
                            }

                            if(reportRef.getLinkedUser().get(3).contains("up.edu.ph")){
                                linkUser.setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.ic_check_circle, 0);
                            }else{
                                linkUser.setCompoundDrawablesWithIntrinsicBounds(0,0, 0, 0);
                            }
                            linkLabel.setVisibility(View.VISIBLE);
                            linkUser.setVisibility(View.VISIBLE);
                            linkPM.setVisibility(View.VISIBLE);
                        }else{
                            linkPM.setVisibility(View.GONE);
                            linkLabel.setVisibility(View.GONE);
                            linkUser.setVisibility(View.GONE);
                            linkPMimage.setVisibility(View.GONE);
                            linkPM.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) { }
                            });
                        }

                        Date timeAdded = reportRef.getTimestampAdded().toDate();
                        Date now = Timestamp.now().toDate();
                        SimpleDateFormat mdyFormat = new SimpleDateFormat ("MMM dd, yyyy");
                        SimpleDateFormat hmFormat = new SimpleDateFormat("hh:mm a");
                        SimpleDateFormat yFormat = new SimpleDateFormat ("yyyy");
                        SimpleDateFormat mdFormat = new SimpleDateFormat("MMM dd");
                        String timeStr = "";
                        if(mdyFormat.format(timeAdded).equals(mdyFormat.format(now))) timeStr += hmFormat.format(timeAdded);
                        else{
                            if(yFormat.format(timeAdded).equals(yFormat.format(now))) timeStr += mdFormat.format(timeAdded) + " at " + hmFormat.format(timeAdded);
                            else timeStr += mdyFormat.format(timeAdded) + " at " + hmFormat.format(timeAdded);
                        }
                        if(reportRef.getTimestampUpdated().compareTo(reportRef.getTimestampAdded()) != 0){
                            Date timestampUpdated = reportRef.getTimestampUpdated().toDate();
                            if(mdyFormat.format(timestampUpdated).equals(mdyFormat.format(now))) timeStr += " · Updated " + hmFormat.format(timestampUpdated);
                            else{
                                if(yFormat.format(timestampUpdated).equals(yFormat.format(now))) timeStr += " · Updated on " + mdFormat.format(timestampUpdated);
                                else timeStr += " · Updated on " + mdyFormat.format(timestampUpdated);
                            }
                        }
                        textDate.setText(timeStr + " ");


                        if(reportRef.getImageUrl() == null) {
                            Glide.with(Activity_ViewReport.this).clear(imageItem);
                            imageItem.setImageDrawable(null);
                            imageItem.setVisibility(View.VISIBLE);
                            imageItem.setVisibility(View.INVISIBLE);
                        }else{
                            CircularProgressDrawable progress = new CircularProgressDrawable(imageItem.getContext());
                            imageItem.layout(0,0,0,0);
                            Glide.with(Activity_ViewReport.this).load(reportRef.getImageUrl()).into(imageItem);
                            imageItem.setVisibility(View.INVISIBLE);
                            imageItem.setVisibility(View.VISIBLE);
                        }

                        main.setVisibility(View.VISIBLE);
                        if(getIntent().getExtras().getInt("notifType") == 2 && !visited) loadComments();
                    }else{
                        finish();
                    }
                }else{
                    finish();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.viewmainreport_menu, menu);
        menu.findItem(R.id.reportmain_commentbutton).setVisible(false);
        menu.findItem(R.id.reportmain_updatebutton).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.reportmain_commentbutton).setVisible(true);
        MenuItem updateButton = menu.findItem(R.id.reportmain_updatebutton);
        if(owner) updateButton.setVisible(true);
        else updateButton.setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.reportmain_updatebutton:
                if(reportRef != null){
                    Intent intent = new Intent(Activity_ViewReport.this, Activity_EditReport.class);
                    intent.putExtra("reportId", reportId);
                    intent.putExtra("reportTitle", reportRef.getTitle());
                    intent.putExtra("reportContent", reportRef.getContent());
                    intent.putExtra("reportLocation", reportRef.getLocation());
                    intent.putExtra("reportOther", reportRef.getOther());
                    intent.putExtra("reportStatus", reportRef.getStatus());
                    intent.putExtra("reportResolved", reportRef.isResolved());
                    intent.putExtra("reportImageUrl", reportRef.getImageUrl());
                    intent.putExtra("reportIfFollower", reportRef.getFollowerIds().contains(user.getEmail().replace(".",",")));
                    intent.putExtra("reportFollowers", new ArrayList<String>(reportRef.getFollowerIds()));
                    startActivity(intent);
                }
                return true;
            case R.id.reportmain_commentbutton:
                if(reportRef != null) loadComments();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadComments(){
        visited = true;
        Intent intent = new Intent(Activity_ViewReport.this, Activity_ViewReportComments.class);
        intent.putExtra("reportId", reportId);
        intent.putExtra("reportTitle", reportRef.getTitle());
        intent.putExtra("reportIfFollower", reportRef.getFollowerIds().contains(user.getEmail().replace(".",",")));
        intent.putExtra("reportFollowers", new ArrayList<String>(reportRef.getFollowerIds()));
        startActivity(intent);
    }
}
