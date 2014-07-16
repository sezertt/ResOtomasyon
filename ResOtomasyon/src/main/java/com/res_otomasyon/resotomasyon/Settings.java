package com.res_otomasyon.resotomasyon;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import TCPClientSide.TCPClient;


public class Settings extends Activity implements View.OnClickListener {

    EditText editText;
    Button btnSave;
    Button btnCancel;
    ShowAlertDialog showAlertDialog;
    Context context = this;
    connectTask cnnTask;

    TCPClient mTcpClient;
    public String srvrMessage;
    boolean mesajGeldi = false;

    String dosyaIsteKomutu;

    int kacinci = 1;
    int kacDosya;

    public enum Komutlar {
        dosyalar;
    }

    public class connectTask extends AsyncTask<String, String, TCPClient> {

        @Override
        protected TCPClient doInBackground(String... message) {

            //we create a TCPClient object
            mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    srvrMessage = message;
                    if (srvrMessage != null) {
                        mesajGeldi = true;
                    }
                    int count = 0;

                    String[] parametreler = srvrMessage.split("&");
                    String[] esitlik;
                    final Dictionary<String, String> collection = new Hashtable<String, String>(parametreler.length);
                    for (String parametre : parametreler) {
                        esitlik = parametre.split("=");
                        if (esitlik.length == 2)
                            collection.put(esitlik[0], esitlik[1]);
                    }
                    String gelenkomut = collection.get("komut");
                    Komutlar komut = Komutlar.valueOf(gelenkomut);

                    kacinci = Integer.parseInt(collection.get("kacinci"));

                    kacDosya = Integer.parseInt(collection.get("kacDosya"));
                    switch (komut) {
                        case dosyalar:
                            if(mTcpClient.getFolder()) // dosya gönderimi başarılı
                            {
                                myHandler.sendEmptyMessage(0);
                            }
                            else // dosya gönderimi başarısız
                            {
                                myHandler.sendEmptyMessage(1);
                            }
                            break;
                    }
                }
            });
            mTcpClient.run();
            return null;
        }
    }

    public Handler myHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            kacinci++;
                            dosyaIsteKomutu = "<komut=veriGonder&kacinci=" + kacinci + ">";
                            if (kacinci <= kacDosya)
                                mTcpClient.sendMessage(dosyaIsteKomutu);
                            else
                                onDestroy();
                        }
                    });
                    break;
                case 1:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dosyaIsteKomutu = "<komut=veriGonder&kacinci=" + kacinci + ">";
                            mTcpClient.sendMessage(dosyaIsteKomutu);
                        }
                    });
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        btnSave = (Button) findViewById(R.id.button);
        btnCancel = (Button) findViewById(R.id.btnIptal);
        btnSave.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        SharedPreferences preferences = this.getSharedPreferences("MyPreferences",
                Context.MODE_PRIVATE);
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
    protected void onDestroy() {
        try {
            if(mTcpClient !=null) {
                mTcpClient.stopClient();
            }
            kacinci = 1;
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            try {
                SharedPreferences preferences = null;
                preferences = this.getSharedPreferences("MyPreferences",
                        Context.MODE_PRIVATE);
                cnnTask = new connectTask();
                mTcpClient.SERVERIP = preferences.getString("IPAddress", "0");
                mTcpClient.SERVERPORT = Integer.parseInt(preferences.getString("Port", "13759"));
                cnnTask.execute("");
            } catch (Exception e) {
                showAlertDialog = new ShowAlertDialog();
                AlertDialog alertDialog = showAlertDialog.showAlert(this, context, "Uyarı!",
                        "Önce ayarları doldurunuz.");
                alertDialog.show();
            }
            kacinci = 1;
            dosyaIsteKomutu = "<komut=veriGonder&kacinci=" + kacinci + ">";
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mTcpClient.sendMessage(dosyaIsteKomutu);

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.button:
                SharedPreferences preferences = this.getSharedPreferences("MyPreferences",
                        Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                try {
                    editor.putString("IPAddress", ((EditText) findViewById(R.id.editTextIP)).getText().toString
                            ());
                    editor.putString("Port", ((EditText) findViewById(R.id.editTextPort)).getText()
                            .toString());
                    editor.putString("TabletName", ((EditText) findViewById(R.id.editTextTableName)).getText()
                            .toString());
                    editor.commit();
                    showAlertDialog = new ShowAlertDialog();
                    AlertDialog alertDialog = showAlertDialog.showAlert(this, context, "Kayıt başarılı",
                            "Ayarlar kayıt edildi.");
                    alertDialog.show();
                } catch (Exception e) {
                    showAlertDialog = new ShowAlertDialog();
                    AlertDialog alertDialog = showAlertDialog.showAlert(this, context, "Kayıt başarısız!",
                            "Ayarlar kayıt edilemedi.");
                    alertDialog.show();
                }
                break;
            case R.id.btnIptal:
                this.finish();
                break;
        }
    }
}
