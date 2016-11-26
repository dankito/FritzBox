package net.dankito.fritzbox.web.callbacks;

/**
 * Created by ganymed on 03/11/16.
 */

public interface DownloadProgressListener {

  void progress(float progress, byte[] downloadedChunk);

}
