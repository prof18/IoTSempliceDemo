/*  Copyright (c) 2016 Marco Gomiero
*   
*   Permission is hereby granted, free of charge, to any person obtaining a copy 
*   of this software and associated documentation files (the "Software"), to deal 
*   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
*   copies of the Software, and to permit persons to whom the Software is
*   furnished to do so, subject to the following conditions:
*
*   The above copyright notice and this permission notice shall be included in all 
*   copies or substantial portions of the Software.
*
*   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
*   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
*   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
*   THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
*   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
*   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
*   SOFTWARE. 
*/

package it.prof.iotsemplicedemo;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Scan and connect to a Bluetooth LE device
 * 
 * Some part of the activity is adapted from the googlesamples/android-BluetoothLeGatt
 */

public class DeviceScanActivity extends AppCompatActivity {

    private MyAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    private BluetoothDevice device;
    private BluetoothLeService mBluetoothLeService;

    private static final int REQUEST_ENABLE_BT = 1;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private boolean mConnected = false;

    private String uuid;
    private int time;

    private RecyclerView mRecyclerView;
    private ArrayList<BluetoothDevice> bDevices;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private android.app.AlertDialog alert;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.listitem_device);

        getSupportActionBar().setTitle(R.string.choose);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecyclerView = (RecyclerView) findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);

        setupSwipe();

       //Runtime permission; necessary to work on API 23.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);

            }

        }

        mHandler = new Handler();
        InizializeBluetooth();

    }

   //swipe to restart a scan
    public void setupSwipe() {

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.container);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {

                mLeDeviceListAdapter.clear();
                mSwipeRefreshLayout.setRefreshing(true);
                scanLeDevice(true);

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("", "Coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.perm_error_title);
                    builder.setMessage(R.string.perm_error_msg);
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
            }
        }
    }

   //check if bluetooth is enabled and set the adapter. If not, open a dialog.
    private void InizializeBluetooth()  {

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }


    //check bluetooth and repopulate list/set adapter
    @Override
    protected void onResume() {
        super.onResume();

        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        bDevices = new ArrayList<BluetoothDevice>();
        mLeDeviceListAdapter = new MyAdapter(bDevices,R.layout.row, this);
        mRecyclerView.setAdapter(mLeDeviceListAdapter);

        scanLeDevice(true);

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {

            unbindService(mServiceConnection);
            mBluetoothLeService = null;

        } catch (IllegalArgumentException e)
        {}

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:

                super.onBackPressed();
                break;

        }
        return super.onOptionsItemSelected(item);

    }


    //if the user deny bluetooth, exit from activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


   //clear the adapter and unregister the receiver
    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        unregisterReceiver(mGattUpdateReceiver);
        mLeDeviceListAdapter.clear();
    }

    // Manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        //what to do when connected to the service
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e("", "Unable to initialize Bluetooth");
                finish();
            }

            //if connected send a toast message
            if(mBluetoothLeService.connect(device.getAddress()))
            {
                Toast.makeText(DeviceScanActivity.this, "Connected to: " + device.getName(),
                        Toast.LENGTH_LONG).show();

               //close activity 1 seconds after the led is off
                Handler handler = new Handler();
                Bundle extras1 = getIntent().getExtras();
                int time1 = extras1.getInt("timeExtra");

                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(DeviceScanActivity.this);
                        builder.setMessage(R.string.dialog_message)
                                .setTitle(R.string.dialog_title)
                                .setCancelable(false)
                                .setPositiveButton("OK",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id) {
                                                finish();
                                            }
                                        })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                    alert.dismiss();
                                            }
                                        });

                        alert = builder.create();
                        alert.show();
                    }
                };

                handler.postDelayed(r, (time1 + 1)*1000);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    
    //manage connected, disconnected and discovered action 
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {

                mConnected = true;

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {

                mConnected = false;

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {

                //check if the connected device is an iBlio Device and write the characteristic
                try {

                    BluetoothGattService mSVC = mBluetoothLeService.getSupportedGattServices().get(5);

                    // creating UUID
                    Bundle extras = getIntent().getExtras();
                    uuid = extras.getString("uuidExtra");
                    System.out.print(uuid);
                    time = extras.getInt("timeExtra");

                    UUID uid = UUID.fromString(uuid);
                    BluetoothGattCharacteristic mCH = mSVC.getCharacteristic(uid);
                    mBluetoothLeService.writeCharacteristic(mCH, time);

                } catch (NullPointerException e) {

                    Toast.makeText(DeviceScanActivity.this, R.string.no_iblio_device, Toast.LENGTH_LONG).show();

                } catch (IndexOutOfBoundsException e) {

                    Toast.makeText(DeviceScanActivity.this, R.string.no_iblio_device, Toast.LENGTH_LONG).show();

                }
            }
        }
    }
        ;

        private static IntentFilter makeGattUpdateIntentFilter() {
            final IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
            intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
            intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
            intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
            return intentFilter;
        }

        //scan device. It use some deprecated methods
        private void scanLeDevice(final boolean enable) {

            if (enable) {
                // Stops scanning after a pre-defined scan period.
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mScanning = false;
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        Toast.makeText(DeviceScanActivity.this, R.string.scan_stopped, Toast.LENGTH_SHORT).show();
                        invalidateOptionsMenu();
                    }
                }, SCAN_PERIOD);

                mScanning = true;
                mBluetoothAdapter.startLeScan(mLeScanCallback);

            } else {

                mScanning = false;
                mBluetoothAdapter.stopLeScan(mLeScanCallback);

            }
            invalidateOptionsMenu();
        }


        // Device scan callback.
        private BluetoothAdapter.LeScanCallback mLeScanCallback =
                new BluetoothAdapter.LeScanCallback() {

                    @Override
                    public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //stop swwipe refresh and add a new device
                                mSwipeRefreshLayout.setRefreshing(false);
                                mLeDeviceListAdapter.addDevice(device);
                                mLeDeviceListAdapter.notifyDataSetChanged();


                            }
                        });
                    }
                };

    //adapter for the racycler view
    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater inflater;

        private int rowLayout;
        private Context mContext;

        public MyAdapter(ArrayList<BluetoothDevice> list, int rowLayout, Context context) {

            this.mLeDevices = list;
            this.rowLayout = rowLayout;
            this.mContext = context;

        }

        public void addDevice(BluetoothDevice device) {

            if(!mLeDevices.contains(device)) {

                mLeDevices.add(device);

            }
        }

        public BluetoothDevice getDevice(int position) {

            return mLeDevices.get(position);

        }

        public void clear() {

            mLeDevices.clear();

        }

        @Override
        public long getItemId(int i) {

            return i;

        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

            View v = LayoutInflater.from(viewGroup.getContext()).inflate(rowLayout, viewGroup, false);
            return new ViewHolder(v);

        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final int position)  {

            final BluetoothDevice mDevice = mLeDevices.get(position);
            final String deviceName = mDevice.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.no_name_device);
            viewHolder.deviceAddress.setText(mDevice.getAddress());

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    device = mLeDeviceListAdapter.getDevice(position);

                    Intent gattServiceIntent = new Intent(getApplicationContext(), BluetoothLeService.class);
                    bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

                }
            });
        }

        @Override
        public int getItemCount() {

            return mLeDevices.size();

        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView deviceName;
            public TextView deviceAddress;
            ImageView image;

            public ViewHolder(View itemView) {

                super(itemView);

                deviceName = (TextView) itemView.findViewById(R.id.device_name);
                deviceAddress = (TextView) itemView.findViewById(R.id.device_address);
                image = (ImageView)itemView.findViewById(R.id.image);

            }

        }

    }

}