package com.gov.sindhpolice.beatbook.ui;

import static androidx.constraintlayout.widget.Constraints.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;
import com.gov.sindhpolice.beatbook.R;
import com.gov.sindhpolice.beatbook.adapters.ImageAdapter;
import com.gov.sindhpolice.beatbook.models.Category;
import com.gov.sindhpolice.beatbook.models.GeneralModel;
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

public class AddFragment extends Fragment implements ImageAdapter.OnImageClickListener {
    private static final int PICK_IMAGE_REQUEST = 1; // Request code for image picking
    private List<Uri> imageUris = new ArrayList<>();
    private ImageAdapter imageAdapter;
    ImageButton btnFetchLocation, btnCaptureImage;
    Button btnAddEntry;
    private RecyclerView recyclerView;
    private AutoCompleteTextView dd_Categories, dd_subCategories;
    private TextInputEditText etTitle, etAddress, etContactNo, etDescription, etLatitude, etLongitude;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add, container, false);
        initializeUIComponents(view);
        fetchCategories();
        setupCategoryListeners();
        return view;
    }

    private void initializeUIComponents(View view) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        dd_Categories = view.findViewById(R.id.dd_Categories);
        dd_subCategories = view.findViewById(R.id.dd_subCategories);
        etTitle = view.findViewById(R.id.etTitle);
        etAddress = view.findViewById(R.id.etAddress);
        etContactNo = view.findViewById(R.id.etContactNo);
        etDescription = view.findViewById(R.id.etDescription);
        etLatitude = view.findViewById(R.id.etLat);
        etLongitude = view.findViewById(R.id.etLong);
        btnFetchLocation = view.findViewById(R.id.btnFetchLocation);
        btnAddEntry = view.findViewById(R.id.btnAddEntry);
        btnCaptureImage = view.findViewById(R.id.btnCaptureImage);

        // Setup RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        if (recyclerView != null) {
            recyclerView.setVisibility(View.VISIBLE);
            // Set layout manager and adapter
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            imageAdapter = new ImageAdapter(imageUris, this);
            recyclerView.setAdapter(imageAdapter);
        } else {
            recyclerView.setVisibility(View.GONE);
            Log.e(TAG, "RecyclerView is null");
        }

        btnFetchLocation.setOnClickListener(v -> fetchCurrentLocation());
        btnCaptureImage.setOnClickListener(v -> openImagePicker());
        btnAddEntry.setOnClickListener(v -> validateAndAddEntry());
    }

    private void setupCategoryListeners() {
        dd_Categories.setOnItemClickListener((parent, view, position, id) -> {
            Category selectedCategory = (Category) parent.getItemAtPosition(position);
            if (selectedCategory.getId() != 0) {
                fetchSubCategories(selectedCategory.getId());
            } else {
                resetSubCategories();  // Reset if "Select Category" is chosen
            }
        });
    }

    private void resetSubCategories() {
        List<Category> subCategories = new ArrayList<>();
        subCategories.add(new Category(0, "Select Sub-Category"));

        ArrayAdapter<Category> subCategoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, subCategories);
        subCategoryAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        dd_subCategories.setAdapter(subCategoryAdapter);
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
            if (addresses != null && !addresses.isEmpty()) {
                etAddress.setText(addresses.get(0).getAddressLine(0));
            } else {
                Toast.makeText(getContext(), "Unable to get address", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Unable to get address", Toast.LENGTH_SHORT).show();
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation();
            } else {
                Toast.makeText(getContext(), "Location permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onDeleteClick(Uri uri) {
        // Handle the delete logic here
        imageUris.remove(uri); // Remove the image from the list
        imageAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                imageUris.add(imageUri);
                imageAdapter.notifyItemInserted(imageUris.size() - 1);
            }
        }
    }

    private void fetchCategories() {
        String url = GeneralModel.API_URL + "categories";
        String token = SharedPrefManager.getInstance(requireActivity()).getToken();

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
        Log.d(TAG, "Response: " + response);
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

                ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categories);
                categoryAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
                dd_Categories.setAdapter(categoryAdapter);
            } else {
                Toast.makeText(getContext(), "Failed to fetch categories", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error parsing categories", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchSubCategories(int categoryId) {
        String url = GeneralModel.API_URL + "subcategories/" + categoryId;
        String token = SharedPrefManager.getInstance(requireActivity()).getToken();

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
        Log.d(TAG, "Response: " + response);
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

                ArrayAdapter<Category> subCategoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, subCategories);
                subCategoryAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
                dd_subCategories.setAdapter(subCategoryAdapter);
            } else {
                Toast.makeText(getContext(), "Failed to fetch subcategories", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error parsing subcategories", Toast.LENGTH_SHORT).show();
        }
    }

    private void validateAndAddEntry() {
        String title = etTitle.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String contactNo = etContactNo.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String latitude = etLatitude.getText().toString().trim();
        String longitude = etLongitude.getText().toString().trim();

        // Use getText() for AutoCompleteTextView
        int selectedCategoryId = ((Category) dd_Categories.getAdapter().getItem(dd_Categories.getListSelection())).getId();
        int selectedSubCategoryId = ((Category) dd_subCategories.getAdapter().getItem(dd_subCategories.getListSelection())).getId();

        // Validate inputs
        if (title.isEmpty() || address.isEmpty() || contactNo.isEmpty() || description.isEmpty() || latitude.isEmpty() || longitude.isEmpty() || selectedCategoryId == 0 || selectedSubCategoryId == 0) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare image data for upload
        List<String> encodedImages = new ArrayList<>();
        for (Uri uri : imageUris) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uri);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
                encodedImages.add(encodedImage);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Error encoding image", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Submit entry to the server
        submitEntryToServer(title, address, contactNo, description, latitude, longitude, selectedCategoryId, selectedSubCategoryId, encodedImages);
    }


    private void submitEntryToServer(String title, String address, String contactNo, String description, String latitude, String longitude, int categoryId, int subCategoryId, List<String> encodedImages) {
        String url = GeneralModel.API_URL + "entries";
        String token = SharedPrefManager.getInstance(requireActivity()).getToken();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> handleSubmitEntryResponse(response),
                error -> Toast.makeText(getContext(), "Error submitting entry", Toast.LENGTH_SHORT).show()) {
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
                params.put("sub_category_id", String.valueOf(subCategoryId));
                params.put("images", new JSONArray(encodedImages).toString()); // Convert the list of images to JSON array
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

    private void handleSubmitEntryResponse(String response) {
        Log.d(TAG, "Response: " + response);
        try {
            JSONObject jsonResponse = new JSONObject(response);
            boolean status = jsonResponse.getBoolean("status");
            if (status) {
                Toast.makeText(getContext(), "Entry added successfully", Toast.LENGTH_SHORT).show();
                clearInputs();
            } else {
                Toast.makeText(getContext(), "Failed to add entry", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearInputs() {
        etTitle.setText("");
        etAddress.setText("");
        etContactNo.setText("");
        etDescription.setText("");
        etLatitude.setText("");
        etLongitude.setText("");
        dd_Categories.setText("", false);
        dd_subCategories.setText("", false);
        imageUris.clear();
        imageAdapter.notifyDataSetChanged();
    }
}
