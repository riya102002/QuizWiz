package com.utkarsh.quizwiz;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizGraphicalStatsActivity extends AppCompatActivity {

    private ListView listView;
    private BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_graphical_stats);

        listView = findViewById(R.id.listView);
        barChart = findViewById(R.id.barChart);

        Intent intent = getIntent();
        String teacherId = intent.getStringExtra("teacherId");
        String selectedSubject = intent.getStringExtra("selectedSubject");
        String selectedClass = intent.getStringExtra("selectedClass");
        String selectedQuizId = intent.getStringExtra("selectedQuizId");
        String selectedDate = intent.getStringExtra("selectedDate");

        DatabaseReference responsesRef = FirebaseDatabase.getInstance()
                .getReference("ResponsesByStudents")
                .child(teacherId)
                .child(selectedDate)
                .child(selectedSubject)
                .child(selectedClass)
                .child(selectedQuizId);

        responsesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<BarEntry> entries = new ArrayList<>();
                List<String> labels = new ArrayList<>();
                List<String> studentInfoList = new ArrayList<>(); // List for student info

                int totalStudents = 0;
                int correctAnswers = 0;
                int incorrectAnswers = 0;

                for (DataSnapshot studentSnapshot : snapshot.getChildren()) {
                    totalStudents++;
                    String studentId = studentSnapshot.getKey();
                    String responseDateAndTime = studentSnapshot.child("responseDateAndTime").getValue(String.class);

                    // Add student info to the list
                    studentInfoList.add("Student ID: " + studentId + "\nDate and Time: " + responseDateAndTime);

                    for (DataSnapshot questionSnapshot : studentSnapshot.getChildren()) {
                        String selectedOption = questionSnapshot.child("selectedOption").getValue(String.class);
                        String correctOption = questionSnapshot.child("correctOption").getValue(String.class);
                        if (selectedOption != null && correctOption != null) {
                            if (selectedOption.equals(correctOption)) {
                                correctAnswers++;
                            } else {
                                incorrectAnswers++;
                            }
                        }
                    }
                }

                // Update the ListView with student info
                ArrayAdapter<String> adapter = new ArrayAdapter<>(QuizGraphicalStatsActivity.this, android.R.layout.simple_list_item_1, studentInfoList);
                listView.setAdapter(adapter);

                entries.add(new BarEntry(0, totalStudents));
                entries.add(new BarEntry(1, correctAnswers));
                entries.add(new BarEntry(2, incorrectAnswers));

                labels.add("Total Students");
                labels.add("Correct Answers");
                labels.add("Incorrect Answers");

                createBarChart(entries, labels);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void createBarChart(List<BarEntry> entries, List<String> labels) {
        int[] barColors = new int[]{Color.BLUE, Color.GREEN, Color.RED}; // Define colors

        // Create separate BarDataSet objects for each entry
        BarDataSet totalStudentsDataSet = new BarDataSet( Collections.singletonList(entries.get(0)), labels.get(0));
        totalStudentsDataSet.setColor(barColors[0]);

        BarDataSet correctAnswersDataSet = new BarDataSet(Collections.singletonList(entries.get(1)), labels.get(1));
        correctAnswersDataSet.setColor(barColors[1]);

        BarDataSet incorrectAnswersDataSet = new BarDataSet(Collections.singletonList(entries.get(2)), labels.get(2));
        incorrectAnswersDataSet.setColor(barColors[2]);

        // Create BarData and add BarDataSet objects to it
        BarData barData = new BarData(totalStudentsDataSet, correctAnswersDataSet, incorrectAnswersDataSet);

        barChart.setData(barData);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        barChart.getDescription().setEnabled(false);
        barChart.animateY(1000);
        barChart.invalidate();
    }
}
