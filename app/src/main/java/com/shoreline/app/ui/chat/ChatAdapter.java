package com.shoreline.app.ui.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.shoreline.app.R;
import com.shoreline.app.data.model.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnActionClickListener {
        void onActionClick(int actionType);
    }

    private final List<ChatMessage> messages;
    private final OnActionClickListener actionClickListener;

    public ChatAdapter(List<ChatMessage> messages, OnActionClickListener actionClickListener) {
        this.messages = messages;
        this.actionClickListener = actionClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == ChatMessage.TYPE_USER) {
            View view = inflater.inflate(R.layout.item_chat_user, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_chat_ai, parent, false);
            return new AiViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).bind(message);
        } else if (holder instanceof AiViewHolder) {
            ((AiViewHolder) holder).bind(message, actionClickListener);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvUserMessage;
        private final TextView tvUserTime;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserMessage = itemView.findViewById(R.id.tvUserMessage);
            tvUserTime = itemView.findViewById(R.id.tvUserTime);
        }

        public void bind(ChatMessage message) {
            tvUserMessage.setText(message.getText());
            tvUserTime.setText(message.getFormattedTime());
        }
    }

    static class AiViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvAiMessage;
        private final TextView tvAiTime;
        private final MaterialButton btnChatAction;

        public AiViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAiMessage = itemView.findViewById(R.id.tvAiMessage);
            tvAiTime = itemView.findViewById(R.id.tvAiTime);
            btnChatAction = itemView.findViewById(R.id.btnChatAction);
        }

        public void bind(ChatMessage message, OnActionClickListener listener) {
            tvAiMessage.setText(message.getText());
            tvAiTime.setText(message.getFormattedTime());

            if (message.getActionType() != ChatMessage.ACTION_NONE && message.getActionLabel() != null) {
                btnChatAction.setVisibility(View.VISIBLE);
                btnChatAction.setText(message.getActionLabel());
                btnChatAction.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onActionClick(message.getActionType());
                    }
                });
            } else {
                btnChatAction.setVisibility(View.GONE);
            }
        }
    }
}

