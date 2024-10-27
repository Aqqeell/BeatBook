package com.gov.sindhpolice.beatbook.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class AddFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageView;
    ImageButton btnFetchLocation;
    Button btnAddEntry, btnCaptureImage;
    private Bitmap capturedImageBitmap;
    private Spinner spinnerCategories, spinnerSubCategories;
    private TextInputEditText etTitle, etAddress, etContactNo, etDescription, etLatitude, etLongitude;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add, container, false);
        initializeUIComponents(view);
        fetchCategories();
        setupSpinnerListeners();
        return view;
    }

    private void initializeUIComponents(View view) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        spinnerCategories = view.findViewById(R.id.spinnerCategories);
        spinnerSubCategories = view.findViewById(R.id.spinnerSubCategories);
        etTitle = view.findViewById(R.id.etTitle);
        etAddress = view.findViewById(R.id.etAddress);
        etContactNo = view.findViewById(R.id.etContactNo);
        etDescription = view.findViewById(R.id.etDescription);
        etLatitude = view.findViewById(R.id.etLat);
        etLongitude = view.findViewById(R.id.etLong);
        btnFetchLocation = view.findViewById(R.id.btnFetchLocation);
        imageView = view.findViewById(R.id.imageView);
        btnAddEntry = view.findViewById(R.id.btnAddEntry);
        btnCaptureImage = view.findViewById(R.id.btnCaptureImage);

        btnFetchLocation.setOnClickListener(v -> fetchCurrentLocation());
        btnCaptureImage.setOnClickListener(v -> openImageChooser());
        btnAddEntry.setOnClickListener(v -> validateAndAddEntry());
    }

    private void setupSpinnerListeners() {
        spinnerCategories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Category selectedCategory = (Category) parent.getItemAtPosition(position);
                if (selectedCategory.getId() != 0) {
                    fetchSubCategories(selectedCategory.getId());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                etLatitude.setText(String.valueOf(latitude));
                etLongitude.setText(String.valueOf(longitude));
                fetchAddressFromLocation(latitude, longitude);
            } else {
                Toast.makeText(getContext(), "Location not found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAddressFromLocation(double latitude, double longitude) {
        try {
            Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            assert addresses != null;
            if (!addresses.isEmpty()) {
                etAddress.setText(addresses.get(0).getAddressLine(0));
            } else {
                Toast.makeText(getContext(), "Unable to get address", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Unable to get address", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation();
            } else {
                Toast.makeText(getContext(), "Location permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST) {
            getActivity();
            if (resultCode == Activity.RESULT_OK && data != null) {
                Uri imageUri = data.getData();
                try {
                    capturedImageBitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                    imageView.setImageBitmap(capturedImageBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Error loading image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void fetchCategories() {
        String url = "http://192.168.200.201:8000/api/v1/categories";
        String token = SharedPrefManager.getInstance(getActivity()).getToken();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                this::handleFetchCategoriesResponse,
                error -> Toast.makeText(getContext(), "Error fetching categories", Toast.LENGTH_SHORT).show()) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(requireContext()).add(stringRequest);
    }

    private void handleFetchCategoriesResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            boolean status = jsonResponse.getBoolean("status");
            if (status) {
                JSONArray jsonArray = jsonResponse.getJSONArray("data");
                List<Category> categories = new ArrayList<>();
                categories.add(new Category(0, "Select Category"));

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject categoryObject = jsonArray.getJSONObject(i);
                    int id = categoryObject.getInt("id");
                    String name = categoryObject.getString("name");
                    categories.add(new Category(id, name));
                }

                ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categories);
                categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategories.setAdapter(categoryAdapter);
            } else {
                Toast.makeText(getContext(), jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error parsing categories", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchSubCategories(int categoryId) {
        String url = "http://192.168.200.201:8000/api/v1/subcategories?category_id=" + categoryId;
        String token = SharedPrefManager.getInstance(getActivity()).getToken();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                this::handleFetchSubCategoriesResponse,
                error -> Toast.makeText(getContext(), "Error fetching subcategories", Toast.LENGTH_SHORT).show()) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(requireContext()).add(stringRequest);
    }

    private void handleFetchSubCategoriesResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            boolean status = jsonResponse.getBoolean("status");
            if (status) {
                JSONArray jsonArray = jsonResponse.getJSONArray("data");
                List<Category> subCategories = new ArrayList<>();
                subCategories.add(new Category(0, "Select Sub-Category"));

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject subCategoryObject = jsonArray.getJSONObject(i);
                    int id = subCategoryObject.getInt("id");
                    String name = subCategoryObject.getString("name");
                    subCategories.add(new Category(id, name));
                }

                ArrayAdapter<Category> subCategoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, subCategories);
                subCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerSubCategories.setAdapter(subCategoryAdapter);
            } else {
                Toast.makeText(getContext(), jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error parsing subcategories", Toast.LENGTH_SHORT).show();
        }
    }

    private void validateAndAddEntry() {
        String title = Objects.requireNonNull(etTitle.getText()).toString().trim();
        String address = Objects.requireNonNull(etAddress.getText()).toString().trim();
        String contactNo = Objects.requireNonNull(etContactNo.getText()).toString().trim();
        String description = Objects.requireNonNull(etDescription.getText()).toString().trim();
        String latitude = Objects.requireNonNull(etLatitude.getText()).toString().trim();
        String longitude = Objects.requireNonNull(etLongitude.getText()).toString().trim();
        int categoryId = ((Category) spinnerCategories.getSelectedItem()).getId();
        int subCategoryId = ((Category) spinnerSubCategories.getSelectedItem()).getId();

        if (title.isEmpty() || address.isEmpty() || contactNo.isEmpty() || description.isEmpty() ||
                latitude.isEmpty() || longitude.isEmpty() || categoryId == 0 || subCategoryId == 0) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        addEntry(title, address, contactNo, description, latitude, longitude, categoryId, subCategoryId);
    }

    private void addEntry(String title, String address, String contactNo, String description,
                          String latitude, String longitude, int categoryId, int subCategoryId) {
        String url = "http://192.168.200.201:8000/api/v1/bookentry";
        String token = SharedPrefManager.getInstance(getActivity()).getToken();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                this::handleAddEntryResponse,
                error -> Toast.makeText(getContext(), "Error adding entry", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("title", title);
                params.put("address", address);
                params.put("contact_no", contactNo);
                params.put("description", description);
                params.put("latitude", latitude);
                params.put("longitude", longitude);
                params.put("category_id", String.valueOf(categoryId));
                params.put("subcategory_id", String.valueOf(subCategoryId));
                params.put("image", encodeImageToBase64());
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(requireContext()).add(stringRequest);
    }

    private String encodeImageToBase64() {
        if (capturedImageBitmap != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            capturedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        }
        return null; // or return an empty string if no image is captured
    }

    private void handleAddEntryResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            boolean status = jsonResponse.getBoolean("status");
            if (status) {
                Toast.makeText(getContext(), "Entry added successfully", Toast.LENGTH_SHORT).show();
                clearInputFields();
            } else {
                Toast.makeText(getContext(), jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearInputFields() {
        etTitle.setText("");
        etAddress.setText("");
        etContactNo.setText("");
        etDescription.setText("");
        etLatitude.setText("");
        etLongitude.setText("");
        imageView.setImageBitmap(null); // Clear the image
        spinnerCategories.setSelection(0); // Reset category spinner
        spinnerSubCategories.setSelection(0); // Reset subcategory spinner
        capturedImageBitmap = null; // Clear the bitmap
    }

}
