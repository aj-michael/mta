package net.ajmichael.mta;

import com.google.transit.realtime.GtfsRealtime;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

class ApiMtaInfoLookup implements LTrainLookup {

  private static final String CONFIG_FILE = "config.properties";
  private static final String apiKey;
  private static final URL L_TRAIN_FEED_URL;

  static {
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    try (InputStream configStream = contextClassLoader.getResourceAsStream(CONFIG_FILE)) {
      Properties properties = new Properties();
      properties.load(configStream);
      apiKey = properties.getProperty("mtaapikey");
      L_TRAIN_FEED_URL =
          new URL("https://api-endpoint.mta.info/Dataservice/mtagtfsfeeds/nyct%2Fgtfs-l");
    } catch (IOException e) {
      throw new RuntimeException("Failed to initialize Main", e);
    }
  }

  private final String stopId;

  public ApiMtaInfoLookup(String stopId) {
    this.stopId = stopId;
  }

  private static InputStream makeRequest() throws IOException {
    HttpURLConnection connection = (HttpURLConnection) L_TRAIN_FEED_URL.openConnection();
    System.out.println("Setting x-api-key to " + apiKey);
    connection.setRequestProperty("Accept", "application/x-protobuf");
    connection.setRequestProperty("x-api-key", apiKey);
    connection.setRequestMethod("GET");
    connection.setUseCaches(false);
    connection.setDoInput(true);
    connection.setDoOutput(true);
    return connection.getInputStream();
  }

  @Override
  public List<Long> nextTrains() throws IOException {
    return GtfsRealtime.FeedMessage.parseFrom(makeRequest())
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