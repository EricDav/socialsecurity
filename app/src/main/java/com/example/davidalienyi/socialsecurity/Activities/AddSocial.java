package com.example.davidalienyi.socialsecurity.Activities;

import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.davidalienyi.socialsecurity.Dialogs.CreatePinDialog;
import com.example.davidalienyi.socialsecurity.R;
import com.example.davidalienyi.socialsecurity.TrippleDes;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.example.davidalienyi.socialsecurity.Helper.getSocialHandlesMapping;
import static com.example.davidalienyi.socialsecurity.Helper.isConnected;

public class AddSocial extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    EditText passwordEditText;
    EditText confirmPasswordEditText;
    EditText changeDaysEditText;
    EditText titleEditText;
    Button addSocialButton;

    String socialHadle;
    TrippleDes trippleDes;
    String days; // Number of days to send reminders to users to change their password.
    Map<Integer, String> socialHandlesMap;
    String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_social);

        Spinner socialSpinner = (Spinner) findViewById(R.id.social_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.social_list, R.layout.custom_spinner_dropdown);
        socialSpinner.setAdapter(adapter);
        socialSpinner.setSelection(1);
        socialSpinner.setOnItemSelectedListener(this);

        Intent intent = getIntent();

        try {
            trippleDes= new TrippleDes();
        } catch (Exception e) {
            e.printStackTrace();
        }

        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirmPassword);
        changeDaysEditText = findViewById(R.id.daysChange);
        socialHandlesMap = getSocialHandlesMapping();
        addSocialButton = findViewById(R.id.addSocial);
        titleEditText = findViewById(R.id.title);


        if (intent.hasExtra("title")) {
            setTitle("Edit Social");
            titleEditText.setText(intent.getStringExtra("title"));
            changeDaysEditText.setText(intent.getStringExtra("days"));
            addSocialButton.setText("UPDATE SOCIAL");
            id = intent.getStringExtra("id");
            TextView pintText = findViewById(R.id.confirmPassText);
            pintText.setText("Enter pin to update social");

            String socialHandle = intent.getStringExtra("socialHandle");
            for (int i = 0; i < socialHandlesMap.size(); i++) {
                if (socialHandlesMap.get(i).equals(socialHandle)) {
                    socialSpinner.setSelection(i); break;
                }
            }
            socialSpinner.setEnabled(false);
        } else {
            socialSpinner.setSelection(0);
        }

        addSocialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addSocialButton.getText().toString().equals("UPDATE SOCIAL")) {
                    updateSocial();
                } else {
                    addSocial();
                }
            }
        });

        socialHadle = "Facebook";
    }

    public void addSocial() {
        if (isValidInputs() && isConnected(this)) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Calendar calender = Calendar.getInstance();

            Integer year = calender.get(Calendar.YEAR);
            Integer month = calender.get(Calendar.MONTH);      // 0 to 11
            Integer day = calender.get(Calendar.DAY_OF_MONTH);
            final String date  = year.toString() + "," + month.toString() + "," + day.toString();

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            Map<String, Object> socialObj = new HashMap<>();
            socialObj.put("dateCreated", date);
            socialObj.put("socialHandle", socialHadle);
            socialObj.put("days", changeDaysEditText.getText().toString());
            socialObj.put("email", sharedPref.getString("loginUser", ""));
            socialObj.put("password", trippleDes.encrypt(passwordEditText.getText().toString()));
            socialObj.put("title", titleEditText.getText().toString());
            socialObj.put("lastUpdated", date);
            addSocialButton.setClickable(false);
            addSocialButton.setText("Adding...");

            db.collection("socials")
                    .add(socialObj)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            addSocialButton.setClickable(true);
                            addSocialButton.setText("Add Social");
                            handleOnSuccess(documentReference.getId(), date);
                            Log.d("SUCCESS", "DocumentSnapshot added with ID: " + documentReference.getId());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            addSocialButton.setClickable(true);
                            addSocialButton.setText("Add Social");
                            Toast.makeText(AddSocial.this, "An error occurred, try again", Toast.LENGTH_SHORT).show();
                            Log.w("FAILURE", "Error adding document", e);
                        }
                    });
        }
    }

    public void updateSocial() {
        if (isValidUpdate() && isConnected(this)) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Calendar calender = Calendar.getInstance();

            Integer year = calender.get(Calendar.YEAR);
            Integer month = calender.get(Calendar.MONTH);      // 0 to 11
            Integer day = calender.get(Calendar.DAY_OF_MONTH);
            final String date  = year.toString() + "," + month.toString() + "," + day.toString();

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            Map<String, Object> socialObj = new HashMap<>();

            socialObj.put("days", changeDaysEditText.getText().toString());
            socialObj.put("password", trippleDes.encrypt(passwordEditText.getText().toString()));
            socialObj.put("title", titleEditText.getText().toString());
            socialObj.put("lastUpdated", date);
            addSocialButton.setClickable(false);
            addSocialButton.setText("Updating...");

            db.collection("socials").document(id)
                    .update(socialObj)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            addSocialButton.setClickable(true);
                            addSocialButton.setText("Add Social");
                            startActivity(new Intent(AddSocial.this, MainActivity.class));

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            addSocialButton.setClickable(true);
                            addSocialButton.setText("UPDATE SOCIAL");
                            Toast.makeText(AddSocial.this, "An error occurred, try again", Toast.LENGTH_SHORT).show();
                            Log.w("FAILURE", "Error adding document", e);
                        }
                    });
        }
    }

    /**
     * It starts the main activities with the details of the created social after saving successfully.
     */
    private void handleOnSuccess(String id, String date) {
        Toast.makeText(this, "Social password saved successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.putExtra("password", passwordEditText.getText().toString());
        intent.putExtra("socialHandle", socialHadle);
        intent.putExtra("id", id);
        intent.putExtra("dateCreated", date);
        intent.putExtra("days", changeDaysEditText.getText().toString());
        intent.putExtra("title", titleEditText.getText().toString());
        intent.putExtra("lastUpdated", date);

        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        socialHadle = socialHandlesMap.get(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.d("NOTHING", "===========>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }

    public boolean isValidInputs() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (passwordEditText.getText().toString().equals("")) {
            Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
            return false;
        } else if (confirmPasswordEditText.getText().toString().equals("")) {
            Toast.makeText(this, "Confirm Password is required", Toast.LENGTH_SHORT).show();
            return false;
        } else if (changeDaysEditText.getText().toString().trim().equals("")) {
            Toast.makeText(this, "Change Password is required", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!passwordEditText.getText().toString().equals(confirmPasswordEditText.getText().toString())) {
            Toast.makeText(this, "Password does not match", Toast.LENGTH_SHORT).show();
            return false;
        } else if (titleEditText.getText().toString().equals("")) {
            Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show();
            return false;
        } else if (sharedPref.getString("pin", "").equals("")) {
            DialogFragment dialogFragment = new CreatePinDialog();
            Bundle data = new Bundle();
            dialogFragment.setArguments(data);
            dialogFragment.show(getFragmentManager(), "missiles");
            return false;
        }

        return true;
    }

    public boolean isValidUpdate() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        if (passwordEditText.getText().toString().equals("")) {
            Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
            return false;
        } else if (confirmPasswordEditText.getText().toString().equals("")) {
            Toast.makeText(this, "Pin is required", Toast.LENGTH_SHORT).show();
            return false;
        } else if (titleEditText.getText().toString().equals("")) {
            Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show();
            return false;
        } else if (changeDaysEditText.getText().toString().trim().equals("")) {
            Toast.makeText(this, "Change Password is required", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!confirmPasswordEditText.getText().toString().equals(trippleDes.decrypt(sharedPref.getString("pin", "")))) {
            Toast.makeText(this, "You entered an incorrect pin", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}
