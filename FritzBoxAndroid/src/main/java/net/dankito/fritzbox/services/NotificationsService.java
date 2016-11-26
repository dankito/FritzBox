package net.dankito.fritzbox.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;

import net.dankito.fritzbox.MainActivity;
import net.dankito.fritzbox.model.NotificationInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ganymed on 26/11/16.
 */

public class NotificationsService {

  protected static int nextNotificationId = 1;

  protected static Map<String, NotificationInfo> currentlyShowingNotifications = new ConcurrentHashMap<>();


  protected Context context;


  public NotificationsService(Context context) {
    this.context = context;
  }


  public void showNotification(String title, String text, int iconId) {
    showNotification(title, text, iconId, null);
  }

  public void showNotification(String title, String text, int iconId, String tag) {
    NotificationManager notificationManager = getNotificationManager();

    Intent intent = new Intent(context, MainActivity.class);

    int notificationId;
    if(tag != null && currentlyShowingNotifications.containsKey(tag)) {
      notificationId = currentlyShowingNotifications.get(tag).getNotificationId();
    }
    else {
      notificationId = nextNotificationId++;
    }

    // The stack builder object will contain an artificial back stack for the started Activity.
    // This ensures that navigating backward from the Activity leads out of your application to the Home screen.
    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
    // Adds the back stack for the Intent (but not the Intent itself)
    stackBuilder.addParentStack(MainActivity.class);
    // Adds the Intent that starts the Activity to the top of the stack
    stackBuilder.addNextIntent(intent);

    PendingIntent pendingIntent = stackBuilder.getPendingIntent(notificationId, PendingIntent.FLAG_UPDATE_CURRENT);
//    PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationId, intent, 0);

    Notification notification = new NotificationCompat.Builder(context)
        .setContentTitle(title)
        .setContentText(text)
        .setSmallIcon(iconId)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build();

    if(tag != null) {
      notificationManager.notify(tag, notificationId, notification);
      currentlyShowingNotifications.put(tag, new NotificationInfo(notificationId, tag));
    }
    else {
      notificationManager.notify(notificationId, notification);
    }
  }


  public boolean dismissNotification(String notificationTag) {
    if(notificationTag != null && currentlyShowingNotifications.containsKey(notificationTag)) {
      NotificationInfo notificationInfo = currentlyShowingNotifications.remove(notificationTag);
      NotificationManager notificationManager = getNotificationManager();

      notificationManager.cancel(notificationInfo.getTag(), notificationInfo.getNotificationId());
      return true;
    }

    return false;
  }


  protected NotificationManager getNotificationManager() {
    return (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
  }

}
