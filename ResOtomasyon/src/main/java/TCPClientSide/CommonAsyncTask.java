package TCPClientSide;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import org.apache.http.NameValuePair;

import java.util.List;

/**
 * Created by Mustafa on 4.7.2014.
 */
public class CommonAsyncTask extends AsyncTask<Handler,String,String>{

    OnAsyncRequestComplete caller;
    public Context context;
    String method = "GET";
    List<NameValuePair> parameters = null;
    public TCPClient client;
    LocalBroadcastManager localBroadcastManager;
    android.os.Handler myHandler;

    public CommonAsyncTask(Activity a,android.os.Handler myHandler) {
        caller = (OnAsyncRequestComplete) a;
        context = a;
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
        this.myHandler = myHandler;
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
        });
        myHandler.sendEmptyMessage(2);
        client.run();
        return null;
    }

    @Override
    protected void onPostExecute(String message) {
        caller.asyncResponse(message);
        super.onPostExecute(message);
    }
}
