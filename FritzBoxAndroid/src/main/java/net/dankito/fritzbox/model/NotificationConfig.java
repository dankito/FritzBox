package net.dankito.fritzbox.model;

/**
 * Created by ganymed on 01/12/16.
 */

public class NotificationConfig {

  protected String title;

  protected String text;

  protected boolean isMultiLineText;

  protected int iconId;


  public NotificationConfig() {

  }

  public NotificationConfig(String title, String text, int iconId) {
    this.title = title;
    this.text = text;
    this.iconId = iconId;
  }

  public NotificationConfig(String title, String text, int iconId, boolean isMultiLineText) {
    this(title, text, iconId);
    this.isMultiLineText = isMultiLineText;
  }


  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public boolean isMultiLineText() {
    return isMultiLineText;
  }

  public void setMultiLineText(boolean multiLineText) {
    isMultiLineText = multiLineText;
  }

  public int getIconId() {
    return iconId;
  }

  public void setIconId(int iconId) {
    this.iconId = iconId;
  }

}
