package com.ngonyoku.maneno;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
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
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";
    public static final int STATUS_REQUEST_CODE = 1;
    public static final String KEY_STATUS_DATA = "com.ngonyoku.maneno.STATUS_DATA";
    private static final int GALLERY_PICK = 2;

    //Firebase
    private DatabaseReference mUserDatabaseRef;
    private FirebaseUser mCurrentUser;
    private StorageReference mProfileImageRef;

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
        mUserDatabaseRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.db_node_users)).child(current_uid);
        mUserDatabaseRef.keepSynced(true); /*Enable Offline capabilities in this Node*/

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
                                .putExtra(KEY_STATUS_DATA, mStatusData),
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
                final String image = snapshot.child(getString(R.string.db_field_image)).getValue().toString();
                String status = snapshot.child(getString(R.string.db_field_status)).getValue().toString();
                String thumb_image = snapshot.child(getString(R.string.db_field_thumb_Image)).getValue().toString();

                final String imageUrl = (image.isEmpty()) ? thumb_image : image;
                if (!image.isEmpty()) {
                    Picasso/*Display the Profile Image*/
                            .get()
                            .load(imageUrl)
                            .networkPolicy(NetworkPolicy.OFFLINE)/*Retrieve Image from Offline*/
                            .into(mDisplayImage, new Callback() {
                                @Override
                                public void onSuccess() {
                                    /*If image was retrieved successfully from offline, then ...*/
                                }

                                @Override
                                public void onError(Exception e) {
                                    /*... else, We should retrieve the Image online.*/
                                    Picasso.get().load(imageUrl).into(mDisplayImage);
                                }
                            })
                    ;
                }
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
                    .setMinCropWindowSize(500, 500)
                    .start(this)
            ;
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                assert result != null;
                showProgress(true);
                Uri resultUri = result.getUri(); /*Returns the Cropped Image*/

                File thumb_file = new File(Objects.requireNonNull(resultUri.getPath()));/*Create a File*/
                try {
                    /*-------Compress our Image to Bitmap-------*/
                    Bitmap thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_file);

                    /*------We push the Bitmap file to Firebase-----*/
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] thumb_byte = baos.toByteArray();

                    uploadProfilePic(resultUri, thumb_byte);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                assert result != null;
                Exception error = result.getError();
                Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadProfilePic(Uri imageUri, byte[] thumb_byte) {
        final StorageReference filepath = mProfileImageRef.child(getString(R.string.storage_profile_images)).child(mCurrentUser.getUid() + ".jpg");
        final StorageReference thumb_filepath = mProfileImageRef.child(getString(R.string.storage_profile_images)).child(getString(R.string.storage_thumbs)).child(mCurrentUser.getUid() + ".jpg");

        /*-------Upload Thumbnail------------*/
        thumb_filepath.putBytes(thumb_byte)
                .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        return thumb_filepath.getDownloadUrl();
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            assert downloadUri != null;
                            String thumbnailUrl = downloadUri.toString();
                            mUserDatabaseRef
                                    .child(getString(R.string.db_field_thumb_Image))
                                    .setValue(thumbnailUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (!task.isSuccessful()) {
                                                Toast.makeText(SettingsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    })
                            ;
                        } else {
                            Toast.makeText(SettingsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        /*-------Upload Profile Pic----------*/
        filepath.putFile(imageUri)
                .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        return filepath.getDownloadUrl();
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            assert downloadUri != null;
                            String imageUrl = downloadUri.toString(); /*Url of the Image*/
                            mUserDatabaseRef
                                    .child(getString(R.string.db_field_image))
                                    .setValue(imageUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(SettingsActivity.this, "Profile photo updated", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(SettingsActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                            showProgress(false);
                                        }
                                    })
                            ;
                        } else {
                            Toast.makeText(SettingsActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
        ;
    }

    private void showProgress(boolean show) {
        if (show) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.GONE);
        }
    }
}