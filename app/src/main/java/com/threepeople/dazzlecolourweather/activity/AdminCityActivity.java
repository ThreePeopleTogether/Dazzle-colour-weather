package com.threepeople.dazzlecolourweather.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.threepeople.dazzlecolourweather.R;
import com.threepeople.dazzlecolourweather.model.CityWeather;
import com.threepeople.dazzlecolourweather.utils.Tools;
import com.threepeople.dazzlecolourweather.view.ColorChangeableIconView;

import java.util.LinkedList;
import java.util.List;

public class AdminCityActivity extends AppCompatActivity {
    private final AppCompatActivity activity;
    private List<CityWeather> noDeleteCities;
    private final OnClickDeleteIconListener listener;
    private boolean editMode;// 标记当前是否处于编辑模式
    private boolean hasDelete;// 标记是否有删除城市

    private ViewGroup adminCityLayout;
    private ViewGroup returnHomeLayout;
    private ColorChangeableIconView returnHome;
    private TextView adminCityTitle;
    private TextView editCity;
    private ListView adminCityList;
    private ViewGroup searchCity;
    private ColorChangeableIconView searchCityIcon;
    private TextView searchCityText;

    {
        activity = this;
        noDeleteCities = new LinkedList<>(Tools.cities);
        listener = theCity -> {
            if (noDeleteCities.size() > 1) {
                noDeleteCities.remove(theCity);
                refreshAdminCityList(true);
            } else {
                Toast.makeText(activity, "至少需保留一个城市", Toast.LENGTH_SHORT).show();
            }
        };
        editMode = false;
        hasDelete = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_city);
        if (Tools.theme.useBlackOnStatusBar()) {
            Tools.setBlackWordOnStatus(this);
        }

        initView();
        settingTheme();
        myListener();
        refreshAdminCityList(false);
    }

    private void initView() {
        adminCityLayout  = findViewById(R.id.adminCityLayout);
        returnHomeLayout = findViewById(R.id.returnHomeLayout);
        returnHome       = findViewById(R.id.returnHome);
        adminCityTitle   = findViewById(R.id.adminCityTitle);
        editCity         = findViewById(R.id.editCity);
        adminCityList    = findViewById(R.id.adminCityList);
        searchCity       = findViewById(R.id.searchCity);
        searchCityIcon   = findViewById(R.id.searchCityIcon);
        searchCityText   = findViewById(R.id.searchCityText);
    }

    private void settingTheme() {
        adminCityLayout.setBackground(Tools.theme.getBackground());
        returnHomeLayout.setBackground(Tools.theme.getTopButtonBackground());
        returnHome.setGoalColor(Tools.theme.getIconColor());
        adminCityTitle.setTextColor(Tools.theme.getTextColor());
        editCity.setTextColor(Tools.theme.getStressColor());
        searchCity.setBackground(Tools.theme.getAddCityButtonBackground());
        searchCityIcon.setGoalColor(Tools.theme.getIconColor());
        searchCityText.setTextColor(Tools.theme.getTextColor());
    }

    private void myListener() {
        returnHomeLayout.setOnClickListener(v -> onBackPressed());

        editCity.setOnClickListener((v -> {
            if (editMode) {
                editMode = false;
                editCity.setText("编辑");
                hasDelete = Tools.cities.size() != noDeleteCities.size();
                Tools.cities = new LinkedList<>(noDeleteCities);
                Tools.saveObjectAtFile(Tools.weatherDataCacheFile, Tools.cities);
                refreshAdminCityList(false);
            } else {
                editMode = true;
                editCity.setText("完成");
                refreshAdminCityList(true);
            }
        }));

        searchCity.setOnClickListener(v -> {
            if (!editMode) {
                startActivity(new Intent(activity, AddCityActivity.class));
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (editMode) {
            editMode = false;
            editCity.setText("编辑");
            noDeleteCities = new LinkedList<>(Tools.cities);
            refreshAdminCityList(false);
        } else {
            Intent back = new Intent();
            back.putExtra(Tools.hasDeleteKey, hasDelete);
            setResult(RESULT_OK, back);
            super.onBackPressed();
        }
    }

    private void refreshAdminCityList(boolean showDelete) {
        adminCityList.setAdapter(new AdminCityListAdapter(activity, noDeleteCities, showDelete, listener));
    }

    private interface OnClickDeleteIconListener {
        void onClick(CityWeather theCity);
    }

    private static class AdminCityListAdapter extends BaseAdapter {
        private final Context context;
        private final List<CityWeather> noDeleteCities;
        private final boolean showDelete;
        private final OnClickDeleteIconListener listener;

        private AdminCityListAdapter(Context context, List<CityWeather> noDeleteCities, boolean showDelete, @NonNull OnClickDeleteIconListener listener) {
            this.context = context;
            this.noDeleteCities = noDeleteCities;
            this.showDelete = showDelete;
            this.listener = listener;
        }

        @Override
        public int getCount() {
            return noDeleteCities.size();
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
            CityWeather theCity = noDeleteCities.get(position);
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = View.inflate(context, R.layout.item_list_admin_city, null);
                viewHolder = new ViewHolder();
                viewHolder.cityName = convertView.findViewById(R.id.cityName);
                viewHolder.location = convertView.findViewById(R.id.location);
                viewHolder.locationIcon = convertView.findViewById(R.id.locationIcon);
                viewHolder.deleteIcon = convertView.findViewById(R.id.deleteIcon);
                convertView.findViewById(R.id.itemAdminLayout).setBackground(Tools.theme.getAdminCityListBackground());
                viewHolder.cityName.setTextColor(Tools.theme.getTextColor());
                viewHolder.location.setTextColor(Tools.theme.getTextColor());
                viewHolder.locationIcon.setGoalColor(Tools.theme.getStressColor());
                viewHolder.deleteIcon.setGoalColor(Tools.theme.getIgnoreColor());
                viewHolder.deleteIcon.setOnClickListener((v -> listener.onClick(theCity)));
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.cityName.setText(theCity.getCityName());
            viewHolder.location.setText(theCity.getCountry() + "-" + theCity.getAdm1()
                    + "-" + theCity.getAdm2());
            if (theCity.getId().equals(Tools.nowLocationCityId)) {
                viewHolder.locationIcon.setVisibility(View.VISIBLE);
            } else {
                viewHolder.locationIcon.setVisibility(View.GONE);
            }
            if (showDelete) {
                viewHolder.deleteIcon.setVisibility(View.VISIBLE);
            } else {
                viewHolder.deleteIcon.setVisibility(View.GONE);
            }
            return convertView;
        }

        private static class ViewHolder {
            private TextView cityName;
            private TextView location;
            private ColorChangeableIconView locationIcon;
            private ColorChangeableIconView deleteIcon;
        }
    }
}
