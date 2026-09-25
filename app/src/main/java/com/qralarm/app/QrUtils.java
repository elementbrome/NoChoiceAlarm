package com.qralarm.app;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.EnumMap;
import java.util.Map;

public final class QrUtils {
    private static final String PREFIX = "QRAWAKE:";
    private static final String PREFS = "qr_alarm_qr_config";
    private static final String KEY_GLOBAL_TOKEN = "global_qr_token";
    private QrUtils() {}

    public static String newToken() {
        byte[] bytes = new byte[18];
        new SecureRandom().nextBytes(bytes);
        return Base64.encodeToString(bytes, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
    }

    public static String payload(String token) { return PREFIX + token; }

    public static synchronized String globalToken(Context c) {
        String token = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_GLOBAL_TOKEN, "");
        if (token == null || token.isEmpty()) {
            // Migration douce depuis la v1 : réutiliser si possible un QR déjà imprimé.
            try {
                for (AlarmModel old : AlarmStore.all(c)) {
                    if (old.customQrToken != null && !old.customQrToken.isEmpty()) {
                        token = old.customQrToken;
                        break;
                    }
                }
            } catch (Exception ignored) {}
            if (token == null || token.isEmpty()) token = newToken();
            c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_GLOBAL_TOKEN, token).apply();
        }
        return token;
    }

    public static String tokenFor(Context c, AlarmModel alarm) {
        if (alarm == null || alarm.useGlobalQr) return globalToken(c);
        if (alarm.customQrToken == null || alarm.customQrToken.isEmpty()) alarm.customQrToken = newToken();
        return alarm.customQrToken;
    }

    public static Bitmap bitmap(String token, int size) throws Exception {
        Map<EncodeHintType,Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.MARGIN, 1);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        BitMatrix matrix = new QRCodeWriter().encode(payload(token), BarcodeFormat.QR_CODE, size, size, hints);
        int[] px = new int[size * size];
        for (int y = 0; y < size; y++) for (int x = 0; x < size; x++) px[y*size+x] = matrix.get(x,y) ? Color.BLACK : Color.WHITE;
        Bitmap b = Bitmap.createBitmap(size,size,Bitmap.Config.ARGB_8888);
        b.setPixels(px,0,size,0,0,size,size);
        return b;
    }

    public static Uri saveToGallery(Context c, Bitmap bitmap, String fileName) throws Exception {
        if (Build.VERSION.SDK_INT >= 29) {
            ContentValues v = new ContentValues();
            v.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            v.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            v.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/NoChoice Alarm");
            v.put(MediaStore.Images.Media.IS_PENDING, 1);
            Uri uri = c.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, v);
            if (uri == null) throw new IllegalStateException("Impossible de créer l’image");
            try (OutputStream os = c.getContentResolver().openOutputStream(uri)) {
                if (os == null || !bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)) throw new IllegalStateException("Écriture impossible");
            }
            v.clear(); v.put(MediaStore.Images.Media.IS_PENDING, 0);
            c.getContentResolver().update(uri, v, null, null);
            return uri;
        } else {
            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "NoChoice Alarm");
            if (!dir.exists() && !dir.mkdirs()) throw new IllegalStateException("Dossier inaccessible");
            File f = new File(dir, fileName);
            try (FileOutputStream os = new FileOutputStream(f)) {
                bitmap.compress(Bitmap.CompressFormat.PNG,100,os);
            }
            MediaScannerConnection.scanFile(c, new String[]{f.getAbsolutePath()}, new String[]{"image/png"}, null);
            return Uri.fromFile(f);
        }
    }
}
