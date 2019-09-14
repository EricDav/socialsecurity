package com.example.davidalienyi.socialsecurity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.davidalienyi.socialsecurity.Models.Social;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.example.davidalienyi.socialsecurity.Helper.isConnected;
import static java.lang.Integer.parseInt;

public class ScheduleReminder extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d("REMINDER", "I GOT IN REMINDER");
        if (isConnected(context)) {
            Log.d("REMINDER1", "I GOT IN REMINDER1");
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setTimestampsInSnapshotsEnabled(true)
                    .build();


            final List<Social> socials = new ArrayList<>();


            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            db.collection("socials")
                    .whereEqualTo("email", sharedPref.getString("loginUser", ""))
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                if (!task.getResult().isEmpty()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Social entry = new Social(document.getId(), document.get("socialHandle").toString(), document.get("dateCreated").toString(),
                                                document.get("password").toString(), document.get("title").toString(), parseInt(document.get("days").toString()), document.get("lastUpdated").toString());
                                        socials.add(entry);

                                        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy MM dd");
                                        String[] parts = document.get("lastUpdated").toString().split(",");
                                        Calendar calender = Calendar.getInstance();
                                        String lastUpdated = parts[0] + " " + parts[1] + " " + parts[2];
                                        String today = Integer.toString(calender.get(Calendar.YEAR)) + " " + Integer.toString(calender.get(Calendar.MONTH)) + " " + Integer.toString(calender.get(Calendar.DAY_OF_MONTH));

                                        try {
                                            Date date1 = myFormat.parse(lastUpdated);
                                            Date date2 = myFormat.parse(today);
                                            long diff = date2.getTime() - date1.getTime();
                                            long dayDiff = Long.parseLong(document.get("days").toString()) - TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

                                            if (dayDiff == 0) {
                                                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "1")
                                                        .setContentTitle(document.get("socialHandle").toString())
                                                        .setContentText("Testing")
                                                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                                            }

                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            } else {

                            }
                        }
                    });
        }
    }
}
