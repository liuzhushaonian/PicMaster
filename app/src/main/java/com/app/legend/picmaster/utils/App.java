package com.app.legend.picmaster.utils;

import android.app.Application;
import android.content.Context;

public class App extends Application {

    static Context context;

    public static Context getContext(){
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context=getApplicationContext();
    }
}
