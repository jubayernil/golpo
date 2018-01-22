package com.nilapp.golpo;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsersActivity extends AppCompatActivity {

    private static Context context;

    private DatabaseReference mUserDatabase;

    private RecyclerView mAllUserListRecyclerView;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        AllUsersActivity.context = getApplicationContext();

        mAllUserListRecyclerView = findViewById(R.id.AllUserListRecyclerView);
        mAllUserListRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mToolbar = findViewById(R.id.AllUsersToolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<AllUsers, AllUsersViewHolder>
                firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<AllUsers, AllUsersViewHolder>
                (AllUsers.class, R.layout.layout_single_user_row_cardview, AllUsersViewHolder.class, mUserDatabase) {
            @Override
            protected void populateViewHolder(AllUsersViewHolder allUsersViewHolder, AllUsers allUsers, int position) {
                allUsersViewHolder.setName(allUsers.getName());
                allUsersViewHolder.setStatus(allUsers.getStatus());
                allUsersViewHolder.setThumbImage(allUsers.getThumb_image());

                //to get the user key from list
                final String userId = getRef(position).getKey();

                allUsersViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent mUserProfileIntent = new Intent(AllUsersActivity.this, UserProfileActivity.class);
                        mUserProfileIntent.putExtra("userId", userId);
                        startActivity(mUserProfileIntent);
                    }
                });
            }
        };

        mAllUserListRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    public static class AllUsersViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private TextView singleUserNameTextView;
        private TextView singleUserStatusTextView;
        private CircleImageView singleUserCircleImageView;

        public AllUsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            /*singleUserNameTextView = mView.findViewById(R.id.SingleUserNameTextView);
            singleUserStatusTextView = mView.findViewById(R.id.SingleUserStatusTextView);
            singleUserCircleImageView = mView.findViewById(R.id.SingleUserCircleImageView);*/
        }

        public void setName(String name) {
            singleUserNameTextView = mView.findViewById(R.id.SingleUserNameTextView);
            singleUserNameTextView.setText(name);
        }

        public void setStatus(String status) {
            singleUserStatusTextView = mView.findViewById(R.id.SingleUserStatusTextView);
            singleUserStatusTextView.setText(status);
        }

        public void setThumbImage(String thumb_image){
            singleUserCircleImageView = mView.findViewById(R.id.SingleUserCircleImageView);
            Picasso.with(getAppContext()).load(thumb_image).placeholder(R.drawable.default_profile_image).into(singleUserCircleImageView);

        }
    }

    public static Context getAppContext() {
        return AllUsersActivity.context;
    }
}
