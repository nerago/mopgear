package au.nerago.mopgear.util;

import java.math.BigInteger;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class BigStreamUtilOld {
    public static <T> Stream<T> countProgress(double estimate, Instant startTime, Stream<T> inputStream) {
        final double percentMultiply = 100.0 / estimate;
        final long reportFrequency = chooseReportFrequency(BigInteger.valueOf((long) estimate));
        return coreCount(reportFrequency, percentMultiply, startTime, inputStream);
    }

    public static <T> Stream<T> countProgress(long estimate, Instant startTime, Stream<T> inputStream) {
        final double percentMultiply = 100.0 / estimate;
        final long reportFrequency = chooseReportFrequency(estimate, 1000000, 100000000);
        return coreCount(reportFrequency, percentMultiply, startTime, inputStream);
    }

    private static <T> Stream<T> coreCount(long reportFrequency, double percentMultiply, Instant startTime, Stream<T> inputStream) {
        if (startTime == null)
            return inputStream;

        AtomicLong count = new AtomicLong();
        return inputStream.peek(set -> {
            long curr = count.incrementAndGet();
            if (curr % reportFrequency == 0) {
                BigStreamUtil.reportProgress(curr, percentMultiply, startTime);
            }
        });
    }

    private static long chooseReportFrequency(long estimate, long min, long max) {
        long freq = Math.round(estimate / 100.0);
        int digitCount = 0;
        while (freq >= 10) {
            freq /= 10;
            digitCount++;
        }
        for (int i = 0; i < digitCount; ++i) {
            freq *= 10;
        }
        freq = Math.clamp(freq, min, max);
        return freq;
    }

    private static long chooseReportFrequency(BigInteger estimate) {
        if (!BigStreamUtil.fitsMaxLong(estimate)) {
            return chooseReportFrequency(Long.MAX_VALUE / 2, 100, 100000000);
        } else {
            return chooseReportFrequency(estimate.longValueExact(), 100, 100000000);
        }
    }
}
