package com.example.mypc.specialproject;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CommentAdapter extends FirestoreRecyclerAdapter<Comment, CommentAdapter.CommentHolder> {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    View v;
    boolean commentMode = false;

    public CommentAdapter(@NonNull FirestoreRecyclerOptions<Comment> options) {
        super(options);
    }

    @NonNull
    @Override
    public CommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment, parent, false);
        return new CommentHolder(v);
    }

    @Override
    protected void onBindViewHolder(@NonNull CommentHolder commentHolder, final int i, @NonNull Comment comment) {
        Date timeCommented = comment.getTimestampCommented().toDate();
        Date now = Timestamp.now().toDate();
        SimpleDateFormat mdyFormat = new SimpleDateFormat ("MMM dd, yyyy");
        SimpleDateFormat hmFormat = new SimpleDateFormat("hh:mm a");
        SimpleDateFormat yFormat = new SimpleDateFormat ("yyyy");
        SimpleDateFormat mdFormat = new SimpleDateFormat("MMM dd");
        String timeStr = "";
        if(mdyFormat.format(timeCommented).equals(mdyFormat.format(now))) timeStr += hmFormat.format(timeCommented);
        else{
            if(yFormat.format(timeCommented).equals(yFormat.format(now))) timeStr += mdFormat.format(timeCommented) + " at " + hmFormat.format(timeCommented);
            else timeStr += mdyFormat.format(timeCommented) + " at " + hmFormat.format(timeCommented);
        }
        if(comment.getTimestampUpdated().compareTo(comment.getTimestampCommented()) != 0){
            Date timestampUpdated = comment.getTimestampUpdated().toDate();
            if(mdyFormat.format(timestampUpdated).equals(mdyFormat.format(now))) timeStr += " · Updated " + hmFormat.format(timestampUpdated);
            else{
                if(yFormat.format(timestampUpdated).equals(yFormat.format(now))) timeStr += " · Updated on " + mdFormat.format(timestampUpdated);
                else timeStr += " · Updated on " + mdyFormat.format(timestampUpdated);
            }
        }
        timeStr += " ";

        if(comment.getCommenterID().equals(user.getEmail().replace(".",","))){
            commentHolder.commenterName.setText("You ");
            commentHolder.commenterName.setTextColor(Color.parseColor("#D15050"));
            commentHolder.commentDate.setText(timeStr);
            commentHolder.commentDate.setTextColor(Color.parseColor("#D15050"));
            commentHolder.commentText.setText(comment.getCommentText());
            commentHolder.commentText.setTextColor(Color.parseColor("#D15050"));
            commentHolder.commenterPMimage.setVisibility(View.GONE);
            commentHolder.commenterPM.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { }
            });
        }else{
            commentHolder.commenterName.setText(comment.getCommenterName()+" ");
            commentHolder.commenterName.setTextColor(Color.parseColor("#000000"));
            commentHolder.commentDate.setText(timeStr);
            commentHolder.commentDate.setTextColor(Color.parseColor("#000000"));
            commentHolder.commentText.setText(comment.getCommentText());
            commentHolder.commentText.setTextColor(Color.parseColor("#000000"));

            if(commentMode){
                commentHolder.commenterPMimage.setVisibility(View.VISIBLE);
                commentHolder.commenterPM.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DocumentSnapshot comment = getSnapshots().getSnapshot(i);
                        Intent intent = new Intent(v.getContext(), Activity_ViewChatMessages.class);
                        intent.putExtra("receiverId", comment.get("commenterID").toString());
                        intent.putExtra("receiverName", comment.get("commenterName").toString());
                        intent.putExtra("receiverEmail", comment.get("commenterEmail").toString());
                        intent.putExtra("receiverPhotoUrl", comment.get("commenterPhotoUrl").toString());
                        v.getContext().startActivity(intent);
                    }
                });
            }else{
                commentHolder.commenterPMimage.setVisibility(View.GONE);
                commentHolder.commenterPM.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) { }
                });
            }
        }
        if(comment.getCommenterEmail().contains("up.edu.ph")){
            commentHolder.commenterName.setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.ic_check_circle, 0);
        }else{
            commentHolder.commenterName.setCompoundDrawablesWithIntrinsicBounds(0,0, 0, 0);
        }
    }

    class CommentHolder extends RecyclerView.ViewHolder{
        TextView commenterName;
        TextView commentDate;
        TextView commentText;
        TextView commenterPMimage;
        RelativeLayout commenterPM;


        public CommentHolder(@NonNull View itemView) {
            super(itemView);
            commenterName = itemView.findViewById(R.id.comment_commentername);
            commentDate = itemView.findViewById(R.id.comment_date);
            commentText = itemView.findViewById(R.id.comment_text);
            commenterPMimage = itemView.findViewById(R.id.comment_pm);
            commenterPM = itemView.findViewById(R.id.comment_commenter);
        }
    }

    public void toCommentMode(){
        this.commentMode = true;
    }

    public void toMessageMode(){
        this.commentMode = false;
    }
}
