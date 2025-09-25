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

    private static final long runSizeMultiply = 4;
//    private static final long runSizeMultiply = 1;

    public FindUpgrades(ModelCombined model, boolean hackAllow) {
        this.model = model;
        this.hackAllow = hackAllow;
    }

    public void run(EquipOptionsMap baseItems, CostedItem[] extraItemArray, StatBlock adjustment, int upgradeLevel) {
        double baseRating = findBase(baseItems, adjustment);

        Function<ItemData, ItemData> enchanting = x -> ItemUtil.defaultEnchants(x, model, true);

        List<JobInfo> jobList =
                makeJobs(model, baseItems, extraItemArray, upgradeLevel, enchanting, adjustment, baseRating)
                .toList().parallelStream() // helps verify
                .map(Solver::runJob)
                .peek(job -> handleResult(job, baseRating))
                .toList();

        reportResults(jobList);
    }

    private double findBase(EquipOptionsMap baseItems, StatBlock adjustment) {
        JobInfo job = new JobInfo();
        job.model = model;
        job.itemOptions = baseItems;
        job.runSizeMultiply = runSizeMultiply;
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

    private Stream<JobInfo> makeJobs(ModelCombined model, EquipOptionsMap baseItems, CostedItem[] extraItemArray, int upgradeLevel, Function<ItemData, ItemData> enchanting, StatBlock adjustment, double baseRating) {
        return ArrayUtil.arrayStream(extraItemArray).mapMulti((extraItemInfo, submitJob) -> {
            int extraItemId = extraItemInfo.itemId();
            int cost = extraItemInfo.cost();
            ItemData extraItem = ItemUtil.loadItemBasic(extraItemId, upgradeLevel);

            for (SlotEquip slot : extraItem.slot.toSlotEquipOptions()) {
                if (canPerformSpecifiedUpgrade(extraItem, slot, baseItems)) {
                    OutputText.printf("JOB %s\n", extraItem.toStringExtended());
                    submitJob.accept(buildUpgradeJob(model, baseItems.deepClone(), extraItem, enchanting, adjustment, slot, baseRating, cost));
                }
            }
        });
    }

    private static void reportItem(JobInfo resultItem) {
        ItemData item = resultItem.extraItem;
        double factor = resultItem.factor;
        int cost = resultItem.cost;
        String stars = ArrayUtil.repeat('*', resultItem.hackCount);
        double plusPercent = (factor - 1.0) * 100;
        if (plusPercent > 0.0) {
            if (cost >= 10) {
                double plusPerCost = plusPercent / cost;
                OutputText.printf("%10s \t%d \t%35s \t$%d \t+%2.2f%% %s\t %1.4f\n", item.slot, item.ref.itemLevel(), item.name, cost, plusPercent, stars, plusPerCost);
            } else {
                OutputText.printf("%10s \t%d \t%35s \t$%d \t+%2.2f%% %s\n", item.slot, item.ref.itemLevel(), item.name, cost, plusPercent, stars);
            }
        } else {
            OutputText.printf("%10s \t%d \t%35s \t$%d \t%2.2f%% %s\n", item.slot, item.ref.itemLevel(), item.name, cost, plusPercent, stars);
        }
    }

    private JobInfo buildUpgradeJob(ModelCombined model, EquipOptionsMap items, ItemData extraItem, Function<ItemData, ItemData> enchanting, StatBlock adjustment, SlotEquip slot, double baseRating, int cost) {
        JobInfo job = new JobInfo();
//        job.singleThread = true;

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

        job.config(model, items, null, adjustment);
        job.runSizeMultiply = runSizeMultiply;
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

    private boolean canPerformSpecifiedUpgrade(ItemData extraItem, SlotEquip slot, EquipOptionsMap reforgedItems) {
        if (SourcesOfItems.ignoredItems.contains(extraItem.ref.itemId()))
            return false;

        if (reforgedItems.get(slot) == null) {
            OutputText.println("SLOT NOT USED IN CURRENT SET " + extraItem.toStringExtended());
            return false;
        }

        if (slot == SlotEquip.Weapon) {
            ItemData exampleWeapon = reforgedItems.get(SlotEquip.Weapon)[0];
            if (extraItem.slot != exampleWeapon.slot) {
                OutputText.println("WRONG WEAPON TYPE " + extraItem.toStringExtended());
                return false;
            }
        }

        SlotEquip pairedSlot = slot.pairedSlot();
        if (reforgedItems.get(slot)[0].ref.equalsTyped(extraItem.ref) ||
                (pairedSlot != null && reforgedItems.get(pairedSlot)[0].ref.equalsTyped(extraItem.ref))) {
            OutputText.println("SAME ITEM " + extraItem.toStringExtended());
            return false;
        }

        if (pairedSlot != null && reforgedItems.get(pairedSlot)[0].ref.itemId() == extraItem.ref.itemId()) {
            OutputText.println("SAME ITEM ID IN OTHER SLOT " + extraItem.toStringExtended());
            return false;
        }

        return true;
    }

}
