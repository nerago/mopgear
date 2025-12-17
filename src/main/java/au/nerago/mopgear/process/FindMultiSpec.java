package au.nerago.mopgear.process;

import au.nerago.mopgear.ItemLoadUtil;
import au.nerago.mopgear.ItemMapUtil;
import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.permute.PossibleIndexed;
import au.nerago.mopgear.permute.PossibleRandom;
import au.nerago.mopgear.permute.Solver;
import au.nerago.mopgear.results.AsWowSimJson;
import au.nerago.mopgear.results.JobInput;
import au.nerago.mopgear.results.JobOutput;
import au.nerago.mopgear.results.OutputText;
import au.nerago.mopgear.util.ArrayUtil;
import au.nerago.mopgear.util.BigStreamUtil;
import au.nerago.mopgear.util.Primes;
import au.nerago.mopgear.util.TopCollectorReporting;

import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "unused"})
public class FindMultiSpec {
    private static final long individualRunSizeMultiply = 1L;
    private final boolean hackAllow = false;

    private final Map<Integer, ReforgeRecipe> fixedForge = new HashMap<>();
    private final Map<Integer, StatBlock> overrideEnchant = new HashMap<>();
    private final List<SpecDetails> specs = new ArrayList<>();
    private final Set<Integer> suppressSlotCheck = new HashSet<>();
    private Predicate<ProposedResults> multiSetFilter;

    public void addFixedForge(int id, ReforgeRecipe reforge) {
        fixedForge.put(id, reforge);
    }

    public void overrideEnchant(int id, StatBlock stats) {
        overrideEnchant.put(id, stats);
    }

    public void multiSetFilter(Predicate<ProposedResults> multiSetFilter) {
        this.multiSetFilter = multiSetFilter;
    }

    public SpecDetailsInterface addSpec(String label, Path gearFile, ModelCombined model, double ratingTargetPercent, int[] extraItems, int extraItemsUpgradeLevel, boolean upgradeCurrentItems) {
        SpecDetails spec = new SpecDetails(label, gearFile, model, ratingTargetPercent, extraItems, extraItemsUpgradeLevel, upgradeCurrentItems);
        specs.add(spec);
        return spec;
    }

    public void suppressSlotCheck(int id) {
        suppressSlotCheck.add(id);
    }

    public void solve(long targetComboCount) {
        OutputText.println("PREPARING SPECS");
        specs.forEach(s -> s.prepareStartingGear(specs));
        specs.forEach(s -> s.prepareExtraItems(specs));

        validateMultiSetAlignItemSlots(specs.stream().map(s -> s.itemOptions).toList());
        OutputText.println();

        OutputText.println("PREPARING BASELINE SPEC RUNS");
        specs.stream().parallel().forEach(this::optimalWithoutCommon);
        OutputText.println();

        specs.forEach(s -> s.prepareRatingMultiplier(specs));

        OutputText.println("PREPARING COMMON ITEMS");
        Map<ItemRef, List<FullItemData>> commonMap = commonInMultiSet(specs);
        OutputText.println();

        long commonCombos = BigStreamUtil.estimateSets(commonMap);
        long skip;
        if (commonCombos < targetComboCount * 2) {
            skip = 1;
        } else {
            skip = Primes.roundToPrimeInt(commonCombos / targetComboCount * 2);
        }

        OutputText.println("COMMON COMBOS " + commonCombos + " SKIP SIZE " + skip);

        // TODO include current setup

        long indexedOutputSize = commonCombos / skip;
        Stream<Map<ItemRef, FullItemData>> commonStream1 = PossibleIndexed.runSolverPartial(commonMap, commonCombos, skip);
        Stream<Map<ItemRef, FullItemData>> commonStream2 = PossibleRandom.runSolverPartial(commonMap, indexedOutputSize / 2);
        Stream<Map<ItemRef, FullItemData>> commonStream = Stream.concat(commonStream1, commonStream2);

        Instant startTime = Instant.now();
        commonStream = BigStreamUtil.countProgressSmall(indexedOutputSize * 3 / 2, startTime, commonStream);

        Stream<ProposedResults> resultStream = commonStream
                .map(r -> subSolveEach(r, specs))
                .filter(Objects::nonNull)
                .unordered()
                .parallel();

        if (multiSetFilter != null) {
            resultStream = resultStream.filter(multiSetFilter);
        }

        OutputText.println("RUNNING");
        Optional<ProposedResults> best = resultStream
                .filter(s -> checkGood(s.resultJobs, specs))
                .collect(new TopCollectorReporting<>(
                        s -> multiRating(s.resultJobs, specs),
                        s -> reportBetter(s.resultJobs, specs)));
        outputResultFinal(best, specs);

        // TODO highlight gems changed vs as loaded

        // TODO keep track of good indexes and search near
    }

    private Map<ItemRef, List<FullItemData>> commonInMultiSet(List<SpecDetails> mapArray) {
        Map<ItemRef, List<FullItemData>> commonOptions = new HashMap<>();
        Map<ItemRef, Set<String>> seenIn = new HashMap<>();
        
        // initially group items by id/upgrade, filtering common forges for each
        for (SpecDetails spec : mapArray) {
            spec.itemOptions.itemStream()
               .collect(Collectors.groupingBy(FullItemData::ref))
               .forEach((ref, forges) -> {
                    seenIn.computeIfAbsent(ref, r -> new HashSet<>()).add(spec.label);
                    commonOptions.compute(ref, (r, prior) -> commonForges(prior, forges));
               });
        }

        // apply fixed forges run setting. should the input be ref or id based?
        for (Map.Entry<Integer, ReforgeRecipe> entry : fixedForge.entrySet()) {
            int id = entry.getKey();
            for (ItemRef ref : commonOptions.keySet().stream().filter(t -> t.itemId() == id).toList()) {
                List<FullItemData> forgeList = commonOptions.get(ref);
                forgeList = onlyMatchingForge(forgeList, entry.getValue());
                commonOptions.put(ref, forgeList);
                OutputText.println("FIXED " + forgeList.getFirst().fullName());
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
        for (Map.Entry<ItemRef, List<FullItemData>> entry : commonOptions.entrySet()) {
            ItemRef ref = entry.getKey();
            List<FullItemData> lst = entry.getValue();

            // check for empty lists
            if (lst.isEmpty()) {
                Optional<FullItemData> any = mapArray.stream().flatMap(x -> x.itemOptions.itemStream())
                        .filter(item -> ref.equalsTyped(item.ref())).findFirst();
                throw new IllegalArgumentException("No common forge for " + ref + " " + any);
            }

            // print common item
            FullItemData item = lst.getFirst();
            Set<String> specs = seenIn.get(ref);
            OutputText.println("COMMON " + item.itemId() + " " + item.fullName() + " " + item.shared.ref().itemLevel() + " " + String.join(" ", specs));
        }

        return commonOptions;
    }

    private void optimalWithoutCommon(SpecDetails spec) {
        JobInput job = new JobInput();
        job.model = spec.model;
        job.setItemOptions(spec.itemOptions);
        job.runSizeMultiply = individualRunSizeMultiply * 4;
        job.hackAllow = hackAllow;
        job.forcePhased = true;
        JobOutput output = Solver.runJob(job);

        FullItemSet set = output.getFinalResultSet().orElseThrow();
        spec.optimalRating = output.resultRating;
        synchronized (OutputText.class) {
            OutputText.println("SET " + spec.label);
            set.outputSet(spec.model);

//            int setItems = spec.model.setBonus().countInAnySet(output.resultSet.orElseThrow().items());
//            OutputText.println("Set Items " + setItems);
        }
    }

    private static List<FullItemData> commonForges(List<FullItemData> prior, List<FullItemData> forges) {
        if (prior == null) {
            return forges;
        }

        ArrayList<FullItemData> commonForges = new ArrayList<>();
        for (FullItemData a : prior) {
            for (FullItemData b : forges) {
                if (FullItemData.isIdenticalItem(a, b)) {
                    commonForges.add(a);
                }
            }
        }
        return commonForges;
    }

    private void filterCommonActuallyUsed(Map<ItemRef, FullItemData> common, List<JobOutput> resultJobs) {
        common.entrySet().removeIf(entry -> {
                    ItemRef ref = entry.getKey();
                    long count = resultJobs.stream()
                            .flatMap(job -> job.resultSet.orElseThrow().items().itemStream())
                            .filter(item -> item.isSameItem(ref))
                            .count();
                    if (count < 2)
                        OutputText.println("REMOVING COMMON " + entry.getValue());
                    return count < 2;
                }
        );
    }

    private ProposedResults subSolveEach(Map<ItemRef, FullItemData> commonChoices, List<SpecDetails> specList) {
        List<JobOutput> results = new ArrayList<>();
        for (SpecDetails spec : specList) {
            JobOutput job = subSolvePart(spec.itemOptions, spec.model, commonChoices, 1, false);
            if (job.resultSet.isEmpty()) {
                return null;
            }
            results.add(job);
        }
        return new ProposedResults(results, commonChoices);
    }

    private JobOutput subSolvePart(EquipOptionsMap fullItemMap, ModelCombined model, Map<ItemRef, FullItemData> chosenMap, int finalMultiply, boolean isFinal) {
        EquipOptionsMap submitMap = fullItemMap.shallowClone();
        buildJobWithSpecifiedItemsFixed(chosenMap, submitMap);

        JobInput job = new JobInput();
        job.model = model;
        job.setItemOptions(submitMap);
        job.runSizeMultiply = individualRunSizeMultiply * finalMultiply;
        job.hackAllow = hackAllow;
        job.forcePhased = isFinal;
        job.singleThread = true;
        return Solver.runJob(job);
    }

    static void buildJobWithSpecifiedItemsFixed(Map<ItemRef, FullItemData> chosenMap, EquipOptionsMap submitMap) {
        for (SlotEquip slot : SlotEquip.values()) {
            FullItemData[] options = submitMap.get(slot);
            if (options == null) {
                continue;
            }

            FullItemData[] newOptions = makeSlotFixed(chosenMap, options);
            submitMap.put(slot, newOptions);
        }
    }

    private static FullItemData[] makeSlotFixed(Map<ItemRef, FullItemData> chosenMap, FullItemData[] options) {
        ArrayList<FullItemData> list = new ArrayList<>();
        HashSet<ItemRef> chosenToAdd = new HashSet<>();

        for (FullItemData item : options) {
            ItemRef ref = item.ref();
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

        return list.toArray(FullItemData[]::new);
    }

    private void validateMultiSetAlignItemSlots(List<EquipOptionsMap> mapsParam) {
        Map<Integer, SlotEquip> seen = new HashMap<>();
        for (EquipOptionsMap map : mapsParam) {
            map.forEachPair((slot, array) -> {
                for (FullItemData item : array) {
                    int itemId = item.itemId();
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

    public static List<FullItemData> onlyMatchingForge(List<FullItemData> forgeList, ReforgeRecipe recipe) {
        if (recipe == null || recipe.isEmpty()) {
            for (FullItemData item : forgeList) {
                if (item.reforge.isEmpty())
                    return List.of(item);
            }
        } else {
            for (FullItemData item : forgeList) {
                if (recipe.equalsTyped(item.reforge))
                    return List.of(item);
            }
        }
        throw new IllegalArgumentException("specified forge not found " + forgeList.getFirst() + " " + recipe);
    }

    private boolean checkGood(List<JobOutput> resultJobs, List<SpecDetails> specList) {
        for (int i = 0; i < resultJobs.size(); ++i) {
            JobOutput job = resultJobs.get(i);
            SpecDetails spec = specList.get(i);
            long jobRating = job.resultRating;

            if (job.resultSet.isEmpty())
                return false;

            if (spec.worstCommonPenalty != 0) {
                double penalty = jobRating / spec.optimalRating * 100.0;
                if (penalty < spec.worstCommonPenalty)
                    return false;
            }
        }
        return true;
    }

    private long multiRating(List<JobOutput> resultJobs, List<SpecDetails> specList) {
        long total = 0;
        for (int i = 0; i < resultJobs.size(); ++i) {
            JobOutput job = resultJobs.get(i);
            SpecDetails spec = specList.get(i);
            long specRating = job.resultRating;
            total += specRating * spec.ratingMultiply;
        }
        return total;
    }

    private void reportBetter(List<JobOutput> resultJobs, List<SpecDetails> specList) {
        long rating = multiRating(resultJobs, specList);
        synchronized (OutputText.class) {
            OutputText.printf("^^^^^^^^^ %s ^^^^^^^ %d ^^^^^^^^^\n", LocalDateTime.now(), rating);
            for (int i = 0; i < resultJobs.size(); ++i) {
                JobOutput job = resultJobs.get(i);
                FullItemSet set = job.getFinalResultSet().orElseThrow();
                SpecDetails spec = specList.get(i);
                OutputText.printf("-------------- %s -------------- %s\n", spec.label, "<HACK>".repeat(job.hackCount));
                job.input.printRecorder.outputNow();
                set.outputSetDetailed(spec.model);
                OutputText.printf("COMMON ITEM PENALTY PERCENT %1.3f\n", job.resultRating / spec.optimalRating * 100.0);
            }
            OutputText.println("#######################################");
        }
    }

    private void outputResultFinal(Optional<ProposedResults> bestSets, List<SpecDetails> specList) {
        if (bestSets.isPresent()) {
            List<JobOutput> resultJobs = bestSets.get().resultJobs;
            long totalRating = multiRating(resultJobs, specList);

            Map<ItemRef, FullItemData> commonFinal = bestSets.get().chosenMap;

            OutputText.println("%%%%%%%%%%%%%%%%%%% COMMON-FORGE %%%%%%%%%%%%%%%%%%%");
            filterCommonActuallyUsed(commonFinal, resultJobs);
            commonFinal.values().forEach(item -> OutputText.println(item.toString()));

            OutputText.println("%%%%%%%%%%%%%% Main.commonFixedItems %%%%%%%%%%%%%%%");
            commonFinal.values().forEach(item -> {
                if (item.reforge.isEmpty())
                    OutputText.printf("map.put(%d, List.of(new ReforgeRecipe(null, null))); // %s %s\n", item.itemId(), item.slot(), item.shared.name());
                else
                    OutputText.printf("map.put(%d, List.of(new ReforgeRecipe(%s, %s))); // %s %s\n", item.itemId(), item.reforge.source(), item.reforge.dest(), item.slot(), item.shared.name());
            });

            OutputText.println("@@@@@@@@@ BEST SET(s) @@@@@@@@@");
            OutputText.printf("^^^^^^^^^^^^^ %d ^^^^^^^^^^^^^\n", totalRating);

            for (int i = 0; i < resultJobs.size(); ++i) {
                SpecDetails spec = specList.get(i);
                OutputText.printf("-------------- %s --------------\n", spec.label);

                JobOutput draftJob = resultJobs.get(i);
                FullItemSet draftSet = draftJob.getFinalResultSet().orElseThrow();
                double draftSpecRating = draftJob.resultRating;
                OutputText.printf("DRAFT %,d\n", (long) draftSpecRating);
                draftJob.input.printRecorder.outputNow();
                draftSet.outputSetDetailed(spec.model);
                AsWowSimJson.writeToOut(draftSet.items());

//                ItemLoadUtil.duplicateAlternateEnchants(spec.itemOptions, spec.model);
                JobOutput revisedJob = subSolvePart(spec.itemOptions, spec.model, commonFinal, 4, true);

                FullItemSet revisedSet = null;
                double revisedSpecRating = 0;
                if (revisedJob.resultSet.isPresent()) {
                    revisedSet = revisedJob.getFinalResultSet().orElse(null);
                    revisedSpecRating = revisedJob.resultRating;
                }

                if (revisedSet != null && revisedSpecRating > draftSpecRating) {
                    OutputText.printf("REVISED %,d\n", (long) revisedSpecRating);
                    revisedJob.input.printRecorder.outputNow();
                    revisedSet.outputSetDetailed(spec.model);
                    AsWowSimJson.writeToOut(revisedSet.items());
                } else if (revisedSet == null) {
                    OutputText.println("REVISED FAIL REVISED FAIL");
                } else {
                    OutputText.println("REVISED no better");
                }

                double specRating = Math.max(draftSpecRating, revisedSpecRating);
                OutputText.printf("COMMON ITEM PENALTY PERCENT %1.3f\n", specRating / spec.optimalRating * 100.0);
            }


            // TODO report on changed enchant

        } else {
            OutputText.println("@@@@@@@@@ NO BEST SET FOUND @@@@@@@@@");
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public interface SpecDetailsInterface {
        boolean isChallengeScale();

        SpecDetailsInterface setChallengeScale(boolean challengeScale);

        Map<Integer, Integer> getDuplicatedItems();

        SpecDetailsInterface setDuplicatedItems(Map<Integer, Integer> duplicatedItems);

        double getWorstCommonPenalty();

        SpecDetailsInterface setWorstCommonPenalty(double penalty);

        SpecDetailsInterface addRemoveItem(int id);
    }

    private class SpecDetails implements SpecDetailsInterface {
        final String label;
        final Path gearFile;
        final ModelCombined model;
        final double ratingTargetPercent;
        int ratingMultiply;
        final int[] extraItems;
        final int extraItemsUpgradeLevel;
        final boolean upgradeCurrentItems;
        boolean challengeScale;
        double worstCommonPenalty;
        Map<Integer, Integer> duplicatedItems;
        List<Integer> removeItems;
        double optimalRating;
        EquipOptionsMap itemOptions;

        private SpecDetails(String label, Path gearFile, ModelCombined model, double ratingTargetPercent, int[] extraItems, int extraItemsUpgradeLevel, boolean upgradeCurrentItems) {
            this.label = label;
            this.gearFile = gearFile;
            this.model = model;
            this.ratingTargetPercent = ratingTargetPercent;
            this.extraItems = extraItems;
            this.extraItemsUpgradeLevel = extraItemsUpgradeLevel;
            this.upgradeCurrentItems = upgradeCurrentItems;
        }

        @Override
        public boolean isChallengeScale() {
            return challengeScale;
        }

        @Override
        public SpecDetailsInterface setChallengeScale(boolean challengeScale) {
            this.challengeScale = challengeScale;
            return this;
        }

        @Override
        public Map<Integer, Integer> getDuplicatedItems() {
            return duplicatedItems;
        }

        @Override
        public SpecDetailsInterface setDuplicatedItems(Map<Integer, Integer> duplicatedItems) {
            this.duplicatedItems = duplicatedItems;
            return this;
        }

        @Override
        public SpecDetailsInterface addRemoveItem(int id) {
            if (removeItems == null)
                removeItems = new ArrayList<>();
            removeItems.add(id);
            return this;
        }

        @Override
        public double getWorstCommonPenalty() {
            return worstCommonPenalty;
        }

        @Override
        public SpecDetailsInterface setWorstCommonPenalty(double penalty) {
            worstCommonPenalty = penalty;
            return this;
        }

        public void prepareStartingGear(List<SpecDetails> allSpecs) {
            itemOptions = ItemLoadUtil.readAndLoad(gearFile, model, null, false);
            if (upgradeCurrentItems)
                itemOptions = ItemMapUtil.upgradeAllTo2(itemOptions);
            remapDuplicates();
        }

        public void prepareExtraItems(List<SpecDetails> allSpecs) {
            for (int itemId : extraItems) {
                addExtra(itemId, allSpecs);
            }
            if (removeItems != null) {
                for (int itemId : removeItems) {
                    itemOptions.mapSlots(array -> Arrays.stream(array).filter(it -> it.itemId() != itemId).toArray(FullItemData[]::new));
                }
            }
        }

        private void addExtra(int itemId, List<SpecDetails> allSpecs) {
            verifyNotAlreadyIncluded(itemId);
            if (!copyFromOtherSpec(itemId, allSpecs)) {
                loadAndGenerate(itemId);
            }
        }

        private void remapDuplicates() {
            if (duplicatedItems != null) {
                itemOptions.forEachPair((slot, array) -> ArrayUtil.mapInPlace(array, this::remapDuplicate));
            }
        }

        private FullItemData remapDuplicate(FullItemData itemData) {
            Integer duplicateId = duplicatedItems.get(itemData.itemId());
            if (duplicateId != null && duplicateId != 0) {
                return itemData.changeDuplicate(duplicateId);
            } else {
                return itemData;
            }
        }

        private void verifyNotAlreadyIncluded(int itemId) {
            FullItemData extraItem = ItemLoadUtil.loadItemBasic(itemId, extraItemsUpgradeLevel);
            SlotEquip[] slots = extraItem.slot().toSlotEquipOptions();
            for (SlotEquip slot : slots) {
                FullItemData[] existing = itemOptions.get(slot);
                if (ArrayUtil.anyMatch(existing, item -> item.itemId() == itemId))
                    throw new IllegalArgumentException("{SET " + label + "} item already included " + itemId + " " + extraItem);
            }
        }

        private boolean copyFromOtherSpec(int itemId, List<SpecDetails> allSpecs) {
            FullItemData[] otherCopies = allSpecs.stream()
                    .flatMap(spec -> spec.itemOptions.itemStream())
                    .filter(item -> item.itemId() == itemId)
                    .distinct()
                    .toArray(FullItemData[]::new);

            if (otherCopies.length > 0) {
                SlotEquip[] slotOptions = otherCopies[0].slot().toSlotEquipOptions();
                for (SlotEquip slot : slotOptions) {
                    FullItemData[] existing = itemOptions.get(slot);
                    itemOptions.put(slot, ArrayUtil.concat(existing, otherCopies));
                    reportNewSlotOptions(slot);
                }
                return true;
            }
            return false;
        }

        private void loadAndGenerate(int itemId) {
            FullItemData extraItem = ItemLoadUtil.loadItemBasic(itemId, 0);
            if (extraItem.isUpgradable() && extraItemsUpgradeLevel > 0)
                extraItem = ItemLoadUtil.loadItemBasic(itemId, extraItemsUpgradeLevel);

            if (overrideEnchant.containsKey(itemId)) {
                extraItem = extraItem.changeEnchant(overrideEnchant.get(itemId));
            } else {
                extraItem = ItemLoadUtil.defaultEnchants(extraItem, model, true, false);
            }

            List<FullItemData> extraForged = Reforger.reforgeItem(model.reforgeRules(), extraItem);

            SlotEquip[] slotOptions = extraItem.slot().toSlotEquipOptions();
            for (SlotEquip slot : slotOptions) {
                FullItemData[] existing = itemOptions.get(slot);
                itemOptions.put(slot, ArrayUtil.concatNullSafe(existing, extraForged));
                reportNewSlotOptions(slot);
            }
        }

        private void reportNewSlotOptions(SlotEquip slot) {
            FullItemData[] slotArray = itemOptions.get(slot);
            HashSet<ItemRef> seen = new HashSet<>();
            ArrayUtil.forEach(slotArray, it -> {
                if (seen.add(it.ref())) {
                    OutputText.println("OPTION " + slot + " " + it);
                }
            });
        }

        public void prepareRatingMultiplier(List<SpecDetails> specs) {
            double totalPercent = specs.stream().mapToDouble(s -> s.ratingTargetPercent).sum();
            if (totalPercent < 0.99 || totalPercent > 1.01)
                throw new IllegalArgumentException("doesn't add to one");

            double targetCombined = 1000000000000000000.0;
            if (optimalRating > targetCombined / 100)
                throw new IllegalArgumentException("need bigger ratings");

            double targetForThis = targetCombined * ratingTargetPercent;
            ratingMultiply = (int) Math.round(targetForThis / optimalRating);

            OutputText.printf("MULTIPLIERS %s base=%,d mult=%d value=%,d\n", label, Math.round(optimalRating), ratingMultiply, Math.round(optimalRating * ratingMultiply));
        }
    }

    public record ProposedResults(List<JobOutput> resultJobs, Map<ItemRef, FullItemData> chosenMap) {
    }
}
