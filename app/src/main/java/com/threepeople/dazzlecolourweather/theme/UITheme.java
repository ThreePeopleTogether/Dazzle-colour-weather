package com.threepeople.dazzlecolourweather.theme;

import android.graphics.drawable.Drawable;

/**
 * 描述生成包装UI主题的接口.
 */
public interface UITheme {
    /**
     * 获取背景图.
     */
    Drawable getBackground();

    /**
     * 获取图标颜色.
     */
    int getIconColor();

    /**
     * 获取文字颜色.
     */
    int getTextColor();

    /**
     * 获取强调颜色.
     */
    int getStressColor();

    /**
     * 获取衬托颜色.
     */
    int getIgnoreColor();

    /**
     * 获取主题颜色.
     */
    int getThemeColor();

    /**
     * 状态栏字体是否使用黑色字体.
     *
     * @return true表示使用黑色字体，false表示不使用
     */
    boolean useBlackOnStatusBar();

    /**
     * 获取圆点的背景图.
     */
    Drawable getPointBackground();

    /**
     * 获取生活指数的背景图.
     */
    Drawable getLifeBackground();

    /**
     * 获取顶部按钮的背景.
     */
    Drawable getTopButtonBackground();

    /**
     * 获取管理城市的List条目背景.
     */
    Drawable getAdminCityListBackground();

    /**
     * 获取添加城市按钮的背景.
     */
    Drawable getAddCityButtonBackground();

    /**
     * 获取搜索城市的输入框背景.
     */
    Drawable getInputCityBackground();

    /**
     * 获取搜索城市的输入框光标背景.
     */
    Drawable getInputCursorBackground();

    /**
     * 获取热门城市的背景.
     */
    Drawable getHotCityBackground();
}
