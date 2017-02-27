package async;

/**
 * 失败的回调
 * Created by zwb on 2015/7/6.
 */
public interface FailCallback<K> {
    void onError(K attachment, Exception e);
}
