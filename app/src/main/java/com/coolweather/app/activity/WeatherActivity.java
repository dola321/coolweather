package com.coolweather.app.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coolweather.app.R;
import com.coolweather.app.receiver.AutoUpdateReceiver;
import com.coolweather.app.service.AutoUpdateService;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

import java.util.Date;

/**
 * Created by dola321 on 2016/4/8.
 */
public class WeatherActivity extends AppCompatActivity implements View.OnClickListener{

    private LinearLayout weatherLayout;

    private TextView cityNameText;      //城市名
    private TextView publishText;       //发布时间
    private TextView weatherDespText;  //天气描述信息
    private TextView temp1Text;
    private TextView temp2Text;
    private TextView currentDateText;

    private Button switchCity;
    private Button refreshCity;

    @Override
    protected void onCreate (Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.weather_layout);
        //初始化控件
        weatherLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
        cityNameText = (TextView) findViewById(R.id.city_name);
        publishText = (TextView) findViewById(R.id.push_text);
        weatherDespText = (TextView) findViewById(R.id.weather_desp);
        temp1Text = (TextView) findViewById(R.id.temp1);
        temp2Text = (TextView) findViewById(R.id.temp2);
        currentDateText = (TextView) findViewById(R.id.current_date);

        switchCity = (Button) findViewById(R.id.switch_city);
        refreshCity = (Button) findViewById(R.id.refresh_weather);
        switchCity.setOnClickListener(this);
        refreshCity.setOnClickListener(this);

        String countyCode = getIntent().getStringExtra("county_code");
        if (!TextUtils.isEmpty(countyCode)){
            //查代号
            publishText.setText("同步中...");
            weatherLayout.setVisibility(View.INVISIBLE);
            cityNameText.setVisibility(View.INVISIBLE);
            queryWeather(countyCode);
        } else {
            //查本地
            showWeather();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.switch_city:
                Intent intent = new Intent(this,ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity", true);
                startActivity(intent);
                finish();
                break;
            case R.id.refresh_weather:
                publishText.setText("同步中...");
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String weather_code = prefs.getString("weather_code", "");
                if (!TextUtils.isEmpty(weather_code)) {
                    queryWeather(weather_code);
                }
                break;
            default:
                break;
        }
    }

    /*
    * 查询天气
     */
    private void queryWeather(String countyCode) {
        String address = "https://api.heweather.com/x3/weather?cityid=" + countyCode + "&key=ee0a81c3b4c449c6a2601eed6b8b28e8";
        queryFromServer(address);
    }

    /*
    * 从服务器查询天气
     */
    private void queryFromServer(final String address) {
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                if (!TextUtils.isEmpty(response)){
                    Utility.handleWeatherReasponse(WeatherActivity.this, response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishText.setText("同步失败...");
                    }
                });
            }
        });
    }

    /*
    * 从SharedPreferences读取天气信息，并显示到界面上
     */
    private void showWeather(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        cityNameText.setText( prefs.getString("city_name", ""));
        temp1Text.setText( prefs.getString("temp1", ""));
        temp2Text.setText( prefs.getString("temp2", ""));
        weatherDespText.setText( prefs.getString("weather_desp", ""));
        publishText.setText( prefs.getString("publish_time", "") + "发布");
        currentDateText.setText( prefs.getString("current_data", ""));
        weatherLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }


}
