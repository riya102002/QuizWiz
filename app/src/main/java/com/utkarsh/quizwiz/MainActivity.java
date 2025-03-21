package com.utkarsh.quizwiz;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the Student and Teacher buttons by their IDs
        ImageView studentButton = findViewById(R.id.studentImageView);
        ImageView teacherButton = findViewById(R.id.teacherImageView);

        TextView textView = findViewById(R.id.footerTextView);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open the LinkedIn profile in a web browser
                String linkedInProfileUrl = "https://utkarsh140503.github.io/Portfolio/";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkedInProfileUrl));
                startActivity(intent);
            }
        });

        // Set click listeners for the buttons
        studentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the popup with student options
                Toast.makeText(MainActivity.this, "Student Option Selected!", Toast.LENGTH_SHORT).show();
                showStudentOptionsPopup();
            }
        });

        teacherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the popup with teacher options
                Toast.makeText(MainActivity.this, "Teacher Option Selected!", Toast.LENGTH_SHORT).show();
                showTeacherOptionsPopup();
            }
        });
    }

    private void showStudentOptionsPopup() {
        // Create the dialog
        Dialog optionsDialog = new Dialog(this);
        optionsDialog.setContentView(R.layout.popup_options);
        optionsDialog.setCancelable(true);

        // Find the signup and login buttons inside the dialog
        ImageView signupButton = optionsDialog.findViewById(R.id.signupButton);
        ImageView loginButton = optionsDialog.findViewById(R.id.loginButton);

        // Set click listeners for the buttons inside the dialog
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the Student Signup activity
                startActivity(new Intent(MainActivity.this, StudentSignupActivity.class));
                optionsDialog.dismiss(); // Close the dialog after starting the activity
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the Student Login activity
                startActivity(new Intent(MainActivity.this, StudentLoginActivity.class));
                optionsDialog.dismiss(); // Close the dialog after starting the activity
            }
        });

        // Show the dialog
        optionsDialog.show();
    }

    private void showTeacherOptionsPopup() {
        // Create the dialog
        Dialog optionsDialog = new Dialog(this);
        optionsDialog.setContentView(R.layout.popup_options);
        optionsDialog.setCancelable(true);

        // Find the signup and login buttons inside the dialog
        ImageView signupButton = optionsDialog.findViewById(R.id.signupButton);
        ImageView loginButton = optionsDialog.findViewById(R.id.loginButton);

        // Set click listeners for the buttons inside the dialog
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the Teacher Signup activity
                startActivity(new Intent(MainActivity.this, TeacherSignupActivity.class));
                optionsDialog.dismiss(); // Close the dialog after starting the activity
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the Teacher Login activity
                startActivity(new Intent(MainActivity.this, TeacherLoginActivity.class));
                optionsDialog.dismiss(); // Close the dialog after starting the activity
            }
        });

        // Show the dialog
        optionsDialog.show();
    }
}
