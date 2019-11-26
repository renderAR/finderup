package com.example.mypc.specialproject;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

public class Notification {
    private String notifId;
    private String content;
    private String source;
    private int type;
    private Timestamp date;
    private boolean status;

    public Notification(){}

    public Notification(String content, Timestamp date, String source, int type){
        this.content = content;
        this.date = date;
        this.source = source;
        this.type = type;
        this.status = false;
    }

    @Exclude
    public String getNotifId() { return notifId; }
    public void setNotifId(String notifId) { this.notifId = notifId; }

    public String getContent() { return content; }

    public Timestamp getDate() { return date; }

    public String getSource() { return source; }

    public int getType() { return type; }

    public boolean isStatus() { return status; }
}