package net.dankito.fritzbox.model;

import net.dankito.fritzbox.utils.StringUtils;

/**
 * Created by ganymed on 26/11/16.
 */

public class UserSettings {

  protected String fritzBoxAddress;

  protected String fritzBoxPassword;

  protected boolean enablePeriodicalMissedCallsCheck;

  protected long periodicalMissedCallsCheckInterval;


  public UserSettings() { // for Jackson

  }

  public UserSettings(String fritzBoxAddress, String fritzBoxPassword) {
    this.fritzBoxAddress = fritzBoxAddress;
    this.fritzBoxPassword = fritzBoxPassword;
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

  public boolean isPeriodicalMissedCallsCheckEnabled() {
    return enablePeriodicalMissedCallsCheck;
  }

  public void setEnablePeriodicalMissedCallsCheck(boolean enablePeriodicalMissedCallsCheck) {
    this.enablePeriodicalMissedCallsCheck = enablePeriodicalMissedCallsCheck;
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
