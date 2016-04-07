package com.coolweather.app.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.coolweather.app.model.City;
import com.coolweather.app.model.Province;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dola321 on 2016/4/7.
 */
public class CoolWeatherDB {

    /*
     * 数据库名
     */
    public static final String DB_NAME = "cool_weather";

    /*
     * 数据库版本
     */
    public static final int VERSION = 1;

    private static CoolWeatherDB coolWeatherDB;

    private SQLiteDatabase db;

    /*
     * 构造方法私有化
     */
    private CoolWeatherDB(Context context){
        CoolWeatherOpenHelper dbHelper = new CoolWeatherOpenHelper(context, DB_NAME, null, VERSION);
        db = dbHelper.getWritableDatabase();
    }

    /*
     *获取CoolWeatherDB实例
     */
    public synchronized static CoolWeatherDB getInstance(Context context) {
        if (coolWeatherDB == null){
            coolWeatherDB = new CoolWeatherDB(context);
        }
        return coolWeatherDB;
    }

    /*
     *将Province实例存储到数据库
     */
    public void saveProvince(Province province){
        if (province != null) {
            ContentValues values = new ContentValues();
            values.put("province_name", province.getProvinceName());
            db.insert("Province", null, values);
        }
    }

    /*
     *从数据库读取全国所有省份信息
     */
    public List<Province> loadProvince() {
        List<Province> list = new ArrayList<Province>();
        Cursor cursor = db.query("Province", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Province province = new Province();
                province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
                list.add(province);
            }while (cursor.moveToNext());
        }
        return list;
    }

    /*
    *将City实例存储到数据库
    */
    public void saveCity(City city){
        if (city != null) {
            ContentValues values = new ContentValues();
            values.put("city_name", city.getCityName());
            values.put("city_code", city.getCityCode());
            values.put("province_name", city.getProvinceName());
            db.insert("City", null, values);
        }
    }

    /*
     *从数据库读取全国所有城市信息
     */
    public List<City> loadCities( String provinceName) {
        List<City> list = new ArrayList<City>();
        Cursor cursor = db.rawQuery("SELECT * FROM City WHERE province_name = ? AND ( city_code LIKE '%0100%' OR city_code LIKE '%01' )",new String[]{provinceName});
        if (cursor.moveToFirst()) {
            do {
                City city = new City();
                city.setId(cursor.getInt(cursor.getColumnIndex("id")));
                city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
                city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
                city.setProvinceName(provinceName);
                list.add(city);
            }while (cursor.moveToNext());
        }
        return list;
    }

    /*
     *从数据库读取某城市下各区信息
     */
    public List<City> loadCounties( String cityCode) {
        List<City> list = new ArrayList<City>();

        int fuckID = Integer.parseInt(cityCode.substring(2));

        String cityID;
        if (fuckID > 101050000){
            cityID = cityCode.substring(0, 9)+"%";
        }else {
            cityID = cityCode.substring(0, 8)+"%";
        }

        Cursor cursor = db.rawQuery("SELECT * FROM City WHERE city_code LIKE ?", new String[]{cityID});
        if (cursor.moveToFirst()) {
            do {
                City city = new City();
                city.setId(cursor.getInt(cursor.getColumnIndex("id")));
                city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
                city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
                list.add(city);
            }while (cursor.moveToNext());
        }

        return list;
    }
}
