package base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import async.AsyncTaskManager;
import intentreflector.IntentReflector;

/**
 * Created by weber_zheng on 17/1/18.
 */

public class ManagerFragment extends Fragment {

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        IntentReflector.getInstance().register(getActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        IntentReflector.getInstance().unregister(getActivity());
        ((BaseActivity)getActivity()).taskManager.cancelAll();
    }
}
