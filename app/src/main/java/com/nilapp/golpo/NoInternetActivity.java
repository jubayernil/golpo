package com.nilapp.golpo;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class NoInternetActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_internet);
    }

    //method for onCreate to check internet connection
    private void checkInternetConnection() {
        //To check Internet Connection
        if (!AppInternetStatus.getInstance(getApplicationContext()).isOnline()) {
            /**
             * Internet is NOT available, Toast It!
             */
        } else {
            Bundle extras = getIntent().getExtras();
            Class nextActivityClass = (Class<Activity>)extras.getSerializable("class_name");

            Intent mNextClassIntent = new Intent(NoInternetActivity.this, nextActivityClass);
            startActivity(mNextClassIntent);
        }
    }

    public void noInternetOnClick(View view) {
        checkInternetConnection();
    }
}
