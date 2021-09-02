package cc.easyandroid.providers.downloads;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

import cc.easyandroid.providers.core.EasyDownLoadManager;

class RealSystemFacade implements SystemFacade {
    private Context mContext;
    private NotificationManager mNotificationManager;
    // 2 GB
    private static final long DOWNLOAD_MAX_BYTES_OVER_MOBILE = 2 * 1024 * 1024 * 1024L;
    // 1 GB
    private static final long DOWNLOAD_RECOMMENDED_MAX_BYTES_OVER_MOBILE = 1024 * 1024 * 1024;

    private final ScheduledThreadPoolExecutor threadPoolExecutor;



    public RealSystemFacade(Context context) {
        mContext = context;
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        threadPoolExecutor = EasyDownLoadManager.getInstance(context).getThreadPoolExecutor();
    }

    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    public Integer getActiveNetworkType() {
        ConnectivityManager connectivity = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            Log.w(Constants.TAG, "couldn't get connectivity manager");
            return null;
        }

        NetworkInfo activeInfo = connectivity.getActiveNetworkInfo();
        if (activeInfo == null) {
            if (Constants.LOGVV) {
                Log.v(Constants.TAG, "network is not available");
            }
            return null;
        }
        return activeInfo.getType();
    }

    public boolean isNetworkRoaming() {
        ConnectivityManager connectivity = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            Log.w(Constants.TAG, "couldn't get connectivity manager");
            return false;
        }

        NetworkInfo info = connectivity.getActiveNetworkInfo();
        boolean isMobile = (info != null && info.getType() == ConnectivityManager.TYPE_MOBILE);
        final TelephonyManager mgr = (TelephonyManager) mContext
                .getSystemService(Context.TELEPHONY_SERVICE);
        boolean isRoaming = isMobile && mgr.isNetworkRoaming();
        if (Constants.LOGVV && isRoaming) {
            Log.v(Constants.TAG, "network is roaming");
        }
        return isRoaming;
    }

    public Long getMaxBytesOverMobile() {
        return DOWNLOAD_MAX_BYTES_OVER_MOBILE;
    }

    @Override
    public Long getRecommendedMaxBytesOverMobile() {
        return DOWNLOAD_RECOMMENDED_MAX_BYTES_OVER_MOBILE;
    }

    @Override
    public void sendBroadcast(Intent intent) {
        mContext.sendBroadcast(intent);
    }

    @Override
    public boolean userOwnsPackage(int uid, String packageName)
            throws NameNotFoundException {
        return mContext.getPackageManager().getApplicationInfo(packageName, 0).uid == uid;
    }

    @Override
    public void postNotification(long id, Notification.Builder builder) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("001", "my_channel", NotificationManager.IMPORTANCE_DEFAULT);
            //channel.enableLights(true); //是否在桌面icon右上角展示小红点
            channel.setLightColor(Color.GREEN); //小红点颜色
            channel.setVibrationPattern(new long[]{0});
            channel.setSound(null, null);
            channel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知
            mNotificationManager.createNotificationChannel(channel);
            builder.setOnlyAlertOnce(true);
            builder.setChannelId("001");
        }
        mNotificationManager.notify(200, builder.build());
    }

    @Override
    public void cancelNotification(long id) {
        mNotificationManager.cancel((int) id);
    }

    @Override
    public void cancelAllNotifications() {
        mNotificationManager.cancelAll();
    }

    @Override
    public void startThread(Thread thread, boolean joinToThreadPool) {
        if (joinToThreadPool) {
            threadPoolExecutor.execute(thread);
        } else {
            thread.start();
        }
    }
}
