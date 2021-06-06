package com.threepeople.dazzlecolourweather.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.qweather.sdk.bean.IndicesBean;
import com.qweather.sdk.bean.weather.WeatherDailyBean;
import com.qweather.sdk.bean.weather.WeatherHourlyBean;
import com.threepeople.dazzlecolourweather.R;
import com.threepeople.dazzlecolourweather.model.CityWeather;
import com.threepeople.dazzlecolourweather.model.NetRequest;
import com.threepeople.dazzlecolourweather.theme.ColorfulDayUITheme;
import com.threepeople.dazzlecolourweather.utils.Tools;
import com.threepeople.dazzlecolourweather.view.AutoHeightViewPager;
import com.threepeople.dazzlecolourweather.view.ColorChangeableIconView;
import com.threepeople.dazzlecolourweather.view.MyScrollView;
import com.threepeople.dazzlecolourweather.view.ProgressView;
import com.threepeople.dazzlecolourweather.view.WaterWaveRefreshLayout;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HomePagerActivity extends AppCompatActivity {
    private final AppCompatActivity activity;
    private double[] locationData;
    private int currentPagerIndex;
    private ProgressView progressView;
    private final Rect rect;
    private boolean lastIsVisible;// 标记progressView可见性
    private final int toAdminRequestCode;
    private float oneDp;
    private final int toSettingThemeCode;

    private View emptyLayout;// “空”布局
    private MyScrollView scrollView;
    private WaterWaveRefreshLayout homePagerRefreshLayout;// 顶层布局及刷新布局
    private ColorChangeableIconView toSettingTheme;// 设置主题图标
    private TextView cityNameTitle;// 标题栏城市名
    private TextView cityNameTitleAdd;// ">>"字符串
    private LinearLayout toAdminCityLayout;// 管理城市图标所在布局
    private ColorChangeableIconView toAdminCity;// 管理城市图标
    private AutoHeightViewPager cityWeatherPager;// 展示多个城市天气数据的ViewPager
    private LinearLayout pointLinear;// 圆点布局

    {
        activity = this;
        locationData = null;
        currentPagerIndex = 0;
        progressView = null;
        rect = new Rect();
        lastIsVisible = true;
        toAdminRequestCode = Tools.getRequestCode();
        toSettingThemeCode = Tools.getRequestCode();

        emptyLayout = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_pager);

        oneDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.0f, getResources().getDisplayMetrics());
        initView();
        settingTheme();
        myListener();
        locationData = getIntent().getDoubleArrayExtra(Tools.locationDataKey);
        currentPagerIndex = getIntent().getIntExtra(Tools.currentPagerKey, -1);
        if (Tools.cities.size() == 0) {
            showEmptyLayout();
        } else {
            if (currentPagerIndex < 0) {
                SharedPreferences read = getSharedPreferences(Tools.weatherCfg, MODE_PRIVATE);
                try {
                    currentPagerIndex = Integer.parseInt(read.getString(Tools.currentPagerKey, "0"));
                    if (currentPagerIndex >= Tools.cities.size()) {
                        currentPagerIndex = 0;
                    }
                } catch (Exception e) {
                    currentPagerIndex = 0;
                }
            }
            initPager();
        }
    }

    private void initView() {
        emptyLayout            = findViewById(R.id.emptyLayout);
        scrollView             = findViewById(R.id.scrollView);
        homePagerRefreshLayout = findViewById(R.id.homePagerRefreshLayout);
        toSettingTheme         = findViewById(R.id.toSettingTheme);
        cityNameTitle          = findViewById(R.id.cityNameTitle);
        cityNameTitleAdd       = findViewById(R.id.cityNameTitleAdd);
        toAdminCityLayout      = findViewById(R.id.toAdminCityLayout);
        toAdminCity            = findViewById(R.id.toAdminCity);
        cityWeatherPager       = findViewById(R.id.cityWeatherPager);
        pointLinear            = findViewById(R.id.pointLinear);
    }

    private void settingTheme() {
        if (Tools.theme.useBlackOnStatusBar()) {
            Tools.setBlackWordOnStatus(this);
        } else {
            Tools.clearStatusTextColor(activity);
        }
        if (Tools.theme instanceof ColorfulDayUITheme) {
            homePagerRefreshLayout.setBackground(((ColorfulDayUITheme) Tools.theme).getHomeBackground());
        } else {
            homePagerRefreshLayout.setBackground(Tools.theme.getBackground());
        }
        homePagerRefreshLayout.setTipTextColor(Tools.theme.getStressColor());
        homePagerRefreshLayout.setWaterWaveColor(Tools.theme.getStressColor());
        toSettingTheme.setGoalColor(Tools.theme.getIconColor());
        cityNameTitle.setTextColor(Tools.theme.getTextColor());
        cityNameTitleAdd.setTextColor(Tools.theme.getTextColor());
        toAdminCityLayout.setBackground(Tools.theme.getTopButtonBackground());
        toAdminCity.setGoalColor(Tools.theme.getIconColor());
    }

    private void initPager() {
        addPoint();
        cityWeatherPager.setAdapter(new CityWeatherPagerAdapter(activity));
        cityWeatherPager.setCurrentItem(currentPagerIndex);
    }

    private void showEmptyLayout() {
        scrollView.setVisibility(View.GONE);
        emptyLayout.setVisibility(View.VISIBLE);
    }

    private void hideEmptyLayout() {
        scrollView.setVisibility(View.VISIBLE);
        emptyLayout.setVisibility(View.GONE);
    }

    private void addPoint() {
        int fiveDp = (int) (5 * oneDp);
        pointLinear.removeAllViews();
        View point;
        LinearLayout.LayoutParams params;
        for (int i = 0; i < Tools.cities.size(); i++) {
            point = new View(activity);
            point.setBackground(Tools.theme.getPointBackground());
            params = new LinearLayout.LayoutParams(fiveDp, fiveDp);
            if (i > 0) {
                params.leftMargin = fiveDp;
            }
            point.setLayoutParams(params);
            if (i == currentPagerIndex) {
                point.setEnabled(false);
            }
            pointLinear.addView(point);
        }
    }

    private void myListener() {
        View.OnClickListener listener = v -> {
            /*Intent toWebPage = new Intent(activity, WebPageActivity.class);
            toWebPage.putExtra(Tools.cityIdKey, Tools.cities.get(currentPagerIndex).getId());*/
            CityWeather theCity = Tools.cities.get(currentPagerIndex);
            Intent toWebPage = new Intent(Intent.ACTION_VIEW, Uri.parse("http://mzkt.xyz/weather?cityAddress=" +
                    theCity.getId() + "&cityName=" + theCity.getCityName()));
            startActivity(toWebPage);
        };
        cityNameTitle.setOnClickListener(listener);
        cityNameTitleAdd.setOnClickListener(listener);

        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            if (scrollView.getScrollY() == 0) {
                homePagerRefreshLayout.setClickable(true);
                homePagerRefreshLayout.setInterceptTouchEvent(true);
            } else {
                homePagerRefreshLayout.setClickable(false);
            }
            if (progressView != null) {
                if (progressView.getGlobalVisibleRect(rect)) {// 可见时
                    if (!lastIsVisible) {
                        progressView.startAnimator();
                    }
                    lastIsVisible = true;
                } else {
                    if (lastIsVisible) {
                        progressView.stopAnimator();
                    }
                    lastIsVisible = false;
                }
            } else {
                getProgressView();
            }
        });

        homePagerRefreshLayout.setOnRefreshListener(() -> {
            cityWeatherPager.setCanScroll(false);
            scrollView.setCanScroll(false);
            if (Tools.cities.size() != 0) {
                NetRequest.requestCityWeather(activity, Tools.cities.get(currentPagerIndex), 3000, new NetRequest.CityWeatherCallback() {
                    @Override
                    public void onSuccess(CityWeather cityWeather) {
                        Tools.cities.set(currentPagerIndex, cityWeather);
                        Tools.saveObjectAtFile(Tools.weatherDataCacheFile, Tools.cities);
                        cityWeatherPager.setAdapter(new CityWeatherPagerAdapter(activity));
                        cityWeatherPager.setCurrentItem(currentPagerIndex);
                        homePagerRefreshLayout.setRefresh(false);
                        reset();
                    }

                    @Override
                    public void onFail() {
                        homePagerRefreshLayout.endRefresh("刷新失败");
                        reset();
                    }

                    @Override
                    public void onTimeout() {
                        homePagerRefreshLayout.endRefresh("网络连接超时");
                        reset();
                    }

                    private void reset() {
                        cityWeatherPager.setCanScroll(true);
                        scrollView.setCanScroll(true);
                    }
                });
            } else {
                double longitude;
                double latitude;
                if (locationData != null) {
                    longitude = locationData[0];
                    latitude = locationData[1];
                } else {
                    longitude = 116.40528;
                    latitude = 39.90498;
                }
                NetRequest.requestCityWeather(activity, longitude, latitude, 3000, new NetRequest.CityWeatherCallback() {
                    @Override
                    public void onSuccess(CityWeather cityWeather) {
                        homePagerRefreshLayout.setRefresh(false);
                        reset();
                        Tools.cities.add(cityWeather);
                        Tools.saveObjectAtFile(Tools.weatherDataCacheFile, Tools.cities);
                        hideEmptyLayout();
                        cityWeatherPager.setAdapter(new CityWeatherPagerAdapter(activity));
                    }

                    @Override
                    public void onFail() {
                        homePagerRefreshLayout.endRefresh("刷新失败");
                        reset();
                    }

                    @Override
                    public void onTimeout() {
                        homePagerRefreshLayout.endRefresh("网络连接超时");
                        reset();
                    }

                    private void reset() {
                        cityWeatherPager.setCanScroll(true);
                        scrollView.setCanScroll(true);
                    }
                });
            }
        });

        cityWeatherPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                if (currentPagerIndex != position) {
                    pointLinear.getChildAt(currentPagerIndex).setEnabled(true);
                    currentPagerIndex = position;
                    pointLinear.getChildAt(currentPagerIndex).setEnabled(false);
                }
                Tools.saveSharedPreferences(activity, Tools.weatherCfg, Tools.currentPagerKey, String.valueOf(currentPagerIndex));
                CityWeather theCity = Tools.cities.get(currentPagerIndex);
                cityNameTitle.setText(theCity.getCityName());
                if ((Tools.getNowTime() - theCity.getUpdateTime()) >= 6 * 60 * 60000) {
                    homePagerRefreshLayout.setRefresh(true);
                }
                getProgressView();
                lastIsVisible = false;
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        toSettingTheme.setOnClickListener(v -> {
            if (!homePagerRefreshLayout.isRefresh()) {
                startActivityForResult(new Intent(activity, SettingThemeActivity.class), toSettingThemeCode);
            }
        });

        toAdminCityLayout.setOnClickListener(v -> {
            if (!homePagerRefreshLayout.isRefresh()) {
                startActivityForResult(new Intent(activity, AdminCityActivity.class), toAdminRequestCode);
            }
        });
    }

    private void getProgressView() {
        PagerAdapter adapter = cityWeatherPager.getAdapter();
        if (adapter instanceof CityWeatherPagerAdapter) {
            progressView = ((CityWeatherPagerAdapter) adapter).getIndexProgressView(currentPagerIndex);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == toAdminRequestCode) {
            if (data != null && data.getBooleanExtra(Tools.hasDeleteKey, false)) {
                currentPagerIndex = 0;
                addPoint();
                cityWeatherPager.setAdapter(new CityWeatherPagerAdapter(activity));
                cityWeatherPager.setCurrentItem(0);
            }
        } else if (requestCode == toSettingThemeCode) {
            if (data != null) {
                boolean hasChange = data.getBooleanExtra(Tools.hasChangeThemeKey, false);
                boolean hasEdit   = data.getBooleanExtra(Tools.hasEditCustomKey, false);
                if (hasChange) {
                    if (hasEdit) {
                        Tools.saveCustomUITheme(activity);
                    } else {
                        Tools.saveUIThemeTypeIfNotCustom(activity);
                    }
                    settingTheme();
                    initPager();
                } else if (hasEdit) {
                    Tools.saveCustomUITheme(activity);
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private static class CityWeatherPagerAdapter extends AutoHeightViewPager.AutoHeightPagerAdapter {
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("更新于：MM月dd日 HH:mm", Locale.CHINA);
        private final Context context;
        private final View[] views;
        private final View[][] hourlyViews;

        private CityWeatherPagerAdapter(Context context) {
            this.context = context;
            this.views = new View[Tools.cities.size()];
            this.hourlyViews = new View[Tools.cities.size()][];
        }

        @Override
        public int getCount() {
            return Tools.cities.size();
        }

        public ProgressView getIndexProgressView(int index) {
            try {
                return ((ViewHolder) views[index].getTag()).progressView;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            ViewHolder viewHolder;
            if (views[position] == null) {
                views[position] = View.inflate(context, R.layout.item_pager_city_weather, null);
                viewHolder = new ViewHolder();
                // 绑定id
                viewHolder.weatherIcon    = views[position].findViewById(R.id.weatherIcon);
                viewHolder.weatherTextTmp = views[position].findViewById(R.id.weatherTextTmp);
                viewHolder.iconLocation   = views[position].findViewById(R.id.iconLocation);
                viewHolder.cityName       = views[position].findViewById(R.id.cityName);
                viewHolder.updateTime     = views[position].findViewById(R.id.updateTime);
                viewHolder.hourlyForecast = views[position].findViewById(R.id.hourlyForecast);
                viewHolder.forecast7DList = views[position].findViewById(R.id.forecast7DList);
                viewHolder.progressView   = views[position].findViewById(R.id.progressView);
                viewHolder.pm10           = views[position].findViewById(R.id.pm10);
                viewHolder.pm2p5          = views[position].findViewById(R.id.pm2p5);
                viewHolder.no2            = views[position].findViewById(R.id.no2);
                viewHolder.so2            = views[position].findViewById(R.id.so2);
                viewHolder.o3             = views[position].findViewById(R.id.o3);
                viewHolder.co             = views[position].findViewById(R.id.co);
                viewHolder.lifeCoatLevel  = views[position].findViewById(R.id.lifeCoatLevel);
                viewHolder.lifeSportLevel = views[position].findViewById(R.id.lifeSportLevel);
                viewHolder.lifeFluLevel   = views[position].findViewById(R.id.lifeFluLevel);
                viewHolder.lifeCatLevel   = views[position].findViewById(R.id.lifeCatLevel);
                // 网页跳转
                ColorChangeableIconView weatherBrand = views[position].findViewById(R.id.weatherBrand);
                weatherBrand.setOnClickListener(v -> context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.qweather.com/"))));
                TextView iconBrand = views[position].findViewById(R.id.iconBrand);
                iconBrand.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
                iconBrand.setOnClickListener((v -> context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://creativecommons.org/licenses/by/4.0/deed.zh")))));
                // 主题设置
                viewHolder.weatherIcon.setGoalColor(Tools.theme.getIconColor());
                viewHolder.weatherTextTmp.setTextColor(Tools.theme.getTextColor());
                viewHolder.iconLocation.setGoalColor(Tools.theme.getIconColor());
                viewHolder.cityName.setTextColor(Tools.theme.getTextColor());
                weatherBrand.setGoalColor(Tools.theme.getIconColor());
                viewHolder.updateTime.setTextColor(Tools.theme.getTextColor());
                views[position].findViewById(R.id.line).setBackgroundColor(Tools.theme.getIgnoreColor());
                ((TextView) views[position].findViewById(R.id.forecast7DTitle)).setTextColor(Tools.theme.getTextColor());
                ((TextView) views[position].findViewById(R.id.airTitle)).setTextColor(Tools.theme.getTextColor());
                ((TextView) views[position].findViewById(R.id.airPolluteTitle)).setTextColor(Tools.theme.getTextColor());
                viewHolder.progressView.setNewColor(Tools.theme.getIgnoreColor(), Tools.theme.getStressColor(), Tools.theme.getIgnoreColor());
                ((TextView) views[position].findViewById(R.id.pm10Title)).setTextColor(Tools.theme.getTextColor());
                ((TextView) views[position].findViewById(R.id.pm2p5Title)).setTextColor(Tools.theme.getTextColor());
                ((TextView) views[position].findViewById(R.id.no2Title)).setTextColor(Tools.theme.getTextColor());
                ((TextView) views[position].findViewById(R.id.so2Title)).setTextColor(Tools.theme.getTextColor());
                ((TextView) views[position].findViewById(R.id.o3Title)).setTextColor(Tools.theme.getTextColor());
                ((TextView) views[position].findViewById(R.id.coTitle)).setTextColor(Tools.theme.getTextColor());
                viewHolder.pm10.setTextColor(Tools.theme.getTextColor());
                viewHolder.pm2p5.setTextColor(Tools.theme.getTextColor());
                viewHolder.no2.setTextColor(Tools.theme.getTextColor());
                viewHolder.so2.setTextColor(Tools.theme.getTextColor());
                viewHolder.o3.setTextColor(Tools.theme.getTextColor());
                viewHolder.co.setTextColor(Tools.theme.getTextColor());
                ((TextView) views[position].findViewById(R.id.lifeTitle)).setTextColor(Tools.theme.getTextColor());
                views[position].findViewById(R.id.life).setBackground(Tools.theme.getLifeBackground());
                ((ColorChangeableIconView) views[position].findViewById(R.id.lifeCoat)).setGoalColor(Tools.theme.getThemeColor());
                ((TextView) views[position].findViewById(R.id.lifeCoatTitle)).setTextColor(Tools.theme.getThemeColor());
                viewHolder.lifeCoatLevel.setTextColor(Tools.theme.getThemeColor());
                ((ColorChangeableIconView) views[position].findViewById(R.id.lifeSport)).setGoalColor(Tools.theme.getThemeColor());
                ((TextView) views[position].findViewById(R.id.lifeSportTitle)).setTextColor(Tools.theme.getThemeColor());
                viewHolder.lifeSportLevel.setTextColor(Tools.theme.getThemeColor());
                ((ColorChangeableIconView) views[position].findViewById(R.id.lifeFlu)).setGoalColor(Tools.theme.getThemeColor());
                ((TextView) views[position].findViewById(R.id.lifeFluTitle)).setTextColor(Tools.theme.getThemeColor());
                viewHolder.lifeFluLevel.setTextColor(Tools.theme.getThemeColor());
                ((ColorChangeableIconView) views[position].findViewById(R.id.lifeCat)).setGoalColor(Tools.theme.getThemeColor());
                ((TextView) views[position].findViewById(R.id.lifeCatTitle)).setTextColor(Tools.theme.getThemeColor());
                viewHolder.lifeCatLevel.setTextColor(Tools.theme.getThemeColor());
                iconBrand.setTextColor(Tools.theme.getTextColor());
                // 设置TAG
                views[position].setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) views[position].getTag();
            }
            CityWeather cityWeather = Tools.cities.get(position);
            viewHolder.weatherIcon.setImageDrawable(Tools.getWeatherIcon(context, cityWeather.getWeatherNow().getIcon()));
            viewHolder.weatherTextTmp.setText(cityWeather.getWeatherNow().getText() + "  " + cityWeather.getWeatherNow().getTemp() + "\u2103");
            if (cityWeather.getId().equals(Tools.nowLocationCityId)) {
                viewHolder.iconLocation.setVisibility(View.VISIBLE);
            } else {
                viewHolder.iconLocation.setVisibility(View.GONE);
            }
            viewHolder.cityName.setText(cityWeather.getCityName());
            viewHolder.updateTime.setText(dateFormat.format(cityWeather.getUpdateTime()));
            viewHolder.forecast7DList.setAdapter(new Forecast7DListAdapter(context, cityWeather.getWeatherDailyList()));
            addHourlyForecast(position, viewHolder.hourlyForecast);
            viewHolder.progressView.setCurrentValue(Integer.parseInt(cityWeather.getAirNow().getAqi()));
            viewHolder.pm10.setText(cityWeather.getAirNow().getPm10());
            viewHolder.pm2p5.setText(cityWeather.getAirNow().getPm2p5());
            viewHolder.no2.setText(cityWeather.getAirNow().getNo2());
            viewHolder.so2.setText(cityWeather.getAirNow().getSo2());
            viewHolder.o3.setText(cityWeather.getAirNow().getO3());
            viewHolder.co.setText(cityWeather.getAirNow().getCo());
            for (IndicesBean.DailyBean dailyBean : cityWeather.getLifeStatusList()){
                String level;
                switch (dailyBean.getType()) {
                    case "3":// 穿衣指数
                        switch (dailyBean.getLevel()) {
                            case "1":
                                level = "寒冷";
                                break;
                            case "2":
                                level = "冷";
                                break;
                            case "3":
                                level = "较冷";
                                break;
                            case "4":
                                level = "较舒适";
                                break;
                            case "5":
                                level = "舒适";
                                break;
                            case "6":
                                level = "热";
                                break;
                            case "7":
                                level = "炎热";
                                break;
                            default:
                                level = "未知";
                                break;
                        }
                        viewHolder.lifeCoatLevel.setText(level);
                        break;
                    case "1":// 运动指数
                        switch (dailyBean.getLevel()) {
                            case "1":
                                level = "适宜";
                                break;
                            case "2":
                                level = "较适宜";
                                break;
                            case "3":
                                level = "较不宜";
                                break;
                            default:
                                level = "未知";
                                break;
                        }
                        viewHolder.lifeSportLevel.setText(level);
                        break;
                    case "9":// 感冒指数
                        switch (dailyBean.getLevel()) {
                            case "1":
                                level = "少发";
                                break;
                            case "2":
                                level = "较易发";
                                break;
                            case "3":
                                level = "易发";
                                break;
                            case "4":
                                level = "极易发";
                                break;
                            default:
                                level = "未知";
                                break;
                        }
                        viewHolder.lifeFluLevel.setText(level);
                        break;
                    case "2":// 洗车指数
                        switch (dailyBean.getLevel()) {
                            case "1":
                                level = "适宜";
                                break;
                            case "2":
                                level = "较适宜";
                                break;
                            case "3":
                                level = "较不宜";
                                break;
                            case "4":
                                level = "不宜";
                                break;
                            default:
                                level = "未知";
                                break;
                        }
                        viewHolder.lifeCatLevel.setText(level);
                        break;
                }
            }
            container.addView(views[position]);
            return views[position];
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView(views[position]);
        }

        @Override
        public View getIndexView(int index) {
            return views[index];
        }

        private void addHourlyForecast(int position, LinearLayout parentView) {
            parentView.removeAllViews();
            List<WeatherHourlyBean.HourlyBean> hourlyDataList = Tools.cities.get(position).getWeatherHourlyList();
            if (hourlyViews[position] == null) {
                hourlyViews[position] = new View[hourlyDataList.size()];
            }
            for (int i = 0; i < hourlyDataList.size(); i++) {
                HourlyViewHolder viewHolder;
                if (hourlyViews[position][i] == null) {
                    hourlyViews[position][i] = View.inflate(context, R.layout.item_hourly_forecast, null);
                    viewHolder = new HourlyViewHolder();
                    viewHolder.hourlyIcon = hourlyViews[position][i].findViewById(R.id.hourlyIcon);
                    viewHolder.hourlyTmp = hourlyViews[position][i].findViewById(R.id.hourlyTmp);
                    viewHolder.hourlyTime = hourlyViews[position][i].findViewById(R.id.hourlyTime);
                    // 主题设置
                    viewHolder.hourlyIcon.setGoalColor(Tools.theme.getIconColor());
                    viewHolder.hourlyTmp.setTextColor(Tools.theme.getTextColor());
                    viewHolder.hourlyTime.setTextColor(Tools.theme.getTextColor());
                    hourlyViews[position][i].setTag(viewHolder);
                } else {
                    viewHolder = (HourlyViewHolder) hourlyViews[position][i].getTag();
                }
                WeatherHourlyBean.HourlyBean hourlyData = hourlyDataList.get(i);
                viewHolder.hourlyIcon.setImageDrawable(Tools.getWeatherIcon(context, hourlyData.getIcon()));
                viewHolder.hourlyTmp.setText(hourlyData.getTemp() + "\u2103");
                viewHolder.hourlyTime.setText(hourlyData.getFxTime().substring(11, 13) + "h");
                parentView.addView(hourlyViews[position][i]);
            }
        }

        private static class ViewHolder {
            private ColorChangeableIconView weatherIcon;// 天气图标
            private TextView weatherTextTmp;// 天气状况和温度（晴  15\u2103）
            private ColorChangeableIconView iconLocation;// 定位图标
            private TextView cityName;// 城市（县、区）名
            private TextView updateTime;// 天气数据更新时间（更新于：1月1日 12:34）
            private LinearLayout hourlyForecast;// 逐小时预报数据
            private ListView forecast7DList;// 逐天预报ListView
            private ProgressView progressView;// 空气污染等级进度
            private TextView pm10;// PM10 数据
            private TextView pm2p5;// PM2.5 数据
            private TextView no2;// NO2 数据
            private TextView so2;// SO2 数据
            private TextView o3;// O3 数据
            private TextView co;// CO 数据
            private TextView lifeCoatLevel;// 穿衣指数等级
            private TextView lifeSportLevel;// 运动指数等级
            private TextView lifeFluLevel;// 感冒指数等级
            private TextView lifeCatLevel;// 洗车指数等级
        }

        private static class HourlyViewHolder {
            private ColorChangeableIconView hourlyIcon;
            private TextView hourlyTmp;
            private TextView hourlyTime;
        }

        private static class Forecast7DListAdapter extends BaseAdapter {
            private final Context context;
            private final List<WeatherDailyBean.DailyBean> forecast7DList;

            private Forecast7DListAdapter(Context context, List<WeatherDailyBean.DailyBean> forecast7DList) {
                this.context = context;
                this.forecast7DList = forecast7DList;
            }

            @Override
            public int getCount() {
                return forecast7DList.size();
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder viewHolder;
                if (convertView == null) {
                    convertView = View.inflate(context, R.layout.item_list_forecast_7d, null);
                    viewHolder = new ViewHolder();
                    viewHolder.date = convertView.findViewById(R.id.date);
                    viewHolder.week = convertView.findViewById(R.id.week);
                    viewHolder.maxMinTmp = convertView.findViewById(R.id.maxMinTmp);
                    viewHolder.forecastText = convertView.findViewById(R.id.forecastText);
                    viewHolder.forecastIcon = convertView.findViewById(R.id.forecastIcon);
                    // 主题设置
                    viewHolder.date.setTextColor(Tools.theme.getTextColor());
                    viewHolder.week.setTextColor(Tools.theme.getTextColor());
                    viewHolder.maxMinTmp.setTextColor(Tools.theme.getTextColor());
                    viewHolder.forecastText.setTextColor(Tools.theme.getTextColor());
                    viewHolder.forecastIcon.setGoalColor(Tools.theme.getIconColor());
                    convertView.findViewById(R.id.line).setBackgroundColor(Tools.theme.getIgnoreColor());
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }
                WeatherDailyBean.DailyBean forecastData = forecast7DList.get(position);
                String date = forecastData.getFxDate();
                viewHolder.date.setText(date.substring(5));
                String week;
                switch (position) {
                    case 0:
                        week = "今天";
                        break;
                    case 1:
                        week = "明天";
                        break;
                    default:
                        week = Tools.getWeek(date);
                        break;
                }
                viewHolder.week.setText(week);
                viewHolder.maxMinTmp.setText(forecastData.getTempMax() + "\u2103 / " + forecastData.getTempMin() + "\u2103");
                viewHolder.forecastText.setText(forecastData.getTextDay());
                viewHolder.forecastIcon.setImageDrawable(Tools.getWeatherIcon(context, forecastData.getIconDay()));
                return convertView;
            }

            private static class ViewHolder {
                private TextView date;// 预报日期
                private TextView week;// 预报日期的周几形式
                private TextView maxMinTmp;// 最高温、最低温
                private TextView forecastText;// 预报白天天气情况
                private ColorChangeableIconView forecastIcon;// 预报白天天气情况图标
            }
        }
    }
}
