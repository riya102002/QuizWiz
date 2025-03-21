package com.utkarsh.quizwiz;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.List;

public class MCQViewQuizStatsActivity extends AppCompatActivity {

    private Spinner quizDateSpinner;
    private Spinner subjectSpinner;
    private ListView quizIdListView;
    private ListView classQuizIdListView;

    private LinearLayout linearLayout;

    private String teacherID, teacherName;
    private List<String> quizDatesList;
    private ArrayAdapter<String> quizDatesAdapter;
    private List<String> subjectsList;
    private ArrayAdapter<String> subjectsAdapter;
    private ArrayAdapter<String> quizIdsAdapter;
    private List<String> quizIdsList;
    private List<String> classQuizIdsList;
    private ArrayAdapter<String> classQuizIdsAdapter;
    ImageView noDataImageView;
    TextView noDataTextView;
    LinearLayout selectClassLayout, dateSpinnerLayout, subjectSpinnerLayout, QuizLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mcqview_quiz_stats);

        selectClassLayout = findViewById(R.id.selectClassLayout);
        dateSpinnerLayout = findViewById(R.id.dateSpinnerLayout);
        subjectSpinnerLayout = findViewById(R.id.subjectSpinnerLayout);
        QuizLayout = findViewById(R.id.QuizLayout);

        quizDateSpinner = findViewById(R.id.quizDateSpinner);
        subjectSpinner = findViewById(R.id.subjectSpinner);
        quizIdListView = findViewById(R.id.quizIdListView);
        classQuizIdListView = findViewById(R.id.classQuizIdListView);
        linearLayout = findViewById(R.id.QuizLayout);

        teacherID = getIntent().getStringExtra("teacherID");
        teacherName = getIntent().getStringExtra("teacherName");
        teacherID = teacherID + "-->" + teacherName;

        quizDatesList = new ArrayList<>();
        quizDatesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, quizDatesList);
        quizDatesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        quizDateSpinner.setAdapter(quizDatesAdapter);

        // Find the ImageView and TextView for no data
        noDataImageView = findViewById(R.id.noDataImageView);
        noDataTextView = findViewById(R.id.noDataTextView);

        subjectsList = new ArrayList<>();
        subjectsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subjectsList);
        subjectsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectSpinner.setAdapter(subjectsAdapter);

        quizIdsList = new ArrayList<>();
        quizIdsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, quizIdsList);
        quizIdListView.setAdapter(quizIdsAdapter);

        classQuizIdsList = new ArrayList<>();
        classQuizIdsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, classQuizIdsList);
        classQuizIdListView.setAdapter(classQuizIdsAdapter);

        loadQuizDates();

        quizDateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedDate = quizDateSpinner.getSelectedItem().toString();
                loadSubjects(selectedDate);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedDate = quizDateSpinner.getSelectedItem().toString();
                String selectedSubject = subjectSpinner.getSelectedItem().toString();
                loadQuizClasses(selectedDate, selectedSubject);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        quizIdListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedQuizId = quizIdsList.get(position);
                String selectedSubject = subjectSpinner.getSelectedItem().toString();
                String selectedDate = quizDateSpinner.getSelectedItem().toString();
                loadClassQuizIds(selectedDate, selectedSubject, selectedQuizId);
                linearLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    private void loadQuizDates() {
        DatabaseReference responsesRef = FirebaseDatabase.getInstance().getReference("ResponsesByStudents")
                .child(teacherID);
        responsesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                quizDatesList.clear();
                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                    quizDatesList.add(dateSnapshot.getKey());
                }
                quizDatesAdapter.notifyDataSetChanged();

                // Show/hide views based on data presence
                if (quizDatesList.isEmpty()) {
                    dateSpinnerLayout.setVisibility(View.GONE);
                    QuizLayout.setVisibility(View.GONE);
                    selectClassLayout.setVisibility(View.GONE);
                    subjectSpinnerLayout.setVisibility(View.GONE);
//                    linearLayout.setVisibility(View.GONE);
                    noDataImageView.setVisibility(View.VISIBLE);
                    noDataTextView.setVisibility(View.VISIBLE);
                } else {
                    dateSpinnerLayout.setVisibility(View.VISIBLE);
                    QuizLayout.setVisibility(View.VISIBLE);
                    selectClassLayout.setVisibility(View.VISIBLE);
                    subjectSpinnerLayout.setVisibility(View.VISIBLE);
//                    linearLayout.setVisibility(View.VISIBLE);
                    noDataImageView.setVisibility(View.GONE);
                    noDataTextView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void loadSubjects(String selectedDate) {
        DatabaseReference responsesRef = FirebaseDatabase.getInstance().getReference("ResponsesByStudents")
                .child(teacherID).child(selectedDate);
        responsesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                subjectsList.clear();
                for (DataSnapshot subjectSnapshot : dataSnapshot.getChildren()) {
                    subjectsList.add(subjectSnapshot.getKey());
                }
                subjectsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void loadQuizClasses(String selectedDate, String selectedSubject) {
        DatabaseReference responsesRef = FirebaseDatabase.getInstance().getReference("ResponsesByStudents")
                .child(teacherID).child(selectedDate).child(selectedSubject);
        responsesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                quizIdsList.clear();
                for (DataSnapshot quizIdSnapshot : dataSnapshot.getChildren()) {
                    quizIdsList.add(quizIdSnapshot.getKey());
                }
                quizIdsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void loadClassQuizIds(String selectedDate, String selectedSubject, String selectedQuizId) {
        DatabaseReference responsesRef = FirebaseDatabase.getInstance().getReference("ResponsesByStudents")
                .child(teacherID).child(selectedDate).child(selectedSubject).child(selectedQuizId);
        responsesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                classQuizIdsList.clear();
                for (DataSnapshot classSnapshot : dataSnapshot.getChildren()) {
                    classQuizIdsList.add(classSnapshot.getKey());
                }
                classQuizIdsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });

        // Show the popup options when a quiz ID is selected
        classQuizIdListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedClass = classQuizIdsList.get(position);
                showPopupOptions(selectedDate, selectedSubject, selectedQuizId, selectedClass);
            }
        });
    }

    private void showPopupOptions(String selectedDate, String selectedSubject, String selectedQuizId, String selectedClass) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_quiz_options, null);
        builder.setView(dialogView);

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        dialogTitle.setText("Select an Option");

        Button btnOverallStats = dialogView.findViewById(R.id.btnOverallStats);
        Button btnStudentStats = dialogView.findViewById(R.id.btnStudentStats);
        Button btnPublishQuizResults = dialogView.findViewById(R.id.btnPusblishQuizResults);

        AlertDialog dialog = builder.create();
        dialog.show();

        btnOverallStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent(MCQViewQuizStatsActivity.this, ShowOverallQuizStatsActivity.class);
                intent.putExtra("selectedClass", selectedClass);
                intent.putExtra("selectedDate", selectedDate);
                intent.putExtra("selectedSubject", selectedSubject);
                intent.putExtra("selectedQuizId", selectedQuizId);
                intent.putExtra("teacherId", teacherID);
                startActivity(intent);
//                showOverallQuizStats(selectedDate, selectedSubject, selectedQuizId, selectedClass);
            }
        });

        btnStudentStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent(MCQViewQuizStatsActivity.this, ShowStudentWiseQuizStatsActivity.class);
                intent.putExtra("selectedClass", selectedClass);
                intent.putExtra("selectedDate", selectedDate);
                intent.putExtra("selectedSubject", selectedSubject);
                intent.putExtra("selectedQuizId", selectedQuizId);
                intent.putExtra("teacherId", teacherID);
                startActivity(intent);
//                showStudentSpecificStats(selectedDate, selectedSubject, selectedQuizId, selectedClass);
            }
        });

        btnPublishQuizResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent(MCQViewQuizStatsActivity.this, PublishQuizResultsActivity.class);
                intent.putExtra("selectedClass", selectedClass);
                intent.putExtra("selectedDate", selectedDate);
                intent.putExtra("selectedSubject", selectedSubject);
                intent.putExtra("selectedQuizId", selectedQuizId);
                intent.putExtra("teacherId", teacherID);
                startActivity(intent);
//                showStudentSpecificStats(selectedDate, selectedSubject, selectedQuizId, selectedClass);
            }
        });
    }
}
