package com.shoreline.app.model;

/**
 * Model representing an Emergency River Contact.
 * Populated from ShoreLineContentProvider cursor.
 */
public class EmergencyContact {

    private final long id;
    private final String name;
    private final String agency;
    private final String phone;
    private final String role;
    private final int priority;

    public EmergencyContact(long id, String name, String agency, String phone, String role, int priority) {
        this.id = id;
        this.name = name;
        this.agency = agency;
        this.phone = phone;
        this.role = role;
        this.priority = priority;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getAgency() { return agency; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
    public int getPriority() { return priority; }
}

