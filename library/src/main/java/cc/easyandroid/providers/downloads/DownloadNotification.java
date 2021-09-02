/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cc.easyandroid.providers.downloads;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Collection;
import java.util.HashMap;

import cc.easyandroid.downloadprovider.R;


/**
 * This class handles the updating of the Notification Manager for the
 * cases where there is an ongoing download. Once the download is complete
 * (be it successful or unsuccessful) it is no longer the responsibility
 * of this component to show the download in the notification manager.
 */
class DownloadNotification {

    Context mContext;
    HashMap<String, NotificationItem> mNotifications;
    private SystemFacade mSystemFacade;

    /**
     * This inner class is used to collate downloads that are owned by
     * the same application. This is so that only one notification line
     * item is used for all downloads of a given application.
     */
    static class NotificationItem {
        int mId;  // This first db _id for the download for the app
        long mTotalCurrent = 0;
        long mTotalTotal = 0;
        int mTitleCount = 0;
        String mPackageName;  // App package name
        String mDescription;
        String[] mTitles = new String[2]; // download titles.
        String mPausedText = null;

        /*
         * Add a second download to this notification item.
         */
        void addItem(String title, long currentBytes, long totalBytes) {
            mTotalCurrent += currentBytes;
            if (totalBytes <= 0 || mTotalTotal == -1) {
                mTotalTotal = -1;
            } else {
                mTotalTotal += totalBytes;
            }
            if (mTitleCount < 2) {
                mTitles[mTitleCount] = title;
            }
            mTitleCount++;
        }
    }


    /**
     * Constructor
     *
     * @param ctx The context to use to obtain access to the
     *            Notification Service
     */
    DownloadNotification(Context ctx, SystemFacade systemFacade) {
        mContext = ctx;
        mSystemFacade = systemFacade;
        mNotifications = new HashMap<String, NotificationItem>();
    }

    /*
     * Update the notification ui.
     */
    public void updateNotification(Collection<DownloadInfo> downloads) {
        updateActiveNotification(downloads);
        updateCompletedNotification(downloads);
    }

    private void updateActiveNotification(Collection<DownloadInfo> downloads) {
        // Collate the notifications
        mNotifications.clear();
        for (DownloadInfo download : downloads) {
            if (!isActiveAndVisible(download)) {
                continue;
            }
            String packageName = download.mPackage;
            long max = download.mTotalBytes;
            long progress = download.mCurrentBytes;
            long id = download.mId;
            String title = download.mTitle;
            if (title == null || title.length() == 0) {
                title = mContext.getResources().getString(
                        R.string.download_unknown_title);
            }
            NotificationItem item;
            if (mNotifications.containsKey(packageName)) {
                item = mNotifications.get(packageName);
                item.addItem(title, progress, max);
            } else {
                item = new NotificationItem();
                item.mId = (int) id;
                item.mPackageName = packageName;
                item.mDescription = download.mDescription;
                item.addItem(title, progress, max);
                mNotifications.put(packageName, item);
            }
            if (download.mStatus == Downloads.STATUS_QUEUED_FOR_WIFI
                    && item.mPausedText == null) {
                item.mPausedText = mContext.getResources().getString(
                        R.string.notification_need_wifi_for_size);
            }
        }

        // Add the notifications
        for (NotificationItem item : mNotifications.values()) {
            // Build the notification object
            Notification.Builder builder = new Notification.Builder(mContext);

            boolean hasPausedText = (item.mPausedText != null);
            int iconResource = android.R.drawable.stat_sys_download;
            if (hasPausedText) {
                iconResource = android.R.drawable.stat_sys_warning;
            }
            builder.setSmallIcon(iconResource);
            builder.setOngoing(true);
            // Build the RemoteView object
            StringBuilder title = new StringBuilder(item.mTitles[0]);
            if (item.mTitleCount > 1) {
                title.append(mContext.getString(R.string.notification_filename_separator));
                title.append(item.mTitles[1]);
                builder.setNumber(item.mTitleCount);
                if (item.mTitleCount > 2) {
                    title.append(mContext.getString(R.string.notification_filename_extras,
                            new Object[]{Integer.valueOf(item.mTitleCount - 2)}));
                }
            } else {
                builder.setContentText(item.mDescription);
            }
            int progress = (int) (item.mTotalCurrent * 100 / item.mTotalTotal);
            builder.setContentTitle(title); //设置通知标题
            builder.setDefaults(Notification.DEFAULT_LIGHTS); //设置通知的提醒方式： 呼吸灯
            builder.setPriority(Notification.PRIORITY_MAX); //设置通知的优先级：最大
            builder.setAutoCancel(false);

            if (item.mTotalTotal > 0) {
                builder.setProgress(100, progress, false);
            } else {
                builder.setProgress(100, 0, true);
            }

            Intent intent = new Intent(Constants.ACTION_LIST);
            intent.setClassName(mContext.getPackageName(),
                    DownloadReceiver.class.getName());
            intent.setData(
                    ContentUris.withAppendedId(DownloadPros.ALL_DOWNLOADS_CONTENT_URI, item.mId));
            intent.putExtra("multiple", item.mTitleCount > 1);

            builder.setContentIntent(PendingIntent.getBroadcast(mContext, 0, intent, 0));


            mSystemFacade.postNotification(item.mId, builder);

        }
    }

    private void updateCompletedNotification(Collection<DownloadInfo> downloads) {
        for (DownloadInfo download : downloads) {
            if (!isCompleteAndVisible(download)) {
                continue;
            }
            // Add the notifications
            Notification.Builder builder = new Notification.Builder(mContext);
            builder.setSmallIcon(android.R.drawable.stat_sys_download_done);

            long id = download.mId;
            String title = download.mTitle;
            if (title == null || title.length() == 0) {
                title = mContext.getResources().getString(
                        R.string.download_unknown_title);
            }
            Uri contentUri =
                    ContentUris.withAppendedId(DownloadPros.ALL_DOWNLOADS_CONTENT_URI, id);
            String caption;
            Intent intent;
            if (Downloads.isStatusError(download.mStatus)) {
                caption = mContext.getResources()
                        .getString(R.string.notification_download_failed);
                intent = new Intent(Constants.ACTION_LIST);
            } else {
                caption = mContext.getResources()
                        .getString(R.string.notification_download_complete);
                if (download.mDestination == Downloads.DESTINATION_EXTERNAL) {
                    intent = new Intent(Constants.ACTION_OPEN);
                } else {
                    intent = new Intent(Constants.ACTION_LIST);
                }
            }
            intent.setClassName(mContext.getPackageName(),
                    DownloadReceiver.class.getName());
            intent.setData(contentUri);

            builder.setWhen(download.mLastMod);
            builder.setContentTitle(title);
            builder.setContentText(caption);
            builder.setContentIntent(PendingIntent.getBroadcast(mContext, 0, intent, 0));

            intent = new Intent(Constants.ACTION_HIDE);
            intent.setClassName(mContext.getPackageName(),
                    DownloadReceiver.class.getName());
            intent.setData(contentUri);

            builder.setDeleteIntent(PendingIntent.getBroadcast(mContext, 0, intent, 0));

            mSystemFacade.postNotification(download.mId, builder);
        }
    }

    private boolean isActiveAndVisible(DownloadInfo download) {
        return 100 <= download.mStatus && download.mStatus < 200
                && download.mVisibility != Downloads.VISIBILITY_HIDDEN;
    }

    private boolean isCompleteAndVisible(DownloadInfo download) {
        return download.mStatus >= 200
                && download.mVisibility == Downloads.VISIBILITY_VISIBLE_NOTIFY_COMPLETED;
    }

}
