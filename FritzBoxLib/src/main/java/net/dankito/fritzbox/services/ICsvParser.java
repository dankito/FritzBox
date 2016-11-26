package net.dankito.fritzbox.services;

import net.dankito.fritzbox.model.Call;

import java.util.List;

/**
 * Created by ganymed on 26/11/16.
 */

public interface ICsvParser {

  List<Call> parseCallList(final String csvString);

}
