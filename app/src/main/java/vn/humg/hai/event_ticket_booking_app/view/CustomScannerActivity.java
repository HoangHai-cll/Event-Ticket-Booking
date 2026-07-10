package vn.humg.hai.event_ticket_booking_app.view;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import vn.humg.hai.event_ticket_booking_app.R;

public class CustomScannerActivity extends AppCompatActivity {

    private CaptureManager capture;
    private DecoratedBarcodeView barcodeScannerView;
    private FloatingActionButton btnToggleFlash;
    private boolean isFlashOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_scanner);

        barcodeScannerView = findViewById(R.id.zxing_barcode_scanner);
        btnToggleFlash = findViewById(R.id.btn_toggle_flash);

        // --- Custom Animation ---
        View laser = findViewById(R.id.view_scanner_laser);
        if (laser != null) {
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.laser_translation);
            laser.startAnimation(animation);
        }

        // --- Setup Capture Manager ---
        capture = new CaptureManager(this, barcodeScannerView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        capture.decode();

        // --- Header Events ---
        findViewById(R.id.btn_scanner_back).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // --- Flashlight Toggle ---
        btnToggleFlash.setOnClickListener(v -> {
            if (isFlashOn) {
                barcodeScannerView.setTorchOff();
                isFlashOn = false;
                btnToggleFlash.setImageResource(R.drawable.ic_lock);
            } else {
                barcodeScannerView.setTorchOn();
                isFlashOn = true;
                btnToggleFlash.setImageResource(R.drawable.ic_edit_pencil);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        capture.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }
}
