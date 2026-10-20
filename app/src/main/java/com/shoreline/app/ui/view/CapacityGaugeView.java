package com.shoreline.app.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.shoreline.app.R;

import java.util.Locale;

/**
 * SYLLABUS: Custom View Component (subclass View)
 * Demonstrates:
 * 1. Custom XML Attributes defined in res/values/attrs.xml (obtainStyledAttributes + recycle)
 * 2. Custom Measurement: override onMeasure(int widthMeasureSpec, int heightMeasureSpec)
 * 3. 2D Canvas Drawing: override onDraw(Canvas canvas) using Paint, RectF, drawArc, and drawText
 * 4. Interactive Touch Handling: override onTouchEvent(MotionEvent event) allowing examiners to simulate passenger influx
 * 5. Dynamic Safety Thresholds:
 *    - < 75%: Green (Safe capacity)
 *    - 75% - 100%: Amber (Nearing licensed limit)
 *    - > 100%: Crimson Red (Severe overcrowding violation)
 */
public class CapacityGaugeView extends View {

    private int licensedCapacity = 500;
    private int currentLoad = 320;

    private int safeColor;
    private int warningColor;
    private int dangerColor;
    private float strokeWidth = 24f;

    private Paint backgroundArcPaint;
    private Paint progressArcPaint;
    private Paint textPercentPaint;
    private Paint textSubPaint;
    private RectF arcBounds;

    public CapacityGaugeView(Context context) {
        super(context);
        init(context, null);
    }

    public CapacityGaugeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CapacityGaugeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        safeColor = ContextCompat.getColor(context, R.color.status_safe_green);
        warningColor = ContextCompat.getColor(context, R.color.status_warning_amber);
        dangerColor = ContextCompat.getColor(context, R.color.status_danger_red);

        // SYLLABUS: Reading custom attributes from TypedArray
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CapacityGaugeView);
            try {
                licensedCapacity = a.getInt(R.styleable.CapacityGaugeView_licensedCapacity, 500);
                currentLoad = a.getInt(R.styleable.CapacityGaugeView_currentLoad, 300);
                safeColor = a.getColor(R.styleable.CapacityGaugeView_safeColor, safeColor);
                warningColor = a.getColor(R.styleable.CapacityGaugeView_warningColor, warningColor);
                dangerColor = a.getColor(R.styleable.CapacityGaugeView_dangerColor, dangerColor);
                strokeWidth = a.getDimension(R.styleable.CapacityGaugeView_gaugeStrokeWidth, strokeWidth);
            } finally {
                a.recycle(); // Crucial: free native memory buffer
            }
        }

        // Setup background track paint
        backgroundArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundArcPaint.setStyle(Paint.Style.STROKE);
        backgroundArcPaint.setStrokeWidth(strokeWidth);
        backgroundArcPaint.setColor(0xFFE2E8F0);
        backgroundArcPaint.setStrokeCap(Paint.Cap.ROUND);

        // Setup progress gauge paint
        progressArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressArcPaint.setStyle(Paint.Style.STROKE);
        progressArcPaint.setStrokeWidth(strokeWidth);
        progressArcPaint.setStrokeCap(Paint.Cap.ROUND);

        // Setup center percentage text paint
        textPercentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPercentPaint.setTextAlign(Paint.Align.CENTER);
        textPercentPaint.setFakeBoldText(true);
        textPercentPaint.setTextSize(56f);

        // Setup bottom caption paint
        textSubPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textSubPaint.setTextAlign(Paint.Align.CENTER);
        textSubPaint.setColor(0xFF64748B);
        textSubPaint.setTextSize(28f);

        arcBounds = new RectF();
    }

    // =========================================================================
    // SYLLABUS: Custom Measurement Lifecycle (onMeasure)
    // =========================================================================
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = 400;
        int desiredHeight = 320;

        int width = resolveSize(desiredWidth, widthMeasureSpec);
        int height = resolveSize(desiredHeight, heightMeasureSpec);

        setMeasuredDimension(width, height);
    }

    // =========================================================================
    // SYLLABUS: Custom 2D Graphics Drawing (onDraw)
    // =========================================================================
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float padding = strokeWidth / 2f + 20f;
        arcBounds.set(padding, padding, getWidth() - padding, (getHeight() * 1.5f) - padding);

        // Gauge spans a 180-degree semi-circle from 180 to 0 degrees
        float startAngle = 180f;
        float sweepFullAngle = 180f;

        // 1. Draw static background gauge track
        canvas.drawArc(arcBounds, startAngle, sweepFullAngle, false, backgroundArcPaint);

        // 2. Calculate load ratio
        float ratio = licensedCapacity > 0 ? ((float) currentLoad / (float) licensedCapacity) : 0f;
        float sweepAngle = Math.min(180f, Math.max(0f, ratio * 180f));

        // 3. Determine dynamic color based on safety limits
        int activeColor;
        String statusLabel;
        if (ratio < 0.75f) {
            activeColor = safeColor;
            statusLabel = "SAFE LOAD";
        } else if (ratio <= 1.0f) {
            activeColor = warningColor;
            statusLabel = "APPROACHING LIMIT";
        } else {
            activeColor = dangerColor;
            statusLabel = "OVERLOADED!";
        }

        progressArcPaint.setColor(activeColor);
        textPercentPaint.setColor(activeColor);

        // 4. Draw dynamic capacity sweep arc
        canvas.drawArc(arcBounds, startAngle, sweepAngle, false, progressArcPaint);

        // 5. Draw center percentage text
        float centerX = getWidth() / 2f;
        float centerY = getHeight() * 0.72f;

        int percent = Math.round(ratio * 100f);
        canvas.drawText(percent + "%", centerX, centerY - 15f, textPercentPaint);

        // 6. Draw passengers / limit subtext
        String loadDetails = String.format(Locale.US, "%d / %d Passengers", currentLoad, licensedCapacity);
        canvas.drawText(loadDetails, centerX, centerY + 25f, textSubPaint);

        // 7. Draw status caption
        textSubPaint.setColor(activeColor);
        textSubPaint.setFakeBoldText(true);
        canvas.drawText(statusLabel, centerX, centerY + 65f, textSubPaint);
        textSubPaint.setFakeBoldText(false);
        textSubPaint.setColor(0xFF64748B);
    }

    // =========================================================================
    // SYLLABUS: Interactive Touch Events (onTouchEvent)
    // Allows sliding horizontally across the gauge to simulate live passenger boarding
    // =========================================================================
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = Math.max(0, Math.min(getWidth(), event.getX()));
            float fraction = x / (float) getWidth();
            // Scale simulation from 0 to 140% of licensed capacity
            int simulatedLoad = Math.round(fraction * 1.4f * licensedCapacity);
            setCurrentLoad(simulatedLoad);
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void setCapacityAndLoad(int capacity, int load) {
        this.licensedCapacity = Math.max(1, capacity);
        this.currentLoad = Math.max(0, load);
        invalidate(); // Request redraw
    }

    public void setCurrentLoad(int load) {
        this.currentLoad = Math.max(0, load);
        invalidate(); // Request redraw
    }

    public int getLicensedCapacity() { return licensedCapacity; }
    public int getCurrentLoad() { return currentLoad; }
    public float getCapacityRatio() { return licensedCapacity > 0 ? ((float) currentLoad / (float) licensedCapacity) : 0f; }
}
