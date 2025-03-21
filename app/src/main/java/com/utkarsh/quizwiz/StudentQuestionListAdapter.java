package com.utkarsh.quizwiz;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class StudentQuestionListAdapter extends ArrayAdapter<QuestionData> {

    public interface OptionSelectedListener {
        void onOptionSelected(int position, String selectedOption);

        void onOptionSelected(String questionID, String selectedOption);
    }

    private OptionSelectedListener optionSelectedListener;

    private List<QuestionData> questionDataList;
    private LayoutInflater inflater;

    public StudentQuestionListAdapter(Context context, List<QuestionData> questionDataList, OptionSelectedListener optionSelectedListener) {
        super(context, 0, questionDataList);
        this.questionDataList = questionDataList;
        this.inflater = LayoutInflater.from(context);
        this.optionSelectedListener = optionSelectedListener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.question_item_layout, parent, false);
        }

        QuestionData questionData = questionDataList.get(position);

        TextView questionIdTextView = view.findViewById(R.id.questionIdTextView);
        TextView questionTextView = view.findViewById(R.id.questionTextView);
        RadioGroup radioGroupOptions = view.findViewById(R.id.radioGroupOptions);
        RadioButton optionARadio = view.findViewById(R.id.optionARadio);
        RadioButton optionBRadio = view.findViewById(R.id.optionBRadio);
        RadioButton optionCRadio = view.findViewById(R.id.optionCRadio);
        RadioButton optionDRadio = view.findViewById(R.id.optionDRadio);

        questionIdTextView.setText("Question ID: " + questionData.getQuestionID());
        questionTextView.setText(questionData.getQuestion());
        optionARadio.setText(questionData.getOptionA());
        optionBRadio.setText(questionData.getOptionB());
        optionCRadio.setText(questionData.getOptionC());
        optionDRadio.setText(questionData.getOptionD());

        radioGroupOptions.clearCheck();

        radioGroupOptions.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                String selectedOption = null;
                switch (checkedId) {
                    case R.id.optionARadio:
                        selectedOption = "A";
                        break;
                    case R.id.optionBRadio:
                        selectedOption = "B";
                        break;
                    case R.id.optionCRadio:
                        selectedOption = "C";
                        break;
                    case R.id.optionDRadio:
                        selectedOption = "D";
                        break;
                }
                if (selectedOption != null) {
                    optionSelectedListener.onOptionSelected(position, selectedOption);
                }
            }
        });

        return view;
    }
}