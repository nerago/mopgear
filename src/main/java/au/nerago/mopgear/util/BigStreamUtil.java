package au.nerago.mopgear.util;

import au.nerago.mopgear.domain.ItemSet;
import au.nerago.mopgear.model.ModelCombined;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class BigStreamUtil {
    public static <T> Stream<T> countProgress(long estimate, Instant startTime, Stream<T> inputStream) {
        final double percentMultiply = 100.0 / estimate;
        final long reportFrequency = chooseReportFrequency(estimate);
        return coreCount(reportFrequency, percentMultiply, startTime, inputStream);
    }

    public static <T> Stream<T> countProgressSmall(long estimate, Instant startTime, Stream<T> inputStream) {
        final double percentMultiply = 100.0 / estimate;
        final long reportFrequency = 1000;
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

    private static long chooseReportFrequency(long estimate) {
        long freq = Math.round(estimate / 100.0);
        int digitCount = 0;
        while (freq >= 10) {
            freq /= 10;
            digitCount++;
        }
        for (int i = 0; i < digitCount; ++i) {
            freq *= 10;
        }
        freq = Math.max(freq, 1000000);
        freq = Math.min(freq, 100000000);
        return freq;
    }

    public static Duration estimateRemain(Instant startTime, double percent) {
        final int factor = 100;

        long multiply = (long) (factor * 100 / percent);

        Duration timeTaken = Duration.between(startTime, Instant.now());
        Duration totalEstimate = timeTaken.multipliedBy(multiply).dividedBy(factor);
        return totalEstimate.minus(timeTaken);
    }

    public static Optional<ItemSet> findBest(ModelCombined model, Stream<ItemSet> finalSets) {
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

//    <T> Stream<T> dropWhile(Predicate<? super T> predicate) {
//        Objects.requireNonNull(predicate);
//        // Reuses the unordered spliterator, which, when encounter is present,
//        // is safe to use as long as it configured not to split
//        return StreamSupport.stream(
//                new WhileOps.UnorderedWhileSpliterator.OfRef.Dropping<>(spliterator(), true, predicate),
//                isParallel()).onClose(this::close);
//    }
}
