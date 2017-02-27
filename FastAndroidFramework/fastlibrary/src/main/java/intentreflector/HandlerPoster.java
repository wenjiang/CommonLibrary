package intentreflector;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import log.Logger;

/**
 * Created by weber_zheng on 17/1/16.
 */

public class HandlerPoster extends Handler {
    HandlerPoster(Looper looper){
        super(looper);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

        Object object = msg.obj;
        if(object instanceof Intent){
            try {
                IntentReflector.getInstance().intentProxy((Intent)object);
            } catch (Exception e) {
                Logger.e(e.toString());
            }
        }
    }
}
