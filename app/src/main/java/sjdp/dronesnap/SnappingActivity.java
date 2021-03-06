package sjdp.dronesnap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.widget.Toast;
import java.io.FileOutputStream;

/**
 * Created by Samuel Poulton on 11/21/15.
 */
public class SnappingActivity extends Activity implements OnClickListener{
    static final private String LOG_TAG = "LOG_SEE_ME";
    static final String PREFS_FILE = "mSharedPreferences";

    private int mNumberOfSnapsTaken = 0;
    private String mFlightName = "";
    private long mTimeLapseDelay = 0;
    private boolean isSnapping = true;
    private long mDurationOfFlight = 0;

    private Resources mRes = null;
    private SharedPreferences mSharedPrefs = null;
    private Thread mCameraThread = null;
    private Camera mCamera;
    private CameraPreview mPreview;
    private SnapDisbatcher mSnapDisbatcher;
    private DirectionListener mDirectionListener;

    // TCP address & ports
    private String mServerURL;
    private int    mServerPort;
    private String mPhoneURL;
    private int    mPhonePort;

    // Widgets
    private TextView mNumSnapET = null;
    private Chronometer mFlightDuraChrono = null;
    private Button mStopBtn = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snapping);
        mRes = getResources();
        mSharedPrefs = getSharedPreferences(PREFS_FILE, MODE_PRIVATE);
        inflateWidgets();
        setParentIntentExtras();
        setNetworkSharedPrefs();

        mSnapDisbatcher = new SnapDisbatcher(mServerURL, mServerPort, getApplication());
        // Android Phone IP Address
        mDirectionListener = new DirectionListener(mPhoneURL, mPhonePort, mFlightName);
        mCamera = setCameraInstance();
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        startSnapping();
    }

    @Override
    protected void onPause(){
        super.onPause();
        pauseSnapping();
    }

    @Override
    protected void onResume(){
        super.onResume();
        resumeSnapping();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(mCameraThread.isAlive())
            mCameraThread.interrupt();
    }

    @Override
    public void onClick(View v) {
        Log.d(LOG_TAG, "onclicked fired");
        switch(v.getId()) {
            case R.id.stop_btn:
                stopSnapping();
                break;
        }
    }

    private void startSnapping(){
        Log.d(LOG_TAG, "startSnapping");

        mFlightDuraChrono.setBase(SystemClock.elapsedRealtime());
        mNumSnapET.setText("0");
    }

    private void resumeSnapping(){
        Log.d(LOG_TAG, "resumeSnapping");

        if(mCamera == null)
            mCamera = setCameraInstance();
        isSnapping = true;
        startCameraSnappingThread();

        mFlightDuraChrono.setBase(SystemClock.elapsedRealtime() - mDurationOfFlight);
        mFlightDuraChrono.start();
    }

    private void pauseSnapping(){
        Log.d(LOG_TAG, "pauseSnapping");

        mFlightDuraChrono.stop();
        mDurationOfFlight =  SystemClock.elapsedRealtime() - mFlightDuraChrono.getBase();

        isSnapping = false;
        if(mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    private void stopSnapping(){
        Log.d(LOG_TAG, "stopSnapping");

        isSnapping = false;
        mFlightDuraChrono.stop();
        mDurationOfFlight =  SystemClock.elapsedRealtime() - mFlightDuraChrono.getBase();
        if(mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
        mSnapDisbatcher.stopDisbatching();
        mDirectionListener.stopListening();
        Intent resultIntent = new Intent();
        resultIntent.putExtra(mRes.getString(R.string.intent_extra_flight_duration), mDurationOfFlight);
        resultIntent.putExtra(mRes.getString(R.string.intent_extra_snap_count), mNumberOfSnapsTaken);

        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void inflateWidgets() {
        mNumSnapET = (TextView) findViewById(R.id.stat_num_of_snaps);
        mFlightDuraChrono = (Chronometer) findViewById(R.id.stat_flight_duration);

        mStopBtn = (Button) findViewById(R.id.stop_btn);
        mStopBtn.setOnClickListener(this);
    }

    private void setParentIntentExtras() {
        Intent parentRequester = this.getIntent();
        mFlightName = parentRequester.getStringExtra(mRes.getString(R.string.intent_extra_flight_name));
        mTimeLapseDelay = parentRequester.getLongExtra(mRes.getString(R.string.intent_extra_time_lapse), 1000);
    }

    private void setNetworkSharedPrefs() {
        mServerURL = mSharedPrefs.getString(mRes.getString(R.string.shared_pref_server_url), "");
        mServerPort = Integer.parseInt(mSharedPrefs.getString(mRes.getString(R.string.shared_pref_server_port), "8150"));

        mPhoneURL = mSharedPrefs.getString(mRes.getString(R.string.shared_pref_phone_url), "");
        mPhonePort = Integer.parseInt(mSharedPrefs.getString(mRes.getString(R.string.shared_pref_phone_port), "9150"));
    }

    // ---------------------------- CAMERA FUNCTIONS ----------------------------------
    private static Camera setCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
            Camera.Parameters params = c.getParameters();
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            c.setParameters(params);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Camera is not available or does not exist.");
            e.printStackTrace();
        }
        return c;
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(LOG_TAG, "Adding picture to upload list");
//            savePicture(data);
            mSnapDisbatcher.addSnaptoUploadList(data);
        }
    };


    private void startCameraSnappingThread(){
        mCameraThread = new Thread() {
            @Override
            public void run() {
                super.run();

                while (isSnapping) {
                    Log.d(LOG_TAG, "Attempting to take picture now.");
                    try {
//                        http://stackoverflow.com/questions/15623944/how-to-autofocus-android-camera-automatically
                        mCamera.autoFocus(new Camera.AutoFocusCallback() {
                            public void onAutoFocus(boolean success, Camera camera) {
                                if (success) {
                                    mCamera.takePicture(null, null, mPicture);
                                }
                            }
                        });

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mNumberOfSnapsTaken++;
                                mNumSnapET.setText(String.valueOf(mNumberOfSnapsTaken));
                            }
                        });
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        Log.d(LOG_TAG, e.getMessage());
                    }

                    try {
                        sleep(mTimeLapseDelay);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        Log.d(LOG_TAG, "Not sleeping very well" + e.getMessage());
                    }

                    Log.d(LOG_TAG, "NUMBER OF SNAPS: " + mNumberOfSnapsTaken);
                }
            }
        };

        mCameraThread.start();
        //Once the picture thread is running and if the network exists, start sending the pictures!
        // and listening for direction responses.
        if(testConnectivity()) {
            mSnapDisbatcher.start();
            mDirectionListener.start();
        } else {
            Toast.makeText(getApplicationContext(), "No network connection.", Toast.LENGTH_LONG).show();
            stopSnapping();
        }
    }

    // ------------------------ Network functions ----------------------------------
    private boolean testConnectivity(){
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }


    // ------------------------- TESTING FUNCTIONS ---------------------------
    private void savePicture(byte[] snap) {
        String storageDir = Environment.getExternalStorageDirectory().toString();
        String filepath = storageDir + "/test.jpg";
        try {
            FileOutputStream outstream = new FileOutputStream(filepath);
            outstream.write(snap, 0, snap.length);
            outstream.close();
        } catch (Exception e) {

        }

    }
}
