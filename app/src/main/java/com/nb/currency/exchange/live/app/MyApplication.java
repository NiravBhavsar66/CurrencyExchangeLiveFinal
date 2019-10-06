package com.nb.currency.exchange.live.app;

import androidx.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.nb.currency.exchange.live.utility.ConnectivityReceiver;

import io.fabric.sdk.android.Fabric;

/**
 * Created by mac on 6/20/17.
 */

public class MyApplication extends MultiDexApplication{

    private static MyApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        mInstance = this;
    }

    public static synchronized MyApplication getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }
}
