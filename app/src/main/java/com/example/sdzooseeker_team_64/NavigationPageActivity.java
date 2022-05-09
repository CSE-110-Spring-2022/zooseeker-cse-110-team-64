package com.example.sdzooseeker_team_64;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class NavigationPageActivity extends AppCompatActivity {
    private int currentExhibitIndex = 0;
    private ArrayList<String> exhibitsList = new ArrayList<>();

    private TextView directionTextView;
    private Button prevButton;
    private Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_page);

        currentExhibitIndex = 0;
        exhibitsList.add("String1");
        exhibitsList.add("String2");
        directionTextView = findViewById(R.id.direction_textView);

        // Set up direction text view for the first exhibit on the sorted list
        // Get the direction text for previous exhibit
        String firstDirectionText = "Set direction text for the first exhibit on the sorted list";
        directionTextView.setText(firstDirectionText);

        // Prepare for buttons
        prevButton = findViewById(R.id.previous_btn);
        nextButton = findViewById(R.id.next_btn);
        updateButtonStates();
    }

    public void onPreviousBtnClicked(View view) {
        // Get the direction text for previous exhibit

        String directionText = "Set direction text for the previous exhibit on the sorted list";

        // Show direction for the previous exhibit on the sorted list
        directionTextView.setText(directionText);
        currentExhibitIndex--;
        updateButtonStates();
    }

    public void onNextBtnClicked(View view) {
        if (isAtLastExhibit()) {
            // The button should finish and dismiss the direction avtivity.
            finish();
        } else {
            // Get the direction text for previous exhibit

            String directionText = "Set direction text for the next exhibit on the sorted list";

            // Show direction for the next exhibit on the sorted list
            directionTextView.setText(directionText);
        }
        currentExhibitIndex++;
        updateButtonStates();
    }


    // Helper functions
    private void updateButtonStates() {
        // Initially the previous button shouldn't appear as there's no previous exhibit
        if (isAtFirstExhibit()) {
            // hide previous button
            prevButton.setAlpha(0);
        } else {
            prevButton.setAlpha(1);
        }

        // At the last exhibit, change the next button to finish as there's no next exhibit
        if (isAtLastExhibit()) {
            // change next button to finish
            nextButton.setText("FINISH");
        } else {
            nextButton.setText("NEXT");
        }
    }

    private boolean isAtFirstExhibit() {
        return currentExhibitIndex == 0;
    }

    private boolean isAtLastExhibit() {
        return currentExhibitIndex >= exhibitsList.size() - 1;
    }

}