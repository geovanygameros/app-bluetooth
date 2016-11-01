package com.example.geovany.android_arduino;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.logging.Handler;


/**
 * Created by geovany on 10/20/16.
 */

public class ControlFragment extends Fragment implements View.OnClickListener{
    View myView;
    ImageButton btnEstancia, btnCochera, btnSala, btnCocheraEstancia, btnEstanciaSala, btnCocheraSala;

    final int handlerState = 0;        				 //used to identify handler message
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();

    private ConnectedThread mConnectedThread;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    private static String address = null;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.control_layout, container, false);
        getActivity().setTitle("Control");

        //link buttons
        btnCochera = (ImageButton) myView.findViewById(R.id.btnCochera);
        btnEstancia = (ImageButton) myView.findViewById(R.id.btnEstancia);
        btnSala = (ImageButton) myView.findViewById(R.id.btnSala);
        btnCocheraEstancia = (ImageButton) myView.findViewById(R.id.btnCocheraEstancia);
        btnEstanciaSala = (ImageButton) myView.findViewById(R.id.btnEstanciaSala);
        btnCocheraSala = (ImageButton) myView.findViewById(R.id.btnCocheraSala);

        btnCochera.setOnClickListener(this);
        btnEstancia.setOnClickListener(this);
        btnSala.setOnClickListener(this);
        btnCocheraEstancia.setOnClickListener(this);
        btnEstanciaSala.setOnClickListener(this);
        btnCocheraSala.setOnClickListener(this);



        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();

        return myView;

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnCochera:
                mConnectedThread.write("2");    // Send "0" via Bluetooth
                Toast.makeText(getActivity(),"Cochera",Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnEstancia:
                Toast.makeText(getActivity(),"Estancia",Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnSala:
                Toast.makeText(getActivity(),"Sala",Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnCocheraEstancia:
                Toast.makeText(getActivity(),"Cochera/Estancia",Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnCocheraSala:
                Toast.makeText(getActivity(),"Cochera/Sala",Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnEstanciaSala:
                Toast.makeText(getActivity(),"Estancia/Sala",Toast.LENGTH_SHORT).show();
                break;

        }
    }
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }
    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {

        if(btAdapter==null) {
            Toast.makeText(getActivity(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }
    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }



        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getActivity(), "La Conexión fallo", Toast.LENGTH_LONG).show();


            }
        }
    }
    @Override
    public void onResume() {
        super.onResume();

        //Get MAC address from DeviceListActivity via intent
        //Intent intent = getActivity().getIntent();

        //Get the MAC address from the DeviceListActivty via EXTRA
        address = getArguments().getString(AjustesFragment.EXTRA_DEVICE_ADDRESS);

        //create device and set the MAC address
        //Log.i("ramiro", "adress : " + address);
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getActivity(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try
            {
                btSocket.close();
            } catch (IOException e2)
            {
                //insert code to deal with this
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("x");
    }
}
