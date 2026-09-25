package com.qralarm.app;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.print.pdf.PrintedPdfDocument;
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
            if (uri == null) throw new IllegalStateException(c.getString(R.string.qr_image_create_failed));
            try (OutputStream os = c.getContentResolver().openOutputStream(uri)) {
                if (os == null || !bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)) {
                    throw new IllegalStateException(c.getString(R.string.qr_image_write_failed));
                }
            }
            v.clear(); v.put(MediaStore.Images.Media.IS_PENDING, 0);
            c.getContentResolver().update(uri, v, null, null);
            return uri;
        } else {
            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "NoChoice Alarm");
            if (!dir.exists() && !dir.mkdirs()) throw new IllegalStateException(c.getString(R.string.qr_folder_unavailable));
            File f = new File(dir, fileName);
            try (FileOutputStream os = new FileOutputStream(f)) {
                bitmap.compress(Bitmap.CompressFormat.PNG,100,os);
            }
            MediaScannerConnection.scanFile(c, new String[]{f.getAbsolutePath()}, new String[]{"image/png"}, null);
            return Uri.fromFile(f);
        }
    }

    /** Opens Android's print UI with an A4 portrait document and a 35 mm QR at 10 mm from top/left. */
    public static void printOnA4(Activity activity, Bitmap bitmap, String jobName) {
        PrintManager printManager = (PrintManager) activity.getSystemService(Context.PRINT_SERVICE);
        if (printManager == null) throw new IllegalStateException(activity.getString(R.string.print_service_unavailable));

        PrintDocumentAdapter adapter = new PrintDocumentAdapter() {
            private PrintAttributes attributes;

            @Override public void onLayout(
                    PrintAttributes oldAttributes,
                    PrintAttributes newAttributes,
                    CancellationSignal cancellationSignal,
                    LayoutResultCallback callback,
                    Bundle extras) {
                if (cancellationSignal.isCanceled()) {
                    callback.onLayoutCancelled();
                    return;
                }
                attributes = newAttributes;
                PrintDocumentInfo info = new PrintDocumentInfo.Builder("NoChoiceAlarm-QR-A4.pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(1)
                        .build();
                callback.onLayoutFinished(info, !newAttributes.equals(oldAttributes));
            }

            @Override public void onWrite(
                    PageRange[] pages,
                    ParcelFileDescriptor destination,
                    CancellationSignal cancellationSignal,
                    WriteResultCallback callback) {
                if (cancellationSignal.isCanceled()) {
                    callback.onWriteCancelled();
                    return;
                }
                PrintedPdfDocument document = null;
                try {
                    PrintAttributes effective = attributes != null ? attributes : a4Attributes();
                    document = new PrintedPdfDocument(activity, effective);
                    PdfDocument.Page page = document.startPage(0);
                    Canvas canvas = page.getCanvas();
                    canvas.drawColor(Color.WHITE);

                    Rect content = page.getInfo().getContentRect();
                    PrintAttributes.Resolution resolution = effective.getResolution();
                    int dpi = resolution == null ? 300 : Math.max(72, resolution.getHorizontalDpi());
                    float marginPx = dpi * 10f / 25.4f; // 10 mm
                    float qrPx = dpi * 35f / 25.4f;     // 35 mm: compact but comfortably scannable
                    float maxSize = Math.min(content.width() - 2f * marginPx, content.height() - 2f * marginPx);
                    qrPx = Math.max(1f, Math.min(qrPx, maxSize));
                    float left = content.left + marginPx;
                    float top = content.top + marginPx;
                    RectF dst = new RectF(left, top, left + qrPx, top + qrPx);
                    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    paint.setFilterBitmap(false);
                    canvas.drawBitmap(bitmap, null, dst, paint);

                    document.finishPage(page);
                    try (FileOutputStream out = new FileOutputStream(destination.getFileDescriptor())) {
                        document.writeTo(out);
                    }
                    callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
                } catch (Exception e) {
                    callback.onWriteFailed(e.getMessage());
                } finally {
                    if (document != null) document.close();
                }
            }
        };

        printManager.print(jobName, adapter, a4Attributes());
    }

    private static PrintAttributes a4Attributes() {
        return new PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setResolution(new PrintAttributes.Resolution("a4_300dpi", "300 dpi", 300, 300))
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .setColorMode(PrintAttributes.COLOR_MODE_MONOCHROME)
                .build();
    }
}
