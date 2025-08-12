package au.nicholas.hardy.mopgear.util;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class BigStreamUtil {
    public static <T> Stream<T> countProgress(final long estimate, Instant startTime, Stream<T> sets) {
        final double estimateFloat = estimate / 100.0;
        AtomicLong count = new AtomicLong();
        return sets.peek(set -> {
            long curr = count.incrementAndGet();
            if (curr % 1000000 == 0) {
                double percent = ((double) curr) / estimateFloat;
                synchronized (System.out) {
                    System.out.print(curr);
                    System.out.print(" ");
                    System.out.printf("%.2f", percent);
                    Duration estimateRemain = estimateRemain(startTime, percent);
                    System.out.print(" ");
                    System.out.print(estimateRemain);
                    System.out.println();
                }
            }
        });
    }

    public static Duration estimateRemain(Instant startTime, double percent) {
        final int factor = 100;

        long multiply = (long) (factor * 100 / percent);

        Duration timeTaken = Duration.between(startTime, Instant.now());
        Duration totalEstimate = timeTaken.multipliedBy(multiply).dividedBy(factor);
        return totalEstimate.minus(timeTaken);
    }
}
