package com.example.davidalienyi.socialsecurity.Activities;

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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.davidalienyi.socialsecurity.R;
import com.example.davidalienyi.socialsecurity.TrippleDes;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.HashMap;
import java.util.Map;

import static com.example.davidalienyi.socialsecurity.Helper.getSecurityQuestions;
import static com.example.davidalienyi.socialsecurity.Helper.isConnected;

public class CreatePinActivity extends AppCompatActivity  implements AdapterView.OnItemSelectedListener  {

    String securityQuestion;
    EditText securityAnswer;
    EditText securityPin;
    EditText securityUpdatePin;
    Button createPinButton;
    Map<Integer, String> securityQuestions;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    FirebaseFirestore db;
    TrippleDes trippleDes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_pin);
        securityQuestions = getSecurityQuestions();
        securityQuestion = securityQuestions.get(0);
        securityAnswer = findViewById(R.id.security_answer);
        createPinButton = findViewById(R.id.createPin);
        securityPin = findViewById(R.id.pin);
        securityUpdatePin = findViewById(R.id.updatedPin);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        db = FirebaseFirestore.getInstance();

        try {
            trippleDes= new TrippleDes();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!sharedPref.getString("pin", "").equals(""))  {
            LinearLayout newPinWrapper = findViewById(R.id.newPinWrapper);
            newPinWrapper.setVisibility(View.VISIBLE);
            createPinButton.setText("Update Pin");
            setTitle("Update Security Pin");
        }

        Spinner socialSpinner = (Spinner) findViewById(R.id.pin_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.security_questions, R.layout.custom_spinner_dropdown);
        socialSpinner.setAdapter(adapter);
        socialSpinner.setSelection(1);
        socialSpinner.setOnItemSelectedListener(this);


        createPinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sharedPref.getString("pin", "").equals(""))  {
                    createPin();
                } else {
                    updatePin();
                }
            }
        });
    }

    public void createPin() {
        Log.d("CONNECTION", ((Boolean) isConnected(this)).toString());
        if (isConnected(this) && isValid()) {
            Map<String, Object> pinObj = new HashMap<>();
            pinObj.put("securityQuestion", securityQuestion);
            pinObj.put("securityAnswer", securityAnswer.getText().toString());
            final String encryptedPin = trippleDes.encrypt(securityPin.getText().toString());
            pinObj.put("pin", encryptedPin);
            pinObj.put("email", sharedPref.getString("loginUser", ""));

            createPinButton.setClickable(false);
            createPinButton.setText("Creating...");

            db.collection("pins")
                    .add(pinObj)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            editor = sharedPref.edit();
                            editor.putString("pin", encryptedPin);
                            editor.putString("pinId", documentReference.getId());
                            editor.apply();
                            createPinButton.setText("Create Pin");
                            Toast.makeText(CreatePinActivity.this, "Pin created successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(CreatePinActivity.this, MainActivity.class));
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            createPinButton.setClickable(true);
                            createPinButton.setText("Create Pin");
                            Toast.makeText(CreatePinActivity.this, "An error occurred, try again", Toast.LENGTH_SHORT).show();
                            Log.w("FAILURE", "Error adding document", e);
                        }
                    });
        }
    }

    public void updatePin() {
        Log.d("UPDATE", "I GOT IN HERE" + sharedPref.getString("pinId", ""));
        if (isConnected(this) && isValidUpdate()) {
            Log.d("UPDATE", "I GOT IN HERE" + sharedPref.getString("pinId", ""));
            Map<String, Object> pinObj = new HashMap<>();
            if (!securityAnswer.getText().toString().equals(""))  {
                pinObj.put("securityQuestion", securityQuestion);
                pinObj.put("securityAnswer", securityAnswer.getText().toString());
            }
            final String encryptedPin = trippleDes.encrypt(securityPin.getText().toString());

            pinObj.put("pin", encryptedPin);

            createPinButton.setClickable(false);
            createPinButton.setText("Updating...");

            db.collection("pins").document(sharedPref.getString("pinId", ""))
                    .update(pinObj)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            editor = sharedPref.edit();
                            editor.putString("pin", encryptedPin);
                            editor.apply();
                            createPinButton.setText("Update Pin");
                            Toast.makeText(CreatePinActivity.this, "Pin created successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(CreatePinActivity.this, MainActivity.class));
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            createPinButton.setClickable(true);
                            createPinButton.setText("Update Pin");
                            Toast.makeText(CreatePinActivity.this, "An error occurred, try again", Toast.LENGTH_SHORT).show();
                            Log.w("FAILURE", "Error adding document", e);
                        }
                    });
        }
    }

    public boolean isValidUpdate() {
        if (securityPin.getText().toString().equals("")) {
            Toast.makeText(this, "Security Pin is required", Toast.LENGTH_SHORT).show();
            return false;
        } else if (securityUpdatePin.getText().toString().equals("")) {
            Toast.makeText(this, "Your previous Security Pin is required", Toast.LENGTH_SHORT).show();
            return false;
        }  else if (securityPin.getText().toString().length() != 6) {
            Toast.makeText(this, "Security Pin must be six digit", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!securityUpdatePin.getText().toString().equals(trippleDes.decrypt(sharedPref.getString("pin", "")))) {
            Toast.makeText(this, "Your previous pin is incorrect", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        securityQuestion = securityQuestions.get(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public boolean isValid() {
        if (securityAnswer.getText().toString().equals("")) {
            Toast.makeText(this, "Security answer is required", Toast.LENGTH_SHORT).show();
            return false;
        }  else if (securityPin.getText().toString().equals("")) {
            Toast.makeText(this, "Security Pin is required", Toast.LENGTH_SHORT).show();
            return false;
        } else if (securityPin.getText().toString().length() != 6) {
            Toast.makeText(this, "Security Pin must be six digit", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}
