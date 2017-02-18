package net.dankito.fritzbox.model;

import net.dankito.fritzbox.utils.StringUtils;

/**
 * Created by ganymed on 26/11/16.
 */

public class UserSettings {

  protected String fritzBoxAddress;

  protected String fritzBoxPassword;

  protected boolean checkOnlyInHomeNetwork;

  protected String homeNetworkSsid;

  protected boolean isPeriodicalMissedCallsCheckEnabled;

  protected long periodicalMissedCallsCheckInterval;


  public UserSettings() { // for Jackson

  }

  public UserSettings(String fritzBoxAddress, String fritzBoxPassword) {
    this.fritzBoxAddress = fritzBoxAddress;
    this.fritzBoxPassword = fritzBoxPassword;

    this.checkOnlyInHomeNetwork = true;
  }


  public boolean isFritzBoxAddressSet() {
    return StringUtils.isNotNullOrEmpty(getFritzBoxAddress());
  }

  public String getFritzBoxAddress() {
    return fritzBoxAddress;
  }

  public void setFritzBoxAddress(String fritzBoxAddress) {
    this.fritzBoxAddress = fritzBoxAddress;
  }

  public boolean isFritzBoxPasswordSet() {
    return StringUtils.isNotNullOrEmpty(getFritzBoxPassword());
  }

  public String getFritzBoxPassword() {
    return fritzBoxPassword;
  }

  public void setFritzBoxPassword(String fritzBoxPassword) {
    this.fritzBoxPassword = fritzBoxPassword;
  }

  public boolean isCheckOnlyInHomeNetwork() {
    return checkOnlyInHomeNetwork;
  }

  public void setCheckOnlyInHomeNetwork(boolean checkOnlyInHomeNetwork) {
    this.checkOnlyInHomeNetwork = checkOnlyInHomeNetwork;
  }

  public String getHomeNetworkSsid() {
    return homeNetworkSsid;
  }

  public void setHomeNetworkSsid(String homeNetworkSsid) {
    this.homeNetworkSsid = homeNetworkSsid;
  }

  public boolean isPeriodicalMissedCallsCheckEnabled() {
    return isPeriodicalMissedCallsCheckEnabled;
  }

  public void setPeriodicalMissedCallsCheckEnabled(boolean periodicalMissedCallsCheckEnabled) {
    isPeriodicalMissedCallsCheckEnabled = periodicalMissedCallsCheckEnabled;
  }

  public long getPeriodicalMissedCallsCheckInterval() {
    return periodicalMissedCallsCheckInterval;
  }

  public void setPeriodicalMissedCallsCheckInterval(long periodicalMissedCallsCheckInterval) {
    this.periodicalMissedCallsCheckInterval = periodicalMissedCallsCheckInterval;
  }


  @Override
  public String toString() {
    return getFritzBoxAddress();
  }

}
