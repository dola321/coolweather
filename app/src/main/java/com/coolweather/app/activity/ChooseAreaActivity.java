package com.coolweather.app.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.app.R;
import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.Province;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dola321 on 2016/4/7.
 */
public class ChooseAreaActivity extends AppCompatActivity {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY =2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private CoolWeatherDB coolWeatherDB;
    private List<String> dataList = new ArrayList<String>();

    private boolean isFromWeatherActivity;

    /*
    * 省列表
     */
    private List<Province> provincesList;

    /*
    * 市列表
     */
    private List<City> cityList;

    /*
    * 选中的省份
     */
    private Province selecteddProvince;

    /*
    * 选中的城市
     */
    private City selectedCity;

    /*
    * 当前选中的级别
     */
    private int currentLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("city_selected", false) && !isFromWeatherActivity) {
            Intent intent = new Intent(this, WeatherActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        setContentView(R.layout.choose_area);
        getSupportActionBar().hide();
        listView = (ListView) findViewById(R.id.list_view);
        titleText = (TextView) findViewById(R.id.title_text);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        coolWeatherDB = CoolWeatherDB.getInstance(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selecteddProvince = provincesList.get(position);
                    queryCities();
                }else if (currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    queryCounties();
                }else if (currentLevel == LEVEL_COUNTY) {
                    String countyCode = cityList.get(position).getCityCode();
                    Intent intent  = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
                    intent.putExtra("county_code", countyCode);
                    startActivity(intent);
                    finish();
                }
            }
        });
        queryProvinces();
    }

    /*
    *  查询全国所有省
     */
    private void queryProvinces() {
        provincesList = coolWeatherDB.loadProvince();
        if (provincesList.size() > 0) {
            dataList.clear();
            for (Province province : provincesList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText("中国");
            currentLevel = LEVEL_PROVINCE;
        } else {
            queryFromServer("province");
        }
    }

    /*
    * 查询选中省内的市
     */
    private void queryCities() {
        cityList = coolWeatherDB.loadCities(selecteddProvince.getProvinceName());
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selecteddProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        } else {
            queryFromServer("city");
        }
    }

    /*
    * 查询选中市内的曲
     */
    private void queryCounties() {
        cityList = coolWeatherDB.loadCounties(selectedCity.getCityCode());

        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedCity.getCityName());
            currentLevel = LEVEL_COUNTY;
        } else {
            Toast.makeText(this,"failed",Toast.LENGTH_SHORT).show();
        }
    }

    private void queryFromServer(final String type) {
        String address;
        if ( "province".equals(type)) {
            address = "http://www.baidu.com";
        }
        else {
            address = "https://api.heweather.com/x3/citylist?search=allchina&key=ee0a81c3b4c449c6a2601eed6b8b28e8";
        }
        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                boolean result =false;
                if ( "province".equals(type)) {
                    response = "\"直辖市\",\"特别行政区\",\"黑龙江\",\"吉林\",\"辽宁\",\"内蒙古\",\"河北\",\"河南\",\"山东\",\"山西\",\"江苏\",\"安徽\",\"陕西\"," +
                            "\"宁夏\",\"甘肃\",\"青海\",\"湖北\",\"湖南\",\"浙江\",\"江西\",\"福建\",\"贵州\",\"四川\",\"广东\",\"广西\",\"云南\",\"海南\",\"新疆\",\"西藏\",\"台湾\"";
                    result = Utility.handleProvinceResponse(coolWeatherDB, response);
                }else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(coolWeatherDB, response);
                }
                if (result) {
                    //通过runOnUiThread方法返回主线程处理逻辑
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ( "province".equals(type)) {
                                queryProvinces();
                            }else if ("city".equals(type)) {
                                queryCities();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                //通过runOnUiThread返回主线程处理逻辑
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /*
    * 显示进度对话框
     */
    private void showProgressDialog(){
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载...");
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
    }

    /*
    * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    /*
    * 捕捉back按键，根据当前级别判断返回
     */

    @Override
    public void onBackPressed() {
        if (currentLevel == LEVEL_COUNTY){
            queryCities();
        }else if (currentLevel == LEVEL_CITY) {
            queryProvinces();
        }else {
            if (isFromWeatherActivity){
                Intent intent = new Intent(this, WeatherActivity.class);
                startActivity(intent);
            }
            super.onBackPressed();
        }
    }
}
