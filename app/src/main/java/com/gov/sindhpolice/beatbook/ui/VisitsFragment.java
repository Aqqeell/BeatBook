package com.gov.sindhpolice.beatbook.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gov.sindhpolice.beatbook.R;
import com.gov.sindhpolice.beatbook.models.GeneralModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class VisitsFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageView;
    private Bitmap bitmap;
    private EditText etBookId, etLat, etLong, etRemarks;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_visit, container, false);

        etBookId = view.findViewById(R.id.etBookId);
        etLat = view.findViewById(R.id.etLat);
        etLong = view.findViewById(R.id.etLong);
        etRemarks = view.findViewById(R.id.etRemarks);
        imageView = view.findViewById(R.id.imageView);
        Button btnSubmit = view.findViewById(R.id.btnSubmit);
        Button btnSelectImage = view.findViewById(R.id.btnSelectImage);

        btnSelectImage.setOnClickListener(v -> openImageChooser());
        btnSubmit.setOnClickListener(v -> submitVisitData());

        return view;
    }

    // Open the image chooser to select an image from the device
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Handle the result from the image chooser
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST) {
            requireActivity();
            if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
                Uri imageUri = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                    imageView.setImageBitmap(bitmap); // Display selected image
                } catch (IOException e) {
                    e.printStackTrace();
                }
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

        // Convert bitmap image to a base64 string for upload
        String imageString = null;
        if (bitmap != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        }

        String finalImageString = imageString;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("status")) {
                            Toast.makeText(getContext(), "Visit submitted successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Error: " + jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Error submitting visit", Toast.LENGTH_SHORT).show()) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("book_id", bookId);
                params.put("lat", latitude);
                params.put("long", longitude);
                params.put("remarks", remarks);
                params.put("pictures[]", finalImageString);  // Send the image as base64 string
                params.put("updated_by", "1");  // Replace with the actual user ID
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
}
