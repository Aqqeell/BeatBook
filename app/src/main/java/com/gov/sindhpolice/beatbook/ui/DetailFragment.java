package com.gov.sindhpolice.beatbook.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView; // Import TextView for displaying details
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.gov.sindhpolice.beatbook.R;
import com.gov.sindhpolice.beatbook.models.GeneralModel;
import com.gov.sindhpolice.beatbook.utils.SharedPrefManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DetailFragment extends Fragment {

    private TextView tvTitle, tvCategory, tvLat, tvLong, tvAddress, tvContact, tvCreatedBy, tvCreatedAt, tvUpdatedAt, tvDesc; // TextViews for displaying details
//    ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        // Initialize TextViews and ProgressBar
        tvTitle = view.findViewById(R.id.tv_title);
        tvCategory = view.findViewById(R.id.tv_category);
        tvLat = view.findViewById(R.id.tv_lat);
        tvLong = view.findViewById(R.id.tv_long);
        tvAddress = view.findViewById(R.id.tv_address);
        tvContact = view.findViewById(R.id.tv_contact_no);
        tvCreatedBy = view.findViewById(R.id.tv_created_by);
        tvCreatedAt = view.findViewById(R.id.tv_created_at);
        tvUpdatedAt = view.findViewById(R.id.tv_updated_at);
        tvDesc = view.findViewById(R.id.tv_description);
//        progressBar = view.findViewById(R.id.progressBar);

        // Get the entry ID from the arguments
        if (getArguments() != null) {
            int entryId = getArguments().getInt("entryId");
            fetchEntryDetails(entryId); // Fetch details using the entry ID
        }

        return view;
    }

    private void fetchEntryDetails(int entryId) {
//        progressBar.setVisibility(View.VISIBLE);
        String url = GeneralModel.API_URL + "bookentries/" + entryId; // Replace with your API URL
        String token = SharedPrefManager.getInstance(getActivity()).getToken();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
//                        progressBar.setVisibility(View.GONE);
                    try {
                        if (response.getBoolean("status")) {
                            JSONObject entryJson = response.getJSONObject("data");
                            // Assuming the response has a "data" object with the same fields as ListAll
                            tvTitle.setText(entryJson.getString("title"));
                            tvCategory.setText(entryJson.getString("category"));
                            tvLat.setText(entryJson.getString("lat"));
                            tvLong.setText(entryJson.getString("long"));
                            tvAddress.setText(entryJson.getString("address"));
                            tvContact.setText(entryJson.getString("contact_no"));
                            tvCreatedBy.setText(entryJson.getString("created_by"));
                            tvCreatedAt.setText(entryJson.getString("created_at"));
                            tvUpdatedAt.setText(entryJson.getString("updated_at"));
                            tvDesc.setText(entryJson.getString("description"));
                        } else {
                            Toast.makeText(getActivity(), response.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "Failed to parse entry details", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
//                        progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token); // Add the token to the headers
                return headers;
            }
        };

        // Add the request to the RequestQueue
        Volley.newRequestQueue(requireActivity()).add(jsonObjectRequest);
    }
}
