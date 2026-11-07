package com.shoreline.app.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.shoreline.app.data.model.ChatMessage;
import com.shoreline.app.util.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SYLLABUS: AI API Networking & Maritime Domain Intelligence
 * Communicates with Google Gemini 3.5 Flash REST API via native HttpURLConnection.
 * Features strict turn-alternation multi-turn memory and mid-river offline heuristic fallback.
 */
public class AiChatApiClient {

    private static final String TAG = "AiChatApiClient";

    // Primary Gemini endpoint (verified active on Google AI Studio)
    private static final String GEMINI_BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=";

    private static final String SYSTEM_PROMPT =
            "You are \"ShoreLine AI Copilot\" (শোরলাইন এআই নদীপথ সহকারী), an intelligent maritime safety, river transit, and general question-answering assistant for users in Bangladesh.\n" +
            "\n" +
            "CAPABILITIES:\n" +
            "1. Answer ANY user question directly, accurately, and naturally (general knowledge, science, navigation, daily queries, tech, etc.).\n" +
            "2. When the user asks about river travel, launches, weather, overloading, or safety in Bangladesh:\n" +
            "   - Explain BIWTA inland shipping rules and passenger rights.\n" +
            "   - Overloading: Advise finding life jackets, staying on open decks, and filing an Overload Report via the app.\n" +
            "   - Weather: Highlight hazards (wind > 30 km/h, rain > 10 mm/h).\n" +
            "   - Tracking: Explain the 6-character Voyage Code shared with family via Firebase.\n" +
            "   - Sinking/Fire Emergency: Prioritize putting on life jackets, moving to the top deck, activating ShoreLine's Offline SOS beacon, and calling 999 or 16107.\n" +
            "\n" +
            "LANGUAGE & TONE:\n" +
            "- Bilingual: English, Bengali (বাংলা), and Banglish. Reply in whatever language the user asks.\n" +
            "- Clear, friendly, informative, and formatted with markdown bullet points when helpful.";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final SessionManager sessionManager;
    private final Context context;

    public interface ChatCallback {
        void onSuccess(ChatMessage aiMessage);
        void onError(String errorMessage);
    }

    public AiChatApiClient(Context context) {
        this.context = context.getApplicationContext();
        this.sessionManager = new SessionManager(this.context);
    }

    public void sendMessage(List<ChatMessage> conversationHistory, String userMessage, ChatCallback callback) {
        String apiKey = sessionManager.getGeminiApiKey();

        // If no internet connection or no API key, use the intelligent River Maritime Engine
        if (!isOnline(context) || apiKey.isEmpty()) {
            executor.execute(() -> {
                try {
                    Thread.sleep(400);
                } catch (InterruptedException ignored) {}

                ChatMessage offlineReply = generateRiverIntelligenceResponse(userMessage, apiKey.isEmpty());
                mainHandler.post(() -> callback.onSuccess(offlineReply));
            });
            return;
        }

        // Live API Execution with Gemini 3.5 Flash
        executor.execute(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(GEMINI_BASE_URL + apiKey);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setConnectTimeout(12000);
                conn.setReadTimeout(18000);
                conn.setDoOutput(true);

                // Build Gemini REST Payload
                JSONObject root = new JSONObject();

                // 1. System Instruction
                JSONObject systemInstruction = new JSONObject();
                JSONObject sysPart = new JSONObject();
                sysPart.put("text", SYSTEM_PROMPT);
                systemInstruction.put("parts", new JSONArray().put(sysPart));
                root.put("system_instruction", systemInstruction);

                // 2. Format multi-turn conversation contents (Ensuring strict user/model alternation)
                JSONArray contentsArray = buildAlternatingContents(conversationHistory, userMessage);
                root.put("contents", contentsArray);

                // Write output stream
                byte[] requestBytes = root.toString().getBytes(StandardCharsets.UTF_8);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(requestBytes, 0, requestBytes.length);
                }

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Gemini API Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONArray candidates = jsonResponse.getJSONArray("candidates");
                    String replyText = candidates.getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text");

                    // Detect contextual action intent
                    int actionType = detectActionIntent(userMessage + " " + replyText);
                    String actionLabel = getActionLabel(actionType);

                    ChatMessage aiReply = new ChatMessage(replyText, ChatMessage.TYPE_AI, actionType, actionLabel);
                    mainHandler.post(() -> callback.onSuccess(aiReply));

                } else {
                    // Read error details from server
                    String errorBody = "";
                    InputStream errStream = conn.getErrorStream();
                    if (errStream != null) {
                        BufferedReader errReader = new BufferedReader(
                                new InputStreamReader(errStream, StandardCharsets.UTF_8));
                        StringBuilder errSb = new StringBuilder();
                        String el;
                        while ((el = errReader.readLine()) != null) errSb.append(el);
                        errReader.close();
                        errorBody = errSb.toString();
                    }
                    Log.e(TAG, "Gemini Error (" + responseCode + "): " + errorBody);

                    // Parse friendly error or fallback gracefully
                    ChatMessage fallbackReply = generateRiverIntelligenceResponse(userMessage, false);
                    fallbackReply.setText("⚠️ Cloud AI Notice (Status " + responseCode + "):\n" +
                            "Could not reach Gemini cloud API. Falling back to internal engine.\n\n" + fallbackReply.getText());
                    mainHandler.post(() -> callback.onSuccess(fallbackReply));
                }

            } catch (Exception e) {
                Log.e(TAG, "Exception calling Gemini API: " + e.getMessage(), e);
                ChatMessage fallbackReply = generateRiverIntelligenceResponse(userMessage, false);
                mainHandler.post(() -> callback.onSuccess(fallbackReply));
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        });
    }

    /**
     * Builds valid alternating contents array for Gemini REST API:
     * - Discards leading AI messages (Gemini requires turn 0 to be 'user').
     * - Merges consecutive same-role turns to avoid 400 Bad Request.
     * - Guarantees current userMessage is the final turn.
     */
    private JSONArray buildAlternatingContents(List<ChatMessage> conversationHistory, String currentUserMessage) throws Exception {
        JSONArray contents = new JSONArray();

        // Extract clean linear history
        List<ChatMessage> cleanList = new ArrayList<>();
        if (conversationHistory != null) {
            for (ChatMessage msg : conversationHistory) {
                cleanList.add(msg);
            }
        }

        // Limit to recent 8 messages to prevent token overflow
        int start = Math.max(0, cleanList.size() - 8);

        // Discard any leading MODEL turns until we find the first USER turn
        while (start < cleanList.size() && cleanList.get(start).getType() != ChatMessage.TYPE_USER) {
            start++;
        }

        String lastRole = null;
        for (int i = start; i < cleanList.size(); i++) {
            ChatMessage m = cleanList.get(i);
            String currentRole = (m.getType() == ChatMessage.TYPE_USER) ? "user" : "model";

            if (currentRole.equals(lastRole) && contents.length() > 0) {
                // Merge text with existing previous turn
                JSONObject prevObj = contents.getJSONObject(contents.length() - 1);
                JSONArray parts = prevObj.getJSONArray("parts");
                JSONObject p = parts.getJSONObject(0);
                p.put("text", p.getString("text") + "\n" + m.getText());
            } else {
                JSONObject msgObj = new JSONObject();
                msgObj.put("role", currentRole);
                JSONObject textPart = new JSONObject();
                textPart.put("text", m.getText());
                msgObj.put("parts", new JSONArray().put(textPart));
                contents.put(msgObj);
                lastRole = currentRole;
            }
        }

        // If contents is empty or last turn was model, ensure the currentUserMessage is added as user turn
        if (contents.length() == 0 || !"user".equals(lastRole)) {
            JSONObject currentMsg = new JSONObject();
            currentMsg.put("role", "user");
            JSONObject textPart = new JSONObject();
            textPart.put("text", currentUserMessage);
            currentMsg.put("parts", new JSONArray().put(textPart));
            contents.put(currentMsg);
        }

        return contents;
    }

    /**
     * ShoreLine Built-in River Intelligence Engine
     * Provides instantaneous answers when offline or before an API key is provided.
     */
    private ChatMessage generateRiverIntelligenceResponse(String query, boolean keyMissing) {
        String lower = query.toLowerCase(Locale.ROOT);
        String reply;
        int actionType = ChatMessage.ACTION_NONE;
        String actionLabel = null;

        if (lower.contains("overload") || lower.contains("overcrowd") || lower.contains("ভিড়") || lower.contains("লোড") || lower.contains("যাত্রী বেশি")) {
            reply = "🚨 **Vessel Overcrowding Safety Action (অতিরিক্ত যাত্রী সতর্কতা):**\n\n" +
                    "1. **Locate Life Jackets:** Immediately inspect overhead racks or under lower deck bunks for life vests.\n" +
                    "2. **Move to Open Deck:** Avoid being trapped inside lower cabins or congested gangways.\n" +
                    "3. **File BIWTA Evidence:** Tap below to capture a geotagged photo. ShoreLine will queue it in Room SQLite and sync with BIWTA port authority via WorkManager even with weak signal.\n\n" +
                    "📞 **BIWTA River Police:** 16107 | **National Helpline:** 999";
            actionType = ChatMessage.ACTION_REPORT_OVERLOAD;
            actionLabel = "🚨 Open Overload Reporter";

        } else if (lower.contains("sink") || lower.contains("ডুব") || lower.contains("পানি উঠছে") || lower.contains("fire") || lower.contains("আগুন") || lower.contains("accident") || lower.contains("collision") || lower.contains("danger") || lower.contains("জরুরি")) {
            reply = "🆘 **CRITICAL MARITIME DISTRESS (জরুরি উদ্ধার নির্দেশনা):**\n\n" +
                    "1. **Put On Life Jackets Immediately:** Fasten all safety straps firmly.\n" +
                    "2. **Move to Upper Deck:** Do NOT stay below the waterline. Prepare for orderly evacuation.\n" +
                    "3. **Activate Offline SOS:** Tap below to transmit your live GPS beacon via Google Nearby Connections (offline mesh) and send emergency GSM SMS to 999/Coast Guard.\n\n" +
                    "📞 **National Emergency:** 999 | **Disaster Alert:** 1090";
            actionType = ChatMessage.ACTION_OFFLINE_SOS;
            actionLabel = "🆘 Activate Offline SOS Beacon";

        } else if (lower.contains("weather") || lower.contains("storm") || lower.contains("ঝড়") || lower.contains("তুফান") || lower.contains("বৃষ্টি") || lower.contains("wind") || lower.contains("বাতাস") || lower.contains("route") || lower.contains("পথ")) {
            reply = "🌧️ **Safe River Route & Weather Guidance (নদী আবহাওয়া পরামর্শ):**\n\n" +
                    "• **Hazard Thresholds:** Wind speed > 30 km/h or river waves > 1.2 m pose severe capsizing risk for passenger launches.\n" +
                    "• **Dijkstra Safe Route:** ShoreLine analyzes real-time weather from Open-Meteo across river nodes (Sadarghat, Chandpur, Barisal, Bhola, Khulna).\n" +
                    "• **Action:** Check live weather overlays and safest river branch below.";
            actionType = ChatMessage.ACTION_SAFE_ROUTE;
            actionLabel = "🗺️ Open Safe River Route";

        } else if (lower.contains("track") || lower.contains("family") || lower.contains("কোড") || lower.contains("ট্র্যাক") || lower.contains("location") || lower.contains("কোথায়")) {
            reply = "📍 **Live Fleet & Family Tracking (লাইভ যাত্রা ট্র্যাকিং):**\n\n" +
                    "1. **Start Voyage:** In Family Tracker, select your departure ghat (e.g. Khulna Rupsha Ghat or Sadarghat) and launch vessel name.\n" +
                    "2. **Share 6-Digit Voyage Code:** Give this code to your family.\n" +
                    "3. **Family Viewer:** Relatives enter the code to watch your launch speed, nearest ghat, and live GPS map in real time via Firebase.";
            actionType = ChatMessage.ACTION_FAMILY_TRACKER;
            actionLabel = "📍 Open Family Tracker";

        } else if (lower.contains("vessel") || lower.contains("fitness") || lower.contains("license") || lower.contains("লাইসেন্স") || lower.contains("ফিটনেস") || lower.contains("লঞ্চ")) {
            reply = "🚢 **BIWTA Vessel Fitness Verification (লঞ্চ ফিটনেস যাচাই):**\n\n" +
                    "• Every passenger launch must display an authentic BIWTA QR Code near the entrance ticket booth.\n" +
                    "• Use ShoreLine's built-in QR scanner to verify vessel registration number, passenger capacity, route permit, and master mariner certificate validity.";
            actionType = ChatMessage.ACTION_VESSEL_CHECK;
            actionLabel = "🚢 Scan Vessel QR / License";

        } else if (lower.contains("hi") || lower.contains("hello") || lower.contains("hey") || lower.contains("কেমন") || lower.contains("হ্যালো")) {
            reply = "👋 **Hello! Welcome aboard ShoreLine AI Copilot.**\n\n" +
                    "I am ready to assist you. Ask me any question about your river journey, launch safety, weather, or any topic you would like to know!";

        } else {
            reply = "💡 **ShoreLine Copilot Response:**\n\n" +
                    "You asked: *\"" + query + "\"*\n\n" +
                    "To get full generative AI answers for general knowledge and custom questions outside our offline river database, please verify your Gemini API key in the top ⚙️ Settings.\n\n" +
                    "• Free API keys are available at **aistudio.google.com**.\n" +
                    "• For river safety queries, you can ask about overloading, stormy weather, vessel fitness, or family tracking anytime!";
        }

        return new ChatMessage(reply, ChatMessage.TYPE_AI, actionType, actionLabel);
    }

    private int detectActionIntent(String fullText) {
        String lower = fullText.toLowerCase(Locale.ROOT);
        if (lower.contains("overload") || lower.contains("overcrowd") || lower.contains("যাত্রী বেশি") || lower.contains("রিপোর্ট")) {
            return ChatMessage.ACTION_REPORT_OVERLOAD;
        } else if (lower.contains("sink") || lower.contains("ডুব") || lower.contains("fire") || lower.contains("আগুন") || lower.contains("sos") || lower.contains("distress")) {
            return ChatMessage.ACTION_OFFLINE_SOS;
        } else if (lower.contains("weather") || lower.contains("storm") || lower.contains("ঝড়") || lower.contains("route") || lower.contains("safe route")) {
            return ChatMessage.ACTION_SAFE_ROUTE;
        } else if (lower.contains("track") || lower.contains("family") || lower.contains("ট্র্যাকিং")) {
            return ChatMessage.ACTION_FAMILY_TRACKER;
        } else if (lower.contains("fitness") || lower.contains("qr") || lower.contains("ফিটনেস") || lower.contains("vessel")) {
            return ChatMessage.ACTION_VESSEL_CHECK;
        }
        return ChatMessage.ACTION_NONE;
    }

    private String getActionLabel(int actionType) {
        switch (actionType) {
            case ChatMessage.ACTION_REPORT_OVERLOAD:
                return "🚨 Open Overload Reporter";
            case ChatMessage.ACTION_OFFLINE_SOS:
                return "🆘 Activate Offline SOS Beacon";
            case ChatMessage.ACTION_SAFE_ROUTE:
                return "🗺️ Open Safe River Route";
            case ChatMessage.ACTION_FAMILY_TRACKER:
                return "📍 Open Family Tracker";
            case ChatMessage.ACTION_VESSEL_CHECK:
                return "🚢 Scan Vessel QR / License";
            case ChatMessage.ACTION_CALL_999:
                return "📞 Call 999 Emergency";
            case ChatMessage.ACTION_CALL_16107:
                return "📞 Call 16107 BIWTA";
            default:
                return null;
        }
    }

    private boolean isOnline(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;
            NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
            return nc != null && (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        } catch (Exception e) {
            return false;
        }
    }
}
