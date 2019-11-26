package com.example.mypc.specialproject;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserHolder> {
    ArrayList<User> userListFull;
    ArrayList<User> userList;
    View v;

    public UserAdapter(ArrayList<User> userList){
        this.userList = userList;
        this.userListFull = new ArrayList<>(userList);
    }

    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        v = layoutInflater.inflate(R.layout.user, parent, false);

        return new UserHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserHolder holder, final int position) {
        User user = userList.get(position);

        holder.userName.setText(user.getName() + " ");
        if(user.getEmail().contains("up.edu.ph")){
            holder.userName.setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.ic_check_circle, 0);
        }else{
            holder.userName.setCompoundDrawablesWithIntrinsicBounds(0,0, 0, 0);
        }
        holder.userCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User user = userList.get(position);
                Intent intent = new Intent(v.getContext(), Activity_ViewChatMessages.class);
                intent.putExtra("receiverId", user.getId());
                intent.putExtra("receiverName", user.getName());
                intent.putExtra("receiverEmail", user.getEmail());
                intent.putExtra("receiverPhotoUrl", user.getPhotoUrl());
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void applySearchOptions(String searchText){
        ArrayList<User> filteredList = new ArrayList<>();
        searchText = searchText.toLowerCase().trim();

        for(User user : userListFull){
            if(!(user.getName().toLowerCase().contains(searchText))) continue;
            filteredList.add(user);
        }

        Collections.sort(filteredList, new Comparator<User>() {
            @Override
            public int compare(User user1, User user2) {
                return user1.getName().compareTo(user2.getName());
            }
        });

        userList.clear();
        userList.addAll(filteredList);
        notifyDataSetChanged();
    }

    class UserHolder extends RecyclerView.ViewHolder{
        RelativeLayout userCard;
        TextView userName;

        public UserHolder(@NonNull View itemView) {
            super(itemView);
            userCard = itemView.findViewById(R.id.user_card);
            userName = itemView.findViewById(R.id.user_username);
        }
    }
}
