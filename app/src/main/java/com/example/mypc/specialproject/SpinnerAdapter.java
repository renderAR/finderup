package com.example.mypc.specialproject;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SpinnerAdapter extends ArrayAdapter<User> {
    public SpinnerAdapter(Context context, ArrayList<User> userList){
        super(context, 0, userList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    private View initView(int position, View convertView, ViewGroup parent){
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_row, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.spinner_userelement);

        User currentUser = getItem(position);
        if(currentUser != null) {
            if(currentUser.getId()!= null) {
                textView.setText(currentUser.getName() + " ");
                if (currentUser.getEmail().contains("up.edu.ph")) {
                    textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check_circle, 0);
                } else {
                    textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }
            }else{
                textView.setText("<no user>");
                textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                textView.setTextColor(Color.parseColor("#777777"));
            }
        }

        return convertView;
    }
}
