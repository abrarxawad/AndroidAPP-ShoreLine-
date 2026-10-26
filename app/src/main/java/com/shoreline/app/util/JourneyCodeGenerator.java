package com.shoreline.app.util;

import java.security.SecureRandom;

/**
 * SYLLABUS: Utility - 6-Character Journey Tracking Code Generator
 * Generates memorable alphanumeric codes (e.g. "PADMA7", "MEGHNA4", "BD8K2W")
 * for passengers to share with their families via SMS or WhatsApp.
 */
public final class JourneyCodeGenerator {

    private static final String[] RIVER_PREFIXES = {
            "PADMA", "MEGHN", "JAMUN", "SURMA", "KARNA",
            "KIRTN", "PASUR", "RUPOR", "TETUL", "BURIG"
    };

    private static final String ALPHANUMERIC_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"; // No ambiguous chars (0, O, 1, I)
    private static final SecureRandom RANDOM = new SecureRandom();

    private JourneyCodeGenerator() {}

    /**
     * Generates a unique 6-character voyage share code.
     * e.g. "PADMA7", "SURMA3", "MEGHN9", or "BD7K9W"
     */
    public static String generateCode() {
        if (RANDOM.nextBoolean()) {
            // River prefix + single digit (e.g., "PADMA7")
            String prefix = RIVER_PREFIXES[RANDOM.nextInt(RIVER_PREFIXES.length)];
            int digit = 2 + RANDOM.nextInt(8); // 2-9
            return prefix + digit;
        } else {
            // 6-character high-entropy alphanumeric (e.g., "BD7K9W")
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                int index = RANDOM.nextInt(ALPHANUMERIC_CHARS.length());
                sb.append(ALPHANUMERIC_CHARS.charAt(index));
            }
            return sb.toString();
        }
    }
}
