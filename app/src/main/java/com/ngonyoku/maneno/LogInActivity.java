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

public class LogInActivity extends AppCompatActivity {
    //Firebase
    private FirebaseAuth mAuth;

    //Views
    private Button mLogIn;
    private TextInputLayout mEmail, mPassword;
    private ProgressBar mProgressBar;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        mAuth = FirebaseAuth.getInstance();

        mLogIn = findViewById(R.id.login_btn);
        mEmail = findViewById(R.id.login_email);
        mPassword = findViewById(R.id.login_password);
        mProgressBar = findViewById(R.id.login_progressBar);
        mToolbar = findViewById(R.id.login_toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(getString(R.string.log_in));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);

        mLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getEditText().getText().toString().trim();
                String password = mPassword.getEditText().getText().toString();
                /*If The fields are not empty, we proceed with Registering the Account*/
                if (!email.isEmpty() && !password.isEmpty()) {
                    logIn(email, password);
                    showProgress(true);
                } else {
                    Toast.makeText(LogInActivity.this, "Please Fill in All Fields", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /*Sign In new Users*/
    private void logIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            showProgress(false);
                            startActivity(
                                    new Intent(LogInActivity.this, MainActivity.class)
                                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            );
                            Toast.makeText(LogInActivity.this, "Welcome Back", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(LogInActivity.this, "Oops, " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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