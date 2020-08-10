package com.ngonyoku.maneno;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {
    private static final String TAG = "UsersActivity";
    public static final int PROFILE_REQUEST_CODE = 3;
    public static final String KEY_USER_ID = "com.ngonyoku.maneno.USER_ID";
    public static final String KEY_DISPLAY_NAME = "com.ngonyoku.maneno.KEY_DISPLAY_NAME";
    public static final String KEY_IMAGE_URL = "com.ngonyoku.maneno.KEY_IMAGE_URL";
    public static final String KEY_STATUS = "com.ngonyoku.maneno.KEY_STATUS";
    //Firebase
    private DatabaseReference mUsersDatabaseRef;

    //Views
    private Toolbar mToolbar;
    private RecyclerView mUsersList;
    private ProgressBar mProgressBar;
    private FirebaseRecyclerAdapter<Users, UsersViewHolder> mFirebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mUsersDatabaseRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.db_node_users));
        mUsersDatabaseRef.keepSynced(true);

        mToolbar = findViewById(R.id.users_appBar);
        mUsersList = findViewById(R.id.users_list);
        mProgressBar = findViewById(R.id.users_progressBar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(getString(R.string.users));
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        showProgress(true);
        Query query = mUsersDatabaseRef;
        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(query, Users.class)
                        .build();
        mFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(options) {
            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new UsersViewHolder(
                        LayoutInflater
                                .from(parent.getContext())
                                .inflate(R.layout.users_single_layout, parent, false)
                );
            }

            @Override
            protected void onBindViewHolder(@NonNull final UsersViewHolder holder, int position, @NonNull final Users model) {
                holder.mDisplayName.setText(model.getName());
                holder.mStatus.setText(model.getStatus());
                final String image = (model.getThumb_image() == null) ? model.getImage() : model.getThumb_image();
                final String user_id = getRef(position).getKey(); /*Returns the key of the current node under users node(in this case the Users Id)*/
                if (!image.isEmpty()) {
                    Picasso
                            .get()
                            .load(image)
                            .fit()
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.color.colorPrimaryLight)
                            .into(holder.mImageView, new Callback() {
                                @Override
                                public void onSuccess() {
                                    /*If image was retrieved successfully from offline, then ...*/
                                }

                                @Override
                                public void onError(Exception e) {
                                    /*... else, We should retrieve the Image online.*/
                                    Picasso.get().load(image).fit().placeholder(R.color.colorPrimaryLight).into(holder.mImageView);
                                }
                            })
                    ;
                }
                showProgress(false);

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivityForResult(
                                new Intent(getApplicationContext(), ProfileActivity.class)
                                        .putExtra(KEY_USER_ID, user_id)
                                        .putExtra(KEY_DISPLAY_NAME, model.getName())
                                        .putExtra(KEY_IMAGE_URL, model.getImage())
                                        .putExtra(KEY_STATUS, model.getStatus()),
                                PROFILE_REQUEST_CODE)
                        ;
                        Log.d(TAG, "onClick: userId => " + user_id);
                    }
                });
            }
        };
        mFirebaseRecyclerAdapter.startListening();/*Listens to changes in the Database and Updates the List*/
        mUsersList.setAdapter(mFirebaseRecyclerAdapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFirebaseRecyclerAdapter.stopListening();/*Stops Listening to changes in the Database*/
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {
        private View mView;
        private CircleImageView mImageView;
        private TextView mDisplayName, mStatus;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;

            mImageView = itemView.findViewById(R.id.user_single_image);
            mDisplayName = itemView.findViewById(R.id.user_single_name);
            mStatus = itemView.findViewById(R.id.user_single_status);
        }
    }

    private void showProgress(boolean show) {
        if (show) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.GONE);
        }
    }
}