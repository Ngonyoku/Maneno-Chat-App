package com.ngonyoku.maneno;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    public static final int STATUS_REQUEST_CODE = 1;
    public static final String STATUS_DATA = "com.ngonyoku.maneno.STATUS_DATA";

    //Firebase
    private DatabaseReference mUserDatabaseRef;
    private FirebaseUser mCurrentUser;

    //Views
    private CircleImageView mDisplayImage;
    private TextView mDisplayName, mStatus;
    private Button mChangeImageBtn, mChangeStatusBtn;

    private String mStatusData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mDisplayImage = findViewById(R.id.settings_image);
        mDisplayName = findViewById(R.id.settings_display_name);
        mStatus = findViewById(R.id.settings_status);
        mChangeImageBtn = findViewById(R.id.settings_change_image_btn);
        mChangeStatusBtn = findViewById(R.id.settings_change_status_btn);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert mCurrentUser != null;
        String current_uid = mCurrentUser.getUid();
        mUserDatabaseRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(getString(R.string.db_node_users))
                .child(current_uid)
        ;

        mChangeStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(SettingsActivity.this, StatusActivity.class)
                                .putExtra(STATUS_DATA, mStatusData),
                        STATUS_REQUEST_CODE);
            }
        });

        /*We retrieve data(User info) from the database and display them*/
        mUserDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                /*The data is retrieved and saved in the following strings*/
                String name = snapshot.child(getString(R.string.db_field_name)).getValue().toString();
                String image = snapshot.child(getString(R.string.db_field_image)).getValue().toString();
                String status = snapshot.child(getString(R.string.db_field_status)).getValue().toString();
                String thumb_image = snapshot.child(getString(R.string.db_field_thumb_Image)).getValue().toString();

                mDisplayName.setText(name);
                mStatus.setText(status);

                mStatusData = status;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}