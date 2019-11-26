package com.example.mypc.specialproject;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportHolder>{
    ArrayList<Report> reportListFull;
    ArrayList<Report> reportList;
    View v;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ReportAdapter(ArrayList<Report> reportList){
        this.reportList = reportList;
        this.reportListFull = new ArrayList<>(reportList);
    }

    @NonNull
    @Override
    public ReportHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        v = layoutInflater.inflate(R.layout.report, parent, false);

        return new ReportHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ReportHolder holder, final int position) {
        Report report = reportList.get(position);

        // display report title, content, reporter name, and color bar
        holder.textTitle.setText(report.getTitle());

        String content = "";
        content += "ITEM DESCRIPTION:\n" + report.getContent();
        if(report.getLocation().length()>0){
            switch(report.getStatus()){
                case 1:
                    if(!report.isResolved()) content += "\n\n\nITEM LOST AT:\n" + report.getLocation();
                    else content += "\n\n\nITEM FORMERLY LOST AT:\n" + report.getLocation();
                    break;
                default:
                    if(!report.isResolved()) content += "\n\n\nITEM FOUND AT:\n" + report.getLocation();
                    else content += "\n\n\nITEM FORMERLY FOUND AT:\n" + report.getLocation();
            }
        }
        if(report.getOther().length()>0) content += "\n\n\nEXTRA INFO:\n" + report.getOther();
        holder.textContent.setText(content);

        if(report.isResolved()) holder.color.setBackgroundColor(Color.parseColor("#00c700"));
        else switch(report.getStatus()){
            case 1:
                holder.color.setBackgroundColor(Color.parseColor("#ede600"));
                break;
            default:
                holder.color.setBackgroundColor(Color.parseColor("#00A0FF"));
        }

        if(report.getReporterID().equals(user.getEmail().replace(".",","))){
            holder.textReporter.setText("You ");
            holder.textReporter.setTextColor(Color.parseColor("#D15050"));
            if(report.getReporterEmail().contains("up.edu.ph")){
                holder.textReporter.setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.ic_check_circle, 0);
            }else{
                holder.textReporter.setCompoundDrawablesWithIntrinsicBounds(0,0, 0, 0);
            }
            holder.textPMimage.setVisibility(View.GONE);
            holder.textPM.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { }
            });
        }else{
            holder.textReporter.setText(report.getReporterName() + " ");
            holder.textReporter.setTextColor(Color.parseColor("#222222"));
            if(report.getReporterEmail().contains("up.edu.ph")){
                holder.textReporter.setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.ic_check_circle, 0);
            }else{
                holder.textReporter.setCompoundDrawablesWithIntrinsicBounds(0,0, 0, 0);
            }
            holder.textPMimage.setVisibility(View.VISIBLE);
            holder.textPM.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Report report = reportList.get(position);
                    Intent intent = new Intent(v.getContext(), Activity_ViewChatMessages.class);
                    intent.putExtra("receiverId", report.getReporterID());
                    intent.putExtra("receiverName", report.getReporterName());
                    intent.putExtra("receiverEmail", report.getReporterEmail());
                    intent.putExtra("receiverPhotoUrl", report.getReporterPhotoUrl());
                    v.getContext().startActivity(intent);
                }
            });
        }

        if(report.isResolved() && report.getLinkedUser() != null){
            holder.linkLabel.setText((report.getStatus() == 1 ? "Formerly lost. Returned by " : "Formerly found. Claimed by "));

            if(report.getLinkedUser().get(0).equals(user.getEmail().replace(".",","))){
                holder.linkUser.setText("You ");
                holder.linkUser.setTextColor(Color.parseColor("#D15050"));
                holder.linkPMimage.setVisibility(View.GONE);
                holder.linkPM.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) { }
                });
            }else{
                holder.linkUser.setText(report.getLinkedUser().get(2) + " ");
                holder.linkUser.setTextColor(Color.parseColor("#222222"));
                holder.linkPMimage.setVisibility(View.VISIBLE);
                holder.linkPM.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Report report = reportList.get(position);
                        Intent intent = new Intent(v.getContext(), Activity_ViewChatMessages.class);
                        intent.putExtra("receiverId", report.getLinkedUser().get(0));
                        intent.putExtra("receiverName", report.getLinkedUser().get(2));
                        intent.putExtra("receiverEmail", report.getLinkedUser().get(3));
                        intent.putExtra("receiverPhotoUrl", report.getLinkedUser().get(4));
                        v.getContext().startActivity(intent);
                    }
                });
            }

            if(report.getLinkedUser().get(3).contains("up.edu.ph")){
                holder.linkUser.setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.ic_check_circle, 0);
            }else{
                holder.linkUser.setCompoundDrawablesWithIntrinsicBounds(0,0, 0, 0);
            }
            holder.linkLabel.setVisibility(View.VISIBLE);
            holder.linkUser.setVisibility(View.VISIBLE);
            holder.linkPM.setVisibility(View.VISIBLE);
        }else if(report.isResolved()){
            holder.linkLabel.setText((report.getStatus() == 1 ? "Formerly lost " : "Formerly found "));
            holder.linkLabel.setVisibility(View.VISIBLE);
            holder.linkPM.setVisibility(View.GONE);
            holder.linkUser.setVisibility(View.GONE);
            holder.linkPMimage.setVisibility(View.GONE);
            holder.linkPM.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { }
            });
        }else{
            holder.linkLabel.setText((report.getStatus() == 1 ? "Lost " : "Found "));
            holder.linkLabel.setVisibility(View.VISIBLE);
            holder.linkPM.setVisibility(View.GONE);
            holder.linkUser.setVisibility(View.GONE);
            holder.linkPMimage.setVisibility(View.GONE);
            holder.linkPM.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { }
            });
        }

        // display report date
        Date timeAdded = report.getTimestampAdded().toDate();
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
        if(report.getTimestampUpdated().compareTo(report.getTimestampAdded()) != 0){
            Date timestampUpdated = report.getTimestampUpdated().toDate();
            if(mdyFormat.format(timestampUpdated).equals(mdyFormat.format(now))) timeStr += " · Updated " + hmFormat.format(timestampUpdated);
            else{
                if(yFormat.format(timestampUpdated).equals(yFormat.format(now))) timeStr += " · Updated on " + mdFormat.format(timestampUpdated);
                else timeStr += " · Updated on " + mdyFormat.format(timestampUpdated);
            }
        }
        timeStr += " ";
        holder.textDate.setText(timeStr);
        // display report item image
        if(report.getImageUrl() == null) {
            Glide.with(holder.itemView.getContext()).clear(holder.imageItem);
            holder.imageItem.setImageDrawable(null);
            holder.imageItem.setVisibility(View.VISIBLE);
            holder.imageItem.setVisibility(View.INVISIBLE);
        }else{
            CircularProgressDrawable progress = new CircularProgressDrawable(holder.imageItem.getContext());
            holder.imageItem.layout(0,0,0,0);
            Glide.with(holder.itemView.getContext()).load(report.getImageUrl()).into(holder.imageItem);
            holder.imageItem.setVisibility(View.INVISIBLE);
            holder.imageItem.setVisibility(View.VISIBLE);
        }
        // display update button for reports created by user only. if clicked, move to update report page
        if(report.getReporterID().equals(user.getEmail().replace(".",","))){
            holder.buttonEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Report reportToEdit = reportList.get(position);
                    Intent intent = new Intent(v.getContext(), Activity_EditReport.class);
                    intent.putExtra("reportId", reportToEdit.getReportId());
                    intent.putExtra("reportTitle", reportToEdit.getTitle());
                    intent.putExtra("reportContent", reportToEdit.getContent());
                    intent.putExtra("reportLocation", reportToEdit.getLocation());
                    intent.putExtra("reportOther", reportToEdit.getOther());
                    intent.putExtra("reportStatus", reportToEdit.getStatus());
                    intent.putExtra("reportResolved", reportToEdit.isResolved());
                    intent.putExtra("reportImageUrl", reportToEdit.getImageUrl());
                    intent.putExtra("reportIfFollower", reportToEdit.getFollowerIds().contains(user.getEmail().replace(".",",")));
                    intent.putExtra("reportFollowers", new ArrayList<String>(reportToEdit.getFollowerIds()));
                    v.getContext().startActivity(intent);
                }
            });
            holder.buttonEdit.setVisibility(View.GONE);
            holder.buttonEdit.setVisibility(View.VISIBLE);
        }else {
            holder.buttonEdit.setVisibility(View.VISIBLE);
            holder.buttonEdit.setVisibility(View.GONE);
        }

        holder.buttonComment.setText("Comment" + (report.getNumOfComments()>0 ? " · "+report.getNumOfComments() : ""));
        holder.buttonComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Report reportToComment = reportList.get(position);
                Intent intent = new Intent(v.getContext(), Activity_ViewReportComments.class);
                intent.putExtra("reportId", reportToComment.getReportId());
                intent.putExtra("reportTitle", reportToComment.getTitle());
                intent.putExtra("reportIfFollower", reportToComment.getFollowerIds().contains(user.getEmail().replace(".",",")));
                intent.putExtra("reportFollowers", new ArrayList<String>(reportToComment.getFollowerIds()));
                v.getContext().startActivity(intent);
            }
        });

        if(report.getFollowerIds().contains(user.getEmail().replace(".",","))){
            holder.buttonFollow.setText("Unfollow");
            holder.buttonFollow.setTextColor(Color.parseColor("#D15050"));
            holder.buttonFollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Report reportToUnfollow = reportList.get(position);
                    db.collection("reports").document(reportToUnfollow.getReportId()).update("followerIds", FieldValue.arrayRemove(user.getEmail().replace(".",",")))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    FirebaseMessaging.getInstance().unsubscribeFromTopic(reportToUnfollow.getReportId());
                                    Toast.makeText(v.getContext(), "Disabled noifications for this report", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(v.getContext(), "Failed to unfollow report", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });
        }else{
            holder.buttonFollow.setText("Follow");
            holder.buttonFollow.setTextColor(Color.parseColor("#000000"));
            holder.buttonFollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Report reportToFollow = reportList.get(position);
                    db.collection("reports").document(reportToFollow.getReportId()).update("followerIds", FieldValue.arrayUnion(user.getEmail().replace(".",",")))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    FirebaseMessaging.getInstance().subscribeToTopic(reportToFollow.getReportId());
                                    Toast.makeText(v.getContext(), "Enabled notifications for this report", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(v.getContext(), "Failed to follow report", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    public void applySearchOptions(String searchText, int filter, boolean lost, boolean found, boolean resolved, boolean up, boolean non_up, int sort, boolean sort_asc){
        ArrayList<Report> filteredList = new ArrayList<>();

        searchText = searchText.toLowerCase().trim();
        for(Report report : reportListFull){
            if(filter == 2 && !report.getFollowerIds().contains(user.getEmail().replace(".",","))) continue;
            else if(filter == 3 && !report.getReporterID().equals(user.getEmail().replace(".",","))) continue;
            if(!(report.getTitle().toLowerCase().contains(searchText) || report.getContent().toLowerCase().contains(searchText) || report.getLocation().toLowerCase().contains(searchText) || report.getOther().toLowerCase().contains(searchText) || report.getReporterName().toLowerCase().contains(searchText))) continue;
            if(!((lost && report.getStatus()==1 && !report.isResolved()) || (found && report.getStatus()==2 && !report.isResolved()) || (resolved && report.isResolved()))) continue;
            if(!((up && report.getReporterEmail().contains("up.edu.ph")) || (non_up && !report.getReporterEmail().contains("up.edu.ph")))) continue;
            filteredList.add(report);
        }

        if(sort == 1){
            if(sort_asc){
                Collections.sort(filteredList, new Comparator<Report>() {
                    @Override
                    public int compare(Report report1, Report report2) {
                        return report1.getTimestampUpdated().toDate().compareTo(report2.getTimestampUpdated().toDate());
                    }
                });
            }else{
                Collections.sort(filteredList, new Comparator<Report>() {
                    @Override
                    public int compare(Report report1, Report report2) {
                        return report2.getTimestampUpdated().toDate().compareTo(report1.getTimestampUpdated().toDate());
                    }
                });
            }
        }else if(sort == 2){
            if(sort_asc){
                Collections.sort(filteredList, new Comparator<Report>() {
                    @Override
                    public int compare(Report report1, Report report2) {
                        return report1.getTimestampAdded().toDate().compareTo(report2.getTimestampAdded().toDate());
                    }
                });
            }else{
                Collections.sort(filteredList, new Comparator<Report>() {
                    @Override
                    public int compare(Report report1, Report report2) {
                        return report2.getTimestampAdded().toDate().compareTo(report1.getTimestampAdded().toDate());
                    }
                });
            }

        }else if(sort == 3){
            if(sort_asc){
                Collections.sort(filteredList, new Comparator<Report>() {
                    @Override
                    public int compare(Report report1, Report report2) {
                        return report1.getReporterName().compareTo(report2.getReporterName());
                    }
                });
            }else{
                Collections.sort(filteredList, new Comparator<Report>() {
                    @Override
                    public int compare(Report report1, Report report2) {
                        return report2.getReporterName().compareTo(report1.getReporterName());
                    }
                });
            }
        }

        reportList.clear();
        reportList.addAll(filteredList);
        notifyDataSetChanged();
    }

    public void reset(ArrayList<Report> reportList){
        this.reportList = reportList;
        this.reportListFull = new ArrayList<>(reportList);
    }

    class ReportHolder extends RecyclerView.ViewHolder {
        RelativeLayout color;
        TextView textTitle;
        TextView textReporter;
        TextView textDate;
        TextView textContent;
        TextView textPMimage;
        RelativeLayout textPM;
        ImageView imageItem;
        Button buttonEdit;
        Button buttonComment;
        Button buttonFollow;
        TextView linkLabel;
        TextView linkUser;
        TextView linkPMimage;
        RelativeLayout linkPM;


        private ReportHolder(@NonNull View itemView) {
            super(itemView);

            color = itemView.findViewById(R.id.report_background);
            textTitle = itemView.findViewById(R.id.report_title);
            textReporter = itemView.findViewById(R.id.report_reporter);
            textDate = itemView.findViewById(R.id.report_date);
            textContent = itemView.findViewById(R.id.report_contenttext);
            textPMimage =  itemView.findViewById(R.id.report_pm);
            textPM =  itemView.findViewById(R.id.report_reportersection);
            imageItem = itemView.findViewById(R.id.report_image);
            buttonEdit = itemView.findViewById(R.id.report_editbutton);
            buttonComment = itemView.findViewById(R.id.report_commentbutton);
            buttonFollow = itemView.findViewById(R.id.report_followbutton);
            linkLabel = itemView.findViewById(R.id.report_linklabel);
            linkUser = itemView.findViewById(R.id.report_linkuser);
            linkPMimage = itemView.findViewById(R.id.report_linkpm);
            linkPM = itemView.findViewById(R.id.report_link);
        }
    }
}
