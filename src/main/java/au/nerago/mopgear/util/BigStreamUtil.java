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
        AtomicLong count = new AtomicLong();
        return inputStream.peek(set -> {
            long curr = count.incrementAndGet();
            if (curr % reportFrequency == 0) {
                double percent = ((double) curr) * percentMultiply;
                synchronized (System.out) {
                    System.out.print(curr);
                    System.out.print(" ");
                    System.out.printf("%.2f", percent);
                    Duration estimateRemain = estimateRemain(startTime, percent);
                    System.out.print(" ");
                    System.out.print(estimateRemain.toString());
                    System.out.println();
                }
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
        if (estimate.compareTo(BigInteger.valueOf(Long.MAX_VALUE / 2)) > 0) {
            return chooseReportFrequency(Long.MAX_VALUE / 2, 100, 100000000);
        } else {
            return chooseReportFrequency(estimate.longValueExact(), 100, 100000000);
        }
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

    public static long estimateSets(List<SkinnyItem[]> skinnyOptions) {
        return skinnyOptions.stream().mapToLong(x -> (long) x.length).reduce((a, b) -> a * b).orElse(0);
    }

    public static <X, T> long estimateSets(Map<X, List<T>> commonMap) {
        return commonMap.values().stream().mapToLong(x -> (long) x.size()).reduce((a, b) -> a * b).orElse(0);
    }
}
