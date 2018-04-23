package brett.lednavigation;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.Toast;

public class CheckConnectivity {
    Context callingActivity;
    CheckConnectivity(Context context)
    {
        callingActivity = context;
    }
    public boolean checkWifiOnAndConnected() {
        WifiManager wifiMgr = (WifiManager) callingActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON

            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();

            if (wifiInfo.getNetworkId() == -1) {
                Toast.makeText(callingActivity, "Wifi Is not Connected, Please connect to a network with a Hue Bridge", Toast.LENGTH_SHORT).show();
                return false; // Not connected to an access point
            }
            return true; // Connected to an access point
        } else {
            Toast.makeText(callingActivity, "WiFi Adapter is OFF, Please Enable", Toast.LENGTH_LONG).show();
            return false; // Wi-Fi adapter is OFF
        }
    }
}
