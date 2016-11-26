package net.dankito.fritzbox.services;

import net.dankito.fritzbox.model.HashAlgorithm;
import net.dankito.fritzbox.utils.web.IWebClient;
import net.dankito.fritzbox.utils.web.RequestParameters;
import net.dankito.fritzbox.utils.web.callbacks.GetStringInfoCallback;
import net.dankito.fritzbox.utils.web.callbacks.LoginCallback;
import net.dankito.fritzbox.utils.web.callbacks.RequestCallback;
import net.dankito.fritzbox.utils.web.responses.GetStringInfoResponse;
import net.dankito.fritzbox.utils.web.responses.LoginResponse;
import net.dankito.fritzbox.utils.web.responses.WebClientResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * In large parts a copy of https://github.com/ISchwarz23/FritzBox-API.
 */
public class FritzBoxClient {

  protected static final int COUNT_CONNECTION_RETRIES = 2;


  private static final Logger log = LoggerFactory.getLogger(FritzBoxClient.class);


  protected String fritzBoxAddress;

  protected String sessionId;

  protected IWebClient webClient;

  protected IDigestService digestService;


  public FritzBoxClient(String fritzBoxAddress, IWebClient webClient, IDigestService digestService) {
    this.fritzBoxAddress = fritzBoxAddress;
    this.webClient = webClient;
    this.digestService = digestService;
  }


  public void loginAsync(final String password, final LoginCallback callback) throws Exception {
    this.getChallenge(new GetStringInfoCallback() {
      @Override
      public void completed(GetStringInfoResponse response) {
        if(response.isSuccessful() == false) {
          callback.completed(new LoginResponse(response.getError()));
        }
        else {
          String challenge = response.getStringInfo();
          sendLogin(challenge, password, new GetStringInfoCallback() {
            @Override
            public void completed(GetStringInfoResponse response) {
              loginCompleted(response, callback);
            }
          });
        }
      }
    });
  }

  protected void loginCompleted(GetStringInfoResponse loginResponse, LoginCallback callback) {
    if(loginResponse.isSuccessful() == false) {
      callback.completed(new LoginResponse(loginResponse.getError()));
    }
    else {
      String sid = loginResponse.getStringInfo();

      if(sid.equals("0000000000000000")) {
        callback.completed(new LoginResponse("Not able to login to FritzBox. Wrong password!"));
      }
      else {
        this.sessionId = sid;
        callback.completed(new LoginResponse(true));
      }
    }
  }

  protected void getChallenge(final GetStringInfoCallback callback) {
    String url = "http://" + this.fritzBoxAddress + "/login_sid.lua";
    webClient.getAsync(createDefaultRequestParameters(url), new RequestCallback() {
      @Override
      public void completed(WebClientResponse response) {
        if(response.isSuccessful() == false) {
          callback.completed(new GetStringInfoResponse(response.getError()));
        }
        else {
          // get challenge challenge
          String responseBody = response.getBody();
          final String challenge = responseBody.substring(responseBody.indexOf("<Challenge>") + 11,
              responseBody.indexOf("<Challenge>") + 19);

          callback.completed(new GetStringInfoResponse(true, challenge));
        }
      }
    });
  }

  protected void sendLogin(final String challenge, final String password, final GetStringInfoCallback callback) {
    try {
      final String loginChallengeResponse = calculateLoginChallengeResponse(challenge, password);
      String url = "http://" + this.fritzBoxAddress + "/login_sid.lua?user=&response=" + loginChallengeResponse;

      webClient.getAsync(createDefaultRequestParameters(url), new RequestCallback() {
        @Override
        public void completed(WebClientResponse response) {
          if(response.isSuccessful() == false) {
            callback.completed(new GetStringInfoResponse(response.getError()));
          }
          else {
            parseSessionIdFromLoginResponse(response, callback);
            return;
          }
        }
      });
    } catch (final UnsupportedEncodingException e) {
      // will never appear
      log.error("UTF-16LE is not supported", e);
      callback.completed(new GetStringInfoResponse("UTF-16LE is not supported"));
    } catch (final NoSuchAlgorithmException e) {
      // will never appear
      log.error("MD5 is not supported", e);
    }
  }

  protected void parseSessionIdFromLoginResponse(WebClientResponse response, GetStringInfoCallback callback) {
    try {
      String responseBody = response.getBody();

      String sessionId = responseBody.substring(responseBody.indexOf("<SID>") + 5, responseBody.indexOf("<SID>") + 21);

      callback.completed(new GetStringInfoResponse(true, sessionId));
    } catch(Exception e) {
      log.error("Could not parse SessionId from Login Response", e);
      callback.completed(new GetStringInfoResponse("Could not parse SessionId from Login Response: " + e.getLocalizedMessage()));
    }
  }

  protected String calculateLoginChallengeResponse(String challenge, String password) throws UnsupportedEncodingException, NoSuchAlgorithmException {
    final String stringToHash = challenge + "-" + password;
    String stringToHashUTF16 = new String(stringToHash.getBytes("UTF-16LE"), "UTF-8");

    final String md5 = digestService.calculateDigest(stringToHashUTF16, HashAlgorithm.MD5);
    return challenge + "-" + md5;
  }


  protected RequestParameters createDefaultRequestParameters(String url) {
    RequestParameters parameters = new RequestParameters(url);

    parameters.setCountConnectionRetries(COUNT_CONNECTION_RETRIES);

    return parameters;
  }

}
