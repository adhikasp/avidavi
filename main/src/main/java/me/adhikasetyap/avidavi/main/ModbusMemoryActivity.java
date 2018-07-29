package me.adhikasetyap.avidavi.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.ACTION_MEMORY_READ_REQUEST;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.ACTION_MEMORY_READ_RESPONSE;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.EXTRA_MEMORY_ADDRESS_NUM;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.EXTRA_MEMORY_ADDRESS_QUANTITY;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.EXTRA_MEMORY_ADDRESS_START;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.EXTRA_MEMORY_ADDRESS_TYPE;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.EXTRA_MEMORY_ADDRESS_VALUE;

public class ModbusMemoryActivity extends AppCompatActivity {

    private static final String TAG = ModbusMemoryActivity.class.getName();

    private static final String[] MODBUS_MEMORY_TYPE = {"Coils", "Discrete inputs", "Input registers", "Holding registers"};

    private Spinner memoryTypeSelection;
    private EditText startAddressText;
    private EditText addressQuantityText;
    private Button scanMasterButton;

    private ListView memoryListView;
    private SimpleAdapter memoryListAdapter;
    private List<HashMap<String, Integer>> memoryListData;
    private View.OnClickListener sendScanRequest = v -> {
        String memoryType = memoryTypeSelection.getSelectedItem().toString();
        int startAddress = Integer.valueOf(startAddressText.getText().toString());
        int addressQuantity = Integer.valueOf(addressQuantityText.getText().toString());

        Intent scanRequest = new Intent(ACTION_MEMORY_READ_REQUEST);
        scanRequest.putExtra(EXTRA_MEMORY_ADDRESS_TYPE, memoryType);
        scanRequest.putExtra(EXTRA_MEMORY_ADDRESS_START, startAddress);
        scanRequest.putExtra(EXTRA_MEMORY_ADDRESS_QUANTITY, addressQuantity);
        LocalBroadcastManager.getInstance(this).sendBroadcast(scanRequest);
    };
    private BroadcastReceiver onScanResponse = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int[] values = intent.getIntArrayExtra(EXTRA_MEMORY_ADDRESS_VALUE);
            int[] addresses = intent.getIntArrayExtra(EXTRA_MEMORY_ADDRESS_NUM);

            memoryListData = new ArrayList<>();
            for (int i = 0; i < addresses.length; i++) {
                HashMap<String, Integer> data = new HashMap<>();
                data.put("address", addresses[i]);
                data.put("value", values[i]);
                memoryListData.add(data);
            }

            // Construct new adapter each time we got update
            // https://stackoverflow.com/questions/3313347/how-to-update-simpleadapter-in-android
            String[] from = {"address", "value"};
            int[] to = {R.id.memory_address, R.id.memory_value};
            memoryListAdapter = new SimpleAdapter(
                    ModbusMemoryActivity.this,
                    memoryListData,
                    R.layout.memory_list_item_view,
                    from,
                    to
            );
            memoryListView.setAdapter(memoryListAdapter);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modbus_memory_activity);

        Toolbar myToolbar = findViewById(R.id.app_toolbar);
        setSupportActionBar(myToolbar);

        // INPUT
        memoryTypeSelection = findViewById(R.id.choose_memory_type);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                MODBUS_MEMORY_TYPE
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        memoryTypeSelection.setAdapter(adapter);

        scanMasterButton = findViewById(R.id.scan);
        scanMasterButton.setOnClickListener(sendScanRequest);

        startAddressText = findViewById(R.id.start_address);
        addressQuantityText = findViewById(R.id.address_quantity);

        // Memory List View
        memoryListView = findViewById(R.id.memory_list_view);

        IntentFilter filter = new IntentFilter(ACTION_MEMORY_READ_RESPONSE);
        LocalBroadcastManager.getInstance(this).registerReceiver(onScanResponse, filter);
    }
}
