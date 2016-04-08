package com.coolweather.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.Province;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.prefs.PreferencesFactory;


/**
 * Created by dola321 on 2016/4/7.
 */
public class Utility {

    /*
     *  解析和处理服务器返回的省级数据
     */
    public synchronized static boolean handleProvinceResponse(CoolWeatherDB coolWeatherDB,String response) {
        if (!TextUtils.isEmpty(response)){
            String[] allProvince = response.split(",");
            if (allProvince != null && allProvince.length > 0 ){
                for(String p : allProvince) {
                    String array = p.substring(1, p.length() - 1);
                    Province province = new Province();
                    province.setProvinceName(array);
                    coolWeatherDB.saveProvince(province);
                }
                return true;
            }
        }
        return false;
    }

    /*
     *  解析和处理服务器返回的市级数据
     */
    public synchronized static boolean handleCityResponse(CoolWeatherDB coolWeatherDB, String response) {
        if (!TextUtils.isEmpty(response)){
            try{
                JSONObject jsonObject =  new JSONObject(response);
                JSONArray cityList = jsonObject.getJSONArray("city_info");
                for(int i=0; i<cityList.length(); i++){
                    JSONObject cityObject = cityList.getJSONObject(i);
                    City city = new City();
                    city.setCityCode(cityObject.getString("id"));
                    city.setCityName(cityObject.getString("city"));
                    city.setProvinceName(cityObject.getString("prov"));
                    //解析出来的数据存储到City表
                    coolWeatherDB.saveCity(city);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    /*
    * 解析服务器返回的json数据，并将解析出的数据存储到本地
     */
    public static void handleWeatherReasponse(Context context, String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather data service 3.0");
            jsonObject = jsonArray.getJSONObject(0);
            JSONObject basic = jsonObject.getJSONObject("basic");
            String cityName = basic.getString("city");
            String weatherCode = basic.getString("id");
            JSONObject tmp = jsonObject.getJSONArray("daily_forecast").optJSONObject(0).getJSONObject("tmp");
            String temp1 = tmp.getString("min");
            String temp2 = tmp.getString("max");
            JSONObject cond = jsonObject.getJSONObject("now").getJSONObject("cond");
            String weatherDesp = cond.getString("txt");
            JSONObject update = basic.getJSONObject("update");
            String publishTime = update.getString("loc");
            saveWeatherInfo(context, cityName, weatherCode, temp1, temp2, weatherDesp, publishTime);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /*
    * 将天气信息返回到SharedPreferences文件夹中
     */
    private static void saveWeatherInfo(Context context, String cityName, String weatherCode, String temp1, String temp2, String weatherDesp, String publishTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("city_selected", true);
        editor.putString("city_name", cityName);
        editor.putString("weather_code", weatherCode);
        editor.putString("temp1", temp1 + "℃");
        editor.putString("temp2", temp2 + "℃");
        editor.putString("weather_desp", weatherDesp);
        editor.putString("publish_time", publishTime.substring(11));
        editor.putString("current_data", sdf.format(new Date()));
        editor.commit();
    }

}
