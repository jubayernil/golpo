package com.nilapp.golpo;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private Toolbar mToolbar;

    private ViewPager mViewPager;
    private MainFragmentAdapter mSectionsPageAdapter;
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = findViewById(R.id.MainToolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Golpo");

        mViewPager = findViewById(R.id.MainViewPager);
        mSectionsPageAdapter = new MainFragmentAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPageAdapter);
        mTabLayout = findViewById(R.id.MainTabLayout);
        mTabLayout.setupWithViewPager(mViewPager);


        mAuth = FirebaseAuth.getInstance();

        //checkInternetConnection();
    }

    //method for onCreate to check internet connection
    private void checkInternetConnection() {
        //To check Internet Connection
        if (!AppInternetStatus.getInstance(getApplicationContext()).isOnline()) {
            /**
             * Internet is NOT available, Toast It!
             */
            Intent mNoInternetIntent = new Intent(MainActivity.this, NoInternetActivity.class);
            mNoInternetIntent.putExtra("class_name", MainActivity.class);
            startActivity(mNoInternetIntent);
            finish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Intent mStartIntent = new Intent(MainActivity.this, StartActivity.class);
            startActivity(mStartIntent);
            finish();
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.activity_main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.mainMenuLogout) {
            FirebaseAuth.getInstance().signOut();

            //to update the activity when sign out for this activity
            recreate();
        }

        if (item.getItemId() == R.id.mainMenuSettings) {
            Intent mSettingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(mSettingsIntent);
        }

        if (item.getItemId() == R.id.mainMenuAllUser) {
            Intent mAllUsersIntent = new Intent(MainActivity.this, AllUsersActivity.class);
            startActivity(mAllUsersIntent);
        }

        return true;
    }
}
