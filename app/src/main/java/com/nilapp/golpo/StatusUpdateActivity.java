package com.nilapp.golpo;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusUpdateActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ProgressDialog mProgressDialog;

    private TextInputLayout mStatusUpdateTextInputLayout;

    private FirebaseUser mFirebaseUser;
    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_update);

        mStatusUpdateTextInputLayout = findViewById(R.id.StatusUpdateTextInputLayout);

        String status = getIntent().getStringExtra("status");
        mStatusUpdateTextInputLayout.getEditText().setText(status);

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = mFirebaseUser.getUid().toString();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);

        mToolbar = findViewById(R.id.StatusUpdateToolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Status Update");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void updateStatusOnClick(View view) {


        mProgressDialog = new ProgressDialog(StatusUpdateActivity.this);
        mProgressDialog.setTitle("Status Updating");
        mProgressDialog.setMessage("Your new status in updating. Please wait...");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        String status = mStatusUpdateTextInputLayout.getEditText().getText().toString();

        mUserDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mProgressDialog.dismiss();
                } else {
                    Toast.makeText(getApplicationContext(), "Status is not updating due to some reason.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
