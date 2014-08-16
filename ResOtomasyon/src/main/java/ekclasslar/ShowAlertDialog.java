package ekclasslar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class ShowAlertDialog {
    public AlertDialog showAlert(Context context, String title, String message) {
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
