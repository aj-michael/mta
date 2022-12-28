package net.ajmichael.mta;

import java.util.List;
import java.io.IOException;

interface LTrainLookup {

  public List<Long> nextTrains() throws IOException;
}
