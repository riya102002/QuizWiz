package com.utkarsh.quizwiz;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class SplashScreenActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 4500; // 2 seconds delay
    private static final int REQUEST_CODE_LINKEDIN = 1;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        ImageView gifImageView = findViewById(R.id.gifImageView);
        Glide.with(this)
                .asGif()
                .load(R.drawable.abcd) // Replace with your GIF resource
                .into(gifImageView);

        textView = findViewById(R.id.footerTextView);

//        textView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // Open the LinkedIn profile in a web browser
//                String linkedInProfileUrl = "https://www.linkedin.com/in/utkarshmi/";
//                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkedInProfileUrl));
//                startActivityForResult(intent, REQUEST_CODE_LINKEDIN);
//            }
//        });

        // Delayed execution to move to the next activity after the splash screen
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_LINKEDIN) {
            // Continue to the next activity
            Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close the splash screen activity
        }
    }
}