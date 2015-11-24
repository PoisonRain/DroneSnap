package sjdp.dronesnap;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View.OnClickListener;

public class SnappingActivity extends Activity implements OnClickListener{
    static final private String LOG_TAG = "LOG_SEE_ME";

    private int mNumberOfSnapsTaken = 0;
    private String mFlightName = "";
    private int mInitialDelayedStart = 0;
    private long mTimeLapseDelay = 0;
    private boolean isSnapping = true;
    private long mDurationOfFlight = 0;
    private Resources mRes = null;

    private CameraFeed mCamera = null;
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
        mCamera = new CameraFeed();

        inflateWidgets();
        setParentIntentExtras();

        startSnapping();
    }

    @Override
    protected void onPause(){
        super.onPause();
        pauseSnapping();
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
                //Convert seconds to milliseconds
                Thread.sleep(mInitialDelayedStart * 60 * 1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        mFlightDuraChrono.start();
    }

    private void pauseSnapping(){
        Log.d(LOG_TAG, "pauseSnapping");
        mFlightDuraChrono.stop();
        mDurationOfFlight =  SystemClock.elapsedRealtime() - mFlightDuraChrono.getBase();
        mPauseResumeBtn.setText(mRes.getString(R.string.start_btn));
        isSnapping = false;
    }

    private void resumeSnapping(){
        Log.d(LOG_TAG, "resumeSnapping");
        mFlightDuraChrono.setBase(SystemClock.elapsedRealtime() - mDurationOfFlight);
        mFlightDuraChrono.start();
        mPauseResumeBtn.setText(mRes.getString(R.string.pause_btn));
        isSnapping = true;
    }

    private void stopSnapping(){
        Log.d(LOG_TAG, "stopSnapping");
        mFlightDuraChrono.stop();
        mDurationOfFlight =  SystemClock.elapsedRealtime() - mFlightDuraChrono.getBase();
        isSnapping = false;

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
}
