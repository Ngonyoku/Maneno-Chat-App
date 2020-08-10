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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {
    //Firebase
    private DatabaseReference mStatusDatabaseRef;
    private FirebaseUser mCurrentUser;

    //Views
    private Toolbar mToolbar;
    private TextInputLayout mStatus;
    private Button mSaveBtn;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert mCurrentUser != null;
        String current_uid = mCurrentUser.getUid();

        mStatusDatabaseRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(getString(R.string.db_node_users))
                .child(current_uid)
        ;

        mToolbar = findViewById(R.id.status_appBar);
        mStatus = findViewById(R.id.status_input);
        mSaveBtn = findViewById(R.id.status_save_btn);
        mProgressBar = findViewById(R.id.status_progressBar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        getSupportActionBar().setTitle(getString(R.string.update_status));

        Intent status = getIntent();
        if (status.getExtras() != null) {
            mStatus.getEditText().setText(status.getStringExtra(SettingsActivity.KEY_STATUS_DATA));
        }

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress(true);
                String status = mStatus.getEditText().getText().toString();
                if (status.isEmpty()) {
                    status = getString(R.string.default_status);
                }
                /*We set the new Status*/
                mStatusDatabaseRef
                        .child(getString(R.string.db_field_status)).setValue(status)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    showProgress(false);
                                    Toast.makeText(StatusActivity.this, "Status updated successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    showProgress(false);
                                    Toast.makeText(StatusActivity.this, "OOps! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                ;
            }
        });

    }

    private void showProgress(boolean show) {
        if (show) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.GONE);
        }
    }
}