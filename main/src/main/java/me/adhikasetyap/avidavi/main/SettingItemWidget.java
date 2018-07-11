package me.adhikasetyap.avidavi.main;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SettingItemWidget extends LinearLayout {

    private TextView settingTitle;
    private TextView settingValue;

    public SettingItemWidget(Context context, AttributeSet attrs) {
        super(context);
        inflate(context, R.layout.setting_item, this);

        settingTitle = findViewById(R.id.title);
        settingValue = findViewById(R.id.value);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SettingItemWidget);
        settingTitle.setText(a.getText(R.styleable.SettingItemWidget_setting_title));
        settingValue.setText(a.getText(R.styleable.SettingItemWidget_setting_value));
        a.recycle();
    }
}
