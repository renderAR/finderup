package com.example.mypc.specialproject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

public class Dialog_Resolve extends DialogFragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Spinner spinner;
    private DialogListener listener;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    //private ArrayList<User> userList = new ArrayList<>();
    private ArrayList<User> userList = new ArrayList<>();
    private SpinnerAdapter userAdapter;
    private ArrayAdapter<User> adapter;
    //private String userList[] = {"test", "test1", "test2"};
    private View view;
    private User selectedUser;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        view  = inflater.inflate(R.layout.dialog_resolve, null);
        spinner = view.findViewById(R.id.resolve_spinner);

        userList.clear();
        db.collection("users").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        User userObj = new User();
                        userList.add(userObj);
                        for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                            userObj = documentSnapshot.toObject(User.class);
                            userObj.setId(documentSnapshot.getId());
                            if(!userObj.getId().equals(user.getEmail().replace(".",","))) userList.add(userObj);
                        }

                        /*adapter = new ArrayAdapter<User>(getActivity(), android.R.layout.simple_spinner_item, userList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner.setAdapter(adapter);*/
                        userAdapter = new SpinnerAdapter(getActivity(), userList);
                        spinner.setAdapter(userAdapter);
                    }
                });

        builder.setView(view)
                .setTitle("Mark report as resolved")
                .setMessage("A resolved report means the item is returned to the owner.\n\n(Optional)\nYou may link a user who\n · helped return the lost item\n · claimed the found item")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) { }
                }).setPositiveButton("Resolve Report", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.onResolveClicked((User) spinner.getSelectedItem());
                    }
                });
        return builder.create();
    }

    public interface DialogListener{
        void onResolveClicked(User user);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (Dialog_Resolve.DialogListener) context;
        }catch(ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement DialogListener");
        }
    }
}
