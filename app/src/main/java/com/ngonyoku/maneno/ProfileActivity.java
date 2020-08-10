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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";

    //Firebase
    private DatabaseReference mUsersDatabaseRef;
    private DatabaseReference mFriendRequestRef, mFriendRef;
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
        mFriendRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.db_node_friends));
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mDisplayName = findViewById(R.id.profile_display_name);
        mDisplayImage = findViewById(R.id.profile_display_photo);
        mStatus = findViewById(R.id.profile_status);
        mFriends = findViewById(R.id.profile_friends);
        mAddFriendButton = findViewById(R.id.profile_add_friend);

        mCurrent_state = getString(R.string.not_friend);

        /*-----------Load the Data inside the Views-----------------------*/
        if (getIntent().getExtras() != null) {
            user_id = getIntent().getStringExtra(UsersActivity.KEY_USER_ID);
            mDisplayName.setText(getIntent().getStringExtra(UsersActivity.KEY_DISPLAY_NAME));
            mStatus.setText(getIntent().getStringExtra(UsersActivity.KEY_STATUS));
            final String imageUrl = getIntent().getStringExtra(UsersActivity.KEY_IMAGE_URL);
            if (!imageUrl.isEmpty()) {
                Picasso
                        .get()
                        .load(imageUrl)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .fit()
                        .into(mDisplayImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(imageUrl).fit().into(mDisplayImage);
                            }
                        })
                ;
            } else {
                Toast.makeText(this, "User doesn't have an Image", Toast.LENGTH_SHORT).show();
            }
        }

        /*--------------- Detect if a user has been send a friend Request or not---------------*/
        mFriendRequestRef
                .child(mCurrentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(user_id)) {
                            String request_type = snapshot /*We get the Request type from the database*/
                                    .child(user_id)
                                    .child(getString(R.string.db_field_request_type))
                                    .getValue()
                                    .toString();

                            if (request_type.equals(getString(R.string.received))) {
                                mCurrent_state = getString(R.string.request_received);
                                mAddFriendButton.setText(R.string.accept_friend_request);
                                mAddFriendButton.setBackground(getDrawable(R.color.colorGreen));
                            } else if (request_type.equals(getString(R.string.sent))) {
                                mCurrent_state = getString(R.string.request_sent);
                                mAddFriendButton.setText(R.string.cancel_friend_request);
                                mAddFriendButton.setBackground(getDrawable(R.color.colorRed));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ProfileActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
        ;

        /*------------------Detect if a User is already a Friend or Not-----------------------*/
        mFriendRef
                .child(mCurrentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(user_id)) {
                            mCurrent_state = getString(R.string.friends); /*Change the current state to request_sent*/
                            mAddFriendButton.setText(R.string.unfriend);
                            mAddFriendButton.setBackground(getDrawable(R.color.colorRed));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ProfileActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
        ;

        /*------------------- Send/Cancel Friend Request---------------------*/
        mAddFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAddFriendButton.setEnabled(false);

                /*Send Friend Request*/
                if (mCurrent_state.equals(getString(R.string.not_friend))) {
                    /*If the user is not a Friend, we can allow the Current user to send a Friend Request*/
                    mFriendRequestRef
                            .child(mCurrentUser.getUid())
                            .child(user_id)
                            .child(getString(R.string.db_field_request_type))
                            .setValue(getString(R.string.sent))
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        mFriendRequestRef
                                                .child(user_id)
                                                .child(mCurrentUser.getUid())
                                                .child(getString(R.string.db_field_request_type))
                                                .setValue(getString(R.string.received))
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        mAddFriendButton.setEnabled(true);
                                                        mCurrent_state = getString(R.string.request_sent); /*Change the current state to request_sent*/
                                                        mAddFriendButton.setText(R.string.cancel_friend_request);
                                                        mAddFriendButton.setBackground(getDrawable(R.color.colorRed));
//                                                        Toast.makeText(ProfileActivity.this, "Request Sent", Toast.LENGTH_SHORT).show();
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

                /*Cancel Friend Request*/
                if (mCurrent_state.equals(getString(R.string.request_sent))) {
                    mFriendRequestRef
                            .child(mCurrentUser.getUid())
                            .child(user_id)
                            .removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendRequestRef
                                            .child(user_id)
                                            .child(mCurrentUser.getUid())
                                            .removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mAddFriendButton.setEnabled(true);
                                                    mCurrent_state = getString(R.string.not_friend); /*Change the current state to request_sent*/
                                                    mAddFriendButton.setText(R.string.add_friend);
                                                    mAddFriendButton.setBackground(getDrawable(R.color.colorAccent));
                                                    Toast.makeText(ProfileActivity.this, "Request Cancelled", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                    ;
                                }
                            })
                    ;
                }

                /*Receive Friend Request*/
                if (mCurrent_state.equals(getString(R.string.request_received))) {
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    mFriendRef/*Add the user as a Friend in the friends node in the database*/
                            .child(mCurrentUser.getUid())
                            .child(user_id)
                            .setValue(currentDate)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        mFriendRef/*Add the user as a Friend in the friends node in the database*/
                                                .child(user_id)
                                                .child(mCurrentUser.getUid())
                                                .setValue(currentDate)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        mFriendRequestRef
                                                                .child(mCurrentUser.getUid())
                                                                .child(user_id)
                                                                .removeValue()
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        mFriendRequestRef
                                                                                .child(user_id)
                                                                                .child(mCurrentUser.getUid())
                                                                                .removeValue()
                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void aVoid) {
                                                                                        mAddFriendButton.setEnabled(true);
                                                                                        mCurrent_state = getString(R.string.friends); /*Change the current state to request_sent*/
                                                                                        mAddFriendButton.setText(R.string.unfriend);
                                                                                        mAddFriendButton.setBackground(getDrawable(R.color.colorRed));
                                                                                        Toast.makeText(ProfileActivity.this, "You are now Friends", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                })
                                                                        ;
                                                                    }
                                                                })
                                                        ;
                                                    }
                                                })
                                        ;
                                    } else {
                                        Toast.makeText(ProfileActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                    ;
                }
            }
        });
    }
}