package com.threepeople.dazzlecolourweather.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.reflect.TypeToken;
import com.threepeople.dazzlecolourweather.R;
import com.threepeople.dazzlecolourweather.model.CityWeather;
import com.threepeople.dazzlecolourweather.model.NetRequest;
import com.threepeople.dazzlecolourweather.theme.WhiteUITheme;
import com.threepeople.dazzlecolourweather.utils.Tools;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class StartPictureActivity extends AppCompatActivity {
    private final AppCompatActivity activity;
    private final int permissionCode;
    private final int openGPSCode;
    private ScheduledFuture<?> timeoutTimer;
    private double[] locationData;
    private boolean successLocation;

    {
        activity = this;
        permissionCode = Tools.getRequestCode();
        openGPSCode = Tools.getRequestCode();
        timeoutTimer = null;
        locationData = null;
        successLocation = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_picture);
        Tools.setBlackWordOnStatus(this);

        Tools.init(activity);
        Tools.initPermission(activity, permissionCode);
        SharedPreferences read = getSharedPreferences(Tools.weatherCfg, MODE_PRIVATE);
        Tools.nowLocationCityId = read.getString(Tools.locationIdKey, null);
    }

    private boolean readWeatherDataCache() {
        Tools.cities = Tools.readListFromFile(Tools.weatherDataCacheFile, new TypeToken<List<CityWeather>>() {}.getType());
        if (Tools.cities == null) {
            Tools.cities = new LinkedList<>();
            return false;
        }
        return true;
    }

    private void requestWeatherData(double longitude, double latitude) {// 北京市 - 北京 - 北京  经度：116.40528  纬度：39.90498
        NetRequest.requestCityWeather(activity, longitude, latitude, 0, new NetRequest.CityWeatherCallback() {
            @Override
            public void onSuccess(CityWeather cityWeather) {
                if (!activity.isFinishing()) {
                    cancelTimeoutTimer();
                    if (successLocation) {
                        Tools.nowLocationCityId = cityWeather.getId();
                    }
                    Tools.saveSharedPreferences(activity, Tools.weatherCfg, Tools.locationIdKey, Tools.nowLocationCityId);
                    Tools.cities.add(cityWeather);
                    toHomePager();
                }
            }

            @Override
            public void onFail() {
                if (!activity.isFinishing()) {
                    cancelTimeoutTimer();
                    toHomePager();
                }
            }

            @Override
            public void onTimeout() {
                // 不会超时
            }
        });
    }

    private void startTimeoutTimer() {
        timeoutTimer = Tools.scheduledThreadPool.schedule(() -> runOnUiThread(() -> {
            Toast.makeText(activity, "请求超时", Toast.LENGTH_SHORT).show();
            toHomePager();
        }), 5, TimeUnit.SECONDS);
    }

    private void cancelTimeoutTimer() {
        if (timeoutTimer != null) {
            timeoutTimer.cancel(true);
        }
    }

    private void toHomePager() {
        if (Tools.cities.size() > 0) {
            Tools.saveObjectAtFile(Tools.weatherDataCacheFile, Tools.cities);
        }
        Intent toHome = new Intent(activity, HomePagerActivity.class);
        if (locationData != null) {
            toHome.putExtra(Tools.locationDataKey, locationData);
        }
        startActivity(toHome);
        activity.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == openGPSCode) {
            startTimeoutTimer();
            Tools.getLocation(activity, new Tools.LocationCallback() {
                @Override
                public void onSuccess(double longitude, double latitude) {
                    successLocation = true;
                    locationData = new double[] {longitude, latitude};
                    requestWeatherData(longitude, latitude);
                }

                @Override
                public void onFail(Context context) {
                    Toast.makeText(context, "定位失败", Toast.LENGTH_SHORT).show();
                    requestWeatherData(116.40528, 39.90498);
                }

                @Override
                public void lackProvider() {
                    onFail(activity);
                }

                @Override
                public void lackPermission(Context context) {
                    onFail(activity);
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == permissionCode) {
            if (readWeatherDataCache()) {
                toHomePager();
            } else {
                startTimeoutTimer();
                Tools.getLocation(activity, new Tools.LocationCallback() {
                    @Override
                    public void onSuccess(double longitude, double latitude) {
                        locationData = new double[] {longitude, latitude};
                        if (!activity.isFinishing()) {
                            successLocation = true;
                            requestWeatherData(longitude, latitude);
                        }
                    }

                    @Override
                    public void onFail(Context context) {
                        if (!activity.isFinishing()) {
                            Toast.makeText(context, "定位失败", Toast.LENGTH_SHORT).show();
                            requestWeatherData(116.40528, 39.90498);
                        }
                    }

                    @Override
                    public void lackProvider() {
                        cancelTimeoutTimer();
                        Tools.showDialogWithTwoButton(activity, "定位服务未启用，是否前往设置？", new WhiteUITheme(activity), new Tools.ControlDialog() {
                            @Override
                            public void onDialog(AlertDialog dialog, View dialogView) {
                                dialog.setCancelable(false);
                                dialog.setCanceledOnTouchOutside(false);
                            }

                            @Override
                            public void onOk(AlertDialog dialog) {
                                dialog.dismiss();
                                startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), openGPSCode);
                            }

                            @Override
                            public void onCancel(AlertDialog dialog) {
                                dialog.dismiss();
                                startTimeoutTimer();
                                requestWeatherData(116.40528, 39.90498);
                            }
                        });
                    }

                    @Override
                    public void lackPermission(Context context) {
                        Toast.makeText(context, "没有定位权限", Toast.LENGTH_SHORT).show();
                        requestWeatherData(116.40528, 39.90498);
                    }
                });
            }
        }
    }
}