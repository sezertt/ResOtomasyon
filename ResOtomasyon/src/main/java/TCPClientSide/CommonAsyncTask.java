package TCPClientSide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.res_otomasyon.resotomasyon.GlobalApplication;

public class CommonAsyncTask extends AsyncTask<Handler, String, String> {

    OnAsyncRequestComplete caller;
    public Context context;
    public TCPClient client;
    LocalBroadcastManager localBroadcastManager;
    android.os.Handler myHandler;
    GlobalApplication g;

    public CommonAsyncTask(Activity a, android.os.Handler myHandler,GlobalApplication g) {
        caller = (OnAsyncRequestComplete) a;
        context = a;
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
        this.myHandler = myHandler;
        this.g = g;
    }

    public interface OnAsyncRequestComplete {
        public void asyncResponse(String mesaj);
    }


    @Override
    protected String doInBackground(Handler... params) {
        client = new TCPClient(new TCPClient.OnMessageReceived() {
            @Override
            public void messageReceived(String message) {
                Intent intent = new Intent("myevent");
                intent.putExtra("message", message);
                localBroadcastManager.sendBroadcastSync(intent);
            }
        }, myHandler,g);
        client.run();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent("myevent");
        intent.putExtra("message", "komut=baglanti&durum=koptu");
        localBroadcastManager.sendBroadcastSync(intent);
        Log.e("BroadCast:", "Baglanti Koptu.");
        return null;
    }

    @Override
    protected void onPostExecute(String message) {
//        caller.asyncResponse(message);
        super.onPostExecute(message);
    }
}
