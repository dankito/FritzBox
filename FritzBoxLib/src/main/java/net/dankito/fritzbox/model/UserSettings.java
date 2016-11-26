package net.dankito.fritzbox.model;

/**
 * Created by ganymed on 26/11/16.
 */

public class UserSettings {

  protected String fritzboxAddress;

  protected String fritzboxPassword;


  public UserSettings(String fritzboxAddress, String fritzboxPassword) {
    this.fritzboxAddress = fritzboxAddress;
    this.fritzboxPassword = fritzboxPassword;
  }


  public String getFritzboxAddress() {
    return fritzboxAddress;
  }

  public void setFritzboxAddress(String fritzboxAddress) {
    this.fritzboxAddress = fritzboxAddress;
  }

  public String getFritzboxPassword() {
    return fritzboxPassword;
  }

  public void setFritzboxPassword(String fritzboxPassword) {
    this.fritzboxPassword = fritzboxPassword;
  }


  @Override
  public String toString() {
    return getFritzboxAddress();
  }

}
