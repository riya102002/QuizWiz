// The Java code in MCQViewQuizActivity.java
package com.utkarsh.quizwiz;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MCQViewQuizActivity extends AppCompatActivity {

    private ListView listViewQuizzes;
    private List<String> quizDates = new ArrayList<>();
    private Map<String, List<String>> quizSubjectsMap = new HashMap<>();

    private DatabaseReference databaseReference;
    String teacherID, teacherName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mcqview_quiz);

        teacherID = getIntent().getStringExtra("teacherID");
        teacherName = getIntent().getStringExtra("teacherName");
        teacherID = teacherID + "-->" + teacherName;

        databaseReference = FirebaseDatabase.getInstance().getReference();

        listViewQuizzes = findViewById(R.id.listViewQuizzes);

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, quizDates);
        listViewQuizzes.setAdapter(adapter);

        Query quizzesQuery = databaseReference.child("quizzes").child(teacherID);
        quizzesQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                quizDates.clear();
                quizSubjectsMap.clear();
                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                    String date = dateSnapshot.getKey();
                    quizDates.add(date);

                    List<String> subjects = new ArrayList<>();
                    for (DataSnapshot subjectSnapshot : dateSnapshot.getChildren()) {
                        subjects.add(subjectSnapshot.getKey());
                    }
                    quizSubjectsMap.put(date, subjects);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MCQViewQuizActivity.this, "Failed to fetch quizzes.", Toast.LENGTH_SHORT).show();
            }
        });

        listViewQuizzes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedDate = quizDates.get(position);
                showSubjectDialog(selectedDate);
            }
        });
    }

    private void showSubjectDialog(String selectedDate) {
        List<String> subjects = quizSubjectsMap.get(selectedDate);
        if (subjects != null) {
            String[] subjectArray = subjects.toArray(new String[0]);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            View dialogView = getLayoutInflater().inflate(R.layout.dialog_subject_list, null);
            builder.setView(dialogView);

            ListView listViewSubjects = dialogView.findViewById(R.id.listViewSubjects);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, subjectArray);
            listViewSubjects.setAdapter(adapter);

            AlertDialog dialog = builder.create();
            dialog.show();

            listViewSubjects.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String selectedSubject = subjectArray[position];
                    showQuizClassDialog(teacherID, selectedDate, selectedSubject);
                }
            });
        }
    }

    private void showQuizClassDialog(String teacherID, String selectedDate, String selectedSubject) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_quiz_class, null);
        builder.setView(dialogView);

        Spinner spinnerQuizClass = dialogView.findViewById(R.id.spinnerQuizClass);

        List<String> classList = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, classList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerQuizClass.setAdapter(adapter);

        // Check classes with quizzes
        for (int i = 1; i <= 12; i++) {
            String classWithPrefix = "Class:" + i;
            checkQuizzesInClass(teacherID, selectedDate, selectedSubject, classWithPrefix, adapter, spinnerQuizClass);
        }

        AlertDialog dialog = builder.create();
        dialog.show();

        dialogView.findViewById(R.id.btnSelectClass).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedQuizClass = spinnerQuizClass.getSelectedItem().toString();
                checkAndShowQuizListDialog(teacherID, selectedDate, selectedSubject, selectedQuizClass);
            }
        });
    }

    private void checkQuizzesInClass(String teacherID, String selectedDate, String selectedSubject, String selectedClass, ArrayAdapter<String> adapter, Spinner spinnerQuizClass) {
        DatabaseReference quizzesRef = databaseReference.child("quizzes").child(teacherID)
                .child(selectedDate).child(selectedSubject).child(selectedClass);

        quizzesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    adapter.add(selectedClass); // Add the class to the adapter
                    spinnerQuizClass.setSelection(adapter.getPosition(selectedClass)); // Set selection
                } else {
//                    Toast.makeText(MCQViewQuizActivity.this, "No quizzes available in this class.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }


    private void checkAndShowQuizListDialog(String teacherID, String selectedDate, String selectedSubject, String selectedClass) {
        Query quizzesQuery = databaseReference.child("quizzes").child(teacherID)
                .child(selectedDate).child(selectedSubject).child(selectedClass);

        quizzesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    showQuizListDialog(teacherID, selectedDate, selectedSubject, selectedClass);
                } else {
//                    Toast.makeText(MCQViewQuizActivity.this, "No quizzes available in this class.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void showQuizListDialog(String teacherID, String selectedDate, String selectedSubject, String selectedClass) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_quiz_list, null);
        builder.setView(dialogView);

        ListView listViewQuizzes = dialogView.findViewById(R.id.listViewQuizzes);

        Query quizzesQuery = databaseReference.child("quizzes").child(teacherID)
                .child(selectedDate).child(selectedSubject).child(selectedClass);
        quizzesQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> quizTitles = new ArrayList<>();
                final List<String> quizIDs = new ArrayList<>();

                for (DataSnapshot quizSnapshot : dataSnapshot.getChildren()) {
                    String quizTitle = quizSnapshot.getKey();
                    quizTitles.add(quizTitle);
                    quizIDs.add(quizSnapshot.getKey());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(MCQViewQuizActivity.this,
                        android.R.layout.simple_list_item_1, quizTitles);
                listViewQuizzes.setAdapter(adapter);

                listViewQuizzes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String selectedQuizID = quizIDs.get(position);
                        showQuizDetailsDialog(teacherID, selectedDate, selectedSubject, selectedClass, selectedQuizID);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showQuizDetailsDialog(String teacherID, String selectedDate, String selectedSubject, String selectedClass, String selectedQuizID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_quiz_details, null);
        builder.setView(dialogView);

        TextView tvQuizDetails = dialogView.findViewById(R.id.tvQuizDetails);

        DatabaseReference quizReference = databaseReference.child("quizzes")
                .child(teacherID).child(selectedDate).child(selectedSubject).child(selectedClass).child(selectedQuizID);

        quizReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                StringBuilder quizDetails = new StringBuilder();

                // Display quiz information
                String quizKey = dataSnapshot.getKey();
                quizDetails.append("Quiz Key: ").append(quizKey).append("\n\n\n");

                // Display questions and their details
                for (DataSnapshot questionSnapshot : dataSnapshot.getChildren()) {
                    if (!questionSnapshot.getKey().equals("quizTitle") && !questionSnapshot.getKey().equals("quizDescription")) {
                        String question = questionSnapshot.child("question").getValue(String.class);
                        String optionA = questionSnapshot.child("A").getValue(String.class);
                        String optionB = questionSnapshot.child("B").getValue(String.class);
                        String optionC = questionSnapshot.child("C").getValue(String.class);
                        String optionD = questionSnapshot.child("D").getValue(String.class);
                        String correctOption = questionSnapshot.child("correctOption").getValue(String.class);

                        quizDetails.append("Question: ").append(question).append("\n");
                        quizDetails.append("A: ").append(optionA).append("\n");
                        quizDetails.append("B: ").append(optionB).append("\n");
                        quizDetails.append("C: ").append(optionC).append("\n");
                        quizDetails.append("D: ").append(optionD).append("\n");
                        quizDetails.append("Correct Option: ").append(correctOption).append("\n\n");
                    }
                }

                tvQuizDetails.setText(quizDetails.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
