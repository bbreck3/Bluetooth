package bluetooth.example.com.bluetooth;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.widget.CompoundButton;
import android.app.AlertDialog;


import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


public class MainActivity extends Activity implements AdapterView.OnItemSelectedListener {
    private static final int REQUEST_ENABLE_BT = 1;
    private Button onBtn;
    private Button offBtn;
    private Button list_paired;
    private Button findBtn;
    private Button graph;
    private TextView text;
    private BluetoothAdapter myBluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;

    private ArrayAdapter<String> BTArrayAdapter;
    private ArrayAdapter<String> bt_dev_list_adapt;
    //lsit of bluetooth devices...
    private final ArrayList<String> bt_dev_list = new ArrayList<String>();


    private Button rec_data; // record data
    private ListView rec_data_list; // list view to add the recorded data to
    private ListView select_val_list;

    private TextView dig_volt; // textview to hold the current current digital voltage
    private TextView pound; // textview to hold the current pounds of weight
    private ToggleButton toggleButton_sound, toggleButton_bluetooth;
    private TextView bluetooth_status;
    private TextView select_pound;
    private ListView  list_paired_dev;
    //my attempt to use a time delay --> while searching for devices : if none found to make a output that says no devices found please try again.... --> doesnt work correctly
    final int secondsDelayed = 1;
    Context main_activity;

    Toast toast;


    private Spinner spinner;

    // The follow lines are for the blueooth pairing capability in app (programmatically)
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    public BluetoothDevice btDevice;
    private BluetoothSocket socket;
    private BluetoothSocket  temp;
    private String password;
    private String MAC_temp, MAC;
    protected static final int SUCCESS_CONNECT = 0;
    protected static final int MESSAGE_READ = 1;


    //The following lines are for the thread..
    ConnectedThread thread;
    Button refresh;

   // private final InputStream mmInStream;
    //private final OutputStream mmOutStream;
    byte[] buffer;


    /**
     *
     *          Message Handler thread --> passes messages between the connected bluetooth device over the socket
     *
     *
     * @param savedInstanceState
     */

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
           // Log.i("tag", "in handler");

           // Toast.makeText(getApplicationContext(),"Reached message Handler", Toast.LENGTH_LONG).show();
            super.handleMessage(msg);
            switch(msg.what){
                case SUCCESS_CONNECT:
                    // DO something
                    ConnectedThread connectedThread = new ConnectedThread((BluetoothSocket)msg.obj);
                    Toast.makeText(getApplicationContext(), "CONNECT", Toast.LENGTH_LONG).show();
                    String s = "successfully connected";
                    connectedThread.write(s.getBytes());
                   // Log.i("tag", "connected");
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[])msg.obj;
                    String string = new String(readBuf);
                    pound.setText("Pound: " + string);
                    Toast.makeText(getApplicationContext(), string, Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         *
         *      Below Provides a an alert to the patient in order to inform them to turn on the "Alarm" Sound notification by pressing the Off button
         */


       AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Alert Dialog");
        alertDialog.setMessage("Press to Turn On Alert,If of choose from select values to activate alert.");
        //alertDialog.setIcon(R.drawable.welcome);

       alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              //confirmation message to user if needed..
            }
        });

        alertDialog.show();


        /**
         *
         *          Bellow is 2 arrays to contain the data for the to list views;
         *          I chose an arraylist becuase it is much easier to add data to an arraylist than to create a node list using a stack or queue data structure
         *
         *          It is literal just : <listname>.add(<date to add here>)
         *
         *          listdata is the list that contains the list that will contain the the values from the Record data button: (Simple grabs data from the Valtage and pounds textviews
         *
         *          static_data_list is the list thta contains the data that will be obly for pounds. This is the list that will contain the data to select
         *          a specifc pounds if not taken from
         *          the pounds text view
         *
         *
         *
         */

        //list of the data that simply grabs the pounds
         final ArrayList<String> listdata = new ArrayList<String>();

        //list of the pounds only --> to be used for selection if selected a specific pound measurement as apposed to taing in from the input at the top
        // --> also to be integrated with the toggle on / off sound
         final ArrayList<String> static_data_list = new ArrayList<String>();
            //Add values to the list --> preset pound values that can be selected form if not taken from microcontroller bluetooth input...
        static_data_list.add("1");
        static_data_list.add("3");
        static_data_list.add("5");
        static_data_list.add("6");
        static_data_list.add("7");
        static_data_list.add("8");




        /*

        Testing the the Button capture of the data record

        */




            // basis textview and buttons setup
        dig_volt = (TextView)findViewById(R.id.textViewVolt); // this is the text view that reads "Voltge" on the app
        pound = (TextView)findViewById(R.id.textView_Pounds); // this is the textView that reads "Pounds" in the app
        rec_data = (Button)findViewById(R.id.btn_records); // this is the button the reads "RECORD DATA"
        rec_data_list = (ListView)findViewById(R.id.listView_Data); // this is the list view that will contain data from "listdata" above
        select_val_list = (ListView)findViewById(R.id.listView_selectData_1); // this is the listview that will contain data from "static_dat_list" above
        list_paired_dev= (ListView)findViewById(R.id.list_paired_dev); //lsit view that displays the bluetooth device that were found during the search
        bluetooth_status = (TextView)findViewById(R.id.bluetooth_Status);// defines the status text view: "ENABLED" if bluetooth is on, "DISABLED" id it is off
        spinner = (Spinner)findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        refresh = (Button)findViewById(R.id.refresh);
        //contect of the main activity
        main_activity = this;

        // button to get to the graph activity (Screen)
        graph = (Button)findViewById(R.id.to_graph);
        graph.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                   // setContentView(R.layout.activity_graph);
                Intent intent = new Intent(MainActivity.this,graph.class);
                startActivity(intent);
            }
        });

        /**
         *
         *      Refresh: onClickListener -->when cliked is the socket is connected --> run the thread again the get different values
         *
         */

            refresh.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(socket.isConnected()){
                        thread.run();
                    }

                }
            });
        // Define a new Adapter
        // First parameter - Context
        // Second parameter - Layout for the row
        // Third parameter - ID of the TextView to which the data is written
        // Forth - the Array of data


        /**
         *
         *      Inorder the pass data from a list of data (an array) to a listview you have to use an adapter
         *
         *      below defines the adapter for the record data list and the pounds only data
         *
         *      data_adapter is the adapter for the rec_data_list above
         *
         *      select_adapter is the adapter for the select_val_list above
         *
         *
         *
         */

        // Record Data List Value: Adapter
        ArrayAdapter<String> data_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1,listdata);

        // Select Value List View Adapter
        ArrayAdapter<String> select_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1,static_data_list);
        select_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //Array adapter to add the bluetooth device list to the ListView --> this is wierd: thsi must be here to work, but the actual Adapter that workd and upadte the
        // bluetooth list is not this one...... --> DO NOT DELETE
        bt_dev_list_adapt = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1,bt_dev_list);


        /**
         *  Below defines the onclick lister event (what happend when you click the "RECORD DATA"  Button on the app
         *
         * All this event does is each time the button is clicked it grabs the value from the Valtage and Pounds textview and adds both values to the correct list
         *
         * For the time being the data is hard coding with the "dig_temp" and the "pound_temp" for the digital voltage and the pounds respectively.
         *
         *
         * Once you finish the mathematical scale and we can get the data read from the bluetooth reciever correctly, this data will replace those values.
         *
         *      remember: to add data a list use the .add() method
         *                  to get the text from a textview: <name of textview>.getText().toString()
         *                    to set the text in order to update a textview: <name of textview>.setText(<put you text here)>)
         *
         */

        rec_data.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                listdata.add(pound.getText().toString() );
            }
        });

        // Below : you must add the adapter to the lsit in order the add data to the listview from the arraylist
        // Assign adapter to ListView
        rec_data_list.setAdapter(data_adapter);
       spinner.setAdapter(select_adapter);

        /**





                    Below are the listview on click listener (Defines what happens when you click a specific item within the list)

                    There is one for the RECORD DATA list and the SELECT list.

                    For the time being SELECT list is the only one that update the "Select" at the top based on user selection.

                    THe other list is simply to list the data from the values taken from "Voltage" and "Pounds" nothing much happens here as the list is there inorder the list the
                    values from the the variables at the top.



         ListView Item Click Listener -- > Impliments Record Data ListView
          */
        rec_data_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition = position;

                // ListView Clicked item value
                String itemValue = (String) rec_data_list.getItemAtPosition(position);

                // Show Alert
                Toast.makeText(getApplicationContext(),
                        "Position :" + itemPosition + "  ListItem : " + itemValue, Toast.LENGTH_LONG)
                        .show();

            }

        });


        /**
         *
         *      Bluetooth ListView on Click Listener
         *
         *       Bellow dediens the onlicklistner fo the bluetooth listview.
         *
         *       when an item in the bluetooth list view is selected: the onclick listnerer definced below is triggered.
         *
         *       When it is triggered is grab the cuurent object selected and opens a dialog the prompt the user for a password. This password
         *       will be the bluetooth pairing password.
         *
         *       In other words , rather than the user havingto go into the android settings, the can now pair a device from the app....
         */

     list_paired_dev.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                  @Override
                 public void onItemClick(AdapterView<?> parent, View view,
                   int position, long id) {

                     // ListView Clicked item index
                    int itemPosition = position;
                    // ListView Clicked item value
                    final String itemValue = (String) list_paired_dev.getItemAtPosition(position);
                      /***
                       *
                       *    Attempt to grab the mac address of currently selected value of the user....
                       */

                   // Toast.makeText(getApplicationContext(), itemValue, Toast.LENGTH_LONG).show();
                      /*String temp = itemValue;
                       String test_char=":";
                      String mac_builder="";


                      for(int i=0; i <itemValue.length();i++){
                          if(test_char.equals(itemValue.charAt(i))){
                              mac_builder+=itemValue.charAt(i-2);
                              mac_builder+=itemValue.charAt(i-1);
                              mac_builder+=":";
                          }
                      }*/
                     // Toast.makeText(getApplicationContext(), mac_builder, Toast.LENGTH_LONG).show();


                    // pairedDevices = myBluetoothAdapter.getBondedDevices();

                      // put it's one to the adapter
                     for (BluetoothDevice device : pairedDevices) {  // loop through all paired devices to find a match
                          String mac = device.getAddress(); // grab the mac of the current device in loop
                          if (mac.equals("30:14:06:20:13:60")) { // if the mac equals the mac of the device attempting to connect --> Output a masage --> this is only for testing purposes --> then create a socket for the device to connect on
                              //
                              //Toast.makeText(getApplicationContext(),device.getName() + " : " + device.getAddress(),Toast.LENGTH_LONG).show();
                              // create a socket for the corect device
                              //createSocket(device);
                          }

                          //add the found paired devices to an array
                          BTArrayAdapter.add(device.getName()+ "\n" + device.getAddress());
                      }

                   }
        });

         /*
                Another list view: For select...



         ListView Item Click Listener -- > Impliments Select VAlue ListView
          */
        //select_pound=(TextView)findViewById(R.id.textView_selectVal);
        select_val_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition = position;

                // ListView Clicked item value
                String itemValue = (String) select_val_list.getItemAtPosition(position);

                /**
                 *   the magic happens here: The currently selected list item is set the textview value a the top: (changes the word "Select" to the currently selectd item)
                 *
                 *
                 *
                 */

                String temp = itemValue;
               // select_pound.setText(temp);
            }

        });


        /***
         *
         *      THis next section covers the bluetooth stuff...
         *
         *      I need to work on searching for and adding bluetooth devices from the app, but for now simply got to setting on your phone to add the device that way.
         *
         *      Once added, you can view paired devices, and turn on and off the bluetooth functionality
         *
         *   below I will only go in and comment the important things you should know...
         *
         *
         */

        // take an instance of BluetoothAdapter - Bluetooth radio
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(myBluetoothAdapter == null) { // if device does not support bluetoot the display a not supported message

            list_paired_dev.setEnabled(false);
            findBtn.setEnabled(false);
            Toast.makeText(getApplicationContext(),"Your device does not support Bluetooth",
                    Toast.LENGTH_LONG).show();
           //bluetooth_status.setText("Status: not supported");


        } else {
            toggleButton_bluetooth = (ToggleButton)findViewById(R.id.toggleButton_bluetooth);  // sets up the blootooth toggle( the on / off button below status in the app
            //text = (TextView) findViewById(R.id.bluetooth_status); //not important but keep incase it was needed in the future

            // defines the onclick listener for the bluetooth toggle: what happend if the toggle is in the "ON" or "OFF" state

            toggleButton_bluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) { // if "ON" State --> change the status to enabled
                      // bluetooth_status.setText("Enabled");
                        // The toggle is enabled
                        if (!myBluetoothAdapter.isEnabled()) {  // if "ON" state and bluetooth is currently off, Request permission to enable, if accepted turn bluetooth on
                            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);

                           Toast.makeText(getApplicationContext(), "Bluetooth turned on", //once blue tooth has been turned on, inform the user with a subtly display
                                    Toast.LENGTH_LONG);

                            Toast.makeText(getApplicationContext(), "Searching for devices...", // inform the user that a search for bluetooth devices is in progress
                                    Toast.LENGTH_LONG).show();
                           ;



                        } else { // if blue tooth functionality is already state is switched to in" inform user that is is already on and inform use that the search for bluetooth devices is in progress
                            Toast.makeText(getApplicationContext(), "Bluetooth is already on",
                                    Toast.LENGTH_LONG).show();

                            Toast.makeText(getApplicationContext(), "Searching for devices...",
                                    Toast.LENGTH_LONG).show();
                            find(buttonView);
                        }

                    }
                    if(!isChecked){ // if the state is turned  to "OFF" chnage the status to Disabled and turn of bluetooth

                      //bluetooth_status.setText("Disabled");
                        myBluetoothAdapter.disable(); //disable the addapter

                    }

                }
                });


            /**
             *  findBtn --> "Search: in the app --> on click finds devices so the find method is called
             *  listBtn --> Simply List devices that are currently paired: this already works button is hidden in the app as you did not want it
             *
             */
           list_paired = (Button)findViewById(R.id.paired);
            list_paired.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                   try {
                       list(v);
                   }catch(Exception e){
                       e.printStackTrace();
                   }

                }
            });

            findBtn = (Button)findViewById(R.id.search);
            findBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) { // if bluetooth is not on and a search is intitiated inform user to turn on bluetooth before searching for device
                    // TODO Auto-generated method stub
                    if (!myBluetoothAdapter.isEnabled()) { // if the bluetooth is not on, prompt user to turn bluetooth on before search for devices
                        Toast.makeText(getApplicationContext(), "Please turn on BlueTooth before searching.", Toast.LENGTH_LONG).show();
                    } else //bluetooth is on so search for device --> with the find method
                        find(v);
                }
            });



            // create the arrayAdapter that contains the BTDevices, and set it to the ListView
            //THis is the Adapter that actually adds bluetooth devices to the the list
           BTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1,bt_dev_list);
            //the the listview to the adapter
            list_paired_dev.setAdapter(BTArrayAdapter);


        }

        /**
         *
         *          Sound Section.....
         *
         *
         *          This section is an oddly placed as I will go back to the blue tooth section after this one:
         *          However it must be here because the sound toggle is defined on the onCreate Android method if I take it out then it wont work.
         *
         *          The other bluetooth methods are below as they are mehtods with can be defined outside of the onCreate Method....
         *
         *
         *
         *
         *
         *          Makes a toggle: if state is "ON" plays a sound, if state is "OFF" turn off music
         *
         *
         *          This section needs work....
         *
         *          For now, just plays music and turns the toggle on and off
         *
         *
         *
         *
         */



        //defines the music toggle button
        toggleButton_sound = (ToggleButton)findViewById(R.id.toggleButton_sound);

        // sets the event listener for what happend if the toggle button is pressed for both "ON" and "OFF" states
        toggleButton_sound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //This is an issue --> the alarm type is very load but it last a while and cannot get it to stop
                // leaving as a notification for now
                //Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

                //creates a notification --> Grabs a sound to play
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                //creates a music player --> makes a music player to play the sound just grabbed
                MediaPlayer mp = MediaPlayer.create(getApplicationContext(), notification);

                // is state is "ON" start the music player and play the sound
                if (isChecked) {
                    // The toggle is enabled
                    mp.start();
                }
                // if the state is "OFF" stop the music player and thus the sound
                else mp.stop();
            }
        });

        /**
         *
         *
         *      The below section is where the graph will go
         *
         */


        GraphView graph = (GraphView)findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[]{

                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)


        });


        graph.addSeries(series);

    } // end onCreate method


    //Methods to mplement the Spinner
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        String item = parent.getItemAtPosition(position).toString();

        // Showing selected spinner item
        Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();

    }

    public void onNothingSelected(AdapterView<?> parent) {
        // TODO Auto-generated method stub

    }





    /***
     *
     *   Coming back to the bluetooth stuff...
     *
     *
     * Bluetooth section
     *
     * @param view
     */



       // on method--> Defines what happens when bluetooth is turned on
    public void on(View view){
        if (!myBluetoothAdapter.isEnabled()) { // if bluetooth is not already on/enabled --> request permission to turn it on
            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);// make an new intent that turn on the BluetoothAdapter and
                                                                                     // and requests permission to turn it on
            startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);

            Toast.makeText(getApplicationContext(),"Bluetooth turned on" ,// if permission is granted, inform user that bluetooth is on
                    Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(getApplicationContext(),"Bluetooth is already on", // ... self exaplanatory
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if(requestCode == REQUEST_ENABLE_BT){
            if(myBluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(),"Bluetooth Enabled", Toast.LENGTH_SHORT).show();
               // bluetooth_status.setText("Status: Enabled");
            } else {

                Toast.makeText(getApplicationContext(),"Bluetooth Enabled", Toast.LENGTH_SHORT).show();
               // bluetooth_status.setText("Status: Disabled");
            }
        }
    }


    // list device method: --> list current connect devices....
    public void list(View view) throws IOException{ // list paired devices
        // get paired devices
        pairedDevices = myBluetoothAdapter.getBondedDevices();

        // put it's one to the adapter
        for (BluetoothDevice device : pairedDevices) {  // loop through all paired devices to find a match
            String mac = device.getAddress(); // grab the mac of the current device in loop
            if (mac.equals("30:14:06:20:13:60")) { // if the mac equals the mac of the device attempting to connect --> Output a masage --> this is only for testing purposes --> then create a socket for the device to connect on
              //  Toast.makeText(getApplicationContext(),device.getName() + " : " + device.getAddress(),Toast.LENGTH_LONG).show();
                    // create a socket for the corect device
                /**
                 *  Below sets the visibility of the the list_paired_dev to invisible:
                 *
                 *  There are three setting:
                 *
                 *  View.GONE removes the list from the and takes the space it occupies in the app and can allocate it to other resources
                 *
                 *  View.INVISIBLE sets the list visible to none...
                 *
                 *  VIEW.INVISIBLE sets the list visibility back to visible
                 *
                 */

                list_paired_dev.setVisibility(View.INVISIBLE);

                    createSocket(device);
            }

            //add the found paired devices to an array
            BTArrayAdapter.add(device.getName()+ "\n" + device.getAddress());
        }
    }


    // this is the important section that defines the Bluetooth Device and receive section;
    // THsi is where : if a bluetooth device is found is can be paired ow added to a list of devices connected...(ListView Component)
    final BroadcastReceiver bReceiver = new BroadcastReceiver(){
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {  // if a bluetooth device is found do stuff
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);  // create new bloothooth object and get its info


                    // add the name and the MAC address of the object to the arrayAdapter
                    BTArrayAdapter.add(device.getName() + "\n" + device.getAddress());

                    BTArrayAdapter.notifyDataSetChanged();
                      // MAC_temp = device.getAddress();

                    MAC_temp = "0C:20:13:12:11:A1";
                    // my test device --> "0C:20:13:12:11:A1"
                    //TEst ALptop: MAC:    C0:14:3D:C0:D1:BE

                  //microcontroller: 30:14:06:20:13:60


                    //pairs the bluetooth device
                    if(device.getAddress().equals("30:14:06:20:13:60")) { // if mac address is a match --> pair device
                    try{
                        //pair device
                        pairDevice(device);
                       // int pair_state = device.getBondState();
                        String pair_state;//= Integer.toString(device.getBondState());

                    } catch (Exception e){
                        e.printStackTrace();
                    }

                }
                    //add the device and info to a list
                    bt_dev_list.add(device.getName() + "\n" + device.getAddress());

                }

                else if(BluetoothDevice.ACTION_FOUND.isEmpty())
                    //bt_dev_list.add("Bluetooth scan found no device. Please try again!");
                Toast.makeText(getApplicationContext(), "Bluetooth scan found no device. Please try again!", Toast.LENGTH_LONG).show();

        }
    };
//creat socket and for device
    private void createSocket(BluetoothDevice device)throws IOException, NoSuchElementException {
       final UUID dev_uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        //final UUID dev_uuid = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
        //Toast.makeText(getApplicationContext(), "Reached Creating Socket", Toast.LENGTH_LONG).show();

        InputStream iStream = null;
        OutputStream oStream = null;
        BluetoothDevice test_dev;

        try {

            //temp = device.createRfcommSocketToServiceRecord(dev_uuid);
            temp = device.createRfcommSocketToServiceRecord(dev_uuid);

        } catch (IOException e1) {
            // TODO Auto-generated catch block
            //Log.d(TAG,"socket not created");
            e1.printStackTrace();
            Log.e("", "Error Creating Socket");
        }

        socket = temp;
       String test= socket.toString();
       test_dev = socket.getRemoteDevice();

        // Toast.makeText(getApplicationContext(), "socket: " + test, Toast.LENGTH_LONG).show();
       // Toast.makeText(getApplicationContext(), "socket Dev : " + test_dev, Toast.LENGTH_LONG).show();
        connectSocket(socket, device);
    }

    public void connectSocket(BluetoothSocket socket, BluetoothDevice device)  throws IOException{

        myBluetoothAdapter.cancelDiscovery();


        boolean success;
        BluetoothSocket fallbackSocket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        try {
            socket.connect();


            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
            Boolean conection_test = socket.isConnected();
            //Toast.makeText(getApplicationContext(), "Socket connection status" + conection_test, Toast.LENGTH_LONG).show();
            //Toast.makeText(getApplicationContext(), "Input" + tmpIn, Toast.LENGTH_LONG).show();

            // mHandler.obtainMessage(SUCCESS_CONNECT);
            Toast.makeText(getApplicationContext(), "Socket Connected", Toast.LENGTH_LONG).show();

          thread = new ConnectedThread(socket);
            thread.run();


               /* byte[] buffer = new byte[128];  // buffer store for the stream
                int bytes; // bytes returned from read()
                tmpIn.read();

                bytes = tmpIn.read(buffer);
                byte[] readBuf = (byte[]) buffer;
                String strIncom = new String(readBuf, 0, bytes);
                Toast.makeText(getApplicationContext(), "Input: " + strIncom, Toast.LENGTH_LONG).show();*/




            /**
             *      This causes the app to crash: but now that socket is successfully connect Thus must work inorder to grab input from the bluetooth device
             *      over the socket....
             *
             */
            //ConnectedThread my_thread = new ConnectedThread(socket);
            //my_thread.run();


           // mHandler.obtainMessage(SUCCESS_CONNECT);


            //  Log.i("Tag", "connect-run");
        } catch (IOException e) {
            Log.e("", e.getMessage());
            e.printStackTrace();
            try {

                socket.close();
                Toast.makeText(getApplicationContext(), "Socket Closed", Toast.LENGTH_LONG).show();
               /* Method m = device.getClass().getMethod("createRfcommSocket",
                        new Class[] { int.class });
                BluetoothSocket mySocket = (BluetoothSocket) m.invoke(device, Integer.valueOf(1));
                mySocket.connect();*/


                /*tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
                Boolean conection_test = socket.isConnected();
                Toast.makeText(getApplicationContext(), "Socket connection status" + conection_test, Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(), "Input" + tmpIn, Toast.LENGTH_LONG).show();

                // mHandler.obtainMessage(SUCCESS_CONNECT);
                Toast.makeText(getApplicationContext(), "Socket Connected", Toast.LENGTH_LONG).show();
                mHandler.obtainMessage(SUCCESS_CONNECT);*/

                /*Class<?> clazz = socket.getRemoteDevice().getClass();
                Class<?>[] paramTypes = new Class<?>[]{Integer.TYPE};

                Method m = clazz.getMethod("createRfcommSocket", paramTypes);
                Object[] params = new Object[]{Integer.valueOf(1)};

                fallbackSocket = (BluetoothSocket) m.invoke(socket.getRemoteDevice(), params);
                fallbackSocket.connect();*/
            } catch (Exception e1) {
               // socket.close();

                Log.e("", e1.getMessage());
                e1.printStackTrace();
            }


        }
    }





    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
           // Handler mHandler;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        public void run() {
            BluetoothSocket mmSocket = socket;
            final InputStream mmInStream;
            final OutputStream mmOutStream;

            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            // Handler mHandler;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;



            byte[] buffer;  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs

                try {
                   // Looper.prepare();

                        // Read from the InputStream
                        buffer = new byte[8096]; //byte[1024];
                        bytes = mmInStream.read(buffer);


                  // pound.setText("Pound: " +Integer.toString(bytes));


                        // Send the obtained bytes to the UI activity
                        //Toast.makeText(getApplicationContext(),"Input: " + bytes, Toast.LENGTH_SHORT).show();
                       mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                               .sendToTarget();
                         thread.sleep(1000);
               // Looper.loop();
                } catch (Exception e) {
                        thread.run();
                    e.printStackTrace();

                }

        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }







    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
                    //notify the with pop htat a bluetooth device is trying to connect and ask for pairing code
                  device.notify();
            // once code inout successfully: pair the device
               device.createBond();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    // method to find the Bluetooth devices:
    public void find(View view) {
        if (myBluetoothAdapter.isDiscovering()) { // if discovery is already in progress cancel disovery as you cant initialize discovery of the device is already searching
            // the button is pressed when it discovers, so cancel the discovery
            myBluetoothAdapter.cancelDiscovery(); // cancel discovery
        }
        else {  // if there is a current search for devices...
            BTArrayAdapter.clear();/// clear the list of discovered devices
            myBluetoothAdapter.startDiscovery(); // start searching again


            // this is a call the register device method above: if a bluetooth device is found
            registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        }
    }

    // turn off bluetooth
    public void off(View view) {
        myBluetoothAdapter.disable();
        Toast.makeText(getApplicationContext(), "Bluetooth Disconnected", Toast.LENGTH_SHORT).show();
        //bluetooth_status.setText("Status: Disconnected");

    }

    @Override

    //the onDestroy method --> the app crashed or if the app close the registered reciver:-- this does not deregister devices, simply close the receiver to it.
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unregisterReceiver(bReceiver);
    }

}