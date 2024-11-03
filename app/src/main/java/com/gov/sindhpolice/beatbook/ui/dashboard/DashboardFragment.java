package com.gov.sindhpolice.beatbook.ui.dashboard;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.gov.sindhpolice.beatbook.LoginActivity;
import com.gov.sindhpolice.beatbook.R;
import com.gov.sindhpolice.beatbook.adapters.BookEntryAdapter;
import com.gov.sindhpolice.beatbook.databinding.FragmentDashboardBinding;
import com.gov.sindhpolice.beatbook.models.GeneralModel;
import com.gov.sindhpolice.beatbook.models.ListAll;
import com.gov.sindhpolice.beatbook.utils.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DashboardFragment extends Fragment implements View.OnClickListener {

    private FragmentDashboardBinding binding;
    private BookEntryAdapter listAdapter;
    private ArrayList<ListAll> entryList;
    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        entryList = new ArrayList<>();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        initComps();
        fetchDashboardEntries(); // Fetch entries when the fragment is created
        return root;
    }

    private void initComps() {
        binding.btnCounter1.setOnClickListener(this);
        binding.btnCounter2.setOnClickListener(this);
        binding.btnCounter3.setOnClickListener(this);
        binding.addNew.setOnClickListener(this);
        binding.btnListAll.setOnClickListener(this);
        binding.btnVisitPlaces.setOnClickListener(this);
        binding.btnLogout.setOnClickListener(this);

        RecyclerView recyclerView = binding.recyclerView;
        progressBar = binding.progressBar;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Initialize the adapter and set it to the RecyclerView
        listAdapter = new BookEntryAdapter(entryList, this); // Update this line
        recyclerView.setAdapter(listAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClick(View view) {
        try {
            Bundle bundle = new Bundle();
            if (view instanceof CardView) {
                CardView button = (CardView) view;
                bundle.putInt("type", button.getId());
                if (button == binding.btnListAll) {
                    String title = getResources().getString(R.string.cv_listAll);
                    bundle.putString("title", title);
                    bundle.putString("type_id", "4");
                    Navigation.findNavController(view).navigate(R.id.action_nav_dashboard_to_listAllFragment, bundle);
                } else if (button == binding.btnVisitPlaces) {
                    String title = getResources().getString(R.string.cv_visitPlaces);
                    bundle.putString("title", title);
                    bundle.putString("type_id", "5");
                    Navigation.findNavController(view).navigate(R.id.action_nav_dashboard_to_visitsFragment, bundle);
                } else if (button == binding.btnLogout) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    builder.setTitle("Logout")
                            .setMessage("Are you sure you want to logout?")
                            .setPositiveButton("Yes", (dialog, which) -> startActivity(new Intent(requireActivity(), LoginActivity.class)))
                            .setNegativeButton("No", null)
                            .show();
                }
            } else if (view instanceof MaterialButton) {
                MaterialButton button1 = (MaterialButton) view;
                bundle.putInt("type", button1.getId());
                if (button1 == binding.addNew) {
                    String title = getResources().getString(R.string.btn_addNew);
                    bundle.putString("title", title);
                    bundle.putString("type_id", "7");
                    Navigation.findNavController(view).navigate(R.id.action_nav_dashboard_to_addFragment, bundle);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void fetchDashboardEntries() {
        progressBar.setVisibility(View.VISIBLE);
        String url = GeneralModel.API_URL + "bookentries"; // Replace with your API URL
        String token = SharedPrefManager.getInstance(getActivity()).getToken();

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
                            listAdapter.notifyDataSetChanged();  // Notify the adapter of data changes after updating the list
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
                    Log.e("DashboardFragment", "Error: " + error.getMessage());
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

        // Add the request to the RequestQueue
        Volley.newRequestQueue(requireActivity()).add(jsonObjectRequest);
    }

    public void onEntryClick(int entryId) {
        // Navigate to DetailFragment using Navigation component
        Bundle bundle = new Bundle();
        bundle.putInt("entryId", entryId); // Pass the selected entry ID

        // Show a toast for demonstration (you can remove this later)
        Toast.makeText(getActivity(), "Clicked entry ID: " + entryId, Toast.LENGTH_SHORT).show();

        Navigation.findNavController(requireView()).navigate(R.id.action_nav_dashboard_to_detailFragment, bundle);
    }
}
