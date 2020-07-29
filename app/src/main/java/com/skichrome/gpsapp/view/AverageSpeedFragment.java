package com.skichrome.gpsapp.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.skichrome.gpsapp.databinding.FragmentAverageSpeedBinding;

public class AverageSpeedFragment extends Fragment
{
    // =================================
    //              Fields
    // =================================

    private FragmentAverageSpeedBinding binding;

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
    }
}
