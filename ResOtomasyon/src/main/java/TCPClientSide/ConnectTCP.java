package TCPClientSide;

import android.os.AsyncTask;

/**
 * Created by Mustafa on 1.7.2014.
 */
public class ConnectTCP {

    private TCPClient mTCPClient;
    public TCPClient getmTCPClient() { return mTCPClient; }
    public void setmTCPClient(TCPClient mTCPClient) {this.mTCPClient = mTCPClient;}

    private static final ConnectTCP holder = new ConnectTCP();
    public static ConnectTCP getInstance() {return holder;}
}
