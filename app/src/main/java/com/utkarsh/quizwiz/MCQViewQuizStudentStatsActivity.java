package com.utkarsh.quizwiz;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
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

public class MCQViewQuizStudentStatsActivity extends AppCompatActivity {

    private Spinner teacherSpinner, dateSpinner, subjectSpinner;
    private ListView statsListView;
    private ArrayAdapter<String> statsListAdapter;
    private List<String> statsList;
    LinearLayout teacherLayout, dateLayout, subjectLayout, noDataLayout;
    private DatabaseReference publishedResultsRef;
    private String studentId, studentClass;
    private ImageView emptyStateImageView;
    private TextView emptyStateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mcqview_quiz_student_stats);

        studentId = getIntent().getStringExtra("studentID");
        studentClass = getIntent().getStringExtra("studentClass");

        emptyStateImageView = findViewById(R.id.noDataImageView);
        emptyStateTextView = findViewById(R.id.noDataTextView);

        teacherLayout = findViewById(R.id.teacherLayout);
        dateLayout = findViewById(R.id.dateLayout);
        subjectLayout = findViewById(R.id.subjectLayout);

        teacherSpinner = findViewById(R.id.teacherSpinner);
        dateSpinner = findViewById(R.id.dateSpinner);
        subjectSpinner = findViewById(R.id.subjectSpinner);
        statsListView = findViewById(R.id.statsListView);

        statsList = new ArrayList<>();
        statsListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, statsList);
        statsListView.setAdapter(statsListAdapter);

        publishedResultsRef = FirebaseDatabase.getInstance().getReference().child("PublishedResults").child("ResultFor"+studentId);

        loadTeacherSpinner();

        statsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedQuizId = statsList.get(position);
                showQuizDetailsPopup(selectedQuizId, teacherSpinner.getSelectedItem().toString(),
                        dateSpinner.getSelectedItem().toString(), subjectSpinner.getSelectedItem().toString());
            }
        });
    }

    private void showQuizDetailsPopup(String quizId, String teacherId, String selectedDate, String selectedSubject) {
        DatabaseReference quizRef = publishedResultsRef.child(teacherId).child(selectedDate)
                .child(selectedSubject).child(quizId);

        quizRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MCQViewQuizStudentStatsActivity.this);
                builder.setTitle("["+quizId+"] Quiz Result");

                LayoutInflater inflater = LayoutInflater.from(MCQViewQuizStudentStatsActivity.this);
                View popupView = inflater.inflate(R.layout.popup_details_quiz, null);

                TextView responseDateTextView = popupView.findViewById(R.id.responseDateTextView);
                TextView scoreTextView = popupView.findViewById(R.id.scoreTextView);
                TextView percentageTextView = popupView.findViewById(R.id.percentageTextView); // New TextView
                ListView questionsListView = popupView.findViewById(R.id.questionsListView);

                String responseDateAndTime = dataSnapshot.child("responseDateAndTime").getValue(String.class);
                responseDateTextView.setText("Response Date and Time: " + responseDateAndTime);

                List<String> questionDetails = new ArrayList<>();
                int correctCount = 0;

                for (DataSnapshot questionSnapshot : dataSnapshot.getChildren()) {
                    String questionId = questionSnapshot.getKey();
                    if (!questionId.equals("responseDateAndTime")) {
                        String selectedOption = questionSnapshot.child("selectedOption").getValue(String.class);
                        String correctOption = questionSnapshot.child("correctOption").getValue(String.class);

                        String correctness = selectedOption.equals(correctOption) ? "Correct" : "Incorrect";
                        questionDetails.add("Question ID: " + questionId + "\n"
                                + "Selected Option: " + selectedOption + "\n"
                                + "Correct Option: " + correctOption + "\n"
                                + "Status: " + correctness);

                        if (correctness.equals("Correct")) {
                            correctCount++;
                        }
                    }
                }

                int totalQuestions = questionDetails.size();
                double percentageScore = (double) correctCount / totalQuestions * 100;

                scoreTextView.setText("Score: " + correctCount + "/" + totalQuestions);
                percentageTextView.setText("Percentage: " + String.format("%.2f", percentageScore) + "%");

                ArrayAdapter<String> questionAdapter = new ArrayAdapter<>(MCQViewQuizStudentStatsActivity.this,
                        android.R.layout.simple_list_item_1, questionDetails);
                questionsListView.setAdapter(questionAdapter);

                builder.setView(popupView);
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


    private void loadTeacherSpinner() {
        Query teacherIdsQuery = publishedResultsRef.orderByKey();
        teacherIdsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> teacherIds = new ArrayList<>();
                for (DataSnapshot teacherSnapshot : dataSnapshot.getChildren()) {
                    String teacherId = teacherSnapshot.getKey();
                    teacherIds.add(teacherId);
                }

                if (teacherIds.isEmpty()) {

                    teacherLayout.setVisibility(View.GONE);
                    dateLayout.setVisibility(View.GONE);
                    subjectLayout.setVisibility(View.GONE);
                    statsListView.setVisibility(View.GONE);
                    emptyStateImageView.setVisibility(View.VISIBLE);
                    emptyStateTextView.setVisibility(View.VISIBLE);

                } else {

                    teacherLayout.setVisibility(View.VISIBLE);
                    dateLayout.setVisibility(View.VISIBLE);
                    subjectLayout.setVisibility(View.VISIBLE);
                    statsListView.setVisibility(View.VISIBLE);
                    emptyStateImageView.setVisibility(View.GONE);
                    emptyStateTextView.setVisibility(View.GONE);

                    ArrayAdapter<String> teacherAdapter = new ArrayAdapter<>(MCQViewQuizStudentStatsActivity.this,
                            android.R.layout.simple_spinner_item, teacherIds);
                    teacherAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    teacherSpinner.setAdapter(teacherAdapter);
                    teacherSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            String selectedTeacherId = teacherIds.get(position);
                            loadDateSpinner(selectedTeacherId);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void loadDateSpinner(String teacherId) {
        Query dateQuery = publishedResultsRef.child(teacherId).orderByKey();
        dateQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> dates = new ArrayList<>();
                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                    String date = dateSnapshot.getKey();
                    dates.add(date);
                }
                ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(MCQViewQuizStudentStatsActivity.this,
                        android.R.layout.simple_spinner_item, dates);
                dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                dateSpinner.setAdapter(dateAdapter);
                dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selectedDate = dates.get(position);
                        loadSubjectSpinner(teacherId, selectedDate);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void loadSubjectSpinner(String teacherId, String selectedDate) {
        Query subjectQuery = publishedResultsRef.child(teacherId).child(selectedDate).orderByKey();
        subjectQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> subjects = new ArrayList<>();
                for (DataSnapshot subjectSnapshot : dataSnapshot.getChildren()) {
                    String subject = subjectSnapshot.getKey();
                    subjects.add(subject);
                }
                ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(MCQViewQuizStudentStatsActivity.this,
                        android.R.layout.simple_spinner_item, subjects);
                subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                subjectSpinner.setAdapter(subjectAdapter);
                subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selectedSubject = subjects.get(position);
                        loadStudentStats(teacherId, selectedDate, selectedSubject);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void loadStudentStats(String teacherId, String selectedDate, String selectedSubject) {
        Query statsQuery = publishedResultsRef.child(teacherId).child(selectedDate).child(selectedSubject).orderByKey();
        statsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                statsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String quizId = snapshot.getKey();
//                    String correctOption = snapshot.child("correctOption").getValue(String.class);
//                    String selectedOption = snapshot.child("selectedOption").getValue(String.class);
//                    String responseDateAndTime = snapshot.child("responseDateAndTime").getValue(String.class);
//
//                    String correctness = selectedOption.equals(correctOption) ? "Correct" : "Incorrect";
//
//                    String stat = "Question ID: " + questionId + "\n"
//                            + "Selected Option: " + selectedOption + "\n"
//                            + "Correct Option: " + correctOption + "\n"
//                            + "Status: " + correctness + "\n"
//                            + "Response Date and Time: " + responseDateAndTime + "\n\n";

                    statsList.add(quizId);
                }
                statsListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }
}
