package com.shoreline.app.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * SYLLABUS: Room Entity - User profile & Role persistence
 * Defines schema for caching user sessions offline.
 * SQLite equivalent: CREATE TABLE users (user_id TEXT PRIMARY KEY, email TEXT, ...);
 */
@Entity(tableName = "users", indices = {@Index(value = {"email"}, unique = true)})
public class UserEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "user_id")
    private String userId;

    @ColumnInfo(name = "email")
    private String email;

    @ColumnInfo(name = "full_name")
    private String fullName;

    @ColumnInfo(name = "phone_number")
    private String phoneNumber;

    @ColumnInfo(name = "role")
    private String role; // "PASSENGER" or "ADMIN"

    @ColumnInfo(name = "emergency_contact")
    private String emergencyContact;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    public UserEntity(@NonNull String userId, String email, String fullName, String phoneNumber, String role, String emergencyContact, long createdAt) {
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.role = role != null ? role : "PASSENGER";
        this.emergencyContact = emergencyContact;
        this.createdAt = createdAt;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
