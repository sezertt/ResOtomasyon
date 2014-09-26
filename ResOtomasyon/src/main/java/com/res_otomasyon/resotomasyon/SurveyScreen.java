package com.res_otomasyon.resotomasyon;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import java.util.Dictionary;
import java.util.Hashtable;

import ekclasslar.ScrollViewFullScreenAdjustment;
import ekclasslar.Survey;


public class SurveyScreen extends Activity {

    GlobalApplication g;
    Survey survey;
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_screen);
        ScrollViewFullScreenAdjustment.assistActivity(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        g = (GlobalApplication) getApplicationContext();
        survey = new Survey(this, g);
        LocalBroadcastManager.getInstance(context).registerReceiver(rec, new IntentFilter("myevent"));
        g.commonAsyncTask.client.sendMessage("komut=anketIstegi");
    }


    BroadcastReceiver rec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String srvrMessage = intent.getStringExtra("message");
            String[] parameters = srvrMessage.split("&");
            String[] sides;

            final Dictionary<String, String> collection = new Hashtable<String, String>(parameters.length);
            for (String parameter : parameters) {
                sides = parameter.split("=");
                if (sides.length == 2)
                    collection.put(sides[0], sides[1]);
            }

            if (collection.get("komut").contentEquals("anketIstegi")) { // anket isteği mesajına gelen cevap
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String[] questionsAndPlaces = collection.get("sorular").split("\\*");
                        survey.createSurvey(questionsAndPlaces);
                    }
                });
            } else // diğer mesajlar hata vs.
            {

            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.survey_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
