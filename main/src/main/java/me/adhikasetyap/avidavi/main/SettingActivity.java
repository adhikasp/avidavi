package me.adhikasetyap.avidavi.main;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.HashMap;

// TODO change this to PreferenceFragment
public class SettingActivity extends PreferenceActivity {

    ListView settingListView;
    SimpleAdapter settingListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.setting_page);

//        Toolbar myToolbar = findViewById(R.id.app_toolbar);
//        setSupportActionBar(myToolbar);
//
//        List<HashMap<String, String>> setting = new ArrayList<>();
//        setting.add(new SettingItem("Server IP Address", "192.168.100.6")
//                .asHashMap());
//        setting.add(new SettingItem("Server Port", "5020")
//                .asHashMap());
//
//        String[] fromColumns = {"label", "value"};
//        int[] toColumns = {R.id.title, R.id.value};
//        settingListView = findViewById(R.id.setting_list);
//        settingListAdapter = new SimpleAdapter(
//                this, setting, R.layout.setting_item, fromColumns, toColumns);
//
//        settingListView.setAdapter(settingListAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.default_menu, menu);
        return true;
    }

    private class SettingItem {
        String label;
        String value;

        SettingItem(String label, String defaultValue) {
            this.label = label;
            this.value = defaultValue;
        }

        HashMap<String, String> asHashMap() {
            HashMap<String, String> h = new HashMap<>();
            h.put("label", label);
            h.put("value", value);
            return h;
        }
    }
}
