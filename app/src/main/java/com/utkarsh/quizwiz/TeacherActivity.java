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

public class TeacherActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageView menuIcon;
    private ImageView userImageView;
    private TextView teacherNameTextView;

    private TextView randomQuotesTextView;
    private ImageView refreshButton;
    private String currentQuote = "";
    String teacherID, teacherName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        randomQuotesTextView = findViewById(R.id.random_quotes_text_view);
        refreshButton = findViewById(R.id.refresh_button);

        fetchRandomQuote();

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchRandomQuote();
            }
        });

        drawerLayout = findViewById(R.id.drawer_layout);
        menuIcon = findViewById(R.id.menu_icon);
        userImageView = findViewById(R.id.user_image_view);
        teacherNameTextView = findViewById(R.id.teacher_name_text_view);

        teacherID = getIntent().getStringExtra("username");
        teacherName = getIntent().getStringExtra("teacherName");

        // Fetch and display user's image and name from Firebase Storage and Realtime Database
        fetchTeacherInfoFromFirebase(teacherID);

        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Set up sidebar options click listeners
        TextView createQuiz = findViewById(R.id.nav_create_quiz);
        TextView viewQuizzes = findViewById(R.id.nav_view_quizzes);
        TextView viewProfile = findViewById(R.id.nav_view_profile);
        TextView stats = findViewById(R.id.nav_stats);

        createQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle "Create Quiz" option click
                // ...
                showCreateQuizPopup();
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        viewQuizzes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle "View Quizzes" option click
                // ...
                showViewQuizPopup();
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        viewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle "View Profile" option click
                // ...
                Intent intent = new Intent(TeacherActivity.this, TeacherProfileActivity.class);
                intent.putExtra("teacherID", teacherID);
                startActivity(intent);
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        stats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle "Stats" option click
                // ...
                showViewStatsPopup();
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });
    }

    private void showCreateQuizPopup() {
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
                Intent intent = new Intent(TeacherActivity.this, MCQQuizCreationActivity.class);
                intent.putExtra("teacherID", teacherID);
                intent.putExtra("teacherName", teacherName);
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

    private void showViewQuizPopup() {
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
                Intent intent = new Intent(TeacherActivity.this, MCQViewQuizActivity.class);
                intent.putExtra("teacherID", teacherID);
                intent.putExtra("teacherName", teacherName);
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
                Intent intent = new Intent(TeacherActivity.this, MCQViewQuizStatsActivity.class);
                intent.putExtra("teacherID", teacherID);
                intent.putExtra("teacherName", teacherName);
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

    private void fetchTeacherInfoFromFirebase(String teacherId) {
        // Reference to Firebase Storage
        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("teacher_images")
                .child(teacherId + ".jpg");

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Load image into circular ImageView using Glide
                Glide.with(TeacherActivity.this)
                        .load(uri)
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.teachers)
                        .into(userImageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                userImageView.setImageResource(R.drawable.teachers);
            }
        });

        // Reference to Firebase Realtime Database
        FirebaseDatabase.getInstance().getReference()
                .child("teachers")
                .child(teacherId)
                .child("teacherName")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String teacherName = snapshot.getValue(String.class);
                        teacherNameTextView.setText(teacherName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle database read error
                    }
                });
    }
}
