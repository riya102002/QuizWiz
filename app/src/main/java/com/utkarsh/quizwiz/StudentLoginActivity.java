package com.utkarsh.quizwiz;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StudentLoginActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private EditText editTextStudentID;
    private EditText editTextStudentPassword;
    private CheckBox rememberMeCheckbox;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;
    private boolean rememberMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_login);

        editTextStudentID = findViewById(R.id.editTextStudentID);
        editTextStudentPassword = findViewById(R.id.editTextStudentPassword);
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox);

        // Initialize SharedPreferences for saving login info
        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        // Load saved login info if available
        rememberMe = loginPreferences.getBoolean("rememberMe", false);
        if (rememberMe) {
            String savedStudentID = loginPreferences.getString("studentID", "");
            String savedPassword = loginPreferences.getString("password", "");
            Toast.makeText(this, "Saved user "+savedStudentID+" found!", Toast.LENGTH_SHORT).show();
            editTextStudentID.setText(savedStudentID);
            editTextStudentPassword.setText(savedPassword);
            rememberMeCheckbox.setChecked(true);
        }

        ImageView loginButton = findViewById(R.id.login_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the student ID and password from the EditText fields
                final String studentID = editTextStudentID.getText().toString().trim();
                final String password = editTextStudentPassword.getText().toString().trim();

                // Validate the inputs
                if (studentID.isEmpty() || password.isEmpty()) {
                    Toast.makeText(StudentLoginActivity.this, "Please enter student ID and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Show a progress dialog while verifying credentials
                progressDialog = new ProgressDialog(StudentLoginActivity.this);
                progressDialog.setMessage("Verifying credentials...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                // Verify the student credentials from Firebase Realtime Database
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("students").child(studentID);
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        progressDialog.dismiss();
                        if (dataSnapshot.exists()) {
                            String storedPassword = dataSnapshot.child("studentPassword").getValue(String.class);
                            String storedName = dataSnapshot.child("studentName").getValue(String.class);
                            String storedClass = dataSnapshot.child("studentClass").getValue(String.class);
                            if (storedPassword != null && storedPassword.equals(password)) {
                                // Password is correct, login successful
                                Toast.makeText(StudentLoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(StudentLoginActivity.this, StudentActivity.class);
                                intent.putExtra("username", studentID);
                                intent.putExtra("studentName", storedName);
                                intent.putExtra("studentClass", storedClass);
                                startActivity(intent);
                                finish();
                            } else {
                                // Incorrect password
                                Toast.makeText(StudentLoginActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Student with given ID not found in the database
                            Toast.makeText(StudentLoginActivity.this, "Student with given ID not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressDialog.dismiss();
                        Toast.makeText(StudentLoginActivity.this, "Failed to verify credentials", Toast.LENGTH_SHORT).show();
                    }
                });

                // Save login info if "Remember Me" is checked
                if (rememberMeCheckbox.isChecked()) {
                    loginPrefsEditor.putBoolean("rememberMe", true);
                    loginPrefsEditor.putString("studentID", studentID);
                    loginPrefsEditor.putString("password", password);
                    loginPrefsEditor.apply();
                } else {
                    loginPrefsEditor.clear();
                    loginPrefsEditor.apply();
                }
            }
        });
    }
}
