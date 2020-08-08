package com.ngonyoku.maneno;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    //Firebase
    private FirebaseAuth mAuth;

    //Views
    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private FriendsFragment mFriendsFragment;
    private ChatsFragment mChatsFragment;
    private RequestFragment mRequestFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        mToolbar = findViewById(R.id.main_page_toolbar);
        mViewPager = findViewById(R.id.main_tab_pager);
        mTabLayout = findViewById(R.id.main_tabs);
        mChatsFragment = new ChatsFragment();
        mFriendsFragment = new FriendsFragment();
        mRequestFragment = new RequestFragment();
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), 0);

        mTabLayout.setupWithViewPager(mViewPager);
        mSectionsPagerAdapter.addFragment(mRequestFragment, getString(R.string.requests));
        mSectionsPagerAdapter.addFragment(mChatsFragment, getString(R.string.chats));
        mSectionsPagerAdapter.addFragment(mFriendsFragment, getString(R.string.friends));
        mViewPager.setAdapter(mSectionsPagerAdapter);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(getString(R.string.app_name));
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Check if the current user is Logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            sendToStartActivity();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.main_log_out:
                mAuth.signOut();
                sendToStartActivity();
                return true;
            case R.id.main_settings_btn:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sendToStartActivity() {
        Intent startIntent = new Intent(this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }
}