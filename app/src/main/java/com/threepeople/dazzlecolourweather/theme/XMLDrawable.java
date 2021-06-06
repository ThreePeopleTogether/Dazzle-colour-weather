package com.threepeople.dazzlecolourweather.theme;

import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;

public class XMLDrawable {
    /**
     * 获取圆角的shape.
     *
     * @param color 填充颜色
     * @param radius 圆角半径
     * @return 所描述的图片
     */
    public static GradientDrawable getCircleCornerShape(int color, float radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(radius);
        return shape;
    }

    /**
     * 获取圆角的selector.
     *
     * @param event 选择器区分的事件，android.R.attr.state_* 所代表的事件
     * @param eventTrueColor 事件为true时的填充颜色
     * @param eventFalseColor 事件为false时的填充颜色
     * @param radius 圆角半径
     * @return 所描述的图片
     */
    public static StateListDrawable getCircleCornerSelector(int event, int eventTrueColor, int eventFalseColor, float radius) {
        GradientDrawable eventTrue = getCircleCornerShape(eventTrueColor, radius);
        GradientDrawable eventFalse = getCircleCornerShape(eventFalseColor, radius);
        StateListDrawable selector = new StateListDrawable();
        selector.addState(new int[] {event}, eventTrue);
        selector.addState(new int[] {- event}, eventFalse);
        return selector;
    }
}
