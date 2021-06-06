package com.threepeople.dazzlecolourweather.view;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.threepeople.dazzlecolourweather.R;

public class WaterWaveRefreshLayout extends LinearLayout {
    private OnRefreshListener listener = null;
    private long refreshTime;// 开始刷新的时间
    private final long animatorTime = 1000;// 动画持续时间（ms）
    private boolean allowRefresh = true;// 是否允许刷新
    private boolean isRefresh = false;// 是否正在刷新
    private boolean refreshSuccess = false;// 是否正在显示刷新成功的提示
    private boolean interceptTouchEvent = false;// 是否拦截触摸事件

    // 绘制用数据
    private final Paint paint;
    private final Path path;
    private final Path animatorArea;// 绘制动画的区域，用于与动画绘制路径取交集
    private final Rect rect;// 用于确定文字边界
    private final int paddingTop;// 最初的的内部上边距
    private float lastY = 0.0f;
    private float downY = 0.0f;
    private float bottomY = 0.0f;// 绘制顶部滑出区域时其的底部所在Y轴的值
    private ValueAnimator waterWaveAnimator = null;
    private float rearXDistance;// 后面的水波X方向位移
    private float frontXDistance;// 前面的水波X方向位移
    private float waveYDistance;// 水波Y方向位移
    private String tipTextWhenEndRefresh;// 当取消正在刷新状态时显示的文本

    // 自定义属性
    private int waterWaveColor;// 水波的颜色
    private final float waterWaveLength;// 一个完整水波的长度
    private final float waterWaveHeight;// 约为一个完整水波的高度
    private final float maxTopHeight;// 顶部区域滑出的最大高度
    private String tipTextForDropDown;// 提示下拉刷新的文本内容
    private String tipTextForRefresh;// 提示松手刷新的文本内容
    private String tipTextOnRefreshSuccess;// 刷新成功时的提示文字
    private final float tipTextSize;// 提示文字的字体大小
    private int tipTextColor;// 提示文字的颜色
    // 通过自定义属性的值计算出的值
    private final float height;// 整个水波动画区域的高度
    private final float refreshLimit;// 顶部区域滑出距离不小于该值时，释放后刷新

    public WaterWaveRefreshLayout(Context context) {
        this(context, null);
    }

    public WaterWaveRefreshLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setClickable(true);// 设置以使其获取ACTION_MOVE事件
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        float oneDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.0f, dm);
        paint = new Paint();
        path = new Path();
        animatorArea = new Path();
        rect = new Rect();
        paddingTop = getPaddingTop();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.WaterWaveRefreshLayout);
        try {
            waterWaveColor = typedArray.getColor(R.styleable.WaterWaveRefreshLayout_waterWaveColor, Color.rgb(25, 139, 236));
            waterWaveLength = typedArray.getDimension(R.styleable.WaterWaveRefreshLayout_waterWaveLength, 60 * oneDp);
            waterWaveHeight = typedArray.getDimension(R.styleable.WaterWaveRefreshLayout_waterWaveHeight, 15 * oneDp);
            maxTopHeight = typedArray.getDimension(R.styleable.WaterWaveRefreshLayout_maxTopHeight, 80 * oneDp);
            // 水波到顶部区域底部的间距
            float waterWaveBottomOffset = typedArray.getDimension(R.styleable.WaterWaveRefreshLayout_waterWaveBottomOffset, 10 * oneDp);
            height = maxTopHeight - waterWaveBottomOffset;
            float refreshLimitScale = typedArray.getFloat(R.styleable.WaterWaveRefreshLayout_refreshLimitScale, 0.75f);
            if (refreshLimitScale < 0.1f) {
                refreshLimit = 0.1f * maxTopHeight;
            } else if (refreshLimitScale > 1.0f) {
                refreshLimit = maxTopHeight;
            } else {
                refreshLimit = refreshLimitScale * maxTopHeight;
            }
            tipTextForDropDown = typedArray.getString(R.styleable.WaterWaveRefreshLayout_tipTextForDropDown);
            if (tipTextForDropDown == null) {
                tipTextForDropDown = "下拉刷新";
            }
            tipTextForRefresh = typedArray.getString(R.styleable.WaterWaveRefreshLayout_tipTextForRefresh);
            if (tipTextForRefresh == null) {
                tipTextForRefresh = "释放立即刷新";
            }
            tipTextOnRefreshSuccess = typedArray.getString(R.styleable.WaterWaveRefreshLayout_tipTextOnRefreshSuccess);
            if (tipTextOnRefreshSuccess == null) {
                tipTextOnRefreshSuccess = "刷新成功";
            }
            tipTextSize = typedArray.getDimension(R.styleable.WaterWaveRefreshLayout_tipTextSize, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14.0f, dm));
            tipTextColor = typedArray.getColor(R.styleable.WaterWaveRefreshLayout_tipTextColor, Color.rgb(25, 139, 236));
        } finally {
            typedArray.recycle();
        }
    }

    /**
     * 设置水波的颜色.
     *
     * @param waterWaveColor 水波的新颜色
     */
    public void setWaterWaveColor(@ColorInt int waterWaveColor) {
        this.waterWaveColor = waterWaveColor;
    }

    /**
     * 设置提示文本的颜色.
     *
     * @param tipTextColor 提示文本的新颜色
     */
    public void setTipTextColor(int tipTextColor) {
        this.tipTextColor = tipTextColor;
    }

    /**
     * 设置是否允许响应刷新.
     *
     * @param allowRefresh true表示允许刷新，false表示禁用刷新
     */
    public void setAllowRefresh(boolean allowRefresh) {
        this.allowRefresh = allowRefresh;
    }

    /**
     * 设置是否拦截触摸事件以响应刷新.
     *
     * @param interceptTouchEvent true表示拦截事件，false表示不拦截
     */
    public void setInterceptTouchEvent(boolean interceptTouchEvent) {
        this.interceptTouchEvent = interceptTouchEvent;
    }

    /**
     * 获得当前是否正在刷新.
     *
     * @return 若正在刷新则返回true，否则返回false
     */
    public boolean isRefresh() {
        return isRefresh;
    }

    /**
     * 设置刷新状态.
     * 若设置的状态与当前状态一致则不做任何处理.
     * 若为结束正在刷新状态，则结束时展示刷新成功的文本.
     *
     * @param isRefresh true表示将当前状态更新为正在刷新，同时触发OnRefreshListener监听回调；
     *                  false则表示将当前正在刷新的状态取消
     * @see #endRefresh(String)
     */
    public void setRefresh(boolean isRefresh) {
        if (this.isRefresh != isRefresh) {
            if (isRefresh) {
                this.isRefresh = true;
                bottomY = maxTopHeight;
                setPadding(getPaddingLeft(), (int) (paddingTop + maxTopHeight), getPaddingRight(), getPaddingBottom());
                startWaterWaveAnimator();
                if (listener != null) {
                    listener.onRefresh();
                }
            } else {
                tipTextWhenEndRefresh = tipTextOnRefreshSuccess;
                this.isRefresh = false;
                refreshSuccess = true;
            }
            refreshView();
        }
    }

    /**
     * 结束正在刷新状态同时展示指定的文本.
     * 若不处于正在刷新状态则不做任何处理.
     *
     * @param tipText 展示的文本
     * @see #setRefresh(boolean)
     */
    public void endRefresh(@NonNull String tipText) {
        if (isRefresh) {
            tipTextWhenEndRefresh = tipText;
            isRefresh = false;
            refreshSuccess = true;
            refreshView();
        }
    }

    /**
     * 确保动画至少完整执行一次.
     */
    private void delayEndRefresh() {
        long passingTime = System.currentTimeMillis() - refreshTime;
        if (passingTime >= animatorTime) {
            isRefresh = false;
            refreshSuccess = true;
            refreshView();
        } else {
            postDelayed(() -> {
                isRefresh = false;
                refreshSuccess = true;
                refreshView();
            }, animatorTime - passingTime);
        }
    }

    /**
     * 设置监听正在刷新状态的回调.
     *
     * @param listener 监听回调
     */
    public void setOnRefreshListener(OnRefreshListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bottomY > 0) {
            paint.reset();
            if (isRefresh) {
                float width = getWidth();// 整个动画可用区域的宽度
                animatorArea.reset();
                animatorArea.addRect(0.0f, 0.0f, width, height, Path.Direction.CW);
                float distance = waterWaveLength / 4.0f;// 完整水波的长度的1/4
                paint.setColor(waterWaveColor);
                paint.setAlpha((int) (255 * 0.3));
                path.reset();
                // 后面的水波
                path.moveTo(- rearXDistance, height - waveYDistance);
                int count = (int) (width / waterWaveLength + 2/*避免首尾留空*/ + 0.5/*四舍五入*/);// 需要绘制的完整水波的个数
                for (int i = 0; i < count; i++) {
                    path.rQuadTo(distance, - waterWaveHeight, 2.0f * distance, 0.0f);
                    path.rQuadTo(distance, waterWaveHeight, 2.0f * distance, 0.0f);
                }
                path.lineTo(width, height);
                path.lineTo(0, height);
                path.lineTo(- rearXDistance, height - waveYDistance);
                path.op(animatorArea, Path.Op.INTERSECT);// 将其与动画绘制区域取交集
                canvas.drawPath(path, paint);
                // 前面的水波
                paint.setAlpha((int) (255 * 0.5));
                path.reset();
                path.moveTo(- frontXDistance, height - waveYDistance);
                for (int i = 0; i < count; i++) {
                    path.rQuadTo(distance, - waterWaveHeight, 2.0f * distance, 0.0f);
                    path.rQuadTo(distance, waterWaveHeight, 2.0f * distance, 0.0f);
                }
                path.lineTo(width, height);
                path.lineTo(0, height);
                path.lineTo(- frontXDistance, height - waveYDistance);
                path.op(animatorArea, Path.Op.INTERSECT);// 将其与动画绘制区域取交集
                canvas.drawPath(path, paint);
            } else {
                if (refreshSuccess) {// 刷新完成
                    waterWaveAnimator.cancel();
                    drawText(canvas, tipTextWhenEndRefresh);
                    postDelayed(() -> {
                        refreshSuccess = false;
                        bottomY = 0.0f;
                        setPadding(getPaddingLeft(), paddingTop, getPaddingRight(), getPaddingBottom());
                        refreshView();
                    }, 500);
                } else {
                    if (bottomY < refreshLimit) {// 下拉刷新
                        drawText(canvas, tipTextForDropDown);
                    } else {// 释放后立即刷新
                        drawText(canvas, tipTextForRefresh);
                    }
                }
            }
        }
    }

    /**
     * 在顶部滑出区域（包括未显示的部分）居中绘制文字.
     *
     * @param canvas 绘制的画布
     * @param text 绘制的文本
     */
    private void drawText(Canvas canvas, String text) {
        paint.setColor(tipTextColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(tipTextSize);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.getTextBounds(text, 0, text.length(), rect);
        float topCenterX = getWidth() / 2.0f;
        float topCenterY = bottomY - maxTopHeight / 2.0f;
        float textOffset = (rect.top + rect.bottom) / 2.0f;
        canvas.drawText(text, topCenterX, topCenterY - textOffset, paint);
    }

    private float srcDownY;// DOWN事件的初始Y坐标
    private boolean isScrollEvent = false;// 是否为滑动事件

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (interceptTouchEvent) {
            switch (ev.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    srcDownY = ev.getY();// 记录坐标
                    downY = srcDownY;
                    lastY = srcDownY;
                    break;
                case MotionEvent.ACTION_MOVE:
                    float scrollDistance = srcDownY - ev.getY();
                    isScrollEvent = Math.abs(scrollDistance) > 1;// 判断是否为滑动事件
                    if (isScrollEvent) {
                        if (scrollDistance > 0) {// 上滑
                            interceptTouchEvent = false;// 取消拦截
                            // 改为DOWN事件重新分发
                            dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                                    MotionEvent.ACTION_DOWN, ev.getX(), ev.getY(), ev.getMetaState()));
                            return true;// 丢弃当前MOVE事件
                        } else {// 下滑
                            interceptTouchEvent = true;
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    onTouchEvent(ev);
                default:
                    isScrollEvent = false;
                    break;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return isScrollEvent ? (interceptTouchEvent || super.onInterceptTouchEvent(ev)) : super.onInterceptTouchEvent(ev);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isRefresh || refreshSuccess) {// 正在刷新时或展示刷新成功时禁止滑动
            return true;
        }
        if (allowRefresh) {
            switch (ev.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_MOVE:
                    float nowY = ev.getY();
                    bottomY = nowY - downY;// 计算顶部区域滑出的高度
                    if (bottomY > 0) {
                        if (bottomY > maxTopHeight && nowY > lastY/*下滑*/) {
                            downY = nowY - maxTopHeight;// 达到最大限制时继续下滑，相当于将DOWN时的Y坐标下移
                            bottomY = maxTopHeight;
                        }
                        lastY = nowY;
                        setPadding(getPaddingLeft(), (int) (paddingTop + bottomY), getPaddingRight(), getPaddingBottom());
                        refreshView();
                        return true;
                    } else {
                        bottomY = 0.0f;
                        setPadding(getPaddingLeft(), paddingTop, getPaddingRight(), getPaddingBottom());
                        break;// 收起顶部区域，继续处理
                    }
                case MotionEvent.ACTION_UP:
                    if (bottomY >= refreshLimit) {// 此时立即刷新
                        isRefresh = true;
                        if (listener != null) {
                            listener.onRefresh();
                        }
                        bottomY = maxTopHeight;
                        setPadding(getPaddingLeft(), (int) (paddingTop + maxTopHeight), getPaddingRight(), getPaddingBottom());
                        startWaterWaveAnimator();
                        return true;
                    }
                default:
                    bottomY = 0.0f;
                    setPadding(getPaddingLeft(), paddingTop, getPaddingRight(), getPaddingBottom());
                    break;
            }
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 启动正在刷新时的水波动画.
     */
    private void startWaterWaveAnimator() {
        refreshTime = System.currentTimeMillis();
        rearXDistance = 0.0f;
        frontXDistance = 0.0f;
        waveYDistance = 0.0f;
        if (waterWaveAnimator == null) {
            waterWaveAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
            waterWaveAnimator.setRepeatCount(ValueAnimator.INFINITE);
            waterWaveAnimator.setRepeatMode(ValueAnimator.RESTART);
            waterWaveAnimator.setDuration(animatorTime);
            waterWaveAnimator.setInterpolator(new LinearInterpolator());
        }
        waterWaveAnimator.start();
        waterWaveAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            private float lastWaveYDistance = 0.0f;

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                rearXDistance = value * waterWaveLength;
                frontXDistance = 2 * rearXDistance;
                float heightLimit = height * 3.0f / 5.0f;
                waveYDistance = value * heightLimit;
                if (waveYDistance < lastWaveYDistance) {
                    waveYDistance = heightLimit;
                }
                lastWaveYDistance = waveYDistance;
                refreshView();
            }
        });
    }

    /**
     * 刷新绘制内容.
     */
    private void refreshView() {
        invalidate();
        requestLayout();
    }

    /**
     * 对正在刷新状态的监听回调.
     */
    public interface OnRefreshListener {
        void onRefresh();
    }
}
