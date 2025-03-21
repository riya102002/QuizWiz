package com.utkarsh.quizwiz;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StudentActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageView menuIcon;
    private ImageView studentImageView;
    private TextView studentNameTextView;

    private TextView randomQuotesTextView;
    private ImageView refreshButton;
    private String currentQuote = "";
    String studentID, studentName, studentClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        randomQuotesTextView = findViewById(R.id.random_quotes_text_view);
        refreshButton = findViewById(R.id.refresh_button);

        fetchRandomQuote();

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchRandomQuote();
            }
        });

        // Set click listener for the quiz icon to open StudentQuizActivity
        ImageView quizIcon = findViewById(R.id.quiz_icon);
        quizIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StudentActivity.this, StudentQuizActivity.class);
                intent.putExtra("studentID", studentID);
                startActivity(intent);
            }
        });

        drawerLayout = findViewById(R.id.drawer_layout);
        menuIcon = findViewById(R.id.menu_icon);
        studentImageView = findViewById(R.id.student_image_view);
        studentNameTextView = findViewById(R.id.student_name_text_view);

        studentID = getIntent().getStringExtra("username");
        studentName = getIntent().getStringExtra("studentName");
        studentClass = getIntent().getStringExtra("studentClass");
        // Fetch and display user's image and name from Firebase Storage and Realtime Database
        fetchStudentInfoFromFirebase(studentID);

        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Set up sidebar options click listeners
        TextView viewProfile = findViewById(R.id.nav_view_profile);
        TextView studentStats = findViewById(R.id.nav_student_stats);

        viewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle "View Profile" option click
                // ...
                Intent intent = new Intent(StudentActivity.this, StudentProfileActivity.class);
                intent.putExtra("studentID", studentID);
                startActivity(intent);
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        studentStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle "Stats" option click
                // ...
                showViewStatsPopup();
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });
    }

    public void showViewStatsPopup(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.popup_create_quiz, null);
        dialogBuilder.setView(dialogView);

        Button btnMCQ = dialogView.findViewById(R.id.btnMCQ);
        Button btnSubjective = dialogView.findViewById(R.id.btnSubjective);

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        btnMCQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle MCQ quiz creation
                Intent intent = new Intent(StudentActivity.this, MCQViewQuizStudentStatsActivity.class);
                intent.putExtra("studentID", studentID);
                intent.putExtra("studentName", studentName);
                intent.putExtra("studentClass", studentClass);
                startActivity(intent);
                alertDialog.dismiss();
            }
        });

        btnSubjective.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle Subjective quiz creation
                alertDialog.dismiss();
            }
        });
    }

    private void fetchRandomQuote() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.quotable.io/random")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject quoteObject = new JSONObject(responseBody);
                        String quoteText = quoteObject.getString("content");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                currentQuote = quoteText;
                                displayQuote(currentQuote);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void displayQuote(String quote) {
        randomQuotesTextView.setText("\"" + quote + "\"");
        refreshButton.setVisibility(View.VISIBLE);
        randomQuotesTextView.setVisibility(View.VISIBLE);
    }

    private void fetchStudentInfoFromFirebase(String studentId) {
        // Reference to Firebase Storage
        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("student_images")
                .child(studentId + ".jpg");

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Load image into circular ImageView using Glide
                Glide.with(StudentActivity.this)
                        .load(uri)
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.students)
                        .into(studentImageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                studentImageView.setImageResource(R.drawable.students);
            }
        });

        // Reference to Firebase Realtime Database
        FirebaseDatabase.getInstance().getReference()
                .child("students")
                .child(studentId)
                .child("studentName")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String studentName = snapshot.getValue(String.class);
                        studentNameTextView.setText(studentName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle database read error
                    }
                });
    }
}
