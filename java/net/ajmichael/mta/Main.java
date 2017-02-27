package net.ajmichael.mta;

import com.google.transit.realtime.GtfsRealtime;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

// L1 is 8th ave
// L6 is Bedford
// L17 is Myrtle-Wyckoff
// L29 is Rockaway-Canarsie
// South trains are Brooklyn-bound
public final class Main {
  private static final String CONFIG_FILE = "config.properties";
  private static final String MANHATTAN_BOUND_BEDFORD = "L06N";
  private static final URL L_TRAIN_FEED_URL;
  private static final long TIMER_DELAY_MILLIS = 3000;
  private static final long TIMER_PERIOD_MILLIS = 10000;
  private static final File fifoFile = new File("/tmp/mtafifo");

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

  public static void main(String[] args) throws Exception {
    new Timer()
        .scheduleAtFixedRate(
            new FetchTask(L_TRAIN_FEED_URL), TIMER_DELAY_MILLIS, TIMER_PERIOD_MILLIS);
  }

  private static class FetchTask extends TimerTask {
    private final URL url;

    private FetchTask(URL url) {
      this.url = url;
    }

    @Override
    public void run() {
      try {
        List<Long> minutesTilNextTrains =
            GtfsRealtime.FeedMessage.parseFrom(url.openStream())
                .getEntityList()
                .stream()
                .map(GtfsRealtime.FeedEntity::getTripUpdate)
                .map(GtfsRealtime.TripUpdate::getStopTimeUpdateList)
                .filter(stopTimeUpdates -> !stopTimeUpdates.isEmpty())
                .flatMap(Collection::stream)
                .filter(
                    stopTimeUpdate -> stopTimeUpdate.getStopId().equals(MANHATTAN_BOUND_BEDFORD))
                .map(GtfsRealtime.TripUpdate.StopTimeUpdate::getArrival)
                .map(GtfsRealtime.TripUpdate.StopTimeEvent::getTime)
                .map(Instant::ofEpochSecond)
                .map(i -> Instant.now().until(i, ChronoUnit.MINUTES))
                .sorted()
                .collect(Collectors.toList());
        System.out.println("Next L trains at Bedford Ave");
        System.out.println(minutesTilNextTrains);
        PrintWriter printWriter = null;
        try {
          if (minutesTilNextTrains.size() >= 2) {
            printWriter = new PrintWriter(new FileWriter(fifoFile, true));
            printWriter.print(
                String.format("%d,%d", minutesTilNextTrains.get(0), minutesTilNextTrains.get(1)));
          } else {
            System.err.println(
                String.format("Only received %d next train times", minutesTilNextTrains.size()));
          }
        } catch (IOException e) {
          e.printStackTrace();
        } finally {
          if (printWriter != null) {
            printWriter.close();
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
