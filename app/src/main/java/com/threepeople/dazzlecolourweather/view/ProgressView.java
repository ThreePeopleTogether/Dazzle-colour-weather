package com.threepeople.dazzlecolourweather.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import com.threepeople.dazzlecolourweather.R;

/**
 * 圆形的进度View.
 * <p><b>可设置的属性：</b></p>
 *       <p><b>属性名</b></p>  <b>值类型</b>         <b>说明</b>         <b>默认值</b>  <br><br>
 *       <p>minValue</p>          整型值      进度的最小值                    0         <br><br>
 *       <p>maxValue</p>          整型值      进度的最大值                   100        <br><br>
 *       <p>currentValue</p>      整型值      当前进度的数值（用于预览）       62        <br><br>
 *       <p>numberSize</p>      尺寸值(sp)    当前进度数值的字体大小          62sp       <br><br>
 *       <p>numberColor</p>       颜色值      当前进度数值的字体颜色        #888888      <br><br>
 *       <p>blankColor</p>        颜色值      无进度空白区域的颜色          #888888      <br><br>
 *       <p>progressColor</p>     颜色值      有进度区域的颜色              #7FD1F8      <br><br>
 *       <p>progressWidth</p>   尺寸值(dp)    进度条的粗细                    5dp        <br><br>
 *
 * @see #setMaxValue(int)
 * @see #setMinValue(int)
 * @see #setMaxMinValue(int, int)
 * @see #setCurrentValue(int)
 * @see #setNewValue(int, int, int)
 * @see #setNewColor(Integer, Integer, Integer)
 */
public class ProgressView extends View {
    private final float oneDp;
    private final Paint paint;
    private final Rect rect;
    private final RectF circle;
    private ValueAnimator animator = null;

    // 绘制数据
    private float radius;// 圆弧半径
    private final float progressWHalf;// 进度条粗细的一半
    private float progressScale;// 进度比例
    private float animatorScale;// 动画过程的进度比例
    private int animatorNumber;// 动画过程的进度数值

    // 属性
    private int minValue;
    private int maxValue;
    private int currentValue;
    private final float numberSize;
    private int numberColor;
    private int blankColor;// 无进度空白区域颜色
    private int progressColor;// 有进度区域颜色
    private final float progressWidth;// 进度条的粗细

    public ProgressView(Context context) {
        this(context, null);
    }

    public ProgressView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        oneDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.0f, dm);
        paint = new Paint();
        rect = new Rect();
        circle = new RectF();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ProgressView);
        try {
            minValue = typedArray.getInt(R.styleable.ProgressView_minValue, 0);
            maxValue = typedArray.getInt(R.styleable.ProgressView_maxValue, 100);
            currentValue = typedArray.getInt(R.styleable.ProgressView_currentValue, 62);
            if (minValue > maxValue) {
                throw new ArithmeticException("最小值必须不大于最大值");
            }
            numberSize = typedArray.getDimension(R.styleable.ProgressView_numberSize, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 62.0f, dm));
            numberColor = typedArray.getColor(R.styleable.ProgressView_numberColor, Color.GRAY);
            blankColor = typedArray.getColor(R.styleable.ProgressView_blankColor, Color.GRAY);
            progressColor = typedArray.getColor(R.styleable.ProgressView_progressColor, Color.rgb(127, 209, 248));
            progressWidth = typedArray.getDimension(R.styleable.ProgressView_progressWidth, 5 * oneDp);
        } finally {
            typedArray.recycle();
        }
        initProgressScale();
        progressWHalf = progressWidth / 2.0f;
    }

    /**
     * 设置进度的最大值.
     *
     * @param maxValue 进度的最大值
     */
    public void setMaxValue(int maxValue) {
        if (minValue > maxValue) {
            throw new ArithmeticException("设置的最大值比当前的最小值（" + minValue + "）小");
        }
        this.maxValue = maxValue;
        initProgressScale();
        refresh();
    }

    /**
     * 设置进度的最小值.
     *
     * @param minValue 进度的最小值
     */
    public void setMinValue(int minValue) {
        if (minValue > maxValue) {
            throw new ArithmeticException("设置的最小值比当前的最大值（" + maxValue + "）大");
        }
        this.minValue = minValue;
        initProgressScale();
        refresh();
    }

    /**
     * 同时设置进度的最大值和最小值.
     *
     * @param maxValue 进度的最大值
     * @param minValue 进度的最小值
     */
    public void setMaxMinValue(int maxValue, int minValue) {
        if (minValue > maxValue) {
            throw new ArithmeticException("最小值必须不大于最大值");
        }
        this.maxValue = maxValue;
        this.minValue = minValue;
        initProgressScale();
        refresh();
    }

    /**
     * 设置新的进度数值.
     *
     * @param currentValue 新的进度值
     */
    public void setCurrentValue(int currentValue) {
        this.currentValue = currentValue;
        initProgressScale();
        refresh();
    }

    /**
     * 设置新的进度相关数据.
     *
     * @param maxValue 进度的最大值
     * @param minValue 进度的最小值
     * @param currentValue 当前的进度数值
     */
    public void setNewValue(int maxValue, int minValue, int currentValue) {
        if (minValue > maxValue) {
            throw new ArithmeticException("最小值必须不大于最大值");
        }
        this.maxValue = maxValue;
        this.minValue = minValue;
        this.currentValue = currentValue;
        initProgressScale();
        refresh();
    }

    /**
     * 设置新的颜色样式.
     *
     * @param blankColor 无进度空白区域的颜色，若为null则不设置
     * @param progressColor 有进度区域的颜色，若为null则不设置
     * @param numberColor 进度提示数字的颜色，若为null则不设置
     */
    public void setNewColor(Integer blankColor, Integer progressColor, Integer numberColor) {
        if (blankColor != null) {
            this.blankColor = blankColor;
        }
        if (progressColor != null) {
            this.progressColor = progressColor;
        }
        if (numberColor != null) {
            this.numberColor = numberColor;
        }
        refresh();
    }

    /**
     * 启动动画.
     */
    public void startAnimator() {
        if (animator == null) {
            animator = ValueAnimator.ofFloat(0.0f, 1.0f);
            animator.setDuration(800);
            animator.setInterpolator(new LinearInterpolator());
            animator.setRepeatCount(0);
            animator.addUpdateListener(animation -> {
                if (currentValue > minValue) {
                    float value = (float) animation.getAnimatedValue();
                    animatorScale = value * progressScale;
                    if (value == 1.0f) {
                        animatorNumber = currentValue;
                    } else {
                        animatorNumber = (int) (value * Math.min(currentValue, maxValue));
                    }
                } else {
                    animatorScale = 0.0f;
                    animatorNumber = currentValue;
                    animation.cancel();
                }
                refresh();
            });
        }
        animator.start();
    }

    /**
     * 停止动画.<br>
     * 并不会复原.
     */
    public void stopAnimator() {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
    }

    private void initProgressScale() {
        if (currentValue < minValue) {
            progressScale = 0.0f;
        } else if (currentValue > maxValue) {
            progressScale = 1.0f;
        } else {
            progressScale = (float) (currentValue - minValue) / (maxValue - minValue);
        }
        animatorScale = progressScale;
        animatorNumber = currentValue;
    }

    private void refresh() {
        invalidate();
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int wSize = MeasureSpec.getSize(widthMeasureSpec);
        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        if (wMode != MeasureSpec.EXACTLY) {
            wSize = (int) (200 * oneDp);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(wSize, MeasureSpec.EXACTLY);
        }
        radius = (wSize - progressWidth) / 2.0f;
        if (radius < 0.0f) {
            radius = 0.0f;
        }
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(wSize, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float width = getWidth();
        float height = getHeight();
        float widthHalf = width / 2.0f;
        float heightHalf = height / 2.0f;
        // 绘制数字
        String number = String.valueOf(animatorNumber);
        paint.reset();
        paint.setColor(numberColor);
        paint.setTextSize(numberSize);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setAntiAlias(true);
        paint.getTextBounds(number, 0, number.length(), rect);
        float baseLineOffset = (rect.bottom + rect.top) / 2.0f;
        canvas.drawText(number, widthHalf, heightHalf - baseLineOffset, paint);
        // 底色圆环（进度比例为1则为进度颜色圆环）
        paint.reset();
        if (animatorScale == 1.0f) {
            paint.setColor(progressColor);
        } else {
            paint.setColor(blankColor);
        }
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(progressWidth);
        paint.setAntiAlias(true);
        canvas.drawCircle(widthHalf, heightHalf, radius, paint);
        // 进度圆弧
        if (animatorScale != 0.0f && animatorScale != 1.0f) {
            paint.setColor(progressColor);
            paint.setStrokeCap(Paint.Cap.ROUND);
            circle.set(progressWHalf, progressWHalf, width - progressWHalf, height - progressWHalf);
            float totalLength = (float) (2 * Math.PI * radius);
            float progressLen = animatorScale * totalLength;
            if (progressLen <= progressWidth) {
                paint.reset();
                paint.setColor(progressColor);
                paint.setAntiAlias(true);
                canvas.drawCircle(widthHalf, progressWHalf, progressWHalf, paint);
            } else {
                float offset;
                if (progressLen < 2 * progressWidth) {
                    offset = (progressLen - progressWidth) / 2.0f;
                } else {
                    offset = progressWHalf;
                }
                float offsetAngle = 360.0f * offset / totalLength;
                canvas.drawArc(circle, -90.0f + offsetAngle, 360.0f * (progressLen - progressWidth) / totalLength, false, paint);
            }
        }
    }
}
