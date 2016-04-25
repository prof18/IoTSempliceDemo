package it.prof.iotsemplicedemo;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by marco on 4/25/16.
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {


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

        BluetoothDevice device = mLeDevices.get(position);
        final String deviceName = device.getName();
        if (deviceName != null && deviceName.length() > 0)
            viewHolder.deviceName.setText(deviceName);
        else
            viewHolder.deviceName.setText("Dispositivo sconosciuto");
        viewHolder.deviceAddress.setText(device.getAddress());


        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {




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