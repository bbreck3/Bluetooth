package bluetooth.example.com.bluetooth;

import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;

import java.util.ArrayList;
import java.util.Set;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.widget.CompoundButton;
import android.app.AlertDialog;


public class MainActivity extends ActionBarActivity {
    private static final int REQUEST_ENABLE_BT = 1;
    private Button onBtn;
    private Button offBtn;
    private Button listBtn;
    private Button findBtn;
    private TextView text;
    private BluetoothAdapter myBluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private ListView myListView;
    private ArrayAdapter<String> BTArrayAdapter;
    private Button rec_data; // record data
    private ListView rec_data_list; // list view to add the recorded data to
    private ListView select_val_list;
    private TextView dig_volt; // textview to hold the current current digital voltage
    private TextView pound; // textview to hold the current pounds of weight
    private ToggleButton toggleButton_sound, toggleButton_bluetooth;
    private TextView bluetooth_status;
    private TextView select_pound;
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
                Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
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
         final ArrayList<String> listdata = new ArrayList<String>();
         final ArrayList<String> static_data_list = new ArrayList<String>();

        /*

        Testing the the Button capture of the data record

        */


            // basis textview and buttons setup
        dig_volt = (TextView)findViewById(R.id.textViewVolt); // this is the text view that reads "Voltge" on the app
        pound = (TextView)findViewById(R.id.textView_Pounds); // this is the textView that reads "Pounds" in the app
        rec_data = (Button)findViewById(R.id.btn_records); // this is the button the reads "RECORD DATA"
        rec_data_list = (ListView)findViewById(R.id.listView_Data); // this is the list view that will contain data from "listdata" above
        select_val_list = (ListView)findViewById(R.id.listView_selectData); // this is the listview that will contain data from "static_dat_list" above
        // toggleButton = (ToggleButton)findViewById(R.id.toggleButton);

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
                String dig_temp = ": 10";
                String pound_temp = ": 1";
                dig_volt.append(dig_temp);
                pound.append(pound_temp);
                listdata.add(dig_volt.getText().toString() + " : " + pound.getText().toString() );
                static_data_list.add(pound.getText().toString());
                //  rec_data_list.addView(listdata);
            }
        });

        // Below : you must add the adapter to the lsit in order the add data to the listview from the arraylist
        // Assign adapter to ListView
        rec_data_list.setAdapter(data_adapter);
        select_val_list.setAdapter(select_adapter);

        /*




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



         /*
                Another list view: For select...



         ListView Item Click Listener -- > Impliments Select VAlue ListView
          */
        select_pound=(TextView)findViewById(R.id.textView_selectVal);
        select_val_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition = position;

                // ListView Clicked item value
                String itemValue = (String) select_val_list.getItemAtPosition(position);

                // Show Alert
               /* Toast.makeText(getApplicationContext(),
                        "Position :" + itemPosition + "  ListItem : " + itemValue, Toast.LENGTH_LONG)
                        .show();*/

                /**
                 *   the magic happens here: The currently selected list item is set the textview value a the top: (changes the word "Select" to the currently selectd item)
                 *
                 *
                 *
                 */

                String temp = itemValue;
                select_pound.setText(temp);
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
        if(myBluetoothAdapter == null) {

            listBtn.setEnabled(false);
            findBtn.setEnabled(false);
            text.setText("Status: not supported");

            Toast.makeText(getApplicationContext(),"Your device does not support Bluetooth",
                    Toast.LENGTH_LONG).show();
        } else {
            toggleButton_bluetooth = (ToggleButton)findViewById(R.id.toggleButton_bluetooth);  // sets up the blootooth toggle( the on / off button below status in the app
            text = (TextView) findViewById(R.id.bluetooth_status); //not important but keep incase it was needed in the future
            bluetooth_status = (TextView)findViewById(R.id.bluetooth_Status);// defines the status text view: "ENABLED" if bluetooth is on, "DISABLED" id it is off

            // defines the onclick listener for the bluetooth toggle: what happend if the toggle is in the "ON" or "OFF" state

            toggleButton_bluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) { // if "ON" State --> change the status to enabled
                        bluetooth_status.setText("Enabled");
                        // The toggle is enabled
                        if (!myBluetoothAdapter.isEnabled()) {  // if "ON" state and bluetooth is currently off, Request permission to enable, if accepted turn bluetooth on
                            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);

                            Toast.makeText(getApplicationContext(), "Bluetooth turned on", //once blue tooth has been turned on, inform the user with a subtly display
                                    Toast.LENGTH_LONG).show();
                           ;

                        } else { // if blue tooth funcationality is already state is switched to in" inform user that is is already on
                            Toast.makeText(getApplicationContext(), "Bluetooth is already on",
                                    Toast.LENGTH_LONG).show();
                        }

                    }
                    if(!isChecked){ // if the state is turned  to "OFF" chnage the status to Disabled and turn of bluetooth

                        bluetooth_status.setText("Disabled");
                        myBluetoothAdapter.disable();

                    }

                }
                });


            /**
             *  Funtions that are implemented but need tuning......
             *
             */
            listBtn = (Button)findViewById(R.id.paired);
            listBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    list(v);
                }
            });

            findBtn = (Button)findViewById(R.id.search);
            findBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    find(v);
                }
            });

            //lsit view that displays the bluetooth device information
            myListView = (ListView)findViewById(R.id.listView1);

            // create the arrayAdapter that contains the BTDevices, and set it to the ListView
            BTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
            myListView.setAdapter(BTArrayAdapter);


        }

        /**
         *
         *          Sound Section.....
         *
         *          Makes a toggle: if state is "ON" plays a sound, if state is "OFF" turn off music
         *
         *
         *          This section needs work....
         *
         *          For now, just plays music and turns the toggle on and off
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

                /***
                 *
                 *
                 * Below is in experimental stages.....
                 */


                   /*
                        my attempt at getting the Alarm ringtone to start and stop
                            mp.start();
                    */
                    /*for (int i = 0; i<=1000000000; i++){
                        if(i==10000) { mp.stop();}
                    }

                } else if(!toggleButton_sound.isChecked()){
                    mp.stop();
                }*/
                //else {  } //do nothing no sound needed


            }
        });











    }


    /***
     *              Other various components for Android LifeCycle and other stuff......
     *
     *
     *
     *
     * @param view
     */















    public void on(View view){
        if (!myBluetoothAdapter.isEnabled()) {
            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);

            Toast.makeText(getApplicationContext(),"Bluetooth turned on" ,
                    Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(getApplicationContext(),"Bluetooth is already on",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if(requestCode == REQUEST_ENABLE_BT){
            if(myBluetoothAdapter.isEnabled()) {
                text.setText("Status: Enabled");
            } else {
                text.setText("Status: Disabled");
            }
        }
    }

    public void list(View view){
        // get paired devices
        pairedDevices = myBluetoothAdapter.getBondedDevices();

        // put it's one to the adapter
        for(BluetoothDevice device : pairedDevices)
            BTArrayAdapter.add(device.getName()+ "\n" + device.getAddress());

        Toast.makeText(getApplicationContext(),"Show Paired Devices",
                Toast.LENGTH_SHORT).show();

    }

    final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name and the MAC address of the object to the arrayAdapter
                BTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                BTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    public void find(View view) {
        if (myBluetoothAdapter.isDiscovering()) {
            // the button is pressed when it discovers, so cancel the discovery
            myBluetoothAdapter.cancelDiscovery();
        }
        else {
            BTArrayAdapter.clear();
            myBluetoothAdapter.startDiscovery();

            registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        }
    }

    public void off(View view){
        myBluetoothAdapter.disable();
        text.setText("Status: Disconnected");

        Toast.makeText(getApplicationContext(),"Bluetooth turned off",
                Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unregisterReceiver(bReceiver);
    }

}