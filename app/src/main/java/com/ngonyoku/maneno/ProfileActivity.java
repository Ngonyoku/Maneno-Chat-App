package com.ngonyoku.maneno;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";

    //Firebase
    private DatabaseReference mUsersDatabaseRef;
    private DatabaseReference mFriendRequestRef;
    private FirebaseUser mCurrentUser;

    //Views
    private ImageView mDisplayImage;
    private TextView mDisplayName, mStatus, mFriends;
    private Button mAddFriendButton;

    private String mCurrent_state;
    private String user_id; /*This is the UID of the user who owns the current profile*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mUsersDatabaseRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.db_node_users));
        mFriendRequestRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.db_node_friend_request));
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mDisplayName = findViewById(R.id.profile_display_name);
        mDisplayImage = findViewById(R.id.profile_display_photo);
        mStatus = findViewById(R.id.profile_status);
        mFriends = findViewById(R.id.profile_friends);
        mAddFriendButton = findViewById(R.id.profile_add_friend);

        mCurrent_state = "not_friend";

        /*Load the Data inside the Views*/
        if (getIntent().getExtras() != null) {
            user_id = getIntent().getStringExtra(UsersActivity.KEY_USER_ID);
            mDisplayName.setText(getIntent().getStringExtra(UsersActivity.KEY_DISPLAY_NAME));
            mStatus.setText(getIntent().getStringExtra(UsersActivity.KEY_STATUS));
            Picasso
                    .get()
                    .load(getIntent().getStringExtra(UsersActivity.KEY_IMAGE_URL))
                    .fit()
                    .into(mDisplayImage)
            ;
        }

        /*Send a Friend Request*/
        mAddFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrent_state.equals("not_friend")) {
                    /*If the user is not a Friend, we can allow the Current user to send a Friend Request*/
                    mFriendRequestRef
                            .child(mCurrentUser.getUid())
                            .child(user_id)
                            .child("request_type")
                            .setValue("sent")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        mFriendRequestRef
                                                .child(user_id)
                                                .child(mCurrentUser.getUid())
                                                .child("request_type")
                                                .setValue("received")
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(ProfileActivity.this, "Request Sent", Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                        ;
                                    } else {
                                        Toast.makeText(ProfileActivity.this, "Failed Sending Request", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                    ;
                }
            }
        });
    }
}