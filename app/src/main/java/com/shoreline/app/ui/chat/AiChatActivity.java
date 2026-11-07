package com.shoreline.app.ui.chat;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.shoreline.app.R;
import com.shoreline.app.data.model.ChatMessage;
import com.shoreline.app.network.AiChatApiClient;
import com.shoreline.app.ui.base.BaseActivity;
import com.shoreline.app.ui.report.ReportOverloadActivity;
import com.shoreline.app.ui.route.SafeRouteActivity;
import com.shoreline.app.ui.sos.OfflineSosActivity;
import com.shoreline.app.ui.tracker.FamilyTrackerActivity;
import com.shoreline.app.ui.vessel.VesselCheckActivity;
import com.shoreline.app.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class AiChatActivity extends BaseActivity implements ChatAdapter.OnActionClickListener {

    private RecyclerView rvChatMessages;
    private EditText etMessageInput;
    private MaterialButton btnSendMessage;
    private LinearLayout layoutTypingIndicator;
    private TextView tvAiStatusSubtitle;
    private ImageView btnApiKeySettings;

    private ChatAdapter chatAdapter;
    private final List<ChatMessage> messageList = new ArrayList<>();
    private AiChatApiClient aiApiClient;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        sessionManager = new SessionManager(this);
        aiApiClient = new AiChatApiClient(this);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSuggestionChips();
        seedInitialWelcomeMessage();
        updateStatusSubtitle();
    }

    private void initViews() {
        rvChatMessages = findViewById(R.id.rvChatMessages);
        etMessageInput = findViewById(R.id.etMessageInput);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        layoutTypingIndicator = findViewById(R.id.layoutTypingIndicator);
        tvAiStatusSubtitle = findViewById(R.id.tvAiStatusSubtitle);
        btnApiKeySettings = findViewById(R.id.btnApiKeySettings);

        btnSendMessage.setOnClickListener(v -> submitUserMessage());

        etMessageInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                submitUserMessage();
                return true;
            }
            return false;
        });

        btnApiKeySettings.setOnClickListener(v -> showApiKeyDialog());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarChat);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter(messageList, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvChatMessages.setLayoutManager(layoutManager);
        rvChatMessages.setAdapter(chatAdapter);
    }

    private void setupSuggestionChips() {
        MaterialButton chipOverload = findViewById(R.id.chipOverload);
        MaterialButton chipWeather = findViewById(R.id.chipWeather);
        MaterialButton chipTracking = findViewById(R.id.chipTracking);
        MaterialButton chipEmergency = findViewById(R.id.chipEmergency);
        MaterialButton chipVessel = findViewById(R.id.chipVessel);

        chipOverload.setOnClickListener(v -> sendMessage("The launch is severely overloaded with too many passengers. What should I do?"));
        chipWeather.setOnClickListener(v -> sendMessage("Is it safe to travel in this weather? How does ShoreLine calculate safe river routes?"));
        chipTracking.setOnClickListener(v -> sendMessage("How can I track my launch and share the Voyage Code with my family?"));
        chipEmergency.setOnClickListener(v -> sendMessage("EMERGENCY: Sinking or fire on board! What are the immediate life-saving steps?"));
        chipVessel.setOnClickListener(v -> sendMessage("How do I check if my launch has a valid BIWTA fitness certificate?"));
    }

    private void seedInitialWelcomeMessage() {
        String welcome = "👋 **Welcome aboard ShoreLine AI Copilot (শোরলাইন নদীপথ সহকারী)!**\n\n" +
                "I am your 24/7 inland waterway safety copilot. Ask me anything about:\n" +
                "• 🚨 Handling overcrowded launches & filing BIWTA evidence\n" +
                "• 🌧️ Cyclone alerts, river waves & safe route navigation\n" +
                "• 📍 Sharing your live launch voyage with relatives\n" +
                "• 🆘 Immediate emergency distress protocols & hotlines\n" +
                "• 🚢 Vessel fitness, capacity limits & QR verification";

        ChatMessage intro = new ChatMessage(welcome, ChatMessage.TYPE_AI);
        messageList.add(intro);
        chatAdapter.notifyItemInserted(0);
    }

    private void submitUserMessage() {
        String text = etMessageInput.getText().toString().trim();
        if (text.isEmpty()) return;
        etMessageInput.setText("");
        sendMessage(text);
    }

    private void sendMessage(String userText) {
        ChatMessage userMsg = new ChatMessage(userText, ChatMessage.TYPE_USER);
        messageList.add(userMsg);
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        rvChatMessages.smoothScrollToPosition(messageList.size() - 1);

        layoutTypingIndicator.setVisibility(View.VISIBLE);

        aiApiClient.sendMessage(messageList, userText, new AiChatApiClient.ChatCallback() {
            @Override
            public void onSuccess(ChatMessage aiMessage) {
                if (isFinishing() || isDestroyed()) return;
                layoutTypingIndicator.setVisibility(View.GONE);
                messageList.add(aiMessage);
                chatAdapter.notifyItemInserted(messageList.size() - 1);
                rvChatMessages.smoothScrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onError(String errorMessage) {
                if (isFinishing() || isDestroyed()) return;
                layoutTypingIndicator.setVisibility(View.GONE);
                ChatMessage errorMsg = new ChatMessage("⚠️ Error: " + errorMessage, ChatMessage.TYPE_AI);
                messageList.add(errorMsg);
                chatAdapter.notifyItemInserted(messageList.size() - 1);
                rvChatMessages.smoothScrollToPosition(messageList.size() - 1);
            }
        });
    }

    @Override
    public void onActionClick(int actionType) {
        switch (actionType) {
            case ChatMessage.ACTION_REPORT_OVERLOAD:
                startActivity(new Intent(this, ReportOverloadActivity.class));
                break;
            case ChatMessage.ACTION_OFFLINE_SOS:
                startActivity(new Intent(this, OfflineSosActivity.class));
                break;
            case ChatMessage.ACTION_SAFE_ROUTE:
                startActivity(new Intent(this, SafeRouteActivity.class));
                break;
            case ChatMessage.ACTION_FAMILY_TRACKER:
                startActivity(new Intent(this, FamilyTrackerActivity.class));
                break;
            case ChatMessage.ACTION_VESSEL_CHECK:
                startActivity(new Intent(this, VesselCheckActivity.class));
                break;
            case ChatMessage.ACTION_CALL_999:
                dialPhoneNumber("999");
                break;
            case ChatMessage.ACTION_CALL_16107:
                dialPhoneNumber("16107");
                break;
        }
    }

    private void dialPhoneNumber(String phone) {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to dial " + phone, Toast.LENGTH_SHORT).show();
        }
    }

    private void showApiKeyDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_gemini_api_key, null);
        EditText etKey = dialogView.findViewById(R.id.etApiKeyInput);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelApiKey);
        MaterialButton btnClear = dialogView.findViewById(R.id.btnClearApiKey);
        MaterialButton btnSave = dialogView.findViewById(R.id.btnSaveApiKey);

        String currentKey = sessionManager.getGeminiApiKey();
        if (!currentKey.isEmpty()) {
            etKey.setText(currentKey);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnClear.setOnClickListener(v -> {
            sessionManager.setGeminiApiKey("");
            dialog.dismiss();
            updateStatusSubtitle();
            Toast.makeText(this, "API Key cleared. Using built-in River Intelligence Engine.", Toast.LENGTH_LONG).show();
        });

        btnSave.setOnClickListener(v -> {
            String newKey = etKey.getText().toString().trim();
            if (newKey.isEmpty()) {
                Toast.makeText(this, "Please enter a valid Gemini API Key", Toast.LENGTH_SHORT).show();
                return;
            }
            sessionManager.setGeminiApiKey(newKey);
            dialog.dismiss();
            updateStatusSubtitle();
            Toast.makeText(this, "Gemini API Key activated!", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    private void updateStatusSubtitle() {
        String key = sessionManager.getGeminiApiKey();
        if (!key.isEmpty()) {
            tvAiStatusSubtitle.setText("🟢 Gemini 3.5 Flash Active • Cloud AI");
        } else {
            tvAiStatusSubtitle.setText("🟢 River Maritime Engine Active • Offline Ready");
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

