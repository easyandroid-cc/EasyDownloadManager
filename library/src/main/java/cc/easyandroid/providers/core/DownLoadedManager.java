package cc.easyandroid.providers.core;

import android.content.Context;

import cc.easyandroid.providers.DownloadManager;

/**
 * 这个类只是增加了完成DownloadManager.STATUS_SUCCESSFUL
 */
public class DownLoadedManager extends EasyDownLoadManager {


    private static DownLoadedManager mInstance;

    /**
     * default constructor
     *
     * @param context
     */
    public DownLoadedManager(Context context) {
        super(context);
    }

    public static synchronized DownLoadedManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DownLoadedManager(context);
        }
        return mInstance;
    }

    @Override
    protected DownloadManager.Query onCreatQuery() {
        return new DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL).orderBy(DownloadManager.COLUMN_ID, DownloadManager.Query.ORDER_DESCENDING);
    }
}
