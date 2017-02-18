package net.dankito.fritzbox.services;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import static android.content.Context.WIFI_SERVICE;


public class AndroidNetworkService implements INetworkService {

  protected Context context;


  public AndroidNetworkService(Context context) {
    this.context = context;
  }


  @Override
  public String getCurrentSsid() {
    WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
    if(wifiManager != null && wifiManager.getConnectionInfo() != null) {
      WifiInfo wifiInfo = wifiManager.getConnectionInfo();

      return wifiInfo.getSSID().replace("\"", "");
    }

    return null;
  }

  @Override
  public boolean isInHomeNetwork(String homeNetworkSsid) {
    String currentSsid = getCurrentSsid();
    return currentSsid != null && currentSsid.equals(homeNetworkSsid);
  }

}
