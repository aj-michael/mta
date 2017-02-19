package net.ajmichael.mta;

import com.google.transit.realtime.NyctSubway;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;

public final class Main {
  private static final String BASE_FEED_URL = "http://datamine.mta.info/mta_esi.php?key=";
  private static final String CONFIG_FILE = "config.properties";
  private static final Properties properties = new Properties();

  static {
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    try (InputStream configStream = contextClassLoader.getResourceAsStream(CONFIG_FILE)) {
      properties.load(configStream);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load properties file", e);
    }
  }

  public static void main(String[] args) throws Exception {
    URL url = new URL(BASE_FEED_URL + properties.getProperty("mtaapikey"));
    NyctSubway.NyctFeedHeader nyctFeedHeader =
        NyctSubway.NyctFeedHeader.parseFrom(url.openStream());
    System.out.println(nyctFeedHeader);
    List<NyctSubway.TripReplacementPeriod> tripReplacementPeriods =
        nyctFeedHeader.getTripReplacementPeriodList();
    for (NyctSubway.TripReplacementPeriod tripReplacementPeriod : tripReplacementPeriods) {
      System.out.println(tripReplacementPeriod.getRouteId());
    }
  }
}
