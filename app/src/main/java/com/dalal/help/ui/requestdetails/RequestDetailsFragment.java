package com.dalal.help.ui.requestdetails;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dalal.help.MainActivity;
import com.dalal.help.R;
import com.dalal.help.call.APICall;
import com.dalal.help.utils.Request;
import com.dalal.help.utils.RequestStatus;
import com.dalal.help.utils.User;
import com.dalal.help.utils.UserType;
import com.dalal.help.utils.UserUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class RequestDetailsFragment extends Fragment {

    private ProgressDialog progressDialog;
    private TextView nameTV, userTV, descriptionTV;
    private Button contactBtn, locationBtn, approveBtn, rejectBtn;
    private User loginUser, userRequest;
    private DatabaseReference requestRef;
    private Request request;
    private EditText msg;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request_details, container, false);
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        requestRef = FirebaseDatabase.getInstance().getReference("requests");
        progressDialog.show();
        contactBtn = view.findViewById(R.id.contact_btn);
        locationBtn = view.findViewById(R.id.location_btn);
        approveBtn = view.findViewById(R.id.approve_btn);
        rejectBtn = view.findViewById(R.id.reject_btn);
        nameTV = (TextView) view.findViewById(R.id.serviceName);
        descriptionTV = (TextView) view.findViewById(R.id.description);
        userTV = (TextView) view.findViewById(R.id.user);
        request = (Request) getArguments().getSerializable("request");
        if (TextUtils.isEmpty(request.getAmount())) {
            nameTV.setText("Name:\n" + request.getName());
        } else {
            nameTV.setText("Amount:\n" + request.getAmount() + " KWD");
        }
        descriptionTV.setText("Description:\n" + request.getDescription());
        loginUser = UserUtils.getInstance(getActivity()).getUser();
        if (!loginUser.getType().equals(UserType.DONATOR.getValue())) {
            userTV.setVisibility(View.VISIBLE);
            contactBtn.setVisibility(View.VISIBLE);
            locationBtn.setVisibility(View.VISIBLE);
            approveBtn.setVisibility(View.VISIBLE);
            rejectBtn.setVisibility(View.VISIBLE);
            FirebaseDatabase.getInstance().getReference("users").child(request.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    userRequest = dataSnapshot.getValue(User.class);
                    userTV.setText("XXX " + userRequest.getName().substring(2, 8) + " XXX");
                    progressDialog.hide();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    progressDialog.hide();
                }
            });

        } else progressDialog.hide();

        contactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contactUser(userRequest.getPhone());
            }
        });
        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLocation();
            }
        });
        approveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog(loginUser.getType().equals(UserType.DONOR.getValue()) ? RequestStatus.COMPLETED : RequestStatus.ACCEPTED);
            }
        });
        rejectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog(RequestStatus.REJECTED);
            }
        });

        return view;
    }

    private void contactUser(String mobileNumber) {
        Uri u = Uri.parse("tel:" + mobileNumber);

        // Create the intent and set the data for the
        // intent as the phone number.
        Intent i = new Intent(Intent.ACTION_DIAL, u);
        startActivity(i);
    }

    private void openLocation() {
        String strUri = "http://maps.google.com/maps?q=loc:" + userRequest.getLatitude() + "," + userRequest.getLongitude() + " (" + userRequest.getPhone() + ")";
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(strUri));

        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");

        startActivity(intent);
    }


    private void updateStatus(RequestStatus status) {
        progressDialog.setMessage("Update Request...");
        progressDialog.setIndeterminate(true);
        progressDialog.show();
        Map<String, Object> data = new HashMap<>();
        data.put("status", status.getValue());
        data.put("reason", msg.getText().toString());
        requestRef.child(request.getKey()).updateChildren(data);
        progressDialog.hide();
        startActivity(new Intent(getActivity(), MainActivity.class));
        String message;
        if (TextUtils.isEmpty(msg.getText().toString().trim())) {
            if (status.equals(RequestStatus.ACCEPTED)) {
                message = "Your request has been approved";
            } else if (status.equals(RequestStatus.REJECTED)) {
                message = "Your request has been rejected";
            } else {
                message = "Your request has been completed";
            }
        } else message = msg.getText().toString().trim();
        sendNotification(message);

    }

    private void sendNotification(String message) {
        String title = "Request Processing";
        if (loginUser.getType().equalsIgnoreCase(UserType.DONOR.getValue())) {
            title = "Payment Succeeded";
        }
        APICall apiCall
                = new APICall();
        apiCall.sendNote(userRequest.getToken(), title, message);
    }

    public void openDialog(final RequestStatus status) {

        final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();

        // Set Custom Title
        TextView title = new TextView(getContext());
        // Title Properties
        title.setText("Send User Notification");
        title.setPadding(10, 10, 10, 10);   // Set Position
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.BLACK);
        title.setTextSize(20);
        alertDialog.setCustomTitle(title);

        // Set Message
        msg = new EditText(getContext());
        // Message Properties
        msg.setGravity(Gravity.CENTER_HORIZONTAL);
        msg.setTextColor(Color.BLACK);
        alertDialog.setView(msg);

        // Set Button
        // you can more buttons
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                updateStatus(status);
            }
        });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.hide();
            }
        });

        new Dialog(getContext());
        alertDialog.show();

        // Set Properties for OK Button
        final Button okBT = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
        LinearLayout.LayoutParams neutralBtnLP = (LinearLayout.LayoutParams) okBT.getLayoutParams();
        neutralBtnLP.gravity = Gravity.FILL_HORIZONTAL;
        okBT.setPadding(50, 10, 10, 10);   // Set Position
        okBT.setTextColor(Color.BLUE);
        okBT.setLayoutParams(neutralBtnLP);

        final Button cancelBT = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        LinearLayout.LayoutParams negBtnLP = (LinearLayout.LayoutParams) okBT.getLayoutParams();
        negBtnLP.gravity = Gravity.FILL_HORIZONTAL;
        cancelBT.setTextColor(Color.RED);
        cancelBT.setLayoutParams(negBtnLP);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.cancel();
        }
    }
}