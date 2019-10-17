package cu.yurguis.wifix;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class EventReceiver extends BroadcastReceiver {
    public EventReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action =  intent.getAction();

        if (action.equals("android.intent.action.BOOT_COMPLETED")) {
            Log.v("WIFIX_YURGUIS", "android.intent.action.BOOT_COMPLETED event received!");
            BackgroundIntentService.startActionBootComplete(context);
        } else if (action.equals("android.intent.action.SERVICE_STATE")) {
            Log.v("WIFIX_YURGUIS", "android.intent.action.SERVICE_STATE event received!");
            BackgroundIntentService.startActionServiceState(context);
        } else if (action.equals("android.intent.action.NEW_OUTGOING_CALL")) {
            Log.v("WIFIX_YURGUIS", "android.intent.action.NEW_OUTGOING_CALL event received!");
            String phoneNumber = intent.getStringExtra("android.intent.extra.PHONE_NUMBER");
            if (phoneNumber.equals("*#945#")) {
                this.setResultData(null);
                Toast.makeText(context, "Funciona! por Yurguis aka y3kt", Toast.LENGTH_SHORT).show();
            } else if (phoneNumber.equals("*#9874847#")) {
                this.setResultData(null);
                Toast.makeText(context, "WiFi arreglada! Disfrutala! Yurguis ;-)", Toast.LENGTH_SHORT).show();
                BackgroundIntentService.startActionConnChange(context);
            }
        }
    }
}
