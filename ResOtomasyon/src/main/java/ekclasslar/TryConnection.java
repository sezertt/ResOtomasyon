package ekclasslar;

import android.os.Handler;

import com.res_otomasyon.resotomasyon.GlobalApplication;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Mustafa on 5.8.2014.
 */
public class TryConnection extends TimerTask {

    public GlobalApplication globalApplication;
    public Handler handler;
    public Timer timer;
    public boolean timerRunning = false;

    public TryConnection(GlobalApplication globalApplication, Handler handler) {
        this.globalApplication = globalApplication;
        this.handler = handler;
    }

    @Override
    public void run() {
        globalApplication.connectServer(handler);
    }

    public boolean startTimer() {
        timer = new Timer();
        timer.schedule(new TryConnection(globalApplication, handler), 0, 10000);
        timerRunning = true;
        return timerRunning;
    }

    public void stopTimer() {
        timer.cancel();
        timer.purge();
        timerRunning = false;
    }
}
