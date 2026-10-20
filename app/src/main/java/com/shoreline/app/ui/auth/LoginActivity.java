package com.shoreline.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;

import com.shoreline.app.R;
import com.shoreline.app.data.local.entity.UserEntity;
import com.shoreline.app.data.remote.AuthRepository;
import com.shoreline.app.databinding.ActivityLoginBinding;
import com.shoreline.app.ui.base.BaseActivity;
import com.shoreline.app.ui.home.HomeActivity;
import com.shoreline.app.ui.lifecycle.LifecycleLabActivity;

/**
 * SYLLABUS: User Authentication & Activity Flow (Login Screen)
 * Demonstrates ViewBinding, form validation, asynchronous repository callbacks,
 * and seamless fallback to offline demo modes.
 */
public class LoginActivity extends BaseActivity {

    private ActivityLoginBinding binding;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authRepository = new AuthRepository(this);

        setupClickListeners();
    }

    private void setupClickListeners() {
        // Sign In Button
        binding.btnLogin.setOnClickListener(v -> attemptLogin());

        // Navigation to Register Screen
        binding.tvGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Instant Evaluation Demo Triggers (Works 100% offline)
        binding.btnDemoPassenger.setOnClickListener(v -> performDemoLogin("PASSENGER"));
        binding.btnDemoAdmin.setOnClickListener(v -> performDemoLogin("ADMIN"));

        // Syllabus requirement: Lifecycle Lab link
        binding.btnOpenLifecycleLab.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, LifecycleLabActivity.class);
            intent.putExtra("EXTRA_LAUNCH_TRIGGER", "LOGIN_SCREEN");
            startActivity(intent);
        });
    }

    private void attemptLogin() {
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";

        // SYLLABUS: Input Validation
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            showToast(getString(R.string.msg_empty_fields));
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast(getString(R.string.msg_invalid_email));
            return;
        }

        setLoading(true);
        authRepository.login(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(UserEntity user, String message) {
                setLoading(false);
                showToast(message);
                navigateToHome();
            }

            @Override
            public void onError(String errorMessage) {
                setLoading(false);
                showSnackbar(binding.getRoot(), errorMessage);
            }
        });
    }

    private void performDemoLogin(String role) {
        setLoading(true);
        authRepository.loginAsGuest(role, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(UserEntity user, String message) {
                setLoading(false);
                showToast(message);
                navigateToHome();
            }

            @Override
            public void onError(String errorMessage) {
                setLoading(false);
                showSnackbar(binding.getRoot(), errorMessage);
            }
        });
    }

    private void navigateToHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean isLoading) {
        binding.loginProgress.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!isLoading);
        binding.btnDemoPassenger.setEnabled(!isLoading);
        binding.btnDemoAdmin.setEnabled(!isLoading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
