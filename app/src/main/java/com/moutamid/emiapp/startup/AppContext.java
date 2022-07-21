package com.moutamid.emiapp.startup;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.annotation.NonNull;

import com.fxn.stash.Stash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.emiapp.models.UserModel;
import com.moutamid.emiapp.utils.Constants;

import java.util.ArrayList;

public class AppContext extends Application {
    public static final String CHANNEL_ID = "exampleServiceChannel";
    public static final String NOTIFICATION_CHANNEL_ID = "notificationServiceChannel";
    @Override
    public void onCreate() {
        super.onCreate();
        Stash.init(this);

        if (Constants.auth().getCurrentUser() == null)
            return;

        createNotificationChannel();

        /*Constants.databaseReference().child(Constants.USERS)
                .child(Constants.auth().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Stash.put(Constants.CURRENT_USER_MODEL, snapshot.getValue(UserModel.class));

                            ArrayList<FollowModel> followersArrayList = new ArrayList<>();
                            ArrayList<FollowModel> followingArrayList = new ArrayList<>();

                            if (snapshot.child(Constants.FOLLOWERS).exists()) {
                                for (DataSnapshot dataSnapshot : snapshot.child(Constants.FOLLOWERS).getChildren()) {
                                    FollowModel model = dataSnapshot.getValue(FollowModel.class);
                                    model.uid = dataSnapshot.getKey();
                                    model.value = true;
                                    followersArrayList.add(model);
                                }
                                Stash.put(Constants.FOLLOWERS_LIST, followersArrayList);
                            }

                            if (snapshot.child(Constants.FOLLOWING).exists()) {
                                for (DataSnapshot dataSnapshot : snapshot.child(Constants.FOLLOWING).getChildren()) {
                                    FollowModel model = dataSnapshot.getValue(FollowModel.class);
                                    model.uid = dataSnapshot.getKey();
                                    model.value = true;
                                    followingArrayList.add(model);
                                }
                                Stash.put(Constants.FOLLOWING_LIST, followingArrayList);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });*/

    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Example Service Channel",
                    NotificationManager.IMPORTANCE_MIN
            );

            NotificationChannel notifyChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Notification Service",
                    NotificationManager.IMPORTANCE_HIGH
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
            manager.createNotificationChannel(notifyChannel);
        }
    }

}