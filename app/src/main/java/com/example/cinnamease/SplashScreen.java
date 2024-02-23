package com.example.cinnamease;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashScreen extends AppCompatActivity {

    private static final int SPLASH_SCREEN_DURATION = 1000; // 1 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start main activity after splash screen duration
                Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(intent);
                finish(); // Prevent going back to splash screen when pressing back button
            }
        }, SPLASH_SCREEN_DURATION);
    }
}