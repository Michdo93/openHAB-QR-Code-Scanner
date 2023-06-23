package org.openhab.qrcodescanner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.common.MlKitException;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;

import java.io.IOException;
import java.util.Locale;

@SuppressWarnings("ALL")
public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private TextView barcodeResultView;
    private RelativeLayout barcodeResultContainer;
    private ImageView closeButton;
    private CameraSource cameraSource;
    private GmsBarcodeScanner barcodeScanner;
    private SurfaceView cameraPreview;
    private SurfaceHolder cameraPreviewHolder;
    private RelativeLayout fragmentContent;
    private boolean isScanning = true;

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private Camera camera;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);

        // Set up the ActionBar with a title
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        barcodeResultView = findViewById(R.id.barcode_result_view);
        barcodeResultContainer = findViewById(R.id.barcode_result_container);
        closeButton = findViewById(R.id.close_button);
        cameraPreview = findViewById(R.id.camera_preview);
        cameraPreviewHolder = cameraPreview.getHolder();
        fragmentContent = findViewById(R.id.fragment_content);

        closeButton.setOnClickListener(view -> hideBarcodeResult());
        cameraPreviewHolder.addCallback(this);

        // Überprüfen, ob die Kameraberechtigung gewährt wurde
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Kameraberechtigung anfordern, wenn sie nicht gewährt wurde
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            startScanning();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        // Kamera-Preview starten, wenn die Surface erstellt wurde
        startCameraPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        // Oberfläche geändert, aber die Kamera-Vorschau nicht neu starten (für die Camera2 API nicht erforderlich)
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Kamera-Preview beenden, wenn die Surface zerstört wird
        releaseCamera();
    }

    private void startCameraPreview() {
        cameraPreview = findViewById(R.id.camera_preview);
        cameraPreviewHolder = cameraPreview.getHolder();

        cameraPreviewHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    camera = Camera.open();
                    camera.setDisplayOrientation(90); // Setze Kameraorientierung auf 90 Grad (Hochformat)
                    camera.setPreviewDisplay(holder);
                    camera.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (cameraPreviewHolder.getSurface() == null) {
                    return;
                }

                try {
                    camera.stopPreview();
                    camera.setPreviewDisplay(cameraPreviewHolder);
                    camera.startPreview();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                camera.stopPreview();
                camera.release();
                camera = null;
            }
        });
    }
    private void releaseCamera() {
        // Kamera freigeben
        if (cameraSource != null) {
            cameraSource.stop();
            cameraSource.release();
            cameraSource = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Kameraberechtigung gewährt
                startScanning();
            } else {
                // Kameraberechtigung wurde verweigert
                // Hier kannst du entsprechende Maßnahmen ergreifen, z.B. eine Benachrichtigung anzeigen
            }
        }
    }

    private void showBarcodeResult(Barcode barcode) {
        String barcodeValue =
                String.format(
                        Locale.US,
                        "Display Value: %s\nRaw Value: %s\nFormat: %s\nValue Type: %s",
                        barcode.getDisplayValue(),
                        barcode.getRawValue(),
                        barcode.getFormat(),
                        barcode.getValueType());
        barcodeResultView.setText(barcodeValue);
        barcodeResultContainer.setVisibility(View.VISIBLE);
        cameraPreview.setVisibility(View.VISIBLE);
        fragmentContent.setVisibility(View.VISIBLE);
        cameraPreview.setZOrderOnTop(false);
    }

    private void hideBarcodeResult() {
        barcodeResultContainer.setVisibility(View.GONE);
        cameraPreview.setVisibility(View.VISIBLE);
        fragmentContent.setVisibility(View.GONE);
        startScanning();
    }

    private void startScanning() {
        GmsBarcodeScannerOptions.Builder optionsBuilder = new GmsBarcodeScannerOptions.Builder();

        cameraPreview.getHolder().addCallback(this);

        GmsBarcodeScanner gmsBarcodeScanner =
                GmsBarcodeScanning.getClient(this, optionsBuilder.build());
        gmsBarcodeScanner
                .startScan()
                .addOnSuccessListener(barcode -> showBarcodeResult(barcode))
                .addOnFailureListener(
                        e -> barcodeResultView.setText(getErrorMessage(e)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCamera();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @SuppressLint("SwitchIntDef")
    private String getErrorMessage(Exception e) {
        if (e instanceof MlKitException) {
            switch (((MlKitException) e).getErrorCode()) {
                case MlKitException.CODE_SCANNER_CAMERA_PERMISSION_NOT_GRANTED:
                    return getString(R.string.error_camera_permission_not_granted);
                case MlKitException.CODE_SCANNER_APP_NAME_UNAVAILABLE:
                    return getString(R.string.error_app_name_unavailable);
                default:
                    return getString(R.string.error_default_message, e);
            }
        } else {
            return e.getMessage();
        }
    }

    public void openSettingsActivity(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
