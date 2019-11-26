package com.example.mypc.specialproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class Activity_ViewUsers extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private ArrayList<User> userList;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private String searchText = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle("Contact User");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewusers);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);

        userList = new ArrayList<User>();
        recyclerView = findViewById(R.id.recycler_users);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db.collection("users").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        userList.clear();
                        for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                            User userObj = documentSnapshot.toObject(User.class);
                            userObj.setId(documentSnapshot.getId());
                            if(!userObj.getId().equals(user.getEmail().replace(".",","))){
                                userList.add(userObj);
                            }
                        }

                        adapter = new UserAdapter(userList);
                        adapter.applySearchOptions(searchText);
                        recyclerView.setAdapter(adapter);
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        db.collection("users").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if(e != null) return;

                userList.clear();
                for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                    User userObj = documentSnapshot.toObject(User.class);
                    userObj.setId(documentSnapshot.getId());
                    if(!userObj.getId().equals(user.getEmail().replace(".",","))){
                        userList.add(userObj);
                    }
                }

                adapter = new UserAdapter(userList);
                adapter.applySearchOptions(searchText);
                recyclerView.setAdapter(adapter);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.viewusers_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.viewusers_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search item or user...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchText = newText;
                if(adapter != null) adapter.applySearchOptions(searchText);
                return false;
            }
        });

        return true;
    }
}
