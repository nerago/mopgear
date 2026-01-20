package au.nerago.mopgear.util;

import au.nerago.mopgear.domain.SkinnyItem;
import au.nerago.mopgear.domain.SolvableEquipOptionsMap;
import au.nerago.mopgear.domain.SolvableItemSet;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class BigStreamUtil {
    private static final long TIME_RATE = 5000;

    public static <T> Stream<T> countProgress(double estimate, Instant startTime, Stream<T> inputStream) {
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

        public ProgressTask(double estimate, Instant startTime) {
            this.percentMultiply = 100.0 / estimate;
            this.startTime = startTime;
        }

        @Override
        public void run() {
            long curr = counters.getTotal();
            reportProgress(curr, percentMultiply, startTime);
        }

        public <T> Stream<T> monitorStream(Stream<T> inputStream) {
            // TODO some kinda acculate op passing state?
            return inputStream
                    .peek(_ -> counters.incrementAndGet())
                    .onClose(this::cancel);
        }
    }

    static void reportProgress(long curr, double percentMultiply, Instant startTime) {
        double percent = (double) curr * percentMultiply;

        Duration timeTaken = Duration.between(startTime, Instant.now());
        Duration totalEstimate = timeTaken.multipliedBy(10000).dividedBy((long) (100 * percent));
        Duration estimateRemain = totalEstimate.minus(timeTaken);
        printProgressLine(curr, percent, estimateRemain);
    }

    static void printProgressLine(long curr, double percent, Duration estimateRemain) {
        if (estimateRemain.compareTo(HOUR_ONE) > 0) {
            System.out.printf("%,d %.1f%% %d:%02d:%02d\n", curr, percent, estimateRemain.toHours(), estimateRemain.toMinutesPart(), estimateRemain.toSecondsPart());
        } else if (estimateRemain.compareTo(MINUTE_ONE) > 0) {
            System.out.printf("%,d %.1f%% %02d:%02d\n", curr, percent, estimateRemain.toMinutes(), estimateRemain.toSecondsPart());
        } else {
            System.out.printf("%,d %.1f%% %dS\n", curr, percent, estimateRemain.toSecondsPart());
        }
    }

    private static final Duration HOUR_ONE = Duration.ofHours(1);
    private static final Duration MINUTE_ONE = Duration.ofMinutes(1);

    public static boolean fitsMaxLong(BigInteger estimate) {
        return estimate.compareTo(BigInteger.valueOf(Long.MAX_VALUE / 2)) < 0;
    }

    public static BigInteger estimateSets(SolvableEquipOptionsMap reforgedItems) {
        Optional<BigInteger> number = reforgedItems.entryStream().map(x -> BigInteger.valueOf(x.b().length)).reduce(BigInteger::multiply);
        if (number.isPresent()) {
            return number.get();
        } else {
            throw new RuntimeException("unable to determine item combination estimate");
        }
    }

    public static BigInteger estimateSets(List<SkinnyItem[]> skinnyOptions) {
        Optional<BigInteger> number =  skinnyOptions.stream().map(x -> BigInteger.valueOf(x.length)).reduce(BigInteger::multiply);
        if (number.isPresent()) {
            return number.get();
        } else {
            throw new RuntimeException("unable to determine item combination estimate");
        }
    }

    public static <X, T> BigInteger estimateSets(Map<X, List<T>> commonMap) {
        Optional<BigInteger> number = commonMap.values().stream().map(x -> BigInteger.valueOf(x.size())).reduce(BigInteger::multiply);
        if (number.isPresent()) {
            return number.get();
        } else {
            throw new RuntimeException("unable to determine item combination estimate");
        }
    }
}
