package net.ajmichael.mta;

import com.google.transit.realtime.GtfsRealtime;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class LTrainLookup {

  private static final String CONFIG_FILE = "config.properties";
  private static final URL L_TRAIN_FEED_URL;

  static {
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    try (InputStream configStream = contextClassLoader.getResourceAsStream(CONFIG_FILE)) {
      Properties properties = new Properties();
      properties.load(configStream);
      L_TRAIN_FEED_URL =
          new URL(
              String.format(
                  "http://datamine.mta.info/mta_esi.php?feed_id=2&key=%s",
                  properties.getProperty("mtaapikey")));
    } catch (IOException e) {
      throw new RuntimeException("Failed to initialize Main", e);
    }
  }

  private final String stopId;

  public LTrainLookup(String stopId) {
    this.stopId = stopId;
  }

  public List<Long> nextTrains() throws IOException {
    return GtfsRealtime.FeedMessage.parseFrom(L_TRAIN_FEED_URL.openStream())
            .getEntityList()
            .stream()
            .map(GtfsRealtime.FeedEntity::getTripUpdate)
            .map(GtfsRealtime.TripUpdate::getStopTimeUpdateList)
            .filter(stopTimeUpdates -> !stopTimeUpdates.isEmpty())
            .flatMap(Collection::stream)
            .filter(
                stopTimeUpdate -> stopTimeUpdate.getStopId().equals(stopId))
            .map(GtfsRealtime.TripUpdate.StopTimeUpdate::getArrival)
            .map(GtfsRealtime.TripUpdate.StopTimeEvent::getTime)
            .map(Instant::ofEpochSecond)
            .map(i -> Instant.now().until(i, ChronoUnit.MINUTES))
            .sorted()
            .collect(Collectors.toList());
  }
}
