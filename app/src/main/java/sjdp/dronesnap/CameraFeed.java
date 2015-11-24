package sjdp.dronesnap;

import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Created by sam on 11/21/15.
 */
public class CameraFeed extends Thread {
    private static final String LOG_TAG = "LOG DEBUG";
    private Camera mCamera;

    public CameraFeed(){
        mCamera = setCameraInstance();
    }

    private static Camera setCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            Log.d(LOG_TAG, "Camera is not available or does not exist.")
            e.printStackTrace();
        }
        return c;
    }

    private PictureCallback mPicture = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {


        }
    };

    public void startCapturing(){

    }

    public void pauseCapturing(){

    }

    public void resumeCapturing(){

    }

    public void stopCapturing(){

    }
}
