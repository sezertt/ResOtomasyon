package com.res_otomasyon.resotomasyon;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;

import java.util.Dictionary;
import java.util.Hashtable;

import ekclasslar.SetViewGroupEnabled;
import ekclasslar.ShowAlertDialog;


public class Settings extends Activity implements View.OnClickListener {

    Button btnSave,btnCancel;
    ShowAlertDialog showAlertDialog;
    Context context = this;
    GlobalApplication g;
    public String srvrMessage;
    AlertDialog alertDialog;
    Boolean dosyaAktarimiVarMi = false;
    int kacinci = 1;
    SharedPreferences preferences;
    Switch switchGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_settings);

        preferences = this.getSharedPreferences("MyPreferences",
                Context.MODE_PRIVATE);

        LocalBroadcastManager.getInstance(context).registerReceiver(rec, new IntentFilter("myevent"));

        g = (GlobalApplication) getApplicationContext();

        AlertDialog.Builder aBuilder = new AlertDialog.Builder(context);
        aBuilder.setTitle("Aktarım");
        aBuilder.setMessage("Dosya Alımı Tamamlandı")
                .setCancelable(false);
        aBuilder.setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                g.bitmapDictionary = g.getImages();
            }
        });
        alertDialog = aBuilder.create();

        btnSave = (Button) findViewById(R.id.button);
        btnCancel = (Button) findViewById(R.id.btnIptal);

        switchGame = (Switch) findViewById(R.id.switchGame);
        switchGame.setChecked(preferences.getBoolean("canPlayGame",false));

        btnSave.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        if (preferences != null) {
            EditText textIP = (EditText) findViewById(R.id.editTextIP);
            textIP.setText(preferences.getString("IPAddress", ""));
            if (textIP.getText().toString().contentEquals("")) {
                textIP.setHint("192.168.2.1");
            }
            EditText textPort = (EditText) findViewById(R.id.editTextPort);
            textPort.setText(preferences.getString("Port", ""));
            if (textPort.getText().toString().contentEquals("")) {
                textPort.setHint("Port");
            }
            EditText textName = (EditText) findViewById(R.id.editTextTableName);

            InputFilter filter = new InputFilter() {
                public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                    for (int i = start; i < end; i++) {
                        if (source.charAt(i) == '<' || source.charAt(i) == '>' || source.charAt(i) == '&' || source.charAt(i) == '=' || source.charAt(i) == '*' || source.charAt(i) == '-') {
                            return "";
                        }
                    }
                    return null;
                }
            };

            textName.setFilters(new InputFilter[]{filter});

            textName.setText(preferences.getString("TabletName", ""));
            if (textName.getText().toString().contentEquals("")) {
                textName.setHint("Tablet Adı");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            veriGuncellemeyiBaslat();
        }*/
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.button:

                SharedPreferences.Editor editor = preferences.edit();
                try {
                    editor.putString("IPAddress", ((EditText) findViewById(R.id.editTextIP)).getText().toString
                            ());
                    editor.putString("Port", ((EditText) findViewById(R.id.editTextPort)).getText()
                            .toString());
                    editor.putString("TabletName", ((EditText) findViewById(R.id.editTextTableName)).getText()
                            .toString());
                    editor.putBoolean("canPlayGame", switchGame.isChecked());
                    editor.apply();
                    g.canPlayGame = switchGame.isChecked();
                    showAlertDialog = new ShowAlertDialog();
                    AlertDialog alertDialog = showAlertDialog.showAlert(context, "Kayıt başarılı",
                            "Ayarlar kayıt edildi.");
                    alertDialog.show();
                } catch (Exception e) {
                    showAlertDialog = new ShowAlertDialog();
                    AlertDialog alertDialog = showAlertDialog.showAlert(context, "Kayıt başarısız!",
                            "Ayarlar kayıt edilemedi.");
                    alertDialog.show();
                }
                break;
            case R.id.btnIptal:
                this.finish();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(rec);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(rec);
    }

    BroadcastReceiver rec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //all events will be received here
            //get message
            srvrMessage = intent.getStringExtra("message");
            if (srvrMessage != null) {
                String[] parametreler = srvrMessage.split("&");
                String[] esitlik;
                final Dictionary<String, String> collection = new Hashtable<String, String>(parametreler.length);
                for (String parametre : parametreler) {
                    esitlik = parametre.split("=");
                    if (esitlik.length == 2)
                        collection.put(esitlik[0], esitlik[1]);
                }
                String gelenkomut = collection.get("komut");

                GlobalApplication.Komutlar komut;

                try
                {
                    komut = GlobalApplication.Komutlar.valueOf(gelenkomut);
                }
                catch (Exception ex)
                {
                    komut = GlobalApplication.Komutlar.Default;
                }

                switch (komut)
                {
                    case guncellemeyiBaslat:
                        veriGuncellemeyiBaslat();
                        break;
                    case dosyalar:
                        kacinci = Integer.parseInt(collection.get("kacinci"));
                        kacinci++;
                        g.commonAsyncTask.client.sendMessage("komut=veriGonder&kacinci=" + kacinci + "&sadeceXML=0");
                        break;
                    case aktarimTamamlandi:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                alertDialog.show();
                                SetViewGroupEnabled.setViewGroupEnabled((RelativeLayout) findViewById(R.id.settings), true);
                                if(g.bitmapDictionary !=null && g.bitmapDictionary.size()>0)
                                    g.bitmapDictionary = null;
                            }
                        });
                        dosyaAktarimiVarMi = false;
                        break;
                    case modemBilgileri:
                        SharedPreferences.Editor editor = preferences.edit();
                        try {
                            editor.putString("SSID", collection.get("SSID"));
                            editor.putString("ModemSifresi", collection.get("Sifre"));
                            editor.apply();
                            showAlertDialog = new ShowAlertDialog();
                            AlertDialog alertDialog = showAlertDialog.showAlert(context, "Kayıt başarılı",
                                    "Modem bilgileri kayıt edildi.");
                            alertDialog.show();
                        } catch (Exception e) {
                            showAlertDialog = new ShowAlertDialog();
                            AlertDialog alertDialog = showAlertDialog.showAlert(context, "Kayıt başarısız!",
                                    "Modem bilgileri kayıt edilemedi.");
                            alertDialog.show();
                        }
                    default:
                        break;
                }


            }
        }
    };

    @Override
    public void onBackPressed() {
        if(!dosyaAktarimiVarMi)
            super.onBackPressed();
    }

    private void veriGuncellemeyiBaslat()
    {
        g.commonAsyncTask.client.sendMessage("komut=veriGonder&kacinci=1&sadeceXML=0");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SetViewGroupEnabled.setViewGroupEnabled((RelativeLayout) findViewById(R.id.settings), false);
            }
        });
        dosyaAktarimiVarMi = true;
    }


}
