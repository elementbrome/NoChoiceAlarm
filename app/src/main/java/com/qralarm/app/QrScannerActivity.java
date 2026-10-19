package com.qralarm.app;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

@SuppressWarnings("deprecation")
public class QrScannerActivity extends Activity implements SurfaceHolder.Callback, Camera.PreviewCallback {
    public static final String EXTRA_QR_TOKEN = "qr_token";
    private static final int REQ_CAMERA = 501;

    private SurfaceView surface;
    private Camera camera;
    private MultiFormatReader reader;
    private String expectedToken;
    private boolean decoding;
    private long lastDecode;
    private boolean done;
    private TextView flashButton;
    private boolean flashOn;
    private boolean flashSupported;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        enableLockScreenScannerUi();
        Ui.enableImmersive(this);
        expectedToken = getIntent().getStringExtra(EXTRA_QR_TOKEN);
        if (expectedToken == null || expectedToken.isEmpty()) {
            long id = getIntent().getLongExtra(AlarmScheduler.EXTRA_ALARM_ID,-1);
            AlarmModel alarm = AlarmStore.get(this,id);
            if (alarm == null || !alarm.usesQr()) { finish(); return; }
            expectedToken = QrUtils.tokenFor(this,alarm);
        }

        reader = new MultiFormatReader();
        Map<DecodeHintType,Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, Collections.singletonList(BarcodeFormat.QR_CODE));
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        reader.setHints(hints);
        buildUi();

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA},REQ_CAMERA);
        }
    }

    private void enableLockScreenScannerUi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            );
        }
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        );
    }

    private void buildUi() {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        surface = new SurfaceView(this);
        surface.getHolder().addCallback(this);
        root.addView(surface,new FrameLayout.LayoutParams(-1,-1));

        TextView overlay = Ui.text(this,getString(R.string.scanner_frame_qr),18,Color.WHITE,true);
        overlay.setGravity(Gravity.CENTER);
        overlay.setPadding(Ui.dp(this,18),Ui.dp(this,12),Ui.dp(this,18),Ui.dp(this,12));
        overlay.setBackground(Ui.rounded(0xDD161921,16,this));
        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(-2,-2,Gravity.TOP|Gravity.CENTER_HORIZONTAL);
        p.topMargin = Ui.dp(this,48);
        root.addView(overlay,p);

        flashButton = Ui.text(this,getString(R.string.flash),20,Color.WHITE,true);
        flashButton.setGravity(Gravity.CENTER);
        flashButton.setPadding(Ui.dp(this,32),0,Ui.dp(this,32),0);
        flashButton.setMinWidth(Ui.dp(this,220));
        flashButton.setBackground(Ui.rounded(0xDD161921,24,this));
        flashButton.setVisibility(android.view.View.INVISIBLE);
        flashButton.setOnClickListener(v->toggleFlash());
        FrameLayout.LayoutParams fp = new FrameLayout.LayoutParams(-2,Ui.dp(this,68),Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);
        fp.bottomMargin=Ui.dp(this,104);
        root.addView(flashButton,fp);

        TextView hint = Ui.text(this,getString(R.string.scanner_auto_validation),14,Color.WHITE,false);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(Ui.dp(this,20),Ui.dp(this,12),Ui.dp(this,20),Ui.dp(this,12));
        FrameLayout.LayoutParams hp = new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM);
        hp.bottomMargin=Ui.dp(this,36); hp.leftMargin=Ui.dp(this,20); hp.rightMargin=Ui.dp(this,20);
        root.addView(hint,hp);
        setContentView(root);
    }

    @Override public void surfaceCreated(SurfaceHolder holder) { maybeOpenCamera(); }
    @Override public void surfaceChanged(SurfaceHolder holder,int format,int w,int h) { if (camera != null) startPreview(holder); }
    @Override public void surfaceDestroyed(SurfaceHolder holder) { releaseCamera(); }

    private void maybeOpenCamera() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || camera != null || done) return;
        try {
            camera = Camera.open();
            Camera.Parameters p = camera.getParameters();
            if (p.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                p.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
            java.util.List<String> flashModes=p.getSupportedFlashModes();
            flashSupported=flashModes!=null && flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH);
            flashOn=false;
            if (flashSupported) p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(p);
            updateFlashUi();
            camera.setDisplayOrientation(90);
            startPreview(surface.getHolder());
        } catch (Exception e) {
            Toast.makeText(this,R.string.camera_open_failed,Toast.LENGTH_LONG).show();
        }
    }

    private void toggleFlash() {
        if (camera==null || !flashSupported) return;
        boolean requested=!flashOn;
        try {
            Camera.Parameters p=camera.getParameters();
            java.util.List<String> modes=p.getSupportedFlashModes();
            if (modes==null || !modes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                flashSupported=false;
                flashOn=false;
                updateFlashUi();
                return;
            }
            p.setFlashMode(requested?Camera.Parameters.FLASH_MODE_TORCH:Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(p);
            flashOn=requested;
            updateFlashUi();
        } catch (Exception e) {
            flashOn=false;
            updateFlashUi();
            Toast.makeText(this,R.string.flash_unavailable,Toast.LENGTH_SHORT).show();
        }
    }

    private void updateFlashUi() {
        if (flashButton==null) return;
        flashButton.setVisibility(flashSupported?android.view.View.VISIBLE:android.view.View.INVISIBLE);
        flashButton.setText(flashOn?R.string.flash_on:R.string.flash);
        flashButton.setBackground(flashOn?Ui.gradient(24,this):Ui.rounded(0xDD161921,24,this));
    }

    private void startPreview(SurfaceHolder holder) {
        try { camera.stopPreview(); } catch (Exception ignored) {}
        try {
            camera.setPreviewDisplay(holder);
            camera.setPreviewCallback(this);
            camera.startPreview();
        } catch (Exception e) {
            Toast.makeText(this,R.string.camera_preview_failed,Toast.LENGTH_LONG).show();
        }
    }

    @Override public void onPreviewFrame(byte[] data, Camera cam) {
        long now = System.currentTimeMillis();
        if (done || decoding || now-lastDecode < 180) return;
        decoding = true;
        lastDecode=now;
        try {
            Camera.Size s = cam.getParameters().getPreviewSize();
            PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data,s.width,s.height,0,0,s.width,s.height,false);
            Result result = reader.decodeWithState(new BinaryBitmap(new HybridBinarizer(source)));
            if (result != null) {
                String expected = QrUtils.payload(expectedToken);
                if (expected.equals(result.getText())) runOnUiThread(this::success);
                else runOnUiThread(() -> Toast.makeText(this,R.string.wrong_qr,Toast.LENGTH_SHORT).show());
            }
        } catch (Exception ignored) {
        } finally {
            reader.reset();
            decoding=false;
        }
    }

    private void success() {
        if (done) return;
        done=true;
        releaseCamera();
        setResult(RESULT_OK);
        finish();
    }

    private void releaseCamera() {
        if (camera != null) {
            if (flashOn) {
                try {
                    Camera.Parameters p=camera.getParameters();
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    camera.setParameters(p);
                } catch (Exception ignored) {}
            }
            try { camera.setPreviewCallback(null); camera.stopPreview(); } catch (Exception ignored) {}
            try { camera.release(); } catch (Exception ignored) {}
            camera=null;
        }
        flashOn=false;
        flashSupported=false;
        updateFlashUi();
    }

    @Override protected void onResume() {
        super.onResume();
        if (surface != null && surface.getHolder().getSurface().isValid()) maybeOpenCamera();
    }

    @Override protected void onPause() {
        releaseCamera();
        super.onPause();
    }

    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grants) {
        super.onRequestPermissionsResult(requestCode,permissions,grants);
        if (requestCode == REQ_CAMERA) {
            if (grants.length>0 && grants[0]==PackageManager.PERMISSION_GRANTED) maybeOpenCamera();
            else {
                Toast.makeText(this,R.string.camera_required_qr,Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) Ui.enableImmersive(this);
    }

}
