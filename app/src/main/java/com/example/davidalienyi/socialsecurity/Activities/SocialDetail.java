package com.example.davidalienyi.socialsecurity.Activities;

import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.davidalienyi.socialsecurity.R;
import com.example.davidalienyi.socialsecurity.TrippleDes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.example.davidalienyi.socialsecurity.Helper.getFormatedDate;

public class SocialDetail extends AppCompatActivity {
    TextView titleTextView;
    TextView socialHandleTextView;
    TextView lastUpdatedTextView;
    Button reviewPasBut;
    TextView passwordTextView;
    EditText pinEditText;
    String password;
    String title;
    String socialHandle;
    String days;
    String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_detail);
        Intent intent = getIntent();
        titleTextView = findViewById(R.id.titleId);
        socialHandleTextView = findViewById(R.id.socialHandleText);
        lastUpdatedTextView = findViewById(R.id.lastUpdated);
        reviewPasBut = findViewById(R.id.revealPasswordButton);
        passwordTextView = findViewById(R.id.decryptPassword);
        pinEditText = findViewById(R.id.revealPin);

        reviewPasBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (reviewPasBut.getText().toString().equals("HIDE PASSWORD")) {
                    passwordTextView.setText("*************");
                    reviewPasBut.setText("REVEAL PASSWORD");
                } else {
                    revealPassword();
                }

            }
        });

        title = intent.getStringExtra("title");
        socialHandle = intent.getStringExtra("socialHandle");
        days = intent.getStringExtra("days");
        id = intent.getStringExtra("id");

        socialHandleTextView.setText(socialHandle);
        titleTextView.setText(title);
        lastUpdatedTextView.setText(getFormatedDate(intent.getStringExtra("lastUpdated")));
        password = intent.getStringExtra("password");
        Log.d("PASSWORD", password);

        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy MM dd");
        String[] parts = intent.getStringExtra("lastUpdated").split(",");
        Calendar calender = Calendar.getInstance();
        String lastUpdated = parts[0] + " " + parts[1] + " " + parts[2];
        String today = Integer.toString(calender.get(Calendar.YEAR)) + " " + Integer.toString(calender.get(Calendar.MONTH)) + " " + Integer.toString(calender.get(Calendar.DAY_OF_MONTH));

        try {
            Date date1 = myFormat.parse(lastUpdated);
            Date date2 = myFormat.parse(today);
            long diff = date2.getTime() - date1.getTime();
            Log.d("DAY", Long.toString(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)));
            long dayDiff = Long.parseLong(days) - TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
            TextView statusTextview = findViewById(R.id.status);
            if (dayDiff > 0) {
                statusTextview.setText("It remains " + Long.toString(dayDiff) + " days for you to update your password");
            } else if (dayDiff == 0) {
                statusTextview.setText("You should update your password today");
            } else {
                statusTextview.setText("You have supposed to have updated your password " + Long.toString(dayDiff) + " days ago");
            }
            Log.d("DAYS_DIFF", Long.toString(dayDiff));

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.social_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_entry:
                editSocial();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    public void editSocial() {
        Intent intent = new Intent(this, AddSocial.class);
        intent.putExtra("title", title);
        intent.putExtra("socialHandle", socialHandle);
        intent.putExtra("days", days);
        intent.putExtra("password", password);
        intent.putExtra("id", id);

        startActivity(intent);
    }


    public void revealPassword() {
        try {
            TrippleDes trippleDes = new TrippleDes();
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            String pin = pinEditText.getText().toString();
            String savedPin = trippleDes.decrypt(sharedPref.getString("pin", ""));

            if (pin.equals(savedPin)) {
                passwordTextView.setText(trippleDes.decrypt(password));
                reviewPasBut.setText("HIDE PASSWORD");
                pinEditText.setText("");
            } else {
                Toast.makeText(this, "You entered an invalid pin", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
