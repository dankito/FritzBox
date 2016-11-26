package net.dankito.fritzbox;

import net.dankito.fritzbox.model.Call;
import net.dankito.fritzbox.model.CallType;
import net.dankito.fritzbox.model.UserSettings;
import net.dankito.fritzbox.services.CsvParser;
import net.dankito.fritzbox.services.DigestService;
import net.dankito.fritzbox.services.FritzBoxClient;
import net.dankito.fritzbox.utils.StringUtils;
import net.dankito.fritzbox.utils.web.OkHttpWebClient;
import net.dankito.fritzbox.utils.web.callbacks.GetCallListCallback;
import net.dankito.fritzbox.utils.web.callbacks.LoginCallback;
import net.dankito.fritzbox.utils.web.responses.GetCallListResponse;
import net.dankito.fritzbox.utils.web.responses.LoginResponse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by ganymed on 26/11/16.
 */
public class FritzBoxClientTest {

  protected static final String TEST_DATA_FILENAME = "testdata.properties";

  protected static final String TEST_DATA_FRITZ_BOX_ADDRESS = "test.fritzbox.address";

  protected static final String TEST_DATA_FRITZ_BOX_PASSWORD = "test.fritzbox.password";


  protected FritzBoxClient underTest;

  protected UserSettings userSettings;


  @Before
  public void setUp() throws IOException {
    Properties testDataProperties = loadTestDataProperties();

    this.userSettings = new UserSettings(getTestFritzBoxAddress(testDataProperties), getTestFritzBoxPassword(testDataProperties));

    underTest = new FritzBoxClient(userSettings, new OkHttpWebClient(), new DigestService(), new CsvParser());
  }


  @Test
  public void loginAsync() {
    final List<LoginResponse> responseList = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.loginAsync(userSettings.getFritzboxPassword(), new LoginCallback() {
      @Override
      public void completed(LoginResponse response) {
        responseList.add(response);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(5, TimeUnit.SECONDS); } catch(Exception ignored) { }

    Assert.assertEquals(1, responseList.size());

    LoginResponse response = responseList.get(0);
    Assert.assertTrue(response.isSuccessful());
  }


  @Test
  public void parseCallListCsv() {
    final List<GetCallListResponse> responseList = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.getCallListAsync(userSettings.getFritzboxPassword(), new GetCallListCallback() {
      @Override
      public void completed(GetCallListResponse response) {
        responseList.add(response);
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(5, TimeUnit.MINUTES); } catch(Exception ignored) { }

    Assert.assertEquals(1, responseList.size());

    GetCallListResponse response = responseList.get(0);
    Assert.assertTrue(response.isSuccessful());
    Assert.assertTrue(response.getCallList().size() > 0);

    for(Call call : response.getCallList()) {
      if("Unbekannt".equals(call.getCallerName()) == false) {
        Assert.assertTrue(StringUtils.isNotNullOrEmpty(call.getCallerNumber()));
      }
      Assert.assertNotNull(call.getDate());
      Assert.assertNotNull(call.getType());
      Assert.assertTrue(StringUtils.isNotNullOrEmpty(call.getSubstationNumber()));

      if(call.getType() == CallType.MISSED_CALL) {
        Assert.assertEquals(0, call.getDuration());
      }
      else if(call.getType() == CallType.UNKNOWEN) {
        Assert.assertTrue(call.getDuration() > 0);
        Assert.assertTrue(StringUtils.isNotNullOrEmpty(call.getSubstationName()));
      }
    }
  }


  protected Properties loadTestDataProperties() throws IOException {
    InputStream testDataInputStream = getClass().getClassLoader().getResourceAsStream(TEST_DATA_FILENAME);
    Properties testDataProperties = new Properties();
    testDataProperties.load(testDataInputStream);
    return testDataProperties;
  }

  protected String getTestFritzBoxAddress(Properties testDataProperties) {
    return testDataProperties.getProperty(TEST_DATA_FRITZ_BOX_ADDRESS);
  }

  protected String getTestFritzBoxPassword(Properties testDataProperties) {
    return testDataProperties.getProperty(TEST_DATA_FRITZ_BOX_PASSWORD);
  }

}