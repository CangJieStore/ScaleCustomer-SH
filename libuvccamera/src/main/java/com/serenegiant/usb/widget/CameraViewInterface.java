
package com.serenegiant.usb.widget;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.view.Surface;

import com.serenegiant.usb.encoder.IVideoEncoder;

public interface CameraViewInterface extends IAspectRatioView2 {
    interface Callback {
        void onSurfaceCreated(CameraViewInterface view, Surface surface);

        void onSurfaceChanged(CameraViewInterface view, Surface surface, int width, int height);

        void onSurfaceDestroy(CameraViewInterface view, Surface surface);
    }

    void onPause();

    void onResume();

    void setCallback(Callback callback);

    SurfaceTexture getSurfaceTexture();

    Surface getSurface();

    boolean hasSurface();

    void setVideoEncoder(final IVideoEncoder encoder);

    Bitmap captureStillImage(int width, int height);
}
