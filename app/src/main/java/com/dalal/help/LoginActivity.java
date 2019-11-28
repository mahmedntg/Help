package com.dalal.help;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.dalal.help.utils.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.text.MessageFormat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private AlertDialog alertDialog;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        progressDialog = new ProgressDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
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
        emailEditText = (EditText) findViewById(R.id.user_email);
        passwordEditText = (EditText) findViewById(R.id.user_password);

    }

    public void loginUser(View view) {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String message = getString(R.string.value_required_msg);
        if (TextUtils.isEmpty(email)) {
            message = MessageFormat.format(message, "Email");
            alertDialog.setMessage(message);
            alertDialog.show();
            return;
        } else if (TextUtils.isEmpty(password)) {
            message = MessageFormat.format(message, "Password");
            alertDialog.show();
            return;
        }
        progressDialog.setMessage("Login, please wait");
        progressDialog.setCancelable(false);
        progressDialog.show();


        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    final String userId = firebaseAuth.getCurrentUser().getUid();
                    firebaseDatabase.getReference("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                            String token = preferences.getString("user_token", null);
                            dataSnapshot.getRef().child("token").setValue(token);
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            SharedPreferences.Editor editor = preferences.edit();
                            user.setUserId(userId);
                            editor.putString("user", new Gson().toJson(user));
                            editor.apply();
                            startActivity(intent);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    progressDialog.dismiss();

                } else {
                    alertDialog.setMessage(task.getException().getMessage());
                    alertDialog.show();
                    progressDialog.dismiss();
                }

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.cancel();
        }
    }


    public void openRegisterActivity(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
    }
}
