package com.gov.sindhpolice.beatbook.ui.dashboard;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        entryList = new ArrayList<>();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        initComps();
        fetchDashboardEntries(); // Fetch entries when the fragment is created
        return binding.getRoot();
    }

    private void initComps() {
        // Setting up button listeners
        binding.btnCounter1.setOnClickListener(this);
        binding.btnCounter2.setOnClickListener(this);
        binding.btnCounter3.setOnClickListener(this);
        binding.addNew.setOnClickListener(this);
        binding.btnListAll.setOnClickListener(this);
        binding.btnVisitPlaces.setOnClickListener(this);
        binding.btnLogout.setOnClickListener(this);

        // Setting up RecyclerView and ProgressBar
        entryList = new ArrayList<>();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        listAdapter = new BookEntryAdapter(entryList, this);
        binding.recyclerView.setAdapter(listAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClick(View view) {
        Bundle bundle = new Bundle();
        try {
            if (view instanceof CardView) {
                handleCardClick((CardView) view, bundle);
            } else if (view instanceof MaterialButton) {
                handleButtonClick((MaterialButton) view, bundle);
            }
        } catch (Exception e) {
            Log.e("DashboardFragment", "Error in onClick: " + e.getMessage());
        }
    }

    private void handleCardClick(CardView card, Bundle bundle) {
        if (card == binding.btnListAll) {
            String title = getResources().getString(R.string.cv_listAll);
            bundle.putString("title", title);
            bundle.putString("type_id", "4");
            Navigation.findNavController(card).navigate(R.id.action_nav_dashboard_to_listAllFragment, bundle);
        } else if (card == binding.btnVisitPlaces) {
            String title = getResources().getString(R.string.cv_visitPlaces);
            bundle.putString("title", title);
            bundle.putString("type_id", "5");
            Navigation.findNavController(card).navigate(R.id.action_nav_dashboard_to_visitsFragment, bundle);
        } else if (card == binding.btnLogout) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> startActivity(new Intent(requireActivity(), LoginActivity.class)))
                    .setNegativeButton("No", null)
                    .show();
        }
    }

    private void handleButtonClick(MaterialButton button, Bundle bundle) {
        if (button == binding.addNew) {
            String title = getResources().getString(R.string.btn_addNew);
            bundle.putString("title", title);
            bundle.putString("type_id", "7");
            Navigation.findNavController(button).navigate(R.id.action_nav_dashboard_to_addFragment, bundle);
        }
    }

    private void fetchDashboardEntries() {
        binding.progressBar.setVisibility(View.VISIBLE);
        String url = GeneralModel.API_URL + "bookentries"; // Replace with your API URL
        String token = SharedPrefManager.getInstance(getActivity()).getToken();

        @SuppressLint("NotifyDataSetChanged") JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    binding.progressBar.setVisibility(View.GONE);
                    try {
                        if (response.getBoolean("status")) {
                            JSONArray dataArray = response.getJSONArray("data");
                            entryList.clear();
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
                        Log.e("DashboardFragment", "JSON Parsing error: " + e.getMessage());
                        Toast.makeText(getActivity(), "Failed to parse data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Log.e("DashboardFragment", "Error: " + error.getMessage());
                    Toast.makeText(getActivity(), "Failed to fetch data", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(requireActivity()).add(jsonObjectRequest);
    }

    public void onEntryClick(int entryId) {
        Bundle bundle = new Bundle();
        bundle.putInt("entryId", entryId);
        Toast.makeText(getActivity(), "Clicked entry ID: " + entryId, Toast.LENGTH_SHORT).show();
        Navigation.findNavController(requireView()).navigate(R.id.action_nav_dashboard_to_detailFragment, bundle);
    }
}
