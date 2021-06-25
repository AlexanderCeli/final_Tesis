package com.example.contcasa;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {


    ImageButton btnOn,btnOff,btnMotorON,btnMotorof,btnventron,btnventrof;
    TextView   sensorView1, conectado;
    Handler bluetoothIn;
    Button alarm ;


    final int handlerState = 0;        				 // utilizado para identificar el mensaje del manejador
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();

    private ConnectedThread mConnectedThread;

    // Servicio de UUID SPP: esto debería funcionar para la mayoría de los dispositivos
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Cadena para la dirección MAC
    private static String address = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Vincular los botones y textViews a las respectivas vistas
        btnOn = (ImageButton) findViewById(R.id.buttonOn);
        btnOff = (ImageButton) findViewById(R.id.buttonOff);
        btnMotorON =(ImageButton) findViewById(R.id.buttonCortOn);
        btnMotorof =(ImageButton) findViewById(R.id.buttonCortOff);
        btnventron =(ImageButton) findViewById(R.id.buttonVenOn);
        btnventrof =(ImageButton) findViewById(R.id.buttonVenOff);
        alarm =(Button) findViewById(R.id.alrm);


        sensorView1 = (TextView) findViewById(R.id.sensorView1);

        conectado = (TextView) findViewById(R.id.textView);



        alarm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, alarma.class);
                startActivity(i);
            }
        });

        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                Log.e("handleMessage: ", msg.toString() );
            }
        };


        btAdapter = BluetoothAdapter.getDefaultAdapter();       // obtén el adaptador Bluetooth
        checkBTState();


        // Configurar oyentes onClick para que los botones envíen datos para encender / apagar
        btnOff.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("aa");    // Enviar por Bluetooth
                Toast.makeText(getBaseContext(), "Apagar el LED", Toast.LENGTH_SHORT).show();


            }
        });

        btnOn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("bb");    // Enviar por Bluetooth
                Toast.makeText(getBaseContext(), "Encender el LED", Toast.LENGTH_SHORT).show();


            }
        });
        btnMotorof.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("qq");    // Enviar por Bluetooth


            }
        });
        btnMotorON.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("ww");    // Enviar por Bluetooth


            }
        });
        btnventrof.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("tt");    // Enviar por Bluetooth


            }
        });
        btnventron.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("rr");    // Enviar por Bluetooth

            }
        });

    }


    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        // crea una conexión saliente segura con un dispositivo BT usando UUID
    }

    @Override
    public void onResume() {
        super.onResume();

        // Obtener la dirección MAC de DeviceListActivity a través de la intención
        Intent intent = getIntent();

        // Obtenga la dirección MAC de DeviceListActivty a través de EXTRA
        address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        // crear dispositivo y configurar la dirección MAC
        //Log.i("ramiro", "adress : " + address);
        BluetoothDevice device = btAdapter.getRemoteDevice(address);
        conectado.setText(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
        }
        // Establezca la conexión de la toma Bluetooth.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try
            {
                btSocket.close();
            } catch (IOException e2)
            {

            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        // Envío un carácter al reanudar la transmisión inicial para comprobar que el dispositivo está conectado
        // Si no es una excepción, se lanzará en el método de escritura y se llamará a finish ()
        mConnectedThread.write("x");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            // No deje las tomas de Bluetooth abiertas cuando deje la actividad
            btSocket.close();
        } catch (IOException e2) {

        }
    }

    //Comprueba que el dispositivo Android Bluetooth esté disponible y solicita que se encienda si está apagado
    private void checkBTState() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //crear una nueva clase para conectar el hilo
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creación del hilo de conexión
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
           // sensorView1.setText(mConnectedThread.getName());

            try {
                //Crea flujos de I/O para la conexión
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;

            mmOutStream = tmpOut;
        }


        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Siga repitiendo para escuchar los mensajes recibidos
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);        	//leer bytes del búfer de entrada
                    String readMessage = new String(buffer, 0, bytes);
                    Log.d("BLUETHOO", "run: "+readMessage );
                    // Envíe los bytes obtenidos a la actividad de la interfaz de usuario a través del controlador via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //convierte la cadena ingresada en bytes
            try {
                mmOutStream.write(msgBuffer);                //escribir bytes a través de la conexión BT a través de la salida
            } catch (IOException e) {
                // si no puedes escribir, cierra la aplicación
                Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
                finish();

            }
        }
    }

}
