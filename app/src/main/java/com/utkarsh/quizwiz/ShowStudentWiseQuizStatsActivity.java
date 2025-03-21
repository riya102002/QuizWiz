package com.utkarsh.quizwiz;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

public class ShowStudentWiseQuizStatsActivity extends AppCompatActivity {

    private Spinner studentSpinner;
    private ListView questionListView;
    private Button getResultButton;

    private String selectedClass, selectedDate, selectedSubject, selectedQuizId, teacherId;

    private DatabaseReference studentResponsesRef;
    private ArrayAdapter<String> questionListAdapter;
    private List<String> questionIds;

    private int correctAnswers = 0;
    private int totalQuestions = 0;
    String selectedStudentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_student_wise_quiz_stats);

        Intent intent = getIntent();
        selectedClass = intent.getStringExtra("selectedQuizId");
        selectedDate = intent.getStringExtra("selectedDate");
        selectedSubject = intent.getStringExtra("selectedSubject");
        selectedQuizId = intent.getStringExtra("selectedClass");
        teacherId = intent.getStringExtra("teacherId");

        studentSpinner = findViewById(R.id.studentSpinner);
        questionListView = findViewById(R.id.questionListView);
        getResultButton = findViewById(R.id.getResultButton);

        questionIds = new ArrayList<>();
        questionListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, questionIds);
        questionListView.setAdapter(questionListAdapter);

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
                List<String> studentIds = new ArrayList<>();
                for (DataSnapshot studentSnapshot : dataSnapshot.getChildren()) {
                    String studentId = studentSnapshot.getKey();
                    studentIds.add(studentId);
                }

                ArrayAdapter<String> studentAdapter = new ArrayAdapter<>(ShowStudentWiseQuizStatsActivity.this, android.R.layout.simple_spinner_item, studentIds);
                studentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                studentSpinner.setAdapter(studentAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ShowStudentWiseQuizStatsActivity.this, "Error retrieving studentIds", Toast.LENGTH_SHORT).show();
            }
        });

        studentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                selectedStudentId = adapterView.getItemAtPosition(position).toString();
                loadQuestionIds(selectedStudentId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });

        getResultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calculateResults();
            }
        });
    }

    private void loadQuestionIds(String selectedStudentId) {
        DatabaseReference studentQuestionIdsRef = studentResponsesRef.child(selectedStudentId);
        studentQuestionIdsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                questionIds.clear();
                for (DataSnapshot questionSnapshot : dataSnapshot.getChildren()) {
                    String questionId = questionSnapshot.getKey();
                    if(!questionId.equals("responseDateAndTime")){
                        String correctResponse = questionSnapshot.child("correctOption").getValue(String.class);
                        String selectedResponse = questionSnapshot.child("selectedOption").getValue(String.class);
                        questionIds.add("Question ID: " + questionId
                                + "\nCorrect Response: " + correctResponse
                                + "\nSelected Response: " + selectedResponse);
                    }
                }
                questionListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ShowStudentWiseQuizStatsActivity.this, "Error retrieving questionIds", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateResults() {
        correctAnswers = 0;
        totalQuestions = questionIds.size();

        for (String questionInfo : questionIds) {
            String[] lines = questionInfo.split("\n");
            String selectedResponse = lines[2].substring(18); // Extracting selected response
            String correctResponse = lines[1].substring(17); // Extracting correct response

            if (selectedResponse.equals(correctResponse)) {
                correctAnswers++;
            }
        }

        double percentage = (correctAnswers * 100.0) / totalQuestions;

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_results, null);

        TextView correctAnswersTextView = dialogView.findViewById(R.id.correctAnswersTextView);
        TextView wrongAnswersTextView = dialogView.findViewById(R.id.wrongAnswersTextView);
        TextView totalQuestionsTextView = dialogView.findViewById(R.id.totalQuestionsTextView);
        TextView percentageTextView = dialogView.findViewById(R.id.percentageTextView);

        correctAnswersTextView.setText("Correct Answers: " + correctAnswers);
        wrongAnswersTextView.setText("Wrong Answers: " + (totalQuestions - correctAnswers));
        totalQuestionsTextView.setText("Total Questions: " + totalQuestions);
        percentageTextView.setText("Percentage of Correct Answers: " + String.format("%.2f", percentage) + "%");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setTitle("Quiz Results for Student: "+selectedStudentId)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();
    }
}
