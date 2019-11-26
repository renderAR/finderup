package com.example.mypc.specialproject;

import android.content.Intent;
import android.graphics.Color;
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

import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ChatroomAdapter extends FirestoreRecyclerAdapter<Chatroom, ChatroomAdapter.ChatroomHolder> {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    View v;

    public ChatroomAdapter(@NonNull FirestoreRecyclerOptions<Chatroom> options) {
        super(options);
    }

    @NonNull
    @Override
    public ChatroomHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatroom, parent, false);
        return new ChatroomHolder(v);
    }
    @Override
    protected void onBindViewHolder(@NonNull ChatroomHolder chatroomHolder, int i, @NonNull Chatroom chatroom) {
        final String chatId;
        final String chatName;
        final String chatEmail;
        final String chatPhotoUrl;
        final String chatLastMessage;
        final String chatLastMessageDateStr;

        if(chatroom.getUserIds().get(0).equals(user.getEmail().replace(".",","))){
            chatId = chatroom.getUserIds().get(1);
            chatName = chatroom.getUserNames().get(1);
            chatEmail = chatroom.getUserEmails().get(1);
            chatPhotoUrl = chatroom.getUserPhotoUrls().get(1);
        }else{
            chatId = chatroom.getUserIds().get(0);
            chatName = chatroom.getUserNames().get(0);
            chatEmail = chatroom.getUserEmails().get(0);
            chatPhotoUrl = chatroom.getUserPhotoUrls().get(0);
        }
        chatLastMessage = chatroom.getLastMessage();

        Date chatLastMessageDate = chatroom.getLastMessageDate().toDate();
        Date now = Timestamp.now().toDate();
        SimpleDateFormat mdyFormat = new SimpleDateFormat ("MMM dd, yyyy");
        SimpleDateFormat hmFormat = new SimpleDateFormat("hh:mm a");
        SimpleDateFormat yFormat = new SimpleDateFormat ("yyyy");
        SimpleDateFormat mdFormat = new SimpleDateFormat("MMM dd");
        String timeStr = "";
        if(mdyFormat.format(chatLastMessageDate).equals(mdyFormat.format(now))) timeStr += hmFormat.format(chatLastMessageDate);
        else{
            if(yFormat.format(chatLastMessageDate).equals(yFormat.format(now))) timeStr += mdFormat.format(chatLastMessageDate) + " at " + hmFormat.format(chatLastMessageDate);
            else timeStr += mdyFormat.format(chatLastMessageDate) + " at " + hmFormat.format(chatLastMessageDate);
        }
        timeStr += " ";
        chatLastMessageDateStr = timeStr;

        chatroomHolder.chatroomUsername.setText(chatName + " ");
        if(chatEmail.contains("up.edu.ph")){
            chatroomHolder.chatroomUsername.setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.ic_check_circle, 0);
        }else{
            chatroomHolder.chatroomUsername.setCompoundDrawablesWithIntrinsicBounds(0,0, 0, 0);
        }
        chatroomHolder.chatroomLastMessage.setText(chatLastMessage);
        if(chatroom.getLastSenderId().equals(user.getEmail().replace(".",","))){
            chatroomHolder.chatroomLastMessage.setTextColor(Color.parseColor("#D15050"));
        }else{
            chatroomHolder.chatroomLastMessage.setTextColor(Color.parseColor("#222222"));
        }
        chatroomHolder.chatroomLastMessageDate.setText(chatLastMessageDateStr);

        if(chatroom.isReceiverSeen() && chatroom.getLastSenderId().equals(user.getEmail().replace(".",","))){
            Date receiverSeenDate = chatroom.getReceiverSeenDate().toDate();
            String timeSeenStr = "Seen ";
            if(mdyFormat.format(receiverSeenDate).equals(mdyFormat.format(now))) timeSeenStr += hmFormat.format(receiverSeenDate);
            else{
                if(yFormat.format(receiverSeenDate).equals(yFormat.format(now))) timeSeenStr += mdFormat.format(receiverSeenDate) + " at " + hmFormat.format(receiverSeenDate);
                else timeSeenStr += mdyFormat.format(receiverSeenDate) + " at " + hmFormat.format(receiverSeenDate);
            }
            timeSeenStr += " ";
            chatroomHolder.chatroomReceiverSeen.setText(timeSeenStr);
            chatroomHolder.chatroomReceiverSeen.setVisibility(View.VISIBLE);
            chatroomHolder.chatroomCard.setBackgroundColor(Color.parseColor("#CCCCCC"));
        }else{
            chatroomHolder.chatroomReceiverSeen.setVisibility(View.GONE);
            if(chatroom.getLastSenderId().equals(user.getEmail().replace(".",",")) || chatroom.isReceiverSeen()) chatroomHolder.chatroomCard.setBackgroundColor(Color.parseColor("#CCCCCC"));
            else chatroomHolder.chatroomCard.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }

        chatroomHolder.chatroomCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(v.getContext(), Activity_ViewChatMessages.class);
                intent.putExtra("receiverId", chatId);
                intent.putExtra("receiverName", chatName);
                intent.putExtra("receiverEmail", chatEmail);
                intent.putExtra("receiverPhotoUrl", chatPhotoUrl);
                v.getContext().startActivity(intent);
            }
        });

    }

    class ChatroomHolder extends RecyclerView.ViewHolder {
        RelativeLayout chatroomCard;
        TextView chatroomUsername;
        TextView chatroomLastMessage;
        TextView chatroomLastMessageDate;
        TextView chatroomReceiverSeen;

        public ChatroomHolder(@NonNull View itemView) {
            super(itemView);
            chatroomCard = itemView.findViewById(R.id.chatroom_card);
            chatroomUsername = itemView.findViewById(R.id.chatroom_username);
            chatroomLastMessage = itemView.findViewById(R.id.chatroom_lastmessage);
            chatroomLastMessageDate = itemView.findViewById(R.id.chatroom_lastmessagedate);
            chatroomReceiverSeen = itemView.findViewById(R.id.chatroom_receiverSeen);
        }
    }
}
