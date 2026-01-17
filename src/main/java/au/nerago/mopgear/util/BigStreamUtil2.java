package au.nerago.mopgear.util;

import java.time.Duration;
import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class BigStreamUtil2 {
    private static final long TIME_RATE = 5000;

    public static <T> Stream<T> countProgress(long estimate, Instant startTime, Stream<T> inputStream) {
        if (startTime == null)
            return inputStream;

        Timer timer = new Timer(true);
        ProgressTask task = new ProgressTask(estimate, startTime);
        timer.scheduleAtFixedRate(task, TIME_RATE, TIME_RATE);
        return task.monitorStream(inputStream);
    }

    private static class ProgressTask extends TimerTask {
        private final ThreadCounters counters = new ThreadCounters();
        private final double percentMultiply;
        private final Instant startTime;

        public ProgressTask(long estimate, Instant startTime) {
            this.percentMultiply = 100.0 / estimate;
            this.startTime = startTime;
        }

        @Override
        public void run() {
            long curr = counters.getTotal();
            BigStreamUtil.reportProgress(curr, percentMultiply, startTime);
        }

        public <T> Stream<T> monitorStream(Stream<T> inputStream) {
            return inputStream
                    .peek(_ -> counters.incrementAndGet())
                    .onClose(this::cancel);
        }
    }
}
