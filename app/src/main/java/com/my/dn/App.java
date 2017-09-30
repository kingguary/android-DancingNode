package com.my.dn;

import android.app.Application;

/**
 * Created by guary on 2017/9/28.
 */

public class App extends Application {
    private static App mInstance;

    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static App getInstance() {
        return mInstance;
    }
}
