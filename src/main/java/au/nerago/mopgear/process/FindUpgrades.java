package au.nerago.mopgear.process;

import au.nerago.mopgear.ItemLoadUtil;
import au.nerago.mopgear.ItemMapUtil;
import au.nerago.mopgear.Solver;
import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.io.SourcesOfItems;
import au.nerago.mopgear.model.ItemLevel;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.results.JobInput;
import au.nerago.mopgear.results.JobOutput;
import au.nerago.mopgear.results.OutputText;
import au.nerago.mopgear.results.UpgradeResultItem;
import au.nerago.mopgear.util.ArrayUtil;
import au.nerago.mopgear.util.RankedGroupsCollection;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"SameParameterValue", "unused"})
public class FindUpgrades {
    private final ModelCombined model;
    private final boolean hackAllow;

    private static final long runSizeMultiply = 2;
//    private static final long runSizeMultiply = 1;

    public FindUpgrades(ModelCombined model, boolean hackAllow) {
        this.model = model;
        this.hackAllow = hackAllow;
    }

    public void run(EquipOptionsMap baseItems, List<EquippedItem> extraItems, StatBlock adjustment) {
        List<CostedItemData> extraItemList = extraItems.stream()
                .map(ei -> new CostedItemData(ItemLoadUtil.loadItem(ei, false), 0))
                .toList();
        runMain(baseItems, extraItemList, adjustment);
    }

    public void run(EquipOptionsMap baseItems, CostedItem[] extraItemArray, StatBlock adjustment, int upgradeLevel) {
        List<CostedItemData> extraItemList = Arrays.stream(extraItemArray)
                .map(ci -> new CostedItemData(ItemLoadUtil.loadItemBasic(ci.itemId(), upgradeLevel), ci.cost()))
                .toList();
        runMain(baseItems, extraItemList, adjustment);
    }

    public void runMaxedItems(EquipOptionsMap baseItems, List<EquippedItem> extraItems, StatBlock adjustment) {
        baseItems = ItemMapUtil.upgradeAllTo2(baseItems);
        List<CostedItemData> extraItemList = extraItems.stream()
                .map(ei -> new EquippedItem(ei.itemId(), ei.gems(), ei.enchant(), ItemLevel.MAX_UPGRADE_LEVEL))
                .map(ei -> new CostedItemData(ItemLoadUtil.loadItem(ei, false), 0))
                .toList();
        runMain(baseItems, extraItemList, adjustment);
    }

    public void runMaxedItems(EquipOptionsMap baseItems, CostedItem[] extraItemArray, StatBlock adjustment) {
        baseItems = ItemMapUtil.upgradeAllTo2(baseItems);
        List<CostedItemData> extraItemList = Arrays.stream(extraItemArray)
                .map(ci -> new CostedItemData(ItemLoadUtil.loadItemBasic(ci.itemId(), ItemLevel.MAX_UPGRADE_LEVEL), ci.cost()))
                .toList();
        runMain(baseItems, extraItemList, adjustment);
    }

    public void runMain(EquipOptionsMap baseItems, List<CostedItemData> extraItemList, StatBlock adjustment) {
        double baseRating = findBase(baseItems, adjustment);

        List<UpgradeResultItem> jobList =
                makeJobs(model, baseItems, extraItemList, adjustment, baseRating)
                .toList().parallelStream() // helps verify
                .map(Solver::runJob)
                .map(job -> handleResult(job, baseRating))
                .toList();

        reportResults(jobList);
    }

    private double findBase(EquipOptionsMap baseItems, StatBlock adjustment) {
        JobInput job = new JobInput();
        job.model = model;
        job.itemOptions = baseItems;
        job.runSizeMultiply = runSizeMultiply;
        job.adjustment = adjustment;
        job.hackAllow = hackAllow;
        JobOutput output = Solver.runJob(job);
        job.printRecorder.outputNow();

        Optional<ItemSet> baseSet = output.resultSet;
        if (baseSet.isPresent()) {
            double baseRating = output.resultRating;
            OutputText.printf("\n%s\nBASE RATING    = %.0f\n\n", baseSet.get().totals, baseRating);

            return baseRating;
        } else {
            throw new IllegalStateException("couldn't find valid baseline set");
        }
    }

    private void reportResults(List<UpgradeResultItem> jobList) {
        reportByCost(jobList);
        reportBySlot(jobList);
        reportCostUpgradeRank(jobList);
        reportOverallRank(jobList);
    }

    private void reportCostUpgradeRank(List<UpgradeResultItem> jobList) {
        RankedGroupsCollection<UpgradeResultItem> grouped = new RankedGroupsCollection<>();

        jobList.forEach(resultItem -> {
            ItemData item = resultItem.item();
            int cost = resultItem.cost();
            if (cost >= 10) {
                double plusPerCost = (resultItem.factor() - 1.0) * 100 / cost;
                grouped.add(resultItem, plusPerCost);
            }
        });

        OutputText.println("RANKING COST PER UPGRADE");
        grouped.forEach((item, factor) -> reportItem(item));
        OutputText.println();
    }

    private static void reportOverallRank(List<UpgradeResultItem> jobList) {
        RankedGroupsCollection<UpgradeResultItem> bestCollection = new RankedGroupsCollection<>();
        jobList.forEach(job -> bestCollection.add(job, job.factor()));

        OutputText.println("RANKING PERCENT UPGRADE");
        bestCollection.forEach((item, factor) -> reportItem(item));
    }

    private void reportByCost(List<UpgradeResultItem> jobList) {
        Map<Integer, RankedGroupsCollection<UpgradeResultItem>> grouped = jobList.stream().collect(
                Collectors.groupingBy(UpgradeResultItem::cost,
                        RankedGroupsCollection.collector(UpgradeResultItem::factor)));
        for (Integer cost : grouped.keySet().stream().sorted(Comparator.naturalOrder()).toList()) {
            RankedGroupsCollection<UpgradeResultItem> best = grouped.get(cost);
            best.forEach((item, factor) -> reportItem(item));
            OutputText.println();
        }
    }

    private static void reportBySlot(List<UpgradeResultItem> jobList) {
        Map<SlotItem, RankedGroupsCollection<UpgradeResultItem>> grouped = jobList.stream().collect(
                Collectors.groupingBy(j -> j.item().slot,
                        RankedGroupsCollection.collector(UpgradeResultItem::factor)));
        for (SlotItem slot : SlotItem.values()) {
            RankedGroupsCollection<UpgradeResultItem> best = grouped.get(slot);
            if (best != null) {
                OutputText.println("RANKING " + slot);
                best.forEach((item, factor) -> reportItem(item));
                OutputText.println();
            }
        }
    }

    private Stream<JobInput> makeJobs(ModelCombined model, EquipOptionsMap baseItems, List<CostedItemData> extraItemList, StatBlock adjustment, double baseRating) {
        return extraItemList.parallelStream().mapMulti((extraItemInfo, submitJob) -> {
            ItemData extraItem = extraItemInfo.item();
            int cost = extraItemInfo.cost();

            for (SlotEquip slot : extraItem.slot.toSlotEquipOptions()) {
                if (canPerformSpecifiedUpgrade(extraItem, slot, baseItems)) {
                    OutputText.printf("JOB %s\n", extraItem.toStringExtended());
                    submitJob.accept(buildUpgradeJob(model, baseItems.deepClone(), extraItem, adjustment, slot, baseRating, cost));
                }
            }
        });
    }

    private static void reportItem(UpgradeResultItem resultItem) {
        ItemData item = resultItem.item();
        double factor = resultItem.factor();
        int cost = resultItem.cost();
        String stars = ArrayUtil.repeat('*', resultItem.hackCount());
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

    private JobInput buildUpgradeJob(ModelCombined model, EquipOptionsMap items, ItemData extraItem, StatBlock adjustment, SlotEquip slot, double baseRating, int cost) {
        JobInput job = new JobInput();
//        job.singleThread = true;

        extraItem = ItemLoadUtil.defaultEnchants(extraItem, model, true);
        job.println("OFFER " + extraItem.toStringExtended());
        job.println("REPLACING " + (items.get(slot) != null ? items.get(slot)[0].toStringExtended() : "NOTHING"));

        ItemData[] extraOptions = Reforger.reforgeItem(model.reforgeRules(), extraItem);
        items.put(slot, extraOptions);
        ArrayUtil.mapInPlace(items.get(slot), x -> ItemLoadUtil.defaultEnchants(x, model, true)); // redundant?

        if (hackAllow) {
//            adjustment = FindStatRange.checkSetAdjust(model, items, job.printRecorder);
//            if (adjustment != null)
//                job.hackCount++;
            job.hackAllow = true;
        }

        job.config(model, items, null, adjustment);
        job.runSizeMultiply = runSizeMultiply;
        job.extraItem = extraItem;
        job.cost = cost;
        return job;
    }

    private UpgradeResultItem handleResult(JobOutput job, double baseRating) {
        Optional<ItemSet> resultSet = job.resultSet;
        job.input.printRecorder.outputNow();

        double factor;
        if (resultSet.isPresent()) {
            OutputText.println("SET STATS " + resultSet.get().totals);
            double extraRating = job.resultRating;
            factor = extraRating / baseRating;
            OutputText.printf("UPGRADE RATING = %.0f FACTOR = %1.3f\n", extraRating, factor);
        } else {
            factor = 0;
            OutputText.println("UPGRADE SET NOT FOUND");
        }
        OutputText.println();
        return new UpgradeResultItem(job.input.extraItem, factor, job.hackCount, job.input.cost);
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
