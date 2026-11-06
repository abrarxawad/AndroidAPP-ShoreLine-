package com.shoreline.app.ui.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * SYLLABUS: Custom View 2 - Nearby Vessel Radar View
 *
 * Demonstrates:
 * 1. Subclassing android.view.View with custom Canvas 2D graphics.
 * 2. Custom onMeasure and onDraw lifecycle implementations.
 * 3. Paint, SweepGradient, and RadialGradient shaders.
 * 4. 60 FPS rotating radar sweep animation via ValueAnimator.
 * 5. Dynamic rendering of peer river launches and pulsing SOS distress beacons.
 */
public class NearbyRadarView extends View {

    public static class RadarBlip {
        public final String id;
        public final String label;
        public final float distanceRatio; // 0.1 to 0.9 of radar radius
        public final float angleDeg;       // 0 to 360
        public final boolean isDistress;
        public float pulseAlpha = 1.0f;

        public RadarBlip(String id, String label, float distanceRatio, float angleDeg, boolean isDistress) {
            this.id = id;
            this.label = label;
            this.distanceRatio = distanceRatio;
            this.angleDeg = angleDeg;
            this.isDistress = isDistress;
        }
    }

    private Paint backgroundPaint;
    private Paint ringPaint;
    private Paint sweepPaint;
    private Paint crosshairPaint;
    private Paint peerBlipPaint;
    private Paint distressBlipPaint;
    private Paint textPaint;

    private float sweepAngle = 0f;
    private float pulseRadius = 0f;
    private ValueAnimator sweepAnimator;
    private ValueAnimator pulseAnimator;

    private final Map<String, RadarBlip> blipMap = new HashMap<>();
    private final Random random = new Random();

    public NearbyRadarView(Context context) {
        super(context);
        init();
    }

    public NearbyRadarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NearbyRadarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Deep maritime radar background (Frosted translucent aquatic base)
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(Color.parseColor("#6601141B"));
        backgroundPaint.setStyle(Paint.Style.FILL);

        // Concentric range rings (Luminous neon cyan)
        ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ringPaint.setColor(Color.parseColor("#4000E5FF"));
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(2.0f);

        // Crosshairs (Soft aquatic mint)
        crosshairPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        crosshairPaint.setColor(Color.parseColor("#3300C9B7"));
        crosshairPaint.setStyle(Paint.Style.STROKE);
        crosshairPaint.setStrokeWidth(1.2f);

        // Rotating radar sweep
        sweepPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sweepPaint.setStyle(Paint.Style.FILL);

        // Normal peer launch blip (Cyan)
        peerBlipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        peerBlipPaint.setColor(Color.parseColor("#00E5FF"));
        peerBlipPaint.setStyle(Paint.Style.FILL);

        // SOS Distress launch blip (Glowing Crimson)
        distressBlipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        distressBlipPaint.setColor(Color.parseColor("#FF1744"));
        distressBlipPaint.setStyle(Paint.Style.FILL);

        // Label typography
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#E0FBFC"));
        textPaint.setTextSize(24f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        setupAnimators();
    }

    private void setupAnimators() {
        // Continuous 360-degree radar beam rotation
        sweepAnimator = ValueAnimator.ofFloat(0f, 360f);
        sweepAnimator.setDuration(3500);
        sweepAnimator.setRepeatCount(ValueAnimator.INFINITE);
        sweepAnimator.setInterpolator(new LinearInterpolator());
        sweepAnimator.addUpdateListener(animation -> {
            sweepAngle = (float) animation.getAnimatedValue();
            invalidate();
        });

        // Pulsing halo for SOS distress alerts
        pulseAnimator = ValueAnimator.ofFloat(0f, 1f);
        pulseAnimator.setDuration(1200);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.addUpdateListener(animation -> {
            pulseRadius = (float) animation.getAnimatedValue();
            invalidate();
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (sweepAnimator != null && !sweepAnimator.isRunning()) {
            sweepAnimator.start();
        }
        if (pulseAnimator != null && !pulseAnimator.isRunning()) {
            pulseAnimator.start();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (sweepAnimator != null) sweepAnimator.cancel();
        if (pulseAnimator != null) pulseAnimator.cancel();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size = Math.min(width > 0 ? width : 500, height > 0 ? height : 500);
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float radius = Math.min(cx, cy) - 16f;
        if (radius <= 0) return;

        // 1. Draw Radar Dark Canvas & Outer Border
        canvas.drawCircle(cx, cy, radius, backgroundPaint);
        canvas.drawCircle(cx, cy, radius, ringPaint);

        // 2. Draw Concentric Range Rings (3 levels: 50m, 100m, 200m)
        canvas.drawCircle(cx, cy, radius * 0.33f, ringPaint);
        canvas.drawCircle(cx, cy, radius * 0.66f, ringPaint);

        // 3. Draw Crosshair Grid
        canvas.drawLine(cx - radius, cy, cx + radius, cy, crosshairPaint);
        canvas.drawLine(cx, cy - radius, cx, cy + radius, crosshairPaint);

        // 4. Draw Rotating Radar Sweep
        int[] sweepColors = new int[]{
                Color.TRANSPARENT,
                Color.parseColor("#0A00E5FF"),
                Color.parseColor("#3300E5FF"),
                Color.parseColor("#8000E5FF")
        };
        float[] positions = new float[]{0.0f, 0.7f, 0.85f, 1.0f};

        SweepGradient sweepGradient = new SweepGradient(cx, cy, sweepColors, positions);
        canvas.save();
        canvas.rotate(sweepAngle, cx, cy);
        sweepPaint.setShader(sweepGradient);
        canvas.drawCircle(cx, cy, radius, sweepPaint);
        canvas.restore();

        // 5. Center Marker (Own Vessel)
        distressBlipPaint.setColor(Color.parseColor("#FFFFFF"));
        canvas.drawCircle(cx, cy, 7f, distressBlipPaint);

        // 6. Draw Detected Nearby Vessels (Peer Blips & SOS Beacons)
        for (RadarBlip blip : blipMap.values()) {
            double rad = Math.toRadians(blip.angleDeg);
            float bx = cx + (float) (Math.cos(rad) * (radius * blip.distanceRatio));
            float by = cy + (float) (Math.sin(rad) * (radius * blip.distanceRatio));

            if (blip.isDistress) {
                // Pulsing red danger halo
                distressBlipPaint.setColor(Color.parseColor("#FF1744"));
                distressBlipPaint.setAlpha((int) ((1f - pulseRadius) * 200));
                canvas.drawCircle(bx, by, 12f + (pulseRadius * 24f), distressBlipPaint);

                distressBlipPaint.setAlpha(255);
                canvas.drawCircle(bx, by, 11f, distressBlipPaint);

                textPaint.setColor(Color.parseColor("#FF5252"));
                canvas.drawText("🚨 " + blip.label, bx, by + 32f, textPaint);
            } else {
                // Calm peer vessel cyan blip
                peerBlipPaint.setColor(Color.parseColor("#00E5FF"));
                canvas.drawCircle(bx, by, 8f, peerBlipPaint);

                textPaint.setColor(Color.parseColor("#80DEEA"));
                canvas.drawText("⛵ " + blip.label, bx, by + 28f, textPaint);
            }
        }
    }

    /**
     * Adds or updates a detected river vessel on the radar display.
     */
    public void addOrUpdatePeer(String endpointId, String name, boolean isDistress) {
        if (!blipMap.containsKey(endpointId)) {
            // Assign a natural semi-random radial position
            float distRatio = 0.35f + (random.nextFloat() * 0.45f);
            float angle = random.nextFloat() * 360f;
            blipMap.put(endpointId, new RadarBlip(endpointId, name, distRatio, angle, isDistress));
        } else {
            RadarBlip existing = blipMap.get(endpointId);
            if (existing != null) {
                blipMap.put(endpointId, new RadarBlip(endpointId, name, existing.distanceRatio, existing.angleDeg, isDistress));
            }
        }
        postInvalidate();
    }

    public void removePeer(String endpointId) {
        blipMap.remove(endpointId);
        postInvalidate();
    }

    public void clearPeers() {
        blipMap.clear();
        postInvalidate();
    }

    public int getPeerCount() {
        return blipMap.size();
    }
}
