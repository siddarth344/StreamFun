package com.example.streamfun;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseIndexArray;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class UpdateInfo extends AppCompatActivity {

    ImageView userImage;
    EditText userName;
    Button updateProfile;
    DatabaseReference databaseReference;
    StorageReference storageReference;

    Uri uri;
    Bitmap bitmap;
    String usertId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_info);

        userImage = findViewById(R.id.user_image);
        //userImage.setBackgroundTintList(getColorStateList(R.color.white));
        //userImage.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white, null)));
        //userImage.getColorsetBackgroundTintList(getColorStateList(R.color.newColor, null)));
        userName = findViewById(R.id.user_name);
        updateProfile = findViewById(R.id.update_profile);

        databaseReference = FirebaseDatabase.getInstance().getReference("userProfiles");
        uri = null;

        usertId = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
        storageReference = FirebaseStorage.getInstance().getReference();
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withContext(getApplicationContext()).withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                                Intent intent = new Intent();
                                intent.setType("image/*");
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(intent.createChooser(intent,"please select file"),101);
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                                permissionToken.continuePermissionRequest();
                            }
                        }).check();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 101 && resultCode == RESULT_OK && data != null && data.getData()!= null){
            uri = data.getData();
            try{
                InputStream inputStream = getContentResolver().openInputStream(uri);
                bitmap = BitmapFactory.decodeStream(inputStream);
                userImage.setImageBitmap(bitmap);

            }catch (Exception e){
                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void UpdateProfile(View view) {
        String name = userName.getText().toString();
        if(uri == null){
            final Map<String, Object> map = new HashMap<>();
            map.put("uname",name);
            databaseReference.child(usertId).updateChildren(map);
            return;
        }
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("File Uploader");
        progressDialog.show();
        final StorageReference uploader = storageReference.child("userProfiles/"+"img"+System.currentTimeMillis());
        uploader.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                uploader.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        final Map<String, Object> map = new HashMap<>();
                        map.put("uimage",uri.toString());
                        map.put("uname",name);
                        databaseReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    databaseReference.child(usertId).updateChildren(map);
                                }else{
                                    databaseReference.child(usertId).setValue(map);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(),"Updated successfully", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        float percent = (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        progressDialog.setMessage("uploading...."+(int)percent+"%");
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String userid = firebaseUser.getUid().toString();
        databaseReference.child(userid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    userName.setText(snapshot.child("uname").getValue().toString());
                    Glide.with(getApplicationContext()).load(snapshot.child("uimage").getValue().toString()).into(userImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
