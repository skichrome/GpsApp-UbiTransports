package com.skichrome.gpsapp.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.skichrome.gpsapp.databinding.FragmentAverageSpeedBinding;
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
        configureViewModel();
    }

    // =================================
    //              Methods
    // =================================

    private void configureViewModel()
    {
        binding.setViewModel(viewModelLazy.getValue());
        viewModelLazy.getValue().getIsInMovement().observe(getViewLifecycleOwner(), isInMovement ->
        {
            if (isInMovement)
            {
                viewModelLazy.getValue().resetMovementIndicator();
                Navigation.findNavController(binding.getRoot()).navigateUp();
            }
        });
    }
}