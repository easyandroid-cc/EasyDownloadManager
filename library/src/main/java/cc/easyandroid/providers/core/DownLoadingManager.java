package cc.easyandroid.providers.core;

import android.content.Context;

import cc.easyandroid.providers.DownloadManager;

/**
 * 这个类只是增加了完成DownloadManager.STATUS_SUCCESSFUL
 */
public class DownLoadingManager extends EasyDownLoadManager {
    private static DownLoadingManager mInstance;

    /**
     * default constructor
     *
     * @param context
     */
    public DownLoadingManager(Context context) {
        super(context);
    }

    public static synchronized DownLoadingManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DownLoadingManager(context);
        }
        return mInstance;
    }

    @Override
    protected DownloadManager.Query onCreatQuery() {
        return new DownloadManager.Query().orderBy(DownloadManager.COLUMN_ID, DownloadManager.Query.ORDER_DESCENDING).setFilterByStatus(DownloadManager.STATUS_RUNNING | DownloadManager.STATUS_PENDING | DownloadManager.STATUS_PAUSED | DownloadManager.STATUS_FAILED);
    }
}
