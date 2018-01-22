package com.nilapp.golpo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegistrationActivity extends AppCompatActivity {

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    final Context context = this;

    private Button mRgOkButton;

    private TextInputLayout
            mDisplayNameTextLayout,
            mEmailTextLayout,
            mPasswordTextLayout;

    private Toolbar mToolbar;

    private ProgressDialog mProgressDialog;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Firebase
        mAuth = FirebaseAuth.getInstance();

        mDisplayNameTextLayout = findViewById(R.id.RegDisplayNameTextLayout);
        mEmailTextLayout = findViewById(R.id.RegEmailTextLayout);
        mPasswordTextLayout = findViewById(R.id.RegPasswordTextLayout);

        mToolbar = findViewById(R.id.RegToolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProgressDialog = new ProgressDialog(this);

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
            Intent mNoInternetIntent = new Intent(RegistrationActivity.this, NoInternetActivity.class);
            mNoInternetIntent.putExtra("class_name", MainActivity.class);
            startActivity(mNoInternetIntent);
            finish();
        }
    }

    public void createAccountOnClick(View view) {

        boolean isValid = true;

        String displayName = mDisplayNameTextLayout.getEditText().getText().toString();
        String email = mEmailTextLayout.getEditText().getText().toString();
        String password = mPasswordTextLayout.getEditText().getText().toString();

        if (displayName.isEmpty()) {
            mDisplayNameTextLayout.setError(getString(R.string.display_name_is_empty));
            isValid = false;
        } else {
            mDisplayNameTextLayout.setErrorEnabled(false);
        }

        if (email.isEmpty()) {
            mEmailTextLayout.setError(getString(R.string.email_is_empty));
            isValid = false;
        } else {
            mEmailTextLayout.setErrorEnabled(false);
        }

        if (password.trim().length() < 5 || password.trim().length() > 8) {
            mPasswordTextLayout.setError(getString(R.string.password_is_empty));
            isValid = false;
        } else {
            mPasswordTextLayout.setErrorEnabled(false);
        }

        //To check Internet Connection
        if (AppInternetStatus.getInstance(getApplicationContext()).isOnline()) {
            /**
             * Internet is available, Toast It!
             */
            if (isValid) {
                mProgressDialog.setTitle("Account Is Creating");
                mProgressDialog.setMessage("Your account for Golpo is creating. Please wait...");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                registerUser(displayName, email, password);
            }
        } else {
            /**
             * Internet is NOT available, Toast It!
             */
            //call buildDialog method using this context
            //getApplicationContext() is not working here
            //for fragment we should pass getActivity() as context to builder instead of getContext()
            //buildDialog(this).show();

            //showNoInternetDialog();
            checkInternetConnection();
            //Toast.makeText(getApplicationContext(), "Ooops! No WiFi/Mobile Networks Connected!", Toast.LENGTH_SHORT).show();
        }
    }

    private void registerUser(final String displayName, String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d("done", "createUserWithEmail:success");

                            //Get the current user ID
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            String currentUserId = currentUser.getUid();
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            //Create database reference and create child. Here Under project Users is child and under Users userId is child
                            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);

                            //Save data into database using HashMap

                            HashMap<String, String > userHashMap = new HashMap<>();
                            userHashMap.put("deviceToken", deviceToken);
                            userHashMap.put("name", displayName);
                            userHashMap.put("status", "Hi there... I am using Golpo");
                            userHashMap.put("image", "default");
                            userHashMap.put("thumb_image", "default");

                            //Set the HashMap's value into database and add task complete listener
                            mUserDatabase.setValue(userHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        //after task complete dismiss the dialog and go to main activity
                                        mProgressDialog.dismiss();
                                        Intent mMainIntent = new Intent(RegistrationActivity.this, MainActivity.class);
                                        //TO finish all previous activity when start Main Activity
                                        mMainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mMainIntent);
                                    }
                                }
                            });

                        } else {
                            // If sign in fails, display a message to the user.
                            mProgressDialog.hide();
                            Log.w("undone", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegistrationActivity.this, "Unable to create account. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //To create a alert no_internet_dialog using this code
    public AlertDialog.Builder buildDialog(Context context) {

        //Build Alert Dialog
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("No Internet Connection");
        builder.setMessage("You need to have Mobile Data or wifi to access this. Press ok to Exit");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        return builder;
    }

    //Dialog for Showing No Internet Connection
    private void showNoInternetDialog() {

        // custom no_internet_dialog
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.no_internet_dialog);

        // set the custom no_internet_dialog components - text, image and button
        //TextView text = (TextView) dialog.findViewById(R.id.text);

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
}
