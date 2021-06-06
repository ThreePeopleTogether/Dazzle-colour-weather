package com.threepeople.dazzlecolourweather.theme;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.TypedValue;

import androidx.annotation.ColorInt;

public class CustomNightUITheme implements UITheme {
    private float oneDp;
    private int iconColor;
    private int textColor;
    private int stressColor;
    private int ignoreColor;
    private int themeColor;
    private int backgroundColor;
    private boolean useBlackOnStatusBar;
    private boolean updateDrawable;
    private boolean useBackgroundColor;
    private Drawable background = null;
    private Drawable lifeBackground = null;
    private Drawable returnBackground = null;
    private Drawable addCityButtonBackground = null;
    private Drawable inputCityBackground = null;
    private Drawable inputCursorBackground = null;

    public CustomNightUITheme(Context context) {
        oneDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.0f, context.getResources().getDisplayMetrics());
        WhiteUITheme theme = new WhiteUITheme(context);
        this.iconColor   = theme.getIconColor();
        this.textColor   = theme.getTextColor();
        this.stressColor = theme.getStressColor();
        this.ignoreColor = theme.getIgnoreColor();
        this.themeColor  = theme.getThemeColor();
        this.useBlackOnStatusBar = true;
        updateDrawable = false;
        useBackgroundColor = true;
        backgroundColor = themeColor;
    }

    public CustomNightUITheme(UITheme theme) {
        if (theme instanceof CustomNightUITheme) {
            CustomNightUITheme that = (CustomNightUITheme) theme;
            background = that.background;
            iconColor = that.iconColor;
            textColor = that.textColor;
            stressColor = that.stressColor;
            ignoreColor = that.ignoreColor;
            themeColor = that.themeColor;
            useBlackOnStatusBar = that.useBlackOnStatusBar;
            useBackgroundColor = that.useBackgroundColor;
            backgroundColor = that.backgroundColor;
        }
    }

    public void initAfterRead(Context context) {
        updateDrawable = false;
        oneDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.0f, context.getResources().getDisplayMetrics());
    }

    public void setIconColor(int iconColor) {
        updateDrawable = true;
        this.iconColor = iconColor;
    }

    public void setTextColor(int textColor) {
        updateDrawable = true;
        this.textColor = textColor;
    }

    public void setStressColor(int stressColor) {
        updateDrawable = true;
        this.stressColor = stressColor;
    }

    public void setIgnoreColor(int ignoreColor) {
        updateDrawable = true;
        this.ignoreColor = ignoreColor;
    }

    public void setThemeColor(int themeColor) {
        updateDrawable = true;
        this.themeColor = themeColor;
    }

    public void setBackground(Drawable background) {
        useBackgroundColor = false;
        this.background = background;
    }

    public void setBackgroundColor(@ColorInt int color) {
        useBackgroundColor = true;
        backgroundColor = color;
        this.background = new ColorDrawable(color);
    }

    public void setUseBlackOnStatusBar(boolean useBlackOnStatusBar) {
        this.useBlackOnStatusBar = useBlackOnStatusBar;
    }

    public boolean useBackgroundColor() {
        return useBackgroundColor;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    @Override
    public Drawable getBackground() {
        if (background == null) {
            background = new ColorDrawable(themeColor);
        }
        return background;
    }

    @Override
    public int getIconColor() {
        return iconColor;
    }

    @Override
    public int getTextColor() {
        return textColor;
    }

    @Override
    public int getStressColor() {
        return stressColor;
    }

    @Override
    public int getIgnoreColor() {
        return ignoreColor;
    }

    @Override
    public int getThemeColor() {
        return themeColor;
    }

    @Override
    public boolean useBlackOnStatusBar() {
        return useBlackOnStatusBar;
    }

    @Override
    public Drawable getPointBackground() {
        GradientDrawable enableTrue = new GradientDrawable();
        enableTrue.setShape(GradientDrawable.OVAL);
        enableTrue.setColor(ignoreColor);
        GradientDrawable enableFalse = new GradientDrawable();
        enableFalse.setShape(GradientDrawable.OVAL);
        enableFalse.setColor(stressColor);
        StateListDrawable selector = new StateListDrawable();
        selector.addState(new int[] {android.R.attr.state_enabled}, enableTrue);
        selector.addState(new int[] {- android.R.attr.state_enabled}, enableFalse);
        return selector;
    }

    @Override
    public Drawable getLifeBackground() {
        if (updateDrawable || lifeBackground == null) {
            lifeBackground = XMLDrawable.getCircleCornerShape(iconColor, 15 * oneDp);
        }
        return lifeBackground;
    }

    @Override
    public Drawable getTopButtonBackground() {
        if (updateDrawable || returnBackground == null) {
            returnBackground = XMLDrawable.getCircleCornerSelector(android.R.attr.state_pressed,
                    ignoreColor, themeColor, 15 * oneDp);
        }
        return returnBackground;
    }

    @Override
    public Drawable getAdminCityListBackground() {
        return XMLDrawable.getCircleCornerSelector(android.R.attr.state_pressed,
                ignoreColor, themeColor, 15 * oneDp);
    }

    @Override
    public Drawable getAddCityButtonBackground() {
        if (updateDrawable || addCityButtonBackground == null) {
            addCityButtonBackground = XMLDrawable.getCircleCornerSelector(android.R.attr.state_pressed,
                    ignoreColor, themeColor, 23 * oneDp);
        }
        return addCityButtonBackground;
    }

    @Override
    public Drawable getInputCityBackground() {
        if (updateDrawable || inputCityBackground == null) {
            inputCityBackground = XMLDrawable.getCircleCornerShape(themeColor, 15 * oneDp);
        }
        return inputCityBackground;
    }

    @Override
    public Drawable getInputCursorBackground() {
        if (updateDrawable || inputCursorBackground == null) {
            GradientDrawable shape = new GradientDrawable();
            shape.setColor(stressColor);
            shape.setSize((int) oneDp, Integer.MAX_VALUE);
            inputCursorBackground = shape;
        }
        return inputCursorBackground;
    }

    @Override
    public Drawable getHotCityBackground() {
        return XMLDrawable.getCircleCornerSelector(android.R.attr.state_pressed,
                ignoreColor, themeColor, 15 * oneDp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CustomNightUITheme that = (CustomNightUITheme) o;
        return iconColor == that.iconColor &&
                textColor == that.textColor &&
                stressColor == that.stressColor &&
                ignoreColor == that.ignoreColor &&
                themeColor == that.themeColor &&
                useBlackOnStatusBar == that.useBlackOnStatusBar &&
                useBackgroundColor == that.useBackgroundColor &&
                useBackgroundColor && backgroundColor == that.backgroundColor;
    }
}
