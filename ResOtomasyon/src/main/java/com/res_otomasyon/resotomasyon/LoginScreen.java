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
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import ekclasslar.FileIO;
import Entity.Employee;
import HashPassword.passwordHash;
import XMLReader.ReadXML;
import ekclasslar.TryConnection;


public class LoginScreen extends Activity implements View.OnClickListener {

    Intent intent;
    Button btnGiris;
    final Context context = this;
    ArrayList<Employee> lstEmployees;
    boolean MasaKilitliMi = false;
    SharedPreferences preferences = null;
    boolean activityVisible = true;
    GlobalApplication g;
    Menu menu;
    TryConnection t;

    @Override
    protected void onResume() {
        FileIO fileIO = new FileIO();
        List<File> files = null;
        try {
            files = fileIO.getListFiles(new File("/mnt/sdcard/shared/Lenovo"));
        } catch (Exception ex) {
            intent = new Intent(LoginScreen.this, Settings.class);
            startActivity(intent);
        }
        if (files != null) {
            ReadXML readXML = new ReadXML();
            lstEmployees = readXML.readEmployees(files);
        }

        preferences = this.getSharedPreferences("KilitliMasa", Context.MODE_PRIVATE);
        MasaKilitliMi = preferences.getBoolean("MasaKilitli", false);

        if (MasaKilitliMi) {
            this.setVisible(false);
            lstEmployees.get(0).PinCode = preferences.getString("PinCode", "0000");
            lstEmployees.get(0).Title = preferences.getString("Title", null);
            Set<String> setPermissions = preferences.getStringSet("Permission", null);
            lstEmployees.get(0).Permissions = setPermissions.toArray(new String[setPermissions.size()]);
            lstEmployees.get(0).UserName = preferences.getString("UserName", null);
            lstEmployees.get(0).Name = preferences.getString("Name", null);
            lstEmployees.get(0).LastName = preferences.getString("LastName", null);
            intent = new Intent(LoginScreen.this, MasaEkrani.class);
            intent.putExtra("lstEmployees", lstEmployees);
            startActivity(intent);
        } else {
            this.setVisible(true);
            ((EditText) findViewById(R.id.editTextPin)).setText("");
        }
        activityVisible = true;
        if (g == null)
            g = (GlobalApplication) getApplicationContext();
        if (!g.commonAsyncTask.client.mRun && !t.timerRunning) {
            t.startTimer();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        activityVisible = false;
        if (t.timerRunning)
            t.stopTimer();
        super.onPause();
    }

    @Override
    protected void onStop() {
        activityVisible = false;
        if (t.timerRunning)
            t.stopTimer();
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        g = (GlobalApplication) getApplicationContext();
        LocalBroadcastManager.getInstance(context).registerReceiver(rec, new IntentFilter("myevent"));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        btnGiris = (Button) findViewById(R.id.btnGiris);
        btnGiris.setOnClickListener(this);

        if (g.commonAsyncTask.client != null) {
            if (g.commonAsyncTask.client.out != null) {
                getActionBar().setTitle(getString(R.string.app_name) + "(Bağlı)");
            } else {
                getActionBar().setTitle(getString(R.string.app_name) + "(Bağlantı yok)");

            }
        }
        t = new TryConnection(g, myHandler);
    }

    BroadcastReceiver rec = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            //all events will be received here
            //get message
            String srvrMessage = intent.getStringExtra("message");
            if (srvrMessage != null) {
                String[] parametreler = srvrMessage.split("&");
                String[] esitlik;
                final Dictionary<String, String> collection = new Hashtable<String, String>(parametreler.length);
                for (String parametre : parametreler) {
                    esitlik = parametre.split("=");
                    if (esitlik.length == 2)
                        collection.put(esitlik[0], esitlik[1]);
                }
                final String gelenkomut = collection.get("komut");
                GlobalApplication.Komutlar komut = GlobalApplication.Komutlar.valueOf(gelenkomut);
                final String baglanti = collection.get("durum");

                if (komut.toString().contentEquals("baglanti") && baglanti.contentEquals("koptu")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (activityVisible) {
                                if (!t.timerRunning)
                                    t.startTimer();
                                getActionBar().setTitle(getString(R.string.app_name) + "(Bağlantı yok)");
                                EditText e = (EditText) findViewById(R.id.editTextPin);
                                btnGiris.setEnabled(false);
                                e.setFocusable(false);
                            }
                        }
                    });
                }
            }
        }
   };


    @Override
    public void onBackPressed() {
   }


    public Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    //Server ile bağlantı kurulup kurulmadığını kontrol etmek için gönderilen mesaj.
                    preferences = LoginScreen.this.getSharedPreferences("MyPreferences",
                            Context.MODE_PRIVATE);
                    String girisKomutu = "<komut=giris&nick=" + preferences.getString("TabletName", "Tablet") + ">";
                    EditText e = (EditText) findViewById(R.id.editTextPin);
                    if (g.commonAsyncTask.client != null) {
                        if (g.commonAsyncTask.client.out != null) {
                            g.commonAsyncTask.client.sendMessage(girisKomutu);
                            e.setFocusableInTouchMode(true);
                            e.setFocusable(true);
                            btnGiris.setEnabled(true);
                            getActionBar().setTitle(getString(R.string.app_name) + "(Bağlı)");
                            if (t != null && t.timer != null) {
                                t.stopTimer();
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login_screen, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
//        item = menu.findItem(R.id.action_settings);
//
//        if(((EditText)findViewById(R.id.editTextPin)).getText().toString().isEmpty()) {
//            item.setVisible(false);
//        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            intent = new Intent(LoginScreen.this, Settings.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnGiris:
                boolean passCorrect = false;
                int getCurrentEmployee = 0;
                String pass = ((EditText) findViewById(R.id.editTextPin)).getText().toString();
                try {
                    if (!pass.contentEquals("")) {
                        for (int i = 0; i < lstEmployees.size(); i++) {
                            passCorrect = passwordHash.validatePassword(pass, lstEmployees.get(i).PinCode);
                            if (passCorrect)
                                getCurrentEmployee = i;
                        }
                    } else {
                        AlertDialog.Builder aBuilder = new AlertDialog.Builder(context);
                        aBuilder.setTitle("Hatalı Pin");
                        aBuilder.setMessage("Pin kodu boş geçilemez").setCancelable(false)
                                .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ((EditText) findViewById(R.id.editTextPin)).setText("");
                                    }
                                });
                        AlertDialog alertDialog = aBuilder.create();
                        alertDialog.show();
                        return;
                    }
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                }
                if (passCorrect) {
                    Employee e = new Employee();
                    e.Name = lstEmployees.get(getCurrentEmployee).Name;
                    e.PinCode = lstEmployees.get(getCurrentEmployee).PinCode;
                    e.Permissions = lstEmployees.get(getCurrentEmployee).Permissions;
                    e.UserName = lstEmployees.get(getCurrentEmployee).UserName;
                    e.LastName = lstEmployees.get(getCurrentEmployee).LastName;
//                    e.PassWord = lstEmployees.get(getCurrentEmployee).PassWord;
//                    e.Title = lstEmployees.get(getCurrentEmployee).Title;
                    lstEmployees.removeAll(lstEmployees);
                    lstEmployees.add(e);
                    intent = new Intent(LoginScreen.this, MasaEkrani.class);
                    intent.putExtra("lstEmployees", lstEmployees);
                    startActivity(intent);
                } else {
                    AlertDialog.Builder aBuilder = new AlertDialog.Builder(context);
                    aBuilder.setTitle("Hatalı Pin");
                    aBuilder.setMessage("Hatalı pin kodu giridiniz.").setCancelable(false)
                            .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ((EditText) findViewById(R.id.editTextPin)).setText("");
                                }
                            });
                    AlertDialog alertDialog = aBuilder.create();
                    alertDialog.show();
                }
                break;
        }
    }
}