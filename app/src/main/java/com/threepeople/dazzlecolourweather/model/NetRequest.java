package com.threepeople.dazzlecolourweather.model;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.qweather.sdk.bean.IndicesBean;
import com.qweather.sdk.bean.air.AirNowBean;
import com.qweather.sdk.bean.base.Code;
import com.qweather.sdk.bean.base.IndicesType;
import com.qweather.sdk.bean.base.Lang;
import com.qweather.sdk.bean.base.Range;
import com.qweather.sdk.bean.geo.GeoBean;
import com.qweather.sdk.bean.weather.WeatherDailyBean;
import com.qweather.sdk.bean.weather.WeatherHourlyBean;
import com.qweather.sdk.bean.weather.WeatherNowBean;
import com.qweather.sdk.view.QWeather;
import com.threepeople.dazzlecolourweather.utils.Tools;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 网络数据请求包装类.
 */
public class NetRequest {
    /**
     * 通过定位信息获取城市天气数据.
     *
     * @param activity 上下文
     * @param longitude 经度
     * @param latitude 纬度
     * @param timeout 超时时间，若为0则永不超时（单位：ms）
     * @param callback 获取结果回调，在UI线程运行
     */
    public static void requestCityWeather(AppCompatActivity activity, double longitude, double latitude, @IntRange(from = 0) int timeout, @NonNull CityWeatherCallback callback) {
        RequestCityWeather request = new RequestCityWeather(activity, timeout, callback);
        request.requestTask(longitude + "," + latitude);
    }

    /**
     * 请求获取城市天气数据.
     *
     * @param activity 上下文
     * @param locationBean 城市数据信息
     * @param timeout 超时时间，若为0则永不超时（单位：ms）
     * @param callback 获取结果回调，在UI线程运行
     */
    public static void requestCityWeather(AppCompatActivity activity, @NonNull GeoBean.LocationBean locationBean, @IntRange(from = 0) int timeout, @NonNull CityWeatherCallback callback) {
        RequestCityWeather request = new RequestCityWeather(activity, timeout, callback);
        request.requestTask(locationBean);
    }

    /**
     * 使用城市ID获取天气数据.
     *
     * @param activity 上下文
     * @param cityId 城市ID
     * @param timeout 超时时间，若为0则永不超时（单位：ms）
     * @param callback 获取结果回调，在UI线程运行
     */
    public static void requestCityWeather(AppCompatActivity activity, @NonNull String cityId, @IntRange(from = 0) int timeout, @NonNull CityWeatherCallback callback) {
        RequestCityWeather request = new RequestCityWeather(activity, timeout, callback);
        request.requestTask(cityId);
    }

    /**
     * 刷新已有CityWeather数据.
     *
     * @param activity 上下文
     * @param cityWeather 已有的CityWeather数据
     * @param timeout 超时时间，若为0则永不超时（单位：ms）
     * @param callback 获取结果回调，在UI线程运行
     */
    public static void requestCityWeather(AppCompatActivity activity, @NonNull CityWeather cityWeather, @IntRange(from = 0) int timeout, @NonNull CityWeatherCallback callback) {
        RequestCityWeather request = new RequestCityWeather(activity, timeout, callback);
        request.requestTask(cityWeather);
    }

    /**
     * 通过定位信息获取城市信息.
     *
     * @param activity 上下文
     * @param longitude 经度
     * @param latitude 纬度
     * @param timeout 超时时间，若为0则永不超时（单位：ms）
     * @param callback 获取结果回调，在UI线程运行
     */
    public static void requestSearchCity(AppCompatActivity activity, double longitude, double latitude, @IntRange(from = 0) int timeout, @NonNull SearchCityCallback callback) {
        requestSearchCity(activity, longitude + "," + latitude, 1, timeout, callback);
    }

    /**
     * 通过搜索关键词搜索城市.
     *
     * @param activity 上下文
     * @param location 搜索关键词（至少两位英文字母或一个中文字符）
     * @param amount 搜索结果的数量，最大为20
     * @param timeout 超时时间，若为0则永不超时（单位：ms）
     * @param callback 搜索结果回调，在UI线程运行
     */
    public static void requestSearchCity(AppCompatActivity activity, String location, @IntRange(from = 1, to = 20) int amount, @IntRange(from = 0) int timeout, @NonNull SearchCityCallback callback) {
        final SearchCityCallback[] callbacks = new SearchCityCallback[] {callback};
        final ScheduledFuture<?>[] taskTimers = new ScheduledFuture<?>[] {null};
        if (timeout > 0) {
            taskTimers[0] = Tools.scheduledThreadPool.schedule(() -> activity.runOnUiThread(() -> {
                callbacks[0].onTimeout();
                callbacks[0] = new SearchCityCallback() {
                    @Override
                    public void onSuccess(List<GeoBean.LocationBean> cities) {}

                    @Override
                    public void notFindCity() {}

                    @Override
                    public void onFail(Code code) {}

                    @Override
                    public void onTimeout() {}

                    @Override
                    public void onError() {}
                };
            }), timeout, TimeUnit.MILLISECONDS);
        }
        QWeather.getGeoCityLookup(activity, location, Range.CN, amount, Lang.ZH_HANS, new QWeather.OnResultGeoListener() {
            @Override
            public void onError(Throwable throwable) {
                cancelTaskTimer();
                String message = throwable.getMessage();
                if (message != null && message.contains("GeoBean data is empty")) {
                    callbacks[0].notFindCity();
                } else {
                    callbacks[0].onError();
                }
            }

            @Override
            public void onSuccess(GeoBean geoBean) {
                cancelTaskTimer();
                Code code = geoBean.getCode();
                switch (code) {
                    case OK:
                        callbacks[0].onSuccess(geoBean.getLocationBean());
                        break;
                    case NO_DATA:
                    case PERMISSION_DENIED:
                    case INVALID_PARAM:
                        callbacks[0].notFindCity();
                        break;
                    default:
                        callbacks[0].onFail(code);
                        break;
                }
            }

            private void cancelTaskTimer() {
                if (taskTimers[0] != null) {
                    taskTimers[0].cancel(true);
                }
            }
        });
    }

    //====================内部类======================
    /**
     * 城市天气相关数据的请求包装类.
     */
    private static class RequestCityWeather {
        private final AppCompatActivity activity;
        private final int timeout;
        private CityWeatherCallback callback;
        private CityWeather cityWeather;
        private int taskCount;// 已完成的任务计数
        private final List<IndicesType> indicesTypes;// 需要获取的生活指数种类
        private static final int taskAmount = 6;// 任务总数
        private ScheduledFuture<?> taskTimer = null;// 超时计时器任务
        private final CityWeatherCallback emptyCallback = new CityWeatherCallback() {
            @Override
            public void onSuccess(CityWeather cityWeather) {}

            @Override
            public void onFail() {}

            @Override
            public void onTimeout() {}
        };

        private RequestCityWeather(AppCompatActivity activity, int timeout, @NonNull CityWeatherCallback callback) {
            this.activity = activity;
            this.timeout = timeout;
            this.callback = callback;
            this.cityWeather = new CityWeather();
            this.taskCount = 0;
            this.indicesTypes = new LinkedList<>();
            indicesTypes.add(IndicesType.DRSG);// 穿衣指数
            indicesTypes.add(IndicesType.SPT);// 运动指数
            indicesTypes.add(IndicesType.FLU);// 感冒指数
            indicesTypes.add(IndicesType.CW);// 洗车指数
        }

        private void incrementTaskCount() {// 调用该方法时，线程均为主线程，故不考虑同步
            if (++taskCount == taskAmount) {
                cancelTimeoutTimer();
                cityWeather.setUpdateTime(Tools.getNowTime());
                callback.onSuccess(cityWeather);
            }
        }

        private void requestTask(String location) {
            startTimeoutTimer();
            requestSearchCity(activity, location, 1, 0, new SearchCityCallback() {
                @Override
                public void onSuccess(List<GeoBean.LocationBean> cities) {
                    cityWeather.setCityInfo(cities.get(0));
                    incrementTaskCount();
                }

                @Override
                public void notFindCity() {
                    endRequest();
                }

                @Override
                public void onFail(Code code) {
                    endRequest();
                }

                @Override
                public void onTimeout() {
                    // 不会超时
                }

                @Override
                public void onError() {
                    endRequest();
                }
            });
            mainRequestTask(location);
        }

        private void requestTask(GeoBean.LocationBean locationBean) {
            startTimeoutTimer();
            cityWeather.setCityInfo(locationBean);
            incrementTaskCount();
            mainRequestTask(locationBean.getId());
        }

        private void requestTask(CityWeather cityWeather) {
            startTimeoutTimer();
            this.cityWeather = cityWeather;
            incrementTaskCount();
            mainRequestTask(cityWeather.getId());
        }

        private void mainRequestTask(String location) {
            QWeather.getWeatherNow(activity, location, new QWeather.OnResultWeatherNowListener() {
                @Override
                public void onError(Throwable throwable) {
                    endRequest();
                }

                @Override
                public void onSuccess(WeatherNowBean weatherNowBean) {
                    if (weatherNowBean.getCode() == Code.OK) {
                        cityWeather.setWeatherNow(weatherNowBean.getNow());
                        incrementTaskCount();
                    } else {
                        endRequest();
                    }
                }
            });
            QWeather.getWeather24Hourly(activity, location, new QWeather.OnResultWeatherHourlyListener() {
                @Override
                public void onError(Throwable throwable) {
                    endRequest();
                }

                @Override
                public void onSuccess(WeatherHourlyBean weatherHourlyBean) {
                    if (weatherHourlyBean.getCode() == Code.OK) {
                        cityWeather.setWeatherHourlyList(weatherHourlyBean.getHourly());
                        incrementTaskCount();
                    } else {
                        endRequest();
                    }
                }
            });
            QWeather.getWeather7D(activity, location, new QWeather.OnResultWeatherDailyListener() {
                @Override
                public void onError(Throwable throwable) {
                    endRequest();
                }

                @Override
                public void onSuccess(WeatherDailyBean weatherDailyBean) {
                    if (weatherDailyBean.getCode() == Code.OK) {
                        cityWeather.setWeatherDailyList(weatherDailyBean.getDaily());
                        incrementTaskCount();
                    } else {
                        endRequest();
                    }
                }
            });
            QWeather.getAirNow(activity, location, Lang.ZH_HANS, new QWeather.OnResultAirNowListener() {
                @Override
                public void onError(Throwable throwable) {
                    endRequest();
                }

                @Override
                public void onSuccess(AirNowBean airNowBean) {
                    if (airNowBean.getCode() == Code.OK) {
                        cityWeather.setAirNow(airNowBean.getNow());
                        incrementTaskCount();
                    } else {
                        endRequest();
                    }
                }
            });
            QWeather.getIndices1D(activity, location, Lang.ZH_HANS, indicesTypes, new QWeather.OnResultIndicesListener() {
                @Override
                public void onError(Throwable throwable) {
                    endRequest();
                }

                @Override
                public void onSuccess(IndicesBean indicesBean) {
                    if (indicesBean.getCode() == Code.OK) {
                        cityWeather.setLifeStatusList(indicesBean.getDailyList());
                        incrementTaskCount();
                    } else {
                        endRequest();
                    }
                }
            });
        }

        private void endRequest() {
            cancelTimeoutTimer();
            callback.onFail();
            callback = emptyCallback;// 空实现以达到终止目的
        }

        private void startTimeoutTimer() {
            if (timeout > 0) {
                taskTimer = Tools.scheduledThreadPool.schedule(() -> activity.runOnUiThread(() -> {
                    callback.onTimeout();
                    callback = emptyCallback;
                }), timeout, TimeUnit.MILLISECONDS);
            }
        }

        private void cancelTimeoutTimer() {
            if (taskTimer != null) {
                taskTimer.cancel(true);
            }
        }
    }

    //===================回调接口=====================
    /**
     * 城市天气数据请求回调接口.
     */
    public interface CityWeatherCallback {
        /**
         * 成功获取城市天气数据时调用.
         *
         * @param cityWeather 城市天气数据
         */
        void onSuccess(CityWeather cityWeather);

        /**
         * 获取天气数据失败时调用.
         */
        void onFail();

        /**
         * 获取天气数据超时时调用.
         */
        void onTimeout();
    }

    /**
     * 城市搜索结果回调接口.
     */
    public interface SearchCityCallback {
        /**
         * 成功获取搜索结果时调用.
         *
         * @param cities 搜索结果列表
         */
        void onSuccess(List<GeoBean.LocationBean> cities);

        /**
         * 无搜索结果时调用.
         */
        void notFindCity();

        /**
         * 请求成功但数据获取失败时调用.
         *
         * @param code 失败码
         */
        void onFail(Code code);

        /**
         * 获取搜索结果超时时调用.
         */
        void onTimeout();

        /**
         * 请求失败时调用.<br>
         * 多为网络问题.
         */
        void onError();
    }
}
