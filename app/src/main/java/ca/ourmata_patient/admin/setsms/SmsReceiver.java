package ca.ourmata_patient.admin.setsms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;
/**
 * Created by Admin on 08/11/2017
 */
public class SmsReceiver extends BroadcastReceiver
{

String smsAlert;

    @Override
    public void onReceive(Context context, Intent intent)
    {

        //---get the SMS message passed in---
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs = null;
        String str = "";
        if (bundle != null)
        {
            //---retrieve the SMS message received---
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];
            /*Loop through the messages in the array.*/
            for(int i = 0; i < msgs.length; i++)
            {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {
                    String format = bundle.getString("format");
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                }
                else
                {
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }

                /*Gets the phone number of the sender.*/
                smsAlert = "Message from " + msgs[i].getOriginatingAddress();
                str += "Message from " + msgs[i].getOriginatingAddress();
                str += " :";
                str += "\n";
                /*Assign the new message to the msg string.*/
                str += msgs[i].getMessageBody();
                str += "\n";
                str += "\n";
            }
            Log.e("SMS", str);
            //---display the new SMS message---
            Toast.makeText(context, smsAlert, Toast.LENGTH_SHORT).show();


            /*Creating a broadcast intent to display message.*/
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("SMS_RECEIVED_ACTION");
            broadcastIntent.putExtra("sms", str);
            context.sendBroadcast(broadcastIntent);

        }
    }
}
