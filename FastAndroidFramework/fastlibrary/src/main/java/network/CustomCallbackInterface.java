package network;

import retrofit.Callback;

/**
 * Created by zwb on 2015/7/22.
 */
public interface CustomCallbackInterface<T> extends Callback<T> {
    String getTag();

    String setTag(String tag);

    void cancel();

    boolean isCanceled();
}