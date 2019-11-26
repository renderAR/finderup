package com.example.mypc.specialproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Activity_EditReport extends AppCompatActivity implements Dialog_Resolve.DialogListener {
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private String reportId;
    private String reportTitle;
    private String reportContent;
    private String reportLocation;
    private String reportOther;
    private String reportImageUrl;
    private boolean reportIfFollower;
    private int statusID;
    private int reportStatus;
    private boolean reportResolved;
    private ArrayList<String> reportFollowers;

    private EditText inputTitle;
    private EditText inputContent;
    private EditText inputLocation;
    private EditText inputOther;
    private RadioGroup inputStatus;
    private Button buttonEditreport;
    private Button buttonImage;
    private Button buttonCamera;
    private Button buttonUnload;
    private ProgressBar progress;
    private StorageReference imageRef;
    private Uri imageUri;
    private static final int IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private int PERMISSION_CODE = 3;
    private File imageFile;
    boolean error = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editreport);

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Update Report");

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        reportId = getIntent().getExtras().getString("reportId");
        reportTitle = getIntent().getExtras().getString("reportTitle");
        reportContent = getIntent().getExtras().getString("reportContent");
        reportLocation = getIntent().getExtras().getString("reportLocation");
        reportOther = getIntent().getExtras().getString("reportOther");
        reportImageUrl = getIntent().getExtras().getString("reportImageUrl");
        reportStatus = getIntent().getExtras().getInt("reportStatus");
        reportIfFollower = getIntent().getExtras().getBoolean("reportIfFollower");
        reportResolved = getIntent().getExtras().getBoolean("reportResolved");
        reportFollowers = getIntent().getStringArrayListExtra("reportFollowers");

        inputTitle = findViewById(R.id.editreport_title);
        inputTitle.setText(reportTitle, TextView.BufferType.EDITABLE);
        inputContent = findViewById(R.id.editreport_content);
        inputContent.setText(reportContent, TextView.BufferType.EDITABLE);
        inputLocation = findViewById(R.id.editreport_location);
        inputLocation.setText(reportLocation, TextView.BufferType.EDITABLE);
        inputOther = findViewById(R.id.editreport_other);
        inputOther.setText(reportOther, TextView.BufferType.EDITABLE);
        progress = findViewById(R.id.editreport_progress);

        inputStatus = findViewById(R.id.editreport_status);
        if(reportStatus == 1){
            inputStatus.check(R.id.editreport_statuslost);
        }else{
            inputStatus.check(R.id.editreport_statusfound);
        }

        imageRef = null;
        if(reportImageUrl != null) imageRef = storage.getReferenceFromUrl(reportImageUrl);

        buttonImage = findViewById(R.id.editreport_imagebutton);
        buttonCamera = findViewById(R.id.editreport_camerabutton);
        buttonUnload = findViewById(R.id.editreport_unloadbutton);
        if(reportImageUrl != null){
            buttonUnload.setVisibility(View.VISIBLE);
        }else{
            buttonImage.setVisibility(View.VISIBLE);
            buttonCamera.setVisibility(View.VISIBLE);
        }
        buttonImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectCamera();
            }
        });
        buttonUnload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unloadImage();
            }
        });

        buttonEditreport = findViewById(R.id.editreport_editbutton);
        buttonEditreport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editReport();
            }
        });
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private void selectCamera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(Build.VERSION.SDK_INT < 23){
            startActivityForResult(intent, CAMERA_REQUEST);
        }else{
            if(ContextCompat.checkSelfPermission(Activity_EditReport.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(Activity_EditReport.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(Activity_EditReport.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                imageFile = null;
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFilename = "img_" + timestamp + ".jpg";
                imageFile = new File(storageDir, imageFilename);

                Uri uri = FileProvider.getUriForFile(Activity_EditReport.this, getApplicationContext().getPackageName()+".provider", imageFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                startActivityForResult(intent, CAMERA_REQUEST);
            }else{
                ActivityCompat.requestPermissions(Activity_EditReport.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_CODE);
            }
        }
    }

    private void unloadImage(){
        reportImageUrl = null;
        imageUri = null;
        buttonUnload.setVisibility(View.GONE);
        buttonImage.setVisibility(View.VISIBLE);
        buttonCamera.setVisibility(View.VISIBLE);
        Toast.makeText(Activity_EditReport.this, "Image unloaded", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && (requestCode == IMAGE_REQUEST || requestCode == CAMERA_REQUEST)) {
            if(data.getData() != null){
                imageUri = data.getData();
            }else{
                imageUri = FileProvider.getUriForFile(Activity_EditReport.this, getApplicationContext().getPackageName()+".provider", imageFile);
            }
            buttonImage.setVisibility(View.GONE);
            buttonCamera.setVisibility(View.GONE);
            buttonUnload.setVisibility(View.VISIBLE);
            Toast.makeText(Activity_EditReport.this, "Image loaded", Toast.LENGTH_SHORT).show();
        }else if(resultCode != RESULT_OK){
            Toast.makeText(Activity_EditReport.this, "Cannot load image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSION_CODE) {
            if(grantResults.length > 0){
                for(int i = 0 ; i < permissions.length ; i++){
                    if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(Activity_EditReport.this, "Permission denied", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                imageFile = null;
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFilename = "img_" + timestamp + ".jpg";
                imageFile = new File(storageDir, imageFilename);

                Uri uri = FileProvider.getUriForFile(Activity_EditReport.this, getApplicationContext().getPackageName()+".provider", imageFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                startActivityForResult(intent, CAMERA_REQUEST);
            }
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    private void editReport(){
        reportTitle = inputTitle.getText().toString();
        reportContent = inputContent.getText().toString();
        reportLocation = inputLocation.getText().toString();
        reportOther = inputOther.getText().toString();
        statusID = inputStatus.getCheckedRadioButtonId();
        if(statusID == R.id.editreport_statuslost) reportStatus= 1;
        else reportStatus = 2;

        if (reportTitle.length() == 0) {
            Toast.makeText(Activity_EditReport.this, "Title is required", Toast.LENGTH_SHORT).show();
        } else if (statusID == -1) {
            Toast.makeText(Activity_EditReport.this, "Status is required", Toast.LENGTH_SHORT).show();
        } else if (reportContent.length() == 0) {
            Toast.makeText(Activity_EditReport.this, "Content is required", Toast.LENGTH_SHORT).show();
        }

        else{
            progress.setVisibility(View.VISIBLE);

            if(imageRef != null) imageRef.delete();
            // if no new image loaded
            if(imageUri == null){
                db.collection("reports").document(reportId).update(
                        "title", reportTitle,
                        "status", reportStatus,
                        "content", reportContent,
                        "location", reportLocation,
                        "other", reportOther,
                        "imageUrl", reportImageUrl,
                        "timestampUpdated", Timestamp.now()
                ).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if(reportIfFollower) FirebaseMessaging.getInstance().unsubscribeFromTopic(reportId);
                        notifyFollowers(user.getDisplayName() + " updated a report.");
                        progress.setVisibility(View.INVISIBLE);
                        Toast.makeText(Activity_EditReport.this, "Successfully edited report", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progress.setVisibility(View.INVISIBLE);
                        Toast.makeText(Activity_EditReport.this, "Error in editing report", Toast.LENGTH_SHORT).show();
                    }
                });

            // new image loaded
            }else{
                final StorageReference fileRef = storageRef.child("images/"+System.currentTimeMillis()+"."+getFileExtension(imageUri));
                fileRef.putFile(imageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if(!task.isSuccessful()) throw task.getException();
                        return fileRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            Uri downloadUri = task.getResult();
                            reportImageUrl = downloadUri.toString();

                            db.collection("reports").document(reportId).update(
                                    "title", reportTitle,
                                    "status", reportStatus,
                                    "content", reportContent,
                                    "location", reportLocation,
                                    "other", reportOther,
                                    "imageUrl", reportImageUrl,
                                    "timestampUpdated", Timestamp.now()
                            ).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    if(reportIfFollower) FirebaseMessaging.getInstance().unsubscribeFromTopic(reportId);
                                    notifyFollowers(user.getDisplayName() + " updated a report.");
                                    progress.setVisibility(View.INVISIBLE);
                                    Toast.makeText(Activity_EditReport.this, "Successfully edited report", Toast.LENGTH_SHORT).show();
                                    if(reportIfFollower) FirebaseMessaging.getInstance().subscribeToTopic(reportId);
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progress.setVisibility(View.INVISIBLE);
                                    Toast.makeText(Activity_EditReport.this, "Error in editing report", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }else {
                            progress.setVisibility(View.INVISIBLE);
                            Toast.makeText(Activity_EditReport.this, "Error in editing report", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }

    private void notifyFollowers(final String message) {
        Notification notificationObj = new Notification(message + "\n\nReport: " + reportTitle, Timestamp.now(), reportId, 1);
        WriteBatch batch = db.batch();
        if(reportFollowers != null) {
            for (String userId : reportFollowers) {
                if (userId.equals(user.getEmail().replace(".", ","))) continue;
                batch.set(db.collection("users").document(userId).collection("notifications").document("1" + reportId), notificationObj);
            }
            batch.commit();
        }

        String serverUrl = "https://fcm.googleapis.com/fcm/send";
        final String serverKey = "key=" + "AAAAUalGRAU:APA91bGRSvcdLbDgj0kXn-mf0bW3N8gGA73Fj7hAHshyHW-ibWgYLyY8rXoHANgjOpaQePvDBANBYJsd4zJbUb8djuTCBhsOp7yVLsHM9ap9_3yTql0qzrMgBCQ6ehLRnS89ifCKh0cf";
        final String contentType = "application/json";
        String TAG = "NOTIFICATION TAG";

        JSONObject notification = new JSONObject();
        JSONObject notificationBody = new JSONObject();
        try {
            notificationBody.put("title", "UPLB Lost and Found");
            notificationBody.put("message", message);

            notification.put("to", "/topics/" + reportId);
            notification.put("data", notificationBody);
        } catch (JSONException e) {
            Log.e(TAG, "onCreate: " + e.getMessage());
        }


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, serverUrl, notification, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                FirebaseMessaging.getInstance().subscribeToTopic(reportId).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("NOTIFICATION TAG", "refollowed");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("NOTIFICATION TAG", "refollowed error");
                    }
                });
                Log.i("NOTIFICATION TAG", "onResponse: " + response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(reportIfFollower) FirebaseMessaging.getInstance().subscribeToTopic(reportId).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("NOTIFICATION TAG", "refollowed");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("NOTIFICATION TAG", "refollowed error");
                    }
                });
                Log.i("NOTIFICATION TAG", "onErrorResponse: Didn't work");
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", serverKey);
                params.put("Content-Type", contentType);
                return params;
            }
        };

        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.editreport_menu, menu);
        if(!reportResolved) menu.findItem(R.id.editreport_resolve).setIcon(R.drawable.ic_resolved);
        else menu.findItem(R.id.editreport_resolve).setIcon(R.drawable.ic_unresolve);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem resolveButton = menu.findItem(R.id.editreport_resolve);
        if(!reportResolved) resolveButton.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_resolved));
        else resolveButton.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_unresolve));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            case R.id.editreport_resolve:
                if(!reportResolved) openResolveDialog();
                else unresolve();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void openResolveDialog(){
        Dialog_Resolve resolveDialog = new Dialog_Resolve();
        resolveDialog.show(getSupportFragmentManager(),"resolve dialog");
    }

    @Override
    public void onResolveClicked(User userLink) {
        progress.setVisibility(View.VISIBLE);
        List<String> userData = null;
        if(userLink.getId() != null){
            userData = new ArrayList<>();
            userData.add(userLink.getId());
            userData.add(userLink.getuID());
            userData.add(userLink.getName());
            userData.add(userLink.getEmail());
            userData.add(userLink.getPhotoUrl());
            userData.add(userLink.getToken());
        }
        db.collection("reports").document(reportId).update(
                "resolved", true,
                "linkedUser", userData,
                "timestampUpdated", Timestamp.now()
        ).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                reportResolved = true;
                progress.setVisibility(View.INVISIBLE);
                invalidateOptionsMenu();
                Toast.makeText(Activity_EditReport.this, "Report resolved", Toast.LENGTH_SHORT).show();
                notifyFollowers(user.getDisplayName() + " resolved a report.");
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progress.setVisibility(View.INVISIBLE);
                Toast.makeText(Activity_EditReport.this, "Failed to resolve report", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unresolve() {
        progress.setVisibility(View.VISIBLE);
        db.collection("reports").document(reportId).update(
                "resolved", false,
                "linkedUser", null,
                "timestampUpdated", Timestamp.now()
        ).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                reportResolved = false;
                progress.setVisibility(View.INVISIBLE);
                invalidateOptionsMenu();
                Toast.makeText(Activity_EditReport.this, "Report unresolved", Toast.LENGTH_SHORT).show();
                notifyFollowers(user.getDisplayName() + " edited a report.");
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progress.setVisibility(View.INVISIBLE);
                Toast.makeText(Activity_EditReport.this, "Failed to unresolve report", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
