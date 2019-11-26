package com.example.mypc.specialproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;


public class Activity_Main extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private DrawerLayout drawer;
    NavigationView navigationView;
    int menuItemID;

    @Override
    protected void onStart(){
        super.onStart();
        if(mAuth.getCurrentUser() == null){
            finish();
            startActivity(new Intent(Activity_Main.this, Activity_Login.class));
        }

        db.collection("users").document(mAuth.getCurrentUser().getEmail().replace(".",",")).collection("notifications").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(e != null) return;

                int unreadNotifs = 0;
                for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                    Notification notification = documentSnapshot.toObject(Notification.class);
                    if(!notification.isStatus()) unreadNotifs += 1;
                }

                TextView notifBadge = (TextView) navigationView.getMenu().findItem(R.id.menu_notif).getActionView();
                notifBadge.setGravity(Gravity.CENTER_VERTICAL);
                notifBadge.setTypeface(null, Typeface.BOLD);
                notifBadge.setTextColor(Color.parseColor("#D15050"));
                notifBadge.setText((unreadNotifs != 0 ? unreadNotifs + "" : ""));
            }
        });

        db.collection("chatrooms").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(e != null) return;

                int unreadMessages = 0;
                for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                    Chatroom chatroom = documentSnapshot.toObject(Chatroom.class);
                    if(chatroom.getUserIds().contains(user.getEmail().replace(".",",")) && !chatroom.isReceiverSeen() && !chatroom.getLastSenderId().equals(user.getEmail().replace(".",","))) {
                        unreadMessages += 1;
                    }
                }

                TextView notifBadge = (TextView) navigationView.getMenu().findItem(R.id.menu_chatuser).getActionView();
                notifBadge.setGravity(Gravity.CENTER_VERTICAL);
                notifBadge.setTypeface(null, Typeface.BOLD);
                notifBadge.setTextColor(Color.parseColor("#D15050"));
                notifBadge.setText((unreadMessages != 0 ? unreadMessages + "" : ""));
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();

        //set toolbar and side menu
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.menu_layout);
        navigationView = findViewById(R.id.menu_view);
        navigationView.setNavigationItemSelectedListener(this);
        menuItemID = R.id.menu_viewreports;

        //fill user info in side menu header
        user = mAuth.getCurrentUser();
        ImageView userImage = navigationView.getHeaderView(0).findViewById(R.id.menu_header_image);
        Glide.with(this).load(user.getPhotoUrl().toString()).override(100,100).apply(RequestOptions.circleCropTransform()).into(userImage);

        TextView userName = navigationView.getHeaderView(0).findViewById(R.id.menu_header_name);
        userName.setText(user.getDisplayName() + " ");
        TextView userAccountType = navigationView.getHeaderView(0).findViewById(R.id.menu_header_accounttype);
        if(user.getEmail().contains("up.edu.ph")){
            userAccountType.setText(" using UP Account ");
            userName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check_circle, 0);
            userName.getCompoundDrawables()[2].setTint(Color.WHITE);
            userAccountType.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_circle, 0, 0, 0);
            userAccountType.getCompoundDrawables()[0].setTint(Color.WHITE);
        }
        else{
            userAccountType.setText(" using non-UP Account ");
            userName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_account_circle, 0);
            userName.getCompoundDrawables()[2].setTint(Color.WHITE);
            userAccountType.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_account_circle, 0, 0, 0);
            userAccountType.getCompoundDrawables()[0].setTint(Color.WHITE);
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // show the default fragment in main page once logged in
        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Fragment_ViewReports()).commit();
            navigationView.setCheckedItem(R.id.menu_viewreports);
        }
    }

    //detect which option in side menu is selected
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        menuItemID = menuItem.getItemId();
        switch(menuItemID){
            //switch content in main page based on selected option
            case R.id.menu_viewreports:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Fragment_ViewReports()).commit();
                break;
            case R.id.menu_addreport:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Fragment_AddReport()).commit();
                break;
            case R.id.menu_chatuser:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Fragment_ViewMessages()).commit();
                break;
            case R.id.menu_notif:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Fragment_ViewNotifications()).commit();
                break;
            case R.id.menu_logout:
                logout();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logout(){
        mAuth.signOut();
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        finish();
                    }
                });

        finish();
        Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(Activity_Main.this, Activity_Login.class));
    }

    // pressing back button will close the side menu if open, return to default option if on another, or exit the app
    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }else if(menuItemID != R.id.menu_viewreports){
            navigationView.setCheckedItem(R.id.menu_viewreports);
            menuItemID = R.id.menu_viewreports;
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Fragment_ViewReports()).commit();
        }else{
            super.onBackPressed();
        }
    }
}
