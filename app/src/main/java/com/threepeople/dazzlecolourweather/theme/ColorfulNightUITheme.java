package com.threepeople.dazzlecolourweather.theme;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.TypedValue;

import androidx.core.content.ContextCompat;

import com.threepeople.dazzlecolourweather.R;

public class ColorfulNightUITheme implements UITheme {
    private final Context context;
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

    public ColorfulNightUITheme(Context context) {
        this.context = context.getApplicationContext();
        oneDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.0f, context.getResources().getDisplayMetrics());
        iconColor   = Color.WHITE;
        textColor   = Color.WHITE;
        stressColor = Color.rgb(157, 101, 255);// #9D65FF
        ignoreColor = Color.rgb(241, 237, 254);// #F1EDFE
        themeColor  = Color.rgb(232, 181, 208);// #E8B5D0
        darkColor   = Color.rgb(177, 100, 141);// #B1648D
        lightColor  = Color.rgb(203, 111, 160);// #CB6FA0
    }

    @Override
    public Drawable getBackground() {
        if (background == null) {
            background = ContextCompat.getDrawable(context, R.drawable.pic_bg_night);
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
            lifeBackground = XMLDrawable.getCircleCornerShape(Color.rgb(124, 64, 123),
                    15 * oneDp);
        }
        return lifeBackground;
    }

    @Override
    public Drawable getTopButtonBackground() {
        if (returnBackground == null) {
            returnBackground = XMLDrawable.getCircleCornerSelector(android.R.attr.state_pressed,
                    darkColor, lightColor, 15 * oneDp);
        }
        return returnBackground;
    }

    @Override
    public Drawable getAdminCityListBackground() {
        return XMLDrawable.getCircleCornerSelector(android.R.attr.state_pressed,
                darkColor, lightColor, 15 * oneDp);
    }

    @Override
    public Drawable getAddCityButtonBackground() {
        if (addCityButtonBackground == null) {
            addCityButtonBackground = XMLDrawable.getCircleCornerSelector(android.R.attr.state_pressed,
                    Color.rgb(88, 57, 127), Color.rgb(152, 87, 145),
                    23 * oneDp);
        }
        return addCityButtonBackground;
    }

    @Override
    public Drawable getInputCityBackground() {
        if (inputCityBackground == null) {
            inputCityBackground = XMLDrawable.getCircleCornerShape(Color.rgb(160, 91, 148),
                    15 * oneDp);
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
