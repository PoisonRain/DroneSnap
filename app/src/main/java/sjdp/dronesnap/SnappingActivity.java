package sjdp.dronesnap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.widget.Toast;

/**
 * Created by Samuel Poulton on 11/21/15.
 */
public class SnappingActivity extends Activity implements OnClickListener{
    static final private String LOG_TAG = "LOG_SEE_ME";

    private int mNumberOfSnapsTaken = 0;
    private String mFlightName = "";
    private int mInitialDelayedStart = 0;
    private long mTimeLapseDelay = 0;
    private boolean isSnapping = true;
    private long mDurationOfFlight = 0;
    private Resources mRes = null;
    private Camera mCamera;
    private CameraPreview mPreview;
    private SnapDisbatcher mSnapDisbatcher;

    // Widgets
    private TextView mNumSnapET = null;
    private Chronometer mFlightDuraChrono = null;
    private Button mPauseResumeBtn = null;
    private Button mStopBtn = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snapping);
        mRes = getResources();
        inflateWidgets();
        setParentIntentExtras();

        mSnapDisbatcher = new SnapDisbatcher();
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
    public void onClick(View v) {
        Log.d(LOG_TAG, "onclicked fired");
        switch(v.getId()) {
            case R.id.pause_resume_btn:
                if(isSnapping)
                    pauseSnapping();
                else
                    resumeSnapping();
                break;
            case R.id.stop_btn:
                stopSnapping();
                break;
        }
    }

    private void startSnapping(){
        Log.d(LOG_TAG, "startSnapping");

        mFlightDuraChrono.setBase(SystemClock.elapsedRealtime());
        mNumSnapET.setText("0");

        if(mInitialDelayedStart > 0) {
            try {
                //Convert minutes to milliseconds
                //TODO: Change this to a count down timer of some sort
                // Potential use a Handler: http://stackoverflow.com/questions/6242268/repeat-a-task-with-a-time-delay
                Thread.sleep(mInitialDelayedStart * 60 * 1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void resumeSnapping(){
        Log.d(LOG_TAG, "resumeSnapping");
        mPauseResumeBtn.setText(mRes.getString(R.string.pause_btn));

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

        mPauseResumeBtn.setText(mRes.getString(R.string.start_btn));
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
        Intent resultIntent = new Intent();
        resultIntent.putExtra(mRes.getString(R.string.intent_extra_flight_duration), mDurationOfFlight);
        resultIntent.putExtra(mRes.getString(R.string.intent_extra_snap_count), mNumberOfSnapsTaken);

        setResult(RESULT_OK, resultIntent);
        finish();
    }


    private void inflateWidgets() {
        mNumSnapET = (TextView) findViewById(R.id.stat_num_of_snaps);
        mFlightDuraChrono = (Chronometer) findViewById(R.id.stat_flight_duration);

        mPauseResumeBtn = (Button) findViewById(R.id.pause_resume_btn);
        mPauseResumeBtn.setOnClickListener(this);

        mStopBtn = (Button) findViewById(R.id.stop_btn);
        mStopBtn.setOnClickListener(this);
    }

    private void setParentIntentExtras() {
        Intent parentRequester = this.getIntent();
        mFlightName = parentRequester.getStringExtra(mRes.getString(R.string.intent_extra_flight_name));
        mInitialDelayedStart = parentRequester.getIntExtra(mRes.getString(R.string.intent_extra_delayed_start), 0);
        mTimeLapseDelay = parentRequester.getLongExtra(mRes.getString(R.string.intent_extra_time_lapse), 1000);
    }

    // ---------------------------- CAMERA FUNCTIONS ----------------------------------
    private static Camera setCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
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
            mSnapDisbatcher.addSnaptoUploadList(data);
        }
    };


    private void startCameraSnappingThread(){
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();

                while (isSnapping) {
                    Log.d(LOG_TAG, "Attempting to take picture now.");
                    try {
                        mCamera.takePicture(null, null, mPicture);

                        Log.d(LOG_TAG, "Attempting to update snaps counter");
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

                    Log.d(LOG_TAG, "Attempting to sleep now");
                    try {
                        sleep(mTimeLapseDelay);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        Log.d(LOG_TAG, e.getMessage());
                    }

                    Log.d(LOG_TAG, "NUMBER OF SNAPS: " + mNumberOfSnapsTaken);
                }
            }
        };
        thread.start();
        //Once the picture thread is running, start sending the pictures!
        if(testNetwork()) {
            mSnapDisbatcher.start();
        } else {
            Toast.makeText(getApplicationContext(), "No network connection.", Toast.LENGTH_SHORT).show();
            stopSnapping();
        }
    }

    // ------------------------ Disbatcher functions ----------------------------------
    private boolean testNetwork(){
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
}
