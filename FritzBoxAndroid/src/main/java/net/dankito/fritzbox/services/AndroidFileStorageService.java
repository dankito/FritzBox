package net.dankito.fritzbox.services;

import android.content.Context;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by ganymed on 26/11/16.
 */

public class AndroidFileStorageService implements IFileStorageService {

  protected Context context;

  protected ObjectMapper mapper = new ObjectMapper();


  public AndroidFileStorageService(Context context) {
    this.context = context;
  }


  public void writeObjectToFile(Object object, String filename) throws Exception {
    String json = mapper.writeValueAsString(object);
    writeToFile(json, filename);
  }

  public void writeToFile(String fileContent, String filename) throws Exception {
    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(filename, Context.MODE_PRIVATE));
    outputStreamWriter.write(fileContent);
    outputStreamWriter.close();
  }


  public <T> T readObjectFromFile(String filename, Class<T> objectClass) throws Exception {
    String json = readFromFile(filename);

    return mapper.readValue(json, objectClass);
  }

  public String readFromFile(String filename) throws Exception {
    String fileContent = "";

    InputStream inputStream = context.openFileInput(filename);

    if(inputStream != null) {
      InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
      BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
      String receiveString = "";
      StringBuilder stringBuilder = new StringBuilder();

      while ( (receiveString = bufferedReader.readLine()) != null ) {
        stringBuilder.append(receiveString);
      }

      inputStream.close();
      fileContent = stringBuilder.toString();
    }

    return fileContent;
  }

}
