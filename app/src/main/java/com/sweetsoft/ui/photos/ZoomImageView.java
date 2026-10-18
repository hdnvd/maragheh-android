package com.sweetsoft.ui.photos;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

/** Pinch-to-zoom, pan and double-tap image view that plays nicely inside a ViewPager2. */
public class ZoomImageView extends AppCompatImageView {
    private static final float MAX_SCALE = 5f;

    private final Matrix base = new Matrix();
    private final Matrix zoom = new Matrix();
    private final Matrix draw = new Matrix();
    private final float[] values = new float[9];
    private final ScaleGestureDetector scaleDetector;
    private final GestureDetector gestureDetector;

    public ZoomImageView(Context context) {
        this(context, null);
    }

    public ZoomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setScaleType(ScaleType.MATRIX);
        scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(@NonNull ScaleGestureDetector d) {
                float current = currentScale();
                float factor = d.getScaleFactor();
                float target = Math.max(1f, Math.min(current * factor, MAX_SCALE));
                factor = target / current;
                zoom.postScale(factor, factor, d.getFocusX(), d.getFocusY());
                apply();
                return true;
            }
        });
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, @NonNull MotionEvent e2, float dx, float dy) {
                if (currentScale() <= 1.01f) return false;
                zoom.postTranslate(-dx, -dy);
                apply();
                return true;
            }

            @Override
            public boolean onDoubleTap(@NonNull MotionEvent e) {
                if (currentScale() > 1.01f) {
                    zoom.reset();
                } else {
                    zoom.postScale(2.5f, 2.5f, e.getX(), e.getY());
                }
                apply();
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
                return performClick();
            }
        });
    }

    private float currentScale() {
        zoom.getValues(values);
        return values[Matrix.MSCALE_X];
    }

    public boolean isZoomed() {
        return currentScale() > 1.01f;
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        zoom.reset();
        fitBase();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        fitBase();
    }

    private void fitBase() {
        Drawable d = getDrawable();
        if (d == null || getWidth() == 0 || getHeight() == 0) return;
        RectF src = new RectF(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        RectF dst = new RectF(0, 0, getWidth(), getHeight());
        base.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER);
        apply();
    }

    /** Keeps the image from being dragged away from the edges. */
    private void apply() {
        Drawable d = getDrawable();
        if (d == null) return;
        draw.set(base);
        draw.postConcat(zoom);
        RectF r = new RectF(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        draw.mapRect(r);
        float dx = 0, dy = 0;
        if (r.width() <= getWidth()) dx = (getWidth() - r.width()) / 2f - r.left;
        else if (r.left > 0) dx = -r.left;
        else if (r.right < getWidth()) dx = getWidth() - r.right;
        if (r.height() <= getHeight()) dy = (getHeight() - r.height()) / 2f - r.top;
        else if (r.top > 0) dy = -r.top;
        else if (r.bottom < getHeight()) dy = getHeight() - r.bottom;
        zoom.postTranslate(dx, dy);
        draw.postTranslate(dx, dy);
        setImageMatrix(draw);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        // While zoomed, stop the pager from stealing horizontal drags.
        getParent().requestDisallowInterceptTouchEvent(isZoomed() || event.getPointerCount() > 1);
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
