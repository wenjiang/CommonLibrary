package base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import async.AsyncTaskManager;
import intentreflector.IntentReflector;

/**
 * Created by weber_zheng on 17/1/12.
 */

public class BaseActivity extends AppCompatActivity {
    protected AsyncTaskManager taskManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentReflector.getInstance().bindManager(this);
        taskManager = new AsyncTaskManager(this);
    }
}
