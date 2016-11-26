package net.dankito.fritzbox.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.dankito.fritzbox.R;
import net.dankito.fritzbox.model.Call;
import net.dankito.fritzbox.model.CallType;
import net.dankito.fritzbox.model.UserSettings;
import net.dankito.fritzbox.utils.StringUtils;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by ganymed on 26/11/16.
 */

public class CallListAdapter extends BaseAdapter {

  protected static final DateFormat CALL_DATE_FORMAT = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());


  protected Activity activity;

  protected UserSettings userSettings;

  protected List<Call> callList = new ArrayList<>();


  public CallListAdapter(Activity activity, UserSettings userSettings) {
    this.activity = activity;
    this.userSettings = userSettings;
  }


  public void setCallListThreadSafe(final List<Call> callList) {
    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        setCallList(callList);
      }
    });
  }

  public void setCallList(List<Call> callList) {
    this.callList = callList;

    notifyDataSetChanged();
  }


  @Override
  public int getCount() {
    return callList.size();
  }

  @Override
  public Object getItem(int index) {
    return callList.get(index);
  }

  @Override
  public long getItemId(int index) {
    return index;
  }

  @Override
  public View getView(int index, View convertView, ViewGroup parent) {
    if(convertView == null) {
      convertView = activity.getLayoutInflater().inflate(R.layout.list_item_call, parent, false);
    }

    Call call = (Call)getItem(index);

    TextView txtvwCallerNumber = (TextView)convertView.findViewById(R.id.txtvwCallerNumber);
    if(StringUtils.isNotNullOrEmpty(call.getCallerName())) {
      txtvwCallerNumber.setText(call.getCallerNumber() + " (" + call.getCallerNumber() + ")");
    }
    else {
      txtvwCallerNumber.setText(call.getCallerNumber());
    }

    TextView txtvwCallDuration = (TextView)convertView.findViewById(R.id.txtvwCallDuration);
    txtvwCallDuration.setText(getCallDurationTextRepresentation(call.getDuration()));

    TextView txtvwCallDate = (TextView)convertView.findViewById(R.id.txtvwCallDate);
    txtvwCallDate.setText(CALL_DATE_FORMAT.format(call.getDate()));

    ImageView imgvwCallTypeIcon = (ImageView)convertView.findViewById(R.id.imgvwCallTypeIcon);
    Picasso.with(activity)
        .load(getCallTypeIconUri(call.getType()))
        .into(imgvwCallTypeIcon);

    return convertView;
  }

  protected String getCallDurationTextRepresentation(int callDuration) {
    if(callDuration <= 0) {
      return "";
    }

    int hours = callDuration / 60;
    int minutes = callDuration % 60;

    return String.format("%d:%02d", hours, minutes);
  }

  protected String getCallTypeIconUri(CallType type) {
    String baseUri = "http://" + userSettings.getFritzboxAddress() + "/css/default/images/";

    if(type == CallType.INCOMMING_CALL) {
      return baseUri + "callin.gif";
    }
    else if(type == CallType.OUTGOING_CALL) {
      return baseUri + "callout.gif";
    }
    else if(type == CallType.MISSED_CALL) {
      return baseUri + "callinfailed.gif";
    }

    return null;
  }

}
