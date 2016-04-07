package com.coolweather.app.util;

/**
 * Created by dola321 on 2016/4/7.
 */
public interface HttpCallbackListener {
    void onFinish(String response);

    void onError (Exception e);
}
