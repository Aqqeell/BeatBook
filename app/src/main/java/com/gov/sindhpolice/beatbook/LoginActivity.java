package com.gov.sindhpolice.beatbook;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.gov.sindhpolice.beatbook.databinding.ActivityLoginBinding;
import com.gov.sindhpolice.beatbook.models.GeneralModel;
import com.gov.sindhpolice.beatbook.utils.SharedPrefManager;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private RequestQueue requestQueue;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up data binding
        ActivityLoginBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        // Set default text for username and password
        binding.etUsername.getEditText().setText("admin@beatbook.com");
        binding.etPassword.getEditText().setText("admin123");

        // Initialize Volley request queue
        requestQueue = Volley.newRequestQueue(this);

        // Set up the button click listener using binding
        binding.btnLogin.setOnClickListener(view -> {
            String email = binding.etUsername.getEditText().getText().toString().trim();
            String password = binding.etPassword.getEditText().getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                // Call the login API using Volley
                loginUser(email, password);
            } else {
                Toast.makeText(LoginActivity.this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void loginUser(String email, String password) {
        String url = GeneralModel.API_URL + "login"; // API URL

        // Check network availability
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare the data in JSON format for the API request
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("email", email);
            requestBody.put("password", password);
            Log.d(TAG, "Request Body: " + requestBody); // Log the request body
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating request body", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a JSON Object Request with POST method
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                response -> {
                    // Log the response
                    Log.d(TAG, "Response: " + response.toString());

                    // Handle the successful response
                    try {
                        boolean status = response.getBoolean("status");
                        String message = response.getString("message");

                        if (status) {
                            // Extract token from the response
                            JSONObject data = response.getJSONObject("data");
                            String token = data.getString("token");

                            // Save token using SharedPrefManager
                            SharedPrefManager.getInstance(LoginActivity.this).saveToken(token);

                            Log.d(TAG, "Login successful. Token saved in SharedPreferences.");

                            // Start MainActivity
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(LoginActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Log the error response
                    Log.e(TAG, "Login error: " + error.toString());

                    // Check if networkResponse is available
                    if (error.networkResponse != null) {
                        String responseBody = new String(error.networkResponse.data);
                        Log.e(TAG, "Response code: " + error.networkResponse.statusCode);
                        Log.e(TAG, "Response body: " + responseBody);

                        try {
                            JSONObject jsonError = new JSONObject(responseBody);
                            String errorMessage = jsonError.optString("message", "Unexpected error occurred.");
                            Toast.makeText(LoginActivity.this, "Login error: " + errorMessage, Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(LoginActivity.this, "Error parsing error response", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        String errorMessage = getErrorMessage(error);
                        Toast.makeText(LoginActivity.this, "Login error: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
        );

        // Add the request to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }

    private static @NonNull String getErrorMessage(VolleyError error) {
        String errorMessage;
        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
            errorMessage = "Network timeout. Please check your connection.";
        } else if (error instanceof AuthFailureError) {
            errorMessage = "Authentication failure. Please check your login credentials.";
        } else if (error instanceof ServerError) {
            errorMessage = "Server error. Please try again later.";
        } else if (error instanceof NetworkError) {
            errorMessage = "Network error. Please check your internet connection.";
        } else if (error instanceof ParseError) {
            errorMessage = "Response parsing error. Please try again.";
        } else {
            errorMessage = "Unexpected error occurred.";
        }
        return errorMessage;
    }
}
