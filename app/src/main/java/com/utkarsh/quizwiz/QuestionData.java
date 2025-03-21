package com.utkarsh.quizwiz;

public class QuestionData {
    private String questionID;
    private String question;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String selectedOption;


    public QuestionData(String questionID, String question, String optionA, String optionB, String optionC, String optionD) {
        this.questionID = questionID;
        this.question = question;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
    }

    public String getQuestionID() {
        return questionID;
    }

    public String getQuestion() {
        return question;
    }

    public String getOptionA() {
        return optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public String getSelectedOption() {
        return selectedOption;
    }

    public void setSelectedOption(String selectedOption) {
        this.selectedOption = selectedOption;
    }
}
