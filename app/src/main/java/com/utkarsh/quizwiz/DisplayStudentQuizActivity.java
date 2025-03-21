package com.utkarsh.quizwiz;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DisplayStudentQuizActivity extends AppCompatActivity {

    private ListView questionsListView;
    private String teacherID, date, subject, studentClass, quizID, studentID;
    private Map<String, String> selectedOptionsMap = new HashMap<>();
    private Map<String, String> correctOptionsMap = new HashMap<>();
    private StudentQuestionListAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_student_quiz);

        questionsListView = findViewById(R.id.questionsListView);

        // Get data from intent
        teacherID = getIntent().getStringExtra("teacherID");
        date = getIntent().getStringExtra("date");
        subject = getIntent().getStringExtra("subject");
        studentClass = getIntent().getStringExtra("class");
        quizID = getIntent().getStringExtra("quizID");
        studentID = getIntent().getStringExtra("studentID");

        // Set the quiz ID heading
        TextView quizIdHeading = findViewById(R.id.quizIdHeading);
        quizIdHeading.setText("Quiz ID: " + quizID);

        DatabaseReference quizRef = FirebaseDatabase.getInstance().getReference("quizzes")
                .child(teacherID).child(date).child(subject).child("Class:" + studentClass)
                .child(quizID);

        quizRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<QuestionData> questionList = new ArrayList<>();
                for (DataSnapshot questionSnapshot : dataSnapshot.getChildren()) {
                    String questionText = (String) questionSnapshot.child("question").getValue();
                    String correctOption = (String) questionSnapshot.child("correctOption").getValue();
                    String optionA = (String) questionSnapshot.child("A").getValue();
                    String optionB = (String) questionSnapshot.child("B").getValue();
                    String optionC = (String) questionSnapshot.child("C").getValue();
                    String optionD = (String) questionSnapshot.child("D").getValue();
                    String questionID = (String) questionSnapshot.getKey(); // Get the question ID from the key

                    correctOptionsMap.put(questionID, correctOption);

                    QuestionData questionData = new QuestionData(questionID, questionText,
                            optionA, optionB, optionC, optionD);
                    questionList.add(questionData);
                }

                adapter = new StudentQuestionListAdapter(DisplayStudentQuizActivity.this, questionList,
                        new StudentQuestionListAdapter.OptionSelectedListener() {
                            @Override
                            public void onOptionSelected(int position, String selectedOption) {
                                selectedOptionsMap.put(questionList.get(position).getQuestionID(), selectedOption);
                            }

                            @Override
                            public void onOptionSelected(String questionID, String selectedOption) {
                                selectedOptionsMap.put(questionID, selectedOption);
                            }
                        });

                questionsListView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });

        Button submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitResponses();
            }
        });
    }

    private void submitResponses() {
        DatabaseReference responsesRef = FirebaseDatabase.getInstance().getReference("ResponsesByStudents")
                .child(teacherID).child(date).child(subject).child("Class:"+studentClass).child(quizID).child(studentID);

        // Get current date and time
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDateAndTime = dateFormat.format(new Date());

        Map<String, Object> studentResponseMap = new HashMap<>();
        studentResponseMap.put("submissionDateTime", currentDateAndTime);

        responsesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (Map.Entry<String, String> entry : selectedOptionsMap.entrySet()) {
                    String questionID = entry.getKey();
                    String selectedOption = entry.getValue();
                    String correctOption = correctOptionsMap.get(questionID);

                    // Check if response for this question already exists
                    if (dataSnapshot.child(questionID).exists()) {
                        Toast.makeText(DisplayStudentQuizActivity.this, "Response already submitted for Quiz ID: " + quizID, Toast.LENGTH_SHORT).show();
                        break;
                    } else {
                        DatabaseReference responsesReff = FirebaseDatabase.getInstance().getReference("ResponsesByStudents")
                                .child(teacherID).child(date).child(subject).child("Class:"+studentClass).child(quizID).child(studentID).child("responseDateAndTime");
                        // Submit response
                        Map<String, Object> responseMap = new HashMap<>();
                        responseMap.put("selectedOption", selectedOption);
                        responseMap.put("correctOption", correctOption);
                        responsesReff.setValue(currentDateAndTime);

                        responsesRef.child(questionID).setValue(responseMap);
                        Toast.makeText(DisplayStudentQuizActivity.this, "Responses submitted successfully", Toast.LENGTH_SHORT).show();
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
