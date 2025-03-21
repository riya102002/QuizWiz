package com.utkarsh.quizwiz;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class TeacherProfileActivity extends AppCompatActivity {

    private ImageView profileImage;
    private EditText etName, etEmail, etPhone, etSubject, etPassword;
    private Button btnUpdate, btnChangeImage;

    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    private String teacherID;
    private Uri imageUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_profile);

        profileImage = findViewById(R.id.profileImage);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etSubject = findViewById(R.id.etSubject);
        etPassword = findViewById(R.id.etPassword);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnChangeImage = findViewById(R.id.btnChangeImage);

        teacherID = getIntent().getStringExtra("teacherID");
        if (TextUtils.isEmpty(teacherID)) {
            Toast.makeText(this, "Teacher ID not found.", Toast.LENGTH_SHORT).show();
            finish();
        }

        databaseReference = FirebaseDatabase.getInstance().getReference().child("teachers").child(teacherID);
        storageReference = FirebaseStorage.getInstance().getReference().child("teacher_images").child(teacherID + ".jpg");

        loadTeacherProfile();

        btnChangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTeacherProfile();
            }
        });
    }

    private void loadTeacherProfile() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("teacherName").getValue(String.class);
                    String email = dataSnapshot.child("teacherEmail").getValue(String.class);
                    String phone = dataSnapshot.child("teacherPhone").getValue(String.class);
                    String subject = dataSnapshot.child("teacherSubject").getValue(String.class);

                    etName.setText(name);
                    etEmail.setText(email);
                    etPhone.setText(phone);
                    etSubject.setText(subject);

                    String imageUrl = dataSnapshot.child("imageUrl").getValue(String.class);
                    if (!TextUtils.isEmpty(imageUrl)) {
                        Glide.with(TeacherProfileActivity.this)
                                .load(imageUrl)
                                .apply(RequestOptions.circleCropTransform())
                                .placeholder(R.drawable.teachers)
                                .into(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
        }
    }

    private void updateTeacherProfile() {
        final String name = etName.getText().toString().trim();
        final String email = etEmail.getText().toString().trim();
        final String phone = etPhone.getText().toString().trim();
        final String subject = etSubject.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(subject)) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.child("teacherName").setValue(name);
        databaseReference.child("teacherEmail").setValue(email);
        databaseReference.child("teacherPhone").setValue(phone);
        databaseReference.child("teacherSubject").setValue(subject);

        if (!TextUtils.isEmpty(password)) {
            databaseReference.child("teacherPassword").setValue(password);
        }

        if (imageUri != null) {
            storageReference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    String imageUrl = task.getResult().toString();
                                    databaseReference.child("imageUrl").setValue(imageUrl);
                                    Toast.makeText(TeacherProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            });
        } else {
            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
        }
    }
}
