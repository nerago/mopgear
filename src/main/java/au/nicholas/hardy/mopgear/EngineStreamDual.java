package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.util.BigStreamUtil;

import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Spliterator.*;

public class EngineStreamDual {

//    public static Collection<ItemSet> runSolver(ModelCombined modelRet, Map<SlotEquip, List<ItemData>> retMap, ModelCombined modelProt, Map<SlotEquip, List<ItemData>> protMap, Object o) {
//    }

//    static Collection<ItemSet> runSolver(StatRatings statRatings, StatRequirements common, Map<SlotEquip, List<ItemData>> items, Instant startTime) {
//        long estimate = estimateSets(items);
//        Stream<ItemSet> initialSets = generateItemCombinations(items);
//        if (startTime != null)
//            initialSets = BigStreamUtil.countProgress(estimate, startTime, initialSets);
//
//        Stream<ItemSet> finalSets = common.filterSets(initialSets).parallel();
//        Optional<ItemSet> opt = finalSets.max(Comparator.comparingLong(x -> statRatings.calcRating(x.totals)));
//        return opt.isPresent() ? Collections.singleton(opt.get()) : Collections.emptyList();
////        return finalSets.collect(new TopCollector1<>(20, ItemSet::getStatRating));
//    }
//
//    // NOTES: we could dig right down a path to find its max/min hit/exp limits, then know if we're on a bad path
//
//    private static long estimateSets(Map<SlotEquip, List<ItemData>> reforgedItems) {
//        return reforgedItems.values().stream().mapToLong(x -> (long) x.size()).reduce((a, b) -> a * b).orElse(0);
//    }
//
////    private static Stream<ItemSet> makeFinalisedSets(Model model, Stream<ItemSet> initialSets) {
////        return initialSets.map(x -> x.finished(model::calcRating));
////    }
//
//    private static Stream<ItemSet> generateItemCombinations(Map<SlotEquip, List<ItemData>> itemsBySlot) {
//        Stream<ItemSet> stream = null;
//        for (List<ItemData> slotItems : itemsBySlot.values()) {
//            if (stream == null) {
//                stream = newCombinationStream(slotItems);
//            } else {
//                stream = applyItemsToCombination(stream, slotItems);
//            }
//        }
//        return stream;
//    }
//
//    private static Stream<ItemSet> newCombinationStream(List<ItemData> slotItems) {
////        return slotItems.parallelStream().unordered().map(ItemSet::singleItem);
////        return slotItems.parallelStream().map(ItemSet::singleItem);
//
//        final ItemSet[] initialSets = new ItemSet[slotItems.size()];
//        for (int i = 0; i < slotItems.size(); ++i) {
//            initialSets[i] = ItemSet.singleItem(slotItems.get(i));
//        }
//        final Spliterator<ItemSet> split = Spliterators.spliterator(initialSets, SIZED | SUBSIZED | ORDERED | DISTINCT | NONNULL | IMMUTABLE);
//        return StreamSupport.stream(split, true);
//    }
//
//    private static Stream<ItemSet> applyItemsToCombination(Stream<ItemSet> stream, List<ItemData> slotItems) {
//        Stream<ItemSet> n = stream.mapMulti((set, sink) -> {
//            for (ItemData add : slotItems) {
//                sink.accept(set.copyWithAddedItem(add));
//            }
//        });
//        return n.parallel();
//    }
//
////    static Stream<ItemSet> listStream(final List<ItemData> addItems) {
////        final ItemData[] array = addItems.toArray(new ItemData[0]);
////        final Spliterator<ItemData> split = Spliterators.spliterator(array, SIZED | SUBSIZED | ORDERED | DISTINCT | NONNULL | IMMUTABLE);
////        return StreamSupport.stream(split, true);
////    }
}
