package com.ngonyoku.maneno;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;

    //Views
    private Button mCreateBtn;
    private TextInputLayout mDisplayName, mEmail, mPassword;
    private ProgressBar mProgressBar;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        mCreateBtn = findViewById(R.id.reg_create_btn);
        mDisplayName = findViewById(R.id.reg_display_name);
        mEmail = findViewById(R.id.reg_email);
        mPassword = findViewById(R.id.reg_password);
        mProgressBar = findViewById(R.id.reg_progressBar);
        mToolbar = findViewById(R.id.register_toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(getString(R.string.create_account));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String displayName = mDisplayName.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString().trim();
                String password = mPassword.getEditText().getText().toString();
                /*If The fields are not empty, we proceed with Registering the Account*/
                if (!email.isEmpty() && !password.isEmpty() && !displayName.isEmpty()) {
                    registerUser(displayName, email, password);
                    showProgress(true);
                } else {
                    Toast.makeText(RegisterActivity.this, "Please Fill in All Fields", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /*Register New Users*/
    private void registerUser(final String displayName, String email, String password) {
        mAuth
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            assert currentUser != null;
                            String uid = currentUser.getUid();
                            /*Update User ProfilegetString(R.string.db_field_name) Details*/
//                            currentUser.updateProfile(
//                                    new UserProfileChangeRequest.Builder()
//                                            .setDisplayName(displayName)
//                                            .build()
//                            );
                            mDatabaseReference = FirebaseDatabase
                                    .getInstance()
                                    .getReference()
                                    .child(getString(R.string.db_node_users))
                                    .child(uid)
                            ;

                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put(getString(R.string.db_field_name), displayName);
                            userMap.put(getString(R.string.db_field_status), getString(R.string.default_status));
                            userMap.put(getString(R.string.db_field_image), "");
                            userMap.put(getString(R.string.db_field_thumb_Image), "");

                            mDatabaseReference.setValue(userMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                startActivity(
                                                        new Intent(RegisterActivity.this, MainActivity.class)
                                                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                                );
                                                Toast.makeText(RegisterActivity.this, "Welcome to " + getString(R.string.app_name), Toast.LENGTH_LONG).show();
                                                finish();
                                            }
                                        }
                                    })
                            ;
                            showProgress(false);
                        } else {
                            Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            showProgress(false);
                        }
                    }
                })
        ;
    }

    private void showProgress(boolean show) {
        if (show) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.GONE);
        }
    }
}