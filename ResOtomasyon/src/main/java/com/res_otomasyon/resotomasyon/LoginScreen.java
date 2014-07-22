package com.res_otomasyon.resotomasyon;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
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
import java.util.List;
import java.util.Set;

import ekclasslar.FileIO;
import Entity.Employee;
import HashPassword.passwordHash;
import XMLReader.ReadXML;


public class LoginScreen extends Activity implements View.OnClickListener {

    Intent intent;
    Button btnGiris;
    Button btnCikis;
    Context context = this;
    ArrayList<Employee> lstEmployees;
    boolean MasaKilitliMi = false;
    SharedPreferences preferences = null;

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if ((event.getKeyCode() == KeyEvent.KEYCODE_HOME)) {
            return true;

        } else {
            return super.dispatchKeyEvent(event);
        }

    }

    @Override
    protected void onResume() {
        preferences = this.getSharedPreferences("KilitliMasa",Context.MODE_PRIVATE);
        MasaKilitliMi = preferences.getBoolean("MasaKilitli",false);

        if(MasaKilitliMi)
        {
            this.setVisible(false);
            lstEmployees.get(0).PinCode = preferences.getString("PinCode", "0000");
            lstEmployees.get(0).Title = preferences.getString("Title",null);
            Set<String> setPermissions = preferences.getStringSet("Permission",null);
            lstEmployees.get(0).Permissions = setPermissions.toArray(new String[setPermissions.size()]);
            lstEmployees.get(0).UserName = preferences.getString("UserName",null);
            lstEmployees.get(0).Name = preferences.getString("Name", null);
            lstEmployees.get(0).LastName = preferences.getString("LastName",null);
            intent = new Intent(LoginScreen.this, MasaEkrani.class);
            intent.putExtra("lstEmployees", lstEmployees);
            startActivity(intent);
        }
        else {
            this.setVisible(true);
            ((EditText) findViewById(R.id.editTextPin)).setText("");
        }
        super.onResume();
    }

    @Override
    protected void onRestart() {
//        preferences = this.getSharedPreferences("KilitliMasa",Context.MODE_PRIVATE);
//        MasaKilitliMi = preferences.getBoolean("MasaKilitli",false);
//        if(MasaKilitliMi)
//        {
//            Button btn = (Button) findViewById(R.id.btnGiris);
//            btn.callOnClick();
//        }
        super.onRestart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login_screen);
        btnGiris = (Button) findViewById(R.id.btnGiris);
        btnCikis = (Button) findViewById(R.id.btnCikis);
        btnGiris.setOnClickListener(this);
        btnCikis.setOnClickListener(this);
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
//        MasaKilitliMi = preferences.getBoolean("MasaKilitli",false);
//        if(preferences !=null && MasaKilitliMi == true) {
//            lstEmployees.get(0).PinCode = preferences.getString("PinCode", "0000");
//            lstEmployees.get(0).Title = preferences.getString("Title",null);
//            Set<String> setPermissions = preferences.getStringSet("Permissions",null);
//            lstEmployees.get(0).Permissions = (String[]) setPermissions.toArray();
//            lstEmployees.get(0).UserName = preferences.getString("UserName",null);
//            lstEmployees.get(0).Name = preferences.getString("Name", null);
//            lstEmployees.get(0).LastName = preferences.getString("LastName",null);
//        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login_screen, menu);
        return true;
    }

    MenuItem item;

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
//                intent = new Intent(LoginScreen.this, MainScreen.class);
//                intent.putExtra("lstEmployees", lstEmployees);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
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
            case R.id.btnCikis:
                this.finish();
                break;
        }
    }
}
