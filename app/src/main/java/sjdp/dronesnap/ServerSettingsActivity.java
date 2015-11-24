package sjdp.dronesnap;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ServerSettingsActivity extends Activity {

    static final String PREFS_FILE = "mSharedPreferences";

    private SharedPreferences mSharedPrefs = null;
    private Resources mRes = null;
    private Button mOkayBtn = null;
    private EditText mUrlET = null;
    private EditText mUsernameET = null;
    private EditText mPasswordET = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_settings);

        mSharedPrefs = getSharedPreferences(PREFS_FILE, MODE_PRIVATE);
        mRes = getResources();
        mOkayBtn = (Button) findViewById(R.id.okay_btn);
        mUrlET = (EditText) findViewById(R.id.server_url_et);
        mUsernameET = (EditText) findViewById(R.id.server_username_et);
        mPasswordET = (EditText) findViewById(R.id.server_password_et);

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
        spEditor.putString(mRes.getString(R.string.shared_pref_url), mUrlET.getText().toString());
        spEditor.putString(mRes.getString(R.string.shared_pref_username), mUsernameET.getText().toString());
        spEditor.putString(mRes.getString(R.string.shared_pref_password), mPasswordET.getText().toString());
        spEditor.apply();
    }

    private void loadSavedServerSettings(){
        mUrlET.setText(mSharedPrefs.getString(mRes.getString(R.string.shared_pref_url), ""));
        mUsernameET.setText(mSharedPrefs.getString(mRes.getString(R.string.shared_pref_username), ""));
        mPasswordET.setText(mSharedPrefs.getString(mRes.getString(R.string.shared_pref_password), ""));
    }
}
