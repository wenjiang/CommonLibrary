package async;


/**
 * 异步任务的后台执行回调
 * Created by zwb on 2015/6/29.
 */
public interface BackgroundCallback<T> {
    T doInBackground();
}
