package com.ngonyoku.maneno;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";
    public static final int STATUS_REQUEST_CODE = 1;
    public static final String STATUS_DATA = "com.ngonyoku.maneno.STATUS_DATA";
    private static final int GALLERY_PICK = 2;
    //Firebase
    private DatabaseReference mUserDatabaseRef;
    private FirebaseUser mCurrentUser;
    private StorageReference mProfileImageRef, mStorageReference;

    //Views
    private CircleImageView mDisplayImage;
    private TextView mDisplayName, mStatus;
    private Button mChangeImageBtn, mChangeStatusBtn;
    private ProgressBar mProgressBar;

    private String mStatusData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mDisplayImage = findViewById(R.id.settings_image);
        mDisplayName = findViewById(R.id.settings_display_name);
        mStatus = findViewById(R.id.settings_status);
        mChangeImageBtn = findViewById(R.id.settings_change_image_btn);
        mChangeStatusBtn = findViewById(R.id.settings_change_status_btn);
        mProgressBar = findViewById(R.id.settings_progressBar);

        mProfileImageRef = FirebaseStorage.getInstance().getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert mCurrentUser != null;
        String current_uid = mCurrentUser.getUid();
        mUserDatabaseRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(getString(R.string.db_node_users))
                .child(current_uid)
        ;

        mChangeImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        Intent.createChooser(
                                new Intent()
                                        .setType("image/*")
                                        .setAction(Intent.ACTION_GET_CONTENT),
                                "SELECT IMAGE"
                        ),
                        GALLERY_PICK
                );
//                CropImage.activity()
//                        .setGuidelines(CropImageView.Guidelines.ON)
//                        .start(SettingsActivity.this)
                ;
            }
        });

        mChangeStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(SettingsActivity.this, StatusActivity.class)
                                .putExtra(STATUS_DATA, mStatusData),
                        STATUS_REQUEST_CODE)
                ;
            }
        });

        /*We retrieve data(User info) from the database and display them*/
        mUserDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                /*The data is retrieved and saved in the following strings*/
                String name = snapshot.child(getString(R.string.db_field_name)).getValue().toString();
                String image = snapshot.child(getString(R.string.db_field_image)).getValue().toString();
                String status = snapshot.child(getString(R.string.db_field_status)).getValue().toString();
                String thumb_image = snapshot.child(getString(R.string.db_field_thumb_Image)).getValue().toString();

                mStorageReference = FirebaseStorage
                        .getInstance()
                        .getReference()
                        .child("profile_images/" + mCurrentUser.getUid() + ".jpg")
                ;
                try {
                    final File localFile = File.createTempFile("profile_pic", "jpg");
                    mStorageReference.getFile(localFile)
                            .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    Bitmap imageBitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                    ((ImageView) mDisplayImage).setImageBitmap(imageBitmap);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(SettingsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            })
                    ;

                } catch (IOException e) {
                    e.printStackTrace();
                }
//                Picasso.get()
//                        .load(image.trim())
//                        .into(mDisplayImage)
//                ; /*Display the Profile Image*/
                mDisplayName.setText(name); /*Display the Name*/
                mStatus.setText(status); /*Display the status*/

                mStatusData = status;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            assert data != null;
            Uri imageUri = data.getData();
            CropImage
                    .activity(imageUri)
                    .setAspectRatio(1, 1)
                    .start(this)
            ;
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                assert result != null;
                showProgress(true);
                Uri resultUri = result.getUri(); /*Returns the Cropped Image*/
                StorageReference filepath = mProfileImageRef.child("profile_images").child(mCurrentUser.getUid() + ".jpg");
                filepath
                        .putFile(resultUri)
                        .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    /*We store the image link in the realtime database*/
                                    String downloadUrl = task.getResult().toString();/*Get Url of image*/
                                    Log.d(TAG, "onComplete: imageUrl => " + downloadUrl);
                                    mUserDatabaseRef
                                            .child(getString(R.string.db_field_image))
                                            .setValue(downloadUrl) /*Store the image url to the database in the "image" field*/
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        showProgress(false);
                                                        Toast.makeText(SettingsActivity.this, "Profile image updated", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            })
                                    ;
                                } else {
                                    Toast.makeText(SettingsActivity.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                ;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void showProgress(boolean show) {
        if (show) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.GONE);
        }
    }
}