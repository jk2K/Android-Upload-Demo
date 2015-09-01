package com.jk2k.helloworld;

import android.app.Application;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by lee on 15/8/27.
 */
public class HelloApplication extends Application {
    private static HelloApplication mInstance;
    private RequestQueue mRequestQueue;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
        mRequestQueue = Volley.newRequestQueue(this);
    }

    public static synchronized HelloApplication getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }
}
