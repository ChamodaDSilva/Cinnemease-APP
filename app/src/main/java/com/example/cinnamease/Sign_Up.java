package com.example.cinnamease;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Sign_Up extends AppCompatActivity {

    EditText editTextName;
    EditText editTextEmail;
    EditText editTextPassword;
    Button btnSignUp;
    Button btnSignIn;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize the ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("ලියාපදිංචි වීම සිදුවෙමින් පවතී...");
        progressDialog.setCancelable(false);

        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        btnSignIn = findViewById(R.id.buttonSigIn1);
        btnSignUp = findViewById(R.id.buttonSignUp);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });

        btnSignIn.setOnClickListener(view -> {
            Intent intent = new Intent(Sign_Up.this, Sign_In.class);
            startActivity(intent);
            finish(); // Prevent going back to splash screen when pressing back button
        });
    }

    private void signUp() {
        // Show the progress dialog
        progressDialog.show();

        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        OkHttpClient client = new OkHttpClient();

        // JSON body
        MediaType mediaType = MediaType.parse("application/json");
        String jsonBody = "{\"name\": \"" + name + "\", \"email\": \"" + email + "\", \"password\": \"" + password + "\"}";
        RequestBody requestBody = RequestBody.create(jsonBody, mediaType);

        // Request
        Request request = new Request.Builder()
                .url("https://chamodadesilva-cinnamease-api.hf.space/signup")
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build();

        // Execute the request asynchronously
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                // Dismiss the progress dialog
                progressDialog.dismiss();
                // Handle response
                String responseBody = response.body().string();
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String message = jsonResponse.getString("message");
                        // Check the message from the server
                        if (message.equals("User signed up successfully")) {
                            Intent intent = new Intent(Sign_Up.this, MainActivity.class);
                            startActivity(intent);
                            finish(); // Prevent going back to splash screen when pressing back button
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(Sign_Up.this, "පරිශීලක දැනටමත් පවතීී", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String error = jsonResponse.getString("error");
                        // Check the error message from the server
                        if (error.equals("පරිශීලක දැනටමත් පවතී")) {
                            // Handle user already exists error
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(Sign_Up.this, "User with this email already exists", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            // Handle other error messages
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                // Dismiss the progress dialog
                progressDialog.dismiss();
                // Handle failure
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Sign_Up.this, "Sign up failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
