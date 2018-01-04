package ca.ourmata_patient.admin.setsms;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
//import android.support.v4.app.ActivityCompat;
//import android.support.v7.app.AppCompatActivity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Modified  by Admin on 08/11/2017.
 */

public class MainActivity extends Activity
{

    /*Setting up the widget objects.*/
    Button msgSend;
    EditText msgNum;
    EditText txtMsg;
    ImageButton smsContact;
    ImageButton smsAddContact;
    ImageButton smsGPS;
    GPSTracker gps;
    TextView inView;
    String smsTimeLine;
    String SetTimeLine ="";
    // Create Preference to check if application is going to be called first
    // time.
    Context mContext=MainActivity.this;
    public SharedPreferences appPreferences;
    boolean isAppInstalled = false;

    /*instantiating the intent filter*/
    IntentFilter intentFilter;

    private BroadcastReceiver intentReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            // display incoming sms messages.

            inView = (TextView) findViewById(R.id.msgDisplay);
            smsTimeLine = (intent.getExtras().getString("sms"));
            SetTimeLine += smsTimeLine;
            inView.setText(SetTimeLine);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get preference value to know that is it first time application is
        // being called.
        setContentView(R.layout.activity_main);appPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        isAppInstalled = appPreferences.getBoolean("isAppInstalled",false);
        if(!isAppInstalled){

            //  create short code

            Intent shortcutIntent = new Intent(getApplicationContext(),MainActivity.class);
            shortcutIntent.setAction(Intent.ACTION_MAIN);
            Intent intent = new Intent();
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "MATA sms");
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource
                    .fromContext(getApplicationContext(), R.drawable.sms_icon01));
            intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            getApplicationContext().sendBroadcast(intent);

            //Make preference true

            SharedPreferences.Editor editor = appPreferences.edit();
            editor.putBoolean("isAppInstalled", true);
            editor.commit();
        }

            msgSend = (Button) findViewById(R.id.btnSend);
            msgNum = (EditText) findViewById(R.id.smsNum);
            txtMsg = (EditText) findViewById(R.id.smsMsg);
            smsGPS = (ImageButton) findViewById(R.id.btnGPS);


            //Action for clicking on the search button (opens the search screen)
            smsContact = (ImageButton) findViewById(R.id.btnContact);
            smsContact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getApplicationContext(), ContactActivity.class));
                }
            });


            //The intent filter for the received sms messages
            intentFilter = new IntentFilter();
            intentFilter.addAction("SMS_RECEIVED_ACTION");


            msgSend.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    String phoneNumber = msgNum.getText().toString();
                    String textMessage = txtMsg.getText().toString();
                    if (phoneNumber.length() > 0 && textMessage.length() > 0)
                    {
                        sendSMS(phoneNumber, textMessage);
                    } else
                        {
                        Toast.makeText(getBaseContext(),
                                "Please enter both phone number and message.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });


            smsGPS.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    gps = new GPSTracker(MainActivity.this);

                    if (gps.canGetLocation())
                    {
                        double latitude = gps.getLatitude();
                        double longitude = gps.getLongitude();
                        String MyLocation = "Sender Location is -\nhttps://www.google.ca/maps/@" + latitude + "," + longitude + ",15z";
                        txtMsg.setText(MyLocation);


                    } else {
                        gps.showSettingsAlert();
                    }
                }
            });

        }


    private void sendSMS(String phoneNumber, final String textMessage)
    {
        String SENT = "SMS SENT";
        String DELIVERED = "SMS DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED), 0);

        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS sent to Physician",
                                Toast.LENGTH_SHORT).show();

                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));
        try
        {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, textMessage, sentPI, deliveredPI);
            txtMsg.setText("");
        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(), "SMS failed, please try again.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }


    }
    @Override
    protected void onResume()
    {
        //registering the receiver
        registerReceiver(intentReceiver, intentFilter);
        super.onResume();
    }
    @Override
    protected void onPause()
    {
        //Unregister the receiver
        unregisterReceiver(intentReceiver);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }



}




