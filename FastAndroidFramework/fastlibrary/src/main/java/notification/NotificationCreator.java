package notification;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by weber_zheng on 17/1/10.
 */

public class NotificationCreator {
    private RemoteViews remoteViews;
    private PendingIntent pendingIntent;
    private Class clazz;
    private int requestCode = -1;
    private int flag = PendingIntent.FLAG_UPDATE_CURRENT;
    private boolean autoCancel = false;
    private int smallIconId = -1;
    private static Map<Integer, Long> notificationMap = new HashMap<>();
    private long TIME_DIFF = 60 * 1000;
    private Context context;

    private NotificationCreator(Context context) {
        this.context = context;
    }

    public static NotificationCreator create(Context context) {
        return new NotificationCreator(context);
    }

    /**
     * 默认是PendingIntent.FLAG_UPDATE_CURRENT
     *
     * @param flag
     * @return
     */
    public NotificationCreator flag(int flag) {
        this.flag = flag;
        return this;
    }

    /**
     * 默认是false
     *
     * @param autoCancel
     * @return
     */
    public NotificationCreator autoCancel(boolean autoCancel) {
        this.autoCancel = autoCancel;
        return this;
    }

    public NotificationCreator target(Class clazz) {
        this.clazz = clazz;
        return this;
    }

    public NotificationCreator contentView(int layoutId) {
        this.remoteViews = new RemoteViews(context.getPackageName(), layoutId);
        return this;
    }

    public NotificationCreator requestCode(int requestCode) {
        this.requestCode = requestCode;
        return this;
    }

    public NotificationCreator smallIcon(int resId) {
        this.smallIconId = resId;
        return this;
    }

    public NotificationCreator intent(Intent intent) throws Exception {
        if (null == clazz) {
            throw new Exception("Please call target method before");
        }

        if (-1 == requestCode) {
            throw new Exception("Please call requestCode method before");
        }

        if (clazz.equals(BroadcastReceiver.class)) {
            pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flag);
        } else if (clazz.equals(Activity.class)) {
            pendingIntent = PendingIntent.getActivity(context, requestCode, intent, flag);
        } else if (clazz.equals(Service.class)) {
            pendingIntent = PendingIntent.getService(context, requestCode, intent, flag);
        }
        return this;
    }

    public NotificationCreator text(int viewId, String text) {
        remoteViews.setTextViewText(viewId, text);
        return this;
    }

    public NotificationCreator setTextColor(int viewId, int color) {
        remoteViews.setTextColor(viewId, color);
        return this;
    }

    public NotificationCreator imageResource(int viewId, int resourceId) {
        remoteViews.setImageViewResource(viewId, resourceId);
        return this;
    }

    public NotificationCreator viewVisible(int viewId, int visible) {
        remoteViews.setViewVisibility(viewId, visible);
        return this;
    }

    public NotificationCreator onClick(int viewId) {
        remoteViews.setOnClickPendingIntent(viewId, pendingIntent);
        return this;
    }

    /**
     * 如果要做圆形图片处理,只能在外面处理,然后再以Bitmap的形式传进来
     *
     * @param viewId
     * @param bitmap
     * @return
     */
    public NotificationCreator imageBitmap(int viewId, Bitmap bitmap) {
        remoteViews.setImageViewBitmap(viewId, bitmap);
        return this;
    }

    public NotificationCreator build(Context context, int id) throws Exception {
        if (smallIconId == -1) {
            throw new Exception("Please call smallIcon before");
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        builder.setAutoCancel(autoCancel);
        builder.setSmallIcon(smallIconId);
        builder.setContentIntent(pendingIntent);
        builder.setCustomContentView(remoteViews);
        Notification notification = builder.build();
        notificationManager.notify(id, notification);
        if (notificationMap.get(id) == null) {
            notificationMap.put(id, System.currentTimeMillis());
        }
        return this;
    }

    public void refreshNotification(Context context, int iconId, int iconResource, int tvId) throws Exception {
        long currentTime = System.currentTimeMillis();
        Set set = notificationMap.keySet();
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            int id = (int)iterator.next();
            long time = notificationMap.get(id);
            long diffTime = currentTime - time;
            if (diffTime >= TIME_DIFF) {
                imageResource(iconId, iconResource)
                        .text(tvId, diffTime / TIME_DIFF + "小时未读").build(context, id);
            }
        }
    }

    public void setTimeDiff(long timeDiff){
        this.TIME_DIFF = timeDiff;
    }

    public void removeNotification(int id) {
        notificationMap.remove(id);
    }
}
