package com.shoreline.app.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * SYLLABUS: Camera Photo Compression & In-Memory Bitmap Optimization
 * Ensures photos taken on river decks are aggressively compressed (<200KB)
 * to allow reliable upload over Bangladesh 2G/EDGE river conditions.
 */
public class ImageCompressionHelper {

    private static final int MAX_DIMENSION = 1024; // Max width/height in pixels
    private static final int TARGET_MAX_BYTES = 200 * 1024; // 200 KB target

    /**
     * Creates a temporary photo file in the app's cache directory and returns a secure content:// URI via FileProvider.
     */
    public static File createPhotoFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "SHORELINE_REPORT_" + timeStamp + "_";
        File storageDir = new File(context.getCacheDir(), "report_cache");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    public static Uri getPhotoUri(Context context, File photoFile) {
        String authority = context.getPackageName() + ".fileprovider";
        return FileProvider.getUriForFile(context, authority, photoFile);
    }

    /**
     * Efficiently reads an image URI, downsamples in memory using inSampleSize,
     * compresses to JPEG <= 200KB, and returns a Base64 encoded string.
     */
    public static CompressionResult compressImageUriToBase64(Context context, Uri imageUri) throws IOException {
        // Step 1: Decode image dimensions without loading full bitmap into memory
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try (InputStream input = context.getContentResolver().openInputStream(imageUri)) {
            BitmapFactory.decodeStream(input, null, options);
        }

        // Step 2: Calculate inSampleSize to downsample
        options.inSampleSize = calculateInSampleSize(options, MAX_DIMENSION, MAX_DIMENSION);
        options.inJustDecodeBounds = false;

        // Step 3: Decode bitmap with calculated subsampling
        Bitmap bitmap;
        try (InputStream input = context.getContentResolver().openInputStream(imageUri)) {
            bitmap = BitmapFactory.decodeStream(input, null, options);
        }

        if (bitmap == null) {
            throw new IOException("Failed to decode bitmap from URI");
        }

        // Step 4: Iteratively compress JPEG quality to hit under 200KB
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int quality = 80;
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);

        while (baos.size() > TARGET_MAX_BYTES && quality > 30) {
            baos.reset();
            quality -= 15;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        }

        byte[] compressedBytes = baos.toByteArray();
        int finalSizeKb = compressedBytes.length / 1024;
        String base64String = Base64.encodeToString(compressedBytes, Base64.NO_WRAP);

        return new CompressionResult(base64String, bitmap, finalSizeKb);
    }

    /**
     * Decodes Base64 string back into a Bitmap for display in UI.
     */
    public static Bitmap decodeBase64ToBitmap(String base64String) {
        if (base64String == null || base64String.trim().isEmpty()) {
            return null;
        }
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            return null;
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static class CompressionResult {
        private final String base64Data;
        private final Bitmap previewBitmap;
        private final int sizeKb;

        public CompressionResult(String base64Data, Bitmap previewBitmap, int sizeKb) {
            this.base64Data = base64Data;
            this.previewBitmap = previewBitmap;
            this.sizeKb = sizeKb;
        }

        public String getBase64Data() { return base64Data; }
        public Bitmap getPreviewBitmap() { return previewBitmap; }
        public int getSizeKb() { return sizeKb; }
    }
}
