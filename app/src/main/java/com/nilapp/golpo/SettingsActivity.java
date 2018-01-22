package com.nilapp.golpo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {


    private FirebaseUser mFirebaseUser;
    private DatabaseReference mUserDatabase;
    private StorageReference mStorageReference;

    private TextView
            mSettingsDisplayNameTextView,
            mSettingsStatusTextView;
    private CircleImageView mSettingsCircleImageView;

    private ProgressDialog mProgressDialog;

    private static final int GALLERY_PIC = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mSettingsDisplayNameTextView = findViewById(R.id.SettingsDisplayNameTextView);
        mSettingsStatusTextView = findViewById(R.id.SettingsStatusTextView);
        mSettingsCircleImageView = findViewById(R.id.SettingsCircleImageView);


        //Get Firebase current user
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = mFirebaseUser.getUid();

        //Get the reference of user from users table
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);

        //text data in offline mode
        mUserDatabase.keepSynced(true);

        //Get the reference for cloud storage
        mStorageReference = FirebaseStorage.getInstance().getReference();

        //add value event listener to get ta data of user
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //when user data loaded from database

                //Toast.makeText(SettingsActivity.this, dataSnapshot.toString(), Toast.LENGTH_SHORT).show();

                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                mSettingsDisplayNameTextView.setText(name);
                mSettingsStatusTextView.setText(status);

                if (!image.equals("default")) {
                    Picasso
                            .with(SettingsActivity.this)
                            .load(image)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.default_profile_image)
                            .into(mSettingsCircleImageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso
                                            .with(SettingsActivity.this)
                                            .load(image)
                                            .placeholder(R.drawable.default_profile_image)
                                            .into(mSettingsCircleImageView);
                                }
                            });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void changeStatusOnClick(View view) {
        String status = mSettingsStatusTextView.getText().toString();

        Intent mStatusUpdateIntent = new Intent(SettingsActivity.this, StatusUpdateActivity.class);
        mStatusUpdateIntent.putExtra("status", status);
        startActivity(mStatusUpdateIntent);
    }

    public void changePictureOnClick(View view) {
        Intent galleryIntent = new Intent();

        //to load only image use intent type
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        //start activity for result to chose image. different way to chose image. here is document
        //to open select file for image
        startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PIC);

        // start picker to get image for cropping and then use the image in cropping activity
        /*CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);*/

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PIC && resultCode == RESULT_OK) {
            String imageUriText = data.getDataString();

            //Toast.makeText(SettingsActivity.this, imageUriText, Toast.LENGTH_LONG).show();

            Uri imageUri = data.getData();
            // start cropping activity for pre-acquired image saved on the device
            //can set crop view size into fixed with .setMinCropWindowSize()
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            try {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {

                    mProgressDialog = new ProgressDialog(SettingsActivity.this);
                    mProgressDialog.setTitle("Image Uploading");
                    mProgressDialog.setMessage("Your selected image is uploading. Please wait...");
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.show();

                    //get the image uri
                    Uri resultUri = result.getUri();

                    //get the image file path from uri
                    File thumbImageFilePath = new File(resultUri.getPath());

                    //convert image into bitmap for image from image file path
                    Bitmap imageBitmap = new Compressor(this)
                            .setMaxWidth(500)
                            .setMaxHeight(500)
                            .setQuality(100)
                            .compressToBitmap(thumbImageFilePath);

                    //convert image into bitmap for thumb image from image file path
                    Bitmap thumbImageBitmap = new Compressor(this)
                            .setMaxWidth(100)
                            .setMaxHeight(100)
                            .setQuality(100)
                            .compressToBitmap(thumbImageFilePath);

                    //firebase bitmap upload code for image
                    ByteArrayOutputStream baosImage = new ByteArrayOutputStream();
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baosImage);
                    final byte[] imageData = baosImage.toByteArray();

                    //firebase bitmap upload code for thumb image
                    ByteArrayOutputStream baosThumbImage = new ByteArrayOutputStream();
                    thumbImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baosThumbImage);
                    final byte[] thumbImageData = baosThumbImage.toByteArray();


                    //get the current user id from database
                    String currentUserId = mFirebaseUser.getUid();

                    //get the storage reference for image
                    StorageReference mImageFilePath = mStorageReference.child("profile_images").child(currentUserId + ".jpg");

                    //get the storage reference for thumb image
                    final StorageReference mThumbImageFilePath = mStorageReference.child("profile_images").child("thumbs").child(currentUserId + ".jpg");

                    //create upload task to store image bitmap in storage
                    //show the storage reference and pass bitmap data
                    UploadTask imageUploadTask = mImageFilePath.putBytes(imageData);
                    imageUploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            if (task.isSuccessful()) {
                                //Toast.makeText(SettingsActivity.this, "Image Upload Successful", Toast.LENGTH_SHORT).show();

                                //get the image url from storage to store the url in database
                                final String imageDownloadUrl = task.getResult().getDownloadUrl().toString();

                                //create upload task to store thumb bitmap in storage
                                //show the storage reference and pass bitmap data
                                UploadTask uploadTask = mThumbImageFilePath.putBytes(thumbImageData);
                                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumbTask) {

                                        //get the thumb image url from storage to save the image url into user database
                                        String thumbDownloadUrl = thumbTask.getResult().getDownloadUrl().toString();

                                        if (thumbTask.isSuccessful()) {

                                            //Set image url and thumb image url into a map so that both field of database can update in same task
                                            //NOTE: hashmap does not update data, it is saved data newly
                                            Map imageHashMap = new HashMap<>();
                                            imageHashMap.put("image", imageDownloadUrl);
                                            imageHashMap.put("thumb_image", thumbDownloadUrl);

                                            //update the value to database reference where database reference is pointed to current user
                                            //all database field should be available to update the database
                                            mUserDatabase.updateChildren(imageHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        mProgressDialog.dismiss();
                                                        Toast.makeText(SettingsActivity.this, "Image URL Save Successful", Toast.LENGTH_SHORT).show();
                                                    } else {

                                                    }
                                                }
                                            });

                                        } else {
                                            Toast.makeText(SettingsActivity.this, "ERROR: Thumb Image Upload Error", Toast.LENGTH_LONG).show();
                                            mProgressDialog.dismiss();
                                        }
                                    }
                                });


                            } else {

                                //Error Handling
                            /*try {
                                throw task.getException();
                            } catch(FirebaseAuthWeakPasswordException e) {
                                mTxtPassword.setError(getString(R.string.error_weak_password));
                                mTxtPassword.requestFocus();
                            } catch(FirebaseAuthInvalidCredentialsException e) {
                                mTxtEmail.setError(getString(R.string.error_invalid_email));
                                mTxtEmail.requestFocus();
                            } catch(FirebaseAuthUserCollisionException e) {
                                mTxtEmail.setError(getString(R.string.error_user_exists));
                                mTxtEmail.requestFocus();
                            } catch(Exception e) {
                                Log.e(TAG, e.getMessage());
                            }*/
                                Toast.makeText(SettingsActivity.this, "ERROR: Image Upload Error", Toast.LENGTH_LONG).show();
                                mProgressDialog.dismiss();
                            }

                        }
                    });

                   /* //to upload image in firebase
                    mImageFilePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        }
                    });*/

                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    //Random string generator
    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(10);
        char tempChar;
        for (int i = 0; i < randomLength; i++) {
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }
}
