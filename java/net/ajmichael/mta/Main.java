package net.ajmichael.mta;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

// L1 is 8th ave
// L6 is Bedford
// L17 is Myrtle-Wyckoff
// L29 is Rockaway-Canarsie
// South trains are Brooklyn-bound
public final class Main {
  private static final String MANHATTAN_BOUND_BEDFORD = "L06N";
  private static final long TIMER_DELAY_MILLIS = 3000;
  private static final long TIMER_PERIOD_MILLIS = 10000;
  private static final File fifoFile = new File("/tmp/mtafifo");



  public static void main(String[] args) throws Exception {
    new Timer().scheduleAtFixedRate(new FetchTask(), TIMER_DELAY_MILLIS, TIMER_PERIOD_MILLIS);
  }

  private static class FetchTask extends TimerTask {

    @Override
    public void run() {
      try {

        List<Long> minutesTilNextTrains = new LTrainLookup(MANHATTAN_BOUND_BEDFORD).nextTrains();
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
