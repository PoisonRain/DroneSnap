package sjdp.dronesnap;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.util.Log;
import android.view.SurfaceView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Created by Samuel Poulton on 11/21/15.
 */
public class CameraFeed extends Thread {
    private static final String LOG_TAG = "LOG DEBUG";
    private Camera mCamera;
    private long mTimeLapseDelay;
    private boolean isCapturing = true;
    private Context mContext;

    public CameraFeed(Context context, long delay) {
        this.mContext = context;
        this.mTimeLapseDelay = delay;
        this.mCamera = setCameraInstance();
    }

    public CameraFeed(Context context){
        this.mContext = context;
        this.mTimeLapseDelay = 1000;
        this.mCamera = setCameraInstance();
    }


    public void run(){
        startCapturing();
    }

    private static Camera setCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            Log.e(LOG_TAG, "Camera is not available or does not exist.");
            e.printStackTrace();
        }
        return c;
    }

    private PictureCallback mPicture = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(LOG_TAG, "Here we go!");
            sendJPGByteArray(data);
        }
    };

    private boolean sendJPGByteArray(byte[] data) {
        Log.d(LOG_TAG, "Sending the jpg byte array.");
        return true;
    }

    private void startCapturing(){
        while(isCapturing) {
            Log.d(LOG_TAG, "Taking a picture.");
            SurfaceView surface = new SurfaceView(mContext);
            try {
                mCamera.setPreviewDisplay(surface.getHolder());
                mCamera.startPreview();
                mCamera.takePicture(null,null,mPicture);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(mTimeLapseDelay);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Unable to time lapse picture captures via thread sleep.");
            }
        }
    }

    public void pauseCapturing(){
//        try {
//            wait();
//        } catch (InterruptedException e) {
//            Log.e(LOG_TAG, "Unable to pause capture.");
//            e.printStackTrace();
//        }
    }

    public void resumeCapturing(){
//        notify();
    }

    public void stopCapturing(){
        isCapturing = false;
        mCamera.release();
    }
}
