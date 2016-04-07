package com.coolweather.app.util;

import android.text.TextUtils;
import android.util.Log;

import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.Province;

import org.json.JSONArray;
import org.json.JSONObject;


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
}
