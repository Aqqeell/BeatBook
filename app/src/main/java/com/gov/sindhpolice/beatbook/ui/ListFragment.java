package com.gov.sindhpolice.beatbook.ui;

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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.gov.sindhpolice.beatbook.R;
import com.gov.sindhpolice.beatbook.adapters.ListAdapter;
import com.gov.sindhpolice.beatbook.models.ListAll;
import com.gov.sindhpolice.beatbook.utils.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ListFragment extends Fragment {

    private RecyclerView recyclerView;
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
        recyclerView = view.findViewById(R.id.recyclerView);
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
        progressBar.setVisibility(View.VISIBLE);
        String url = "http://192.168.200.201:8000/api/v1/bookentries"; // Replace with your API URL
        String token = SharedPrefManager.getInstance(getActivity()).getToken();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
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
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        Log.e("ListAllFragment", "Error: " + error.getMessage());
                        Toast.makeText(getActivity(), "Failed to fetch data", Toast.LENGTH_SHORT).show();
                    }
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
        Volley.newRequestQueue(getActivity()).add(jsonObjectRequest);
    }

    public void onEntryClick(int entryId) {
        // Make your API call using the entryId
        String url = "http://192.168.200.201:8000/api/v1/bookentries/" + entryId; // Replace with your API URL

        Bundle bundle = new Bundle();
        bundle.putInt("entryId", entryId); // Pass the selected entry ID

        // Show a toast for demonstration (you can remove this later)
        Toast.makeText(getActivity(), "Clicked entry ID: " + entryId, Toast.LENGTH_SHORT).show();

        // Navigate to DetailFragment using Navigation component
        Navigation.findNavController(getView()).navigate(R.id.action_listAllFragment_to_detailFragment, bundle);

        // Proceed to make your API call here
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle the response from your API call here
                        // You can update the UI or navigate to another fragment if needed
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error response here
                        Log.e("ListAllFragment", "Error: " + error.getMessage());
                        Toast.makeText(getActivity(), "Failed to fetch entry details", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Add the request to the RequestQueue
        Volley.newRequestQueue(getActivity()).add(jsonObjectRequest);
    }
}
