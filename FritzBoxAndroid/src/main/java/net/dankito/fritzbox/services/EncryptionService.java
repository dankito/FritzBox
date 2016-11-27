package net.dankito.fritzbox.services;

import android.content.Context;
import android.util.Base64;

import net.dankito.fritzbox.R;
import net.dankito.fritzbox.services.exceptions.EncryptionServiceException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Copied from https://github.com/sveinungkb/encrypted-userprefs/blob/master/src/SecurePreferences.java
 */
public class EncryptionService implements IEncryptionService {

  private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
  private static final String SECRET_KEY_HASH_TRANSFORMATION = "SHA-256";
  private static final String CHARSET = "UTF-8";

  private final Cipher writer;
  private final Cipher reader;

  /**
   * This will initialize an instance of the SecurePreferences class
   * Hardcoding your key in the application is bad, but better than plaintext preferences. Having the user enter the key upon application launch is a safe(r) alternative, but annoying to the user.
   * true will encrypt both values and keys. Keys can contain a lot of information about
   * the plaintext value of the value which can be used to decipher the value.
   * @throws Exception
   */
  public EncryptionService(Context context) throws EncryptionServiceException {
    try {
      this.writer = Cipher.getInstance(TRANSFORMATION);
      this.reader = Cipher.getInstance(TRANSFORMATION);

      InputStream inputStream = context.getResources().openRawResource(R.raw.a);
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
      String a = bufferedReader.readLine();
      String b = bufferedReader.readLine();
      bufferedReader.close();
      inputStream.close();
      a = decrypt(a, "0366D8637F9C6B21", new byte[]{76, 75, 68, 83, 106, 102, 101, 114, 116, 115, 57, 117, 108, 107, 51, 52});
      b = decrypt(b, a, new byte[]{100, 51, 70, -61, -92, 37, 100, 57, 74, 97, 102, -62, -89, -61, -68, 42});

      initCiphers(b);
    } catch(Exception e) {
      throw new EncryptionServiceException(e);
    }
  }

  protected void initCiphers(String secureKey) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException,
      InvalidAlgorithmParameterException {
    IvParameterSpec ivSpec = getIv();
    SecretKeySpec secretKey = getSecretKey(secureKey);

    writer.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
    reader.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
  }

  protected IvParameterSpec getIv() {
    byte[] iv = new byte[writer.getBlockSize()];
    System.arraycopy("fldsjfodasjifudslfjdsaofshaufihadsf".getBytes(), 0, iv, 0, writer.getBlockSize());

    return new IvParameterSpec(iv);
  }

  protected SecretKeySpec getSecretKey(String key) throws UnsupportedEncodingException, NoSuchAlgorithmException {
    byte[] keyBytes = createKeyBytes(key);
    return new SecretKeySpec(keyBytes, TRANSFORMATION);
  }

  protected byte[] createKeyBytes(String key) throws UnsupportedEncodingException, NoSuchAlgorithmException {
    MessageDigest md = MessageDigest.getInstance(SECRET_KEY_HASH_TRANSFORMATION);
    md.reset();
    byte[] keyBytes = md.digest(key.getBytes(CHARSET));
    return keyBytes;
  }


  @Override
  public String encrypt(String value) throws EncryptionServiceException {
    return encrypt(value, writer);
  }

  protected String encrypt(String value, Cipher writer) throws EncryptionServiceException {
    try {
      byte[] secureValue = convert(writer, value.getBytes(CHARSET));

      return Base64.encodeToString(secureValue, Base64.NO_WRAP);
    } catch(UnsupportedEncodingException e) {
      throw new EncryptionServiceException(e);
    }
  }

  @Override
  public String decrypt(String securedEncodedValue) throws EncryptionServiceException {
    byte[] securedValue = Base64.decode(securedEncodedValue, Base64.NO_WRAP);
    byte[] value = convert(reader, securedValue);

    try {
      return new String(value, CHARSET);
    } catch(UnsupportedEncodingException e) {
      throw new EncryptionServiceException(e);
    }
  }

  protected String decrypt(String encryptedText, String AES_KEY, byte[] IV_VECTOR) throws Exception {
    byte[] encryted_bytes = Base64.decode(encryptedText, Base64.DEFAULT);

    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
    byte[] static_key = AES_KEY.getBytes();

    SecretKeySpec keySpec = new SecretKeySpec(static_key, "AES");
    IvParameterSpec ivSpec = new IvParameterSpec(IV_VECTOR);
    cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

    byte[] decrypted = cipher.doFinal(encryted_bytes);
    String result = new String(decrypted);

    return result;
  }

  protected byte[] convert(Cipher cipher, byte[] bs) throws EncryptionServiceException {
    try {
      return cipher.doFinal(bs);
    } catch(Exception e) {
      throw new EncryptionServiceException(e);
    }
  }

}
