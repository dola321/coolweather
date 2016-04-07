package com.coolweather.app.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by dola321 on 2016/4/7.
 */
public class HttpUtil {

    public static void sendHttpRequest (final String address, final HttpCallbackListener listener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("test","sendHttp");
                HttpURLConnection connection = null;                                      //HttpURLConnection
                try {
                    URL url = new URL(address);                   //指定访问的服务器地址
                    connection = (HttpURLConnection) url.openConnection();                 //获取一个HttpURLConnection实例
                    connection.setRequestMethod("GET");                        //GET表示希望从服务器获取数据   POST表示希望提交数据给服务器
                    connection.setConnectTimeout(8000);                                                 //连接超时的毫秒数
                    connection.setReadTimeout(8000);                                                    //读取超时的毫秒数
                    InputStream in = connection.getInputStream();                                       //获取服务器返回的输入流
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));              //将输入流转换成String
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    if (listener != null) {
                        listener.onFinish(response.toString());
                    }
                } catch (Exception e) {
                    if (listener != null) {
                        listener.onError(e);
                    }
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }
}
