package cc.easyandroid.downloadprovider.simple;

import android.view.View;

/**
 * Created by Administrator on 2016/3/14.
 */
public interface IVisiblePosition {
    int getFirstVisiblePosition();

    int getLastVisiblePosition();

    View getVisibleView(int position);
}
