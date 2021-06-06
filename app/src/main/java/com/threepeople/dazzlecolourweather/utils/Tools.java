package com.threepeople.dazzlecolourweather.utils;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qweather.sdk.view.HeConfig;
import com.threepeople.dazzlecolourweather.R;
import com.threepeople.dazzlecolourweather.model.CityWeather;
import com.threepeople.dazzlecolourweather.theme.BlackUITheme;
import com.threepeople.dazzlecolourweather.theme.ColorfulDayUITheme;
import com.threepeople.dazzlecolourweather.theme.ColorfulNightUITheme;
import com.threepeople.dazzlecolourweather.theme.CustomDayUITheme;
import com.threepeople.dazzlecolourweather.theme.CustomNightUITheme;
import com.threepeople.dazzlecolourweather.theme.UITheme;
import com.threepeople.dazzlecolourweather.theme.WhiteUITheme;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Tools {
    /**
     * UI主题.
     */
    public static UITheme theme = null;

    public static CustomDayUITheme customDayUITheme = null;

    public static CustomNightUITheme customNightUITheme = null;

    /**
     * 城市天气信息.
     */
    public static List<CityWeather> cities = null;

    /**
     * 当前位置的城市id.
     */
    public static String nowLocationCityId = null;

    /**
     * 天气数据缓存文件.
     */
    public static File weatherDataCacheFile = null;

    /**
     * 通用计时器.
     */
    public static final ScheduledExecutorService scheduledThreadPool;

    // 缓存文件/文件夹名
    /**
     * 天气数据缓存文件名.
     */
    public static final String weatherDataCache;

    /**
     * 天气的简单配置文件.
     */
    public static final String weatherCfg;

    // 键名
    /**
     * 定位城市Id存储的键.
     */
    public static final String locationIdKey;

    /**
     * 存储或传递ViewPager当前页索引的键.
     */
    public static final String currentPagerKey;

    /**
     * 传递定位信息的键.
     */
    public static final String locationDataKey;

    /**
     * 传递城市id的键.
     */
    public static final String cityIdKey;

    /**
     * 传递是否有删除城市的操作的键.
     */
    public static final String hasDeleteKey;

    /**
     * 传递是否有变更主题的键.
     */
    public static final String hasChangeThemeKey;

    /**
     * 传递是否有编辑自定义主题的键.
     */
    public static final String hasEditCustomKey;

    /**
     * 传递图片Uri的键.
     */
    public static final String picUriKey;

    /**
     * 传递图片裁剪结果的键.
     */
    public static final String cutResultKey;

    static {
        scheduledThreadPool = Executors.newScheduledThreadPool(1);
        weatherDataCache = "WeatherDataCache";
        weatherCfg = "WeatherCfg";
        locationIdKey = "locationId";
        currentPagerKey = "currentPager";
        locationDataKey = "locationData";
        cityIdKey = "cityId";
        hasDeleteKey = "hasDelete";
        hasChangeThemeKey = "hasChangeTheme";
        hasEditCustomKey = "hasEditCustom";
        picUriKey = "picUri";
        cutResultKey = "cutResult";
    }

    // 类私有成员
    private static final MyLog myLog;
    private static final Random random;
    private static final Gson gson;
    private static ScheduledFuture<?> timeTask;// 获取网络时间的任务
    private static long netTime;// 成功获取时的网络时间
    private static long baseTime;// 成功获取时的系统开机时间
    private static boolean isNetTime;// 标记是否为网络时间
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH", Locale.CHINA);
    private static final String themeKey;// 保存主题的键
    private static final String customUIDirectory;// 保存自定义主题的缓存目录
    private static final String customDayPicNameKey;// 保存自定义白天主题的背景图片名
    private static final String customNightPicNameKey;// 保存自定义黑夜主题的背景图片名
    private static final String customDayFilename;// 保存自定义白天主题的文件名
    private static final String customNightFilename;// 保存自定义黑夜主题的文件名

    static {
        myLog = new MyLog("ToolsTAG");
        random = new Random();
        gson = new Gson();
        timeTask = null;
        isNetTime = false;
        themeKey = "theme";
        customUIDirectory = "custom_ui_theme";
        customDayPicNameKey = "dayPic";
        customNightPicNameKey = "nightPic";
        customDayFilename = "day";
        customNightFilename = "night";
    }

    public static long getNowTime() {
        if (isNetTime) {
            return SystemClock.elapsedRealtime() - baseTime + netTime;
        } else {
            return System.currentTimeMillis();
        }
    }

    /**
     * 根据日期获取星期数.
     *
     * @param date 日期，格式为：yyyy-MM-dd
     * @return 若正常执行则返回日期所对应的星期数，否则返回当前时间对应的星期数.
     */
    public static String getWeek(String date) {
        String[] YMD = date.split("-");
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.set(Integer.parseInt(YMD[0]), Integer.parseInt(YMD[1]) - 1, Integer.parseInt(YMD[2]));
        } catch (Exception e) {
            // ignore
        }
        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY:
                return "星期日";
            case Calendar.MONDAY:
                return "星期一";
            case Calendar.TUESDAY:
                return "星期二";
            case Calendar.WEDNESDAY:
                return "星期三";
            case Calendar.THURSDAY:
                return "星期四";
            case Calendar.FRIDAY:
                return "星期五";
            case Calendar.SATURDAY:
                return "星期六";
            default:
                return "星期？";
        }
    }

    /**
     * 获取随机的请求码.
     *
     * @return 随机的一个整型数
     */
    public static int getRequestCode() {
        return random.nextInt(32768) + 128;
    }

    /**
     * 设置状态栏字体为黑色.
     *
     * @param activity 需要设置的Activity
     */
    public static void setBlackWordOnStatus(AppCompatActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    public static void clearStatusTextColor(AppCompatActivity activity) {
        activity.getWindow().getDecorView().setSystemUiVisibility(0);
    }

    /**
     * 展示一个带有确定和取消按钮的提示弹窗.
     *
     * @param context 上下文
     * @param message 提示信息
     * @param controlDialog 控制响应弹窗事件的接口
     */
    public static void showDialogWithTwoButton(Context context, String message, ControlDialog controlDialog) {
        showDialogWithTwoButton(context, message, theme, controlDialog);
    }

    /**
     * 展示一个带有确定和取消按钮的提示弹窗.
     *
     * @param context 上下文
     * @param message 提示信息
     * @param controlDialog 控制响应弹窗事件的接口
     */
    public static void showDialogWithTwoButton(Context context, String message, UITheme theme, ControlDialog controlDialog) {
        View dialogView = View.inflate(context, R.layout.dialog_tip_with_two_button, null);
        AlertDialog dialog = new AlertDialog.Builder(context, R.style.CircleCornerAlertDialog)
                .setView(dialogView)
                .create();
        View dialogLayout = dialogView.findViewById(R.id.dialogLayout);
        dialogLayout.setBackgroundColor(theme.getThemeColor());
        TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);
        dialogMessage.setText(message);
        dialogView.findViewById(R.id.dialogLine).setBackgroundColor(theme.getIgnoreColor());
        dialogView.findViewById(R.id.dialogLine2).setBackgroundColor(theme.getIgnoreColor());
        Button cancel = dialogView.findViewById(R.id.dialogCancel);
        Button ok = dialogView.findViewById(R.id.dialogOk);
        if (!(theme instanceof BlackUITheme)) {
            ((TextView) dialogView.findViewById(R.id.dialogTitle)).setTextColor(theme.getTextColor());
            dialogMessage.setTextColor(theme.getTextColor());
            cancel.setTextColor(theme.getTextColor());
            ok.setTextColor(theme.getTextColor());
        }
        dialog.show();
        controlDialog.onDialog(dialog, dialogView);
        cancel.setOnClickListener(v -> controlDialog.onCancel(dialog));
        ok.setOnClickListener(v -> controlDialog.onOk(dialog));
    }

    /**
     * 通过定位服务获取位置信息.
     *
     * @param activity 上下文
     * @param callback 定位回调
     */
    public static void getLocation(AppCompatActivity activity, LocationCallback callback) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            callback.lackPermission(activity);
            return;
        }
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Criteria criteria = new Criteria();
            criteria.setCostAllowed(false);
            criteria.setBearingRequired(false);
            criteria.setAltitudeRequired(false);
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            criteria.setPowerRequirement(Criteria.POWER_LOW);
            String provider = locationManager.getBestProvider(criteria, true);
            if (provider == null) {
                callback.onFail(activity);
                return;
            }
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                callback.onSuccess(location.getLongitude(), location.getLatitude());
            } else {
                myLog.v("获取定位信息为空");
                locationManager.requestLocationUpdates(provider, 0, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        myLog.v("位置更新");
                        callback.onSuccess(location.getLongitude(), location.getLatitude());
                        locationManager.removeUpdates(this);
                    }

                    @Override
                    public void onProviderDisabled(@NonNull String provider) {
                        callback.onFail(activity);
                    }
                });
            }
        } else {
            callback.lackProvider();
        }
    }

    public static Drawable getWeatherIcon(Context context, String iconCode) {
        if (!iconCode.equals("999")) {
            try {
                InputStream stream = context.getAssets().open("weather_icon/pic_weather_" + iconCode + ".png");
                return Drawable.createFromStream(stream, null);
            } catch (IOException e) {
                // ignore
            }
        }
        return ContextCompat.getDrawable(context, R.drawable.pic_weather_999);
    }

    /**
     * 简单判断是否是夜间.
     */
    public static boolean isNightTime() {
        try {
            int nowHour = Integer.parseInt(dateFormat.format(getNowTime()));
            return nowHour < 6 || nowHour >= 18;
        } catch (Exception e) {
            return false;
        }
    }

    public static void saveUIThemeTypeIfNotCustom(Context context) {
        if (theme instanceof ColorfulDayUITheme || theme instanceof ColorfulNightUITheme) {
            saveSharedPreferences(context, weatherCfg, themeKey, "Colorful");
        } else if (theme instanceof WhiteUITheme) {
            saveSharedPreferences(context, weatherCfg, themeKey, "White");
        } else if (theme instanceof BlackUITheme) {
            saveSharedPreferences(context, weatherCfg, themeKey, "Black");
        } else if (theme instanceof CustomDayUITheme || theme instanceof CustomNightUITheme) {
            saveSharedPreferences(context, weatherCfg, themeKey, "Custom");
        }
    }

    public static void saveCustomUITheme(Context context) {
        if (customDayUITheme == null || customNightUITheme == null) {
            return;
        }
        saveSharedPreferences(context, weatherCfg, themeKey, "Custom");
        StringBuilder json;
        // 保存白天主题
        json = new StringBuilder("{");
        json.append("\"iconColor\":\"").append(customDayUITheme.getIconColor()).append("\",");
        json.append("\"textColor\":\"").append(customDayUITheme.getTextColor()).append("\",");
        json.append("\"stressColor\":\"").append(customDayUITheme.getStressColor()).append("\",");
        json.append("\"ignoreColor\":\"").append(customDayUITheme.getIgnoreColor()).append("\",");
        json.append("\"themeColor\":\"").append(customDayUITheme.getThemeColor()).append("\",");
        json.append("\"useBlackOnStatusBar\":\"").append(customDayUITheme.useBlackOnStatusBar()).append('"');
        if (customDayUITheme.useBackgroundColor()) {
            json.append(",\"useBackgroundColor\":\"").append(true).append("\",");
            json.append("\"backgroundColor\":\"").append(customDayUITheme.getBackgroundColor()).append('"');
        } else {
            File file = savePicture(context, customDayUITheme.getBackground());
            if (file != null) {
                SharedPreferences sp = context.getSharedPreferences(weatherCfg, Context.MODE_PRIVATE);
                String srcFilename = sp.getString(customDayPicNameKey, null);
                if (srcFilename != null) {
                    deleteFile(generateFileAtFiles(context, customUIDirectory, srcFilename, true));
                }
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(customDayPicNameKey, file.getName());
                editor.apply();
            }
        }
        json.append('}');
        myLog.v("白天主题配置 ==> " + json);
        saveFile(generateFileAtFiles(context, customUIDirectory, customDayFilename, true), json.toString());
        // 保存黑夜主题
        json = new StringBuilder("{");
        json.append("\"iconColor\":\"").append(customNightUITheme.getIconColor()).append("\",");
        json.append("\"textColor\":\"").append(customNightUITheme.getTextColor()).append("\",");
        json.append("\"stressColor\":\"").append(customNightUITheme.getStressColor()).append("\",");
        json.append("\"ignoreColor\":\"").append(customNightUITheme.getIgnoreColor()).append("\",");
        json.append("\"themeColor\":\"").append(customNightUITheme.getThemeColor()).append("\",");
        json.append("\"useBlackOnStatusBar\":\"").append(customNightUITheme.useBlackOnStatusBar()).append('"');
        if (customNightUITheme.useBackgroundColor()) {
            json.append(",\"useBackgroundColor\":\"").append(true).append("\",");
            json.append("\"backgroundColor\":\"").append(customNightUITheme.getBackgroundColor()).append('"');
        } else {
            File file = savePicture(context, customNightUITheme.getBackground());
            if (file != null) {
                SharedPreferences sp = context.getSharedPreferences(weatherCfg, Context.MODE_PRIVATE);
                String srcFilename = sp.getString(customNightPicNameKey, null);
                if (srcFilename != null) {
                    deleteFile(generateFileAtFiles(context, customUIDirectory, srcFilename, true));
                }
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(customNightPicNameKey, file.getName());
                editor.apply();
            }
        }
        json.append('}');
        myLog.v("黑夜主题配置 ==> " + json);
        saveFile(generateFileAtFiles(context, customUIDirectory, customNightFilename, true), json.toString());
    }

    /**
     * 读取或初始化自定义主题.
     *
     * @param context 上下文
     * @return 若两种自定义主题均由缓存读取，则返回true；否则返回false。<br>
     *         不论返回值如何，两种自定义主题一定不为null.
     */
    public static boolean readCustomTheme(Context context) {
        boolean readDay = true;
        boolean readNight = true;
        SharedPreferences sp = null;
        if (customDayUITheme == null) {
            File day = generateFileAtFiles(context, customUIDirectory, customDayFilename, true);
            String dayJson = readFile(day);
            customDayUITheme = gson.fromJson(dayJson, CustomDayUITheme.class);
            if (customDayUITheme == null) {
                readDay = false;
                customDayUITheme = new CustomDayUITheme(context);
            } else {
                customDayUITheme.initAfterRead(context);
                readDayPic:
                if (!customDayUITheme.useBackgroundColor()) {
                    sp = context.getSharedPreferences(weatherCfg, Context.MODE_PRIVATE);
                    String picName = sp.getString(customDayPicNameKey, null);
                    if (picName != null) {
                        File dayPic = generateFileAtFiles(context, customUIDirectory, picName, true);
                        try (FileInputStream fis = new FileInputStream(dayPic)) {
                            Drawable pic = Drawable.createFromStream(fis, null);
                            if (pic != null) {
                                customDayUITheme.setBackground(pic);
                                break readDayPic;
                            }
                        } catch (IOException e) {
                            // ignore
                        }
                    }
                    customDayUITheme.setBackgroundColor(customDayUITheme.getThemeColor());
                }
            }
        }
        if (customNightUITheme == null) {
            File night = generateFileAtFiles(context, customUIDirectory, customNightFilename, true);
            String nightJson = readFile(night);
            customNightUITheme = gson.fromJson(nightJson, CustomNightUITheme.class);
            if (customNightUITheme == null) {
                readNight = false;
                customNightUITheme = new CustomNightUITheme(context);
            } else {
                customNightUITheme.initAfterRead(context);
                readNightPic:
                if (!customNightUITheme.useBackgroundColor()) {
                    if (sp == null) {
                        sp = context.getSharedPreferences(weatherCfg, Context.MODE_PRIVATE);
                    }
                    String picName = sp.getString(customNightPicNameKey, null);
                    if (picName != null) {
                        File nightPic = generateFileAtFiles(context, customUIDirectory, picName, true);
                        try (FileInputStream fis = new FileInputStream(nightPic)) {
                            Drawable pic = Drawable.createFromStream(fis, null);
                            if (pic != null) {
                                customNightUITheme.setBackground(pic);
                                break readNightPic;
                            }
                        } catch (IOException e) {
                            // ignore
                        }
                    }
                    customNightUITheme.setBackgroundColor(customNightUITheme.getThemeColor());
                }
            }
        }
        return readDay && readNight;
    }

    //=======================初始化=========================
    /**
     * 在应用启动时的初始化工作.<br>
     * <font color="#FF0000">全局执行一次</font>.
     *
     * @param activity 上下文
     */
    public static void init(AppCompatActivity activity) {
        timeTask = scheduledThreadPool.scheduleAtFixedRate(() -> {
            try {
                URL url = new URL("https://m.baidu.com");
                URLConnection uc = url.openConnection();
                uc.connect();
                uc.setReadTimeout(1000);
                uc.setConnectTimeout(1000);
                netTime = uc.getDate();
                baseTime = SystemClock.elapsedRealtime();
                isNetTime = true;
                myLog.v("取得网络时间");
                cancelTimeTask();
            } catch (Exception e) {
                myLog.v("正在获取网络时间...");
            }
        }, 0, 3, TimeUnit.SECONDS);
        HeConfig.init(activity.getString(R.string.PublicId), activity.getString(R.string.PrivateKey));
        HeConfig.switchToDevService();
        initTheme(activity.getApplicationContext());
        weatherDataCacheFile = Tools.generateFileAtFiles(activity, null, Tools.weatherDataCache, false);
    }

    private static void cancelTimeTask() {
        if (timeTask != null) {
            timeTask.cancel(true);
            timeTask = null;
        }
    }

    /**
     * 初始化UI主题.
     */
    private static void initTheme(Context context) {
        SharedPreferences read = context.getSharedPreferences(weatherCfg, Context.MODE_PRIVATE);
        switch (read.getString(themeKey, "Colorful")) {
            case "White":
                theme = new WhiteUITheme(context);
                break;
            case "Black":
                theme = new BlackUITheme(context);
                break;
            case "Custom":
                if (readCustomTheme(context)) {
                    theme = isNightTime() ? customNightUITheme : customDayUITheme;
                    break;
                }
            case "Colorful":
            default:
                theme = isNightTime() ? new ColorfulNightUITheme(context) : new ColorfulDayUITheme(context);
                break;
        }
    }

    /**
     * 初始化应用所需敏感权限.<br>
     * <font color="#FF0000">仅在应用启动时执行一次</font>.
     *
     * @param activity 上下文
     * @param permissionCode 请求敏感权限的请求码
     */
    public static void initPermission(AppCompatActivity activity, int permissionCode) {
        String[] permissions = new String[] {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };// 为确保触发权限请求回调，不过滤已获得的权限
        ActivityCompat.requestPermissions(activity, permissions, permissionCode);
    }

    //========================文件==========================
    /**
     * 简单配置的存储.
     * 存储路径为 /data/user/0/包名/shared_prefs/ 下
     *
     * @param context 上下文
     * @param fileName 配置的文件名
     * @param keyAndValue 以key，value，key，value，...这种方式的存储值序列。
     *                    若数目为奇数个，将舍掉最后一个
     */
    public static void saveSharedPreferences(Context context, String fileName, String... keyAndValue) {
        if (keyAndValue.length > 0) {
            if (keyAndValue.length % 2 == 1) {
                keyAndValue = Arrays.copyOf(keyAndValue, keyAndValue.length - 1);
            }
            SharedPreferences read = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = read.edit();
            for (int i = 0; i < keyAndValue.length; i += 2) {
                editor.putString(keyAndValue[i], keyAndValue[i + 1]);
            }
            editor.apply();
        }
    }

    /**
     * 移除指定配置文件中指定的配置.
     *
     * @param context 上下文
     * @param fileName 配置的文件名
     * @param keys 需移除的配置的键
     */
    public static void removeSharedPreferencesCfg(Context context, String fileName, String... keys) {
        SharedPreferences read = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = read.edit();
        for (String key : keys ) {
            editor.remove(key);
        }
        editor.apply();
    }

    /**
     * 对较大的数据进行文件存储.
     *
     * @param file 用来存储数据的文件，原文件内容将被覆盖。
     *             若文件不存在，将会创建。
     * @param data 将被存储的数据
     * @return 若文件存储成功，则返回存储数据的文件引用；否则返回null。
     */
    public static File saveFile(@NonNull File file, @NonNull String data) {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            if (!parent.mkdirs()) {
                return null;
            }
        }
        try {
            if (file.exists() || file.createNewFile()) {
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(data.getBytes());
                    return file;
                }
            } else {
                return null;
            }
        } catch (IOException e) {
            myLog.d("文件存储失败", e);
            return null;
        }
    }

    /**
     * 从文件中读取数据.
     *
     * @param file 指定的要读取数据的文件。
     * @return 若读取成功，则返回读取的结果；否则返回null。
     */
    public static String readFile(@NonNull File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            int len;
            byte[] cache = new byte[1024];
            StringBuilder stringBuilder = new StringBuilder();
            while ((len = fis.read(cache)) != -1) {
                stringBuilder.append(new String(cache, 0, len));
            }
            return String.valueOf(stringBuilder);
        } catch (IOException e) {
            myLog.d("文件读取失败", e);
            return null;
        }
    }

    /**
     * 将对象保存到文件.
     *
     * @param file 用来存储数据的文件，原文件内容将被覆盖。
     *             若文件不存在，将会创建。
     * @param object 保存的对象
     * @param <T> 任意对象类型
     * @return 若文件存储成功，则返回存储数据的文件引用；否则返回null。
     */
    public static <T> File saveObjectAtFile(File file, T object) {
        return saveFile(file, gson.toJson(object));
    }

    /**
     * 从文件读取对象.
     *
     * @param file 指定的要读取对象的文件
     * @param tClass 读取的对象类型
     * @param <T> 任意对象类型
     * @return 若读取成功，则返回读取的对象；否则返回null。
     */
    public static <T> T readObjectFromFile(File file, Class<T> tClass) {
        String readData = readFile(file);
        try {
            return gson.fromJson(readData, tClass);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从文件中读取List对象.
     *
     * @param file 指定的要读取对象的文件
     * @param type List的类型，可通过{@link TypeToken}获取
     * @param <E> 集合的元素类型
     * @return 若读取成功，则返回读取的List集合；否则返回null
     */
    public static <E> List<E> readListFromFile(File file, Type type) {
        String data = readFile(file);
        if (data == null) {
            return null;
        }
        try {
            return gson.fromJson(data, type);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取完整的规范路径.
     */
    private static String getIntegralFilePath(String directory, String path) {
        if (!TextUtils.isEmpty(directory)) {
            if (directory.lastIndexOf('/') == directory.length() - 1) {// 若末尾有'/'，则去除
                directory = directory.substring(0, directory.length() - 1);
            }
            if (directory.indexOf('/') == 0) {
                path += directory;
            } else {
                path += ("/" + directory);
            }
        }
        return path;
    }

    /**
     * 通过指定的各个属性生成cache目录下的文件引用.
     *
     * @param directory 存储的文件目录
     * @param filename 存储的文件名
     * @param atExternal true表示保存在外部存储设备；false表示保存在内部存储
     * @return 返回生成的文件引用
     */
    public static File generateFileAtCache(Context context, String directory, String filename, boolean atExternal) {
        String path;
        if (atExternal) {
            path = getExternalCachePath(context);
        } else {
            path = getInternalCachePath(context);
        }
        return new File(getIntegralFilePath(directory, path), filename);
    }

    /**
     * 通过指定的各个属性生成files目录下的文件引用.
     *
     * @param directory 存储的文件目录
     * @param filename 存储的文件名
     * @param atExternal true表示保存在外部存储设备；false表示保存在内部存储
     * @return 返回生成的文件引用
     */
    public static File generateFileAtFiles(Context context, String directory, String filename, boolean atExternal) {
        String path;
        if (atExternal) {
            path = getExternalFilesPath(context);
        } else {
            path = getInternalFilesPath(context);
        }
        return new File(getIntegralFilePath(directory, path), filename);
    }

    //// 内部存储(internal)，路径一般为：
    ////         /data/user/0/包名/...
    /**
     * 获得内部存储中的cache目录路径.
     *
     * @return 返回内部存储中的cache目录路径
     */
    public static String getInternalCachePath(Context context) {
        return context.getCacheDir().getAbsolutePath();
    }

    /**
     * 获得内部存储中的files目录路径.
     *
     * @return 返回内部存储中的files目录路径.
     */
    public static String getInternalFilesPath(Context context) {
        return context.getFilesDir().getAbsolutePath();
    }

    //// 外部存储(external)，路径一般为：
    ////         /storage/emulated/0/Android/data/包名/...
    /**
     * 获得外部存储中的cache目录路径.
     *
     * @return 返回外部存储中的cache目录路径；
     *         若获取为空，则返回内部存储中的cache目录路径
     */
    public static String getExternalCachePath(Context context) {
        File cache = context.getExternalCacheDir();
        if (cache != null) {
            return context.getExternalCacheDir().getAbsolutePath();
        } else {
            return getInternalCachePath(context);
        }
    }

    /**
     * 获得外部存储中的files目录路径.
     *
     * @return 返回外部存储中的files目录路径；
     *         若获取为空，则返回内部存储中的files目录路径
     */
    public static String getExternalFilesPath(Context context) {
        File file = context.getExternalFilesDir("");
        if (file != null) {
            return context.getExternalFilesDir("").getAbsolutePath();
        } else {
            return getInternalFilesPath(context);
        }
    }

    /**
     * 从指定的目录获取File数组.
     *
     * @param directory 指定的目录
     * @param parentPath 指定的根目录
     * @return 若指定的确为目录则返回目录下所有文件的File数组，否则返回null
     * @see #getInternalCachePath(Context)
     * @see #getInternalFilesPath(Context)
     * @see #getExternalCachePath(Context)
     * @see #getExternalFilesPath(Context)
     */
    public static File[] getFilesFromDirectory(String directory, String parentPath) {
        File file = new File(getIntegralFilePath(directory, parentPath));
        if (file.exists() && file.isDirectory()) {
            return file.listFiles();
        } else {
            return null;
        }
    }

    /**
     * 从指定的目录下获取所有文件的文件名.
     *
     * @param directory 指定的目录
     * @param parentPath 指定的根目录
     * @return 若指定的确为目录则返回目录下所有文件的文件名，否则返回null
     * @see #getInternalCachePath(Context)
     * @see #getInternalFilesPath(Context)
     * @see #getExternalCachePath(Context)
     * @see #getExternalFilesPath(Context)
     */
    public static String[] getFilenamesFromDirectory(String directory, String parentPath) {
        File file = new File(getIntegralFilePath(directory, parentPath));
        if (file.exists() && file.isDirectory()) {
            return file.list();
        } else {
            return null;
        }
    }

    /**
     * 删除指定的文件.
     * 若为目录，则目录下的文件也将被删除.
     *
     * @param file 待删除的文件
     */
    public static void deleteFile(File file) {
        try {
            if (file != null && file.exists()) {
                if (file.isDirectory()) {
                    File[] files = file.listFiles();
                    if (files != null) {
                        for (File f : files) {
                            deleteFile(f);
                        }
                    }
                }
                if (!file.delete()) {
                    myLog.v("文件删除失败: " + file.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            myLog.v("文件删除失败", e);
        }
    }

    private static File savePicture(Context context, Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            File file = generateFileAtFiles(context, customUIDirectory, String.valueOf(getNowTime()), true);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                if (!parent.mkdirs()) {
                    return null;
                }
            }
            try {
                if (file.exists() || file.createNewFile()) {
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        ((BitmapDrawable) drawable).getBitmap().compress(Bitmap.CompressFormat.PNG, 100, fos);
                        fos.flush();
                        return file;
                    }
                } else {
                    return null;
                }
            } catch (IOException e) {
                myLog.d("保存图片失败", e);
                return null;
            }
        } else {
            myLog.d("保存的图片不是BitmapDrawable的对象");
            return null;
        }
    }

    //=======================内部类/接口===========================
    /**
     * 定位回调接口.
     */
    public interface LocationCallback {
        /**
         * 成功获取定位时调用.
         *
         * @param longitude 经度
         * @param latitude 纬度
         */
        void onSuccess(double longitude, double latitude);

        /**
         * 因异常情况或主动关闭定位服务导致定位失败时调用.<br>
         * 默认实现为提示“定位失败”.
         *
         * @param context 上下文
         */
        default void onFail(Context context) {
            myLog.v("定位失败");
            Toast.makeText(context, "定位失败", Toast.LENGTH_SHORT).show();
        }

        /**
         * 当没有定位服务的提供者时调用.<br>
         * 通常为GPS未启用.
         */
        void lackProvider();

        /**
         * 当没有定位权限时调用.<br>
         * 默认实现为提示“没有定位权限”.
         *
         * @param context 上下文
         */
        default void lackPermission(Context context) {
            Toast.makeText(context.getApplicationContext(), "没有定位权限", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 弹窗控制接口.
     */
    public interface ControlDialog {
        /**
         * 对弹窗或其布局进行设置.
         *
         * @param dialog 弹窗的引用
         * @param dialogView 弹窗的布局View的引用
         */
        void onDialog(AlertDialog dialog, View dialogView);

        /**
         * 响应确定按钮点击时的操作.
         *
         * @param dialog 弹窗的引用
         */
        void onOk(AlertDialog dialog);

        /**
         * 响应取消按钮点击时的操作.<br>
         * 默认实现为令弹窗消失.
         *
         * @param dialog 弹窗的引用
         */
        default void onCancel(AlertDialog dialog) {
            dialog.dismiss();
        }
    }

    /**
     * 进行大图片的传递.
     */
    public static class BitmapBinder extends Binder {
        private final Bitmap bitmap;

        public BitmapBinder(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }
    }
}
