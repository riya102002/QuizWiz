package com.utkarsh.quizwiz;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

// Existing imports...

public class ShowOverallQuizStatsActivity extends AppCompatActivity {

    private ListView studentListView;
    private TextView studentCountTextView; // New TextView for student count
    private ArrayAdapter<String> studentListAdapter;
    private List<String> studentIds;

    private DatabaseReference studentResponsesRef;
    private String selectedClass, selectedDate, selectedSubject, selectedQuizId, teacherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_overall_quiz_stats);

        selectedClass = getIntent().getStringExtra("selectedQuizId");
        selectedDate = getIntent().getStringExtra("selectedDate");
        selectedSubject = getIntent().getStringExtra("selectedSubject");
        selectedQuizId = getIntent().getStringExtra("selectedClass");
        teacherId = getIntent().getStringExtra("teacherId");

        studentListView = findViewById(R.id.studentListView);
        studentCountTextView = findViewById(R.id.studentCountTextView); // Initialize the TextView
        studentIds = new ArrayList<>();
        studentListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, studentIds);
        studentListView.setAdapter(studentListAdapter);

        studentResponsesRef = FirebaseDatabase.getInstance().getReference()
                .child("ResponsesByStudents")
                .child(teacherId)
                .child(selectedDate)
                .child(selectedSubject)
                .child(selectedClass)
                .child(selectedQuizId);

        Query studentIdsQuery = studentResponsesRef.orderByKey();
        studentIdsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                studentIds.clear();
                for (DataSnapshot studentSnapshot : dataSnapshot.getChildren()) {
                    String studentId = studentSnapshot.getKey();
                    studentIds.add(studentId);
                }
                studentListAdapter.notifyDataSetChanged();
                if(studentIds.size()==1){
                    studentCountTextView.setText("Quiz ["+selectedQuizId+"] attempted by " + studentIds.size() + " student"); // Update student count
                }else{
                    studentCountTextView.setText("Quiz ["+selectedQuizId+"] attempted by " + studentIds.size() + " students"); // Update student count
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });

        studentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedStudentId = studentIds.get(position);
                showStudentDetailsPopup(selectedStudentId);
            }
        });
    }

    private void showStudentDetailsPopup(String studentId) {
        DatabaseReference studentRef = studentResponsesRef.child(studentId);

        studentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ShowOverallQuizStatsActivity.this);
                builder.setTitle(studentId+"'s Performance");

                StringBuilder detailsText = new StringBuilder();
                for (DataSnapshot questionSnapshot : dataSnapshot.getChildren()) {
                    String questionId = questionSnapshot.getKey();
                    String selectedOption = questionSnapshot.child("selectedOption").getValue(String.class);
                    String correctOption = questionSnapshot.child("correctOption").getValue(String.class);
                    String responseDateAndTime = dataSnapshot.child("responseDateAndTime").getValue(String.class);

                    String correctness;
                    if (selectedOption != null && correctOption != null) {
                        correctness = selectedOption.equals(correctOption) ? "Correct" : "Incorrect";
                    } else {
                        correctness = "N/A"; // Handle the case where either option is null
                    }

                    if(!questionId.equals("responseDateAndTime")){
                        detailsText.append("Question ID: ").append(questionId).append("\n");
                        detailsText.append("Selected Option: ").append(selectedOption).append("\n");
                        detailsText.append("Correct Option: ").append(correctOption).append("\n");
                        detailsText.append("Status: ").append(correctness).append("\n");
                        detailsText.append("Response Date and Time: ").append(responseDateAndTime).append("\n\n");
                    }
                }

                builder.setMessage(detailsText.toString());
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_show_stats, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_show_graphical_stats) {
            // Redirect to QuizGraphicalStatsActivity
            Intent intent = new Intent(this, QuizGraphicalStatsActivity.class);
            intent.putExtra("selectedClass", selectedClass);
            intent.putExtra("selectedDate", selectedDate);
            intent.putExtra("selectedSubject", selectedSubject);
            intent.putExtra("selectedQuizId", selectedQuizId);
            intent.putExtra("teacherId", teacherId);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

