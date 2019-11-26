package com.example.mypc.specialproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class Fragment_ViewMessages extends Fragment {
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private RecyclerView recyclerView;
    private ChatroomAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().setTitle("User Messages");
        View view = inflater.inflate(R.layout.fragment_viewmessages, container, false);
        setHasOptionsMenu(true);

        recyclerView = view.findViewById(R.id.recycler_chatrooms);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        Query query = db.collection("chatrooms").whereArrayContains("userIds", user.getEmail().replace(".",",")).orderBy("lastMessageDate",  Query.Direction.DESCENDING );
        FirestoreRecyclerOptions<Chatroom> options = new FirestoreRecyclerOptions.Builder<Chatroom>()
                .setQuery(query, Chatroom.class).build();
        adapter = new ChatroomAdapter(options);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.viewmessages_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.viewmessages_add:
                startActivity(new Intent(getActivity(), Activity_ViewUsers.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
