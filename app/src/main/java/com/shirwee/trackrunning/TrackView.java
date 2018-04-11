package com.shirwee.trackrunning;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.support.annotation.FloatRange;
import android.util.AttributeSet;
import android.view.View;

/**
 * 轨道
 * @author shirwee
 */
public class TrackView
        extends View
{
    private final float  default_track_radius;
    private final float default_stroke_width;
    private final int default_finished_color          = Color.rgb(66, 145, 241);
    private final int default_unfinished_color        = Color.rgb(204, 204, 204);
    private final int   min_size;
    private final int default_padding;

    private int   trackBackgroundColor;
    private int   trackProgressColor;
    private int   trackStrokeColor;
    private float trackStrokeWidth;
    private float trackWidth;
    private Paint backgroundPaint;
    private Paint progressPaint;
    private Paint strokePaint;
    private Path        trackPath   = new Path();
    private PathMeasure pathMeasure = new PathMeasure();
    private RectF       trackRect   = new RectF();
    private int     width;
    private int     height;
    private float   trackRadius;
    private float   totalLength;

    private Path destPath = new Path();
    private float progress;
    private float ratio;

    public TrackView(Context context) {
        this(context, null);
    }

    public TrackView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TrackView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        min_size = (int) Utils.dp2px(getResources(), 300);
        default_stroke_width = Utils.dp2px(getResources(), 6);
        default_track_radius = Utils.dp2px(getResources(), 50);
        default_padding = (int) Utils.dp2px(getResources(),11);

        final TypedArray attributes = context.getTheme()
                                             .obtainStyledAttributes(attrs,
                                                                     R.styleable.TrackView,
                                                                     defStyleAttr,
                                                                     0);
        initByAttributes(attributes);
        attributes.recycle();

        initPainters();
    }

    private void initPath() {
        trackPath.reset();

        float delta = trackStrokeWidth + trackWidth / 2+default_padding;
        // 起点
        float startX = width - trackRadius * (1+ratio) - delta;
        float startY = delta;

        trackPath.moveTo(startX, startY);
        trackPath.lineTo(startX + trackRadius*ratio, startY);

        // 右上
        trackRect.set(startX + trackRadius*ratio-trackRadius, startY, width - delta, delta + trackRadius+trackRadius);
        trackPath.arcTo(trackRect, -90, 90);
        trackPath.lineTo(width - delta, height - delta - trackRadius);

        // 右下
        trackRect.set(startX + trackRadius*ratio-trackRadius,
                      height - delta - trackRadius-trackRadius,
                      width - delta,
                      height - delta);
        trackPath.arcTo(trackRect, 0, 90);
        trackPath.lineTo(delta + trackRadius, height - delta);

        // 左下
        trackRect.set(delta, height - delta - trackRadius-trackRadius, delta + trackRadius+trackRadius, height - delta);
        trackPath.arcTo(trackRect, 90, 90);
        trackPath.lineTo(delta, delta + trackRadius);

        // 左上
        trackRect.set(delta, delta, delta + trackRadius+trackRadius, delta + trackRadius+trackRadius);
        trackPath.arcTo(trackRect, 180, 90);
        trackPath.lineTo(delta + trackRadius * (1+ratio), delta);

        pathMeasure.setPath(trackPath, false);
        totalLength = pathMeasure.getLength();

    }

    protected void initPainters() {
        // 轨道画笔
        backgroundPaint = new Paint();
        backgroundPaint.setColor(trackBackgroundColor);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(trackWidth);
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setStrokeCap(Paint.Cap.ROUND);

        progressPaint = new Paint();
        progressPaint.setColor(trackProgressColor);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setAntiAlias(true);
        progressPaint.setStrokeWidth(trackWidth);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        strokePaint = new Paint();
        strokePaint.setAntiAlias(true);
        strokePaint.setColor(trackStrokeColor);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(trackStrokeWidth + trackWidth);
        strokePaint.setStrokeCap(Paint.Cap.ROUND);

    }

    protected void initByAttributes(TypedArray attributes) {
        // 轨道的属性
        trackBackgroundColor = attributes.getColor(R.styleable.TrackView_track_background_color,
                                                   default_finished_color);
        trackProgressColor = attributes.getColor(R.styleable.TrackView_track_progress_color,
                                                 default_unfinished_color);
        trackStrokeColor = attributes.getColor(R.styleable.TrackView_track_stroke_color,
                                               default_finished_color);
        trackStrokeWidth = attributes.getDimension(R.styleable.TrackView_track_stroke_width,
                                                   default_stroke_width);
        trackWidth = attributes.getDimension(R.styleable.TrackView_track_width,
                                             default_stroke_width);
        trackRadius = attributes.getDimension(R.styleable.TrackView_track_radius,
                                              default_track_radius);
        ratio = attributes.getFloat(R.styleable.TrackView_track_ratio,1f);

    }

    @Override
    public void invalidate() {
        initPainters();
        super.invalidate();
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(@FloatRange(from = 0.0f,
                                        to = 1.0f) float progress)
    {
        this.progress = progress;
        invalidate();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measure(widthMeasureSpec), measure(heightMeasureSpec));
    }

    private int measure(int measureSpec) {
        int result;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        } else {
            result = min_size;
            if (mode == MeasureSpec.AT_MOST) {
                result = Math.min(result, size);
            }
        }
        return result;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;

        initPath();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 背景track
        canvas.drawPath(trackPath, strokePaint);
        canvas.drawPath(trackPath, backgroundPaint);

//        在android KITKAT版本及之前的版本，该方法所获取到的path是无法被绘制显示出来的。要解决该问题的简单方法就是在所要获取的path上执行lineTo(0, 0) 或执行rLineTo(0, 0);
        destPath.reset();
        destPath.lineTo(0,0);
        pathMeasure.getSegment(0, totalLength * progress, destPath, true);
        canvas.drawPath(destPath, progressPaint);


    }

    public float getTotalLength() {
        return totalLength;
    }

    public Path getTrackPath() {
        return trackPath;
    }

    public int getTrackWidth() {
        return (int) (trackStrokeWidth*2+trackWidth);
    }
}
