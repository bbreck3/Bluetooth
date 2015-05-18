package bluetooth.example.com.bluetooth;

import android.content.DialogInterface;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.widget.CompoundButton;
import android.app.AlertDialog;
import android.widget.AdapterView.OnItemClickListener;


public class MainActivity extends ActionBarActivity {
    private static final int REQUEST_ENABLE_BT = 1;
    private Button onBtn;
    private Button offBtn;
    private Button listBtn;
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
    private ListView  myListView1;
    //my attempt to use a time delay --> while searching for devices : if none found to make a output that says no devices found please try again.... --> doesnt work correctly
    final int secondsDelayed = 1;
    Context main_activity;

    // The follow 5  lines are for the blueooth pairing capability in app (programmatically)
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    public BluetoothDevice btDevice;
    private String password;

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

        //list of the data that simply grabs the pounds
         final ArrayList<String> listdata = new ArrayList<String>();

        //list of the pounds only --> to be used for selection if selected a specific pound measurement as apposed to taing in from the input at the top
        // --> also to be integrated with the toggle on / off sound
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
        myListView1= (ListView)findViewById(R.id.listView1); //lsit view that displays the bluetooth device that were found during the search
        bluetooth_status = (TextView)findViewById(R.id.bluetooth_Status);// defines the status text view: "ENABLED" if bluetooth is on, "DISABLED" id it is off

        //contect of the main activity
        main_activity = this;

        // button to get to the graph activity (Screen)
        graph = (Button)findViewById(R.id.to_graph);
        graph.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                    setContentView(R.layout.activity_graph);
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

                String pound_temp = ": 1";
               // dig_volt.append(dig_temp);
                pound.append(pound_temp);
                listdata.add(pound.getText().toString() );
                static_data_list.add(pound.getText().toString());

            }
        });

        // Below : you must add the adapter to the lsit in order the add data to the listview from the arraylist
        // Assign adapter to ListView
        rec_data_list.setAdapter(data_adapter);
        select_val_list.setAdapter(select_adapter);

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

       myListView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition = position;
               // ListView Clicked item value
                final String itemValue = (String)myListView1.getItemAtPosition(position);

                /**
                 *
                 *      Pair Bluetoth Device Programmatically:
                 *      Ignore this section as I found another easier way to do it:
                 *      :
                 *
                 *
                 */

                /**
                 *
                 *
                 *      Below is the Alert Dialog that Allow the user to input the bluetooth Device Pin from with the app.
                 *
                 */

                // Define the EditText result variable to hold the result of the user input

                // EditText result = (EditText)findViewById(R.id.editTextDialogUserInput);
                // get prompts.xml view

             /*  LayoutInflater li = LayoutInflater.from(main_activity);
                View promptsView = li.inflate(R.layout.prompts, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        main_activity);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);

                final TextView dev_name_mess = (TextView)findViewById(R.id.pair_mess);
                final TextView dev_name_prompt = (TextView)findViewById(R.id.pair_mess_dev_name);
                final TextView dev_pin_mess = (TextView)findViewById(R.id.pair_mess_dev_pin);

                final EditText userInput = (EditText) promptsView
                        .findViewById(R.id.editTextDialogUserInput);

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {








                                       // dev_name_prompt.setText(itemValue);

                                        // get user input and set it to result
                                        // edit text

                                        //This test worked successfully
                                        //Now that can grab user inout from the app -- >we can use this to askk for the bluetooth password return the password
                                        // programmatically ans the use the store password to pair a new bluetooth device with in the app....
                                        // password is the value the user input --> this variable is now the bluetooth pairing password.....
                                        password = userInput.getText().toString();


                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.cancel();
                                    }
                                });

                // create alert dialog
                AlertDialog bt_pair = alertDialogBuilder.create();

                // show it
                bt_pair.show();*/



              /*  Toast.makeText(getApplicationContext(),
                        "Position :" + itemPosition + "  ListItem : " + itemValue, Toast.LENGTH_LONG)
                        .show();*/

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
        if(myBluetoothAdapter == null) { // if device does not support bluetoot the display a not supported message

            listBtn.setEnabled(false);
            findBtn.setEnabled(false);
           bluetooth_status.setText("Status: not supported");

            Toast.makeText(getApplicationContext(),"Your device does not support Bluetooth",
                    Toast.LENGTH_LONG).show();
        } else {
            toggleButton_bluetooth = (ToggleButton)findViewById(R.id.toggleButton_bluetooth);  // sets up the blootooth toggle( the on / off button below status in the app
            //text = (TextView) findViewById(R.id.bluetooth_status); //not important but keep incase it was needed in the future


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

                            Toast.makeText(getApplicationContext(), "Searching for devices...", // inform the user that a search for bluetooth devices is in progress
                                    Toast.LENGTH_LONG).show();
                           ;


                        } else { // if blue tooth functionality is already state is switched to in" inform user that is is already on and inform use that the search for bluetooth devices is in progress
                            Toast.makeText(getApplicationContext(), "Bluetooth is already on",
                                    Toast.LENGTH_LONG).show();

                            Toast.makeText(getApplicationContext(), "Searching for devices...",
                                    Toast.LENGTH_LONG).show();
                        }

                    }
                    if(!isChecked){ // if the state is turned  to "OFF" chnage the status to Disabled and turn of bluetooth

                       bluetooth_status.setText("Disabled");
                        myBluetoothAdapter.disable(); //disable the addapter

                    }

                }
                });


            /**
             *  findBtn --> "Search: in the app --> on click finds devices so the find method is called
             *  listBtn --> Simply List devices that are currently paired: this already works button is hidden in the app as you did not want it
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
                public void onClick(View v) { // if bluetooth is not on and a search is intitiated inform user to turn on bluetooth before searching for device
                    // TODO Auto-generated method stub
                    if(!myBluetoothAdapter.isEnabled()){ // if the bluetooth is not on, prompt user to turn bluetooth on before search for devices
                        Toast.makeText(getApplicationContext(),"Please turn on BlueTooth before searching.",Toast.LENGTH_LONG).show();
                    }
                    else //bluetooth is on so search for device --> with the find method
                    find(v);
                }
            });



            // create the arrayAdapter that contains the BTDevices, and set it to the ListView
            //THis is the Adapter that actually adds bluetooth devices to the the list
           BTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1,bt_dev_list);
            //the the listview to the adapter
            myListView1.setAdapter(BTArrayAdapter);


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
                bluetooth_status.setText("Status: Enabled");
            } else {
                bluetooth_status.setText("Status: Disabled");
            }
        }
    }


    // list device method: --> list current connect devices....
    public void list(View view){
        // get paired devices
        pairedDevices = myBluetoothAdapter.getBondedDevices();

        // put it's one to the adapter
        for(BluetoothDevice device : pairedDevices)
            BTArrayAdapter.add(device.getName()+ "\n" + device.getAddress());

        Toast.makeText(getApplicationContext(),"Show Paired Devices",
                Toast.LENGTH_SHORT).show();

    }


    // this is the important section that defines the Bluetooth Device and receive section;
    // THsi is where : if a bluetooth device is found is can be paired ow added to a list of devices connected...(ListView Component)
    final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {  // if a bluetooth device is found do stuff
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);  // create new bloothooth object and get its info


                    // add the name and the MAC address of the object to the arrayAdapter
                    BTArrayAdapter.add(device.getName() + "\n" + device.getAddress());

                    BTArrayAdapter.notifyDataSetChanged();

                    //pairs the bluetooth device
                    pairDevice(device);
                    //add the device and info to a list
                    bt_dev_list.add(device.getName() + "\n" + device.getAddress());

                }
                // This doesnt work correctly for some reason--> if a blue tooth devices is found the above works, but if a bluetooth device is not found, then it does nothing
                // it is very strange --> needs debugging...
                else if(BluetoothDevice.ACTION_FOUND.isEmpty())
                    //bt_dev_list.add("Bluetooth scan found no device. Please try again!");
                Toast.makeText(getApplicationContext(), "Bluetooth scan found no device. Please try again!", Toast.LENGTH_LONG).show();

        }
    };



    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);


            /**
             *
             *
             *
             *      If nore this section: THis is me tinkering with something
             */
                //device.notify();
               //device.createBond();

               // int bond_state = device.getBondState();


            /**
             *          Does Not Work
             *
             *
             */
           /*switch (device.getBondState()){
                case 12:  Toast.makeText(getApplicationContext(),"Device Paired!",Toast.LENGTH_LONG).show();
                        break;
                case 11:  Toast.makeText(getApplicationContext(),"Pairing Paired!",Toast.LENGTH_LONG).show();
                    break;
                case 10: Toast.makeText(getApplicationContext(),"Device Paired!",Toast.LENGTH_LONG).show();
                        break;

            }*/
           /* if(device.getBondState()==device.BOND_BONDED){
                Toast.makeText(getApplicationContext(),"Device Paired!",Toast.LENGTH_LONG).show();
            }
            else if(device.getBondState()==device.BOND_BONDING){
                Toast.makeText(getApplicationContext(),"Pairing...",Toast.LENGTH_LONG).show();
            }
            else if(device.getBondState()==device.BOND_NONE){
                Toast.makeText(getApplicationContext(),"Error Please try Again!",Toast.LENGTH_LONG).show();
            }*/
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
    public void off(View view){
        myBluetoothAdapter.disable();
        bluetooth_status.setText("Status: Disconnected");

        Toast.makeText(getApplicationContext(),"Bluetooth turned off",
                Toast.LENGTH_LONG).show();
    }

    @Override

    //the onDestroy method --> the app crashed or if the app close the registered reciver:-- this does not deregister devices, simply close the receiver to it.
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unregisterReceiver(bReceiver);
    }

}