package me.adhikasetyap.avidavi.main.core.utilities;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import me.adhikasetyap.avidavi.main.R;
import me.adhikasetyap.avidavi.main.SensorListItemView;

import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.ACTION_SENSOR_OFF;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.ACTION_SENSOR_ON;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.EXTRA_SENSOR_NAME;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.EXTRA_SENSOR_TYPE;

public class SensorListAdapter extends SimpleAdapter {

    private static final String TAG = SensorListAdapter.class.getName();

    public SensorListAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        TextView v = view.findViewById(R.id.sensor_name);
        String sensorName = v.getText().toString();

        Switch switchButton = view.findViewById(R.id.sensor_switch);
        switchButton.setChecked(true);
        switchButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String action;
            if (isChecked) {
                action = ACTION_SENSOR_ON;
            } else {
                action = ACTION_SENSOR_OFF;
            }
            Intent switchSensor = new Intent(action);
            switchSensor.putExtra(EXTRA_SENSOR_NAME, sensorName);
            LocalBroadcastManager
                    .getInstance(view.getContext())
                    .sendBroadcast(switchSensor);
        });

        return view;
    }
}
