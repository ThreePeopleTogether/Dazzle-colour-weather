package com.threepeople.dazzlecolourweather.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.threepeople.dazzlecolourweather.R;

public class RoundAngleLinearLayout extends LinearLayout {
    private final DisplayMetrics dm;
    private final float oneDp;
    private final float scale;
    private final Paint paint;
    private final Path path;
    private final DashPathEffect dashPathEffect;
    private int lineColor;

    private final float cornerRadius;// 圆角半径

    public RoundAngleLinearLayout(Context context) {
        this(context, null);
    }

    public RoundAngleLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        dm = context.getResources().getDisplayMetrics();
        oneDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.0f, dm);
        scale = (float) dm.heightPixels / dm.widthPixels;
        paint = new Paint();
        path = new Path();
        lineColor = Color.WHITE;
        dashPathEffect = new DashPathEffect(new float[] {10, 5}, 0);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundAngleLinearLayout);
        try {
            cornerRadius = typedArray.getDimension(R.styleable.RoundAngleLinearLayout_cornerRadius, 0.0f);
        } finally {
            typedArray.recycle();
        }
        if (cornerRadius > 0) {
            setLayerType(LAYER_TYPE_HARDWARE, null);
        }
    }

    public void setLineColor(@ColorInt int lineColor) {
        this.lineColor = lineColor;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
            int hSize = (int) (MeasureSpec.getSize(widthMeasureSpec) * scale);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(hSize, MeasureSpec.EXACTLY);
        } else if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
            int wSize = (int) (MeasureSpec.getSize(heightMeasureSpec) / scale);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(wSize, MeasureSpec.EXACTLY);
        } else {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(dm.widthPixels, MeasureSpec.AT_MOST);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(dm.heightPixels, MeasureSpec.AT_MOST);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (cornerRadius > 0) {
            path.reset();
            path.addRoundRect(0.0f, 0.0f, getWidth(), getHeight(), cornerRadius, cornerRadius, Path.Direction.CW);
            canvas.save();
            canvas.clipPath(path, Region.Op.DIFFERENCE);
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.SRC_IN);
            canvas.restore();
        }
        float oneDpHalf = oneDp / 2.0f;
        paint.reset();
        paint.setColor(lineColor);
        paint.setStrokeWidth(oneDp);
        paint.setStyle(Paint.Style.STROKE);
        paint.setPathEffect(dashPathEffect);// 虚线
        paint.setAntiAlias(true);
        path.reset();
        path.addRoundRect(oneDpHalf, oneDpHalf, getWidth() - oneDpHalf, getHeight() - oneDpHalf, cornerRadius, cornerRadius, Path.Direction.CW);
        canvas.drawPath(path, paint);
    }
}
