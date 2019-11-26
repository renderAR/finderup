package com.example.mypc.specialproject;

import com.google.firebase.firestore.Exclude;

public class User {
    private String id;
    private String uID;
    private String name;
    private String email;
    private String photoUrl;
    private String token;

    public User(){}

    public User(String uID, String name, String email, String photoUrl, String token){
        this.uID = uID;
        this.name = name;
        this.email = email;
        this.photoUrl = photoUrl;
        this.token = token;
    }

    @Exclude
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getuID() { return uID; }

    public String getName(){
        return name;
    }

    public String getEmail(){
        return email;
    }

    public String getPhotoUrl(){
        return photoUrl;
    }

    public String getToken() { return token; }

    @Override
    public String toString() {
        if(id == null) return "<no user>";
        else return (name + (email.contains("up.edu.ph") ? " " : " "));
    }
}
