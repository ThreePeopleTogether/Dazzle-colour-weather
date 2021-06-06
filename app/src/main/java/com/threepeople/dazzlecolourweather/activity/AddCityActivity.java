package com.threepeople.dazzlecolourweather.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.qweather.sdk.bean.base.Code;
import com.qweather.sdk.bean.geo.GeoBean;
import com.threepeople.dazzlecolourweather.R;
import com.threepeople.dazzlecolourweather.model.CityWeather;
import com.threepeople.dazzlecolourweather.model.NetRequest;
import com.threepeople.dazzlecolourweather.utils.Tools;
import com.threepeople.dazzlecolourweather.view.ColorChangeableIconView;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddCityActivity extends AppCompatActivity {
    private final AppCompatActivity activity;
    private final View[] hotCityViews;
    private final HotCity[] hotCities;
    private InputMethodManager inputMethodManager;
    private boolean locationRunning;// 是否正在定位
    private boolean locationSuccess;// 是否定位成功
    GeoBean.LocationBean cityLocationData;// 仅由定位获得的数据
    private final NetRequest.CityWeatherCallback cityWeatherCallback;

    private ViewGroup searchCityLayout;
    private ViewGroup returnAdminLayout;
    private ColorChangeableIconView returnAdmin;
    private TextView searchCityTitle;
    private ViewGroup locationLayout;
    private ColorChangeableIconView locationIcon;
    private TextView locationText;
    private ViewGroup searchLayout;
    private ColorChangeableIconView searchIcon;
    private EditText inputSearch;
    private LinearLayout hotCityLayout;
    private ListView searchResultList;

    {
        activity = this;
        hotCityViews = new View[4];
        hotCities = new HotCity[] {
                new HotCity("101010100", "北京"),
                new HotCity("101020100", "上海"),
                new HotCity("101280101", "广州"),
                new HotCity("101280601", "深圳")
        };
        cityWeatherCallback = new NetRequest.CityWeatherCallback() {
            @Override
            public void onSuccess(CityWeather cityWeather) {
                Tools.cities.add(cityWeather);
                Tools.saveObjectAtFile(Tools.weatherDataCacheFile, Tools.cities);
                startActivity(getRestartIntent(Tools.cities.size() - 1));
            }

            @Override
            public void onFail() {
                Toast.makeText(activity, "获取天气数据失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTimeout() {
                Toast.makeText(activity, "网络连接超时", Toast.LENGTH_SHORT).show();
            }
        };
        locationRunning = true;
        locationSuccess = false;
        cityLocationData = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_city);
        if (Tools.theme.useBlackOnStatusBar()) {
            Tools.setBlackWordOnStatus(this);
        }

        inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        initView();
        settingTheme();
        addHotCity();
        inputSearch.setFilters(new InputFilter[]{new InputFilter() {
            private final Pattern pattern = Pattern.compile("[^a-zA-Z\u4E00-\u9FCB]");

            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                Matcher matcher = pattern.matcher(source);
                return matcher.replaceAll("");
            }
        }});
        myListener();
        Tools.getLocation(activity, new Tools.LocationCallback() {
            @Override
            public void onSuccess(double longitude, double latitude) {
                requestLocationData(longitude, latitude);
            }

            @Override
            public void onFail(Context context) {
                locationRunning = false;
                locationText.setText("定位失败");
            }

            @Override
            public void lackProvider() {
                onFail(activity);
            }

            @Override
            public void lackPermission(Context context) {
                onFail(context);
            }
        });
    }

    private void initView() {
        searchCityLayout  = findViewById(R.id.searchCityLayout);
        returnAdminLayout = findViewById(R.id.returnAdminLayout);
        returnAdmin       = findViewById(R.id.returnAdmin);
        searchCityTitle   = findViewById(R.id.searchCityTitle);
        locationLayout    = findViewById(R.id.locationLayout);
        locationIcon      = findViewById(R.id.locationIcon);
        locationText      = findViewById(R.id.locationText);
        searchLayout      = findViewById(R.id.searchLayout);
        searchIcon        = findViewById(R.id.searchIcon);
        inputSearch       = findViewById(R.id.inputSearch);
        hotCityLayout     = findViewById(R.id.hotCityLayout);
        searchResultList  = findViewById(R.id.searchResultList);
    }

    private void settingTheme() {
        searchCityLayout.setBackground(Tools.theme.getBackground());
        returnAdminLayout.setBackground(Tools.theme.getTopButtonBackground());
        returnAdmin.setGoalColor(Tools.theme.getIconColor());
        searchCityTitle.setTextColor(Tools.theme.getTextColor());
        locationIcon.setGoalColor(Tools.theme.getIconColor());
        locationText.setTextColor(Tools.theme.getTextColor());
        searchLayout.setBackground(Tools.theme.getInputCityBackground());
        searchIcon.setGoalColor(Tools.theme.getIconColor());
        inputSearch.setHintTextColor(Tools.theme.getIgnoreColor());
        inputSearch.setTextColor(Tools.theme.getTextColor());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            inputSearch.setTextCursorDrawable(Tools.theme.getInputCursorBackground());
        }
    }

    private void myListener() {
        returnAdminLayout.setOnClickListener((v) -> onBackPressed());

        locationLayout.setOnClickListener((v) -> {
            if (locationSuccess) {
                Intent intent = hasTheCity(cityLocationData.getId());
                if (intent == null) {
                    NetRequest.requestCityWeather(activity, cityLocationData, 3000, cityWeatherCallback);
                } else {
                    startActivity(intent);
                }
            } else if (!locationRunning) {
                // 定位失败后主动定位
                locationText.setText("正在定位");
                Tools.getLocation(activity, new Tools.LocationCallback() {
                    @Override
                    public void onSuccess(double longitude, double latitude) {
                        requestLocationData(longitude, latitude);
                    }

                    @Override
                    public void onFail(Context context) {
                        locationRunning = false;
                        locationText.setText("定位失败");
                    }

                    @Override
                    public void lackProvider() {
                        onFail(activity);
                        Tools.showDialogWithTwoButton(activity, "定位服务未启用，是否前往设置？", new Tools.ControlDialog() {
                            @Override
                            public void onDialog(AlertDialog dialog, View dialogView) {}

                            @Override
                            public void onOk(AlertDialog dialog) {
                                dialog.dismiss();
                                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            }

                            @Override
                            public void onCancel(AlertDialog dialog) {
                                dialog.dismiss();
                            }
                        });
                    }

                    @Override
                    public void lackPermission(Context context) {
                        onFail(activity);
                        Toast.makeText(activity, "没有定位权限", Toast.LENGTH_SHORT).show();
                        String[] permissions = new String[] {
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                        };
                        ActivityCompat.requestPermissions(activity, permissions, Tools.getRequestCode());
                    }
                });
            }
        });

        inputSearch.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                inputMethodManager.hideSoftInputFromWindow(inputSearch.getWindowToken(), 0);
                searchResultList.setAdapter(null);
                String input = String.valueOf(inputSearch.getText());
                if (TextUtils.isEmpty(input)) {
                    Toast.makeText(activity, "输入不能为空", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (input.length() == 1) {
                    char in = input.charAt(0);
                    if ((in >= 'a' && in <= 'z') || (in >= 'A' && in <= 'Z')) {
                        Toast.makeText(activity, "请至少输入两个英文字母或一个汉字", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
                NetRequest.requestSearchCity(activity, input, 10, 3000, new NetRequest.SearchCityCallback() {
                    @Override
                    public void onSuccess(List<GeoBean.LocationBean> cities) {
                        searchResultList.setAdapter(new SearchResultListAdapter(activity, cities));
                    }

                    @Override
                    public void notFindCity() {
                        Toast.makeText(activity, "无搜索结果", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFail(Code code) {
                        Toast.makeText(activity, "搜索失败", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onTimeout() {
                        Toast.makeText(activity, "网络连接超时", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError() {
                        Toast.makeText(activity, "网络连接不畅", Toast.LENGTH_SHORT).show();
                    }
                });
                return true;
            }
            return false;
        });

        searchResultList.setOnItemClickListener((parent, view, position, id) -> {
            ListAdapter adapter = searchResultList.getAdapter();
            if (adapter instanceof SearchResultListAdapter) {
                GeoBean.LocationBean locationBean = ((SearchResultListAdapter) adapter).citiesLocation.get(position);
                Intent intent = hasTheCity(locationBean.getId());
                if (intent == null) {
                    NetRequest.requestCityWeather(activity, locationBean, 3000, cityWeatherCallback);
                } else {
                    startActivity(intent);
                }
            }
        });
    }

    private void addHotCity() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        float oneDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.0f, dm);
        int nineDp = (int) (9 * oneDp);
        int itemWidth = (int) ((dm.widthPixels - 10 * oneDp - 8 * nineDp) / 4.0f);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(itemWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(nineDp, nineDp, nineDp, nineDp);
        hotCityLayout.removeAllViews();
        View item;
        for (int i = 0; i < hotCityViews.length; i++) {
            item = View.inflate(activity, R.layout.item_search_hot_city, null);
            item.setLayoutParams(params);
            TextView hotCityName = item.findViewById(R.id.hotCityName);
            hotCityName.setText(hotCities[i].cityName);
            item.setBackground(Tools.theme.getHotCityBackground());
            hotCityName.setTextColor(Tools.theme.getTextColor());
            final int position = i;
            hotCityName.setOnClickListener((v) -> {
                HotCity hotCity = hotCities[position];
                Intent intent = hasTheCity(hotCity.cityId);
                if (intent == null) {
                    NetRequest.requestCityWeather(activity, hotCity.cityId, 3000, cityWeatherCallback);
                } else {
                    startActivity(intent);
                }
            });
            hotCityLayout.addView(item);
        }
    }

    /**
     * 根据城市ID来判断已有数据中是否含有该城市.
     *
     * @param cityId 城市ID
     * @return 若已有该城市则返回一个将使用新栈启动主页的意图并
     *         包含目标城市ID在已有数据中的索引；否则返回null
     */
    private Intent hasTheCity(String cityId) {
        for (int i = 0; i < Tools.cities.size(); i++) {
            if (Tools.cities.get(i).getId().equals(cityId)) {
                return getRestartIntent(i);
            }
        }
        return null;
    }

    /**
     * 获取一个将使用新栈启动主页的意图并跳转至主页指定的页面.
     *
     * @param homePagerIndex 预跳转的主页的指定位置的索引
     * @return 按条件生成的意图
     */
    private Intent getRestartIntent(int homePagerIndex) {
        Intent toHome = new Intent(activity, HomePagerActivity.class);
        toHome.putExtra(Tools.currentPagerKey, homePagerIndex);
        toHome.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        toHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return toHome;
    }

    private void requestLocationData(double longitude, double latitude) {
        NetRequest.requestSearchCity(activity, longitude, latitude, 3000, new NetRequest.SearchCityCallback() {
            @Override
            public void onSuccess(List<GeoBean.LocationBean> cities) {
                locationRunning = false;
                locationSuccess = true;
                cityLocationData = cities.get(0);
                Tools.nowLocationCityId = cityLocationData.getId();
                Tools.saveSharedPreferences(activity, Tools.weatherCfg, Tools.locationIdKey, Tools.nowLocationCityId);
                locationText.setText(cityLocationData.getName());
            }

            @Override
            public void notFindCity() {
                onFail(null);
            }

            @Override
            public void onFail(Code code) {
                locationRunning = false;
                locationText.setText("定位失败");
            }

            @Override
            public void onTimeout() {
                onFail(null);
            }

            @Override
            public void onError() {
                onFail(null);
            }
        });
    }

    private static class HotCity {
        private final String cityId;
        private final String cityName;

        private HotCity(String cityId, String cityName) {
            this.cityId = cityId;
            this.cityName = cityName;
        }

        @NonNull
        @Override
        public String toString() {
            return "HotCity{" +
                    "cityId='" + cityId + '\'' +
                    ", cityName='" + cityName + '\'' +
                    '}';
        }
    }

    private static class SearchResultListAdapter extends BaseAdapter {
        private final Context context;
        private final List<GeoBean.LocationBean> citiesLocation;

        private SearchResultListAdapter(Context context, List<GeoBean.LocationBean> citiesLocation) {
            this.context = context;
            this.citiesLocation = citiesLocation;
        }

        @Override
        public int getCount() {
            return citiesLocation.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = View.inflate(context, R.layout.item_list_search_result, null);
                viewHolder = new ViewHolder();
                viewHolder.cityName = convertView.findViewById(R.id.cityName);
                viewHolder.location = convertView.findViewById(R.id.location);
                viewHolder.cityName.setTextColor(Tools.theme.getTextColor());
                viewHolder.location.setTextColor(Tools.theme.getTextColor());
                convertView.findViewById(R.id.line).setBackgroundColor(Tools.theme.getIgnoreColor());
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            GeoBean.LocationBean locationBean = citiesLocation.get(position);
            viewHolder.cityName.setText(locationBean.getName());
            viewHolder.location.setText(locationBean.getCountry() + "-" + locationBean.getAdm1()
                    + "-" + locationBean.getAdm2());
            return convertView;
        }

        private static class ViewHolder {
            private TextView cityName;
            private TextView location;
        }
    }
}
