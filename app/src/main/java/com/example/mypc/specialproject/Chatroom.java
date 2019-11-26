package com.example.mypc.specialproject;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.util.HashMap;
import java.util.List;

public class Chatroom {
    private String chatroomId;
    private String lastMessage;
    private String lastSenderId;
    private Timestamp lastMessageDate;
    private List<String> userIds;
    private List<String> userNames;
    private List<String> userEmails;
    private List<String> userPhotoUrls;
    private boolean receiverSeen;
    private Timestamp receiverSeenDate;

    public Chatroom(){}
    public Chatroom(String lastMessage, Timestamp lastMessageDate, String lastSenderId, List<String> userIds, List<String> userNames, List<String> userEmails, List<String> userPhotoUrls){
        this.lastMessage = lastMessage;
        this.lastMessageDate = lastMessageDate;
        this.lastSenderId = lastSenderId;
        this.userIds = userIds;
        this.userNames = userNames;
        this.userEmails = userEmails;
        this.userPhotoUrls = userPhotoUrls;
        this.receiverSeen = false;
    }

    @Exclude
    public String getChatroomId() { return chatroomId; }
    public void setChatroomId(String chatroomId) { this.chatroomId = chatroomId; }

    public String getLastMessage() { return lastMessage; }

    public Timestamp getLastMessageDate() { return lastMessageDate; }

    public String getLastSenderId() { return lastSenderId; }

    public List<String> getUserIds() { return userIds; }

    public List<String> getUserNames() { return userNames; }

    public List<String> getUserEmails() { return userEmails; }

    public List<String> getUserPhotoUrls() { return userPhotoUrls; }

    public boolean isReceiverSeen() { return receiverSeen; }

    public Timestamp getReceiverSeenDate() { return receiverSeenDate; }
}
