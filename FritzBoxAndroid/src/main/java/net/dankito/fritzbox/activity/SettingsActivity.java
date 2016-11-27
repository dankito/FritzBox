package net.dankito.fritzbox.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import net.dankito.fritzbox.FritzBoxAndroidApplication;
import net.dankito.fritzbox.R;
import net.dankito.fritzbox.model.UserSettings;
import net.dankito.fritzbox.services.FritzBoxClient;
import net.dankito.fritzbox.services.UserSettingsManager;
import net.dankito.fritzbox.util.AlertHelper;
import net.dankito.fritzbox.utils.web.callbacks.LoginCallback;
import net.dankito.fritzbox.utils.web.responses.LoginResponse;

import javax.inject.Inject;

/**
 * Created by ganymed on 27/11/16.
 */

public class SettingsActivity extends AppCompatActivity {

  @Inject
  protected UserSettings userSettings;

  @Inject
  protected UserSettingsManager userSettingsManager;

  @Inject
  protected FritzBoxClient fritzBoxClient;


  protected EditText edtxtAddress;

  protected EditText edtxtPassword;

  protected CheckBox chkbxPeriodicallyCheckForMissedCalls;

  protected TextView txtvwEveryLabel;
  protected EditText edtxtPeriodicalMissedCallsCheckInterval;
  protected TextView txtvwMinutesLabel;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ((FritzBoxAndroidApplication)getApplicationContext()).getComponent().inject(this);

    setupViews();
  }

  protected void setupViews() {
    setContentView(R.layout.fragment_settings);

    if(userSettings.isFritzBoxAddressSet() == false || userSettings.isFritzBoxPasswordSet() == false) {
      TextView txtvwHintFritzBoxAddressOrPasswordNotSet = (TextView)findViewById(R.id.txtvwHintFritzBoxAddressOrPasswordNotSet);
      txtvwHintFritzBoxAddressOrPasswordNotSet.setVisibility(View.VISIBLE);
    }

    edtxtAddress = (EditText)findViewById(R.id.edtxtAddress);
    edtxtAddress.setText(userSettings.getFritzBoxAddress());

    edtxtPassword = (EditText)findViewById(R.id.edtxtPassword);
    edtxtPassword.setText(userSettings.getFritzBoxPassword());

    Button btnTestFritzBoxSettings = (Button)findViewById(R.id.btnTestFritzBoxSettings);
    btnTestFritzBoxSettings.setOnClickListener(btnTestFritzBoxSettingsClickListener);

    txtvwEveryLabel = (TextView)findViewById(R.id.txtvwEveryLabel);
    txtvwMinutesLabel = (TextView)findViewById(R.id.txtvwMinutesLabel);

    edtxtPeriodicalMissedCallsCheckInterval = (EditText)findViewById(R.id.edtxtPeriodicalMissedCallsCheckInterval);
    edtxtPeriodicalMissedCallsCheckInterval.setText("" + (userSettings.getPeriodicalMissedCallsCheckInterval() / 1000 / 60));

    chkbxPeriodicallyCheckForMissedCalls = (CheckBox)findViewById(R.id.chkbxPeriodicallyCheckForMissedCalls);
    chkbxPeriodicallyCheckForMissedCalls.setOnCheckedChangeListener(chkbxPeriodicallyCheckForMissedCallsCheckedChangeListener);
    chkbxPeriodicallyCheckForMissedCalls.setChecked(userSettings.isPeriodicalMissedCallsCheckEnabled());

    Button btnOk = (Button)findViewById(R.id.btnOk);
    btnOk.setOnClickListener(btnOkClickListener);

    Button btnCancel = (Button)findViewById(R.id.btnCancel);
    btnCancel.setOnClickListener(btnCancelClickListener);

    setupToolbar();
  }

  protected void setupToolbar() {
    Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
    toolbar.setTitle(R.string.action_settings);

    setSupportActionBar(toolbar);

    ActionBar actionBar = getSupportActionBar();
    if(actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setHomeButtonEnabled(true);
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if(id == android.R.id.home) {
      closeActivity();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }


  protected void testFritzBoxSettings() {
    fritzBoxClient.loginAsync(edtxtAddress.getText().toString(), edtxtPassword.getText().toString(), new LoginCallback() {
      @Override
      public void completed(LoginResponse response) {
        if(response.isSuccessful() == false) {
          showTestingFritzBoxSettingsFailedMessage(response);
        }
        else {
          showTestingFritzBoxSettingsSucceededMessage();
        }
      }
    });
  }

  protected void showTestingFritzBoxSettingsFailedMessage(LoginResponse response) {
    String title = getString(R.string.fragment_settings_testing_fritz_box_settings_failed_title);
    AlertHelper.showMessageThreadSafe(this, response.getError(), title);
  }

  protected void showTestingFritzBoxSettingsSucceededMessage() {
    AlertHelper.showMessageThreadSafe(this, getString(R.string.fragment_settings_testing_fritz_box_settings_succeeded));
  }


  protected void saveUserSettings() {
    userSettings.setFritzBoxAddress(edtxtAddress.getText().toString());
    userSettings.setFritzBoxPassword(edtxtPassword.getText().toString());

    userSettings.setPeriodicalMissedCallsCheckEnabled(chkbxPeriodicallyCheckForMissedCalls.isChecked());
    try {
      int periodInMinutes = Integer.parseInt(edtxtPeriodicalMissedCallsCheckInterval.getText().toString());
      userSettings.setPeriodicalMissedCallsCheckInterval(periodInMinutes * 60 * 1000);
    } catch(Exception shouldNeverOccur) { }

    try {
      userSettingsManager.saveUserSettings(userSettings);
    } catch(Exception e) {
      // TODO: show error message to user
    }
  }


  protected View.OnClickListener btnTestFritzBoxSettingsClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      testFritzBoxSettings();
    }
  };

  protected CompoundButton.OnCheckedChangeListener chkbxPeriodicallyCheckForMissedCallsCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
      edtxtPeriodicalMissedCallsCheckInterval.setEnabled(checked);
      txtvwEveryLabel.setEnabled(checked);
      txtvwMinutesLabel.setEnabled(checked);
    }
  };

  protected View.OnClickListener btnOkClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      saveUserSettings();
      closeActivity();
    }
  };

  protected View.OnClickListener btnCancelClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      closeActivity();
    }
  };

  protected void closeActivity() {
    finish();
  }

}
