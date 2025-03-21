package com.utkarsh.quizwiz;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentQuizActivity extends AppCompatActivity {

    private Spinner teacherSpinner;
    private Spinner dateSpinner;
    private ListView subjectListView;
    private Map<String, List<String>> teacherQuizDatesMap;
    private Map<String, Map<String, List<String>>> dateSubjectMap;
    private String studentID, studentClass;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_quiz);

        teacherSpinner = findViewById(R.id.teacherSpinner);
        dateSpinner = findViewById(R.id.dateSpinner);
        subjectListView = findViewById(R.id.subjectListView);

        studentID = getIntent().getStringExtra("studentID");

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("students").child(studentID);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    studentClass = dataSnapshot.child("studentClass").getValue(String.class);
                } else {
                    // Student with given ID not found in the database
                    Toast.makeText(StudentQuizActivity.this, "Class Not Found for the given Student!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(StudentQuizActivity.this, "Failed to retrieve student class", Toast.LENGTH_SHORT).show();
            }
        });

        teacherQuizDatesMap = new HashMap<>();
        dateSubjectMap = new HashMap<>();

        FirebaseDatabase.getInstance().getReference("quizzes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> teacherIDs = new ArrayList<>();
                for (DataSnapshot teacherSnapshot : dataSnapshot.getChildren()) {
                    String teacherID = teacherSnapshot.getKey();
                    teacherIDs.add(teacherID);

                    List<String> quizDates = new ArrayList<>();
                    Map<String, List<String>> dateSubjectListMap = new HashMap<>();
                    for (DataSnapshot dateSnapshot : teacherSnapshot.getChildren()) {
                        String quizDate = dateSnapshot.getKey();
                        quizDates.add(quizDate);

                        List<String> subjects = new ArrayList<>();
                        for (DataSnapshot subjectSnapshot : dateSnapshot.getChildren()) {
                            String subject = subjectSnapshot.getKey();
                            subjects.add(subject);
                        }
                        dateSubjectListMap.put(quizDate, subjects);
                    }
                    teacherQuizDatesMap.put(teacherID, quizDates);
                    dateSubjectMap.put(teacherID, dateSubjectListMap);
                }

                ArrayAdapter<String> teacherAdapter = new ArrayAdapter<>(StudentQuizActivity.this, android.R.layout.simple_spinner_item, teacherIDs);
                teacherAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                teacherSpinner.setAdapter(teacherAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });

        teacherSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedTeacherID = parent.getItemAtPosition(position).toString();
                List<String> quizDates = teacherQuizDatesMap.get(selectedTeacherID);
                ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(StudentQuizActivity.this, android.R.layout.simple_spinner_item, quizDates);
                dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                dateSpinner.setAdapter(dateAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedTeacherID = teacherSpinner.getSelectedItem().toString();
                String selectedDate = parent.getItemAtPosition(position).toString();
                updateSubjectListView(selectedTeacherID, selectedDate);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        subjectListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedTeacherID = teacherSpinner.getSelectedItem().toString();
                String selectedDate = dateSpinner.getSelectedItem().toString();
                String selectedSubject = parent.getItemAtPosition(position).toString();
                String selectedClass = studentClass;

                DatabaseReference quizzesRef = FirebaseDatabase.getInstance().getReference("quizzes")
                        .child(selectedTeacherID).child(selectedDate).child(selectedSubject).child("Class:" + selectedClass);

                quizzesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<String> quizIDs = new ArrayList<>();
                        for (DataSnapshot quizSnapshot : dataSnapshot.getChildren()) {
                            String quizID = quizSnapshot.getKey();
                            quizIDs.add(quizID);
                        }

                        if (!quizIDs.isEmpty()) {
                            showQuizIDsDialog(quizIDs, selectedTeacherID, selectedDate, selectedSubject);
                        } else {
                            Toast.makeText(StudentQuizActivity.this, "No quizzes available for selected subject and class.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error
                    }
                });
            }
        });
    }

    private void updateSubjectListView(String selectedTeacherID, String selectedDate) {
        List<String> subjects = dateSubjectMap.get(selectedTeacherID).get(selectedDate);
        List<String> subjectsWithQuizzes = new ArrayList<>();

        for (String subject : subjects) {
            DatabaseReference quizzesRef = FirebaseDatabase.getInstance().getReference("quizzes")
                    .child(selectedTeacherID).child(selectedDate).child(subject).child("Class:" + studentClass);

            quizzesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        subjectsWithQuizzes.add(subject);
                        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(StudentQuizActivity.this, android.R.layout.simple_list_item_1, subjectsWithQuizzes);
                        subjectListView.setAdapter(subjectAdapter);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error
                }
            });
        }
    }

    private void showQuizIDsDialog(List<String> quizIDs, String selectedTeacherID, String selectedDate, String selectedSubject) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.student_dialog_quiz_ids, null);
        builder.setView(dialogView);

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        dialogTitle.setText("Select Quiz ID");

        ListView listViewQuizIDs = dialogView.findViewById(R.id.listViewQuizIDs);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, quizIDs);
        listViewQuizIDs.setAdapter(adapter);

        listViewQuizIDs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedQuizID = quizIDs.get(position);

                Intent intent = new Intent(StudentQuizActivity.this, DisplayStudentQuizActivity.class);
                intent.putExtra("teacherID", selectedTeacherID);
                intent.putExtra("date", selectedDate);
                intent.putExtra("subject", selectedSubject);
                intent.putExtra("class", studentClass);
                intent.putExtra("quizID", selectedQuizID);
                intent.putExtra("studentID", studentID);
                startActivity(intent);
            }
        });

        builder.show();
    }

}