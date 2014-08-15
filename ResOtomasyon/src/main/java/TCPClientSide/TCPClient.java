package TCPClientSide;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import org.apache.http.util.EncodingUtils;

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

import ekclasslar.BitConverter;

/**
 * Created by Mustafa on 20.5.2014.
 */
public class TCPClient {

    public String serverMessage;
    public static String SERVERIP; //your computer IP address
    public static int SERVERPORT;
    private OnMessageReceived mMessageListener = null;
    public boolean mRun = false, dosyaAlimiBasariliMi;
    ;
    public InputStream stream;
    public Socket socket;
    public PrintWriter out;
    public InetAddress serverAddr;
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

    static Boolean IsSocketConnected(Socket s) throws IOException {
        //The long, but simpler-to-understand version:

        Boolean part1 = s.isOutputShutdown();
        Boolean part2 = (s.getInputStream().available() == 0);
        if ((part1 && part2 ) || !s.isConnected())
            return false;
        else
            return true;
    }


    class pingToServer extends TimerTask {
        @Override
        public void run() {
            try {
                serverStatus = serverAddr.isReachable(100);
//                serverStatus = IsSocketConnected(socket);
                if(!serverStatus)
                {
                    mRun=false;
                    if (socket != null && socket.isConnected()) {
                        try {
                            stream.close();
                            socket.close();
                            Log.e("Soket&Stream", "kapandı");
                        }
                        catch (Exception ex)
                        {
                            Log.e("Soket&Stream", "Kapanırken Hata Verdi");
                        }
                    }
                    callStopPing();
                }
            } catch (IOException e) {
                mRun = false;
                Log.e("Soket", "Bağlantı Koptu");

                if (socket != null && socket.isConnected()) {
                    try {
                        stream.close();
                        socket.close();
                        Log.e("Soket&Stream", "kapandı");
                    }
                    catch (Exception ex)
                    {
                        Log.e("Soket&Stream", "Kapanırken Hata Verdi");
                        e.printStackTrace();
                    }
                }
                callStopPing();
            }
        }
    }

    void callPingToServer() {
        timer.schedule(new pingToServer(), 3000, 5000);
    }

    //Eğer bağlantı kopmuş ise ping atmayı durdur.
    void callStopPing() {
        timer.cancel();
        timer.purge();
    }

    public void sendMessage(String message) {
        String komutMessage = "<" + message + ">";
        if (out != null && !out.checkError()) {
            out.println(komutMessage);
            out.flush();
        }
    }

    class AsyncTaskClass extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            //uzun islem oncesi yapilacaklar
        }

        @Override
        protected String doInBackground(String... strings) {

            //uzun islem sirasinda yapilacaklar
            try {

            } catch (Exception ex) {

            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            //uzun islem bitince yapilacaklar
        }
    }


    public void getFolder() throws Exception {
        try {
            byte[] length = new byte[4];

            stream.read(length, 0, 4);

            int[] arrayimiz = new int[4];

            arrayimiz[0] = (length[0] & 0xff);
            arrayimiz[1] = (length[1] & 0xff);
            arrayimiz[2] = (length[2] & 0xff);
            arrayimiz[3] = (length[3] & 0xff);

            int fileDataLen = arrayimiz[0] * 1 + arrayimiz[1] * 256 + arrayimiz[2] * 65536 + arrayimiz[3] * 16777216;

            stream.read(length, 0, 4);
            int fileNameLen = BitConverter.toInt32(length, 0);

            byte[] fileNameBuffer = new byte[fileNameLen];
            stream.read(fileNameBuffer, 0, fileNameLen);
            String fileName = EncodingUtils.getString(fileNameBuffer, Charset.defaultCharset().name());

            String[] uzanti = fileName.split("\\.");

            byte[] buffer = new byte[fileDataLen];

            try {
                int bytesNeeded = fileDataLen;
                int bytesReceived = 0;
                do {
                    //read byte from client
                    int bytesRead = stream.read(buffer, bytesReceived, bytesNeeded - bytesReceived);
                    bytesReceived += bytesRead;
                    // merge byte array to another byte array
                } while (bytesReceived < bytesNeeded);   //  <- you should do this async.
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            try {
                FileOutputStream fOutStream;
                if (uzanti[1].contentEquals("png")) {
                    File folder = new File("/mnt/sdcard/shared/Lenovo/resimler/");
                    if (!folder.exists()) {
                        folder.mkdirs();
                    }
                    fOutStream = new FileOutputStream("/mnt/sdcard/shared/Lenovo/resimler/" + fileName + "", false);
                } else {
                    File folder = new File("/mnt/sdcard/shared/Lenovo/");
                    if (!folder.exists()) {
                        folder.mkdirs();
                    }
                    fOutStream = new FileOutputStream("/mnt/sdcard/shared/Lenovo/" + fileName + "", false);
                }
                fOutStream.write(buffer, 0, fileDataLen);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
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

            try {
                socket.setKeepAlive(true);
                //send the message to the server
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                if (socket.isConnected())
                    myHandler.sendEmptyMessage(2);
                Log.e("client.out", "Oluştu");
                Log.e("TCP Client", "C: Sent.");

                Log.e("TCP Client", "C: Done.");
                byte FIRST_BYTE = (byte) 60;
                //>
                byte LAST_BYTE = (byte) 62;

                stream = socket.getInputStream();

                while (mRun) {
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
                            result[i] = bList.get(i);
                        }
                        serverMessage = new String(result, "UTF-8");

                    if (serverMessage.length() > 13)
                        if (serverMessage.substring(0, 14).contentEquals("komut=dosyalar")) {
                            getFolder();
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