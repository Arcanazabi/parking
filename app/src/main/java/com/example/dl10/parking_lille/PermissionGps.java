package com.example.dl10.parking_lille;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;


public class PermissionGps extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("La connection GPS n'est pas disponible. Cliquez sur OK pour l'activer.")
                .setTitle("Activer GPS");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                getActivity().startActivity(settingsIntent);
            }
        });
        builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                PermissionGps.this.getDialog().cancel();
            }
        });

        AlertDialog dialog = builder.create();

        return dialog;

    }
}
