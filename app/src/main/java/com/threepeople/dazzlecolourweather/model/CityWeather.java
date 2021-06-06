package com.threepeople.dazzlecolourweather.model;

import androidx.annotation.NonNull;

import com.qweather.sdk.bean.IndicesBean;
import com.qweather.sdk.bean.air.AirNowBean;
import com.qweather.sdk.bean.geo.GeoBean;
import com.qweather.sdk.bean.weather.WeatherDailyBean;
import com.qweather.sdk.bean.weather.WeatherHourlyBean;
import com.qweather.sdk.bean.weather.WeatherNowBean;

import java.util.List;

/**
 * 城市天气相关数据.
 */
public class CityWeather {
    private long updateTime;// 数据更新时间
    // 城市属性
    private String id;// 城市id
    private String country;// 所属国家，如：中国
    private String adm1;// 所属一级行政区，如：江苏
    private String adm2;// 所属次级行政区，如：扬州
    private String cityName;// 城市（区、县）名称，如：邗江
    // 以下是天气数据
    private WeatherNowBean.NowBaseBean weatherNow;// 实况天气数据
    private List<WeatherHourlyBean.HourlyBean> weatherHourlyList;// 未来24小时逐小时预报
    private List<WeatherDailyBean.DailyBean> weatherDailyList;// 未来7天逐天预报
    private AirNowBean.NowBean airNow;// 实况空气质量
    private List<IndicesBean.DailyBean> lifeStatusList;// 生活指数实况

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public void setCityInfo(GeoBean.LocationBean locationBean) {
        this.id = locationBean.getId();
        this.country = locationBean.getCountry();
        this.adm1 = locationBean.getAdm1();
        this.adm2 = locationBean.getAdm2();
        this.cityName = locationBean.getName();
    }

    public void setWeatherNow(WeatherNowBean.NowBaseBean weatherNow) {
        this.weatherNow = weatherNow;
    }

    public void setWeatherHourlyList(List<WeatherHourlyBean.HourlyBean> weatherHourlyList) {
        this.weatherHourlyList = weatherHourlyList;
    }

    public void setWeatherDailyList(List<WeatherDailyBean.DailyBean> weatherDailyList) {
        this.weatherDailyList = weatherDailyList;
    }

    public void setAirNow(AirNowBean.NowBean airNow) {
        this.airNow = airNow;
    }

    public void setLifeStatusList(List<IndicesBean.DailyBean> lifeStatusList) {
        this.lifeStatusList = lifeStatusList;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public String getId() {
        return id;
    }

    public String getCountry() {
        return country;
    }

    public String getAdm1() {
        return adm1;
    }

    public String getAdm2() {
        return adm2;
    }

    public String getCityName() {
        return cityName;
    }

    public WeatherNowBean.NowBaseBean getWeatherNow() {
        return weatherNow;
    }

    public List<WeatherHourlyBean.HourlyBean> getWeatherHourlyList() {
        return weatherHourlyList;
    }

    public List<WeatherDailyBean.DailyBean> getWeatherDailyList() {
        return weatherDailyList;
    }

    public AirNowBean.NowBean getAirNow() {
        return airNow;
    }

    public List<IndicesBean.DailyBean> getLifeStatusList() {
        return lifeStatusList;
    }

    @NonNull
    @Override
    public String toString() {
        return "CityWeather{" +
                "\n\t" + country + "：" + adm1 + " - " + adm2 + " - " + cityName +
                "\n\t实况天气：" + weatherNow.getText() +
                "\n\t两小时后天气：" + weatherHourlyList.get(1).getText() +
                "\n\t明天白天天气：" + weatherDailyList.get(0).getTextDay() +
                "\n\t实况空气质量：" + airNow.getLevel() +
                "\n\t穿衣指数等级：" + lifeStatusList.get(0).getLevel()
                + "\n}";
    }
}
