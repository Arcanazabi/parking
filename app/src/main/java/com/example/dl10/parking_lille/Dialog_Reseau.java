package com.example.dl10.parking_lille;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class Dialog_Reseau extends DialogFragment{

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState)
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Le serveur ne répond pas")
                .setTitle("Oops !");
        builder.setPositiveButton("Actualiser", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                Dialog_Reseau.this.getDialog().cancel();
                getActivity().recreate();
            }
        });
        AlertDialog dialog = builder.create();
        return dialog;
    }
}
