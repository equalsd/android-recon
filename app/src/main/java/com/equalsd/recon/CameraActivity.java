package com.equalsd.recon;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CameraActivity extends Activity implements PictureCallback, SurfaceHolder.Callback, Camera.AutoFocusCallback {

    public static final String EXTRA_CAMERA_DATA = "camera_data";

    private static final String KEY_IS_CAPTURING = "is_capturing";

    private boolean previewRunning, cameraReleased, focusAreaSupported, meteringAreaSupported;
    private int focusAreaSize;
    private Matrix matrix;

    private Camera mCamera = null;
    private ImageView mCameraImage;
    private SurfaceView mCameraPreview;
    private Button mCaptureImageButton;
    private byte[] mCameraData;
    private Button mDoneButton;
    private boolean mIsCapturing;
    private ContentResolver contentResolver;
    private DBHelper mydb;
    //private boolean rotation = true;
    String rotation = "";
    String savedRotation = "";
    private Sensor sensor;

    static String state;
    static String username;
    static String current = "";
    String catString = "";
    ArrayList<String> category;
    //OrientationEventListener OEL;
    private long time = 3;

    private Handler customHandler = new Handler();

    Handler h;
    Runnable runnable;


    private OnClickListener mCaptureImageButtonClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d("recon", (String) mCaptureImageButton.getText());
            if (!mCaptureImageButton.getText().equals("Take Picture")) {
                h.removeCallbacks(runnable);
                time = 4;
                mCaptureImageButton.setText("Take Picture");
                //mDoneButton.setEnabled(true);
                mCameraImage.setVisibility(View.INVISIBLE);
                mCameraPreview.setVisibility(View.VISIBLE);
            } else {
                rotation = savedRotation;
                captureImage();
                /*Bitmap bitmap = BitmapFactory.decodeByteArray(mCameraData, 0, mCameraData.length);
                mCameraImage.setImageBitmap(bitmap);
                mCamera.stopPreview();
                mCameraPreview.setVisibility(View.INVISIBLE);
                mCameraImage.setVisibility(View.VISIBLE);*/
                mDoneButton.setEnabled(false);
                mCaptureImageButton.setEnabled(true);
                runner();
            }
        }
    };

    private OnClickListener mRecaptureImageButtonClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            setupImageCapture();
        }
    };

    private OnClickListener mDoneButtonClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mCameraData != null) {
                Intent intent = new Intent();
                intent.putExtra(EXTRA_CAMERA_DATA, mCameraData);
                setResult(RESULT_OK, intent);
            } else {
                setResult(RESULT_CANCELED);
            }
            finish();
        }
    };

    /*private void done() {
        if (mCameraData != null) {
            Intent intent = new Intent();
            intent.putExtra(EXTRA_CAMERA_DATA, mCameraData);
            setResult(RESULT_OK, intent);
        } else {
            setResult(RESULT_CANCELED);
        }
        finish();
    }*/

    public void runner() {
        if (h == null) {
            h = new Handler();
        }
        runnable = new Runnable() {
            public void run() {
                Log.d("TimerExample", "Going for...." + time);
                time = time - 1;
                try {
                    //Toast.makeText(getApplicationContext(), "so far: " + time, Toast.LENGTH_LONG).show();
                    mCaptureImageButton.setText("Click to Cancel (" + time + ")");
                    if (time == 0) {
                        //Log.d("recon", "stop Preview");
                        mCamera.stopPreview();
                        h.removeCallbacks(runnable);
                        save();
                        time = 3;
                        mCaptureImageButton.setText("Take Picture");
                        mCaptureImageButton.setEnabled(false);
                        mDoneButton.setText("Saving");
                        //mDoneButton.setEnabled(true);
                        mCameraImage.setVisibility(View.INVISIBLE);
                        mCameraPreview.setVisibility(View.VISIBLE);
                        releaseCameraAndPreview();
                        startCamera();
                    } else {
                        //time += 1000;
                        h.postDelayed(this, 1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        h.postDelayed(runnable, 1000);
        //runnable.run();
    }

    private void releaseCameraAndPreview() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    private File openFileForImage() {
        File imageDirectory = null;
        String storageState = Environment.getExternalStorageState();
        if (storageState.equals(Environment.MEDIA_MOUNTED)) {
            imageDirectory = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "com.adaveracity.camera");
            if (!imageDirectory.exists() && !imageDirectory.mkdirs()) {
                imageDirectory = null;
            } else {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_mm_dd_hh_mm",
                        Locale.getDefault());

                return new File(imageDirectory.getPath() +
                        File.separator + "image_" +
                        dateFormat.format(new Date()) + ".png");
            }
        }
        return null;
    }

    private void saveImageToFile(File file) {
        Bitmap mCameraBitmap = ((BitmapDrawable) mCameraImage.getDrawable()).getBitmap();
        if (mCameraBitmap != null) {
            FileOutputStream outStream = null;

            OutputStream fOut = null;
            try {
                //get Bitmap from Camera
                Bitmap bitmap = BitmapFactory.decodeByteArray(mCameraData, 0, mCameraData.length);
                Bitmap pictureBitmap = bitmap; // obtaining the Bitmap
                Bitmap rotatedBitmap = null;

                fOut = new FileOutputStream(file);
                //Bitmap rotatedBitmap = Bitmap.createBitmap(pictureBitmap, 0, 0, pictureBitmap.getWidth(), pictureBitmap.getHeight(), matrix, true);
                //rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
                //pictureBitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
                mCameraBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);

                fOut.flush();
                fOut.close(); // do not forget to close the stream

                String mediaString = MediaStore.Images.Media.insertImage(this.getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
                //String mediaString = MediaStore.Images.Media.insertImage(CameraActivity.this.getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
                //String mediaString = MediaStore.Images.Media.insertImage(contentResolver, file.getAbsolutePath(), file.getName(), file.getName());
                Toast.makeText(getApplicationContext(), "Saved to: " + file.getPath() + " as: " + mediaString, Toast.LENGTH_LONG).show();

                String[] track = state.split("-");

                ArrayList<AbstractMap.SimpleEntry<String, String>> pairs = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
                pairs.add(new AbstractMap.SimpleEntry("location", current));
                pairs.add(new AbstractMap.SimpleEntry("picture", mediaString));
                pairs.add(new AbstractMap.SimpleEntry("notes", ""));
                pairs.add(new AbstractMap.SimpleEntry("tracking", track[1]));
                pairs.add(new AbstractMap.SimpleEntry("category", catString));
                pairs.add(new AbstractMap.SimpleEntry("user", username));
                mydb.insertData("elements", pairs);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void save() {
        File saveFile = openFileForImage();
        if (saveFile != null) {
            saveImageToFile(saveFile);
        } else {
            Toast.makeText(getApplicationContext(), "Unable to save image to file.",
                    Toast.LENGTH_LONG).show();
        }
        //mCameraPreview.setVisibility(View.INVISIBLE);
        //mCameraImage.setVisibility(View.VISIBLE);
        //mCamera.startPreview();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);

        mCameraImage = (ImageView) findViewById(R.id.camera_image_view);
        mCameraImage.setVisibility(View.INVISIBLE);

        mCameraPreview = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = mCameraPreview.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mCaptureImageButton = (Button) findViewById(R.id.capture_image_button);
        mCaptureImageButton.setOnClickListener(mCaptureImageButtonClickListener);
        mDoneButton = (Button) findViewById(R.id.done_button);

        final Button doneButton = (Button) findViewById(R.id.done_button);
        doneButton.setOnClickListener(mDoneButtonClickListener);

        //setup for DB
        mydb = new DBHelper(this);
        state = List.state;
        username = List.username;
        category = List.category;
        if (!category.isEmpty()) {
            current = category.get(category.size() - 1);
            catString = TextUtils.join("|", List.category);
        }

        contentResolver = CameraActivity.this.getContentResolver();
        mIsCapturing = true;

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        java.util.List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
        if (sensorList.size() > 0) {
            sensor = sensorList.get(0);
        } else {
            Log.d("recon sensor:", "Orientation sensor not present");
        }
        sensorManager.registerListener(orientationListener, sensor, 0, null);
        focusAreaSize = 5;
        matrix = new Matrix();

        mCameraPreview.setFocusable(true);
        mCameraPreview.setFocusableInTouchMode(true);
        mCameraPreview.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //Toast.makeText(getApplicationContext(), "touched", Toast.LENGTH_LONG).show();
                if (mCamera != null) {
                    //cancel previous actions
                    mCamera.cancelAutoFocus();
                    //Log.d("recon", String.valueOf(event.getX() + " " + event.getY()));
                    Rect focusRect = calculateTapArea(event.getX(), event.getY(), 1f);
                    Rect meteringRect = calculateTapArea(event.getX(), event.getY(), 1.5f);

                    Camera.Parameters parameters = null;
                    try {
                        parameters = mCamera.getParameters();
                    } catch (Exception e) {
                        //Log.e();
                    }

                    // check if parameters are set (handle RuntimeException: getParameters failed (empty parameters))
                    if (parameters != null) {
                        //parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
                        Camera.Area focusArea = new Camera.Area(focusRect, 1000);
                        java.util.List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
                        focusAreas.add(focusArea);
                        //parameters.setFocusAreas((List<Camera.Area>) new Camera.Area(focusRect, 1000));
                        parameters.setFocusAreas(focusAreas);

                        if (meteringAreaSupported) {
                            Camera.Area focusAreaM = new Camera.Area(meteringRect, 1000);
                            java.util.List<Camera.Area> focusAreaMs = new ArrayList<Camera.Area>();
                            focusAreaMs.add(focusAreaM);
                            //parameters.setMeteringAreas(Collections.newArrayList(new Camera.Area(meteringRect, 1000)));
                            parameters.setFocusAreas(focusAreaMs);
                        }

                        try {
                            mCamera.setParameters(parameters);
                            mCamera.autoFocus(CameraActivity.this);
                        } catch (Exception e) {
                            //Log.e(e);
                        }

                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putBoolean(KEY_IS_CAPTURING, mIsCapturing);
        //Toast.makeText(CameraActivity.this, "saved.", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        //Toast.makeText(CameraActivity.this, "restore.", Toast.LENGTH_LONG).show();

        mIsCapturing = savedInstanceState.getBoolean(KEY_IS_CAPTURING, mCameraData == null);
        if (mCameraData != null) {
            Log.d("recon", "Camera Restore null");
            setupImageDisplay();
        } else {
            Log.d("recon", "Camera Restore non-null");
            setupImageCapture();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCamera();
        //Toast.makeText(CameraActivity.this, "resume", Toast.LENGTH_LONG).show();


    }

    private void startCamera() {
        customHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mCamera == null) {
                    Log.d("recon", "camera null");
                    try {
                        mDoneButton.setEnabled(true);
                        mCaptureImageButton.setEnabled(true);
                        mDoneButton.setText("Return");
                        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                        Log.d("recon", "camera is open");
                        mCamera.setPreviewDisplay(mCameraPreview.getHolder());
                        mCamera.cancelAutoFocus();
                        //mCamera.autoFocus(focusOnTouch(MotionEvent););
                        Log.d("recon", "previewDisplay");
                        //surfaceHolder = mCameraPreview.getHolder();
                        //surfaceHolder.addCallback(this);
                        //surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                        if (mIsCapturing) {
                            Log.d("recon", "started");
                            //mCamera.startPreview();
                            //setupImageDisplay();
                            //setupImageCapture();

                            Camera.Parameters parameters = mCamera.getParameters();
                            Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
                            Log.d("recon display", String.valueOf(display.getRotation()));

                            if (display.getRotation() == Surface.ROTATION_0) {
                                //parameters.setPreviewSize(height, width);
                                mCamera.setDisplayOrientation(90);
                            }

                            if (display.getRotation() == Surface.ROTATION_90) {
                                //parameters.setPreviewSize(width, height);
                                mCamera.setDisplayOrientation(0);
                            }

                            if (display.getRotation() == Surface.ROTATION_180) {
                                //parameters.setPreviewSize(height, width);
                            }

                            if (display.getRotation() == Surface.ROTATION_270) {
                                //parameters.setPreviewSize(width, height);
                                mCamera.setDisplayOrientation(180);
                            }

                            if (parameters.getMaxNumFocusAreas() > 0) {
                                focusAreaSupported = true;
                            }

                            if (parameters.getMaxNumMeteringAreas() > 0) {
                                meteringAreaSupported = true;
                            }

                            //parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
                            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                            mCamera.setParameters(parameters);
                            mCamera.startPreview();
                        }
                    } catch (Exception e) {
                        Toast.makeText(CameraActivity.this, "Unable to open camera.", Toast.LENGTH_LONG)
                                .show();
                    }
                } else {
                    Log.d("recon", "camera not null");
                }
            }
        }, 1200);
    }



    @Override
    protected void onPause() {
        super.onPause();

        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        mCameraData = data;
        setupImageDisplay();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        /*if (mCamera != null) {
            Log.d("recon", "Surface changed null");
            try {
                mCamera.setPreviewDisplay(holder);
                if (mIsCapturing) {
                    Log.d("recon", "Surface preview");
                    Camera.Parameters parameters = mCamera.getParameters();
                    Display display = ((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
                    Log.d("recon display", String.valueOf(display.getRotation()));

                    if(display.getRotation() == Surface.ROTATION_0)
                    {
                        parameters.setPreviewSize(height, width);
                        mCamera.setDisplayOrientation(90);
                    }

                    if(display.getRotation() == Surface.ROTATION_90)
                    {
                        parameters.setPreviewSize(width, height);
                        mCamera.setDisplayOrientation(0);
                    }

                    if(display.getRotation() == Surface.ROTATION_180)
                    {
                        parameters.setPreviewSize(height, width);
                    }

                    if(display.getRotation() == Surface.ROTATION_270)
                    {
                        parameters.setPreviewSize(width, height);
                        mCamera.setDisplayOrientation(180);
                    }

                    mCamera.setParameters(parameters);
                    //mCamera.startPreview();
                }
            } catch (IOException e) {
                Toast.makeText(CameraActivity.this, "Unable to start camera preview.", Toast.LENGTH_LONG).show();
            }
        }

        //setCameraDisplayOrientation(this, 0, mCamera);*/
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        /*if (mCamera != null) {
            // Call stopPreview() to stop updating the preview surface.
            mCamera.stopPreview();
        }*/
    }

    private void captureImage() {
        mCamera.takePicture(null, null, this);
    }

    private void setupImageCapture() {
        Log.d("recon", "setupImageCapture");
        mCamera.startPreview();
        //mCaptureImageButton.setText(R.string.capture_image);
        //mCaptureImageButton.setOnClickListener(mCaptureImageButtonClickListener);
    }

    private void setupImageDisplay() {
        Bitmap bitmap = BitmapFactory.decodeByteArray(mCameraData, 0, mCameraData.length);
        Log.d("recon", "image display");

        //do rotation of image here too;

        Matrix matrix = new Matrix();
        switch (rotation) {
            case "Top":
                matrix.postRotate(90);
                break;
            case "Bottom":
                matrix.postRotate(270);
                break;
            case "Right":
                matrix.postRotate(0);
                break;
            case "Left":
                matrix.postRotate(180);
                break;
        }

        //Toast.makeText(getApplicationContext(), rotation, Toast.LENGTH_LONG).show();

        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);


        mCameraImage.setImageBitmap(rotatedBitmap);
        mCamera.stopPreview();
        mCameraPreview.setVisibility(View.INVISIBLE);
        mCameraImage.setVisibility(View.VISIBLE);
        //mCaptureImageButton.setText(R.string.recapture_image);
        //mCaptureImageButton.setOnClickListener(mRecaptureImageButtonClickListener);
        //save();
    }

    /*@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE) {
            Log.e("On Config Change", "LANDSCAPE");
        } else {
            Log.e("On Config Change","PORTRAIT");
        }
    }*/

    /*public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        //Log.d("recon orientation:", String.valueOf(rotation));

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }*/

    private SensorEventListener orientationListener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor arg0, int arg1) {

        }

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
                //float azimuth = sensorEvent.values[0];
                float pitch = sensorEvent.values[1];
                float roll = sensorEvent.values[2];

                if (pitch < -45 && pitch > -135) {
                    //Log.d("recon orientation: ", "Top side of the phone is Up!");
                    rotation = "Top";
                } else if (pitch > 45 && pitch < 135) {
                    //Log.d("recon orientation: ", "Bottom side of the phone is Up!");
                    rotation = "Bottom";
                } else if (roll > 45) {
                    //Log.d("recon orientation: ", "Right side of the phone is Up!");
                    rotation = "Right";
                } else if (roll < -45) {
                    //Log.d("recon orientation: ", "Left side of the phone is Up!");
                    rotation = "Left";
                }

            }
        }

    };

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        //Toast.makeText(getApplicationContext(), "autofocused", Toast.LENGTH_LONG).show();
    }

    /**
     * On each tap event we will calculate focus area and metering area.
     * <p>
     * Metering area is slightly larger as it should contain more info for exposure calculation.
     * As it is very easy to over/under expose
     */

    public boolean isFocusAreaSupported() {
        return focusAreaSupported;
    }

    public int getFocusAreaSize() {
        return focusAreaSize;
    }

    /**
     * Convert touch position x:y to {@link Camera.Area} position -1000:-1000 to 1000:1000.
     * <p>
     * Rotate, scale and translate touch rectangle using matrix configured in
     * {@link SurfaceHolder.Callback#surfaceChanged(android.view.SurfaceHolder, int, int, int)}
     */
    private Rect calculateTapArea(float x, float y, float coefficient) {
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();

        int left = clamp((int) x - areaSize / 2, 0, mCameraPreview.getWidth() - areaSize);
        int top = clamp((int) y - areaSize / 2, 0, mCameraPreview.getHeight() - areaSize);

        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);
        Log.d("recon h", String.valueOf(rectF));
        matrix.mapRect(rectF);

        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
    }

    private int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }
}