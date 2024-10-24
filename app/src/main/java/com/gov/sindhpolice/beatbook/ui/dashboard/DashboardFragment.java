package com.gov.sindhpolice.beatbook.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.gov.sindhpolice.beatbook.LoginActivity;
import com.gov.sindhpolice.beatbook.MainActivity;
import com.gov.sindhpolice.beatbook.R;
import com.gov.sindhpolice.beatbook.databinding.FragmentDashboardBinding;
public class DashboardFragment extends Fragment implements View.OnClickListener {

    private FragmentDashboardBinding binding;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        initComps();
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
                            .setPositiveButton("Yes", (dialog, which) -> {
                                startActivity(new Intent(requireActivity(), LoginActivity.class));
                            })
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
}