package com.gov.sindhpolice.beatbook.ui;

import static androidx.constraintlayout.widget.Constraints.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
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
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
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
    private final List<Uri> imageUris = new ArrayList<>();
    private ImageAdapter imageAdapter;
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
        ImageButton btnFetchLocation = view.findViewById(R.id.btnFetchLocation);
        Button btnAddEntry = view.findViewById(R.id.btnAddEntry);
        ImageButton btnCaptureImage = view.findViewById(R.id.btnCaptureImage);

        // Setup RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        if(recyclerView != null){
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            imageAdapter = new ImageAdapter(imageUris, this);
            recyclerView.setAdapter(imageAdapter);
        }else{
            recyclerView.setVisibility(View.GONE);
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
        dd_subCategories.setAdapter(subCategoryAdapter);
    }

    private void fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        etLatitude.setText(String.valueOf(location.getLatitude()));
                        etLongitude.setText(String.valueOf(location.getLongitude()));
                        fetchAddressFromLocation(location.getLatitude(), location.getLongitude());
                    } else {
                        // If last location is null, request location updates
                        requestLocationUpdates();
                    }
                });
    }

    private void requestLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000); // 10 seconds interval
        locationRequest.setFastestInterval(5000); // 5 seconds fastest interval

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        etLatitude.setText(String.valueOf(location.getLatitude()));
                        etLongitude.setText(String.valueOf(location.getLongitude()));
                        fetchAddressFromLocation(location.getLatitude(), location.getLongitude());
                        // Stop location updates after getting a valid location
                        fusedLocationClient.removeLocationUpdates(this);
                        break; // Exit after getting the first valid location
                    }
                }
            }
        }, Looper.getMainLooper());
    }

    private void fetchAddressFromLocation(double latitude, double longitude) {
        if (!Geocoder.isPresent()) {
            Toast.makeText(getContext(), "Geocoder not available", Toast.LENGTH_SHORT).show();
            return;
        }

        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            Log.d("Location", "Fetching address for Latitude: " + latitude + ", Longitude: " + longitude);
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                etAddress.setText(addresses.get(0).getAddressLine(0));
            } else {
                Toast.makeText(getContext(), "Unable to get address, no addresses found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "IOException: Unable to get address", Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Invalid latitude or longitude", Toast.LENGTH_SHORT).show();
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
        String url = GeneralModel.API_URL + "subcategories?category_id=" + categoryId;
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
        Log.d(TAG, "Subcategories Response: " + response);
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
        String created_by = "1";
        String entity = "1";
        String title = Objects.requireNonNull(etTitle.getText()).toString();
        String address = Objects.requireNonNull(etAddress.getText()).toString();
        String contactNo = Objects.requireNonNull(etContactNo.getText()).toString();
        String description = Objects.requireNonNull(etDescription.getText()).toString();
        String latitude = Objects.requireNonNull(etLatitude.getText()).toString();
        String longitude = Objects.requireNonNull(etLongitude.getText()).toString();
        String category = dd_Categories.getText().toString().trim();
        String subCategory = dd_subCategories.getText().toString().trim();

        if (title.isEmpty() || address.isEmpty() || contactNo.isEmpty() || description.isEmpty() ||
                latitude.isEmpty() || longitude.isEmpty() || category.isEmpty() || subCategory.isEmpty()) {
            Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert images to Base64
        List<String> base64Images = new ArrayList<>();
        for (Uri uri : imageUris) {
            base64Images.add(convertImageToBase64(uri));
        }

        // Add entry to the server
        submitEntry(entity,created_by, title, address, contactNo, description, latitude, longitude, category, subCategory, base64Images.toString());
    }

    private String convertImageToBase64(Uri imageUri) {
        String base64 = "";
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), imageUri);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            base64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return base64;
    }

    private void submitEntry(String title, String address, String contactNo, String description,
                             String latitude, String longitude, String category, String subCategory,
                             String base64Images, String entity, String created_by) {
        String url = GeneralModel.API_URL + "bookentry";
        String token = SharedPrefManager.getInstance(requireActivity()).getToken();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("status")) {
                            Toast.makeText(getContext(), "Entry added successfully", Toast.LENGTH_SHORT).show();
                            // Clear fields
                            clearFields();
                        } else {
                            Toast.makeText(getContext(), "Failed to add entry", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Error adding entry", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
//                params.put("title", title);
//                params.put("address", address);
//                params.put("contact_no", contactNo);
//                params.put("description", description);
                try {
                    params.put("title", new JSONArray(latitude).toString());
                    params.put("address", new JSONArray(address).toString());
                    params.put("contact_no", new JSONArray(contactNo).toString());
                    params.put("description", new JSONArray(description).toString());
                    params.put("latitude", new JSONArray(latitude).toString());
                    params.put("longitude", new JSONArray(longitude).toString());
                    params.put("category", new JSONArray(category).toString());
                    params.put("subcategory", new JSONArray(subCategory).toString());
                    params.put("images[]", new JSONArray(base64Images).toString());
                    params.put("created_by",new JSONArray(created_by).toString());;
                    params.put("entity", new JSONArray(entity).toString());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
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

    @SuppressLint("NotifyDataSetChanged")
    private void clearFields() {
        etTitle.setText("");
        etAddress.setText("");
        etContactNo.setText("");
        etDescription.setText("");
        etLatitude.setText("");
        etLongitude.setText("");
        dd_Categories.setText("");
        dd_subCategories.setText("");
        imageUris.clear();
        imageAdapter.notifyDataSetChanged();
    }
}
