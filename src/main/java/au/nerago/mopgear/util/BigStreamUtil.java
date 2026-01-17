package au.nerago.mopgear.util;

import au.nerago.mopgear.domain.SkinnyItem;
import au.nerago.mopgear.domain.SolvableEquipOptionsMap;
import au.nerago.mopgear.domain.SolvableItemSet;
import au.nerago.mopgear.model.ModelCombined;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class BigStreamUtil {
    public static Stream<SolvableItemSet> countProgress(BigInteger estimate, Instant startTime, Stream<SolvableItemSet> inputStream) {
//        final double percentMultiply = BigDecimal.valueOf(100).divide(new BigDecimal(estimate), 4, RoundingMode.UP).doubleValue();
        final double percentMultiply = 100.0 / estimate.doubleValue();
        final long reportFrequency = chooseReportFrequency(estimate);
        return coreCount(reportFrequency, percentMultiply, startTime, inputStream);
    }

    public static <T> Stream<T> countProgress(long estimate, Instant startTime, Stream<T> inputStream) {
        final double percentMultiply = 100.0 / estimate;
        final long reportFrequency = chooseReportFrequency(estimate, 1000000, 100000000);
        return coreCount(reportFrequency, percentMultiply, startTime, inputStream);
    }

    public static <T> Stream<T> countProgressSmall(long estimate, Instant startTime, Stream<T> inputStream) {
        final double percentMultiply = 100.0 / estimate;
        final long reportFrequency = chooseReportFrequency(estimate, 100, 10000);
        return coreCount(reportFrequency, percentMultiply, startTime, inputStream);
    }

    private static <T> Stream<T> coreCount(long reportFrequency, double percentMultiply, Instant startTime, Stream<T> inputStream) {
        if (startTime == null)
            return inputStream;

        AtomicLong count = new AtomicLong();
        return inputStream.peek(set -> {
            long curr = count.incrementAndGet();
            if (curr % reportFrequency == 0) {
                reportProgress(curr, percentMultiply, startTime);
            }
        });
    }

    static void reportProgress(long curr, double percentMultiply, Instant startTime) {
        double percent = ((double) curr) * percentMultiply;
        Duration estimateRemain = estimateRemain(startTime, curr);
        printProgressLine(curr, percent, estimateRemain);
    }

    static void printProgressLine(long curr, double percent, Duration estimateRemain) {
        if (estimateRemain.compareTo(HOUR_ONE) > 0) {
            System.out.printf("%d %.1f%% %d:%02d:%02d\n", curr, percent, estimateRemain.toHours(), estimateRemain.toMinutesPart(), estimateRemain.toSecondsPart());
        } else if (estimateRemain.compareTo(MINUTE_ONE) > 0) {
            System.out.printf("%d %.1f%% %02d:%02d\n", curr, percent, estimateRemain.toMinutes(), estimateRemain.toSecondsPart());
        } else {
            System.out.printf("%d %.1f%% %dS\n", curr, percent, estimateRemain.toSecondsPart());
        }
    }

    private static final Duration HOUR_ONE = Duration.ofHours(1);
    private static final Duration MINUTE_ONE = Duration.ofMinutes(1);

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
        if (!fitsMaxLong(estimate)) {
            return chooseReportFrequency(Long.MAX_VALUE / 2, 100, 100000000);
        } else {
            return chooseReportFrequency(estimate.longValueExact(), 100, 100000000);
        }
    }

    public static boolean fitsMaxLong(BigInteger estimate) {
        return estimate.compareTo(BigInteger.valueOf(Long.MAX_VALUE / 2)) < 0;
    }

    public static Duration estimateRemain(Instant startTime, double percent) {
        final int factor = 100;

        long multiply = (long) (factor * 100 / percent);

        Duration timeTaken = Duration.between(startTime, Instant.now());
        Duration totalEstimate = timeTaken.multipliedBy(multiply).dividedBy(factor);
        return totalEstimate.minus(timeTaken);
    }

    public static Optional<SolvableItemSet> findBest(ModelCombined model, Stream<SolvableItemSet> finalSets) {
        return finalSets.collect(new TopCollector1<>(model::calcRating));
//        return finalSets.max(Comparator.comparingLong(model::calcRating));
    }

    public static <T> Stream<T> randomSkipper(Stream<T> stream, int range) {
        AtomicLong countDown = new AtomicLong(ThreadLocalRandom.current().nextInt(0, range) + 1);
        return stream.filter(item -> {
            if (countDown.decrementAndGet() == 0) {
                countDown.setRelease(ThreadLocalRandom.current().nextInt(0, range) + 1);
                return true;
            } else {
                return false;
            }
        });
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
