package com.res_otomasyon.resotomasyon;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.res_otomasyon.resotomasyon.LoginScreen;
import com.res_otomasyon.resotomasyon.R;

import java.io.IOException;

import TCPClientSide.ConnectTCP;
import TCPClientSide.TCPClient;

public class StartScreen extends Activity {

    TCPClient mTcpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setVisible(false);
        String sdcardReady = Environment.getExternalStorageState();

        while (!sdcardReady.contentEquals("mounted")) {
            sdcardReady = Environment.getExternalStorageState();
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_start_screen);
    }


    @Override
    protected void onStop() {
        try {
            mTcpClient = ConnectTCP.getInstance().getmTCPClient();
            if (mTcpClient != null) {
                try {
                    mTcpClient.stopClient();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mTcpClient = null;
        } catch (Exception e) {

        }
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.start_screen, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.setVisible(false);
        Intent intent = new Intent(getApplicationContext(), LoginScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
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
