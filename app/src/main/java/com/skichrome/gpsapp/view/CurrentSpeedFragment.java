package com.skichrome.gpsapp.view;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.skichrome.gpsapp.databinding.FragmentCurrentSpeedBinding;
import com.skichrome.gpsapp.model.local.database.RoomLocation;
import com.skichrome.gpsapp.util.ExtensionsKt;
import com.skichrome.gpsapp.viewmodel.CurrentSpeedFragmentViewModel;

import java.util.List;

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
    }

    // =================================
    //              Methods
    // =================================
    // --- UI & configuration --- //

    private void configureViewModel()
    {
        viewModelLazy.getValue().sayHello();
        viewModelLazy.getValue().getLocation().observe(this, locations ->
        {
            if (locations == null || locations.isEmpty())
            {
                ExtensionsKt.errorLog(requireActivity(), "Location list is empty or null", null);
                return;
            }
            handleLocationResult(locations);
        });
    }

    private void handleLocationResult(List<RoomLocation> locations)
    {
        RoomLocation lastLocation = locations.get(locations.size() - 1);
        float speed = lastLocation.getSpeed() * 3.6f;
        int intSpeed = Math.round(speed);
        if (intSpeed <= 100 && intSpeed > 0)
            updateUI(intSpeed);
        if (intSpeed > 100)
            updateUI(100);
        if (intSpeed <= 0)
            updateUI(1);

        ExtensionsKt.errorLog(requireActivity(), "onLocationResult: " + speed + " km/h (=>" + intSpeed + ")", null);
    }

    private void updateUI(int percent)
    {
        DisplayMetrics metrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        binding.fragmentCurrentSpeedIndicator.getLayoutParams().width = percent * metrics.widthPixels / 100;
    }
}