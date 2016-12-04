package net.dankito.fritzbox.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.dankito.fritzbox.MainActivity;
import net.dankito.fritzbox.model.NotificationConfig;
import net.dankito.fritzbox.model.NotificationInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ganymed on 26/11/16.
 */

public class NotificationsService {

  protected static final String CURRENTLY_SHOWING_NOTIFICATIONS_FILENAME = "CurrentlyShownNotifications.json";

  private static final Logger log = LoggerFactory.getLogger(NotificationsService.class);


  protected static int nextNotificationId = 1;


  protected Context context;

  protected IFileStorageService fileStorageService;

  protected Map<String, NotificationInfo> currentlyShowingNotifications = new ConcurrentHashMap<>();


  public NotificationsService(Context context, IFileStorageService fileStorageService) {
    this.context = context;
    this.fileStorageService = fileStorageService;

    loadCurrentlyShowingNotifications();
  }


  public void showNotification(NotificationConfig config) {
    showNotification(config, null);
  }

  public void showNotification(NotificationConfig config, String tag) {
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

    Notification notification = createNotification(config, pendingIntent);

    if(tag != null) {
      notificationManager.notify(tag, notificationId, notification);
      addToCurrentlyShowingNotifications(notificationId, tag);
    }
    else {
      notificationManager.notify(notificationId, notification);
    }
  }

  protected Notification createNotification(NotificationConfig config, PendingIntent pendingIntent) {
    NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
        .setContentTitle(config.getTitle())
        .setContentText(config.getText())
        .setSmallIcon(config.getIconId())
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        ;

    if(config.isMultiLineText()) {
      builder.setStyle(new NotificationCompat.BigTextStyle().bigText(config.getText()));
    }

    if(config.letLedBlink()) {
      builder.setLights(config.getLedArgb(), config.getLedOnMillis(), config.getLedOffMillis());
    }

    return builder.build();
  }


  public boolean dismissNotification(String notificationTag) {
    if(notificationTag != null && currentlyShowingNotifications.containsKey(notificationTag)) {
      NotificationInfo notificationInfo = removeFromCurrentlyShowingNotifications(notificationTag);
      NotificationManager notificationManager = getNotificationManager();

      notificationManager.cancel(notificationInfo.getTag(), notificationInfo.getNotificationId());
      return true;
    }

    return false;
  }


  protected void addToCurrentlyShowingNotifications(int notificationId, String notificationTag) {
    NotificationInfo addedNotification = new NotificationInfo(notificationId, notificationTag);

    currentlyShowingNotifications.put(notificationTag, addedNotification);

    saveCurrentlyShowingNotifications();
  }

  protected NotificationInfo removeFromCurrentlyShowingNotifications(String notificationTag) {
    NotificationInfo removedNotification = currentlyShowingNotifications.remove(notificationTag);

    saveCurrentlyShowingNotifications();

    return removedNotification;
  }

  protected void loadCurrentlyShowingNotifications() {
    try {
      String json = fileStorageService.readFromFile(CURRENTLY_SHOWING_NOTIFICATIONS_FILENAME);
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

      currentlyShowingNotifications = mapper.readValue(json, new TypeReference<ConcurrentHashMap<String, NotificationInfo>>(){ } );
    } catch(Exception e) {
      log.error("Could not load " + CURRENTLY_SHOWING_NOTIFICATIONS_FILENAME, e);
    }
  }

  protected void saveCurrentlyShowingNotifications() {
    try {
      fileStorageService.writeObjectToFile(currentlyShowingNotifications, CURRENTLY_SHOWING_NOTIFICATIONS_FILENAME);
    } catch(Exception e) {
      log.error("Could not write currently showing notifications to file " + CURRENTLY_SHOWING_NOTIFICATIONS_FILENAME, e);
    }
  }


  protected NotificationManager getNotificationManager() {
    return (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
  }

}
