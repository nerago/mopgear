package au.nerago.mopgear;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.results.AsWowSimJson;
import au.nerago.mopgear.results.JobInfo;
import au.nerago.mopgear.results.OutputText;
import au.nerago.mopgear.util.*;

import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "unused"})
public class FindMultiSpec {
//    private static final int TARGET_COMBO_COUNT = 320000;
    @SuppressWarnings("FieldCanBeLocal")
    private static final long individualRunSizeMultiply = 1L;
    @SuppressWarnings("FieldCanBeLocal")
    private final boolean hackAllow = false;

    private final Map<Integer, ReforgeRecipe> fixedForge = new HashMap<>();
    private final Map<Integer, StatBlock> overrideEnchant = new HashMap<>();
    private final List<SpecDetails> specs = new ArrayList<>();
    private final Set<Integer> suppressSlotCheck = new HashSet<>();

    public void addFixedForge(int id, ReforgeRecipe reforge) {
        fixedForge.put(id, reforge);
    }

    public void overrideEnchant(int id, StatBlock stats) {
        overrideEnchant.put(id, stats);
    }

    public void addSpec(String label, Path gearFile, ModelCombined model, int ratingMultiply, int[] extraItems, int extraItemsUpgradeLevel, boolean upgradeCurrentItems, boolean challengeScale, Map<Integer, Integer> duplicatedItem) {
        specs.add(new SpecDetails(label, gearFile, model, ratingMultiply, extraItems, extraItemsUpgradeLevel, upgradeCurrentItems, challengeScale, duplicatedItem));
    }

    public void suppressSlotCheck(int id) {
        suppressSlotCheck.add(id);
    }

    public void solve(Instant startTime, long targetComboCount) {
        OutputText.println("PREPARING SPECS");
        for (SpecDetails spec : specs) {
            spec.prepareA(specs);
        }
        for (SpecDetails spec : specs) {
            spec.prepareB(specs);
        }

        validateMultiSetAlignItemSlots(specs.stream().map(s -> s.itemOptions).toList());

        OutputText.println("PREPARING BASELINE SPEC RUNS");
        specs.stream().parallel().forEach(this::optimalWithoutCommon);

        OutputText.println("PREPARING COMMON ITEMS");
        Map<ItemRef, List<ItemData>> commonMap = commonInMultiSet(specs);

        long commonCombos = ItemUtil.estimateSets(commonMap);
        long skip;
        if (commonCombos < targetComboCount * 2) {
            skip = 1;
        } else {
            skip = Primes.roundToPrimeInt(commonCombos / targetComboCount * 2);
        }

        OutputText.println("COMMON COMBOS " + commonCombos + " SKIP SIZE " + skip);

        long indexedOutputSize = commonCombos / skip;
        Stream<Map<ItemRef, ItemData>> commonStream1 = PossibleIndexed.runSolverPartial(commonMap, commonCombos, skip);
        Stream<Map<ItemRef, ItemData>> commonStream2 = PossibleRandom.runSolverPartial(commonMap, indexedOutputSize);
        Stream<Map<ItemRef, ItemData>> commonStream = Stream.concat(commonStream1, commonStream2);

        commonStream = BigStreamUtil.countProgressSmall(indexedOutputSize * 2, startTime, commonStream);

        Stream<ProposedResults> resultStream = commonStream
                .map(r -> subSolveEach(r, specs))
                .filter(Objects::nonNull)
                .unordered()
                .parallel();

        OutputText.println("RUNNING");
        Optional<ProposedResults> best = resultStream.collect(
                new TopCollectorReporting<>(s -> multiRating(s.resultJobs, specs),
                        s -> reportBetter(s.resultJobs, specs)));
        outputResultFinal(best, specs);
    }

    private Map<ItemRef, List<ItemData>> commonInMultiSet(List<SpecDetails> mapArray) {
        Map<ItemRef, List<ItemData>> commonOptions = new HashMap<>();
        Map<ItemRef, Set<String>> seenIn = new HashMap<>();
        
        // initially group items by id/upgrade, filtering common forges for each
        for (SpecDetails spec : mapArray) {
            spec.itemOptions.itemStream()
               .collect(Collectors.groupingBy(item -> item.ref))
               .forEach((ref, forges) -> {
                    seenIn.computeIfAbsent(ref, r -> new HashSet<>()).add(spec.label);
                    commonOptions.compute(ref, (r, prior) -> commonForges(prior, forges));
               });
        }

        // apply fixed forges run setting. should the input be ref or id based?
        for (Map.Entry<Integer, ReforgeRecipe> entry : fixedForge.entrySet()) {
            int id = entry.getKey();
            for (ItemRef ref : commonOptions.keySet().stream().filter(t -> t.itemId() == id).toList()) {
                List<ItemData> forgeList = commonOptions.get(ref);
                forgeList = ItemUtil.onlyMatchingForge(forgeList, entry.getValue());
                commonOptions.put(ref, forgeList);
                OutputText.println("FIXED " + forgeList.getFirst().name);
            }
        }

        // remove anything that only appears in one set
        for (Map.Entry<ItemRef, Set<String>> seenEntry : seenIn.entrySet()) {
            ItemRef ref = seenEntry.getKey();
            if (seenEntry.getValue().size() <= 1 && !fixedForge.containsKey(ref.itemId())) {
                commonOptions.remove(ref);
            }
        }

        // validate and output results
        for (Map.Entry<ItemRef, List<ItemData>> entry : commonOptions.entrySet()) {
            ItemRef ref = entry.getKey();
            List<ItemData> lst = entry.getValue();

            // check for empty lists
            if (lst.isEmpty()) {
                Optional<ItemData> any = mapArray.stream().flatMap(x -> x.itemOptions.itemStream())
                        .filter(item -> ref.equalsTyped(item.ref)).findFirst();
                throw new IllegalArgumentException("No common forge for " + any);
            }

            // print common item
            ItemData item = lst.getFirst();
            Set<String> specs = seenIn.get(ref);
            OutputText.println("COMMON " + ref.itemId() + " " + item.name + " " + item.ref.itemLevel() + " " + String.join(" ", specs));
        }

        return commonOptions;
    }

    private void optimalWithoutCommon(SpecDetails spec) {
        JobInfo job = new JobInfo();
        job.model = spec.model;
        job.itemOptions = spec.itemOptions;
        job.runSizeMultiply = individualRunSizeMultiply;
        job.hackAllow = hackAllow;
        Solver.runJob(job);
        spec.optimalRating = spec.model.calcRating(job.resultSet.orElseThrow());
        OutputText.printf("%s base=%,d mult=%d value=%,d\n", spec.label, Math.round(spec.optimalRating), spec.ratingMultiply, Math.round(spec.optimalRating * spec.ratingMultiply));
    }

    private static List<ItemData> commonForges(List<ItemData> prior, List<ItemData> forges) {
        if (prior == null) {
            return forges;
        }

        ArrayList<ItemData> commonForges = new ArrayList<>();
        for (ItemData a : prior) {
            for (ItemData b : forges) {
                if (ItemData.isIdenticalItem(a, b)) {
                    commonForges.add(a);
                }
            }
        }
        return commonForges;
    }

    private void filterCommonActuallyUsed(Map<ItemRef, ItemData> common, List<JobInfo> resultJobs) {
        common.entrySet().removeIf(entry -> {
                    ItemRef ref = entry.getKey();
                    long count = resultJobs.stream().flatMap(job -> job.resultSet.orElseThrow()
                                    .items.entryStream().map(Tuple.Tuple2::b))
                            .filter(item -> ref.equalsTyped(item.ref))
                            .count();
                    if (count < 2)
                        OutputText.println("REMOVING COMMON " + entry.getValue());
                    return count < 2;
                }
        );
    }

    private ProposedResults subSolveEach(Map<ItemRef, ItemData> commonChoices, List<SpecDetails> specList) {
        List<JobInfo> results = new ArrayList<>();
        for (SpecDetails spec : specList) {
            JobInfo job = subSolvePart(spec.itemOptions, spec.model, commonChoices);
            if (job.resultSet.isEmpty()) {
                return null;
            }
            results.add(job);
        }
        return new ProposedResults(results, commonChoices);
    }

    private JobInfo subSolvePart(EquipOptionsMap fullItemMap, ModelCombined model, Map<ItemRef, ItemData> chosenMap) {
        EquipOptionsMap submitMap = fullItemMap.shallowClone();
        buildJobWithSpecifiedItemsFixed(chosenMap, submitMap);

        JobInfo job = new JobInfo();
        job.model = model;
        job.itemOptions = submitMap;
        job.runSizeMultiply = individualRunSizeMultiply;
        job.hackAllow = hackAllow;
        return Solver.runJob(job);
    }

    static void buildJobWithSpecifiedItemsFixed(Map<ItemRef, ItemData> chosenMap, EquipOptionsMap submitMap) {
        for (SlotEquip slot : SlotEquip.values()) {
            ItemData[] options = submitMap.get(slot);
            if (options == null) {
                continue;
            }

            ItemData[] newOptions = makeSlotFixed(chosenMap, options);
            submitMap.put(slot, newOptions);
        }
    }

    private static ItemData[] makeSlotFixed(Map<ItemRef, ItemData> chosenMap, ItemData[] options) {
        ArrayList<ItemData> list = new ArrayList<>();
        HashSet<ItemRef> chosenToAdd = new HashSet<>();

        for (ItemData item : options) {
            ItemRef ref = item.ref;
            if (chosenMap.containsKey(ref)) {
                chosenToAdd.add(ref);
            } else {
                // not a common/fixed item
                list.add(item);
            }
        }

        for (ItemRef ref : chosenToAdd) {
            list.add(chosenMap.get(ref));
        }

        return list.toArray(ItemData[]::new);
    }

    private void validateMultiSetAlignItemSlots(List<EquipOptionsMap> mapsParam) {
        Map<Integer, SlotEquip> seen = new HashMap<>();
        for (EquipOptionsMap map : mapsParam) {
            map.forEachPair((slot, array) -> {
                for (ItemData item : array) {
                    int itemId = item.ref.itemId();
                    SlotEquip val = seen.get(itemId);
                    if (val == null) {
                        seen.put(itemId, slot);
                    } else if (val != slot && !suppressSlotCheck.contains(itemId)) {
                        throw new IllegalArgumentException("duplicate in non matching slot " + item);
                    }
                }
            });
        }
    }

    private long multiRating(List<JobInfo> resultJobs, List<SpecDetails> specList) {
        long total = 0;
        for (int i = 0; i < resultJobs.size(); ++i) {
            ItemSet set = resultJobs.get(i).resultSet.orElseThrow();
            SpecDetails spec = specList.get(i);
            long specRating = spec.model.calcRating(set);
            total += specRating * spec.ratingMultiply;
        }
        return total;
    }

    private void reportBetter(List<JobInfo> resultJobs, List<SpecDetails> specList) {
        long rating = multiRating(resultJobs, specList);
        synchronized (OutputText.class) {
            OutputText.printf("^^^^^^^^^ %s ^^^^^^^ %d ^^^^^^^^^\n", LocalDateTime.now(), rating);
            for (int i = 0; i < resultJobs.size(); ++i) {
                JobInfo job = resultJobs.get(i);
                ItemSet set = job.resultSet.orElseThrow();
                SpecDetails spec = specList.get(i);
                OutputText.printf("-------------- %s -------------- %s\n", spec.label, "<HACK>".repeat(job.hackCount));
                job.printRecorder.outputNow();
                set.outputSet(spec.model);
            }
            OutputText.println("#######################################");
        }
    }

    private void outputResultFinal(Optional<ProposedResults> bestSets, List<SpecDetails> specList) {
        if (bestSets.isPresent()) {
            List<JobInfo> resultJobs = bestSets.get().resultJobs;
            long totalRating = multiRating(resultJobs, specList);

            Map<ItemRef, ItemData> commonFinal = bestSets.get().chosenMap;

            OutputText.println("%%%%%%%%%%%%%%%%%%% COMMON-FORGE %%%%%%%%%%%%%%%%%%%");
            filterCommonActuallyUsed(commonFinal, resultJobs);
            commonFinal.values().forEach(item -> OutputText.println(item.toString()));

            OutputText.println("%%%%%%%%%%%%%% Main.commonFixedItems %%%%%%%%%%%%%%%");
            commonFinal.values().forEach(item -> {
                if (item.reforge.isEmpty())
                    OutputText.printf("map.put(%d, List.of(new ReforgeRecipe(null, null))); // %s %s\n", item.ref.itemId(), item.slot, item.name);
                else
                    OutputText.printf("map.put(%d, List.of(new ReforgeRecipe(%s, %s))); // %s %s\n", item.ref.itemId(), item.reforge.source(), item.reforge.dest(), item.slot, item.name);
            });

            OutputText.println("@@@@@@@@@ BEST SET(s) @@@@@@@@@");
            OutputText.printf("^^^^^^^^^^^^^ %d ^^^^^^^^^^^^^\n", totalRating);

            for (int i = 0; i < resultJobs.size(); ++i) {
                SpecDetails spec = specList.get(i);
                OutputText.printf("-------------- %s --------------\n", spec.label);

                OutputText.println("DRAFTDRAFTDRAFT");
                JobInfo draftJob = resultJobs.get(i);
                ItemSet draftSet = draftJob.resultSet.orElseThrow();
                double draftSpecRating = spec.model.calcRating(draftSet);
                draftJob.printRecorder.outputNow();
                draftSet.outputSet(spec.model);
                AsWowSimJson.writeToOut(draftSet.items);

                OutputText.println("REVISEDREVISEDREVISED");
                JobInfo revisedJob = subSolvePart(spec.itemOptions, spec.model, commonFinal);
                ItemSet revisedSet = revisedJob.resultSet.orElseThrow();
                double revisedSpecRating = spec.model.calcRating(revisedSet);
                if (revisedSpecRating > draftSpecRating) {
                    revisedJob.printRecorder.outputNow();
                    revisedSet.outputSet(spec.model);
                    AsWowSimJson.writeToOut(revisedSet.items);
                } else {
                    OutputText.println("Revised no better");
                }

                double specRating = Math.max(draftSpecRating, revisedSpecRating);
                OutputText.printf("COMMON ITEM PENALTY PERCENT %1.3f\n", specRating / spec.optimalRating * 100.0);
            }

        } else {
            OutputText.println("@@@@@@@@@ NO BEST SET FOUND @@@@@@@@@");
        }
    }

    private class SpecDetails {
        final String label;
        final Path gearFile;
        final ModelCombined model;
        final int ratingMultiply;
        final int[] extraItems;
        final int extraItemsUpgradeLevel;
        final boolean upgradeCurrentItems;
        final boolean challengeScale;
        final Map<Integer, Integer> duplicatedItems;
        double optimalRating;
        EquipOptionsMap itemOptions;

        private SpecDetails(String label, Path gearFile, ModelCombined model, int ratingMultiply, int[] extraItems, int extraItemsUpgradeLevel, boolean upgradeCurrentItems, boolean challengeScale, Map<Integer, Integer> remapDuplicateId) {
            this.label = label;
            this.gearFile = gearFile;
            this.model = model;
            this.ratingMultiply = ratingMultiply;
            this.extraItems = extraItems;
            this.extraItemsUpgradeLevel = extraItemsUpgradeLevel;
            this.upgradeCurrentItems = upgradeCurrentItems;
            this.challengeScale = challengeScale;
            this.duplicatedItems = remapDuplicateId;
        }

        public void prepareA(List<SpecDetails> allSpecs) {
            itemOptions = ItemUtil.readAndLoad(false, gearFile, model.reforgeRules(), null);
            if (upgradeCurrentItems)
                itemOptions = ItemUtil.upgradeAllTo2(itemOptions);
            remapDuplicates();
        }

        public void prepareB(List<SpecDetails> allSpecs) {
            for (int itemId : extraItems) {
                addExtra(itemId, allSpecs);
            }
        }

        private void addExtra(int itemId, List<SpecDetails> allSpecs) {
            verifyNotAlreadyIncluded(itemId);
            if (!copyFromOtherSpec(itemId, allSpecs)) {
                loadAndGenerate(itemId);
            }
        }

        private void remapDuplicates() {
            itemOptions.forEachPair((slot, array) -> ArrayUtil.mapInPlace(array, this::remapDuplicate));
        }

        private ItemData remapDuplicate(ItemData itemData) {
            Integer duplicateId = duplicatedItems.get(itemData.ref.itemId());
            if (duplicateId != null && duplicateId != 0) {
                return itemData.changeDuplicate(duplicateId);
            } else {
                return itemData;
            }
        }

        private void verifyNotAlreadyIncluded(int itemId) {
            ItemData extraItem = ItemUtil.loadItemBasic(itemId, extraItemsUpgradeLevel);
            SlotEquip[] slots = extraItem.slot.toSlotEquipOptions();
            for (SlotEquip slot : slots) {
                ItemData[] existing = itemOptions.get(slot);
                if (ArrayUtil.anyMatch(existing, item -> item.ref.itemId() == itemId))
                    throw new IllegalArgumentException("{SET " + label + "} item already included " + itemId + " " + extraItem);
            }
        }

        private boolean copyFromOtherSpec(int itemId, List<SpecDetails> allSpecs) {
            ItemData[] otherCopies = allSpecs.stream()
                    .flatMap(spec -> spec.itemOptions.itemStream())
                    .filter(item -> item.ref.itemId() == itemId)
                    .distinct()
                    .toArray(ItemData[]::new);

            if (otherCopies.length > 0) {
                SlotEquip[] slotOptions = otherCopies[0].slot.toSlotEquipOptions();
                for (SlotEquip slot : slotOptions) {
                    ItemData[] existing = itemOptions.get(slot);
                    itemOptions.put(slot, ArrayUtil.concat(existing, otherCopies));
                    reportNewSlotOptions(slot);
                }
                return true;
            }
            return false;
        }

        private void loadAndGenerate(int itemId) {
            ItemData extraItem = ItemUtil.loadItemBasic(itemId, extraItemsUpgradeLevel);
            if (overrideEnchant.containsKey(itemId)) {
                extraItem = extraItem.changeFixed(overrideEnchant.get(itemId));
            } else {
                extraItem = ItemUtil.defaultEnchants(extraItem, model, true);
            }

            ItemData[] extraForged = Reforger.reforgeItem(model.reforgeRules(), extraItem);

            SlotEquip[] slotOptions = extraItem.slot.toSlotEquipOptions();
            for (SlotEquip slot : slotOptions) {
                ItemData[] existing = itemOptions.get(slot);
                itemOptions.put(slot, ArrayUtil.concat(existing, extraForged));
                reportNewSlotOptions(slot);
            }
        }

        private void reportNewSlotOptions(SlotEquip slot) {
            ItemData[] slotArray = itemOptions.get(slot);
            HashSet<ItemRef> seen = new HashSet<>();
            ArrayUtil.forEach(slotArray, it -> {
                if (seen.add(it.ref)) {
                    OutputText.println("OPTION " + slot + " " + it);
                }
            });
        }
    }

    private record ProposedResults(List<JobInfo> resultJobs, Map<ItemRef, ItemData> chosenMap) {
    }
}
