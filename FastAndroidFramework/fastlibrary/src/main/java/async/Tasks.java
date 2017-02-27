package async;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.ProgressBar;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * 异步任务的管理类
 * Created by zwb on 2015/7/2.
 */
final class Tasks {
    private static int index = -1;
    private static Map<String, Task> taskMap = new HashMap<>();

    private Tasks() {
        throw new UnsupportedOperationException();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static <K, T> String executeInBackground(K attachment, BackgroundCallback<T> backgroundCallback, DoneCallback<K, T> doneCallback, FailCallback<K> failCallback, ProgressBar progressBar) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return executeInBackground(attachment, backgroundCallback, doneCallback, failCallback, progressBar, AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            String tag = attachment.getClass().getSimpleName() + "_" + index++;
            Task task = new Task<>(attachment, backgroundCallback, doneCallback, failCallback, progressBar);
            taskMap.put(tag, task);
            task.execute();
            return tag;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static <K, T> String executeInBackground(K attachment, BackgroundCallback<T> backgroundCallback, DoneCallback<K, T> doneCallback, FailCallback<K> failCallback, ProgressBar progressBar, Executor executor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            String tag = attachment.getClass().getSimpleName() + "_" + index++;
            Task task = new Task<>(attachment, backgroundCallback, doneCallback, failCallback, progressBar);
            taskMap.put(tag, task);
            task.executeOnExecutor(executor);
            return tag;
        } else {
            throw new RuntimeException("you cannot use a custom executor on pre honeycomb devices");
        }
    }

    /**
     * 停止对应tag的Task
     *
     * @param tag
     */
    public static void cancel(String tag) {
        Task task = taskMap.get(tag);
        task.cancel(true);
        taskMap.remove(tag);
    }

    /**
     * 取消所有异步任务
     *
     * @param attachment
     */
    public static void cancelAll(Object attachment) {
        Set<String> keyTag = taskMap.keySet();
        Iterator iterator = keyTag.iterator();
        while (iterator.hasNext()) {
            String tag = (String) iterator.next();
            if (tag.contains(attachment.getClass().getSimpleName())) {
                Task task = taskMap.get(tag);
                task.cancel(true);
                iterator.remove();
            }
        }
    }
}
