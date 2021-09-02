package cc.easyandroid.downloadprovider.simple;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import cc.easyandroid.providers.core.EasyDownLoadManager;
import cc.easyandroid.providers.downloads.DownloadService;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_main);
        listView = (ListView) findViewById( R.id.listview);
        adapter = new MyAdapter(this);
        EasyDownLoadManager.getInstance(this).addObserver(adapter);
        listView.setAdapter(adapter);
        List<String> urls = new ArrayList<>();
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");
        urls.add("http://down.mumayi.com/41052/mbaidu");


        adapter.setIVisiblePosition(new IVisiblePosition() {
            @Override
            public int getFirstVisiblePosition() {
                return listView.getFirstVisiblePosition();
            }

            @Override
            public int getLastVisiblePosition() {
                //listView.get
                return listView.getLastVisiblePosition();
            }

            @Override
            public View getVisibleView(int position) {
                ;
                return listView.getChildAt((position - getFirstVisiblePosition()) % listView.getChildCount());
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, DownloadService.class));
        } else {
            startService(new Intent(this, DownloadService.class));
        }
        adapter.setUrls(urls);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EasyDownLoadManager.getInstance(this).deleteObserver(adapter);
        EasyDownLoadManager.destroy();
    }
}
