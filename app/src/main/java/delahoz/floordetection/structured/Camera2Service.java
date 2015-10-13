package delahoz.floordetection.structured;


import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by yuengdelahoz on 10/9/15.
 */

public class Camera2Service extends Service {
    private static final String TAG = "Camera2Service";
    private static final int CAMERA = CameraCharacteristics.LENS_FACING_BACK;
    private CameraDevice cameraDevice;
    private CameraCaptureSession session;
    private ImageReader imageReader;
    private ImageTools imgTool;
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    public static boolean active = false;
    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    private CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            Log.i(TAG,"onOpened");
            cameraDevice = camera;
            try {
                cameraDevice.createCaptureSession(Arrays.asList(imageReader.getSurface()), sessionStateCallback,null);
            } catch (CameraAccessException e) {
                Log.e(TAG, e.getMessage());
                stopBackgroundThread();
            }
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            Log.i(TAG,"onDisconnected");
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            Log.e(TAG, "onError");
        }
    };

    private CameraCaptureSession.StateCallback sessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            Log.i(TAG,"onConfigured");
            Camera2Service.this.session = session;
            try {
                session.setRepeatingRequest(createCaptureRequest(), null,null);
            } catch (CameraAccessException e) {
                Log.e(TAG, e.getMessage());
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {
            Log.i(TAG,"onConfiguredFailed");
        }
    };

    private ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            Image img = reader.acquireLatestImage();
//            processImage(img);
            img.close();
            Log.i(TAG,"onImageAvailable");

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Intent mainActivity = new Intent(this, Camera2Activity.class);
        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Camera Service Notification")
                .setContentText("Processing images in the background");
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(Camera2Activity.class);
        stackBuilder.addNextIntent(mainActivity);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);
        startForeground(41413, mBuilder.build());
    }

    /**
     * Return the Camera Id which matches the field CAMERA.
     */
    public String getCamera(CameraManager manager) {
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (cOrientation == CAMERA) {
                    return cameraId;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        startBackgroundThread();
        active = true;
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);

        try {
            imageReader = ImageReader.newInstance(320, 240, ImageFormat.YUV_420_888,2);
            imageReader.setOnImageAvailableListener(onImageAvailableListener, null);
            Log.i(TAG, "onStartCommand");
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            mCameraOpenCloseLock.release();
            manager.openCamera(getCamera(manager), cameraStateCallback,null);
            imgTool = new ImageTools();
        } catch (CameraAccessException e) {
            Log.e(TAG, e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        closeCamera();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Process image data as desired.
     */
    private void processImage(Image image) {
        //Process image data
        final long timestamp = System.currentTimeMillis();
//        Log.i(TAG, ""+timestamp);
//        Mat rgbMat = imgTool.YUV2RGB(image);
//        imgTool.SaveImage(rgbMat, timestamp + "_P_");
    }

    private CaptureRequest createCaptureRequest() {
        try {
            CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            builder.addTarget(imageReader.getSurface());
            return builder.build();
        } catch (CameraAccessException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }


    /** Starts a background thread and its {@link Handler}. */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /** Stops the background thread and its {@link Handler}.*/
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * Closes the current {@link CameraDevice}.
     */
    private void closeCamera() {
        Log.i(TAG,"Closing Camera");
        Toast.makeText(this, "stopping service", Toast.LENGTH_SHORT).show();

        try {
            mCameraOpenCloseLock.acquire();
            if (null != session) {
                session.close();
                session = null;
            }
            if (null != cameraDevice) {
                cameraDevice.close();
                cameraDevice = null;
            }
            if (null != imageReader) {
                imageReader.close();
                imageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            stopBackgroundThread();
            mCameraOpenCloseLock.release();
            active = false;
            Log.i(TAG,"Closed Camera");
        }
    }
}
