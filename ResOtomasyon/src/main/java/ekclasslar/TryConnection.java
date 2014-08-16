package ekclasslar;

import android.os.Handler;
import com.res_otomasyon.resotomasyon.GlobalApplication;
import java.util.Timer;
import java.util.TimerTask;

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
        try {
            globalApplication.connectServer(handler);
        } catch (Exception ignored) {
        }
    }

    public void startTimer() {
        timer = new Timer();
        timer.schedule(new TryConnection(globalApplication, handler), 0, 10000);
        timerRunning = true;
    }

    public void stopTimer() {
        timer.cancel();
        timer.purge();
        timerRunning = false;
    }
}
