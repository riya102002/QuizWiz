package com.utkarsh.quizwiz;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PublishQuizResultsActivity extends AppCompatActivity {

    private TextView resultLabelTextView;
    private ListView studentListView;
    private Button publishButton;
    private ArrayAdapter<String> studentListAdapter;
    private List<String> studentIds;

    private DatabaseReference studentResponsesRef, publishedResultsRef;
    private String selectedClass, selectedDate, selectedSubject, selectedQuizId, teacherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_quiz_results);

        selectedClass = getIntent().getStringExtra("selectedQuizId");
        selectedDate = getIntent().getStringExtra("selectedDate");
        selectedSubject = getIntent().getStringExtra("selectedSubject");
        selectedQuizId = getIntent().getStringExtra("selectedClass");
        teacherId = getIntent().getStringExtra("teacherId");

        resultLabelTextView = findViewById(R.id.resultLabelTextView);
        studentListView = findViewById(R.id.studentListView);
        publishButton = findViewById(R.id.publishButton);

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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });

        studentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Handle item click if needed
            }
        });

        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishQuizResults();
            }
        });

        publishedResultsRef = FirebaseDatabase.getInstance().getReference().child("PublishedResults");
    }

    private void publishQuizResults() {
        for (String studentId : studentIds) {
            DatabaseReference studentRef = studentResponsesRef.child(studentId);

            studentRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String resultFor = "ResultFor" + studentId;
                    String responseDateAndTime = dataSnapshot.child("responseDateAndTime").getValue(String.class);

                    for (DataSnapshot questionSnapshot : dataSnapshot.getChildren()) {
                        if (!questionSnapshot.getKey().equals("responseDateAndTime")) {
                            String questionId = questionSnapshot.getKey();
                            String selectedOption = questionSnapshot.child("selectedOption").getValue(String.class);
                            String correctOption = questionSnapshot.child("correctOption").getValue(String.class);

                            DatabaseReference responsesReff = FirebaseDatabase.getInstance().getReference("PublishedResults")
                                    .child(resultFor).child(teacherId).child(selectedDate).child(selectedSubject).child(selectedQuizId).child("responseDateAndTime");

                            Map<String, Object> questionData = new HashMap<>();
                            questionData.put("correctOption", correctOption);
                            questionData.put("selectedOption", selectedOption);
                            responsesReff.setValue(responseDateAndTime);

                            String resultPath = resultFor + "/" + teacherId + "/" + selectedDate + "/" + selectedSubject
                                     + "/" + selectedQuizId + "/" + questionId;

                            publishedResultsRef.child(resultPath).setValue(questionData);

                            Toast.makeText(PublishQuizResultsActivity.this, "Results Published Successfully!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error
                }
            });
        }
    }
}
