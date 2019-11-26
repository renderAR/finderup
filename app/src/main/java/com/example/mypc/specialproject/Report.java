package com.example.mypc.specialproject;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.util.List;

public class Report {
    private String reportId;
    private String title;
    private String content;
    private String location;
    private String other;
    private int status;
    private boolean resolved;
    private String imageUrl;
    private Timestamp timestampAdded;
    private Timestamp timestampUpdated;
    private int numOfComments;

    private String reporterID;
    private String reporterName;

    private String reporterEmail;
    private String reporterPhotoUrl;

    private List<String> linkedUser;
    private List<String> followerIds;

    public Report(){}

    public Report(String title, String content, String location, String other, int status, String reporterID, String reporterName, String reporterEmail, String reporterPhotoUrl, List<String> followerIds){
        this.title = title;
        this.content = content;
        this.location = location;
        this.other = other;
        this.status = status;
        this.resolved = false;
        this.reporterID = reporterID;
        this.timestampAdded = Timestamp.now();
        this.timestampUpdated = this.timestampAdded;
        this.numOfComments = 0;
        this.reporterName = reporterName;
        this.reporterEmail = reporterEmail;
        this.reporterPhotoUrl = reporterPhotoUrl;
        this.followerIds = followerIds;
    }

    public Report(String title, String content, String location, String other, int status, String imageUrl, String reporterID, String reporterName, String reporterEmail, String reporterPhotoUrl, List<String> followerIds){
        this.title = title;
        this.content = content;
        this.location = location;
        this.other = other;
        this.status = status;
        this.resolved = false;
        this.imageUrl = imageUrl;
        this.timestampAdded = Timestamp.now();
        this.timestampUpdated = this.timestampAdded;
        this.numOfComments = 0;
        this.reporterID = reporterID;
        this.reporterName = reporterName;
        this.reporterEmail = reporterEmail;
        this.reporterPhotoUrl = reporterPhotoUrl;
        this.followerIds = followerIds;
    }

    @Exclude
    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public String getTitle(){
        return title;
    }

    public boolean isResolved() { return resolved; }

    public String getContent(){
        return content;
    }

    public String getLocation() { return location; }

    public String getOther() { return other; }

    public int getStatus(){
        return status;
    }

    public String getReporterID(){ return reporterID; };

    public String getReporterName(){ return reporterName; };

    public String getReporterEmail(){ return reporterEmail; };

    public String getReporterPhotoUrl(){ return reporterPhotoUrl; };

    public String getImageUrl() { return imageUrl; }

    public Timestamp getTimestampAdded() { return timestampAdded; }

    public Timestamp getTimestampUpdated() { return timestampUpdated; }

    public int getNumOfComments() { return numOfComments; }

    public List<String> getLinkedUser() { return linkedUser; }

    public List<String> getFollowerIds() { return followerIds; }
}
