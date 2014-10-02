package com.res_otomasyon.resotomasyon;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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


public class LoginScreen extends ActionBarActivity implements View.OnClickListener {

    Intent intent;
    Button btnGiris;
    final Context context = this;
    ArrayList<Employee> lstEmployees;
    boolean masaKilitliMi = false;
    SharedPreferences preferences = null;
    boolean activityVisible = true;
    GlobalApplication g;
    Menu menu;
    TryConnection t;
    Employee employee;


    public boolean kilitliMasayaGit() {
        FileIO fileIO = new FileIO();
        List<File> files = null;
        try {
            files = fileIO.getListFiles(new File(Environment.getExternalStorageDirectory().getPath() + "/shared/Lenovo"));
        } catch (Exception ex) {
            intent = new Intent(LoginScreen.this, Settings.class);
            startActivity(intent);
        }

        if (files != null) {
            ReadXML readXML = new ReadXML();
            lstEmployees = readXML.readEmployees(files);
        }

        preferences = this.getSharedPreferences("KilitliMasa", Context.MODE_PRIVATE);
        masaKilitliMi = preferences.getBoolean("MasaKilitli", false);
        employee = new Employee();
        if (masaKilitliMi && g.baglantiVarMi) {
            this.setVisible(false);
            employee.PinCode = preferences.getString("PinCode", "0000");
            employee.Title = preferences.getString("Title", null);
            Set<String> setPermissions = preferences.getStringSet("Permission", null);
            employee.Permissions = setPermissions.toArray(new String[setPermissions.size()]);
            employee.UserName = preferences.getString("UserName", null);
            employee.Name = preferences.getString("Name", null);
            employee.LastName = preferences.getString("LastName", null);
            intent = new Intent(LoginScreen.this, MasaEkrani.class);
            intent.putExtra("Employee", employee);
            startActivity(intent);
        } else {
            if(menu != null)
                menu.setGroupVisible(0,true);
            LocalBroadcastManager.getInstance(context).registerReceiver(rec, new IntentFilter("myevent"));
            this.setVisible(true);
            ((EditText) findViewById(R.id.editTextPin)).setText("");
            activityVisible = true;
            if (g == null) {
                g = (GlobalApplication) getApplicationContext();
            }
            if (t == null)
                t = new TryConnection(g, myHandler);

            if (!g.baglantiVarMi && !t.timerRunning) {
                t.startTimer();
            }
        }
        return masaKilitliMi;
    }

    @Override
    protected void onResume() {
        btnGiris.setEnabled(true);
//        g.globalDepartmanlar.clear();

//        NetworkChangeReceiver checkNetwork = new NetworkChangeReceiver();
//        checkNetwork.onReceive(context,intent);


        kilitliMasayaGit();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (!masaKilitliMi)
            LocalBroadcastManager.getInstance(context).unregisterReceiver(rec);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (!masaKilitliMi)
            LocalBroadcastManager.getInstance(context).unregisterReceiver(rec);
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
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        btnGiris = (Button) findViewById(R.id.btnGiris);
        btnGiris.setOnClickListener(this);

        EditText pin = (EditText) findViewById(R.id.editTextPin);

        pin.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (btnGiris.isEnabled())
                        btnGiris.callOnClick();
                }
                return false;
            }
        });

        if (g.commonAsyncTask.client != null) {
            if (g.commonAsyncTask.client.out != null) {
                LoginScreen.this.getSupportActionBar().setTitle(getString(R.string.app_name) + "(Bağlı)");
            } else {
                LoginScreen.this.getSupportActionBar().setTitle(getString(R.string.app_name) + "(Bağlantı yok)");
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
                                LoginScreen.this.getSupportActionBar().setTitle(getString(R.string.app_name) + "(Bağlantı yok)");
                                EditText e = (EditText) findViewById(R.id.editTextPin);
                                btnGiris.setEnabled(false);
                                if(masaKilitliMi)
                                    menu.setGroupVisible(0,false);
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
        // geri basınca birşey yapmasın diye
    }

    public Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    //Server ile bağlantı kurulup kurulmadığını kontrol etmek için gönderilen mesaj.
                    preferences = LoginScreen.this.getSharedPreferences("MyPreferences",
                            Context.MODE_PRIVATE);
                    String girisKomutu = "komut=giris&nick=" + preferences.getString("TabletName", "Tablet");
                    EditText e = (EditText) findViewById(R.id.editTextPin);
                    if (g.commonAsyncTask.client != null) {
                        if (g.commonAsyncTask.client.out != null) {
                            g.commonAsyncTask.client.sendMessage(girisKomutu);
                            e.setFocusableInTouchMode(true);
                            e.setFocusable(true);
                            btnGiris.setEnabled(true);
                            LoginScreen.this.getSupportActionBar().setTitle(getString(R.string.app_name) + "(Bağlı)");
                            if (t != null && t.timer != null)
                                t.stopTimer();
                            if(kilitliMasayaGit())
                                activityVisible = false;
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
        if (id == R.id.masaSec) {
            intent = new Intent(LoginScreen.this, MasaSecEkrani.class);
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
                            if (passCorrect) {
                                getCurrentEmployee = i;
                                break;
                            }
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
                    employee = new Employee();
                    employee.Name = lstEmployees.get(getCurrentEmployee).Name;
                    employee.PinCode = lstEmployees.get(getCurrentEmployee).PinCode;
                    employee.Permissions = lstEmployees.get(getCurrentEmployee).Permissions;
                    employee.UserName = lstEmployees.get(getCurrentEmployee).UserName;
                    employee.LastName = lstEmployees.get(getCurrentEmployee).LastName;
//                    e.PassWord = lstEmployees.get(getCurrentEmployee).PassWord;
//                    e.Title = lstEmployees.get(getCurrentEmployee).Title;
//                    lstEmployees.removeAll(lstEmployees);
//                    lstEmployees.add(e);
                    intent = new Intent(LoginScreen.this, MasaEkrani.class);
                    intent.putExtra("Employee", employee);
                    startActivity(intent);
                    btnGiris.setEnabled(false);
                } else {
                    AlertDialog.Builder aBuilder = new AlertDialog.Builder(context);
                    aBuilder.setTitle("Hatalı Pin");
                    aBuilder.setMessage("Hatalı pin kodu girdiniz.").setCancelable(false)
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
            default:
                break;
        }
    }
}