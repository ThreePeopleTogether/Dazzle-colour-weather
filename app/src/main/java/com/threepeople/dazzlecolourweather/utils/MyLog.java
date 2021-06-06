package com.threepeople.dazzlecolourweather.utils;

import android.util.Log;

import java.io.Serializable;

/**
 * 对Log类进行包装.
 * 更改DEBUG的值可决定是否输出低于信息级别的日志.
 */
public class MyLog implements Serializable {
    private static final long serialVersionUID = 202103101817L;
    private static final boolean DEBUG = false;
    private final String tag;

    public MyLog(String tag) {
        this.tag = tag;
    }

    public void v(String msg) {
        if (DEBUG) {
            Log.v(tag, msg);
        }
    }

    public void v(String errorInfo, Exception e) {
        if (DEBUG) {
            Log.v(tag, errorInfo + " (" + e.getClass().getSimpleName() + "): " + e.getMessage());
        }
    }

    public void d(String msg) {
        if (DEBUG) {
            Log.d(tag, msg);
        }
    }

    public void d(String errorInfo, Exception e) {
        if (DEBUG) {
            Log.d(tag, errorInfo + " (" + e.getClass().getSimpleName() + "): " + e.getMessage());
        }
    }

    public void i(String msg) {
        Log.i(tag, msg);
    }

    public void i(String errorInfo, Exception e) {
        Log.i(tag, errorInfo + " (" + e.getClass().getSimpleName() + "): " + e.getMessage());
    }

    public void w(String msg) {
        Log.w(tag, msg);
    }

    public void w(String errorInfo, Exception e) {
        Log.w(tag, errorInfo + " (" + e.getClass().getSimpleName() + "): " + e.getMessage());
    }

    public void e(String msg) {
        Log.e(tag, msg);
    }

    public void e(String errorInfo, Exception e) {
        Log.e(tag, errorInfo + " (" + e.getClass().getSimpleName() + "): " + e.getMessage());
    }
}
