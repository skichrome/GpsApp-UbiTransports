package com.skichrome.gpsapp.view;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.skichrome.gpsapp.R;
import com.skichrome.gpsapp.databinding.FragmentCurrentSpeedBinding;
import com.skichrome.gpsapp.model.local.database.RoomLocation;
import com.skichrome.gpsapp.util.ExtensionsKt;
import com.skichrome.gpsapp.viewmodel.CurrentSpeedFragmentViewModel;

import kotlin.Lazy;

import static org.koin.java.KoinJavaComponent.inject;

public class CurrentSpeedFragment extends Fragment
{
    // =================================
    //              Fields
    // =================================

    private FragmentCurrentSpeedBinding binding;

    private Lazy<CurrentSpeedFragmentViewModel> viewModelLazy = inject(CurrentSpeedFragmentViewModel.class);

    // =================================
    //        Superclass Methods
    // =================================

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        binding = FragmentCurrentSpeedBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        configureViewModel();
    }

    // =================================
    //              Methods
    // =================================

    // --- UI & configuration --- //

    private void configureViewModel()
    {
        viewModelLazy.getValue().getLastLocation().observe(getViewLifecycleOwner(), locations ->
        {
            if (locations == null)
                return;

            handleLocationResult(locations);
        });

        viewModelLazy.getValue().getStopped().observe(getViewLifecycleOwner(), isStopped ->
        {
            if (isStopped)
            {
                navigateToAverageSpeedFragment();
                viewModelLazy.getValue().resetStoppedIndicator();
            }
        });
    }

    private void handleLocationResult(RoomLocation location)
    {
        float speed = location.getSpeed();
        int intSpeed = Math.round(speed);
        if (intSpeed <= 100 && intSpeed > 0)
            updateUI(intSpeed);
        if (intSpeed > 100)
            updateUI(100);
        if (intSpeed <= 0)
            updateUI(1);

        binding.fragmentCurrentSpeedSpeedText.setText(getString(R.string.fragment_current_speed_speed_text, intSpeed));
        binding.fragmentCurrentSpeedIdTimestampSpeed.setText(getString(R.string.fragment_current_speed_id_timestamp_speed, location.getId(), location.getTimestamp()));
        binding.fragmentCurrentSpeedLatLng.setText(getString(R.string.fragment_current_speed_lat_lng, location.getLatitude(), location.getLongitude()));
    }

    private void updateUI(int percent)
    {
        DisplayMetrics metrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        binding.fragmentCurrentSpeedIndicator.getLayoutParams().width = percent * metrics.widthPixels / 100;
    }

    // --- Navigation --- //

    private void navigateToAverageSpeedFragment()
    {
        NavDirections opts = CurrentSpeedFragmentDirections.actionCurrentSpeedFragmentToAverageSpeedFragment();
        Navigation.findNavController(binding.getRoot()).navigate(opts);
    }
}