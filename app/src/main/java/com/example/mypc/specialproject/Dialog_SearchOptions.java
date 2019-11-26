package com.example.mypc.specialproject;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class Dialog_SearchOptions extends DialogFragment {
    private RadioGroup filterReports;
    private CheckBox filterLost;
    private CheckBox filterFound;
    private CheckBox filterResolved;
    private CheckBox filterUP;
    private CheckBox filterNonUP;

    private RadioGroup sortReports;
    private RadioGroup sortOrder;

    private int filter, sort;
    private boolean lost, found, resolved, up, non_up, order_asc;

    private DialogListener listener;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_searchoptions, null);
        filterReports = view.findViewById(R.id.radiogroup_filter);
        filterLost = view.findViewById(R.id.checkbox_filterlost);
        filterFound = view.findViewById(R.id.checkbox_filterfound);
        filterResolved = view.findViewById(R.id.checkbox_filterresolved);
        filterUP = view.findViewById(R.id.checkbox_filterup);
        filterNonUP = view.findViewById(R.id.checkbox_filternonup);
        sortReports = view.findViewById(R.id.radiogroup_sort);
        sortOrder = view.findViewById(R.id.radiogroup_sortorder);

        //initialize search settings
        filter = getArguments().getInt("filter");
        lost = getArguments().getBoolean("lost");
        found = getArguments().getBoolean("found");
        resolved = getArguments().getBoolean("resolved");
        up = getArguments().getBoolean("up");
        non_up = getArguments().getBoolean("non_up");
        sort = getArguments().getInt("sort");
        order_asc = getArguments().getBoolean("order_asc");

        switch(filter){
            case 1:
                filterReports.check(R.id.radio_filterall);
                break;
            case 2:
                filterReports.check(R.id.radio_filterfollow);
                break;
            case 3:
                filterReports.check(R.id.radio_filteryour);
                break;
            default: filterReports.check(R.id.radio_filterall);
        }
        filterLost.setChecked(lost);
        filterFound.setChecked(found);
        filterResolved.setChecked(resolved);
        filterUP.setChecked(up);
        filterNonUP.setChecked(non_up);
        switch(sort){
            case 1:
                sortReports.check(R.id.radio_sortupdated);
                break;
            case 2:
                sortReports.check(R.id.radio_sortreported);
                break;
            case 3:
                sortReports.check(R.id.radio_sortreporter);
                break;
            default: sortReports.check(R.id.radio_sortupdated);
        }
        if(order_asc) sortOrder.check(R.id.radio_sortasc);
        else sortOrder.check(R.id.radio_sortdesc);

        builder.setView(view)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) { }
                }).setPositiveButton("Apply changes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //get filter values
                        switch(filterReports.getCheckedRadioButtonId()){
                            case R.id.radio_filterall:
                                filter = 1;
                                break;
                            case R.id.radio_filterfollow:
                                filter = 2;
                                break;
                            case R.id.radio_filteryour:
                                filter = 3;
                                break;
                            default: filter = 1;
                        }
                        lost = filterLost.isChecked();
                        found = filterFound.isChecked();
                        resolved = filterResolved.isChecked();
                        up = filterUP.isChecked();
                        non_up = filterNonUP.isChecked();

                        //get sort values
                        switch(sortReports.getCheckedRadioButtonId()){
                            case R.id.radio_sortupdated:
                                sort = 1;
                                break;
                            case R.id.radio_sortreported:
                                sort = 2;
                                break;
                            case R.id.radio_sortreporter:
                                sort = 3;
                                break;
                            default: sort = 1;
                        }
                        order_asc = (sortOrder.getCheckedRadioButtonId() == R.id.radio_sortasc);

                        listener.onApplySearchOptionsClicked(filter, lost, found, resolved, up, non_up, sort, order_asc);
                    }
        });
        return builder.create();
    }

    public interface DialogListener{
        void onApplySearchOptionsClicked(int filter, boolean lost, boolean found, boolean resolved, boolean up, boolean non_up, int sort, boolean order_asc);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (DialogListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement DialogListener");
        }
    }
}
