package com.gov.sindhpolice.beatbook.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.gov.sindhpolice.beatbook.R;
import com.gov.sindhpolice.beatbook.models.Category;
import com.gov.sindhpolice.beatbook.utils.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageView;
    private Bitmap capturedImageBitmap;
    private Spinner spinnerCategories, spinnerSubCategories;
    private ArrayAdapter<Category> categoryAdapter, subCategoryAdapter;
    private List<Category> categories, subCategories;
    private TextInputEditText etTitle, etAddress, etContactNo, etDescription, etLatitude, etLongitude;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add, container, false);

        spinnerCategories = view.findViewById(R.id.spinnerCategories);
        spinnerSubCategories = view.findViewById(R.id.spinnerSubCategories); // Subcategory spinner
        etTitle = view.findViewById(R.id.etTitle);
        etAddress = view.findViewById(R.id.etAddress);
        etContactNo = view.findViewById(R.id.etContactNo);
        etDescription = view.findViewById(R.id.etDescription);
        etLatitude = view.findViewById(R.id.etLat);
        etLongitude = view.findViewById(R.id.etLong);
        imageView = view.findViewById(R.id.imageView);
        Button btnAddEntry = view.findViewById(R.id.btnAddEntry);
        Button btnCaptureImage = view.findViewById(R.id.btnCaptureImage);

        fetchCategories();
        setupSpinner();

        btnCaptureImage.setOnClickListener(v -> openImageChooser());
        btnAddEntry.setOnClickListener(v -> addEntry());

        return view;
    }

    // Open image chooser
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Handle image selection
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                capturedImageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                imageView.setImageBitmap(capturedImageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Error loading image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchCategories() {
        String url = "http://10.0.2.2:8000/api/v1/categories";
        String token = SharedPrefManager.getInstance(getActivity()).getToken();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        boolean status = jsonResponse.getBoolean("status");
                        if (status) {
                            JSONArray jsonArray = jsonResponse.getJSONArray("data");
                            categories = new ArrayList<>();
                            categories.add(new Category(0, "Select Category"));

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject categoryObject = jsonArray.getJSONObject(i);
                                int id = categoryObject.getInt("id");
                                String name = categoryObject.getString("name");
                                categories.add(new Category(id, name));
                            }

                            categoryAdapter = new ArrayAdapter<>(getContext(),
                                    android.R.layout.simple_spinner_item, categories);
                            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerCategories.setAdapter(categoryAdapter);
                        } else {
                            Toast.makeText(getContext(), jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error parsing categories", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Error fetching categories", Toast.LENGTH_SHORT).show()) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(getContext()).add(stringRequest);
    }

    // Fetch subcategories based on selected category
    private void fetchSubCategories(int categoryId) {
        String url = "http://10.0.2.2:8000/api/v1/subcategories?category_id=" + categoryId;
        String token = SharedPrefManager.getInstance(getActivity()).getToken();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        boolean status = jsonResponse.getBoolean("status");
                        if (status) {
                            JSONArray jsonArray = jsonResponse.getJSONArray("data");
                            subCategories = new ArrayList<>();
                            subCategories.add(new Category(0, "Select SubCategory"));

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject subCategoryObject = jsonArray.getJSONObject(i);
                                int id = subCategoryObject.getInt("id");
                                String name = subCategoryObject.getString("name");
                                subCategories.add(new Category(id, name));
                            }

                            subCategoryAdapter = new ArrayAdapter<>(getContext(),
                                    android.R.layout.simple_spinner_item, subCategories);
                            subCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerSubCategories.setAdapter(subCategoryAdapter);
                        } else {
                            Toast.makeText(getContext(), jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error parsing subcategories", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Error fetching subcategories", Toast.LENGTH_SHORT).show()) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(getContext()).add(stringRequest);
    }

    private void setupSpinner() {
        spinnerCategories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Category selectedCategory = (Category) parent.getItemAtPosition(position);
                if (selectedCategory.getId() != 0) {
                    fetchSubCategories(selectedCategory.getId()); // Fetch subcategories based on selected category
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void addEntry() {
        String title = etTitle.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String contactNo = etContactNo.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String latitude = etLatitude.getText().toString().trim();
        String longitude = etLongitude.getText().toString().trim();
        int selectedCategoryId = ((Category) spinnerCategories.getSelectedItem()).getId();
        int selectedSubCategoryId = ((Category) spinnerSubCategories.getSelectedItem()).getId(); // Get selected subcategory

        String imageString = null;
        if (capturedImageBitmap != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            capturedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] imageBytes = stream.toByteArray();
            imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        }

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("title", title);
            jsonObject.put("address", address);
            jsonObject.put("contact_no", contactNo);
            jsonObject.put("description", description);
            jsonObject.put("latitude", latitude);
            jsonObject.put("longitude", longitude);
            jsonObject.put("category_id", selectedCategoryId);
            jsonObject.put("subcategory_id", selectedSubCategoryId); // Include subcategory
            if (imageString != null) {
                jsonObject.put("image", imageString);
            }
            jsonObject.put("created_by", "user_id");

            String url = "http://10.0.2.2:8000/api/v1/addEntry";

            StringRequest jsonRequest = new StringRequest(Request.Method.POST, url,
                    response -> Toast.makeText(getContext(), "Entry added successfully", Toast.LENGTH_SHORT).show(),
                    error -> Toast.makeText(getContext(), "Error adding entry", Toast.LENGTH_SHORT).show()) {
                @Override
                public byte[] getBody() {
                    return jsonObject.toString().getBytes();
                }

                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
            };

            RequestQueue requestQueue = Volley.newRequestQueue(getContext());
            requestQueue.add(jsonRequest);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error creating JSON request", Toast.LENGTH_SHORT).show();
        }
    }
}
