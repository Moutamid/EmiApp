package com.moutamid.emiapp.service;

import static com.google.firebase.database.FirebaseDatabase.getInstance;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.emiapp.activities.MainActivity;
import com.moutamid.emiapp.utils.Constants;

import java.util.Timer;
import java.util.TimerTask;

public class YourService extends Service {
    public int counter = 0;
    AlertDialog alert;

    @Override
    public void onCreate() {
        super.onCreate();
        AlertDialog.Builder builder = new AlertDialog.Builder(YourService.this, androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog);
        builder.setMessage("Please pay your Emi!");
        builder.setTitle("Alert from Emi App");
        builder.setCancelable(false);
        /*builder.setNegativeButton(getResources().getString(R.string.Hide), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setPositiveButton(getResources().getString(R.string.sure), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });*/

        alert = builder.create();
        alert.setCancelable(false);
        alert.setCanceledOnTouchOutside(false);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        } else {
            alert.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground() {
        String NOTIFICATION_CHANNEL_ID = "example.permanence";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    /*private DatabaseReference databaseReference = getInstance().getReference();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();*/

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
//        startTimer();

        Constants.databaseReference().child(Constants.USERS)
                .child(Constants.auth().getUid())
                .child("isLocked")
                .addValueEventListener(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {

                            boolean isLocked = snapshot.getValue(Boolean.class);

                            if (isLocked) {
                                // IS LOCKED AND SHOW NOTIFICATION AND DIALOG
                                NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
                                notificationHelper.sendHighPriorityNotification(
                                        "Message from Emi App",
                                        "Please pay your EMI!!", MainActivity.class);
                                showDialog();
                            } else {
                                // REMOVE NOTIFICATION AND DIALOG IF SHOWING
                                hideDialog();
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(), error.toException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        /*databaseReference.child(Constants.NOTIFICATIONS).child(mAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            return;
                        }

                        ArrayList<NotificationModel> notificationModelArrayList = new ArrayList<>();

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                            NotificationModel model = dataSnapshot.getValue(NotificationModel.class);
                            notificationModelArrayList.add(model);

                        }

                        for (NotificationModel model : notificationModelArrayList) {

                            databaseReference.child(Constants.NOTIFICATIONS).child(mAuth.getUid())
                                    .child(model.getPushKey())
                                    .removeValue();
*/


//                        }
//
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });

        return START_STICKY;
    }

    public void showDialog() {
        alert.show();
    }

    public void hideDialog() {
        if (alert.isShowing()) {
            alert.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        stoptimertask();

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, Restarter.class);
        this.sendBroadcast(broadcastIntent);
    }


    /*private Timer timer;
    private TimerTask timerTask;

    public void startTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            public void run() {
                Log.i("Count", "=========  " + (counter++));
            }
        };
        timer.schedule(timerTask, 1000, 1000); //
    }

    public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
*/
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i("onTaskRemoved: ", "called.");
        Intent restartServiceIntent = new Intent(getApplicationContext(),
                this.getClass());
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePendingIntent =
                PendingIntent.getService(getApplicationContext(),
                        1, restartServiceIntent,
                        PendingIntent.FLAG_IMMUTABLE);
//                        PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext()
                .getSystemService(Context.ALARM_SERVICE);
        alarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartServicePendingIntent);
        super.onTaskRemoved(rootIntent);
    }
}
