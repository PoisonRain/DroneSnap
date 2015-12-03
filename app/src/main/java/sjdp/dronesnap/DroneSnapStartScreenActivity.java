package sjdp.dronesnap;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Samuel Poulton on 11/21/15.
 */
public class DroneSnapStartScreenActivity extends Activity {

    private Resources mRes = null;
    private EditText mFlightName = null;
    private EditText mDelayedStart = null;
    private EditText mTimeLapse = null;
    private Button mStartBtn = null;
    private Button mServerSettingsBtn = null;

    static final private String DEFAULT_DELAY = "0";
    static final private String DEFAULT_LAPSE = "4000";
    static final private int SNAP_STATS_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drone_snap_start_screen);

        mRes = getResources();
        mFlightName = (EditText) findViewById(R.id.flight_name_et);
        mDelayedStart = (EditText) findViewById(R.id.delayed_start_et);
        mTimeLapse = (EditText) findViewById(R.id.time_lapse_et);

        setEditTextDefaults();

        mStartBtn = (Button) findViewById(R.id.start_btn);
        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), SnappingActivity.class);
                i.putExtra(
                        mRes.getString(R.string.intent_extra_flight_name),
                        mFlightName.getText().toString());
                i.putExtra(
                        mRes.getString(R.string.intent_extra_delayed_start),
                        Integer.parseInt(mDelayedStart.getText().toString()));
                i.putExtra(
                        mRes.getString(R.string.intent_extra_time_lapse),
                        Long.parseLong(mTimeLapse.getText().toString()));

                try {
                    startActivityForResult(i, SNAP_STATS_REQUEST_CODE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mServerSettingsBtn = (Button) findViewById(R.id.server_settings_btn);
        mServerSettingsBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getApplicationContext(), ServerSettingsActivity.class);
                        try {
                            startActivity(i);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent returnIntent){
        super.onActivityResult(requestCode, resultCode, returnIntent);

        if(requestCode == SNAP_STATS_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            long flightDuration = returnIntent.getLongExtra(mRes.getString(R.string.intent_extra_flight_duration), 0);
            int snapCount = returnIntent.getIntExtra(mRes.getString(R.string.intent_extra_snap_count), 0);
            String result = "Flight Duration: " + flightDuration + "\n Total Snaps: " + snapCount;

            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
        }
    }

    private void setEditTextDefaults() {
        SimpleDateFormat fmt = new SimpleDateFormat("MM_dd_yyyy");
        Date date = new Date();
        String dateString = fmt.format(date);

        mFlightName.setText(dateString);
        mDelayedStart.setText(DEFAULT_DELAY);
        mTimeLapse.setText(DEFAULT_LAPSE);

    }
}
