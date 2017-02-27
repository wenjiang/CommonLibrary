package async;

/**
 * 执行完毕的操作
 * Created by zwb on 2015/7/2.
 */
public interface DoneCallback<K, T> {
    void onSuccess(K attachment, T result);
}
