package com.utkarsh.quizwiz;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MCQQuizCreationActivity extends AppCompatActivity {

    private ListView listViewQuestions;
    private Button btnAddQuestion;
    private Button btnSave;

    private static final String[] OPTIONS = {"A", "B", "C", "D"};

    private List<View> questionLayouts = new ArrayList<>();
    private QuestionListAdapter adapter;

    private DatabaseReference databaseReference;
    EditText etSubject;
    EditText etClass;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mcqquiz_creation);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        listViewQuestions = findViewById(R.id.listViewQuestions);
        btnAddQuestion = findViewById(R.id.btnAddQuestion);
        btnSave = findViewById(R.id.btnSave);
        etSubject = findViewById(R.id.etSubject);
        etClass = findViewById(R.id.etClass);


        adapter = new QuestionListAdapter(this, questionLayouts, new QuestionListAdapter.RemoveClickListener() {
            @Override
            public void onRemoveClick(View questionEntryLayout) {
                removeQuestionLayout(questionEntryLayout);
            }
        });
        listViewQuestions.setAdapter(adapter);

        btnAddQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addQuestionEntryLayout();
                adapter.notifyDataSetChanged();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveQuizData();
            }
        });
    }

    private void removeQuestionLayout(View questionEntryLayout) {
        questionLayouts.remove(questionEntryLayout);
        adapter.notifyDataSetChanged();
    }

    private void addQuestionEntryLayout() {
        LayoutInflater inflater = LayoutInflater.from(this);

        EditText etTotalQuestions = findViewById(R.id.etTotalQuestions);
        String totalQuestionsStr = etTotalQuestions.getText().toString().trim();

        if (!totalQuestionsStr.isEmpty()) {
            int totalQuestions = Integer.parseInt(totalQuestionsStr);

            for (int i = 0; i < totalQuestions; i++) {
                View questionEntryLayout = inflater.inflate(R.layout.question_entry_layout, null);
                ImageView btnRemoveQuestion = questionEntryLayout.findViewById(R.id.btnRemoveQuestion);

                Spinner spinnerCorrectOption = questionEntryLayout.findViewById(R.id.spinnerCorrectOption);
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, OPTIONS);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCorrectOption.setAdapter(spinnerAdapter);

                btnRemoveQuestion.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        questionLayouts.remove(questionEntryLayout);
                        adapter.notifyDataSetChanged();
                    }
                });

                questionLayouts.add(questionEntryLayout);
            }

            adapter.notifyDataSetChanged(); // Notify adapter about the data change
        }
    }

    private void saveQuizData() {
        String teacherID = getIntent().getStringExtra("teacherID");
        String teacherName = getIntent().getStringExtra("teacherName");
        String currentDate = getCurrentDate();

        DatabaseReference quizReference = databaseReference.child("quizzes")
                .child(teacherID + "-->" + teacherName)
                .child(currentDate);

        String subject = etSubject.getText().toString().trim();

        for (View questionEntryLayout : questionLayouts) {
            EditText etQuestion = questionEntryLayout.findViewById(R.id.etQuestion);
            EditText etOptionA = questionEntryLayout.findViewById(R.id.etOptionA);
            EditText etOptionB = questionEntryLayout.findViewById(R.id.etOptionB);
            EditText etOptionC = questionEntryLayout.findViewById(R.id.etOptionC);
            EditText etOptionD = questionEntryLayout.findViewById(R.id.etOptionD);
            Spinner spinnerCorrectOption = questionEntryLayout.findViewById(R.id.spinnerCorrectOption);
            String correctOption = spinnerCorrectOption.getSelectedItem().toString();

            String question = etQuestion.getText().toString().trim();
            String optionA = etOptionA.getText().toString().trim();
            String optionB = etOptionB.getText().toString().trim();
            String optionC = etOptionC.getText().toString().trim();
            String optionD = etOptionD.getText().toString().trim();

            if (!question.isEmpty() && !optionA.isEmpty() && !optionB.isEmpty() && !optionC.isEmpty()
                    && !optionD.isEmpty() && !correctOption.isEmpty()) {

                DatabaseReference subjectReference = quizReference.child(subject).child("Class:" + etClass.getText().toString());

                String questionID = subjectReference.push().getKey(); // Generate a unique question ID

                Map<String, String> questionData = new HashMap<>();
                questionData.put("question", question);
                questionData.put("A", optionA);
                questionData.put("B", optionB);
                questionData.put("C", optionC);
                questionData.put("D", optionD);
                questionData.put("correctOption", correctOption);
                questionData.put("questionID", questionID);

                String quizID = currentDate+" -> "+getCurrentTime(); // Get current time in the format hh_mm_ss

                subjectReference.child(quizID).child(questionID).setValue(questionData); // Store the data with the unique question ID
            }
        }

        Toast.makeText(this, "Quiz data saved successfully!", Toast.LENGTH_SHORT).show();
    }

    private String getCurrentTime() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return timeFormat.format(new Date());
    }

    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(new Date());
    }
}
