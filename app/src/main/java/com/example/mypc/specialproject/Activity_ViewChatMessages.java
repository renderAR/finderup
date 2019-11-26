package com.example.mypc.specialproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

public class Activity_ViewChatMessages extends AppCompatActivity {
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private EditText inputMessage;
    private ImageView sendButton;

    private RecyclerView recyclerView;
    private CommentAdapter adapter;
    private String receiverId;
    private String receiverName;
    private String receiverEmail;
    private String receiverPhotoUrl;
    private String chatroomId ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewchatmessages);

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        receiverId = getIntent().getExtras().getString("receiverId");
        receiverName = getIntent().getExtras().getString("receiverName");
        setTitle("Message " + receiverName);
        receiverEmail = getIntent().getExtras().getString("receiverEmail");
        receiverPhotoUrl = getIntent().getExtras().getString("receiverPhotoUrl");
        chatroomId = (user.getEmail().replace(".", ",").compareTo(receiverId) < 0 ? user.getEmail().replace(".", ",")+receiverId : receiverId+user.getEmail().replace(".", ","));
        loadChat();

        inputMessage = findViewById(R.id.viewchatmessages_input);
        sendButton = findViewById(R.id.viewchatmessages_send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(chatroomId);
            }
        });

        db.collection("chatrooms").document(chatroomId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Chatroom chatroom = documentSnapshot.toObject(Chatroom.class);
                if(chatroom != null){
                    if(!(chatroom.getLastSenderId().equals(user.getEmail().replace(".",",")) || chatroom.isReceiverSeen())){
                        db.collection("chatrooms").document(chatroomId).update(
                                "receiverSeen", true,
                                "receiverSeenDate", Timestamp.now());
                    }
                }
            }
        });
    }

    private void loadChat(){
        Query query = db.collection("chatrooms").document(chatroomId).collection("comments").orderBy("timestampCommented", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Comment> options = new FirestoreRecyclerOptions.Builder<Comment>()
                .setQuery(query, Comment.class).build();

        adapter = new CommentAdapter(options);
        recyclerView = findViewById(R.id.recycler_chatmessages);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void sendMessage(final String chatroomId){
        final String messageText = inputMessage.getText().toString();
        if(messageText.isEmpty()) return;
        final Timestamp messageTimeSent = Timestamp.now();
        inputMessage.setText("");

        List<String> userIds = new ArrayList<String>();
        List<String> userNames = new ArrayList<String>();
        List<String> userEmails = new ArrayList<String>();
        List<String> userPhotoUrls = new ArrayList<String>();
        if(user.getEmail().replace(".", ",").compareTo(receiverId) < 0){
            userIds.add(user.getEmail().replace(".", ","));
            userIds.add(receiverId);
            userNames.add(user.getDisplayName());
            userNames.add(receiverName);
            userEmails.add(user.getEmail());
            userEmails.add(receiverEmail);
            userPhotoUrls.add(user.getPhotoUrl().toString());
            userPhotoUrls.add(receiverPhotoUrl);
        }else{
            userIds.add(receiverId);
            userIds.add(user.getEmail().replace(".", ","));
            userNames.add(receiverName);
            userNames.add(user.getDisplayName());
            userEmails.add(receiverEmail);
            userEmails.add(user.getEmail());
            userPhotoUrls.add(receiverPhotoUrl);
            userPhotoUrls.add(user.getPhotoUrl().toString());
        }

        WriteBatch batch = db.batch();

        Chatroom chatroom = new Chatroom(messageText, messageTimeSent, user.getEmail().replace(".",","), userIds, userNames, userEmails, userPhotoUrls);
        batch.set(db.collection("chatrooms").document(chatroomId), chatroom);

        Comment message = new Comment(user.getEmail().replace(".",","), user.getDisplayName(), user.getEmail(), user.getPhotoUrl().toString(), messageText, messageTimeSent);
        batch.set(db.collection("chatrooms").document(chatroomId).collection("comments").document(), message);

        batch.commit()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() -1);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Activity_ViewChatMessages.this, "Failed to send", Toast.LENGTH_SHORT).show();
                    }
                });
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
}
