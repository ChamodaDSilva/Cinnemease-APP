package com.example.cinnamease;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

public class Sign_In extends AppCompatActivity {

    Button buttonSignIn;
    EditText editTextEmail;
    EditText editTextPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        buttonSignIn = findViewById(R.id.buttonSignIn);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);

        buttonSignIn.setOnClickListener(view -> signIn());
    }

    private void signIn() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        OkHttpClient client = new OkHttpClient();

        // JSON body
        MediaType mediaType = MediaType.parse("application/json");
        String jsonBody = "{\"email\": \"" + email + "\", \"password\": \"" + password + "\"}";
        RequestBody requestBody = RequestBody.create(jsonBody, mediaType);

        // Request
        Request request = new Request.Builder()
                .url("https://chamodadesilva-cinnamease-api.hf.space/login")
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build();

        // Execute the request asynchronously
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                // Handle response
                String responseBody = response.body().string();
                if (response.isSuccessful()) {
                    if (responseBody.contains("User logged in successfully")) {
                        runOnUiThread(() -> {
                            //Toast.makeText(Sign_In.this, "Login successful", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(Sign_In.this, MainActivity.class);
                            startActivity(intent);
                            finish(); // Prevent going back to splash screen when pressing back button
                            // Proceed to the next activity or perform necessary actions
                        });
                    }else if (responseBody.contains("Invalid email or password")) {
                        runOnUiThread(() -> {
                            Toast.makeText(Sign_In.this, "Invalid email or password", Toast.LENGTH_LONG).show();
                            // Proceed to the next activity or perform necessary actions
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(Sign_In.this, "Unexpected response: " + responseBody, Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    if (responseBody.contains("Invalid email or password")) {
                        runOnUiThread(() -> {
                            Toast.makeText(Sign_In.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(Sign_In.this, "Login failed: " + responseBody, Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            }

            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                // Handle failure
                runOnUiThread(() -> {
                    Toast.makeText(Sign_In.this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
