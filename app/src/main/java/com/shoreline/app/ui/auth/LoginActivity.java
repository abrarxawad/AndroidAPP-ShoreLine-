package com.shoreline.app.ui.auth;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.shoreline.app.R;
import com.shoreline.app.data.local.entity.UserEntity;
import com.shoreline.app.data.remote.AuthRepository;
import com.shoreline.app.databinding.ActivityLoginBinding;
import com.shoreline.app.ui.base.BaseActivity;
import com.shoreline.app.ui.home.HomeActivity;
import com.shoreline.app.ui.lifecycle.LifecycleLabActivity;
import com.shoreline.app.util.LocaleHelper;

/**
 * SYLLABUS: User Authentication & Activity Flow (Login Screen)
 * Demonstrates ViewBinding, form validation, asynchronous repository callbacks,
 * runtime language switching (Bangla/English), and seamless fallback to offline demo modes.
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

        setupLanguageToggle();
        setupClickListeners();
    }

    private void setupLanguageToggle() {
        boolean isBangla = LocaleHelper.isBangla(this);
        if (isBangla) {
            binding.toggleLanguage.check(R.id.btnLangBn);
            updateToggleStyles(true);
        } else {
            binding.toggleLanguage.check(R.id.btnLangEn);
            updateToggleStyles(false);
        }

        binding.toggleLanguage.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.btnLangBn) {
                if (!LocaleHelper.isBangla(this)) {
                    LocaleHelper.setLocale(this, LocaleHelper.LANG_BANGLA);
                    restartForLanguageChange();
                }
            } else if (checkedId == R.id.btnLangEn) {
                if (LocaleHelper.isBangla(this)) {
                    LocaleHelper.setLocale(this, LocaleHelper.LANG_ENGLISH);
                    restartForLanguageChange();
                }
            }
        });
    }

    private void updateToggleStyles(boolean isBangla) {
        int activeColor = ContextCompat.getColor(this, R.color.navara_neon_cyan);
        int inactiveColor = ContextCompat.getColor(this, R.color.navara_text_subtitle);
        int activeBorder = ContextCompat.getColor(this, R.color.navara_neon_cyan);
        int inactiveBorder = ContextCompat.getColor(this, R.color.navara_glass_border);

        if (isBangla) {
            binding.btnLangBn.setTextColor(activeColor);
            binding.btnLangBn.setStrokeColor(ColorStateList.valueOf(activeBorder));
            binding.btnLangEn.setTextColor(inactiveColor);
            binding.btnLangEn.setStrokeColor(ColorStateList.valueOf(inactiveBorder));
        } else {
            binding.btnLangEn.setTextColor(activeColor);
            binding.btnLangEn.setStrokeColor(ColorStateList.valueOf(activeBorder));
            binding.btnLangBn.setTextColor(inactiveColor);
            binding.btnLangBn.setStrokeColor(ColorStateList.valueOf(inactiveBorder));
        }
    }

    private void restartForLanguageChange() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
        overridePendingTransition(0, 0);
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
