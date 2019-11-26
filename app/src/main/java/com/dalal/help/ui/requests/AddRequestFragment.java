package com.dalal.help.ui.requests;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.dalal.help.MainActivity;
import com.dalal.help.R;
import com.dalal.help.utils.Request;
import com.dalal.help.utils.RequestStatus;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class AddRequestFragment extends Fragment {

    private EditText nameET, amountET, descriptionET;
    private Spinner serviceTypeSpinner;
    List<String> list = new ArrayList<>();
    private AlertDialog alertDialog;
    private ProgressDialog progressDialog;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private String serviceType;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_request, container, false);
        progressDialog = new ProgressDialog(view.getContext());
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getContext());
        alertDialogBuilder.setTitle("data not valid");
        alertDialogBuilder
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        alertDialog = alertDialogBuilder.create();
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("requests");
        DatabaseReference serviceTypeRef = FirebaseDatabase.getInstance().getReference("services");
        nameET = (EditText) view.findViewById(R.id.serviceName);
        amountET = (EditText) view.findViewById(R.id.amount);
        descriptionET = (EditText) view.findViewById(R.id.description);
        serviceTypeSpinner = view.findViewById(R.id.serviceType);

        final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(view.getContext(),
                R.layout.spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        serviceTypeSpinner.setAdapter(dataAdapter);
        serviceTypeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    list.add(postSnapshot.child("name").getValue(String.class));
                }
                dataAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        serviceTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                serviceType = parent.getItemAtPosition(position).toString();
                if (serviceType.equalsIgnoreCase("money")) {
                    nameET.setVisibility(View.GONE);
                    amountET.setVisibility(View.VISIBLE);
                } else {
                    amountET.setVisibility(View.GONE);
                    nameET.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                amountET.setVisibility(View.GONE);
                nameET.setVisibility(View.VISIBLE);
            }
        });

        view.findViewById(R.id.add_request_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRequest(v);
            }
        });

        return view;
    }

    public void addRequest(View view) {
        final String name = nameET.getText().toString().trim();
        final String description = descriptionET.getText().toString().trim();
        final String amount = amountET.getText().toString().trim();
        String message = getString(R.string.value_required_msg).trim();

        if (TextUtils.isEmpty(name) && (TextUtils.isEmpty(amount) || Double.valueOf(amount) <= 0)) {
            message = TextUtils.isEmpty(name) ? MessageFormat.format(message, "Name") : MessageFormat.format(message, "Amount");
            alertDialog.setMessage(message);
            alertDialog.show();
            return;
        } else if (TextUtils.isEmpty(description)) {
            message = MessageFormat.format(message, "Description");
            alertDialog.setMessage(message);
            alertDialog.show();
            return;
        }
        progressDialog.setMessage("Adding Request");
        progressDialog.show();
        String key = databaseReference.push().getKey();
        Request request = new Request(name, TextUtils.isEmpty(amount) ? 0.0 : Double.valueOf(amount), description, RequestStatus.PENDING.getValue(), firebaseAuth.getCurrentUser().getUid(), serviceType);
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, request);
        databaseReference.updateChildren(childUpdates);
        progressDialog.hide();
        startActivity(new Intent(this.getContext(), MainActivity.class));
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.cancel();
        }
    }

}