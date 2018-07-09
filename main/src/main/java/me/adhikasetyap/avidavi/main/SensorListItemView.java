package me.adhikasetyap.avidavi.main;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;

public class SensorListItemView extends ConstraintLayout {
    public SensorListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.sensor_list_item_view, this);
    }
}
