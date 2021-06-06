package com.threepeople.dazzlecolourweather.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class AutoHeightViewPager extends ViewPager {
    private boolean canScroll = true;// 能否切换页面
    private OnPageChangeListener onPageChangeListener = null;

    public AutoHeightViewPager(@NonNull Context context) {
        super(context);
    }

    public AutoHeightViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 设置能否滑动以切换页面.
     *
     * @param canScroll true表示能，false表示不能
     */
    public void setCanScroll(boolean canScroll) {
        this.canScroll = canScroll;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        AutoHeightPagerAdapter adapter = (AutoHeightPagerAdapter) getAdapter();
        if (adapter != null) {
            View view = adapter.getIndexView(getCurrentItem());
            if (view != null) {
                int height = measureHeight(heightMeasureSpec, view);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private int measureHeight(int measureSpec, View view) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            return specSize;
        } else {
            view.measure(0, 0);
            if (specMode == MeasureSpec.UNSPECIFIED) {
                return view.getMeasuredHeight();
            } else {
                return Math.min(view.getMeasuredHeight(), specSize);
            }
        }
    }

    @Override
    public void setCurrentItem(int item) {
        super.setCurrentItem(item);
        if (onPageChangeListener != null) {
            onPageChangeListener.onPageSelected(item);
            onPageChangeListener.onPageScrollStateChanged(SCROLL_STATE_IDLE);
        }
    }

    @Override
    public void addOnPageChangeListener(@NonNull OnPageChangeListener listener) {
        this.onPageChangeListener = listener;
        super.addOnPageChangeListener(listener);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return canScroll && super.onTouchEvent(ev);
    }

    @Override
    public void setAdapter(@Nullable PagerAdapter adapter) {
        if (!(adapter instanceof AutoHeightPagerAdapter)) {
            throw new RuntimeException("适配器必须是类AutoHeightPagerAdapter及其子类的对象");
        }
        super.setAdapter(adapter);
    }

    public static abstract class AutoHeightPagerAdapter extends PagerAdapter {
        /**
         * 获取索引处的View.
         *
         * @param index 合法的索引
         * @return 索引处的View
         */
        @Nullable
        public abstract View getIndexView(int index);
    }
}
