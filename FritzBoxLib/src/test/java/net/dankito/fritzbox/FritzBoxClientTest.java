package net.dankito.fritzbox;

import net.dankito.fritzbox.services.DigestService;
import net.dankito.fritzbox.services.FritzBoxClient;
import net.dankito.fritzbox.utils.web.OkHttpWebClient;
import net.dankito.fritzbox.utils.web.callbacks.LoginCallback;
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

  protected Properties testDataProperties = null;


  @Before
  public void setUp() throws IOException {
    this.testDataProperties = loadTestDataProperties();

    underTest = new FritzBoxClient(getTestFritzBoxAddress(), new OkHttpWebClient(), new DigestService());
  }


  @Test
  public void login() throws Exception {
    final List<LoginResponse> responseList = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.loginAsync(getTestFritzBoxPassword(), new LoginCallback() {
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


  protected Properties loadTestDataProperties() throws IOException {
    InputStream testDataInputStream = getClass().getClassLoader().getResourceAsStream(TEST_DATA_FILENAME);
    Properties testDataProperties = new Properties();
    testDataProperties.load(testDataInputStream);
    return testDataProperties;
  }

  protected String getTestFritzBoxAddress() {
    return testDataProperties.getProperty(TEST_DATA_FRITZ_BOX_ADDRESS);
  }

  protected String getTestFritzBoxPassword() {
    return testDataProperties.getProperty(TEST_DATA_FRITZ_BOX_PASSWORD);
  }

}