package net.dankito.fritzbox.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.dankito.fritzbox.FritzBoxAndroidApplication;
import net.dankito.fritzbox.R;
import net.dankito.fritzbox.model.UserSettings;
import net.dankito.fritzbox.services.UserSettingsManager;

import javax.inject.Inject;

/**
 * Created by ganymed on 27/11/16.
 */

public class SettingsActivity extends AppCompatActivity {

  @Inject
  protected UserSettings userSettings;

  @Inject
  protected UserSettingsManager userSettingsManager;

  protected EditText edtxtAddress;

  protected EditText edtxtPassword;


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


  protected void saveUserSettings() {
    userSettings.setFritzBoxAddress(edtxtAddress.getText().toString());
    userSettings.setFritzBoxPassword(edtxtPassword.getText().toString());

    try {
      userSettingsManager.saveUserSettings(userSettings);
    } catch(Exception e) {
      // TODO: show error message to user
    }
  }


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
