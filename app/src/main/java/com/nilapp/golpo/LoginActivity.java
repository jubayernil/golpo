package com.nilapp.golpo;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    final Context context = this;

    private Toolbar mToolbar;

    private TextInputLayout
                            mLoginEmailTextLayout,
                            mLoginPasswordTextLayout;
    private ProgressDialog mProgressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mProgressDialog = new ProgressDialog(this);

        mLoginEmailTextLayout = findViewById(R.id.LoginEmailTextLayout);
        mLoginPasswordTextLayout = findViewById(R.id.LoginPasswordTextLayout);

        mToolbar = findViewById(R.id.LoginToolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        checkInternetConnection();
    }

    //method for onCreate to check internet connection
    private void checkInternetConnection() {
        //To check Internet Connection
        if (!AppInternetStatus.getInstance(getApplicationContext()).isOnline()) {
            /**
             * Internet is NOT available, Toast It!
             */
            //showNoInternetDialog();

            Intent mNoInternetIntent = new Intent(LoginActivity.this, NoInternetActivity.class);
            mNoInternetIntent.putExtra("class_name", MainActivity.class);
            startActivity(mNoInternetIntent);
            finish();
        }
    }


    public void loginOnClick(View view) {
        boolean isValid = true;

        String
                email = mLoginEmailTextLayout.getEditText().getText().toString(),
                password = mLoginPasswordTextLayout.getEditText().getText().toString();

        if (email.isEmpty()) {
            mLoginEmailTextLayout.setError(getString(R.string.email_is_empty));
            isValid = false;
        } else {
            mLoginEmailTextLayout.setErrorEnabled(false);
        }

        if (password.trim().length() < 5 || password.trim().length() > 8) {
            mLoginPasswordTextLayout.setError(getString(R.string.password_is_empty));
            isValid = false;
        } else {
            mLoginPasswordTextLayout.setErrorEnabled(false);
        }

        //To check Internet Connection
        if (AppInternetStatus.getInstance(getApplicationContext()).isOnline()) {
            /**
             * Internet is available, Toast It!
             */
            if (isValid) {
                mProgressDialog.setTitle("Login To Your Account");
                mProgressDialog.setMessage("Login process is sunning. Please wait...");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                loginUser(email, password);
            }
        } else {
            /**
             * Internet is NOT available, Toast It!
             */
            //showNoInternetDialog();
            checkInternetConnection();

            //Toast.makeText(getApplicationContext(), "Ooops! No WiFi/Mobile Networks Connected!", Toast.LENGTH_SHORT).show();
        }

    }

    //Dialog for Showing No Internet Connection
    private void showNoInternetDialog() {

        // custom no_internet_dialog
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.no_internet_dialog);

        // set the custom no_internet_dialog components - text, image and button
        TextView text = (TextView) dialog.findViewById(R.id.text);

        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
        // if button is clicked, close the custom no_internet_dialog
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d("login_success", "signInWithEmail:success");

                            mProgressDialog.dismiss();

                            //get current user id
                            String currentUserId = mAuth.getCurrentUser().getUid();

                            //Get devise token to store in users database in firebase
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            mUserDatabase.child(currentUserId).child("deviceToken").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {

                                        Intent mMainIntent = new Intent(LoginActivity.this, MainActivity.class);
                                        //TO finish all previous activity when start Main Activity
                                        mMainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mMainIntent);

                                    } else {
                                        Toast.makeText(LoginActivity.this, "Device token not found",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } else {
                            // If sign in fails, display a message to the user.
                            mProgressDialog.hide();

                            //Log.w("login_fail", "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, R.string.login_failed_message,
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }
}
