package me.adhikasetyap.avidavi.main;

import android.content.Context;
import android.content.Intent;
import android.hardware.SensorEventListener;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.ACTION_SENSOR_OFF;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.ACTION_SENSOR_ON;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.EXTRA_SENSOR_NAME;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.EXTRA_SENSOR_TYPE;

public class SensorListItemView extends ConstraintLayout {

    private static final String TAG = SensorListItemView.class.getName();

    private String sensorName;

    public SensorListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.sensor_list_item_view, this);
    }
}
