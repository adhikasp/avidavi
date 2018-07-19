package me.adhikasetyap.avidavi.main;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.widget.ListView;
import android.widget.SimpleAdapter;

// TODO change this to PreferenceFragment
public class SettingActivity extends PreferenceActivity {

    ListView settingListView;
    SimpleAdapter settingListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.setting_page);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.default_menu, menu);
        return true;
    }
}
