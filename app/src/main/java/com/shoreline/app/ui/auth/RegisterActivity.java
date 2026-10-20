package com.shoreline.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;

import com.shoreline.app.R;
import com.shoreline.app.data.local.entity.UserEntity;
import com.shoreline.app.data.remote.AuthRepository;
import com.shoreline.app.databinding.ActivityRegisterBinding;
import com.shoreline.app.ui.base.BaseActivity;
import com.shoreline.app.ui.home.HomeActivity;

/**
 * SYLLABUS: User Registration & Role Assignment
 * Collects traveler credentials, assigns role, and provisions offline session.
 */
public class RegisterActivity extends BaseActivity {

    private ActivityRegisterBinding binding;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authRepository = new AuthRepository(this);

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.btnDoRegister.setOnClickListener(v -> attemptRegister());

        binding.tvGoToLogin.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String fullName = binding.etRegFullName.getText() != null ? binding.etRegFullName.getText().toString().trim() : "";
        String email = binding.etRegEmail.getText() != null ? binding.etRegEmail.getText().toString().trim() : "";
        String password = binding.etRegPassword.getText() != null ? binding.etRegPassword.getText().toString().trim() : "";
        String phone = binding.etRegPhone.getText() != null ? binding.etRegPhone.getText().toString().trim() : "";
        String emergency = binding.etRegEmergency.getText() != null ? binding.etRegEmergency.getText().toString().trim() : "";

        // Determine selected role from RadioGroup
        String role = (binding.rgRole.getCheckedRadioButtonId() == R.id.rbAdmin) ? "ADMIN" : "PASSENGER";

        // SYLLABUS: Comprehensive Form Validation
        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            showToast(getString(R.string.msg_empty_fields));
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast(getString(R.string.msg_invalid_email));
            return;
        }

        if (password.length() < 6) {
            showToast(getString(R.string.msg_weak_password));
            return;
        }

        if (TextUtils.isEmpty(emergency)) {
            emergency = "999"; // Fallback to Bangladesh National Emergency Service
        }

        setLoading(true);
        authRepository.register(email, password, fullName, phone, role, emergency, new AuthRepository.AuthCallback() {
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
        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean isLoading) {
        binding.regProgress.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnDoRegister.setEnabled(!isLoading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
