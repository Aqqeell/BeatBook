package com.gov.sindhpolice.beatbook.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.gov.sindhpolice.beatbook.R;
import com.gov.sindhpolice.beatbook.adapters.ListAdapter;
import com.gov.sindhpolice.beatbook.models.GeneralModel;
import com.gov.sindhpolice.beatbook.models.ListAll;
import com.gov.sindhpolice.beatbook.utils.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ListFragment extends Fragment {

    private ListAdapter listAdapter;
    private ArrayList<ListAll> entryList;
    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        entryList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_all, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        listAdapter = new ListAdapter(entryList, this); // Pass the fragment to the adapter
        recyclerView.setAdapter(listAdapter);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetchEntries(); // Fetch entries when the view is created
    }

    private void fetchEntries() {
        String url = GeneralModel.API_URL + "bookentries"; // Replace with your API URL
        String token = SharedPrefManager.getInstance(getActivity()).getToken();

        progressBar.setVisibility(View.VISIBLE);

        @SuppressLint("NotifyDataSetChanged") JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        if (response.getBoolean("status")) {
                            JSONArray dataArray = response.getJSONArray("data");
                            entryList.clear(); // Clear the list before adding new entries
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject entryJson = dataArray.getJSONObject(i);
                                ListAll entry = new ListAll(
                                        entryJson.getInt("id"),
                                        entryJson.getString("category"),
                                        entryJson.getString("title"),
                                        entryJson.getString("created_by"),
                                        entryJson.getString("created_at")
                                );
                                entryList.add(entry);
                            }
                            listAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getActivity(), response.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "Failed to parse data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e("ListAllFragment", "Error: " + error.getMessage());
                    Toast.makeText(getActivity(), "Failed to fetch data", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token); // Add the token to the headers
                return headers;
            }
        };
        // Set a custom RetryPolicy with a 30-second timeout, 1 retry, and default backoff multiplier
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000, // Timeout in milliseconds (30 seconds)
                3, // Number of retries
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT // Backoff multiplier (default)
        ));

// Add the request to the RequestQueue
        RequestQueue requestQueue = Volley.newRequestQueue(requireActivity());
        requestQueue.add(jsonObjectRequest);
    }

    public void onEntryClick(int entryId) {
        // Make your API call using the entryId
        String url = GeneralModel.API_URL +"bookentries/" + entryId; // Replace with your API URL

        Bundle bundle = new Bundle();
        bundle.putInt("entryId", entryId); // Pass the selected entry ID

        // Navigate to DetailFragment using Navigation component
        Navigation.findNavController(requireView()).navigate(R.id.action_listAllFragment_to_detailFragment, bundle);

        // Proceed to make your API call here
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    // Handle the response from your API call here
                    // You can update the UI or navigate to another fragment if needed
                },
                error -> {
                    // Handle error response here
                    Log.e("ListAllFragment", "Error: " + error.getMessage());
                    Toast.makeText(getActivity(), "Failed to fetch entry details", Toast.LENGTH_SHORT).show();
                }
        );

        // Add the request to the RequestQueue
        Volley.newRequestQueue(requireActivity()).add(jsonObjectRequest);
    }
}
