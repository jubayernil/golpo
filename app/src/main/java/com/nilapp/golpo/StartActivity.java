package com.nilapp.golpo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartActivity extends AppCompatActivity {

    private Button mStartRegistrationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mStartRegistrationButton = findViewById(R.id.StartRegistrationButton);

    }

    public void createAccountOnClick(View view) {
        Intent mRegistrationIntent = new Intent(StartActivity.this, RegistrationActivity.class);
        startActivity(mRegistrationIntent);
    }

    public void startLoginOnClick(View view) {
        Intent mLoginIntent = new Intent(StartActivity.this, LoginActivity.class);
        startActivity(mLoginIntent);
    }
}
