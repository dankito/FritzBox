package net.dankito.fritzbox.model;

/**
 * Created by ganymed on 26/11/16.
 */

public class NotificationInfo {

  protected int notificationId;

  protected String tag;


  public NotificationInfo(int notificationId) {
    this.notificationId = notificationId;
  }

  public NotificationInfo(int notificationId, String tag) {
    this(notificationId);
    this.tag = tag;
  }


  public int getNotificationId() {
    return notificationId;
  }

  public boolean isTagSet() {
    return getTag() != null;
  }

  public String getTag() {
    return tag;
  }


  @Override
  public String toString() {
    if(isTagSet()) {
      return "" + getNotificationId() + " (" + getTag() + ")";
    }
    else {
      return "" + getNotificationId();
    }
  }

}
