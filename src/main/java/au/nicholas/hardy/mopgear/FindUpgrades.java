package au.nicholas.hardy.mopgear;

import au.nicholas.hardy.mopgear.domain.*;
import au.nicholas.hardy.mopgear.io.ItemCache;
import au.nicholas.hardy.mopgear.io.SourcesOfItems;
import au.nicholas.hardy.mopgear.model.ModelCombined;
import au.nicholas.hardy.mopgear.results.JobInfo;
import au.nicholas.hardy.mopgear.results.OutputText;
import au.nicholas.hardy.mopgear.util.ArrayUtil;
import au.nicholas.hardy.mopgear.util.BestCollection;
import au.nicholas.hardy.mopgear.util.Tuple;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"SameParameterValue", "unused", "OptionalUsedAsFieldOrParameterType"})
public class FindUpgrades {
    private final ModelCombined model;
    private boolean hackAllow;
    private ItemCache itemCache;

    //    private static final Long runSize = null; // full search
//    private static final long runSize = 10000000; // <1 min runs
    private static final long runSize = 50000000; // 4 min total runs
//    private static final long runSize = 100000000; // 10 min total runs
//    private static final long runSize = 300000000; // 25 min total runs
//    private static final long runSize = 1000000000; // 60 min runs

    public FindUpgrades(ItemCache itemCache, ModelCombined model, boolean hackAllow) {
        this.itemCache = itemCache;
        this.model = model;
        this.hackAllow = hackAllow;
    }

    public void run(EquipOptionsMap baseItems, Tuple.Tuple2<Integer, Integer>[] extraItemArray, StatBlock adjustment) {
        ItemSet baseSet = SolverEntry.chooseEngineAndRun(model, baseItems, null, runSize, adjustment).orElseThrow();
        double baseRating = model.calcRating(baseSet);
        OutputText.printf("\n%s\nBASE RATING    = %.0f\n\n", baseSet.totals, baseRating);

        Function<ItemData, ItemData> enchanting = x -> ItemUtil.defaultEnchants(x, model, true);

        List<JobInfo> jobList =
                makeJobs(model, baseItems, extraItemArray, enchanting, adjustment, baseRating)
                .map(SolverEntry::runJob)
                .peek(job -> handleResult(job, baseRating))
                .toList();

        reportResults(extraItemArray, jobList);
    }

    private void reportResults(Tuple.Tuple2<Integer, Integer>[] extraItemArray, List<JobInfo> jobList) {
        reportBySlot(extraItemArray, jobList);
        reportCostUpgradeRank(extraItemArray, jobList);
        reportOverallRank(extraItemArray, jobList);
    }

    private void reportCostUpgradeRank(Tuple.Tuple2<Integer, Integer>[] extraItemArray, List<JobInfo> jobList) {
        BestCollection<JobInfo> bestCollection = new BestCollection<>();

        jobList.forEach(job -> {
            ItemData item = job.extraItem;
            int cost = ArrayUtil.findAny(extraItemArray, x -> x.a() == item.id).b();
            if (cost >= 10) {
                double plusPerCost = (job.factor - 1.0) * 100 / cost;
                bestCollection.add(job, plusPerCost);
            }
        });

        OutputText.println("RANKING COST PER UPGRADE");
        bestCollection.forEach((item, factor) -> reportItem(item, extraItemArray));
    }

    private static void reportOverallRank(Tuple.Tuple2<Integer, Integer>[] extraItemArray, List<JobInfo> jobList) {
        BestCollection<JobInfo> bestCollection = new BestCollection<>();
        jobList.forEach(job -> bestCollection.add(job, job.factor));

        OutputText.println("RANKING PERCENT UPGRADE");
        bestCollection.forEach((item, factor) -> reportItem(item, extraItemArray));
    }

    private static void reportBySlot(Tuple.Tuple2<Integer, Integer>[] extraItemArray, List<JobInfo> jobList) {
        Map<Object, BestCollection<JobInfo>> grouped = jobList.stream().collect(
                Collectors.groupingBy(j -> j.extraItem.slot,
                        BestCollection.collector(job -> job.factor)));
        for (SlotItem slot : SlotItem.values()) {
            BestCollection<JobInfo> best = grouped.get(slot);
            if (best != null) {
                OutputText.println("RANKING " + slot);
                best.forEach((item, factor) -> reportItem(item, extraItemArray));
                OutputText.println();
            }
        }
    }

    private Stream<JobInfo> makeJobs(ModelCombined model, EquipOptionsMap baseItems, Tuple.Tuple2<Integer, Integer>[] extraItemArray, Function<ItemData, ItemData> enchanting, StatBlock adjustment, double baseRating) {
        return ArrayUtil.arrayStream(extraItemArray).mapMulti((extraItemInfo, submitJob) -> {
            int extraItemId = extraItemInfo.a();
            ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId);
            SlotEquip slot = extraItem.slot.toSlotEquip();

            if (!canSkipUpgradeCheck(extraItem, slot, baseItems)) {
                OutputText.println("JOB " + extraItem.toStringExtended() + " $" + extraItemInfo.b());

                submitJob.accept(checkForUpgrade(model, baseItems.deepClone(), extraItem, enchanting, adjustment, slot, baseRating));

                if (slot == SlotEquip.Trinket1) {
                    submitJob.accept(checkForUpgrade(model, baseItems.deepClone(), extraItem, enchanting, adjustment, SlotEquip.Trinket2, baseRating));
                }
                if (slot == SlotEquip.Ring1) {
                    submitJob.accept(checkForUpgrade(model, baseItems.deepClone(), extraItem, enchanting, adjustment, SlotEquip.Ring2, baseRating));
                }
            }
        });
    }

    private static void reportItem(JobInfo resultItem, Tuple.Tuple2<Integer, Integer>[] extraItemArray) {
        ItemData item = resultItem.extraItem;
        double factor = resultItem.factor;
        String stars = ArrayUtil.repeat('*', resultItem.hackCount);
        int cost = ArrayUtil.findAny(extraItemArray, x -> x.a() == item.id).b();
        if (factor > 1.0) {
            double plusPercent = (factor - 1.0) * 100;
            if (cost >= 10) {
                double plusPerCost = plusPercent / cost;
                OutputText.printf("%10s \t%35s \t$%d \t%1.3f%s \t+%2.1f%%\t %1.4f\n", item.slot, item.name, cost, factor, stars, plusPercent, plusPerCost);
            } else {
                OutputText.printf("%10s \t%35s \t$%d \t%1.3f%s \t+%2.1f%%\n", item.slot, item.name, cost, factor, stars, plusPercent);
            }
        } else {
            OutputText.printf("%10s \t%35s \t$%d \t%1.3f%s\n", item.slot, item.name, cost, factor, stars);
        }
    }

    private JobInfo checkForUpgrade(ModelCombined model, EquipOptionsMap items, ItemData extraItem, Function<ItemData, ItemData> enchanting, StatBlock adjustment, SlotEquip slot, double baseRating) {
        JobInfo job = new JobInfo();
        job.singleThread = true;

        extraItem = enchanting.apply(extraItem);
        job.println("OFFER " + extraItem);
        job.println("REPLACING " + (items.get(slot) != null ? items.get(slot)[0] : "NOTHING"));

        ItemData[] extraOptions = Reforger.reforgeItem(model.reforgeRules(), extraItem);
        items.put(slot, extraOptions);
        ArrayUtil.mapInPlace(items.get(slot), enchanting); // redundant?

        if (hackAllow) {
            adjustment = FindStatRange.checkSetAdjust(model, items, job);
            if (adjustment != null)
                job.hackCount++;
            job.hackAllow = true;
        }

        job.config(model, items, null, runSize, adjustment);
        job.extraItem = extraItem;
        return job;
    }

    private void handleResult(JobInfo job, double baseRating) {
        Optional<ItemSet> resultSet = job.resultSet;
        job.printRecorder.outputNow();
        if (resultSet.isPresent()) {
            OutputText.println("SET STATS " + resultSet.get().totals);
            double extraRating = job.model.calcRating(resultSet.get());
            double factor = extraRating / baseRating;
            OutputText.printf("UPGRADE RATING = %.0f FACTOR = %1.3f\n", extraRating, factor);
            job.factor = factor;
        } else {
            OutputText.println("UPGRADE SET NOT FOUND");
            job.factor = 0;
        }
        OutputText.println();
    }

    private boolean canSkipUpgradeCheck(ItemData extraItem, SlotEquip slot, EquipOptionsMap reforgedItems) {
        if (SourcesOfItems.ignoredItems.contains(extraItem.id))
            return true;

        if (reforgedItems.get(slot) == null) {
            OutputText.println("SLOT NOT USED IN CURRENT SET " + extraItem.toStringExtended());
            return true;
        }

        SlotEquip pairedSlot = slot.pairedSlot();
        if (reforgedItems.get(slot)[0].id == extraItem.id ||
                (pairedSlot != null && reforgedItems.get(pairedSlot)[0].id == extraItem.id)) {
            OutputText.println("SAME ITEM " + extraItem.toStringExtended());
            return true;
        }

        return false;
    }

}
