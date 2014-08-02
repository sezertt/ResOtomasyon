package TCPClientSide;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.res_otomasyon.resotomasyon.GlobalApplication;

import ekclasslar.BitConverter;

import org.apache.http.util.EncodingUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Mustafa on 20.5.2014.
 */
public class TCPClient {

    public String serverMessage;
    public static String SERVERIP; //your computer IP address
    public static int SERVERPORT;
    private OnMessageReceived mMessageListener = null;
    public boolean mRun = false;
    public InputStream stream;
    public Socket socket;
    public PrintWriter out;
    public InetAddress serverAddr;
    Boolean aktarimVar = false;
    boolean serverStatus = false;
    Handler myHandler;

    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TCPClient(OnMessageReceived listener, Handler myHandler) {
        mMessageListener = listener;
        this.myHandler = myHandler;
    }

    //Bağlantının kopup kopmadığını anlamak için server a ping at.
    Timer timer = new Timer();

    class pingToServer extends TimerTask {
        @Override
        public void run() {
            try {
                serverStatus = serverAddr.isReachable(100);
                if (!serverStatus) {
                    mRun = false;
                    if (socket != null && socket.isConnected()) {
                        stream.close();
                        socket.close();
                        Log.e("Soket&Stream", "kapandı");
                    }
                    callStopPing();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void callPingToServer() {
        timer.schedule(new pingToServer(), 1500, 5000);
    }

    //Eğer bağlantı kopmuş ise ping atmayı durdur.
    void callStopPing() {
        timer.cancel();
    }

    public void sendMessage(String message) {
        if (out != null && !out.checkError()) {
            out.println(message);
            out.flush();
        }
    }

    public boolean getFolder() {
        byte[] buffer = new byte[1024 * 5000];
        try {
            int receivedBytesLength = socket.getInputStream().read(buffer);
            int fileNameLen = BitConverter.toInt32(buffer, 0);
            String fileName = EncodingUtils.getString(buffer, 4, fileNameLen, Charset.defaultCharset().name());

            String[] uzanti = fileName.split("\\.");

            if (uzanti[1].contentEquals("png")) {
                File folder = new File("/mnt/sdcard/shared/Lenovo/resimler/");
                if (!folder.exists()) {
                    folder.mkdirs();
                }
                File file = new File("/mnt/sdcard/shared/Lenovo/resimler/" + fileName + "");
                if (!file.exists())
                    file.createNewFile();
                else {
                    file.delete();
                    file.createNewFile();
                }
                FileOutputStream out = new FileOutputStream("/mnt/sdcard/shared/Lenovo/resimler/" + fileName + "");
                out.write(buffer, 4 + fileNameLen, receivedBytesLength - 4 - fileNameLen);
            } else {
                File folder = new File("/mnt/sdcard/shared/Lenovo/");
                if (!folder.exists()) {
                    folder.mkdirs();
                }
                File file = new File("/mnt/sdcard/shared/Lenovo/" + fileName + "");
                if (!file.exists())
                    file.createNewFile();
                else {
                    file.delete();
                    file.createNewFile();
                }
                FileOutputStream out = new FileOutputStream("/mnt/sdcard/shared/Lenovo/" + fileName + "");
                out.write(buffer, 4 + fileNameLen, receivedBytesLength - 4 - fileNameLen);
            }

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void stopClient() throws IOException {
        if (stream != null) {
            stream.close();
        }
        mRun = false;
    }

    public void run() {

        mRun = true;
        try {
            //here you must put your computer's IP address.
            serverAddr = InetAddress.getByName(SERVERIP);
            //Server'a ping atmayı başlat.
            callPingToServer();
            //create a socket to make the connection with the server
            socket = new Socket();
            SocketAddress socketAddress = new InetSocketAddress(serverAddr, SERVERPORT);
            Log.e("TCP Client", "C: Connecting...");
            socket.connect(socketAddress, 1500);
            //Eğer soket bağlantısı kurulabilirse handler'a mesaj gönder.
            if (socket.isConnected())
                myHandler.sendEmptyMessage(2);
            try {
                socket.setKeepAlive(true);
                //send the message to the server
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                Log.e("client.out", "Oluştu");
                String copyOfServerMessage = "";
                Log.e("TCP Client", "C: Sent.");

                Log.e("TCP Client", "C: Done.");
                byte FIRST_BYTE = (byte) 60;
                //>
                byte LAST_BYTE = (byte) 62;

                stream = socket.getInputStream();
                Boolean dosyaAlimiBasariliMi = false;

                while (mRun) {
                    if (!aktarimVar) {
                        int firstByte = stream.read();

                        if ((byte) firstByte != FIRST_BYTE) {
                            //Başlangıç byte'ı değil, bu karakteri atla!
                            continue;
                        }

                        //Mesajı oku
                        ArrayList<Byte> bList = new ArrayList<Byte>();

                        while ((byte) (firstByte = stream.read()) != LAST_BYTE) {
                            bList.add((byte) firstByte);
                        }

                        byte[] result = new byte[bList.size()];

                        for (int i = 0; i < bList.size(); i++) {
                            result[i] = bList.get(i).byteValue();
                        }
                        serverMessage = new String(result, "UTF-8");
                        copyOfServerMessage = serverMessage;
                    } else {
                        dosyaAlimiBasariliMi = getFolder();
                        serverMessage += "&aktarim=" + dosyaAlimiBasariliMi;
                        copyOfServerMessage = "";
                        aktarimVar = false;
                    }
                    if (copyOfServerMessage.length() > 13) {
                        if (copyOfServerMessage.substring(0, 14).contentEquals("komut=dosyalar")) {
                            aktarimVar = true;
                            continue;
                        }
                    } else if (serverMessage.contentEquals("komut=aktarimTamamlandi")) {
                        aktarimVar = false;
                    }

                    if (serverMessage != null && mMessageListener != null) {
                        mMessageListener.messageReceived(serverMessage);
                    }
                }

                Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + serverMessage + "'");

            } catch (Exception e) {
                Log.e("TCP", "S: Error", e);
                //Bağlantı kopar veya bir hata oluşur ise ping atmayı durdur.
                callStopPing();
                mRun = false;

            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
                //Bağlantı kopar veya bir hata oluşur ise ping atmayı durdur.
                callStopPing();
                mRun = false;
            }

        } catch (Exception e) {
            Log.e("TCP", "C: Error", e);
            //Bağlantı kopar veya bir hata oluşur ise ping atmayı durdur.
            callStopPing();
            mRun = false;
        }

    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}