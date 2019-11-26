package com.example.mypc.specialproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class Activity_Login extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 1234;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseFirestore db;
    private FirebaseUser mUser;
    private ProgressBar progress;

    // if user is logged in on the starting page, force redirect to the main page
    @Override
    protected void onStart(){
        super.onStart();
        if(mAuth.getCurrentUser() != null){
            finish();
            startActivity(new Intent(Activity_Login.this, Activity_Main.class));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // perform Google login when the login button is pressed
        progress = findViewById(R.id.login_progress);
        Button login_btn = findViewById(R.id.login_button);
        login_btn.setOnClickListener(this);

        Button rateApp = findViewById(R.id.rate_button);
        rateApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://docs.google.com/forms/d/1sGHHW89QVchYRvsqFdXiQyDWuNxpYZ2BXKL93Fc_faI/"));
                startActivity(intent);
            }
        });
    }

    @Override
    public void onClick(View v){
        login();
    }

    private void login(){
        progress.setVisibility(View.VISIBLE);
        Intent loginIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(loginIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if(account!=null) authenticate(account);
            } catch (ApiException e) {
                progress.setVisibility(View.INVISIBLE);
                Log.w(TAG, "Google sign in failed: ", e);
            }
        }
    }

    private void authenticate(GoogleSignInAccount account){
        Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "signInWithCredential:success");
                    progress.setVisibility(View.INVISIBLE);

                    FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if(!task.isSuccessful()){
                                Toast.makeText(Activity_Login.this, "Failed to Login", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            String token = task.getResult().getToken();
                            mUser = mAuth.getCurrentUser();

                            User user = new User(mUser.getUid(), mUser.getDisplayName(), mUser.getEmail(), ( mUser.getPhotoUrl()!=null ? mUser.getPhotoUrl().toString(): ""), token);
                            db.collection("users").document(mUser.getEmail().replace(".",",")).set(user);

                            // if user credentials are valid when logging in, redirect to main page
                            finish();
                            startActivity(new Intent(Activity_Login.this, Activity_Main.class));
                        }
                    });
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.getException());
                    progress.setVisibility(View.INVISIBLE);
                    Toast.makeText(Activity_Login.this, "Failed to Login", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
