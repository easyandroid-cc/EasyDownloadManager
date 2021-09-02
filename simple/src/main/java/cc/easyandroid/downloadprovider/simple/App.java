package cc.easyandroid.downloadprovider.simple;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;

import cc.easyandroid.providers.downloads.Constants;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("cgp App onCreate");
        ConnectivityManager manager = (ConnectivityManager)  getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            System.out.println("cgp 注册网络监听=" + "" );
            manager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    super.onAvailable(network);
                    Intent intent = new Intent(Constants.ACTION_NETWORK_AVAILABLE);
                    sendBroadcast(intent);
                    System.out.println("cgp 发送 网络改变=" + "" + network.toString());
                }
            });
        }

    }
}
