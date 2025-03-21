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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class StudentSignupActivity extends AppCompatActivity {

    private static final int IMAGE_REQUEST_CODE = 1;

    private ImageView imageViewStudent;
    private Uri imageUri;
    private ProgressDialog progressDialog;

    EditText editTextStudentTeacherID;
    String studentID, studentName, studentClass, studentPhone, studentPassword, studentSchool, studentTeacherID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_signup);

        EditText editTextStudentID = findViewById(R.id.editTextStudentID);
        EditText editTextStudentName = findViewById(R.id.editTextStudentName);
        EditText editTextStudentClass = findViewById(R.id.editTextStudentClass);
        EditText editTextStudentPhone = findViewById(R.id.editTextStudentPhone);
        EditText editTextStudentSchool = findViewById(R.id.editTextStudentSchool);
        EditText editTextStudentPassword = findViewById(R.id.editTextStudentPassword);
        editTextStudentTeacherID = findViewById(R.id.editTextStudentTeacherID);

        Button buttonSignup = findViewById(R.id.buttonSignup);
        imageViewStudent = findViewById(R.id.imageViewStudent);

        buttonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the teacher details from the EditText fields
                studentID = editTextStudentID.getText().toString().trim();
                studentName = editTextStudentName.getText().toString().trim();
                studentClass = editTextStudentClass.getText().toString().trim();
                studentPhone = editTextStudentPhone.getText().toString().trim();
                studentSchool = editTextStudentSchool.getText().toString().trim();
                studentPassword = editTextStudentPassword.getText().toString().trim();
                studentTeacherID = editTextStudentTeacherID.getText().toString().trim();

                // Validate the inputs
                if (studentID.isEmpty() || studentName.isEmpty() || studentClass.isEmpty() ||
                        studentPhone.isEmpty() || studentSchool.isEmpty() || studentPassword.isEmpty() ||
                studentTeacherID.isEmpty() || imageUri == null) {
                    Toast.makeText(StudentSignupActivity.this, "Please fill in all the details", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Show a progress dialog while uploading data and image
                progressDialog = new ProgressDialog(StudentSignupActivity.this);
                progressDialog.setMessage("Signing up student...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                // Validate teacherID before signing up the student
                validateTeacherID(studentTeacherID);

                // Upload the teacher image to Firebase Storage and get the download URL
//                uploadStudentImage(studentID, studentName, studentClass, studentPhone, studentPassword, studentSchool, studentTeacherID);
            }
        });

        // Set click listener for the teacher image view to pick an image
        imageViewStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });
    }

    private void validateTeacherID(final String teacherID) {
        DatabaseReference teacherReference = FirebaseDatabase.getInstance().getReference().child("teachers");
        teacherReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(teacherID)) {
                    // Teacher ID exists, proceed to upload image and sign up
                    progressDialog = new ProgressDialog(StudentSignupActivity.this);
                    progressDialog.setMessage("Signing up student...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    uploadStudentImage(studentID, studentName, studentClass, studentPhone, studentPassword, studentSchool, studentTeacherID);
                } else {
                    // Invalid Teacher ID, show error message
                    editTextStudentTeacherID.setError("Invalid Teacher ID");
                    progressDialog.dismiss(); // Dismiss progress dialog only if the teacher ID is invalid
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled as needed
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
            imageViewStudent.setImageURI(imageUri);
        }
    }

    private void uploadStudentImage(String studentID, String studentName, String studentClass, String studentPhone, String studentPassword, String studentSchool, String studentTeacherID) {
        // Get the reference to Firebase Storage
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference().child("student_images").child(studentID + ".jpg");

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
                        saveTeacherDetailsToDatabase(studentID, studentName, studentClass, studentPhone, studentPassword, studentSchool, studentTeacherID, imageUrl);
                    } else {
                        // Failed to get download URL
                        progressDialog.dismiss();
                        Toast.makeText(StudentSignupActivity.this, "Failed to upload student image", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(StudentSignupActivity.this, "Failed to upload student image", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveTeacherDetailsToDatabase(String studentID, String studentName, String studentClass, String studentPhone, String studentPassword, String studentSchool, String studentTeacherID, String imageUrl) {
        // Get the reference to Firebase Realtime Database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("students").child(studentID);

        // Create a Teacher object to save to the database
        Student student = new Student(studentID, studentName, studentClass, studentPhone, studentPassword, studentSchool, studentTeacherID, imageUrl);

        // Save the teacher object to the database
        databaseReference.setValue(student).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    // Teacher signup success
                    Toast.makeText(StudentSignupActivity.this, "Student signup successful", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    // Failed to save teacher details to the database
                    Toast.makeText(StudentSignupActivity.this, "Failed to signup student", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

class Student {
    private String studentID;
    private String studentName;
    private String studentClass;
    private String studentPhone;
    private String studentSchool;
    private String studentPassword;
    private String studentTeacherID;
    private String imageUrl;

    // Empty constructor for Firebase
    public Student() {
    }

    public Student(String studentID, String studentName, String studentClass, String studentPhone, String studentPassword, String studentSchool, String studentTeacherID, String imageUrl) {
        this.studentID = studentID;
        this.studentName = studentName;
        this.studentClass = studentClass;
        this.studentPhone = studentPhone;
        this.studentSchool = studentSchool;
        this.studentPassword = studentPassword;
        this.studentTeacherID = studentTeacherID;
        this.imageUrl = imageUrl;
    }

    public String getStudentID() {
        return studentID;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStudentClass() {
        return studentClass;
    }

    public String getStudentPhone() {
        return studentPhone;
    }

    public String getStudentSchool() {
        return studentSchool;
    }

    public String getStudentPassword() {
        return studentPassword;
    }

    public String getStudentTeacherID() {
        return studentTeacherID;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}

