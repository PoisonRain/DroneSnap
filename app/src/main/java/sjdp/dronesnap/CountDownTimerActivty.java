package sjdp.dronesnap;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;

/**
 * Created by Samuel Poulton on 12/10/15.
 * Activity that displays a countdown before the Snapping Activity is launched.
 */
public class CountDownTimerActivty extends Activity {
    private TextView mTimerTextView;
    private PreLaunchTimer mTimer;
    private Resources mRes;
    private Bundle mExtraBundle; //This is to be past on to the Snapping Activity
    private int mInitialDelayedStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_count_down_timer);

        mRes = getResources();

        setupExtras();

        mTimerTextView = (TextView) findViewById(R.id.count_down_timer);
        mTimer = new PreLaunchTimer(mInitialDelayedStart * 60 * 1000, 1000);
        mTimer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTimer.cancel();
    }

    private void setupExtras() {
        Intent parentRequester = this.getIntent();
        mExtraBundle = parentRequester.getExtras();
        mInitialDelayedStart = parentRequester.getIntExtra(mRes.getString(R.string.intent_extra_delayed_start), 0);
    }

    public class PreLaunchTimer extends CountDownTimer {

        public PreLaunchTimer(long startTime, long interval) {
            super(startTime, interval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            int min = (int) ((millisUntilFinished / (1000*60)) % 60);
            int sec = (int) (millisUntilFinished / 1000) % 60 ;

            mTimerTextView.setText(String.format("%02d:%02d", min, sec));
        }

        @Override
        public void onFinish() {
            // Create intent for Snapping Activity here
            Intent i = new Intent(getApplicationContext(), SnappingActivity.class);
            i.putExtras(mExtraBundle);
            startActivity(i);
            finish();
        }
    }
}
