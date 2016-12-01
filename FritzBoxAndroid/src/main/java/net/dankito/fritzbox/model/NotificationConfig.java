package net.dankito.fritzbox.model;

/**
 * Created by ganymed on 01/12/16.
 */

public class NotificationConfig {

  protected String title;

  protected String text;

  protected boolean isMultiLineText;

  protected int iconId;

  protected boolean letLedBlink;

  protected int ledArgb;

  protected int ledOnMillis;

  protected int ledOffMillis;


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

  public NotificationConfig(int ledArgb, int ledOnMillis, int ledOffMillis) {
    this.letLedBlink = true;
    this.ledArgb = ledArgb;
    this.ledOnMillis = ledOnMillis;
    this.ledOffMillis = ledOffMillis;
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

  public boolean letLedBlink() {
    return letLedBlink;
  }

  public void setLetLedBlink(boolean letLedBlink) {
    this.letLedBlink = letLedBlink;
  }

  public void setLetLedBlink(int ledArgb, int ledOnMillis, int ledOffMillis) {
    setLetLedBlink(true);
    setLedArgb(ledArgb);
    setLedOnMillis(ledOnMillis);
    setLedOffMillis(ledOffMillis);
  }

  public int getLedArgb() {
    return ledArgb;
  }

  public void setLedArgb(int ledArgb) {
    this.ledArgb = ledArgb;
  }

  public int getLedOnMillis() {
    return ledOnMillis;
  }

  public void setLedOnMillis(int ledOnMillis) {
    this.ledOnMillis = ledOnMillis;
  }

  public int getLedOffMillis() {
    return ledOffMillis;
  }

  public void setLedOffMillis(int ledOffMillis) {
    this.ledOffMillis = ledOffMillis;
  }

}
