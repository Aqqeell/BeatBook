package com.gov.sindhpolice.beatbook.ui;

import static androidx.constraintlayout.widget.Constraints.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gov.sindhpolice.beatbook.R;
import com.gov.sindhpolice.beatbook.adapters.ImageAdapter;
import com.gov.sindhpolice.beatbook.models.GeneralModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VisitsFragment extends Fragment implements ImageAdapter.OnImageClickListener {

    private static final int PICK_IMAGE_REQUEST = 1;
    private List<Uri> imageUris = new ArrayList<>();
    private ImageAdapter imageAdapter;
    private RecyclerView recyclerView;
    private ImageButton btnSelectImage;
    private EditText etBookId, etLat, etLong, etRemarks;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_visit, container, false);

        // Initialize EditTexts and buttons
        etLat = view.findViewById(R.id.etLat);
        etLong = view.findViewById(R.id.etLong);
        etRemarks = view.findViewById(R.id.etRemarks);
        btnSelectImage = view.findViewById(R.id.btnSelectImage);
        Button btnSubmit = view.findViewById(R.id.btnSubmit);
        recyclerView = view.findViewById(R.id.recyclerView);

        // Set up listeners
        btnSelectImage.setOnClickListener(v -> openImagePicker());
        btnSubmit.setOnClickListener(v -> submitVisitData());

        // Initialize RecyclerView
        initComp();

        return view;
    }

    private void initComp() {
        // Set up RecyclerView with ImageAdapter
        if(recyclerView != null){
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            imageAdapter = new ImageAdapter(imageUris, this);
            recyclerView.setAdapter(imageAdapter);
        }else{
            recyclerView.setVisibility(View.GONE);
        }
    }

    // Open the image chooser to select an image from the device
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onDeleteClick(Uri uri) {
        // Handle the delete logic here
        imageUris.remove(uri); // Remove the image from the list
        imageAdapter.notifyDataSetChanged();
    }

    // Handle the result from the image chooser
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                imageUris.add(imageUri);
                imageAdapter.notifyItemInserted(imageUris.size() - 1);
            } else {
                Toast.makeText(getContext(), "Failed to get image.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method to submit visit data
    private void submitVisitData() {
        String url = GeneralModel.API_URL + "visits";
        String token = "your_authorization_token";  // Replace with your token
        String bookId = etBookId.getText().toString().trim();
        String latitude = etLat.getText().toString().trim();
        String longitude = etLong.getText().toString().trim();
        String remarks = etRemarks.getText().toString().trim();

        // Validate input fields
        if (bookId.isEmpty() || latitude.isEmpty() || longitude.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a StringRequest for sending data
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("status")) {
                            Toast.makeText(getContext(), "Visit submitted successfully", Toast.LENGTH_SHORT).show();
                            clearFields(); // Clear fields after successful submission
                        } else {
                            Toast.makeText(getContext(), "Error: " + jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON Parsing error: " + e.getMessage());
                        Toast.makeText(getContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "Volley error: " + error.getMessage());
                    Toast.makeText(getContext(), "Error submitting visit", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("book_id", bookId);
                params.put("lat", latitude);
                params.put("long", longitude);
                params.put("remarks", remarks);
                // You can add images as base64 strings if required
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);  // Add the authorization token
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(requireActivity());
        requestQueue.add(stringRequest);
    }

    // Clear input fields after submission
    private void clearFields() {
        etBookId.setText("");
        etLat.setText("");
        etLong.setText("");
        etRemarks.setText("");
        imageUris.clear(); // Clear image selection
        imageAdapter.notifyDataSetChanged();
    }
}
