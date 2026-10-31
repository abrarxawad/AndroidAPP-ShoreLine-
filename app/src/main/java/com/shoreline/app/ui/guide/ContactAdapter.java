package com.shoreline.app.ui.guide;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shoreline.app.databinding.ItemEmergencyContactBinding;
import com.shoreline.app.model.EmergencyContact;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for rendering official river emergency contact directory cards.
 */
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    public interface OnContactClickListener {
        void onDial(EmergencyContact contact);
    }

    private final List<EmergencyContact> contacts = new ArrayList<>();
    private OnContactClickListener listener;

    public void setOnContactClickListener(OnContactClickListener listener) {
        this.listener = listener;
    }

    public void setContacts(List<EmergencyContact> newContacts) {
        contacts.clear();
        if (newContacts != null) {
            contacts.addAll(newContacts);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemEmergencyContactBinding binding = ItemEmergencyContactBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ContactViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        holder.bind(contacts.get(position));
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    class ContactViewHolder extends RecyclerView.ViewHolder {
        private final ItemEmergencyContactBinding binding;

        ContactViewHolder(ItemEmergencyContactBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(EmergencyContact contact) {
            binding.tvContactAgency.setText(contact.getAgency());
            binding.tvContactName.setText(contact.getName());
            binding.tvContactRole.setText(contact.getRole());
            binding.tvContactPhone.setText("📞 " + contact.getPhone());

            binding.btnActionDial.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDial(contact);
                }
            });
        }
    }
}

