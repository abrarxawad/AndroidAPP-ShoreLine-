package com.shoreline.app.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.shoreline.app.databinding.ActivitySplashBinding;
import com.shoreline.app.ui.auth.LoginActivity;
import com.shoreline.app.ui.base.BaseActivity;
import com.shoreline.app.ui.home.HomeActivity;
import com.shoreline.app.ui.lifecycle.LifecycleLabActivity;
import com.shoreline.app.util.SessionManager;

/**
 * SYLLABUS: Multi-Activity Flow & Session Router (Splash Screen)
 * Evaluates active session state and routes to either HomeActivity or LoginActivity.
 */
public class SplashActivity extends BaseActivity {

    private static final int SPLASH_DELAY_MS = 2000;

    private ActivitySplashBinding binding;
    private final Handler splashHandler = new Handler(Looper.getMainLooper());
    private Runnable navigationRunnable;
    private boolean hasNavigated = false;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        binding.btnOpenLifecycleLab.setOnClickListener(v -> navigateToLifecycleLab());

        navigationRunnable = () -> {
            if (!isFinishing() && !isDestroyed()) {
                routeNextScreen();
            }
        };

        splashHandler.postDelayed(navigationRunnable, SPLASH_DELAY_MS);
    }

    private synchronized void routeNextScreen() {
        if (hasNavigated) {
            return;
        }
        hasNavigated = true;

        if (navigationRunnable != null) {
            splashHandler.removeCallbacks(navigationRunnable);
        }

        Intent intent;
        if (sessionManager.isLoggedIn()) {
            intent = new Intent(SplashActivity.this, HomeActivity.class);
        } else {
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }
        startActivity(intent);
        finish();
    }

    private synchronized void navigateToLifecycleLab() {
        if (hasNavigated) {
            return;
        }
        hasNavigated = true;

        if (navigationRunnable != null) {
            splashHandler.removeCallbacks(navigationRunnable);
        }

        Intent intent = new Intent(SplashActivity.this, LifecycleLabActivity.class);
        intent.putExtra("EXTRA_LAUNCH_TRIGGER", "SPLASH_BUTTON");
        intent.putExtra("EXTRA_LAUNCH_TIMESTAMP", System.currentTimeMillis());
        intent.putExtra("EXTRA_APP_VERSION", "1.0.0");
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (navigationRunnable != null) {
            splashHandler.removeCallbacks(navigationRunnable);
        }
        binding = null;
    }
}
