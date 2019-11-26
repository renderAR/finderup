package com.example.mypc.specialproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Activity_ViewReportComments extends AppCompatActivity {
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private EditText inputComment;
    private ImageView sendButton;

    private RecyclerView recyclerView;
    private CommentAdapter adapter;
    private String reportId;
    private String reportTitle;
    private boolean reportIfFollower;
    private ArrayList<String> reportFollowers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewreportcomments);

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Report Comments");

        reportId = getIntent().getExtras().getString("reportId");
        reportTitle = getIntent().getExtras().getString("reportTitle");
        reportIfFollower = getIntent().getExtras().getBoolean("reportIfFollower");
        reportFollowers = getIntent().getStringArrayListExtra("reportFollowers");
        loadComments();

        inputComment = findViewById(R.id.viewreportcomments_input);
        sendButton = findViewById(R.id.viewreportcomments_send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendComment();
            }
        });
    }

    private void loadComments(){
        Query query = db.collection("reports").document(reportId).collection("comments").orderBy("timestampCommented", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Comment> options = new FirestoreRecyclerOptions.Builder<Comment>()
                .setQuery(query, Comment.class).build();

        adapter = new CommentAdapter(options);
        adapter.toCommentMode();
        recyclerView = findViewById(R.id.recycler_reportcomments);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void sendComment(){
        final String commentText = inputComment.getText().toString();
        if(commentText.isEmpty()) return;

        inputComment.setText("");

        WriteBatch batch = db.batch();

        Comment comment = new Comment(user.getEmail().replace(".",","), user.getDisplayName(), user.getEmail(), user.getPhotoUrl().toString(), commentText, Timestamp.now());
        batch.set(db.collection("reports").document(reportId).collection("comments").document(), comment);
        batch.update(db.collection("reports").document(reportId), "numOfComments", FieldValue.increment(1));
        batch.commit()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if(reportIfFollower) FirebaseMessaging.getInstance().unsubscribeFromTopic(reportId);
                        notifyFollowers(commentText);
                        recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() -1);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Activity_ViewReportComments.this, "Failed to send", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void notifyFollowers(final String comment) {
        Notification notificationObj = new Notification(user.getDisplayName() + " commented on a report.\n\nReport: " + reportTitle  + "\nComment: " + comment, Timestamp.now(), reportId, 2);
        WriteBatch batch = db.batch();

        if(reportFollowers != null) {
            for (String userId : reportFollowers) {
                if (userId.equals(user.getEmail().replace(".", ","))) continue;
                batch.set(db.collection("users").document(userId).collection("notifications").document("2" + reportId), notificationObj);
            }
            batch.commit();
        }

        String serverUrl = "https://fcm.googleapis.com/fcm/send";
        final String serverKey = "key=" + "AAAAUalGRAU:APA91bGRSvcdLbDgj0kXn-mf0bW3N8gGA73Fj7hAHshyHW-ibWgYLyY8rXoHANgjOpaQePvDBANBYJsd4zJbUb8djuTCBhsOp7yVLsHM9ap9_3yTql0qzrMgBCQ6ehLRnS89ifCKh0cf";
        final String contentType = "application/json";
        String TAG = "NOTIFICATION TAG";

        JSONObject notification = new JSONObject();
        JSONObject notificationBody = new JSONObject();
        try {
            notificationBody.put("title", "UPLB Lost and Found");
            notificationBody.put("message",user.getDisplayName() + " commented on a report.");

            notification.put("to", "/topics/" + reportId);
            notification.put("data", notificationBody);
        } catch (JSONException e) {
            Log.e(TAG, "onCreate: " + e.getMessage());
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, serverUrl, notification, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if(reportIfFollower) FirebaseMessaging.getInstance().subscribeToTopic(reportId).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("NOTIFICATION TAG", "refollowed");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("NOTIFICATION TAG", "refollowed error");
                    }
                });;
                Log.i("NOTIFICATION TAG", "onResponse: " + response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(reportIfFollower) FirebaseMessaging.getInstance().subscribeToTopic(reportId).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("NOTIFICATION TAG", "refollowed");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("NOTIFICATION TAG", "refollowed error");
                    }
                });
                Log.i("NOTIFICATION TAG", "onErrorResponse: Didn't work");
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", serverKey);
                params.put("Content-Type", contentType);
                return params;
            }
        };

        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
