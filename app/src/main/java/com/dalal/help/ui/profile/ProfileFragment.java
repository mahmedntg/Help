package com.dalal.help.ui.profile;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.dalal.help.R;
import com.dalal.help.utils.GPS_Service;
import com.dalal.help.utils.User;
import com.dalal.help.utils.UserUtils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    private ProgressDialog progressDialog;
    private EditText nameET, phoneET;
    private AlertDialog alertDialog;
    private DatabaseReference userRef;
    private double latitude, longitude;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(getActivity());
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle("data not valid");
        alertDialogBuilder
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        alertDialog = alertDialogBuilder.create();
        userRef = FirebaseDatabase.getInstance().getReference("users");
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        final User user = UserUtils.getInstance(getActivity()).getUser();
        nameET = view.findViewById(R.id.name);
        phoneET = view.findViewById(R.id.phone);
        nameET.setText(user.getName());
        phoneET.setText(user.getPhone());

        view.findViewById(R.id.update_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = getString(R.string.value_required_msg).trim();
                String name = nameET.getText().toString().trim();
                String phone = phoneET.getText().toString().trim();
                String userId = user.getUserId();
                if (TextUtils.isEmpty(name)) {
                    message = MessageFormat.format(message, "Name");
                    alertDialog.setMessage(message);
                    alertDialog.show();
                    return;
                } else if (name.length() < 15) {
                    alertDialog.setMessage("Name should be minimum 15 characters");
                    alertDialog.show();
                    return;
                } else if (TextUtils.isEmpty(phone)) {
                    message = MessageFormat.format(message, "Phone");
                    alertDialog.setMessage(message);
                    alertDialog.show();
                    return;
                }
                updateUser(userId, name, phone);
            }
        });
        return view;
    }

    private void updateUser(String userId, String name, String phone) {
        getLocation();
        progressDialog.setMessage("Update User..");
        progressDialog.setIndeterminate(true);
        progressDialog.show();
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("phone", phone);
        data.put("latitude", latitude);
        data.put("longitude", longitude);
        userRef.child(userId).updateChildren(data);
        nameET.setText(name);
        phoneET.setText(phone);
        progressDialog.hide();

    }

    private void getLocation() {
        String tim = "0";
        GPS_Service gps = new GPS_Service(getActivity(), tim);
        getActivity().startService(new Intent(getActivity(), GPS_Service.class));
        if (gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

        } else {
            gps.showSettingsAlert();
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.cancel();
        }
    }

}