package com.moutamid.emiapp.activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.UserManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.fxn.stash.Stash;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.judemanutd.autostarter.AutoStartPermissionHelper;
import com.moutamid.emiapp.R;
import com.moutamid.emiapp.service.AdminReceiver;
import com.moutamid.emiapp.service.YourService;

public class HomeActivity extends AppCompatActivity {
    int REQUEST_OVERLAY_PERMISSION = 1;
    int REQUEST_ADMIN_PERMISSION = 2;
    Intent mServiceIntent;
    private YourService mYourService;

    private DevicePolicyManager mgr = null;
    private ComponentName cn = null;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        cn = new ComponentName(this, AdminReceiver.class);
        mgr = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);

        if (Stash.getBoolean("firstTime", true)) {
            Stash.put("firstTime", false);
            Toast.makeText(this, "Please allow our app to run in background", Toast.LENGTH_SHORT).show();
            AutoStartPermissionHelper.Companion.getInstance()
                    .getAutoStartPermission(
                            HomeActivity.this,
                            true,
                            true);
        }

        if (mgr.isAdminActive(cn) && Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "App started", Toast.LENGTH_SHORT).show();
            initService();
        }

        SwitchMaterial switchMaterial = findViewById(R.id.switchMaterial);
        switchMaterial.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                PackageManager p = getPackageManager();
                ComponentName componentName = new ComponentName(HomeActivity.this,
                        MainActivity.class);
                if (b) {
                    // HIDE APP
                    p.setComponentEnabledSetting(componentName,
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP);

                } else {
                    // SHOW APP
                    p.setComponentEnabledSetting(componentName,
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP);

                }

                Toast.makeText(HomeActivity.this, "Done", Toast.LENGTH_SHORT).show();
            }
        });

        SwitchMaterial switchMaterialN = findViewById(R.id.switchMaterialNotifi);
        switchMaterialN.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);

                Toast.makeText(HomeActivity.this, "Please turn off all notifications!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    boolean isSleepingModeDisabled = false;

    @Override
    protected void onResume() {
        super.onResume();
        isSleepingModeDisabled = true;
        if (!mgr.isAdminActive(cn)) {
            Toast.makeText(this, "Please allow our app to be an admin", Toast.LENGTH_SHORT).show();
            Intent intent =
                    new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, cn);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "For purpose only");
            startActivityForResult(intent, REQUEST_ADMIN_PERMISSION);
        }
    }

    private void initService() {
        mYourService = new YourService();
        mServiceIntent = new Intent(this, mYourService.getClass());
        if (!isMyServiceRunning(mYourService.getClass())) {
            startService(mServiceIntent);
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent intent = new Intent();
                String packageName = getPackageName();
                PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
                if (!pm.isIgnoringBatteryOptimizations(packageName)) {
//                    intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + packageName));
                    startActivity(intent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("Service status", "Running");
                return true;
            }
        }
        Log.i("Service status", "Not running");
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (Settings.canDrawOverlays(this)) {

                Toast.makeText(this, "Thanks for allowing the permissions.", Toast.LENGTH_SHORT).show();
                initService();

            } else {
                // permission not granted...
                Toast.makeText(this, "Permission not granted!", Toast.LENGTH_SHORT).show();
            }

        }
        if (requestCode == REQUEST_ADMIN_PERMISSION) {

            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Please allow over lapping permissions!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
            }
            /*else {
                initService();
            }*/


        }
    }

}