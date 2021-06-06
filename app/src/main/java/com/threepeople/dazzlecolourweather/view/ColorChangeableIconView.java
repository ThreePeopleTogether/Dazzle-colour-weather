package com.threepeople.dazzlecolourweather.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.threepeople.dazzlecolourweather.R;

/**
 * 可变色的图标展示View.<br>
 * 间接继承于{@link android.widget.ImageView}，图标设置方法与其相同.<br><br>
 *
 * <p><b>可设置的属性：</b></p>
 *       <p><b>属性名</b></p>  <b>值类型</b>    <b>说明</b>    <b>默认值</b>  <br><br>
 *         <p>baseColor</p>       颜色值      图标原来的颜色   #FFFFFFFF      <br><br>
 *         <p>goalColor</p>       颜色值       图标的新颜色    #FFFFFFFF      <br><br>
 *
 * <p><b><font color="#FF0000">注意：</font></b></p>
 *     1. 请确保baseColor与原来的图标颜色相同，否则可能失去预想的效果<br>
 *     2. 原始图标请勿为透明，否则亦可能失去预想的效果<br>
 *
 * @see #setBaseColor(int)
 * @see #setGoalColor(int)
 * @see #setBaseAndGoalColor(int, int)
 */
public class ColorChangeableIconView extends AppCompatImageView {
    private final ColorMatrix colorMatrix;
    private ColorMatrixColorFilter colorFilter;

    private ColorInfo baseColor;
    private ColorInfo goalColor;

    public ColorChangeableIconView(@NonNull Context context) {
        this(context, null);
    }

    public ColorChangeableIconView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorChangeableIconView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        colorMatrix = new ColorMatrix();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ColorChangeableIconView);
        try {
            baseColor = new ColorInfo(typedArray.getColor(R.styleable.ColorChangeableIconView_baseColor, Color.WHITE));
            goalColor = new ColorInfo(typedArray.getColor(R.styleable.ColorChangeableIconView_goalColor, Color.WHITE));
        } finally {
            typedArray.recycle();
        }
        initColorFilter();
    }

    /**
     * 设置图标原始的颜色.<br>
     * 设置后将重新绘制.
     *
     * @param baseColor 图标的原始颜色
     * @see #setGoalColor(int)
     * @see #setBaseAndGoalColor(int, int)
     */
    public void setBaseColor(int baseColor) {
        this.baseColor = new ColorInfo(baseColor);
        initColorFilter();
        refresh();
    }

    /**
     * 设置图标的新颜色.<br>
     * 设置后将重绘.
     *
     * @param goalColor 图标的新颜色
     * @see #setBaseColor(int)
     * @see #setBaseAndGoalColor(int, int)
     */
    public void setGoalColor(int goalColor) {
        this.goalColor = new ColorInfo(goalColor);
        initColorFilter();
        refresh();
    }

    /**
     * 同时设置图标的原始颜色和新颜色.<br>
     * 设置后将重绘.
     *
     * @param baseColor 图标的原始颜色
     * @param goalColor 图标的新颜色
     * @see #setBaseColor(int)
     * @see #setGoalColor(int)
     */
    public void setBaseAndGoalColor(int baseColor, int goalColor) {
        this.baseColor = new ColorInfo(baseColor);
        this.goalColor = new ColorInfo(goalColor);
        initColorFilter();
        refresh();
    }

    private void initColorFilter() {
        colorMatrix.set(new float[] {
                1.0f, 0.0f, 0.0f, 0.0f, goalColor.r - baseColor.r,
                0.0f, 1.0f, 0.0f, 0.0f, goalColor.g - baseColor.g,
                0.0f, 0.0f, 1.0f, 0.0f, goalColor.b - baseColor.b,
                0.0f, 0.0f, 0.0f, 1.0f, goalColor.a - baseColor.a
        });
        colorFilter = new ColorMatrixColorFilter(colorMatrix);
    }

    private void refresh() {
        invalidate();
        requestLayout();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();
        if (drawable == null && (drawable = getBackground()) == null) {
            super.onDraw(canvas);
            return;
        }
        drawable.setColorFilter(colorFilter);
        setImageDrawable(drawable);
        setBackground(null);
        super.onDraw(canvas);
    }

    private static class ColorInfo {
        private final int a;
        private final int r;
        private final int g;
        private final int b;

        private ColorInfo(int color) {
            a = color >> 24 & 0xFF;
            r = (color & 0x00FF0000) >> 16;
            g = (color & 0x0000FF00) >> 8;
            b = color & 0x000000FF;
        }

        @NonNull
        @Override
        public String toString() {
            String aHex = Integer.toHexString(a);
            String rHex = Integer.toHexString(r);
            String gHex = Integer.toHexString(g);
            String bHex = Integer.toHexString(b);
            return '#' + (a < 16 ? '0' + aHex : aHex) + (r < 16 ? '0' + rHex : rHex)
                    + (g < 16 ? '0' + gHex : gHex) + (b < 16 ? '0' + bHex : bHex)
                    + '(' + a + ", " + r + ", " + g + ", " + b + ')';
        }
    }
}
