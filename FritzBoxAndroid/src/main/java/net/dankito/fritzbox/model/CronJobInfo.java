package net.dankito.fritzbox.model;

import android.app.PendingIntent;

/**
 * Created by ganymed on 26/11/16.
 */

public class CronJobInfo {

  protected Runnable runnableToExecute;

  protected PendingIntent pendingIntent;


  public CronJobInfo(Runnable runnableToExecute, PendingIntent pendingIntent) {
    this.runnableToExecute = runnableToExecute;
    this.pendingIntent = pendingIntent;
  }


  public Runnable getRunnableToExecute() {
    return runnableToExecute;
  }

  public PendingIntent getPendingIntent() {
    return pendingIntent;
  }

}
