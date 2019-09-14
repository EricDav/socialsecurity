package com.example.davidalienyi.socialsecurity.Activities;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.davidalienyi.socialsecurity.Adapters.SocialAdapter;
import com.example.davidalienyi.socialsecurity.Models.Social;
import com.example.davidalienyi.socialsecurity.R;
import com.example.davidalienyi.socialsecurity.RecyclerTouchListner;
import com.example.davidalienyi.socialsecurity.ScheduleReminder;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static com.example.davidalienyi.socialsecurity.Helper.cancelProgress;
import static com.example.davidalienyi.socialsecurity.Helper.openProgress;
import static java.lang.Integer.parseInt;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    List<Social> socials = new ArrayList<>();
    RecyclerView recyclerView;
    TextView textView;
    ImageView profilePic;
    SharedPreferences sharedPref;
    private static final int REQUEST_CODE = 1;
    private static final long TIME_INTERVAL = System.currentTimeMillis() + 10 * 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final Intent addSocialIntent = new Intent(this, AddSocial.class);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(addSocialIntent, 1);
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        createNotificationChannel();
        startAlarm();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        TextView profileEmail = navigationView.getHeaderView(0).findViewById(R.id.profileEmail);
        textView = findViewById(R.id.emptyText);
        profilePic = navigationView.getHeaderView(0).findViewById(R.id.profilePicture);
        TextView profileName = navigationView.getHeaderView(0).findViewById(R.id.profileName);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        profileEmail.setText(sharedPref.getString("loginUser", ""));
        profileName.setText(sharedPref.getString("displayName", ""));
        Picasso.get().load(sharedPref.getString("photoUrl", ""))
                        .into(profilePic);

        if (savedInstanceState != null && savedInstanceState.containsKey("passwords")) {
            textView.setVisibility(View.GONE);
            String[] socialHandles = savedInstanceState.getStringArray("socialHandles");
            String[] passwords = savedInstanceState.getStringArray("passwords");
            String[] ids = savedInstanceState.getStringArray("ids");
            String[] dateCreateds = savedInstanceState.getStringArray("dateCreateds");
            String[] titles = savedInstanceState.getStringArray("titles");
            String[] lastUpdateds = savedInstanceState.getStringArray("lastUpdateds");
            int[] days = savedInstanceState.getIntArray("days");

            createEntries(socialHandles, passwords, ids, dateCreateds, titles, days, lastUpdateds);
            setRecyclerViewLayout(socials);
        } else {
            fetchEntries(true);
        }

        recyclerView.addOnItemTouchListener(new RecyclerTouchListner(getApplicationContext(), recyclerView, new RecyclerTouchListner.ClickListener() {
            @Override
            public void onClick(View view, int position) {

                Social social = socials.get(position);
                Intent intent = new Intent(MainActivity.this, SocialDetail.class);
                intent.putExtra("id", social.getId());
                intent.putExtra("title", social.getTitle());
                intent.putExtra("dateCreated", social.getDateCreated());
                intent.putExtra("password", social.getPassword());
                intent.putExtra("socialHandle", social.getSocialHandle());
                intent.putExtra("days", social.getDays().toString());
                intent.putExtra("lastUpdated", social.getLastUpdated());

                startActivityForResult(intent, 2);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }

    private void createEntries(String[] socialHandles, String[] passwords, String[] ids, String[] dateCreateds, String[] titles, int[] days, String[] lastUpdateds) {
        List<Social> savedSocials = new ArrayList<>();
        for (int i = 0; i < passwords.length; i++) {
            Social social = new Social(ids[i], socialHandles[i], dateCreateds[i], passwords[i], titles[i], days[i], lastUpdateds[i]);
            savedSocials.add(social);
        }

        socials = savedSocials;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1) {
            String socialHandle = data.getStringExtra("socialHandle");
            String password = data.getStringExtra("password");
            String id = data.getStringExtra("id");
            String dateCreated = data.getStringExtra("dateCreated");
            String title = data.getStringExtra("title");
            String lastUpdated = data.getStringExtra("lastUpdated");
            Integer days = parseInt(data.getStringExtra("days"));


            Social social = new Social(id, socialHandle, dateCreated, password, title, days, lastUpdated);
            socials.add(social);
            setRecyclerViewLayout(socials);
        } else if (resultCode == RESULT_OK && requestCode == 2) {

        }


        if (socials.size() > 0) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
            textView.setText("You did not have any entry in your diary. Click the icon below to create an entry");
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (socials != null && socials.size() != 0) {
            String[] socialHandles = new String[socials.size()];
            String[] passwords = new String[socials.size()];
            String[] ids = new String[socials.size()];
            String[] dateCreateds = new String[socials.size()];
            String[] titles = new String[socials.size()];
            String[] lastUpdateds = new String[socials.size()];
            int[] days = new int[socials.size()];

            for (int i = 0; i < socials.size(); i++) {
                socialHandles[i] = socials.get(i).getSocialHandle();
                passwords[i] = socials.get(i).getPassword();
                ids[i] = socials.get(i).getId();
                dateCreateds[i] = socials.get(i).getDateCreated();
                titles[i] = socials.get(i).getTitle();
                days[i] = socials.get(i).getDays();
            }

            outState.putStringArray("socialHandles", socialHandles);
            outState.putStringArray("passwords", passwords);
            outState.putStringArray("ids", ids);
            outState.putStringArray("dateCreateds", dateCreateds);
            outState.putStringArray("titles", titles);
            outState.putIntArray("days", days);
            outState.putStringArray("lastUpdateds", lastUpdateds);
        }

    }

    /**
     * Fetch all the entries the current user has created from firebase.
     */
    public void fetchEntries(final boolean shouldDisplayError) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();


        socials = new ArrayList<>();


        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        db.collection("socials")
                .whereEqualTo("email", sharedPref.getString("loginUser", ""))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()) {
                                textView.setText("You did not have any entry in your diary. Click the icon below to create an entry");
                            } else {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Social entry = new Social(document.getId(), document.get("socialHandle").toString(), document.get("dateCreated").toString(),
                                            document.get("password").toString(), document.get("title").toString(), parseInt(document.get("days").toString()), document.get("lastUpdated").toString());
                                    socials.add(entry);
                                }

                                setRecyclerViewLayout(socials);
                                textView.setVisibility(View.GONE);
                            }
                        } else {
                            if (shouldDisplayError) {
                                // Button button = findViewById(R.id.reloadId);
                                // button.setVisibility(View.VISIBLE);
                                // textView.setText("An error occurred while fetching entries");
                            }

                        }
                    }
                });
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.logout) {
            signOut();
            // Handle the camera action
        } else if (id == R.id.exit) {
            this.finishAffinity();
        } else if (id == R.id.pin) {
            startActivity(new Intent(this, CreatePinActivity.class));
        } else if (id == R.id.security_tips) {
            startActivity(new Intent(this, TipsActivity.class));
        } else if (id == R.id.about) {
            startActivity(new Intent(this, AboutActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * It sets the Recycler view layouts.
     */
    public void setRecyclerViewLayout(List<Social> socials) {
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(new SocialAdapter(socials));
    }

    public void startAlarm() {
        Log.d("ALARM", "setting Alarm");
        Intent intentAlarm = new Intent(this, ScheduleReminder.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), REQUEST_CODE, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
        long oneMinute = 10000 ;
        intentAlarm.setAction("SEND_MESSAGE");
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, TIME_INTERVAL, oneMinute, pendingIntent);
    }

    /**
     * Sign out the current user and returns back to login page.
     */
    private void signOut() {
        final ProgressDialog progress = new ProgressDialog(this);
        openProgress(progress, "logging out");
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        cancelProgress(progress);
                        sharedPref.edit().clear().apply();
                        startActivity(new Intent(MainActivity.this, SignInActivity.class));
                        finish();
                    }
                });
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "undelivered";
            String description = "Update Password";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("1", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
