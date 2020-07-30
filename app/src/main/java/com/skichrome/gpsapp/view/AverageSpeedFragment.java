package com.skichrome.gpsapp.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.skichrome.gpsapp.R;
import com.skichrome.gpsapp.databinding.FragmentAverageSpeedBinding;
import com.skichrome.gpsapp.util.ExtensionsKt;
import com.skichrome.gpsapp.viewmodel.AverageSpeedFragmentViewModel;

import kotlin.Lazy;

import static org.koin.java.KoinJavaComponent.inject;

public class AverageSpeedFragment extends Fragment
{
    // =================================
    //              Fields
    // =================================

    private FragmentAverageSpeedBinding binding;
    private Lazy<AverageSpeedFragmentViewModel> viewModelLazy = inject(AverageSpeedFragmentViewModel.class);
    private boolean averageComputed = false;

    // =================================
    //        Superclass Methods
    // =================================

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        binding = FragmentAverageSpeedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        configureBackPressed();
        configureViewModel();
    }

    // =================================
    //              Methods
    // =================================

    private void configureBackPressed()
    {
        OnBackPressedCallback callback = new OnBackPressedCallback(true)
        {
            @Override
            public void handleOnBackPressed()
            {
                requireActivity().finish();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
    }

    private void configureViewModel()
    {
        viewModelLazy.getValue().getLocations().observe(getViewLifecycleOwner(), locationList ->
        {
            if (locationList == null || locationList.isEmpty())
                return;

            if (!averageComputed)
            {
                binding.fragmentAverageSpeedLocationCount.setText(getString(R.string.fragment_average_speed_points_number, locationList.size()));
                averageComputed = true;
            }
        });

        viewModelLazy.getValue().getIsInMovement().observe(getViewLifecycleOwner(), isInMovement ->
        {
            if (isInMovement)
            {
                viewModelLazy.getValue().resetMovementIndicator();
                Navigation.findNavController(binding.getRoot()).navigateUp();
            }
        });

        viewModelLazy.getValue().getMovementCount().observe(getViewLifecycleOwner(), movCount ->
                ExtensionsKt.shortToast(requireActivity(), getString(R.string.average_speed_view_model_movement_count_msg, movCount)));
        viewModelLazy.getValue().getAverage().observe(getViewLifecycleOwner(), average ->
                binding.fragmentAverageSpeedSpeedValue.setText(getString(R.string.fragment_average_speed_speed_value, average)));
    }
}