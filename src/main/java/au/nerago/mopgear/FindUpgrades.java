package au.nerago.mopgear;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.io.ItemCache;
import au.nerago.mopgear.io.SourcesOfItems;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.results.JobInfo;
import au.nerago.mopgear.results.OutputText;
import au.nerago.mopgear.util.RankedGroupsCollection;
import au.nerago.mopgear.util.ArrayUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"SameParameterValue", "unused"})
public class FindUpgrades {
    private final ModelCombined model;
    private final boolean hackAllow;
    private final ItemCache itemCache;

    private static final Long runSize = null; // full search
//    private static final long runSize = 100000; // dummy
//    private static final long runSize = 10000000; // <1 min runs
//    private static final long runSize = 50000000; // 4 min total runs
//    private static final long runSize = 100000000; // 10 min total runs
//    private static final long runSize = 300000000; // 25 min total runs
//    private static final long runSize = 1000000000; // 60 min runs

    public FindUpgrades(ItemCache itemCache, ModelCombined model, boolean hackAllow) {
        this.itemCache = itemCache;
        this.model = model;
        this.hackAllow = hackAllow;
    }

    public void run(EquipOptionsMap baseItems, CostedItem[] extraItemArray, StatBlock adjustment) {
        double baseRating = findBase(baseItems, adjustment);

        Function<ItemData, ItemData> enchanting = x -> ItemUtil.defaultEnchants(x, model, true);

        List<JobInfo> jobList =
                makeJobs(model, baseItems, extraItemArray, enchanting, adjustment, baseRating)
                .map(Solver::runJob)
                .peek(job -> handleResult(job, baseRating))
                .toList();

        reportResults(jobList);
    }

    private double findBase(EquipOptionsMap baseItems, StatBlock adjustment) {
        JobInfo job = new JobInfo();
        job.model = model;
        job.itemOptions = baseItems;
        job.runSize = runSize;
        job.adjustment = adjustment;
        job.hackAllow = hackAllow;
        Solver.runJob(job);
        job.printRecorder.outputNow();

        Optional<ItemSet> baseSet = job.resultSet;
        if (baseSet.isPresent()) {
            double baseRating = model.calcRating(baseSet.get());
            OutputText.printf("\n%s\nBASE RATING    = %.0f\n\n", baseSet.get().totals, baseRating);

            return baseRating;
        } else {
            throw new IllegalStateException("couldn't find valid baseline set");
        }
    }

    private void reportResults(List<JobInfo> jobList) {
        reportByCost(jobList);
        reportBySlot(jobList);
        reportCostUpgradeRank(jobList);
        reportOverallRank(jobList);
    }

    private void reportCostUpgradeRank(List<JobInfo> jobList) {
        RankedGroupsCollection<JobInfo> grouped = new RankedGroupsCollection<>();

        jobList.forEach(job -> {
            ItemData item = job.extraItem;
            int cost = job.cost;
            if (cost >= 10) {
                double plusPerCost = (job.factor - 1.0) * 100 / cost;
                grouped.add(job, plusPerCost);
            }
        });

        OutputText.println("RANKING COST PER UPGRADE");
        grouped.forEach((item, factor) -> reportItem(item));
        OutputText.println();
    }

    private static void reportOverallRank(List<JobInfo> jobList) {
        RankedGroupsCollection<JobInfo> bestCollection = new RankedGroupsCollection<>();
        jobList.forEach(job -> bestCollection.add(job, job.factor));

        OutputText.println("RANKING PERCENT UPGRADE");
        bestCollection.forEach((item, factor) -> reportItem(item));
    }

    private void reportByCost(List<JobInfo> jobList) {
        Map<Integer, RankedGroupsCollection<JobInfo>> grouped = jobList.stream().collect(
                Collectors.groupingBy(j -> j.cost,
                        RankedGroupsCollection.collector(job -> job.factor)));
        for (Integer cost : grouped.keySet().stream().sorted(Comparator.naturalOrder()).toList()) {
            RankedGroupsCollection<JobInfo> best = grouped.get(cost);
            best.forEach((item, factor) -> reportItem(item));
            OutputText.println();
        }
    }

    private static void reportBySlot(List<JobInfo> jobList) {
        Map<SlotItem, RankedGroupsCollection<JobInfo>> grouped = jobList.stream().collect(
                Collectors.groupingBy(j -> j.extraItem.slot,
                        RankedGroupsCollection.collector(job -> job.factor)));
        for (SlotItem slot : SlotItem.values()) {
            RankedGroupsCollection<JobInfo> best = grouped.get(slot);
            if (best != null) {
                OutputText.println("RANKING " + slot);
                best.forEach((item, factor) -> reportItem(item));
                OutputText.println();
            }
        }
    }

    private Stream<JobInfo> makeJobs(ModelCombined model, EquipOptionsMap baseItems, CostedItem[] extraItemArray, Function<ItemData, ItemData> enchanting, StatBlock adjustment, double baseRating) {
        return ArrayUtil.arrayStream(extraItemArray).mapMulti((extraItemInfo, submitJob) -> {
            int extraItemId = extraItemInfo.itemId();
            int cost = extraItemInfo.cost();
            ItemData extraItem = ItemUtil.loadItemBasic(itemCache, extraItemId);
            SlotEquip slot = extraItem.slot.toSlotEquip();

            if (!canSkipUpgradeCheck(extraItem, slot, baseItems)) {
                OutputText.printf("JOB %s\n\n", extraItem.toStringExtended());

                submitJob.accept(checkForUpgrade(model, baseItems.deepClone(), extraItem, enchanting, adjustment, slot, baseRating, cost));

                if (slot == SlotEquip.Trinket1) {
                    submitJob.accept(checkForUpgrade(model, baseItems.deepClone(), extraItem, enchanting, adjustment, SlotEquip.Trinket2, baseRating, cost));
                }
                if (slot == SlotEquip.Ring1) {
                    submitJob.accept(checkForUpgrade(model, baseItems.deepClone(), extraItem, enchanting, adjustment, SlotEquip.Ring2, baseRating, cost));
                }
            }
        });
    }

    private static void reportItem(JobInfo resultItem) {
        ItemData item = resultItem.extraItem;
        double factor = resultItem.factor;
        int cost = resultItem.cost;
        String stars = ArrayUtil.repeat('*', resultItem.hackCount);
        if (factor > 1.0) {
            double plusPercent = (factor - 1.0) * 100;
            if (cost >= 10) {
                double plusPerCost = plusPercent / cost;
                OutputText.printf("%10s \t%d \t%35s \t$%d \t%1.3f%s \t+%2.1f%%\t %1.4f\n", item.slot, item.itemLevel, item.name, cost, factor, stars, plusPercent, plusPerCost);
            } else {
                OutputText.printf("%10s \t%d \t%35s \t$%d \t%1.3f%s \t+%2.1f%%\n", item.slot, item.itemLevel, item.name, cost, factor, stars, plusPercent);
            }
        } else {
            OutputText.printf("%10s \t%d \t%35s \t$%d \t%1.3f%s\n", item.slot, item.itemLevel, item.name, cost, factor, stars);
        }
    }

    private JobInfo checkForUpgrade(ModelCombined model, EquipOptionsMap items, ItemData extraItem, Function<ItemData, ItemData> enchanting, StatBlock adjustment, SlotEquip slot, double baseRating, int cost) {
        JobInfo job = new JobInfo();
        job.singleThread = true;

        extraItem = enchanting.apply(extraItem);
        job.println("OFFER " + extraItem.toStringExtended());
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
        job.cost = cost;
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
