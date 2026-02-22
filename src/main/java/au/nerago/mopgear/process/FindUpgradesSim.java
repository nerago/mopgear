package au.nerago.mopgear.process;

import au.nerago.mopgear.ItemLoadUtil;
import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.io.*;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.permute.Solver;
import au.nerago.mopgear.results.*;
import au.nerago.mopgear.util.ArrayUtil;
import au.nerago.mopgear.util.Tuple;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static au.nerago.mopgear.results.JobInput.RunSizeCategory.*;

@SuppressWarnings({"SameParameterValue", "unused"})
public class FindUpgradesSim {
    private final ModelCombined model;
    private final PrintRecorder printer = PrintRecorder.withAutoOutput();
    private final long runSizeMultiply = 1;
    private final JobInput.RunSizeCategory runCategory = Medium;
    private final Map<Integer, ReforgeRecipe> fixedForge = new HashMap<>();

    public FindUpgradesSim(ModelCombined model) {
        this.model = model;
    }

    public FindUpgradesSim addFixedForge(int id, ReforgeRecipe reforge) {
        fixedForge.put(id, reforge);
        return this;
    }

    public void run(EquipOptionsMap baseItems, int[] extraItemArray, int upgradeLevel) {
        List<FullItemData> extraItemList = Arrays.stream(extraItemArray)
                .mapToObj(id -> ItemLoadUtil.loadItemBasic(id, upgradeLevel, printer))
                .toList();
        runMain(baseItems, extraItemList);
    }

    public void run(EquipOptionsMap baseItems, CostedItem[] costedItems, int upgradeLevel) {
        List<FullItemData> extraItemList = Arrays.stream(costedItems)
            .map(ci -> ItemLoadUtil.loadItemBasic(ci.itemId(), upgradeLevel, printer))
            .toList();
        runMain(baseItems, extraItemList);
    }

    private void runMain(EquipOptionsMap baseItems, List<FullItemData> extraItemList) {
        OutputText.println("FINDING BASELINE");
        SimOutputReader.SimResultStats baseStats = findBase(baseItems);

        OutputText.println("RUNNING MAIN");
        List<UpgradeResultSimItem> resultList =
                makeJobs(model, baseItems, extraItemList)
                .toList().parallelStream() // helps verify
                .map(Solver::runJob)
                .map(this::handleBasicSolveResult)
                .filter(Objects::nonNull)
                .map(x -> runOneSim(x.a(), x.b(), x.c(), x.d()))
                .toList();

        reportResults(baseStats, resultList);
    }

//    private List<CostedItemData> costsToBossIds(List<CostedItemData> extraItemList) {
//        return extraItemList.stream()
//                .map(cid ->
//                        new CostedItemData(
//                                cid.item(),
//                                BossLookup.bossIdForItemName(cid.item().shared.name())
//                        )
//                ).toList();
//    }

    private SimOutputReader.SimResultStats findBase(EquipOptionsMap baseItems) {
        JobInput job = new JobInput(runCategory, runSizeMultiply, false);
        job.model = model;
        job.setItemOptions(baseItems);
        job.hackAllow = false;
        JobOutput output = Solver.runJob(job);
        job.printRecorder.outputNow();

        Optional<FullItemSet> baseSet = output.getFinalResultSet();
        if (baseSet.isPresent()) {
            return runOneSim(model.spec(), baseSet.get());
        } else {
            throw new IllegalStateException("couldn't find valid baseline set");
        }
    }

    private void reportResults(SimOutputReader.SimResultStats baseStats, List<UpgradeResultSimItem> resultList) {
        resultList.forEach(itemResult -> {
            OutputText.printf(">>> %s %s\n", itemResult.slot(), itemResult.item());
            OutputText.printf("%s\n", itemResult.simResult().file());
            itemResult.itemSet().outputSetDetailed(model);
            AsWowSimJson.writeFullToOut(itemResult.itemSet().items(), model);
            OutputText.printf("DPS  %s\n", formatDiff(baseStats.dps(), itemResult.simResult().dps(), true));
            OutputText.printf("TPS  %s\n", formatDiff(baseStats.tps(), itemResult.simResult().tps(), true));
            OutputText.printf("DTPS %s\n", formatDiff(baseStats.dtps(), itemResult.simResult().dtps(), false));
            OutputText.printf("HPS  %s\n", formatDiff(baseStats.hps(), itemResult.simResult().hps(), true));
            OutputText.printf("TMI  %s\n", formatDiff(baseStats.tmi(), itemResult.simResult().tmi(), false));
            OutputText.printf("DEA  %s\n", formatDiff(baseStats.death() * 100, itemResult.simResult().death() * 100, false));
            OutputText.println();
        });
    }

    private String formatDiff(double base, double changed, boolean wantHigh) {
        if (changed > base) {
            double percent = (changed - base) / base * 100.0;
            return String.format("%10.2f->%10.2f = +%.3f%% %s", base, changed, percent, wantHigh ? "better" : "worse");
        } else {
            double percent = (base - changed) / base * 100.0;
            return String.format("%10.2f->%10.2f = -%.3f%% %s", base, changed, percent, wantHigh ? "worse" : "better");
        }
    }

    private Stream<JobInput> makeJobs(ModelCombined model, EquipOptionsMap baseItems, List<FullItemData> extraItemList) {
        return extraItemList.parallelStream().mapMulti((extraItem, submitJob) -> {
            for (SlotEquip slot : extraItem.slot().toSlotEquipOptions()) {
                if (canPerformSpecifiedUpgrade(extraItem, slot, baseItems)) {
                    OutputText.printf("JOB %s\n", extraItem.toStringExtended());
                    submitJob.accept(buildUpgradeJob(model, baseItems.deepClone(), extraItem, slot));
                }
            }
        });
    }

    private UpgradeResultSimItem runOneSim(FullItemData extraItem, SlotEquip slot, SpecType spec, FullItemSet itemSet) {
        SimOutputReader.SimResultStats resultStats = runOneSim(spec, itemSet);
        return new UpgradeResultSimItem(extraItem, slot, itemSet, resultStats);
    }

    private static SimOutputReader.@NotNull SimResultStats runOneSim(SpecType spec, FullItemSet itemSet) {
        UUID taskId = UUID.randomUUID();
        Path inputFile = SimInputModify.makeWithGear(spec, itemSet.items(), taskId.toString());
        Path outputFile = inputFile.resolveSibling(inputFile.getFileName() + ".out");
        SimCliExecute.run(inputFile, outputFile);
        return SimOutputReader.readInput(outputFile);
    }

    private static void reportItem(UpgradeResultItem resultItem) {
        FullItemData item = resultItem.item();
        double factor = resultItem.factor();
        int cost = resultItem.cost();
        String stars = ArrayUtil.repeat('*', resultItem.hackCount());
        double plusPercent = (factor - 1.0) * 100;
        String boss = BossLookup.bossNameForBossId(cost);
        if (plusPercent > 0.0) {
            OutputText.printf("%10s \t%d \t%35s \t+%2.2f%% %s\t %s\n", item.slot(), item.shared.ref().itemLevel(), item.shared.name(), plusPercent, stars, boss);
        } else {
            OutputText.printf("%10s \t%d \t%35s \t%2.2f%% %s\t %s\n", item.slot(), item.shared.ref().itemLevel(), item.shared.name(), plusPercent, stars, boss);
        }
    }

    private JobInput buildUpgradeJob(ModelCombined model, EquipOptionsMap items, FullItemData extraItem, SlotEquip slot) {
        JobInput job = new JobInput(runCategory, runSizeMultiply, false);

        extraItem = ItemLoadUtil.defaultEnchants(extraItem, model, true, false);
        job.println("OFFER " + extraItem.toStringExtended());
        job.println("REPLACING " + (items.get(slot) != null ? items.get(slot)[0].toStringExtended() : "NOTHING"));

//        List<FullItemData> forged;
//        if (recipe != null) {
//            forged = enchanted.stream().map(item -> Reforger.presetReforge(item, recipe)).toList();
//        } else {
//            forged = enchanted.stream().flatMap(item -> Reforger.reforgeItem(model.reforgeRules(), item).stream()).toList();
//        }


        List<FullItemData> extraOptions;
        if (fixedForge.containsKey(extraItem.itemId())) {
            extraOptions = Collections.singletonList(Reforger.presetReforge(extraItem, fixedForge.get(extraItem.itemId())));
        } else {
            extraOptions = Reforger.reforgeItem(model.reforgeRules(), extraItem);
        }
        items.put(slot, extraOptions);

        job.model = model;
        job.setItemOptions(items);
        job.startTime = null;
        job.extraItem = extraItem;
        job.extraItemSlot = slot;
        return job;
    }

    private Tuple.Tuple4<FullItemData, SlotEquip, SpecType, FullItemSet> handleBasicSolveResult(JobOutput job) {
        Optional<FullItemSet> resultSet = job.getFinalResultSet();
        job.input.printRecorder.outputNow();

        if (resultSet.isEmpty()) {
            OutputText.println("UPGRADE SET NOT FOUND " + job.input.extraItemSlot + " " + job.input.extraItem);
            return null;
        }

        return Tuple.create(job.input.extraItem, job.input.extraItemSlot, job.input.model.spec(), job.getFinalResultSet().orElseThrow());
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
