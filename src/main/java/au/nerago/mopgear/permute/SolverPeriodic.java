package au.nerago.mopgear.permute;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.util.*;

import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.IntConsumer;
import java.util.function.Predicate;

public class SolverPeriodic {
    private final static int threadCount = 8;

    public static Optional<SolvableItemSet> runSolver(ModelCombined model, SolvableEquipOptionsMap itemOptions, StatBlock adjustment, Instant startTime, long desiredRunSize, BigInteger estimateFullCombos, Predicate<SolvableItemSet> specialFilter) {
        ConcurrentLinkedQueue<BestHolder<SolvableItemSet>> resultQueue = new ConcurrentLinkedQueue<>();
        long eachThreadCount = desiredRunSize / threadCount;
        List<LongHolder> counters = ArrayUtil.makeListFromFunc(threadCount, LongHolder::new);
        int[][] slotIndexBags = makeSlotIndexBags(itemOptions);

        Runnable stopTimer = BigStreamUtil.watchProgress(desiredRunSize, startTime, counters);
        runThreads(threadNum -> {
            workerFunc(resultQueue, model, eachThreadCount, itemOptions, slotIndexBags, threadNum, counters, specialFilter, adjustment);
        });
        stopTimer.run();

        return finalBest(resultQueue);
    }


    private static void workerFunc(ConcurrentLinkedQueue<BestHolder<SolvableItemSet>> resultQueue, ModelCombined model, long eachThreadCount, SolvableEquipOptionsMap itemOptions, int[][] slotIndexBags, int threadNum, List<LongHolder> counters, Predicate<SolvableItemSet> specialFilter, StatBlock adjustment) {
        BestHolder<SolvableItemSet> best = new BestHolder<>();

        int[] indexes = new int[16];
        for (int i = 0; i < 16; ++i) {
            int[] slotArray = slotIndexBags[i];
            if (slotArray != null) {
                indexes[i] = Math.toIntExact((slotArray.length - 1 + (threadNum * eachThreadCount)) % slotArray.length);
            }
        }

        for (int loop = 0; loop < eachThreadCount; ++loop) {
            SolvableItemSet itemSet = makeSetFromArrays(itemOptions, indexes, slotIndexBags, adjustment);
            if (model.filterOneSet(itemSet) && (specialFilter == null || specialFilter.test(itemSet))) {
                long rating = model.calcRating(itemSet);
                best.add(itemSet, rating);
            }
        }

        resultQueue.add(best);
    }

    private static SolvableItemSet makeSetFromArrays(SolvableEquipOptionsMap itemOptions, int[] slotIndexes, int[][] slotIndexBags, StatBlock adjustment) {
        SolvableEquipMap equip = SolvableEquipMap.empty();

        SlotEquip[] slotValues = SlotEquip.values();
        for (int slot = 0; slot < slotValues.length; ++slot) {
            SlotEquip slotValue = slotValues[slot];
            SolvableItem[] options = itemOptions.get(slotValue);
            int[] bag = slotIndexBags[slot];
            if (bag != null && bag.length == 1) {
                equip.put(slotValue, options[0]);
            } else if (bag != null && bag.length > 1) {
                int outerIndex = slotIndexes[slot];
                int innerIndex = bag[outerIndex];
                slotIndexes[slot] = (outerIndex + 1) % bag.length;

                equip.put(slotValue, options[innerIndex]);
            }
        }
        return SolvableItemSet.manyItems(equip, adjustment);
    }

    private static int[][] makeSlotIndexBags(SolvableEquipOptionsMap itemOptions) {
        int[][] result = new int[16][];

        int biggestSlot = itemOptions.entryStream().map(Tuple.Tuple2::b).mapToInt(array -> array.length).max().orElseThrow();
        int[] primeArray = Primes.smallPrimes(biggestSlot);
        int primeIndex = 0;

        SlotEquip[] slotValues = SlotEquip.values();
        for (int i = 0; i < slotValues.length; ++i) {
            SlotEquip slot = slotValues[i];
            SolvableItem[] slotOptions = itemOptions.get(slot);
            if (slotOptions != null) {
                int slotSize = slotOptions.length;
                if (slotSize == 1) {
                    result[i] = new int[]{0};
                } else if (slotSize > 1) {
                    int period = primeArray[primeIndex++];
                    result[i] = makeSlotBagCycling(slotSize, period);
                }
            }
        }

        return result;
    }

    private static int[] makeSlotBagCycling(int slotSize, int period) {
        int[] bag = new int[period];
        for (int i = 0; i < period; ++i) {
            bag[i] = i % slotSize;
        }
        return bag;
    }

    private static void runThreads(IntConsumer threadFunc) {
        List<Thread> threadList = new ArrayList<>();
        for (int i = 0; i < SolverPeriodic.threadCount; ++i) {
            int finalThreadNum = i;
            Thread thread = new Thread(() -> threadFunc.accept(finalThreadNum));
            thread.start();
            threadList.add(thread);
        }

        try {
            for (Thread thread : threadList) {
                    thread.join();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static Optional<SolvableItemSet> finalBest(ConcurrentLinkedQueue<BestHolder<SolvableItemSet>> resultQueue) {
        BestHolder<SolvableItemSet> best = new BestHolder<>();
        while (!resultQueue.isEmpty()) {
            BestHolder<SolvableItemSet> threadResult = resultQueue.poll();
            best.combine(threadResult);
        }
        return best.getOptional();
    }
}
