package base;

import android.app.Application;
import android.content.Context;

import database.DatabaseStore;
import database.SharedPreferencesManager;
import log.AndroidLogTool;
import log.LogLevel;
import log.Logger;

/**
 * Created by weber_zheng on 17/1/10.
 */

public class BaseApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        Logger.init().logLevel(LogLevel.FULL).logTool(new AndroidLogTool());
        Logger.debug(true);
        SharedPreferencesManager.init(this);
        DatabaseStore.getInstance().init(this);
    }

    public static Context getContext(){
        return context;
    }
}
