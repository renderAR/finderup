package com.example.mypc.specialproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class Fragment_ViewReports extends Fragment implements Dialog_SearchOptions.DialogListener {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView recyclerView;
    private ReportAdapter adapter;
    private ArrayList<Report> reportList;

    //filter and sort default search options
    private String searchText = "";
    private int filter, sort;
    private boolean lost, found, resolved, up, non_up, order_asc;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().setTitle("View Reports");
        View view = inflater.inflate(R.layout.fragment_viewreports, container, false);
        setHasOptionsMenu(true);

        reportList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recycler_reports);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        loadSearchOptions();
        db.collection("reports").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        reportList.clear();
                        for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                            Report report = documentSnapshot.toObject(Report.class);
                            report.setReportId(documentSnapshot.getId());
                            reportList.add(report);
                        }

                        adapter = new ReportAdapter(reportList);
                        adapter.applySearchOptions(searchText, filter, lost, found, resolved, up, non_up, sort, order_asc);
                        recyclerView.setAdapter(adapter);
                    }
                });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        db.collection("reports").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if(e != null) return;

                reportList.clear();
                for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                    Report report = documentSnapshot.toObject(Report.class);
                    report.setReportId(documentSnapshot.getId());
                    reportList.add(report);
                }

                /*Parcelable recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
                adapter = new ReportAdapter(reportList);
                adapter.applySearchOptions(searchText, filter, lost, found, resolved, up, non_up, sort, order_asc);
                recyclerView.setAdapter(adapter);
                recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);*/

                if(adapter != null){
                    adapter.reset(reportList);
                    adapter.applySearchOptions(searchText, filter, lost, found, resolved, up, non_up, sort, order_asc);
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.viewreports_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.viewreports_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search item or user...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchText = newText;
                if(adapter != null) {
                    adapter.applySearchOptions(searchText, filter, lost, found, resolved, up, non_up, sort, order_asc);
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.viewreports_filter:
                openSearchOptions();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openSearchOptions(){
        Dialog_SearchOptions searchOptionsDialog = new Dialog_SearchOptions();
        searchOptionsDialog.show(getFragmentManager().beginTransaction(), "Search Options Dialog");
        searchOptionsDialog.setTargetFragment(Fragment_ViewReports.this, 1);
        Bundle args = new Bundle();
        args.putInt("filter", filter);
        args.putBoolean("lost", lost);
        args.putBoolean("found", found);
        args.putBoolean("resolved", resolved);
        args.putBoolean("up", up);
        args.putBoolean("non_up", non_up);
        args.putInt("sort", sort);
        args.putBoolean("order_asc", order_asc);
        searchOptionsDialog.setArguments(args);

    }

    @Override
    public void onApplySearchOptionsClicked(int filter, boolean lost, boolean found, boolean resolved, boolean up, boolean non_up, int sort, boolean order_asc) {
        this.filter = filter;
        this.lost = lost;
        this.found = found;
        this.resolved = resolved;
        this.up = up;
        this.non_up = non_up;
        this.sort = sort;
        this.order_asc = order_asc;
        if(adapter != null) adapter.applySearchOptions(searchText, filter, lost, found, resolved, up, non_up, sort, order_asc);
    }

    @Override
    public void onPause() {
        saveSearchOptions();
        super.onPause();
    }

    private void saveSearchOptions(){
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("SEARCH_OPTIONS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("FILTER", filter);
        editor.putBoolean("LOST", lost);
        editor.putBoolean("FOUND", found);
        editor.putBoolean("RESOLVED", resolved);
        editor.putBoolean("UP", up);
        editor.putBoolean("NON-UP", non_up);
        editor.putInt("SORT", sort);
        editor.putBoolean("ORDER-ASC", order_asc);
        editor.apply();
    }

    private void loadSearchOptions(){
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("SEARCH_OPTIONS", Context.MODE_PRIVATE);
        filter = sharedPreferences.getInt("FILTER", 1);
        lost = sharedPreferences.getBoolean("LOST", true);
        found = sharedPreferences.getBoolean("FOUND", true);
        resolved = sharedPreferences.getBoolean("RESOLVED", true);
        up = sharedPreferences.getBoolean("UP", true);
        non_up = sharedPreferences.getBoolean("NON-UP", true);
        sort = sharedPreferences.getInt("SORT", 1);
        order_asc = sharedPreferences.getBoolean("ORDER-ASC", false);
    }
}