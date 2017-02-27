package async;

import android.os.AsyncTask;

/**
 * 异步任务的抽象基类
 * Created by zwb on 2015/7/2.
 */
abstract class AbstractTask<K, T> extends AsyncTask<Object, Integer, T> {
    private BackgroundCallback<T> backgroundCallback;
    private Exception exception;
    protected K attachment;

    protected abstract void onSuccess(K attachment, T result);

    protected abstract void onError(K attachment, Exception e);

    AbstractTask(BackgroundCallback<T> backgroundCallback) {
        this.backgroundCallback = backgroundCallback;
    }

    @Override
    protected final T doInBackground(Object... params) {
            return backgroundCallback.doInBackground();
    }

    @Override
    protected final void onPostExecute(T result) {
        if (!isCancelled()) {
            if (exception == null) {
                onSuccess(attachment, result);
            } else {
                onError(attachment, exception);
            }
        }
    }
}
