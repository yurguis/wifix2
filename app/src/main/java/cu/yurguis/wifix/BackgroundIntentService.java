package cu.yurguis.wifix;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class BackgroundIntentService extends IntentService {
    private static final String ACTION_BOOT_COMPLETE = "cu.yurguis.wifix.action.boot_complete";
    private static final String ACTION_CONNECTIVITY_CHANGE = "cu.yurguis.wifix.action.connectivity_change";
    private static final String ACTION_SERVICE_STATE = "cu.yurguis.wifix.action.service_state";

    // Whether there is a Wi-Fi connection.
    private static boolean wifiConnected = false;
    // Whether there is a mobile connection.
    private static boolean mobileConnected = false;

    public static void startActionBootComplete(Context context) {
        Intent intent = new Intent(context, BackgroundIntentService.class);
        intent.setAction(ACTION_BOOT_COMPLETE);
        context.startService(intent);
    }

    public static void startActionConnChange(Context context) {
        Intent intent = new Intent(context, BackgroundIntentService.class);
        intent.setAction(ACTION_CONNECTIVITY_CHANGE);
        context.startService(intent);
    }

    public static void startActionServiceState(Context context) {
        Intent intent = new Intent(context, BackgroundIntentService.class);
        intent.setAction(ACTION_SERVICE_STATE);
        context.startService(intent);
    }

    public BackgroundIntentService() {
        super("BackgroundIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_BOOT_COMPLETE.equals(action)) {
                handleDefaultAction();
            } else if (ACTION_CONNECTIVITY_CHANGE.equals(action)) {
                handleDefaultAction();
            } else if (ACTION_SERVICE_STATE.equals(action)) {
                handleDefaultAction();
            }
        }
    }

    private void handleDefaultAction() {
        setCountry();
    }

    private void setCountry() {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String countryCode = tm.getNetworkCountryIso();

        if (countryCode.equalsIgnoreCase("CU")) {
            boolean result = execute("sqlite3 /data/data/com.android.providers.settings/databases/settings.db \"UPDATE global SET value='us' WHERE name='wifi_country_code'\";");
            if (result) {
                Log.v("WIFIX_YURGUIS", "Superuser acces granted! Values have been changed");
            } else {
                Log.v("WIFIX_YURGUIS", "Superuser acces denied! Check it!");
            }
//            ContentValues values = new ContentValues();
//            values.put("value", "us");
//            db.update("global", values, String.format("%s = ?", "name"),
//                    new String[]{"wifi_country_code"});
//            Shell.SU.run("sqlite3 /data/data/com.android.providers.settings/databases/settings.db \"UPDATE global SET value='us' WHERE name='wifi_country_code'\";");
//            refreshWiFi();
        }
    }

    private void refreshWiFi() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        if (!mWifi.isConnectedOrConnecting()) {
            if (wifiMgr.isWifiEnabled()) {
                wifiMgr.setWifiEnabled(false);
                wifiMgr.setWifiEnabled(true);
            } else {
                wifiMgr.setWifiEnabled(true);
                wifiMgr.setWifiEnabled(false);
            }
        }
    }

    public static Boolean isCountryCU() {
        List<String> countryCode;

        countryCode = Shell.SU.run("sqlite3 /data/data/com.android.providers.settings/databases/settings.db \"SELECT (value) FROM global WHERE name='wifi_country_code'\";");
        if (countryCode != null && countryCode.toString().equalsIgnoreCase("[cu]")) {
            Log.v("WIFIX_YURGUIS", "Country is: " + countryCode.toString() + ". Change needed!");
            return true;
        } else {
            Log.v("WIFIX_YURGUIS", "Country is: " + countryCode.toString() + ". OK!");
            return false;
        }
    }

    public final boolean execute(String command)
    {
        boolean retval = false;
        try {
            if (null != command && command.length() > 0) {
                Process suProcess = Runtime.getRuntime().exec("su");

                DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());

                // Execute commands that require root access
                os.writeBytes(command + "\n");
                os.flush();
                os.writeBytes("exit\n");
                os.flush();

                try {
                    int suProcessRetval = suProcess.waitFor();
                    if (255 != suProcessRetval) {
                        // Root access granted
                        retval = true;
                    }
                    else {
                        // Root access denied
                        retval = false;
                    }
                } catch (Exception ex) {
                    Log.e("WIFIX_YURGUIS", "Error executing root action", ex);
                }
            }
        } catch (IOException ex) {
            Log.w("WIFIX_YURGUIS", "Can't get root access", ex);
        } catch (SecurityException ex) {
            Log.w("WIFIX_YURGUIS", "Can't get root access", ex);
        } catch (Exception ex) {
            Log.w("WIFIX_YURGUIS", "Error executing internal operation", ex);
        }
        return retval;
    }
}
