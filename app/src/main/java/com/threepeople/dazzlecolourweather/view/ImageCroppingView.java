package com.threepeople.dazzlecolourweather.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.threepeople.dazzlecolourweather.R;

import java.io.FileNotFoundException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ImageCroppingView extends View {
    private final Context context;
    private final DisplayMetrics dm;// 设备显示器信息
    private final Path path;
    private final Paint paint;
    private final DashPathEffect dashPathEffect;
    private final RectF picRectF;// 图片区域矩形
    private final RectF cropRectF;// 裁剪区域矩形
    private final PointF fingerPoint0;// 第一个手指触摸点的坐标
    private final PointF fingerPoint1;// 第二个手指触摸点的坐标
    private final PointF centerPoint;// 两个手指触点的中心点
    private BitmapDrawable picture = null;
    private float scale;// 裁剪区域高度对宽度的比例
    private boolean isMovingCrop = false;// 操作目标为裁剪区域
    private boolean isMovingPic = false;// 操作目标为图片
    private double lastDistance;// 上一次双指操作时两触点的距离

    // *****************属性值*****************
    private int sizeScale;
    private int widthWeight;
    private int heightWeight;
    private int backgroundColor;
    private int shadowColor;
    private boolean showFourAngle;
    private int fillStyle;
    private boolean styleUseDashed;
    private float divideLineWidth;
    private int divideLineColor;

    // ****************枚举常量****************
    /**
     * 使用设置的宽高权重来指定裁剪区域的比例.
     */
    public static final int SCALE_USE_WEIGHT = 0;

    /**
     * 使用当前设备的尺寸来指定裁剪区域的比例.
     */
    public static final int SCALE_DEVICE_SIZE = 1;

    /**
     * 使用当前设备尺寸的反转比例来指定裁剪区域的比例.
     */
    public static final int SCALE_DEVICE_SIZE_INVERT = 2;

    @IntDef({SCALE_USE_WEIGHT, SCALE_DEVICE_SIZE, SCALE_DEVICE_SIZE_INVERT})
    @Retention(RetentionPolicy.SOURCE)
    private @interface SizeScale {}

    /**
     * 裁剪区域内部不填充样式.
     */
    public static final int STYLE_NONE = 0;

    /**
     * 裁剪区域内部将绘制一个内切椭圆.<br>
     * <font color="#F57C00">仅在API 21及以上设置有效</font>.
     */
    public static final int STYLE_CIRCLE = 1;

    /**
     * 裁剪区域内部将绘制九宫格.
     */
    public static final int STYLE_NINE_GRID = 2;

    @IntDef({STYLE_NONE, STYLE_CIRCLE, STYLE_NINE_GRID})
    @Retention(RetentionPolicy.SOURCE)
    private @interface FillStyle {}

    public ImageCroppingView(Context context) {
        this(context, null);
    }

    public ImageCroppingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        dm = context.getResources().getDisplayMetrics();
        // 1dp转换为像素单位的大小
        float oneDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.0f, dm);
        path = new Path();
        paint = new Paint();
        dashPathEffect = new DashPathEffect(new float[] {10, 5}, 0);
        picRectF = new RectF();
        cropRectF = new RectF();
        fingerPoint0 = new PointF();
        fingerPoint1 = new PointF();
        centerPoint = new PointF();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ImageCroppingView);
        try {
            sizeScale = typedArray.getInt(R.styleable.ImageCroppingView_sizeScale, SCALE_USE_WEIGHT);
            widthWeight = typedArray.getInt(R.styleable.ImageCroppingView_widthWeight, 1);
            heightWeight = typedArray.getInt(R.styleable.ImageCroppingView_heightWeight, 1);
            backgroundColor = typedArray.getColor(R.styleable.ImageCroppingView_backgroundColor, Color.rgb(66, 66, 66));
            shadowColor = typedArray.getColor(R.styleable.ImageCroppingView_shadowColor, Color.argb(127, 0, 0, 0));
            showFourAngle = typedArray.getBoolean(R.styleable.ImageCroppingView_showFourAngle, true);
            fillStyle = typedArray.getInt(R.styleable.ImageCroppingView_fillStyle, STYLE_NONE);
            styleUseDashed = typedArray.getBoolean(R.styleable.ImageCroppingView_styleUseDashed, true);
            divideLineWidth = typedArray.getDimension(R.styleable.ImageCroppingView_divideLineWidth, oneDp);
            divideLineColor = typedArray.getColor(R.styleable.ImageCroppingView_divideLineColor, Color.WHITE);
        } finally {
            typedArray.recycle();
        }
        setSizeScale(sizeScale);
    }

    private void initScale(int wWeight, int hWeight) {
        scale = (float) hWeight / wWeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(dm.widthPixels, MeasureSpec.AT_MOST);
        }
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(dm.heightPixels, MeasureSpec.AT_MOST);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        measureCropArea();
        canvas.drawColor(backgroundColor);// 填充背景色
        // 绘制图片
        if (picture != null) {
            picture.setBounds((int) picRectF.left, (int) picRectF.top, (int) picRectF.right, (int) picRectF.bottom);
            picture.draw(canvas);// 避免因直接使用drawBitmap方法带来的可能的内存不足的问题
        }
        // 绘制裁剪区域
        drawCropArea(canvas);
    }

    /**
     * 通过合理计算得出裁剪区域.
     */
    private void measureCropArea() {
        if (cropRectF.isEmpty()) {
            float width = getWidth();
            float height = getHeight();
            boolean wideView = width > height;// 视图偏宽
            boolean wideCrop = scale < 1;// 裁剪区域偏宽
            float cropWidth;
            float cropHeight;
            if (wideView) {
                if (wideCrop) {
                    // 判断视图的高是否足够容纳的下裁剪区域需要的高度
                    // 若直接使用视图高度为基准可能使得裁剪区域过于细长
                    float cropWidthTemp = width * 2.0f / 3.0f;
                    float cropHeightTemp = cropWidthTemp * scale;
                    if (cropHeightTemp > height) {
                        cropHeight = height * 2.0f / 3.0f;
                        cropWidth = cropHeight / scale;
                    } else {
                        cropWidth = cropWidthTemp;
                        cropHeight = cropHeightTemp;
                    }
                } else {
                    // 以高度的2/3作为裁剪区域高度
                    // 宽度按设定比例得出
                    cropHeight = height * 2.0f / 3.0f;
                    cropWidth = cropHeight / scale;
                }
            } else {
                if (wideCrop) {
                    // 以宽度的2/3作为裁剪区域宽度
                    // 高度按设定比例得出
                    cropWidth = width * 2.0f / 3.0f;
                    cropHeight = cropWidth * scale;
                } else {
                    // 判断视图的宽是否足够容纳的下裁剪区域需要的宽度
                    // 若直接使用视图宽度为基准可能使得裁剪区域过细高
                    float cropHeightTemp = height * 2.0f / 3.0f;
                    float cropWidthTemp = cropHeightTemp / scale;
                    if (cropWidthTemp > width) {
                        cropWidth = width * 2.0f / 3.0f;
                        cropHeight = cropWidth * scale;
                    } else {
                        cropWidth = cropWidthTemp;
                        cropHeight = cropHeightTemp;
                    }
                }
            }
            float cropLeft = (width - cropWidth) / 2.0f;
            float cropTop = (height - cropHeight) / 2.0f;
            cropRectF.set(cropLeft, cropTop, cropLeft + cropWidth, cropTop + cropHeight);
            measurePicture();
        }
    }

    private void measurePicture() {
        if (picture != null) {
            float picWidth = picture.getIntrinsicWidth();
            float picHeight = picture.getIntrinsicHeight();
            float cropHeight = cropRectF.height();
            float cropWidth = cropRectF.width();
            if (picWidth > picHeight) {// 宽图，按图片高度缩放到裁剪区域高度
                picWidth = picWidth * cropHeight / picHeight;
                if (picWidth < cropWidth) {// 缩放后宽度不够，则放大宽度
                    picHeight = cropHeight * cropWidth / picWidth;
                    picWidth = cropWidth;
                } else {
                    picHeight = cropHeight;
                }
            } else {// 高图，按图片宽度缩放到裁剪区域宽度
                picHeight = picHeight * cropWidth / picWidth;
                if (picHeight < cropHeight) {// 缩放后高度不够，则放大高度
                    picWidth = cropWidth * cropHeight / picHeight;
                    picHeight = cropHeight;
                } else {
                    picWidth = cropWidth;
                }
            }
            // 将图片居中放置
            float picLeft = (getWidth() - picWidth) / 2.0f;
            float picTop = (getHeight() - picHeight) / 2.0f;
            picRectF.set(picLeft, picTop, picLeft + picWidth, picTop + picHeight);
        }
    }

    /**
     * 绘制裁剪区域.
     */
    private void drawCropArea(Canvas canvas) {
        // 绘制阴影层
        canvas.save();
        canvas.clipRect(cropRectF, Region.Op.DIFFERENCE);
        canvas.drawColor(shadowColor);
        canvas.restore();
        // 绘制四个角
        if (showFourAngle) {
            path.reset();
            float lineLength = 0.1f * Math.min(cropRectF.width(), cropRectF.height());
            // 左上角
            path.moveTo(cropRectF.left - divideLineWidth, cropRectF.top + lineLength);
            path.rLineTo(0, - lineLength - divideLineWidth);
            path.rLineTo(divideLineWidth + lineLength, 0);
            // 右上角
            path.moveTo(cropRectF.right - lineLength, cropRectF.top - divideLineWidth);
            path.rLineTo(lineLength + divideLineWidth, 0);
            path.rLineTo(0, divideLineWidth + lineLength);
            // 右下角
            path.moveTo(cropRectF.right + divideLineWidth, cropRectF.bottom - lineLength);
            path.rLineTo(0, lineLength + divideLineWidth);
            path.rLineTo(- divideLineWidth - lineLength, 0);
            // 左下角
            path.moveTo(cropRectF.left + lineLength, cropRectF.bottom + divideLineWidth);
            path.rLineTo(- lineLength - divideLineWidth, 0);
            path.rLineTo(0, - divideLineWidth - lineLength);
            paint.reset();
            paint.setColor(divideLineColor);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2 * divideLineWidth);
            canvas.drawPath(path, paint);
        }
        // 绘制区域内样式
        if (fillStyle != STYLE_NONE) {
            if (!showFourAngle) {
                paint.reset();
                paint.setColor(divideLineColor);
                paint.setStyle(Paint.Style.STROKE);
            }
            paint.setStrokeWidth(divideLineWidth);
            if (styleUseDashed) {
                paint.setPathEffect(dashPathEffect);
            }
            path.reset();
            float strokeHalf = divideLineWidth / 2.0f;
            if (fillStyle == STYLE_CIRCLE) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    paint.setAntiAlias(true);// 抗锯齿
                    path.addOval(cropRectF.left + strokeHalf, cropRectF.top + strokeHalf,
                            cropRectF.right - strokeHalf, cropRectF.bottom - strokeHalf, Path.Direction.CW);
                }
            } else if (fillStyle == STYLE_NINE_GRID) {
                float cropWidth = cropRectF.width();
                float cropHeight = cropRectF.height();
                // 上横
                path.moveTo(cropRectF.left + strokeHalf, cropRectF.top + cropHeight / 3.0f);
                path.lineTo(cropRectF.right - strokeHalf, cropRectF.top + cropHeight / 3.0f);
                // 下横
                path.moveTo(cropRectF.left + strokeHalf, cropRectF.top + cropHeight * 2.0f / 3.0f);
                path.lineTo(cropRectF.right - strokeHalf, cropRectF.top + cropHeight * 2.0f / 3.0f);
                // 左竖
                path.moveTo(cropRectF.left + cropWidth / 3.0f, cropRectF.top + strokeHalf);
                path.lineTo(cropRectF.left + cropWidth / 3.0f, cropRectF.bottom - strokeHalf);
                // 右竖
                path.moveTo(cropRectF.left + cropWidth * 2.0f / 3.0f, cropRectF.top + strokeHalf);
                path.lineTo(cropRectF.left + cropWidth * 2.0f / 3.0f, cropRectF.bottom - strokeHalf);
            }
            canvas.drawPath(path, paint);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (picture != null) {
            float x0 = event.getX();
            float y0 = event.getY();
            float x1 = 0.0f;
            float y1 = 0.0f;
            if (event.getPointerCount() == 2) {// 双指
                x1 = event.getX(1);
                y1 = event.getY(1);
            }
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    isMovingCrop = cropRectF.contains(x0, y0);
                    isMovingPic = false;
                    fingerPoint0.set(x0, y0);
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    isMovingCrop = false;
                    if (isMovingPic = event.getPointerCount() == 2 // 双指操作
                            // 至少有一点在图片范围内
                            && (picRectF.contains(fingerPoint0.x, fingerPoint0.y) || picRectF.contains(x1, y1))) {
                        fingerPoint1.set(x1, y1);
                        updateCenterPoint();
                        lastDistance = computeDistance();
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (isMovingCrop) {
                        float xDiff = x0 - fingerPoint0.x;
                        float yDiff = y0 - fingerPoint0.y;
                        fingerPoint0.set(x0, y0);
                        // 限制不能滑出图片的范围
                        if (cropRectF.left + xDiff < picRectF.left) {// 左移
                            xDiff = picRectF.left - cropRectF.left;
                        } else if (cropRectF.right + xDiff > picRectF.right) {// 右移
                            xDiff = picRectF.right - cropRectF.right;
                        }
                        if (cropRectF.top + yDiff < picRectF.top) {// 上移
                            yDiff = picRectF.top - cropRectF.top;
                        } else if (cropRectF.bottom + yDiff > picRectF.bottom) {// 下移
                            yDiff = picRectF.bottom - cropRectF.bottom;
                        }
                        // 限制不能滑出整个视图的范围
                        if (cropRectF.left + xDiff < 0) {// 左移
                            xDiff = - cropRectF.left;
                        } else if (cropRectF.right + xDiff > getWidth()) {// 右移
                            xDiff = getWidth() - cropRectF.right;
                        }
                        if (cropRectF.top + yDiff < 0) {// 上移
                            yDiff = - cropRectF.top;
                        } else if (cropRectF.bottom + yDiff > getHeight()) {// 下移
                            yDiff = getHeight() - cropRectF.bottom;
                        }
                        cropRectF.offset(xDiff, yDiff);
                        refresh();
                    }
                    if (isMovingPic) {
                        double distance = computeDistance();
                        fingerPoint0.set(x0, y0);
                        fingerPoint1.set(x1, y1);
                        if (Math.abs(distance - lastDistance) <= 20/*临界值*/) {// 平行移动
                            // 考虑到滑动过程中的轻微抖动，因此设定临界值，
                            // 两点距离的变动值在该值以内均视为平行移动
                            float centerX = centerPoint.x;
                            float centerY = centerPoint.y;
                            updateCenterPoint();
                            float xDiff = centerPoint.x - centerX;
                            float yDiff = centerPoint.y - centerY;
                            // 限制必须包含裁剪区域
                            if (picRectF.left + xDiff > cropRectF.left) {// 右移
                                xDiff = cropRectF.left - picRectF.left;
                            } else if (picRectF.right + xDiff < cropRectF.right) {// 左移
                                xDiff = picRectF.right - cropRectF.right;
                            }
                            if (picRectF.top + yDiff > cropRectF.top) {// 下移
                                yDiff = cropRectF.top - picRectF.top;
                            } else if (picRectF.bottom + yDiff < cropRectF.bottom) {// 上移
                                yDiff = picRectF.bottom - cropRectF.bottom;
                            }
                            picRectF.offset(xDiff, yDiff);
                        } else {// 缩放
                            // 将双指中心点转化为缩放中心点
                            float zoomCenterX = Math.max(picRectF.left, Math.min(centerPoint.x, picRectF.right));
                            float zoomCenterY = Math.max(picRectF.top, Math.min(centerPoint.y, picRectF.bottom));
                            updateCenterPoint();
                            float picWidth = picRectF.width();
                            float picHeight = picRectF.height();
                            // 计算缩放中心点在图片中x、y方向上的位置比例
                            float xScale = (zoomCenterX - picRectF.left) / picWidth;// 缩放中心x方向位置比例
                            float yScale = (zoomCenterY - picRectF.top) / picHeight;// 缩放中心y方向位置比例
                            float zoomScale = (float) (distance / lastDistance);// 图片的缩放比例
                            // 限制至少要包含裁剪区域
                            float newPicWidth = Math.max(picWidth * zoomScale, cropRectF.width());// 缩放后图片的宽度
                            float newPicHeight = newPicWidth * picHeight / picWidth;// 缩放后图片的高度
                            if (newPicHeight < cropRectF.height()) {// 需要放大，放大后的图片宽度一定大于裁剪区域的宽度
                                newPicHeight *= (cropRectF.height() / newPicHeight);
                                newPicWidth = newPicHeight * picWidth / picHeight;
                            }
                            // 根据缩放中心的位置比例计算图片的矩阵位置
                            float newPicLeft = zoomCenterX - newPicWidth * xScale;
                            float newPicTop = zoomCenterY - newPicHeight * yScale;
                            picRectF.set(newPicLeft, newPicTop, newPicLeft + newPicWidth, newPicTop + newPicHeight);
                            // 校正图片位置
                            // 此时图片的宽高一定大于裁剪区域的宽高
                            float xDiff = 0.0f;
                            float yDiff = 0.0f;
                            if (picRectF.left > cropRectF.left) {
                                xDiff = cropRectF.left - picRectF.left;
                            } else if (picRectF.right < cropRectF.right) {
                                xDiff = cropRectF.right - picRectF.right;
                            }
                            if (picRectF.top > cropRectF.top) {
                                yDiff = cropRectF.top - picRectF.top;
                            } else if (picRectF.bottom < cropRectF.bottom) {
                                yDiff = cropRectF.bottom - picRectF.bottom;
                            }
                            picRectF.offset(xDiff, yDiff);
                        }
                        refresh();
                        lastDistance = distance;
                    }
                    break;
                default:
                    isMovingCrop = false;
                    isMovingPic = false;
                    break;
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

    private void updateCenterPoint() {
        centerPoint.set((fingerPoint0.x + fingerPoint1.x) / 2.0f, (fingerPoint0.y + fingerPoint1.y) / 2.0f);
    }

    private double computeDistance() {
        return Math.sqrt(Math.pow(fingerPoint0.x - fingerPoint1.x, 2.0) + Math.pow(fingerPoint0.y - fingerPoint1.y, 2.0));
    }

    /**
     * 获得裁剪结果.
     *
     * @return 裁剪结果
     */
    public Bitmap getCroppingResult() {
        if (picture != null) {
            Bitmap bitmap = picture.getBitmap();
            int pictureWidth = bitmap.getWidth();
            int pictureHeight = bitmap.getHeight();
            float picWidth = picRectF.width();
            float picHeight = picRectF.height();
            return Bitmap.createBitmap(
                    bitmap,
                    (int) (pictureWidth * (cropRectF.left - picRectF.left) / picWidth),// 截取的起始x
                    (int) (pictureHeight * (cropRectF.top - picRectF.top) / picHeight),// 截取的起始y
                    (int) (pictureWidth * cropRectF.width() / picWidth),// 截取的宽度
                    (int) (pictureHeight * cropRectF.height() / picHeight)// 截取的高度
            );
        }
        return null;
    }

    // ****************更新数据****************
    /**
     * 设置目标裁剪图片.
     *
     * @param picture 目标图片
     * @return 当前对象的引用
     */
    public ImageCroppingView setPicture(@NonNull BitmapDrawable picture) {
        this.picture = picture;
        cropRectF.setEmpty();
        return this;
    }

    /**
     * 设置目标裁剪图片.
     *
     * @param picture 目标图片
     * @return 当前对象的引用
     */
    public ImageCroppingView setPicture(@NonNull Bitmap picture) {
        return setPicture(new BitmapDrawable(context.getResources(), picture));
    }

    /**
     * 使用图片的URI设置目标裁剪图片.
     *
     * @param pictureUri 目标图片的URI
     * @return 当前对象的引用
     * @throws FileNotFoundException 如果无法打开提供的URI
     * @throws RuntimeException 如果传入的URI文件不是图片
     */
    public ImageCroppingView setPicture(@NonNull Uri pictureUri) throws FileNotFoundException, RuntimeException {
        Drawable drawable = Drawable.createFromStream(
                context.getContentResolver().openInputStream(pictureUri), null);
        if (!(drawable instanceof BitmapDrawable)) {
            throw new RuntimeException("错误的图片类型");
        }
        return setPicture((BitmapDrawable) drawable);
    }

    /**
     * 设置裁剪区域尺寸比例的类型.
     *
     * @param sizeScale 类型值
     * @return 当前对象的引用
     * @see #SCALE_USE_WEIGHT
     * @see #SCALE_DEVICE_SIZE
     * @see #SCALE_DEVICE_SIZE_INVERT
     */
    public ImageCroppingView setSizeScale(@SizeScale int sizeScale) {
        this.sizeScale = sizeScale;
        switch (sizeScale) {
            case SCALE_USE_WEIGHT:
                initScale(widthWeight, heightWeight);
                break;
            case SCALE_DEVICE_SIZE:
                initScale(dm.widthPixels, dm.heightPixels);
                break;
            case SCALE_DEVICE_SIZE_INVERT:
                initScale(dm.heightPixels, dm.widthPixels);
                break;
            default:
                scale = 1.0f;
                break;
        }
        cropRectF.setEmpty();
        return this;
    }

    /**
     * 设置新的裁剪区域宽高比例.<br>
     * sizeScale的值将同时设为{@link #SCALE_USE_WEIGHT}.
     *
     * @param widthWeight 宽度所占分量
     * @param heightWeight 高度所占分量
     * @return 当前对象的引用
     */
    public ImageCroppingView setWeightScale(int widthWeight, int heightWeight) {
        this.widthWeight = widthWeight;
        this.heightWeight = heightWeight;
        sizeScale = SCALE_USE_WEIGHT;
        initScale(widthWeight, heightWeight);
        cropRectF.setEmpty();
        return this;
    }

    public ImageCroppingView setCroppingBackgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public ImageCroppingView setShadowColor(@ColorInt int shadowColor) {
        this.shadowColor = shadowColor;
        return this;
    }

    public ImageCroppingView setShowFourAngle(boolean showFourAngle) {
        this.showFourAngle = showFourAngle;
        return this;
    }

    public ImageCroppingView setStyleUseDashed(boolean styleUseDashed) {
        this.styleUseDashed = styleUseDashed;
        return this;
    }

    public ImageCroppingView setDivideLineColor(@ColorInt int divideLineColor) {
        this.divideLineColor = divideLineColor;
        return this;
    }

    public ImageCroppingView setDivideLineWidth(float divideLineWidth) {
        this.divideLineWidth = divideLineWidth;
        return this;
    }

    /**
     * 设置裁剪区域的内部样式.
     *
     * @param fillStyle 样式值
     * @return 当前对象的引用
     * @see #STYLE_NONE
     * @see #STYLE_CIRCLE
     * @see #STYLE_NINE_GRID
     */
    public ImageCroppingView setFillStyle(@FillStyle int fillStyle) {
        this.fillStyle = fillStyle;
        return this;
    }

    /**
     * 刷新所有的设置.
     */
    public void refresh() {
        invalidate();
        requestLayout();
    }
}
