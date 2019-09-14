package com.example.davidalienyi.socialsecurity.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.example.davidalienyi.socialsecurity.Activities.CreatePinActivity;

/**
 * Created by David on 01/07/2018.
 */

public class CreatePinDialog extends DialogFragment {

    ProgressDialog progress;
    // Use the Builder class for convenient dialog construction

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(getActivity());

        builder.setMessage("You can't add your social password because you are yet to set up a pin for revealing your passwords.")
                .setPositiveButton("Create Pin", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(getActivity(), CreatePinActivity.class));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    public void deleteEntry() {

    }

    public void dismissLoader() {
        progress.dismiss();
    }
}
