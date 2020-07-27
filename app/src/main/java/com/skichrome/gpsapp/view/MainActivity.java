package com.skichrome.gpsapp.view;

import android.Manifest;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.skichrome.gpsapp.R;
import com.skichrome.gpsapp.databinding.ActivityMainBinding;
import com.skichrome.gpsapp.util.ExtensionsKt;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity
{
    // =================================
    //              Fields
    // =================================

    private ActivityMainBinding binding;

    // =================================
    //        Superclass Methods
    // =================================

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        MainActivityPermissionsDispatcher.beginLocationUpdatesWithPermissionCheck(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    // =================================
    //              Methods
    // =================================

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    void beginLocationUpdates()
    {
        ExtensionsKt.shortToast(this, R.string.activity_main_permission_granted_msg);
        configurePermissionStatusImg(true);
    }

    @OnShowRationale(Manifest.permission.ACCESS_FINE_LOCATION)
    void showLocationRationale(final PermissionRequest request)
    {
        new AlertDialog.Builder(this)
                .setMessage("test")
                .setPositiveButton(R.string.activity_main_dialog_positive_btn, (dialog, button) -> request.proceed())
                .setNegativeButton(R.string.activity_main_dialog_negative_btn, (dialog, button) -> request.cancel())
                .show();
    }

    @OnPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION)
    void onPermissionDenied()
    {
        configurePermissionStatusImg(false);
    }

    @OnNeverAskAgain(Manifest.permission.ACCESS_FINE_LOCATION)
    void onNeverAskPermission()
    {
        configurePermissionStatusImg(null);
    }

    private void configurePermissionStatusImg(Boolean isLocationGranted)
    {
        if (isLocationGranted == null)
        {
            binding.activityMainPermissionInfoImg.setImageResource(R.drawable.ic_baseline_error_outline_24);
            binding.activityMainPermissionInfoText.setText(R.string.activity_main_never_ask_permission_msg);
            return;
        }

        int backgroundReference = isLocationGranted ? R.drawable.ic_baseline_check_circle_outline_24 : R.drawable.ic_baseline_error_outline_24;
        int textReference = isLocationGranted ? R.string.activity_main_permission_granted_msg : R.string.activity_main_permission_denied_msg;
        binding.activityMainPermissionInfoImg.setImageResource(backgroundReference);
        binding.activityMainPermissionInfoText.setText(textReference);
    }
}