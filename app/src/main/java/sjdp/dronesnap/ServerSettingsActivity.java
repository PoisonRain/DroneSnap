package sjdp.dronesnap;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ServerSettingsActivity extends Activity {

    static final String PREFS_FILE = "mSharedPreferences";

    private SharedPreferences mSharedPrefs = null;
    private Resources mRes = null;
    private Button mOkayBtn = null;
    private EditText mServerUrlET = null;
    private EditText mServerPortET = null;
    private EditText mPhoneUrlET = null;
    private EditText mPhonePortET = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_settings);

        mSharedPrefs = getSharedPreferences(PREFS_FILE, MODE_PRIVATE);
        mRes = getResources();
        mOkayBtn = (Button) findViewById(R.id.okay_btn);

        mServerUrlET = (EditText) findViewById(R.id.server_url_et);
        mServerPortET = (EditText) findViewById(R.id.server_port_et);

        mPhoneUrlET = (EditText) findViewById(R.id.phone_url_et);
        mPhonePortET = (EditText) findViewById(R.id.phone_port_et);

        loadSavedServerSettings();

        mOkayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add code to save the settings
                saveServerSettings();
                finish();
            }
        });
    }

    @Override
    protected void onPause(){
        super.onPause();
        saveServerSettings();
    }

    private void saveServerSettings() {
        SharedPreferences.Editor spEditor = mSharedPrefs.edit();
        spEditor.putString(mRes.getString(R.string.shared_pref_server_url), mServerUrlET.getText().toString());
        spEditor.putString(mRes.getString(R.string.shared_pref_server_port), mServerPortET.getText().toString());
        spEditor.putString(mRes.getString(R.string.shared_pref_phone_url), mPhoneUrlET.getText().toString());
        spEditor.putString(mRes.getString(R.string.shared_pref_phone_port), mPhonePortET.getText().toString());
        spEditor.apply();
    }

    private void loadSavedServerSettings(){
        String x = mRes.getString(R.string.shared_pref_server_port, 0);
        Log.d("LOG", x);
        mServerUrlET.setText(mSharedPrefs.getString(mRes.getString(R.string.shared_pref_server_url), ""));
        mServerPortET.setText(mSharedPrefs.getString(mRes.getString(R.string.shared_pref_server_port), ""));
        mPhoneUrlET.setText(mSharedPrefs.getString(mRes.getString(R.string.shared_pref_phone_url), ""));
        mPhonePortET.setText(mSharedPrefs.getString(mRes.getString(R.string.shared_pref_phone_port), ""));
    }
}
