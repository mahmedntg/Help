package com.dalal.help;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.dalal.help.utils.GPS_Service;
import com.dalal.help.utils.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.MessageFormat;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameET, phoneET, emailET, passwordET, confirmPasswordET;
    private RadioGroup typeRG;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private AlertDialog alertDialog;
    GPS_Service gps;
    private double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        progressDialog = new ProgressDialog(this);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);
        alertDialogBuilder.setTitle("data not valid");
        alertDialogBuilder
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        alertDialog = alertDialogBuilder.create();
        nameET = (EditText) findViewById(R.id.name);
        phoneET = (EditText) findViewById(R.id.phone);
        emailET = (EditText) findViewById(R.id.user_email);
        passwordET = (EditText) findViewById(R.id.user_password);
        confirmPasswordET = (EditText) findViewById(R.id.user_confirm_password);
        typeRG = (RadioGroup) findViewById(R.id.typeRadioGroup);

    }

    public void registerUser(View view) {
        final String name = nameET.getText().toString().trim();
        final String phone = phoneET.getText().toString().trim();
        final String email = emailET.getText().toString().trim();
        final String password = passwordET.getText().toString().trim();
        String confirmPassword = confirmPasswordET.getText().toString().trim();
        int typeId = typeRG.getCheckedRadioButtonId();
        final String type = ((RadioButton) findViewById(typeId)).getText().toString();

        String message = getString(R.string.value_required_msg).trim();
        if (TextUtils.isEmpty(name)) {
            message = MessageFormat.format(message, "Name");
            alertDialog.setMessage(message);
            alertDialog.show();
            return;
        } else if (TextUtils.isEmpty(email)) {
            message = MessageFormat.format(message, "email");
            alertDialog.setMessage(message);
            alertDialog.show();
            return;
        } else if (TextUtils.isEmpty(password)) {
            message = MessageFormat.format(message, "Password");
            alertDialog.setMessage(message);
            alertDialog.show();
            return;
        } else if (TextUtils.isEmpty(confirmPassword)) {
            message = MessageFormat.format(message, "Confirm Password");
            alertDialog.setMessage(message);
            alertDialog.show();
            return;
        } else if (!password.equals(confirmPassword)) {
            message = MessageFormat.format(message, "Password and Confirm Password not match");
            alertDialog.setMessage(message);
            alertDialog.show();
            return;
        }
        getLocation();
        progressDialog.setMessage("Registering user");
        progressDialog.show();
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String userId = firebaseAuth.getCurrentUser().getUid();
                    DatabaseReference ref = databaseReference.child(userId);
                    User user = new User(name, phone, email, type, latitude, longitude);
                    ref.setValue(user);
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                } else {
                    progressDialog.hide();
                    alertDialog.setMessage(task.getException().getMessage());
                    alertDialog.show();
                }

            }
        });
    }

    private void getLocation() {
        String tim = "0";
        gps = new GPS_Service(this, tim);
        startService(new Intent(this, GPS_Service.class));
        if (gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

        } else {
            gps.showSettingsAlert();
        }

    }

    public void openLoginActivity(View view) {
        startActivity(new Intent(this, LoginActivity.class));
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.cancel();
        }
    }

}
