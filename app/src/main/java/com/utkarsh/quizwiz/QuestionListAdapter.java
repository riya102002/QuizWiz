package com.utkarsh.quizwiz;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.EditText;

import java.util.List;

public class QuestionListAdapter extends BaseAdapter {

    private Context context;
    private List<View> questionLayouts;
    private RemoveClickListener removeClickListener;

    public QuestionListAdapter(Context context, List<View> questionLayouts, RemoveClickListener removeClickListener) {
        this.context = context;
        this.questionLayouts = questionLayouts;
        this.removeClickListener = removeClickListener;
    }

    @Override
    public int getCount() {
        return questionLayouts.size();
    }

    @Override
    public Object getItem(int position) {
        return questionLayouts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View questionEntryLayout = questionLayouts.get(position);

        ImageView btnRemoveQuestion = questionEntryLayout.findViewById(R.id.btnRemoveQuestion);
        // Handle remove button click
        btnRemoveQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeClickListener.onRemoveClick(questionEntryLayout);
            }
        });

        return questionEntryLayout;
    }

    public interface RemoveClickListener {
        void onRemoveClick(View questionEntryLayout);
    }
}
