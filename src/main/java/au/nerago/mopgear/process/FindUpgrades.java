package au.nerago.mopgear.process;

import au.nerago.mopgear.ItemLoadUtil;
import au.nerago.mopgear.ItemMapUtil;
import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.io.BossLookup;
import au.nerago.mopgear.io.SourcesOfItems;
import au.nerago.mopgear.model.ItemLevel;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.permute.Solver;
import au.nerago.mopgear.results.JobInput;
import au.nerago.mopgear.results.JobOutput;
import au.nerago.mopgear.results.OutputText;
import au.nerago.mopgear.results.UpgradeResultItem;
import au.nerago.mopgear.util.ArrayUtil;
import au.nerago.mopgear.util.RankedGroupsCollection;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static au.nerago.mopgear.results.JobInput.RunSizeCategory.Final;
import static au.nerago.mopgear.results.JobInput.RunSizeCategory.SubSolveItem;

@SuppressWarnings({"SameParameterValue", "unused"})
public class FindUpgrades {
    private final ModelCombined model;
    private final boolean hackAllow;

    private static long runSizeMultiply = 1;
    private static final boolean costsTraditional = false;

    public FindUpgrades(ModelCombined model, boolean hackAllow) {
        this.model = model;
        this.hackAllow = hackAllow;
    }

    public FindUpgrades setRunSizeMultiply(long multiply) {
        runSizeMultiply = multiply;
        return this;
    }

    public void run(EquipOptionsMap baseItems, List<EquippedItem> extraItems, StatBlock adjustment) {
        List<CostedItemData> extraItemList = extraItems.stream()
                .map(ei -> new CostedItemData(ItemLoadUtil.loadItem(ei, model.enchants(), false), 0))
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
                .map(ei -> new EquippedItem(ei.itemId(), ei.gems(), ei.enchant(), ItemLevel.MAX_UPGRADE_LEVEL, ei.reforging(), ei.randomSuffix()))
                .map(ei -> new CostedItemData(ItemLoadUtil.loadItem(ei, model.enchants(), false), 0))
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
        OutputText.println("FINDING BASELINE");
        double baseRating = findBase(baseItems, adjustment);

        extraItemList = checkDuplicates(extraItemList);
        if (!costsTraditional)
            extraItemList = costsToBossIds(extraItemList);

        OutputText.println("RUNNING MAIN");
        List<UpgradeResultItem> jobList =
                makeJobs(model, baseItems, extraItemList, adjustment, baseRating)
                .toList().parallelStream() // helps verify
                .map(Solver::runJob)
                .map(job -> handleResult(job, baseRating))
                .toList();

        reportResults(jobList);
    }

    private List<CostedItemData> costsToBossIds(List<CostedItemData> extraItemList) {
        return extraItemList.stream()
                .map(cid ->
                        new CostedItemData(
                                cid.item(),
                                BossLookup.bossIdForItemName(cid.item().shared.name())
                        )
                ).toList();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private List<CostedItemData> checkDuplicates(List<CostedItemData> extraItemList) {
        HashMap<Integer, CostedItemData> result = new HashMap<>();
        for (CostedItemData item : extraItemList) {
            CostedItemData current = result.get(item.item().itemId());
            // TODO consider reenabling check, should delete the old celestial version etc
//            if (result.values().stream().anyMatch(x -> x.item().shared.name().equals(item.item().shared.name()) && x.item().itemId() != item.item().itemId())) {
//                throw new RuntimeException("alternate items for " + item.item().shared.name());
//            }
            if (current == null || current.cost() == 0 || current.cost() == -1) {
                result.put(item.item().itemId(), item);
            } else if (item.cost() == 0 || item.cost() == -1) {
                // ignore
            } else if (current.cost() != item.cost()) {
                throw new RuntimeException("different costs for " + item.item());
            }
        }
        return result.values().stream().toList();
    }

    private double findBase(EquipOptionsMap baseItems, StatBlock adjustment) {
        JobInput job = new JobInput(Final, runSizeMultiply, false);
        job.model = model;
        job.setItemOptions(baseItems);
        job.adjustment = adjustment;
        job.hackAllow = hackAllow;
        JobOutput output = Solver.runJob(job);
        job.printRecorder.outputNow();

        Optional<FullItemSet> baseSet = output.getFinalResultSet();
        if (baseSet.isPresent()) {
            double baseRating = output.resultRating;
            OutputText.printf("\n%s\nBASE RATING    = %.0f\n\n", baseSet.get().totalForRating(), baseRating);

            return baseRating;
        } else {
            throw new IllegalStateException("couldn't find valid baseline set");
        }
    }

    private void reportResults(List<UpgradeResultItem> jobList) {
        reportByCost(jobList);
        reportBySlot(jobList);
        if (costsTraditional)
            reportCostUpgradeRank(jobList);
        reportOverallRank(jobList);
    }

    private void reportCostUpgradeRank(List<UpgradeResultItem> jobList) {
        RankedGroupsCollection<UpgradeResultItem> grouped = new RankedGroupsCollection<>();

        jobList.forEach(resultItem -> {
            FullItemData item = resultItem.item();
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
            OutputText.printf("COST %d %s\n", cost, BossLookup.bossNameForBossId(cost));
            best.forEach((item, factor) -> reportItem(item));
            OutputText.println();
        }
    }

    private static void reportBySlot(List<UpgradeResultItem> jobList) {
        Map<SlotItem, RankedGroupsCollection<UpgradeResultItem>> grouped = jobList.stream().collect(
                Collectors.groupingBy(j -> j.item().slot(),
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
            FullItemData extraItem = extraItemInfo.item();
            int cost = extraItemInfo.cost();

            for (SlotEquip slot : extraItem.slot().toSlotEquipOptions()) {
                if (canPerformSpecifiedUpgrade(extraItem, slot, baseItems)) {
                    OutputText.printf("JOB %s\n", extraItem.toStringExtended());
                    submitJob.accept(buildUpgradeJob(model, baseItems.deepClone(), extraItem, adjustment, slot, baseRating, cost));
                }
            }
        });
    }

    private static void reportItem(UpgradeResultItem resultItem) {
        FullItemData item = resultItem.item();
        double factor = resultItem.factor();
        int cost = resultItem.cost();
        String stars = ArrayUtil.repeat('*', resultItem.hackCount());
        double plusPercent = (factor - 1.0) * 100;
        if (costsTraditional) {
            if (plusPercent > 0.0) {
                if (cost >= 10) {
                    double plusPerCost = plusPercent / cost;
                    OutputText.printf("%10s \t%d \t%35s \t$%d \t+%2.2f%% %s\t %1.4f\n", item.slot(), item.shared.ref().itemLevel(), item.shared.name(), cost, plusPercent, stars, plusPerCost);
                } else {
                    OutputText.printf("%10s \t%d \t%35s \t$%d \t+%2.2f%% %s\n", item.slot(), item.shared.ref().itemLevel(), item.shared.name(), cost, plusPercent, stars);
                }
            } else {
                OutputText.printf("%10s \t%d \t%35s \t$%d \t%2.2f%% %s\n", item.slot(), item.shared.ref().itemLevel(), item.shared.name(), cost, plusPercent, stars);
            }
        } else {
            String boss = BossLookup.bossNameForBossId(cost);
            if (plusPercent > 0.0) {
                OutputText.printf("%10s \t%d \t%35s \t+%2.2f%% %s\t %s\n", item.slot(), item.shared.ref().itemLevel(), item.shared.name(), plusPercent, stars, boss);
            } else {
                OutputText.printf("%10s \t%d \t%35s \t%2.2f%% %s\t %s\n", item.slot(), item.shared.ref().itemLevel(), item.shared.name(), plusPercent, stars, boss);
            }
        }
    }

    private JobInput buildUpgradeJob(ModelCombined model, EquipOptionsMap items, FullItemData extraItem, StatBlock adjustment, SlotEquip slot, double baseRating, int cost) {
        JobInput job = new JobInput(SubSolveItem, runSizeMultiply, false);
        job.singleThread = true;

        extraItem = ItemLoadUtil.defaultEnchants(extraItem, model, true, false);
        job.println("OFFER " + extraItem.toStringExtended());
        job.println("REPLACING " + (items.get(slot) != null ? items.get(slot)[0].toStringExtended() : "NOTHING"));

        List<FullItemData> extraOptions = Reforger.reforgeItem(model.reforgeRules(), extraItem);
        items.put(slot, extraOptions);
        ArrayUtil.mapInPlace(items.get(slot), x -> ItemLoadUtil.defaultEnchants(x, model, true, false)); // redundant?

        if (hackAllow) {
//            adjustment = FindStatRange.checkSetAdjust(model, items, job.printRecorder);
//            if (adjustment != null)
//                job.hackCount++;
            job.hackAllow = true;
        }

        job.model = model;
        job.setItemOptions(items);
        job.startTime = null;
        job.adjustment = adjustment;
        job.extraItem = extraItem;
        job.cost = cost;
        return job;
    }

    private UpgradeResultItem handleResult(JobOutput job, double baseRating) {
        Optional<FullItemSet> resultSet = job.getFinalResultSet();
        job.input.printRecorder.outputNow();

        double factor;
        if (resultSet.isPresent()) {
            OutputText.println("SET STATS " + resultSet.get().totalForRating());
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

    private boolean canPerformSpecifiedUpgrade(FullItemData extraItem, SlotEquip slot, EquipOptionsMap reforgedItems) {
        if (SourcesOfItems.ignoredItems.contains(extraItem.itemId()))
            return false;

        if (reforgedItems.get(slot) == null) {
            OutputText.println("SLOT NOT USED IN CURRENT SET " + extraItem.toStringExtended());
            return false;
        }

        if (slot == SlotEquip.Weapon) {
            FullItemData exampleWeapon = reforgedItems.get(SlotEquip.Weapon)[0];
            if (extraItem.slot() != exampleWeapon.slot()) {
                OutputText.println("WRONG WEAPON TYPE " + extraItem.toStringExtended());
                return false;
            }
        }

        SlotEquip pairedSlot = slot.pairedSlot();
        if (reforgedItems.get(slot)[0].ref().equalsTyped(extraItem.ref()) ||
                (pairedSlot != null && reforgedItems.get(pairedSlot)[0].ref().equalsTyped(extraItem.ref()))) {
            OutputText.println("SAME ITEM " + extraItem.toStringExtended());
            return false;
        }

        if (pairedSlot != null && reforgedItems.get(pairedSlot)[0].itemId() == extraItem.itemId()) {
            OutputText.println("SAME ITEM ID IN OTHER SLOT " + extraItem.toStringExtended());
            return false;
        }

        return true;
    }
}
