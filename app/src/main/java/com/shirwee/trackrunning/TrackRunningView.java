package com.shirwee.trackrunning;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 轨道赛跑
 *
 * @author shirwee
 */
public class TrackRunningView
        extends FrameLayout {
    private final int default_image_width;
    private List<View> views = new ArrayList<>();
    private List<Item> items = new ArrayList<>();

    private TrackView   trackView;
    private PathMeasure mPathMeasure;
    private float[] mCurrentPosition = new float[2];
    private int max = 100;


    public TrackRunningView(Context context) {
        this(context, null);
    }

    public TrackRunningView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TrackRunningView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        default_image_width = (int) Utils.dp2px(getResources(), 36);
        initUI();
    }

    public void setItems(@NonNull List<Item> items) {
        this.items = items;
        initUI();
    }

    public void initUI() {
        views.clear();
        removeAllViews();

        inflate(getContext(), R.layout.layout_track_view, this);
        trackView = (TrackView) findViewById(R.id.track_view);

        for (int i = 0; i < items.size(); i++) {
            CircleImageView view = new CircleImageView(getContext());
            view.setLayoutParams(new LayoutParams(default_image_width,
                    default_image_width));
            view.setBorderColor(0xFFFFFFFF);
            view.setBorderWidth((int) Utils.dp2px(getResources(), 3));
            view.setImageResource(R.mipmap.ic_launcher_round);
            view.setTag(items.get(i).getId());
            views.add(view);
            addView(view);
        }

        ViewTreeObserver observer = trackView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onGlobalLayout() {
                trackView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // 初始化图片的位置
                Path dest = new Path();
                Path path = trackView.getTrackPath();
                path.offset(trackView.getLeft() - default_image_width / 2, trackView.getTop() - default_image_width / 2, dest);
                mPathMeasure = new PathMeasure(dest, false);
                mPathMeasure.getPosTan(0, mCurrentPosition, null);

                for (View view : views) {
                    view.setTranslationX(mCurrentPosition[0]);
                    view.setTranslationY(mCurrentPosition[1]);
                }
            }
        });


    }

    /**
     * 设置总分数
     */
    public void setMax(int max) {
        this.max = max;
    }

    /**
     * refresh single item
     */
    public void refreshProgress(int id, int target) {
        boolean isRefresh = false;
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            if (id == item.getId()) {
                float targetProgress = target * 1.0f / max;
                if (targetProgress > trackView.getProgress()) {
                    isRefresh = true;
                } else {
                    isRefresh = false;
                }
                startAnimator(item.getCurrent(), target, views.get(i), isRefresh);
                // 记录当前位置
                item.setCurrent(target);
                break;
            }
        }

    }

    /**
     * refresh all item
     */
    public void refreshProgress(@NonNull List<Item> items) {
        boolean isRefresh = false;
        Item maxItem = Collections.max(items);
        for (int i = 0;i<items.size();i++) {
            Item item = items.get(i);
            if (maxItem.equals(item)) {
                isRefresh = true;
            }else {
                isRefresh = false;
            }
            startAnimator(this.items.get(i).getCurrent(),item.getTarget(),views.get(i),isRefresh);
            this.items.get(i).setCurrent(item.getTarget());
        }

    }


    /**
     * 计算每次的位置
     */
    public void startAnimator(int current, int target, final View view, final boolean isRefresh) {

        float currentLength = trackView.getTotalLength() * (current * 1.0f / max);
        float targetLength = trackView.getTotalLength() * (target * 1.0f / max);

        final float currentProgressLength = trackView.getTotalLength()*trackView.getProgress();

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(currentLength, targetLength);
        valueAnimator.setDuration(1000);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float currentPath = (float) animation.getAnimatedValue();
                mPathMeasure.getPosTan(currentPath, mCurrentPosition, null);

                view.setTranslationX(mCurrentPosition[0]);
                view.setTranslationY(mCurrentPosition[1]);

                if (isRefresh && currentPath > currentProgressLength) {
                    trackView.setProgress(currentPath / trackView.getTotalLength());
                }
            }
        });
        valueAnimator.start();
    }

}