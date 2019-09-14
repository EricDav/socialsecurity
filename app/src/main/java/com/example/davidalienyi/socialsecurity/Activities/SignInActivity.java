package com.example.davidalienyi.socialsecurity.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.davidalienyi.socialsecurity.Models.Social;
import com.example.davidalienyi.socialsecurity.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import static com.example.davidalienyi.socialsecurity.Helper.cancelProgress;
import static com.example.davidalienyi.socialsecurity.Helper.isConnected;
import static com.example.davidalienyi.socialsecurity.Helper.openProgress;
import static java.lang.Integer.parseInt;

public class SignInActivity extends Activity {

    GoogleSignInClient mGoogleSignInClient;
    public static int RC_SIGN_IN = 1;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        progressDialog = new ProgressDialog(this);

        if (account != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        setStatusBarColor();

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setStatusBarColor() {
        Window window = getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));
    }

    /**
     * It signs a user in.
     */
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
        openProgress(progressDialog, "Loading...");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        openProgress(progressDialog, "Logining...");

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    /**
     * It handles signin request
     *
     * @param completedTask The completed task
     */
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setTimestampsInSnapshotsEnabled(true)
                    .build();

            final GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

            db.collection("pins")
                    .whereEqualTo("email", account.getEmail())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                SharedPreferences.Editor editor = sharedPref.edit();
                                if (task.getResult().isEmpty()) {
                                } else {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        editor.putString("pin", document.get("pin").toString());
                                        editor.putString("pinId", document.getId());
                                    }
                                }
                                editor.putString("photoUrl", account.getPhotoUrl().toString());
                                editor.putString("displayName", account.getDisplayName());
                                editor.putString("loginUser", account.getEmail());
                                editor.apply();

                                cancelProgress(progressDialog);
                                startActivity(new Intent(SignInActivity.this, MainActivity.class));
                                finish();
                            } else {

                            }
                        }
                    });

        } catch (ApiException e) {
            cancelProgress(progressDialog);

            if (isConnected(SignInActivity.this)) {
                Toast.makeText(SignInActivity.this, "Could not sign in an error occurred", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SignInActivity.this, "No internet connection In error", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
