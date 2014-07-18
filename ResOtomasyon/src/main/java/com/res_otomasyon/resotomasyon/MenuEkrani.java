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
import android.text.InputType;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ekclasslar.FileIO;
import ekclasslar.UrunBilgileri;
import Entity.Employee;
import Entity.Urunler;
import HashPassword.passwordHash;
import TCPClientSide.CommonAsyncTask;
import TCPClientSide.ConnectTCP;
import TCPClientSide.TCPClient;
import XMLReader.ReadXML;


public class MenuEkrani extends Activity implements CommonAsyncTask.OnAsyncRequestComplete {

    Menu menu;

    public Handler myHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    try {
                        try {
                            SharedPreferences preferences = context.getSharedPreferences("KilitliMasa",
                                    Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            if (!masaKilitliMi) {

                                try {
                                    if (!masaKilitliMi)
                                        masaKilitliMi = true;
                                    editor.putString("PinCode", lstEmployee.get(0).PinCode);
                                    editor.putString("Name", lstEmployee.get(0).Name);
                                    editor.putString("LastName", lstEmployee.get(0).LastName);
                                    editor.putBoolean("MasaKilitli", masaKilitliMi);
                                    editor.putString("Title", lstEmployee.get(0).Title);
                                    editor.putString("masaAdi",masaAdi);
                                    editor.putString("departmanAdi",departmanAdi);
                                    Set<String> mySet = new HashSet<String>(Arrays.asList(lstEmployee.get
                                            (0).Permissions));
                                    editor.putStringSet("Permission", mySet);
                                    editor.putString("UserName", lstEmployee.get(0).UserName);
                                    editor.commit();
                                } catch (Exception e) {

                                }
                            } else {
//                                passCorrect = passwordHash.validatePassword(m_Text,
//                                        lstEmployee.get(0).PinCode);
//                                if (passCorrect && masaKilitliMi) {
//                                    masaKilitliMi = false;
//                                    item.setTitle(R.string.masa_ac);
//                                    editor.putBoolean("MasaKilitli", masaKilitliMi);
//                                    editor.commit();
//                                }
                                if (masaKilitliMi) {
                                    masaKilitliMi = false;
                                    item.setTitle(R.string.masa_ac);
                                    editor.putBoolean("MasaKilitli", masaKilitliMi);
                                    editor.commit();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } catch (Exception ex) {


                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        if (!masaKilitliMi)
            this.finish();
    }

    @Override
    public void onBackPressed() {
        if (masaKilitliMi)
            return;
        else
            super.onBackPressed();
    }

    @Override
    protected void onResume() {

        super.onResume();
    }


    String departmanAdi, masaAdi;
    TCPClient mTCPClient;
    String message;
    private String m_Text = "";
    ArrayList<Employee> lstEmployee;

    boolean masaKilitliMi = false;
    boolean passCorrect = false;
    Context context = this;
    MenuItem item;

    ArrayList<Urunler> lstProducts;


    // more efficient than HashMap for mapping integers to objects
    SparseArray<UrunBilgileri> groups = new SparseArray<UrunBilgileri>();

    BroadcastReceiver rec = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            //all events will be received here
            //get message
            String message = intent.getStringExtra("message");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mTCPClient = ConnectTCP.getInstance().getmTCPClient();
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_menu_ekrani);
        Bundle extras = getIntent().getExtras();
        departmanAdi = extras.getString("DepartmanAdi");
        masaAdi = extras.getString("MasaAdi");
        lstEmployee = (ArrayList<Employee>) extras.getSerializable("lstEmployees");
        FileIO fileIO = new FileIO();
        List<File> files = null;

        try {
            files = fileIO.getListFiles(new File("/mnt/sdcard/shared/Lenovo"));
        } catch (Exception ex) {
        }

        ReadXML readUrun = new ReadXML();
        lstProducts = readUrun.readUrunler(files);
        LocalBroadcastManager.getInstance(this).registerReceiver(rec, new IntentFilter("myevent"));

        createData();
        ExpandableListView listView = (ExpandableListView) findViewById(R.id.listView);
        MyExpandableListAdapter adapter = new MyExpandableListAdapter(this,
                groups);
        listView.setAdapter(adapter);
    }


    public void createData() {
        int j = 0;
        for (int i = 0; i < lstProducts.size(); i++) {
            UrunBilgileri group = new UrunBilgileri(lstProducts.get(i).urunKategorisi);
            if (i + 1 < lstProducts.size()) {
                while (lstProducts.get(i).urunKategorisi.contentEquals(lstProducts.get(i + 1).urunKategorisi)) {
                    group.productName.add(lstProducts.get(i).urunAdi);
                    group.productPrice.add(lstProducts.get(i).porsiyonFiyati);
                    group.productInfo.add(lstProducts.get(i).urunAciklamasi);

                    i++;
                    if (i + 1 >= lstProducts.size())
                        break;
                }
            }
            groups.append(j, group);
            j++;
        }
    }

    @Override
    protected void onStop() {
        ConnectTCP.getInstance().setmTCPClient(mTCPClient);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        ConnectTCP.getInstance().setmTCPClient(mTCPClient);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(rec);
        super.onDestroy();
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ekrani, menu);
        this.menu = menu;
        masaKilitliMi = context.getSharedPreferences("KilitliMasa", Context.MODE_PRIVATE).getBoolean
                ("MasaKilitli", masaKilitliMi);
        if (masaKilitliMi) {
            this.item = menu.findItem(R.id.action_lockTable);
            item.setTitle(R.string.masa_ac);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        this.item = item;
        switch (id) {
            case R.id.action_lockTable:
                if (item.getTitle().toString().contentEquals("Masayı Kilitle")) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Masa Kilitle");
                    builder.setPositiveButton("Masayı Kilitle", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            myHandler.sendEmptyMessage(0);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                    return false;
                } else if (item.getTitle().toString().contentEquals("Masa Kilidini Kaldır")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Title");

                    final EditText input = new EditText(context);
                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    builder.setView(input);

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            m_Text = input.getText().toString();
                            myHandler.sendEmptyMessage(0);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                }
                break;

            case R.id.action_hesap:
                Intent intent = new Intent(MenuEkrani.this, HesapEkrani.class);
                startActivity(intent);
                break;

            case R.id.action_masaTemizleyin:

                break;

            case R.id.action_gorusOneri:

                break;

            case R.id.action_garsonIstiyorum:

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void asyncResponse(String mesaj) {
        message = mesaj;
    }
}


