package com.threepeople.dazzlecolourweather.theme;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.TypedValue;

/**
 * 黑色UI主题.
 */
public class BlackUITheme implements UITheme {
    private final float oneDp;
    private final int iconColor;
    private final int textColor;
    private final int stressColor;
    private final int ignoreColor;
    private final int themeColor;
    private final int darkColor;
    private final int lightColor;
    private Drawable background = null;
    private Drawable lifeBackground = null;
    private Drawable returnBackground = null;
    private Drawable addCityButtonBackground = null;
    private Drawable inputCityBackground = null;
    private Drawable inputCursorBackground = null;

    public BlackUITheme(Context context) {
        oneDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.0f, context.getResources().getDisplayMetrics());
        iconColor   = Color.rgb(242, 242, 250);// #F2F2FA
        textColor   = Color.rgb(242, 242, 250);// #F2F2FA
        stressColor = Color.rgb(127, 209, 248);// #7FD1F8
        ignoreColor = Color.rgb(207, 216, 220);// #CFD8DC
        themeColor  = Color.rgb(250, 250, 250);// #FFFFFF
        darkColor   = Color.rgb(35, 32, 49);// #232031
        lightColor  = Color.rgb(69, 64, 91);// #45405B
    }

    @Override
    public Drawable getBackground() {
        if (background == null) {
            background = new ColorDrawable(Color.rgb(35, 32, 49));
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
        return false;
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
        if (lifeBackground == null) {
            lifeBackground = XMLDrawable.getCircleCornerShape(Color.BLACK, 15 * oneDp);
        }
        return lifeBackground;
    }

    @Override
    public Drawable getTopButtonBackground() {
        if (returnBackground == null) {
            returnBackground = XMLDrawable.getCircleCornerSelector(android.R.attr.state_pressed,
                    lightColor, darkColor, 15 * oneDp);
        }
        return returnBackground;
    }

    @Override
    public Drawable getAdminCityListBackground() {
        return XMLDrawable.getCircleCornerSelector(android.R.attr.state_pressed,
                lightColor, darkColor, 15 * oneDp);
    }

    @Override
    public Drawable getAddCityButtonBackground() {
        if (addCityButtonBackground == null) {
            addCityButtonBackground = XMLDrawable.getCircleCornerSelector(android.R.attr.state_pressed,
                    darkColor, lightColor, 23 * oneDp);
        }
        return addCityButtonBackground;
    }

    @Override
    public Drawable getInputCityBackground() {
        if (inputCityBackground == null) {
            inputCityBackground = XMLDrawable.getCircleCornerShape(Color.rgb(117, 117, 117), 15 * oneDp);
        }
        return inputCityBackground;
    }

    @Override
    public Drawable getInputCursorBackground() {
        if (inputCursorBackground == null) {
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
                darkColor, lightColor, 15 * oneDp);
    }
}
