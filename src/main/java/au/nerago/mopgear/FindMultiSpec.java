package au.nerago.mopgear;

import au.nerago.mopgear.domain.*;
import au.nerago.mopgear.io.ItemCache;
import au.nerago.mopgear.model.ModelCombined;
import au.nerago.mopgear.results.JobInfo;
import au.nerago.mopgear.results.OutputText;
import au.nerago.mopgear.util.*;

import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class FindMultiSpec {
    public static final int RANDOM_COMBOS = 100000;
    //        Long runSize = 200000L;
    @SuppressWarnings("FieldCanBeLocal")
    private final long runSizeMultiply = 1L;
    @SuppressWarnings("FieldCanBeLocal")
    private final boolean hackAllow = false;

    private final Map<Integer, ReforgeRecipe> fixedForge = new HashMap<>();
    private final List<SpecDetails> specs = new ArrayList<>();
    private final Set<Integer> duplicatedItem = new HashSet<>();

    public void addFixedForge(int id, ReforgeRecipe reforge) {
        fixedForge.put(id, reforge);
    }

    public void addSpec(SpecDetails spec) {
        specs.add(spec);
    }

    public void haveDuplicateItem(int id) {
        duplicatedItem.add(id);
    }

    public void solve(Instant startTime) {
        for (SpecDetails spec : specs) {
            spec.prepareA(specs);
        }
        for (SpecDetails spec : specs) {
            spec.prepareB(specs);
        }

        ItemUtil.validateMultiSetAlignItemSlots(specs.stream().map(s -> s.itemOptions).toList());

//        addDuplicatedItems();

        Map<ItemRef, List<ItemData>> commonMap = commonInMultiSet(specs);

        long commonCombos = ItemUtil.estimateSets(commonMap);
        OutputText.println("COMMON COMBOS " + commonCombos);

        int skip = Primes.roundToPrimeInt(999999);

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

        Optional<ProposedResults> best = resultStream.collect(
                new TopCollectorReporting<>(s -> multiRating(s.resultJobs, specs),
                        s -> reportBetter(s.resultJobs, specs)));
        outputResultTwins(best, specs);
    }

    private void addDuplicatedItems() {
        int dupNum = 1;
        for (int itemId : duplicatedItem) {
            for (SpecDetails spec : specs) {
                for (SlotEquip slot : SlotEquip.values()) {
                    ItemData[] currentOpts = spec.itemOptions.get(slot);
                    if (currentOpts != null) {
                        ArrayList<ItemData> optList = new ArrayList<>();
                        for (ItemData item : currentOpts) {
                            optList.add(item);
                            if (item.ref.itemId() == itemId) {
                                optList.add(item.changeDuplicate(dupNum));
                            }
                        }
                        spec.itemOptions.put(slot, optList.toArray(ItemData[]::new));
                    }
                }
            }
        }
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
            OutputText.println("COMMON " + ref.itemId() + " " + item.name + " " + String.join(" ", specs));
        }

        return commonOptions;
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
        job.runSizeMultiply = runSizeMultiply;
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

    private long multiRating(List<JobInfo> resultJobs, List<SpecDetails> specList) {
        long total = 0;
        for (int i = 0; i < resultJobs.size(); ++i) {
            ItemSet set = resultJobs.get(i).resultSet.orElseThrow();
            SpecDetails spec = specList.get(i);
            total += spec.model.calcRating(set) * spec.ratingMultiply;
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

    private void outputResultTwins(Optional<ProposedResults> bestSets, List<SpecDetails> specList) {
        if (bestSets.isPresent()) {
            List<JobInfo> resultJobs = bestSets.get().resultJobs;
            Map<ItemRef, ItemData> common = bestSets.get().chosenMap;
            long rating = multiRating(resultJobs, specList);

            OutputText.println("@@@@@@@@@ BEST SET(s) @@@@@@@@@");
            OutputText.printf("^^^^^^^^^^^^^ %d ^^^^^^^^^^^^^\n", rating);

            for (int i = 0; i < resultJobs.size(); ++i) {
                JobInfo job = resultJobs.get(i);
                ItemSet set = job.resultSet.orElseThrow();
                SpecDetails spec = specList.get(i);
                OutputText.printf("-------------- %s --------------\n", spec.label);
                set.outputSet(spec.model);
                job.printRecorder.outputNow();
            }

            OutputText.println("%%%%%%%%%%%%%%%%%%% COMMON-FORGE %%%%%%%%%%%%%%%%%%%");
            filterCommonActuallyUsed(common, resultJobs);
            common.values().forEach(item -> OutputText.println(item.toString()));

            OutputText.println("%%%%%%%%%%%%%% Main.commonFixedItems %%%%%%%%%%%%%%%");
            common.values().forEach(item -> {
                if (item.reforge == null || item.reforge.isEmpty())
                    OutputText.printf("map.put(%d, List.of(new ReforgeRecipe(null, null))); // %s %s\n", item.ref.itemId(), item.slot, item.name);
                else
                    OutputText.printf("map.put(%d, List.of(new ReforgeRecipe(%s, %s))); // %s %s\n", item.ref.itemId(), item.reforge.source(), item.reforge.dest(), item.slot, item.name);
            });
        } else {
            OutputText.println("@@@@@@@@@ NO BEST SET FOUND @@@@@@@@@");
        }
    }

    public static class SpecDetails {
        final String label;
        final Path gearFile;
        final ModelCombined model;
        final int ratingMultiply;
        final int[] extraItems;
        final int extraItemsUpgradeLevel;
        final boolean challengeScale;
        final Map<Integer, Integer> remapDuplicateId;
        EquipOptionsMap itemOptions;

        public SpecDetails(String label, Path gearFile, ModelCombined model, int ratingMultiply, int[] extraItems, int extraItemsUpgradeLevel, boolean challengeScale, Map<Integer, Integer> remapDuplicateId) {
            this.label = label;
            this.gearFile = gearFile;
            this.model = model;
            this.ratingMultiply = ratingMultiply;
            this.extraItems = extraItems;
            this.extraItemsUpgradeLevel = extraItemsUpgradeLevel;
            this.challengeScale = challengeScale;
            this.remapDuplicateId = remapDuplicateId;
        }

        public void prepareA(List<SpecDetails> allSpecs) {
            itemOptions = ItemUtil.readAndLoad(false, gearFile, model.reforgeRules(), null);
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
            Integer replaceId = remapDuplicateId.get(itemData.ref.itemId());
            if (replaceId != null) {
                return itemData.changeDuplicate(itemData.ref.duplicateNum() + 1);
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
            extraItem = ItemUtil.defaultEnchants(extraItem, model, true);
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
