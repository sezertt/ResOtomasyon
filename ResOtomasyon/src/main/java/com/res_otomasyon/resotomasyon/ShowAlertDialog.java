package com.res_otomasyon.resotomasyon;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by Mustafa on 26.6.2014.
 */
public class ShowAlertDialog {
    public AlertDialog showAlert(final Activity activity, Context context, String title, String message) {
        AlertDialog.Builder aBuilder = new AlertDialog.Builder(context);
        aBuilder.setTitle(title);
        aBuilder.setMessage(message).setCancelable(false)
                .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        AlertDialog alertDialog = aBuilder.create();
        return alertDialog;
    }
}
