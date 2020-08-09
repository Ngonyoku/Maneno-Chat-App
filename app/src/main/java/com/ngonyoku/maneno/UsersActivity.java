package com.ngonyoku.maneno;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {
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
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull Users model) {
                holder.mDisplayName.setText(model.getName());
                holder.mStatus.setText(model.getStatus());
                Picasso.get()
                        .load(model.getImage())
                        .fit()
                        .placeholder(R.color.colorPrimaryLight)
                        .into(holder.mImageView)
                ;
                showProgress(false);
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
        private CircleImageView mImageView;
        private TextView mDisplayName, mStatus;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);

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