package com.example.mypc.specialproject;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import static android.app.Activity.RESULT_OK;

public class Fragment_AddReport extends Fragment {
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private EditText inputTitle;
    private EditText inputContent;
    private EditText inputLocation;
    private EditText inputOther;
    private RadioGroup radiogroupStatus;
    private Button buttonImage;
    private Button buttonCamera;
    private Button buttonUnload;
    private Uri imageUri;
    private static final int IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private int PERMISSION_CODE = 3;
    private File imageFile;
    private Button buttonAddreport;
    private ProgressBar progress;

    private Report reportToAdd;
    private String title;
    private String content;
    private String location;
    private String other;
    private int statusID;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().setTitle("Add a Report");

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        View view = inflater.inflate(R.layout.fragment_addreport, container, false);

        inputTitle = view.findViewById(R.id.addreport_title);
        inputContent = view.findViewById(R.id.addreport_content);
        inputLocation = view.findViewById(R.id.addreport_location);
        inputOther = view.findViewById(R.id.addreport_other);
        radiogroupStatus = view.findViewById(R.id.addreport_status);
        progress = view.findViewById(R.id.addreport_progress);

        buttonImage = view.findViewById(R.id.addreport_imagebutton);
        buttonImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
        buttonCamera = view.findViewById(R.id.addreport_camerabutton);
        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectCamera();
            }
        });
        buttonUnload = view.findViewById(R.id.addreport_unloadbutton);
        buttonUnload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unloadImage();
            }
        });


        buttonAddreport = view.findViewById(R.id.addreport_addbutton);
        buttonAddreport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addReport();
            }
        });
        return view;
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private void selectCamera(){
        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (Build.VERSION.SDK_INT < 23) {
                startActivityForResult(intent, CAMERA_REQUEST);
            } else {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    imageFile = null;
                    File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String imageFilename = "img_" + timestamp + ".jpg";
                    imageFile = new File(storageDir, imageFilename);

                    Uri uri = FileProvider.getUriForFile(getActivity(), getActivity().getApplicationContext().getPackageName() + ".provider", imageFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                    startActivityForResult(intent, CAMERA_REQUEST);
                } else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_CODE);
                }
            }
        }catch(Exception e){
            Toast.makeText(getActivity(), "Camera error", Toast.LENGTH_SHORT).show();
        }
    }

    private void unloadImage(){
        imageUri = null;
        buttonUnload.setVisibility(View.GONE);
        buttonImage.setVisibility(View.VISIBLE);
        buttonCamera.setVisibility(View.VISIBLE);
        Toast.makeText(getActivity(), "Image unloaded", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && (requestCode == IMAGE_REQUEST || requestCode == CAMERA_REQUEST)) {
            if(data != null){
                if(data.getData() != null){
                    imageUri = data.getData();
                }else{
                    imageUri = FileProvider.getUriForFile(getActivity(), getActivity().getApplicationContext().getPackageName()+".provider", imageFile);
                }
                buttonImage.setVisibility(View.GONE);
                buttonCamera.setVisibility(View.GONE);
                buttonUnload.setVisibility(View.VISIBLE);
                Toast.makeText(getActivity(), "Image loaded", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getActivity(), "Cannot load image", Toast.LENGTH_SHORT).show();
            }
        }else if(resultCode != RESULT_OK){
            Toast.makeText(getActivity(), "Cannot load image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSION_CODE) {
            if(grantResults.length > 0){
                for(int i = 0 ; i < permissions.length ; i++){
                    if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(getActivity(), "Permission denied", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                imageFile = null;
                File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFilename = "img_" + timestamp + ".jpg";
                imageFile = new File(storageDir, imageFilename);

                Uri uri = FileProvider.getUriForFile(getActivity(), getActivity().getApplicationContext().getPackageName()+".provider", imageFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                startActivityForResult(intent, CAMERA_REQUEST);
            }
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cr = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    private void addReport() {
        title = inputTitle.getText().toString();
        content = inputContent.getText().toString();
        location = inputLocation.getText().toString();
        other = inputOther.getText().toString();
        statusID = radiogroupStatus.getCheckedRadioButtonId();

        if (title.length() == 0) {
            Toast.makeText(getActivity(), "Item is required", Toast.LENGTH_SHORT).show();
        } else if (statusID == -1) {
            Toast.makeText(getActivity(), "Status is required", Toast.LENGTH_SHORT).show();
        } else if (content.length() == 0) {
            Toast.makeText(getActivity(), "Content is required", Toast.LENGTH_SHORT).show();
        }

        else {
            progress.setVisibility(View.VISIBLE);
            if (imageUri != null) {
                final StorageReference fileRef = storageRef.child("images/"+System.currentTimeMillis()+"."+getFileExtension(imageUri));
                fileRef.putFile(imageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return fileRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();

                            List<String> followers = new ArrayList<String>();
                            followers.add(user.getEmail().replace(".",","));

                            reportToAdd = new Report(title, content, location, other, (statusID == R.id.addreport_statuslost ? 1 : 2), downloadUri.toString(), user.getEmail().replace(".",","), user.getDisplayName(), user.getEmail(), user.getPhotoUrl().toString(), followers);
                            db.collection("reports").add(reportToAdd)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            String reportId = documentReference.getId();
                                            FirebaseMessaging.getInstance().subscribeToTopic(reportId);

                                            progress.setVisibility(View.INVISIBLE);
                                            if(getActivity()!=null) Toast.makeText(getActivity(), "Successfully added report", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progress.setVisibility(View.INVISIBLE);
                                            if(getActivity()!=null) Toast.makeText(getActivity(), "Error in adding report", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            progress.setVisibility(View.INVISIBLE);
                            Toast.makeText(getActivity(), "Error in adding report" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                List<String> followers = new ArrayList<String>();
                followers.add(user.getEmail().replace(".",","));

                reportToAdd = new Report(title, content, location, other, (statusID == R.id.addreport_statuslost ? 1 : 2), user.getEmail().replace(".",","), user.getDisplayName(), user.getEmail(), user.getPhotoUrl().toString(), followers);
                db.collection("reports").add(reportToAdd)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                String reportId = documentReference.getId();
                                FirebaseMessaging.getInstance().subscribeToTopic(reportId);

                                progress.setVisibility(View.INVISIBLE);
                                if(getActivity()!=null) Toast.makeText(getActivity(), "Successfully added report", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progress.setVisibility(View.INVISIBLE);
                                if(getActivity()!=null) Toast.makeText(getActivity(), "Error in adding report", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }
}
