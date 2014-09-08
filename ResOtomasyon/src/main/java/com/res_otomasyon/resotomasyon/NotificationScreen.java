package com.res_otomasyon.resotomasyon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import java.util.Dictionary;
import java.util.Hashtable;
import ekclasslar.NotificationExpandableAdapter;

public class NotificationScreen extends ActionBarActivity {
    GlobalApplication g;
    public String srvrMessage;
    NotificationExpandableAdapter adapter;
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        g = (GlobalApplication) getApplicationContext();
        LocalBroadcastManager.getInstance(context).registerReceiver(rec, new IntentFilter("myevent"));
        final NotificationExpandableAdapter expandableAdapter = new NotificationExpandableAdapter(this, g.lstMasaninSiparisleri, g);
        ExpandableListView listView = (ExpandableListView) findViewById(R.id.expandable_notification_listview);
        adapter = expandableAdapter;
        listView.setAdapter(adapter);
    }


    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(rec);
        super.onPause();
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(rec);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.notifications, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
        /*
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
        */
    }

    BroadcastReceiver rec;
    {
        rec = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //all events will be received here
                //get message
                srvrMessage = intent.getStringExtra("message");
                String[] parametreler = srvrMessage.split("&");
                String[] esitlik;
                final Dictionary<String, String> collection = new Hashtable<String, String>(parametreler.length);
                for (String parametre : parametreler) {
                    esitlik = parametre.split("=");
                    if (esitlik.length == 2)
                        collection.put(esitlik[0], esitlik[1]);
                }
                String gelenkomut = collection.get("komut");
                GlobalApplication.Komutlar komut = GlobalApplication.Komutlar.valueOf(gelenkomut);
                switch (komut) {
                    case siparis:
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        break;
                    default:
                        break;
                }
//                myHandler.sendEmptyMessage(1);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

//    public Handler myHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case 1:
//
//                    break;
//            }
//        }
//    };

}
