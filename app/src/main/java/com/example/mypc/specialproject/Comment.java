package com.example.mypc.specialproject;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

public class Comment {
    private String commentId;
    private String commenterID;
    private String commenterName;
    private String commenterEmail;
    private String commenterPhotoUrl;
    private String commentText;
    private Timestamp timestampCommented;
    private Timestamp timestampUpdated;

    public Comment(){}

    public Comment(String commenterID, String commenterName, String commenterEmail, String commenterPhotoUrl, String commentText, Timestamp timestampCommented){
        this.commenterID = commenterID;
        this.commenterName = commenterName;
        this.commenterEmail = commenterEmail;
        this.commenterPhotoUrl = commenterPhotoUrl;
        this.commentText = commentText;
        this.timestampCommented = timestampCommented;
        this.timestampUpdated = timestampCommented;
    }

    @Exclude
    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }

    public String getCommenterID() { return commenterID; }

    public String getCommenterName() { return commenterName; }

    public String getCommenterEmail() { return commenterEmail; }

    public String getCommenterPhotoUrl() { return commenterPhotoUrl; }

    public String getCommentText() { return commentText; }

    public Timestamp getTimestampCommented() { return timestampCommented; }

    public Timestamp getTimestampUpdated() { return timestampUpdated; }
}
