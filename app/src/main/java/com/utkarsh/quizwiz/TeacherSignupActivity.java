package com.utkarsh.quizwiz;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
//import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class TeacherSignupActivity extends AppCompatActivity {

    private static final int IMAGE_REQUEST_CODE = 1;

    private ImageView imageViewTeacher;
    private Uri imageUri;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_signup);

        EditText editTextTeacherID = findViewById(R.id.editTextTeacherID);
        EditText editTextTeacherName = findViewById(R.id.editTextTeacherName);
        EditText editTextTeacherSubject = findViewById(R.id.editTextTeacherSubject);
        EditText editTextTeacherPhone = findViewById(R.id.editTextTeacherPhone);
        EditText editTextTeacherEmail = findViewById(R.id.editTextTeacherEmail);
        EditText editTextTeacherPassword = findViewById(R.id.editTextTeacherPassword);

        Button buttonSignup = findViewById(R.id.buttonSignup);
        imageViewTeacher = findViewById(R.id.imageViewTeacher);

        buttonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the teacher details from the EditText fields
                String teacherID = editTextTeacherID.getText().toString().trim();
                String teacherName = editTextTeacherName.getText().toString().trim();
                String teacherSubject = editTextTeacherSubject.getText().toString().trim();
                String teacherPhone = editTextTeacherPhone.getText().toString().trim();
                String teacherEmail = editTextTeacherEmail.getText().toString().trim();
                String teacherPassword = editTextTeacherPassword.getText().toString().trim();

                // Validate the inputs
                if (teacherID.isEmpty() || teacherName.isEmpty() || teacherSubject.isEmpty() ||
                        teacherPhone.isEmpty() || teacherEmail.isEmpty() || teacherPassword.isEmpty() || imageUri == null) {
                    Toast.makeText(TeacherSignupActivity.this, "Please fill in all the details", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Show a progress dialog while uploading data and image
                progressDialog = new ProgressDialog(TeacherSignupActivity.this);
                progressDialog.setMessage("Signing up teacher...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                // Upload the teacher image to Firebase Storage and get the download URL
                uploadTeacherImage(teacherID, teacherName, teacherSubject, teacherPhone, teacherEmail, teacherPassword);
            }
        });

        // Set click listener for the teacher image view to pick an image
        imageViewTeacher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });
    }

    private void pickImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageViewTeacher.setImageURI(imageUri);
        }
    }

    private void uploadTeacherImage(String teacherID, String teacherName, String teacherSubject, String teacherPhone, String teacherEmail, String teacherPassword) {
        // Get the reference to Firebase Storage
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference().child("teacher_images").child(teacherID + ".jpg");

        // Upload the image
        UploadTask uploadTask = storageReference.putFile(imageUri);

        // Get the download URL for the uploaded image
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return storageReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    if (downloadUri != null) {
                        String imageUrl = downloadUri.toString();

                        // Save the teacher details and image URL to Firebase Realtime Database
                        saveTeacherDetailsToDatabase(teacherID, teacherName, teacherSubject, teacherPhone, teacherEmail, teacherPassword, imageUrl);
                    } else {
                        // Failed to get download URL
                        progressDialog.dismiss();
                        Toast.makeText(TeacherSignupActivity.this, "Failed to upload teacher image", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(TeacherSignupActivity.this, "Failed to upload teacher image", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveTeacherDetailsToDatabase(String teacherID, String teacherName, String teacherSubject, String teacherPhone, String teacherEmail, String teacherPassword, String imageUrl) {
        // Get the reference to Firebase Realtime Database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("teachers").child(teacherID);

        // Create a Teacher object to save to the database
        Teacher teacher = new Teacher(teacherID, teacherName, teacherSubject, teacherPhone, teacherEmail, teacherPassword, imageUrl);

        // Save the teacher object to the database
        databaseReference.setValue(teacher).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    // Teacher signup success
                    Toast.makeText(TeacherSignupActivity.this, "Teacher signup successful", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    // Failed to save teacher details to the database
                    Toast.makeText(TeacherSignupActivity.this, "Failed to signup teacher", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

class Teacher {
    private String teacherID;
    private String teacherName;
    private String teacherSubject;
    private String teacherPhone;
    private String teacherEmail;
    private String teacherPassword;
    private String imageUrl;

    // Empty constructor for Firebase
    public Teacher() {
    }

    public Teacher(String teacherID, String teacherName, String teacherSubject, String teacherPhone, String teacherEmail, String teacherPassword, String imageUrl) {
        this.teacherID = teacherID;
        this.teacherName = teacherName;
        this.teacherSubject = teacherSubject;
        this.teacherPhone = teacherPhone;
        this.teacherEmail = teacherEmail;
        this.teacherPassword = teacherPassword;
        this.imageUrl = imageUrl;
    }

    public String getTeacherID() {
        return teacherID;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public String getTeacherSubject() {
        return teacherSubject;
    }

    public String getTeacherPhone() {
        return teacherPhone;
    }

    public String getTeacherEmail() {
        return teacherEmail;
    }

    public String getTeacherPassword() {
        return teacherPassword;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}

