package com.example.streamfun;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.core.SyncTree;
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

public class AddVideo extends AppCompatActivity {

    VideoView videoView;
    Button chooseButton, uploadButton;
    MediaController mediaController;
    Uri videoUri;

    StorageReference storageReference;
    DatabaseReference databaseReference;
    ProgressBar progressBar;
    EditText videoName;
    Member member;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_video);

        getSupportActionBar().hide();
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorAccent, this.getTheme()));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorAccent));
        }

        videoView = findViewById(R.id.video_view);
        chooseButton = findViewById(R.id.choose_video);
        uploadButton = findViewById(R.id.upload_button);
        mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        videoView.start();

        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference("myVideos");
        videoName = findViewById(R.id.video_name);
        progressBar = findViewById(R.id.progressbar);
        member = new Member();

    }

    public void Choosevideo(View view) {


        Dexter.withContext(getApplicationContext())
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        Intent intent = new Intent();
                        intent.setType("video/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(intent, 5);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 5 && resultCode == RESULT_OK && data != null && data.getData() != null){
            videoUri = data.getData();
            videoView.setVideoURI(videoUri);
        }
    }

    public String getExtension(Uri uri){
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(getContentResolver().getType(uri));
    }
    public void UploadVideo(View view) {
        String name = videoName.getText().toString();
        if(videoUri != null && !name.equals("")) {
            progressBar.setVisibility(View.VISIBLE);
//            final ProgressDialog progressDialog = new ProgressDialog(this);
//            progressDialog.setTitle("File Uploader");
//            progressDialog.show();

            final StorageReference reference = storageReference.child("myVideos/" + System.currentTimeMillis() + "." + getExtension(videoUri));
            Task<Uri> urlTask = reference.putFile(videoUri)
                    .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw  task.getException();
                            }
                            return reference.getDownloadUrl();
                        }
                    })
                        .addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {

                                if(task.isSuccessful()){
                                    Uri uri = task.getResult();
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(getApplicationContext(),"file uploaded",Toast.LENGTH_SHORT).show();
                                    member.setMyVideoName(name);
                                    member.setMyVideoUrl(uri.toString());
                                    databaseReference.child(databaseReference.push().getKey()).setValue(member);
                                }else{
                                    Toast.makeText(getApplicationContext(),"uploading failed, try again",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

//                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                        @Override
//                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                                @Override
//                                public void onSuccess(Uri uri) {
//                                    member.setMyVideoName(name);
//                                    member.setMyVideoUrl(uri.toString());
//                                    String idx = databaseReference.push().getKey();
//                                    databaseReference.child(idx).setValue(member);
//                                }
//                            });
//                            progressBar.setVisibility(View.INVISIBLE);
//                            Toast.makeText(getApplicationContext(), "Uploaded", Toast.LENGTH_SHORT).show();
//                            startActivity(new Intent(getApplicationContext(), HomePage.class));
//                        }
//                    })
//                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//                        @Override
//                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
//
//                        }
//                    });
        }else{
            if(name.equals("")){
                videoName.setError("write video name");
            }else {
                Toast.makeText(getApplicationContext(),"select video",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
