package com.nilapp.golpo;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView mUserProfileUserImageView;

    private TextView
            mUserProfileUserNameTextView,
            mUserProfileCurrentUserStatusTextView,
            mUserProfileTotalFriendsTextView;

    private Button
            mUserProfileFriendRequestButton,
            mUserProfileDeclineFriendRequestButton;

    //User Database Reference
    private DatabaseReference mUsersDatabase;

    //Friend Request Database Reference
    private DatabaseReference mFriendRequestsDatabase;

    //Friends Database Reference
    private DatabaseReference mFriendsDatabase;

    //Firebase Notification Database Reference
    private DatabaseReference mNotificationDatabase;

    //Firebase User Reference
    private FirebaseUser mCurrentUser;

    private ProgressDialog mProgressDialog;

    //the status of a user about friendship
    private String mCurrentState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        final String userId = getIntent().getStringExtra("userId");

        mProgressDialog = new ProgressDialog(this);
        //mProgressDialog.setTitle("Data Loading");
        mProgressDialog.setMessage("Profile Is Loading. Please Wait...");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mUserProfileUserImageView = findViewById(R.id.UserProfileUserImageView);

        mUserProfileUserNameTextView = findViewById(R.id.UserProfileUserNameTextView);
        mUserProfileCurrentUserStatusTextView = findViewById(R.id.UserProfileCurrentUserStatusTextView);
        mUserProfileTotalFriendsTextView = findViewById(R.id.UserProfileTotalFriendsTextView);

        mUserProfileFriendRequestButton = findViewById(R.id.UserProfileFriendRequestButton);
        mUserProfileDeclineFriendRequestButton = findViewById(R.id.UserProfileDeclineFriendRequestButton);
        mUserProfileDeclineFriendRequestButton.setVisibility(View.INVISIBLE);

        //the friend status is not friend
        mCurrentState = "NotFriend";

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        mFriendRequestsDatabase = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String imageUrl = dataSnapshot.child("image").getValue().toString();

                mUserProfileUserNameTextView.setText(name);
                mUserProfileCurrentUserStatusTextView.setText(status);
                Picasso.with(getApplicationContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.default_profile_image)
                        .into(mUserProfileUserImageView);


                //---------------Friend List / Friend Request State---------------//


                mFriendRequestsDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        //when friend request is sent or received
                        if (dataSnapshot.hasChild(userId)) {
                            String requestStatus = dataSnapshot.child(userId).child("requestStatus").getValue().toString();

                            if (requestStatus.equals("received")) {
                                mCurrentState = "RequestReceived";
                                mUserProfileFriendRequestButton.setText("Accept Friend Request");
                                mUserProfileDeclineFriendRequestButton.setVisibility(View.VISIBLE);
                            } else if (requestStatus.equals("sent")) {
                                mCurrentState = "RequestSent";
                                mUserProfileFriendRequestButton.setText("Cancel Friend Request");
                            }

                            mProgressDialog.dismiss();
                        } else {
                            //When both users are friend
                            mFriendsDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.hasChild(userId)) {
                                        mCurrentState = "Friend";
                                        mUserProfileFriendRequestButton.setText("Unfriend");
                                    }
                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                    mProgressDialog.dismiss();
                                }
                            });


                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mUserProfileFriendRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //When Current User click Send Friend Request Button then it will be disable to user
                //so that he can not send request again
                mUserProfileFriendRequestButton.setEnabled(false);

                //----------Not Friend State----------//

                //check the user is friend or not
                if (mCurrentState.equals("NotFriend")) {
                    //if there is not friendship, the Current User able to send friend request to visiting user
                    //for this task, in FriendRequests table, there will be create belows field
                    //Current User, under this Visiting User, Under this Request Status, under this the value will be sent
                    //because Current User sends friend request to visiting user
                    mFriendRequestsDatabase
                            .child(mCurrentUser.getUid())
                            .child(userId).child("requestStatus")
                            .setValue("sent")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {
                                        //When Current user successfully sends friend request to visiting user
                                        //then in in FriendRequests table, there will be create belows field
                                        //Visiting User, under this Current User, Under this Request Status, under this the value will be received
                                        //because the Visiting User needs to see the friend request who sent to him
                                        mFriendRequestsDatabase
                                                .child(userId)
                                                .child(mCurrentUser.getUid())
                                                .child("requestStatus")
                                                .setValue("received")
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        HashMap<String, String> notificationHashMap = new HashMap<>();
                                                        notificationHashMap.put("notificationFrom", mCurrentUser.getUid());
                                                        notificationHashMap.put("notificationType", "received");

                                                        mNotificationDatabase
                                                                .child(userId)
                                                                .push()
                                                                .setValue(notificationHashMap)
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                        if (task.isSuccessful()) {

                                                                            //Send Friend Request button is enable to make change on it
                                                                            mCurrentState = "RequestSent";
                                                                            mUserProfileFriendRequestButton.setText("Cancel Friend Request");

                                                                        } else {
                                                                            Toast.makeText(UserProfileActivity.this, "Notification is not Successfully sent", Toast.LENGTH_SHORT).show();
                                                                        }

                                                                    }
                                                                });

                                                        //Toast.makeText(UserProfileActivity.this, "Friend Request Send Successful", Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                    } else {
                                        Toast.makeText(UserProfileActivity.this, "Failed To Send Friend Request", Toast.LENGTH_SHORT).show();
                                    }

                                    mUserProfileFriendRequestButton.setEnabled(true);
                                }
                            });
                }


                //----------Cancel Friend Request State----------//

                if (mCurrentState.equals("RequestSent")) {
                    mFriendRequestsDatabase
                            .child(mCurrentUser.getUid())
                            .child(userId)
                            .removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                        mFriendRequestsDatabase
                                                .child(userId)
                                                .child(mCurrentUser.getUid())
                                                .removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {

                                                            mUserProfileFriendRequestButton.setEnabled(true);
                                                            mCurrentState = "NotFriend";
                                                            mUserProfileFriendRequestButton.setText("Send Friend Request");

                                                        } else {
                                                            Toast.makeText(UserProfileActivity.this, "Failed To Delete Friend Request From Visiting User", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                    } else {
                                        Toast.makeText(UserProfileActivity.this, "Failed To Delete Friend Request From Current User", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }

                //----------Accept Friend Request State----------//

                if (mCurrentState.equals("RequestReceived")) {


                    //get the current date to store
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    //In Friends database store friend under current user and store date under visiting user
                    mFriendsDatabase
                            .child(mCurrentUser.getUid())
                            .child(userId)
                            .setValue(currentDate)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                        //Visiting user is successfully saved with current date under Current user
                                        //Now Visiting user will be in current user friend list
                                        //Here in Friends Database store friend under visiting user and store date under current user
                                        mFriendsDatabase
                                                .child(userId)
                                                .child(mCurrentUser.getUid())
                                                .setValue(currentDate)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {

                                                            mFriendRequestsDatabase
                                                                    .child(mCurrentUser.getUid())
                                                                    .child(userId)
                                                                    .removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {

                                                                                //Delete relevant data from FriendRequests table
                                                                                mFriendRequestsDatabase
                                                                                        .child(userId)
                                                                                        .child(mCurrentUser.getUid())
                                                                                        .removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()) {

                                                                                                    mUserProfileFriendRequestButton.setEnabled(true);
                                                                                                    mCurrentState = "Friend";
                                                                                                    mUserProfileFriendRequestButton.setText("Unfriend");
                                                                                                    mUserProfileDeclineFriendRequestButton.setVisibility(View.INVISIBLE);

                                                                                                } else {
                                                                                                    Toast.makeText(UserProfileActivity.this, "Failed To Delete Friend Request From Visiting User", Toast.LENGTH_SHORT).show();
                                                                                                }
                                                                                            }
                                                                                        });

                                                                            } else {
                                                                                Toast.makeText(UserProfileActivity.this, "Failed To Delete Friend Request From Current User", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    });


                                                        } else {
                                                            Toast.makeText(UserProfileActivity.this, "Failed To Accept Friend Request For Current User", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                    } else {
                                        Toast.makeText(UserProfileActivity.this, "Failed To Accept Friend Request For Visiting User", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }

                //----------Urfiend State----------//

                if (mCurrentState.equals("Friend")) {

                    mFriendsDatabase
                            .child(mCurrentUser.getUid())
                            .child(userId)
                            .removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {

                                        mFriendsDatabase
                                                .child(userId)
                                                .child(mCurrentUser.getUid())
                                                .removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful()) {

                                                            mUserProfileFriendRequestButton.setEnabled(true);
                                                            mCurrentState = "NotFriend";
                                                            mUserProfileFriendRequestButton.setText("Send Friend Request");

                                                        } else {
                                                            Toast.makeText(UserProfileActivity.this, "Failed To Unfriend Request the Current User", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                    } else {
                                        Toast.makeText(UserProfileActivity.this, "Failed To Unfriend Request the Visiting User", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });


        mUserProfileDeclineFriendRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (mCurrentState.equals("RequestReceived")) {
                    mFriendRequestsDatabase
                            .child(mCurrentUser.getUid())
                            .child(userId)
                            .removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                        mFriendRequestsDatabase
                                                .child(userId)
                                                .child(mCurrentUser.getUid())
                                                .removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {

                                                            mUserProfileFriendRequestButton.setEnabled(true);
                                                            mCurrentState = "NotFriend";
                                                            mUserProfileFriendRequestButton.setText("Send Friend Request");
                                                            mUserProfileDeclineFriendRequestButton.setVisibility(View.INVISIBLE);

                                                        } else {
                                                            Toast.makeText(UserProfileActivity.this, "Failed To Delete Friend Request From Visiting User", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                    } else {
                                        Toast.makeText(UserProfileActivity.this, "Failed To Delete Friend Request From Current User", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }

            }
        });

    }

}
