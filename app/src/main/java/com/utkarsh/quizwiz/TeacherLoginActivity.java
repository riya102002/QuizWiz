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

public class TeacherLoginActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private EditText editTextTeacherID;
    private EditText editTextTeacherPassword;
    private CheckBox rememberMeCheckbox;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;
    private boolean rememberMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_login);

        editTextTeacherID = findViewById(R.id.editTextTeacherID);
        editTextTeacherPassword = findViewById(R.id.editTextTeacherPassword);
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox);

        // Initialize SharedPreferences for saving login info
        loginPreferences = getSharedPreferences("loginPrefsTeacher", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        // Load saved login info if available
        rememberMe = loginPreferences.getBoolean("rememberMe", false);
        if (rememberMe) {
            String savedTeacherID = loginPreferences.getString("teacherID", "");
            String savedPassword = loginPreferences.getString("password", "");
            Toast.makeText(this, "Saved user "+savedTeacherID+" found!", Toast.LENGTH_SHORT).show();

            editTextTeacherID.setText(savedTeacherID);
            editTextTeacherPassword.setText(savedPassword);
            rememberMeCheckbox.setChecked(true);
        }

        ImageView loginButton = findViewById(R.id.login_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the teacher ID and password from the EditText fields
                final String teacherID = editTextTeacherID.getText().toString().trim();
                final String password = editTextTeacherPassword.getText().toString().trim();

                // Validate the inputs
                if (teacherID.isEmpty() || password.isEmpty()) {
                    Toast.makeText(TeacherLoginActivity.this, "Please enter teacher ID and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Show a progress dialog while verifying credentials
                progressDialog = new ProgressDialog(TeacherLoginActivity.this);
                progressDialog.setMessage("Verifying credentials...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                // Verify the teacher credentials from Firebase Realtime Database
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("teachers").child(teacherID);
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        progressDialog.dismiss();
                        if (dataSnapshot.exists()) {
                            String storedPassword = dataSnapshot.child("teacherPassword").getValue(String.class);
                            String storedName = dataSnapshot.child("teacherName").getValue(String.class);
                            if (storedPassword != null && storedPassword.equals(password)) {
                                // Password is correct, login successful
                                Toast.makeText(TeacherLoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(TeacherLoginActivity.this, TeacherActivity.class);
                                intent.putExtra("username", teacherID);
                                intent.putExtra("teacherName", storedName);
                                startActivity(intent);
                                finish();
                            } else {
                                // Incorrect password
                                Toast.makeText(TeacherLoginActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Teacher with given ID not found in the database
                            Toast.makeText(TeacherLoginActivity.this, "Teacher with given ID not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressDialog.dismiss();
                        Toast.makeText(TeacherLoginActivity.this, "Failed to verify credentials", Toast.LENGTH_SHORT).show();
                    }
                });

                // Save login info if "Remember Me" is checked
                if (rememberMeCheckbox.isChecked()) {
                    loginPrefsEditor.putBoolean("rememberMe", true);
                    loginPrefsEditor.putString("teacherID", teacherID);
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
